package ughdhdh.bigtires.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ughdhdh.bigtires.index.BigTiresComponents;
import ughdhdh.bigtires.index.BigTiresPartialModels;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountRenderer;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.index.OffroadDataComponents;

/**
 * Mixin в WheelMountRenderer (мод Offroad).
 * <p>
 * Две задачи на ОРИГИНАЛЬНОМ (не {@code motorcycle_wheel_mount}) Offroad-креплении:
 * <ol>
 *   <li>Разворот на 180° ({@code BigTiresComponents.FLIPPED}).</li>
 *   <li>Покраска колеса BigTires в {@code TIRE_COLOR}/{@code RIM_COLOR} — см.
 *       {@link #bigtires$wheelRenderWithExtras}. {@code SuperByteBuffer.renderInto}
 *       не резолвит tintIndex сам по себе (это не полноценный блок-рендер с
 *       {@code BlockColors}, а прямая заливка уже запечённых вершин), поэтому
 *       вместо базового (некрашенного) буфера {@code buf}, который строит само
 *       Offroad, рендерятся ТРИ отдельные под-модели с уже применённым цветом —
 *       см. {@link BigTiresPartialModels}.</li>
 * </ol>
 * <h3>Два раздельных пути рендера базовой шины в оригинальном коде Offroad</h3>
 * Судя по реальному исходнику {@code WheelMountRenderer.renderSafe()}, шина
 * рендерится ОДНИМ из двух взаимоисключающих способов:
 * <ol>
 *   <li>{@code tireLike.model().isPresent()} → своя OBJ-модель через
 *       {@code SuperByteBuffer.renderInto(...)} — этот путь используют колёса
 *       BigTires (у них {@code TireLike.model()} явно задан в {@code BigTireLikes}).</li>
 *   <li>{@code tireLike.model().isEmpty()} → ванильный
 *       {@code ItemRenderer.renderStatic(...)} — этот путь используют СОБСТВЕННЫЕ
 *       тайры Offroad (у них модель не заведена через {@code TireLike}, а
 *       рендерится обычным способом item-модели; покраска BigTires для них
 *       недоступна).</li>
 * </ol>
 * Реальный порядок вызовов {@code SuperByteBuffer.renderInto(...)} внутри
 * {@code renderSafe()} (проверено по декомпилированному исходнику, а НЕ по
 * догадке): {@code teleOuter}=0, {@code teleInner}=1, {@code teleMount}=2,
 * {@code wheel}=3 (только в ветке 1 выше), {@code springTop}=4, {@code springMiddle}=5,
 * {@code springBottom}=6, {@code diodeLeft}=7, {@code diodeRight}=8.
 */
@Mixin(value = WheelMountRenderer.class, remap = false)
public class MixinWheelMountRenderer {

    @Unique private ItemStack bigtires$item = ItemStack.EMPTY;
    @Unique private BlockState bigtires$state = null;
    @Unique private int bigtires$light = 0;

    @Inject(method = "renderSafe", at = @At("HEAD"), remap = false)
    private void bigtires$captureData(
            WheelMountBlockEntity be, float partialTicks,
            PoseStack ms, MultiBufferSource buffer, int light, int overlay,
            CallbackInfo ci) {
        bigtires$item  = be.getHeldItem();
        bigtires$state = be.getBlockState();
        bigtires$light = light;
    }

    // ── ПУТЬ 1: колёса СО своей моделью (BigTires) — REDIRECT ordinal=3 ───────
    // Настоящий "wheel" renderInto-вызов, подтверждено по декомпилированному исходнику.

    @Redirect(
            method = "renderSafe",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/render/SuperByteBuffer;renderInto(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V",
                    ordinal = 3,
                    remap = false
            ),
            remap = false
    )
    private void bigtires$wheelRenderWithExtras(SuperByteBuffer buf, PoseStack ms, VertexConsumer vc) {
        ItemStack stack = bigtires$item;

        if (!stack.isEmpty() && Boolean.TRUE.equals(stack.get(BigTiresComponents.FLIPPED))) {
            TireLike tire = stack.get(OffroadDataComponents.TIRE);
            Vec3 offset   = (tire != null) ? tire.offset() : Vec3.ZERO;
            ms.mulPose(Axis.ZP.rotationDegrees(180.0f));
            ms.translate(offset.x * 2, 0, 0);
        }

        TireLike tireLike = stack.isEmpty() ? null : stack.get(OffroadDataComponents.TIRE);
        if (tireLike != null && tireLike.model().isPresent()) {
            // Игнорируем некрашеный buf, который построило само Offroad, и
            // рендерим три под-модели BigTires с покраской (см. javadoc класса).
            ResourceLocation baseModelRL = tireLike.model().get();
            Integer tireColor = stack.get(BigTiresComponents.TIRE_COLOR);
            Integer rimColor  = stack.get(BigTiresComponents.RIM_COLOR);

            bigtires$renderTintedPart(ms, vc, BigTiresPartialModels.tireVariant(baseModelRL), tireColor);
            bigtires$renderTintedPart(ms, vc, BigTiresPartialModels.rimVariant(baseModelRL), rimColor);
            bigtires$renderTintedPart(ms, vc, BigTiresPartialModels.neutralVariant(baseModelRL), null);
        } else {
            // Подстраховка: модели нет (не должно происходить в этой ветке) — рендерим как раньше.
            buf.renderInto(ms, vc);
        }
    }

    @Unique
    private void bigtires$renderTintedPart(PoseStack ms, VertexConsumer vc,
                                           PartialModel part,
                                           Integer colorArgb) {
        SuperByteBuffer partBuf = CachedBuffers.partial(part, bigtires$state);
        partBuf.light(bigtires$light).translate(-0.5f, 0f, -0.5f);
        if (colorArgb != null) {
            partBuf.color((colorArgb >> 16) & 0xFF, (colorArgb >> 8) & 0xFF, colorArgb & 0xFF, 255);
        }
        partBuf.renderInto(ms, vc);
    }

    // ── ПУТЬ 2: колёса БЕЗ модели (собственные тайры Offroad) — INJECT ────────
    // Рендерятся через ItemRenderer.renderStatic(), а не renderInto — редирект
    // тут не сработает никогда, нужен отдельный inject прямо перед этим вызовом.

    @Inject(
            method = "renderSafe",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderStatic(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;IILcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;I)V",
                    remap = true,
                    ordinal = 0
            )
    )
    private void bigtires$plainItemRenderExtras(
            WheelMountBlockEntity be, float partialTicks,
            PoseStack poseStack, MultiBufferSource buffer, int light, int overlay,
            CallbackInfo ci) {
        ItemStack stack = be.getHeldItem();
        if (stack.isEmpty()) return;

        if (Boolean.TRUE.equals(stack.get(BigTiresComponents.FLIPPED))) {
            TireLike tire = stack.get(OffroadDataComponents.TIRE);
            Vec3 offset   = (tire != null) ? tire.offset() : Vec3.ZERO;
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
            poseStack.translate(offset.x * 2, 0, 0);
        }
    }
}
