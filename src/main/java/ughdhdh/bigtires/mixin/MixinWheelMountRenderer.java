package ughdhdh.bigtires.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ughdhdh.bigtires.client.WheelColorOverlayRegistry;
import ughdhdh.bigtires.client.WheelColorRenderType;
import ughdhdh.bigtires.index.BigTiresComponents;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountRenderer;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.index.OffroadDataComponents;

/**
 * Mixin в WheelMountRenderer (мод Offroad).
 * <p>
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
 *       рендерится обычным способом item-модели).</li>
 * </ol>
 * Реальный порядок вызовов {@code SuperByteBuffer.renderInto(...)} внутри
 * {@code renderSafe()} (проверено по декомпилированному исходнику, а НЕ по
 * догадке): {@code teleOuter}=0, {@code teleInner}=1, {@code teleMount}=2,
 * {@code wheel}=3 (только в ветке 1 выше), {@code springTop}=4, {@code springMiddle}=5,
 * {@code springBottom}=6, {@code diodeLeft}=7, {@code diodeRight}=8.
 * <p>
 * Раньше здесь стоял {@code ordinal=4}, который в реальности перехватывал
 * {@code springTop} (пружину подвески), а не колесо — отсюда баги "сдвинутая
 * модель" (overlay рисовался в системе координат пружины) и "цвет не виден"
 * (для колёс без модели никакой {@code renderInto} для шины вообще не вызывается,
 * поэтому оба ordinal варианта тут были одинаково бесполезны). Исправлено на
 * {@code ordinal=3}, и добавлен отдельный inject в ветку 2 для колёс без модели
 * (собственные тайры Offroad).
 * <p>
 * <h3>Ключ реестра — item id, а не model RL</h3>
 * Раз у колёс без модели {@code TireLike.model()} всегда пуст, ключевать реестр
 * по нему для них в принципе невозможно. Реестр теперь ключуется по registry id
 * самого предмета ({@code BuiltInRegistries.ITEM.getKey(stack.getItem())}) —
 * этот идентификатор существует всегда, независимо от способа рендера.
 */
@Mixin(value = WheelMountRenderer.class, remap = false)
public class MixinWheelMountRenderer {

    @Unique private ItemStack bigtires$item = ItemStack.EMPTY;
    @Unique private BlockState bigtires$state = null;
    @Unique private MultiBufferSource bigtires$buffer = null;
    @Unique private int bigtires$light = 0;

    @Inject(method = "renderSafe", at = @At("HEAD"), remap = false)
    private void bigtires$captureData(
            WheelMountBlockEntity be, float partialTicks,
            PoseStack ms, MultiBufferSource buffer, int light, int overlay,
            CallbackInfo ci) {
        bigtires$item   = be.getHeldItem();
        bigtires$state  = be.getBlockState();
        bigtires$buffer = buffer;
        bigtires$light  = light;
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

        buf.renderInto(ms, vc);

        bigtires$renderColorOverlaysIfPresent(ms, stack);
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

        // Инжект стоит ДО вызова renderStatic — offset уже применён к poseStack
        // самим Offroad (см. renderSafe: ms.translate(tireLike.offset()...) идёт
        // непосредственно перед этим вызовом в обеих ветках). Значит наш overlay
        // рисуется в той же точке, где окажется и сама (ванильно отрендеренная) шина.
        bigtires$renderColorOverlaysIfPresent(poseStack, stack);
    }

    // ── Общая логика покраски для обеих веток ─────────────────────────────────

    @Unique
    private void bigtires$renderColorOverlaysIfPresent(PoseStack ms, ItemStack stack) {
        Integer tireColor = stack.get(BigTiresComponents.TIRE_COLOR);
        Integer rimColor  = stack.get(BigTiresComponents.RIM_COLOR);
        if (tireColor == null && rimColor == null) return;

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!WheelColorOverlayRegistry.has(itemId)) return;

        bigtires$renderOverlays(ms, itemId, tireColor, rimColor);
    }

    @Unique
    private void bigtires$renderOverlays(PoseStack ms, ResourceLocation itemId,
                                         @Nullable Integer tireColor,
                                         @Nullable Integer rimColor) {
        if (tireColor != null) {
            PartialModel tireModel = WheelColorOverlayRegistry.getTireModel(itemId);
            ResourceLocation tireMask = WheelColorOverlayRegistry.getTireMaskTexture(itemId);
            if (tireModel != null && tireMask != null) {
                SuperByteBuffer buf = CachedBuffers.partial(tireModel, bigtires$state);
                buf.light(bigtires$light).translate(-0.5f, 0f, -0.5f);
                buf.color((tireColor >> 16) & 0xFF, (tireColor >> 8) & 0xFF, tireColor & 0xFF, 255);
                buf.renderInto(ms, bigtires$buffer.getBuffer(WheelColorRenderType.overlay(tireMask)));
            }
        }
        if (rimColor != null) {
            PartialModel rimModel = WheelColorOverlayRegistry.getRimModel(itemId);
            ResourceLocation rimMask = WheelColorOverlayRegistry.getRimMaskTexture(itemId);
            if (rimModel != null && rimMask != null) {
                SuperByteBuffer buf = CachedBuffers.partial(rimModel, bigtires$state);
                buf.light(bigtires$light).translate(-0.5f, 0f, -0.5f);
                buf.color((rimColor >> 16) & 0xFF, (rimColor >> 8) & 0xFF, rimColor & 0xFF, 255);
                buf.renderInto(ms, bigtires$buffer.getBuffer(WheelColorRenderType.overlay(rimMask)));
            }
        }
    }
}
