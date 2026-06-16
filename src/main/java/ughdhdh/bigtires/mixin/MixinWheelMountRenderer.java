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
import ughdhdh.bigtires.WheelColorData;
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
 * Добавляет два поведения при рендере колеса:
 * <ol>
 *   <li><b>Flip</b> — отражение модели, если у предмета компонент {@code FLIPPED}.</li>
 *   <li><b>Color overlay</b> — два дополнительных прохода рендеринга поверх базовой модели:
 *     <ul>
 *       <li>tire overlay × {@code WHEEL_COLOR.tireColor()} (шейдер читает R-канал маски)</li>
 *       <li>rim overlay × {@code WHEEL_COLOR.rimColor()} (шейдер читает G-канал маски)</li>
 *     </ul>
 *   </li>
 * </ol>
 *
 * <h3>Архитектура инъекций</h3>
 * <ul>
 *   <li>{@code HEAD inject} — захватывает {@code buffer}, {@code light}, {@code blockState}
 *       в {@code @Unique} поля (они нужны в @Redirect, который не получает outer params).</li>
 *   <li>{@code @Redirect ordinal=3} — заменяет вызов {@code SuperByteBuffer.renderInto}
 *       для основной модели шины. Внутри: сначала флип (если нужен), затем базовый рендер,
 *       затем два overlay-прохода.</li>
 *   <li>{@code @Inject ordinal=0 ItemRenderer.renderStatic} — флип для plain item (резервный
 *       путь Offroad, когда у шины нет partial model).</li>
 * </ul>
 */
@Mixin(value = WheelMountRenderer.class, remap = false)
public class MixinWheelMountRenderer {

    // Захваченные данные кадра (render thread — thread safety не нужна)
    @Unique private ItemStack     bigtires$item   = ItemStack.EMPTY;
    @Unique private BlockState    bigtires$state  = null;
    @Unique private MultiBufferSource bigtires$buffer = null;
    @Unique private int           bigtires$light  = 0;

    // ── HEAD: захватываем данные текущего вызова renderSafe ──────────────────

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

    // ── REDIRECT ordinal=3: базовый рендер + flip + color overlays ───────────
    //
    // Заменяет вызов SuperByteBuffer.renderInto(...) для основной модели шины.
    // ordinal=3 соответствует рендеру partial model шины в WheelMountRenderer.renderSafe().
    // Предыдущий @Inject (bigtires$flipForPartialModel) УДАЛЁН — его логика перенесена сюда.

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

        // 1. Применяем flip если нужно (перенесено из старого @Inject)
        if (!stack.isEmpty() && Boolean.TRUE.equals(stack.get(BigTiresComponents.FLIPPED))) {
            TireLike tire = stack.get(OffroadDataComponents.TIRE);
            Vec3 offset   = (tire != null) ? tire.offset() : Vec3.ZERO;
            ms.mulPose(Axis.ZP.rotationDegrees(180.0f));
            ms.translate(offset.x * 2, 0, 0);
        }

        // 2. Базовый рендер (оригинальный вызов renderInto)
        buf.renderInto(ms, vc);

        // 3. Color overlays — только если есть цветовые данные
        if (stack.isEmpty()) return;
        WheelColorData colorData = stack.get(BigTiresComponents.WHEEL_COLOR);
        if (colorData == null) return;

        TireLike tireLike = stack.get(OffroadDataComponents.TIRE);
        if (tireLike == null || tireLike.model().isEmpty()) return;

        ResourceLocation baseModelRL = tireLike.model().get();
        PartialModel     overlayModel = WheelColorOverlayRegistry.getOverlayModel(baseModelRL);
        ResourceLocation maskTexture  = WheelColorOverlayRegistry.getMaskTexture(baseModelRL);
        if (overlayModel == null || maskTexture == null) return;

        bigtires$renderOverlays(ms, overlayModel, maskTexture, colorData);
    }

    /** Два overlay-прохода: шина (R) и диск (G). */
    @Unique
    private void bigtires$renderOverlays(PoseStack ms, PartialModel overlayModel,
                                         ResourceLocation maskTexture, WheelColorData colorData) {
        // Tire overlay — шейдер читает R-канал color_mask.png
        SuperByteBuffer tireOv = CachedBuffers.partial(overlayModel, bigtires$state);
        tireOv.light(bigtires$light).translate(-0.5f, 0f, -0.5f);
        int tc = colorData.tireColor();
        tireOv.color((tc >> 16) & 0xFF, (tc >> 8) & 0xFF, tc & 0xFF, 255);
        tireOv.renderInto(ms, bigtires$buffer.getBuffer(WheelColorRenderType.tire(maskTexture)));

        // Rim overlay — шейдер читает G-канал color_mask.png
        SuperByteBuffer rimOv = CachedBuffers.partial(overlayModel, bigtires$state);
        rimOv.light(bigtires$light).translate(-0.5f, 0f, -0.5f);
        int rc = colorData.rimColor();
        rimOv.color((rc >> 16) & 0xFF, (rc >> 8) & 0xFF, rc & 0xFF, 255);
        rimOv.renderInto(ms, bigtires$buffer.getBuffer(WheelColorRenderType.rim(maskTexture)));
    }

    // ── INJECT: flip для plain item (путь без partial model) ─────────────────

    @Inject(
            method = "renderSafe",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderStatic(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;IILcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;I)V",
                    remap = true,
                    ordinal = 0
            )
    )
    private void bigtires$flipForPlainItem(
            WheelMountBlockEntity be, float partialTicks,
            PoseStack poseStack, MultiBufferSource buffer, int light, int overlay,
            CallbackInfo ci) {
        ItemStack stack = be.getHeldItem();
        if (stack.isEmpty() || !Boolean.TRUE.equals(stack.get(BigTiresComponents.FLIPPED))) return;
        TireLike tire = stack.get(OffroadDataComponents.TIRE);
        Vec3 offset   = (tire != null) ? tire.offset() : Vec3.ZERO;
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
        poseStack.translate(offset.x * 2, 0, 0);
    }
}
