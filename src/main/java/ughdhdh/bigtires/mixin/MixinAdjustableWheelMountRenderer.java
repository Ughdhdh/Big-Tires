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
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.index.OffroadDataComponents;

/**
 * Compat-mixin для мода "Suspension Wrench" (dev.ughdhdh.suspension_wrench).
 */
@Mixin(targets = "dev.ughdhdh.suspension_wrench.renderer.AdjustableWheelMountRenderer", remap = false)
public class MixinAdjustableWheelMountRenderer {

    @Unique private ItemStack bigtires$item = ItemStack.EMPTY;
    @Unique private BlockState bigtires$state = null;
    @Unique private int bigtires$light = 0;

    @Inject(method = "renderSafe", at = @At("HEAD"), remap = false, require = 0)
    private void bigtires$captureData(
            WheelMountBlockEntity be, float partialTicks,
            PoseStack ms, MultiBufferSource buffer, int light, int overlay,
            CallbackInfo ci) {
        bigtires$item  = be.getHeldItem();
        bigtires$state = be.getBlockState();
        bigtires$light = light;
    }

    @Redirect(
            method = "renderSafe",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/render/SuperByteBuffer;renderInto(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V",
                    ordinal = 3,
                    remap = false
            ),
            remap = false,
            require = 0
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
            ResourceLocation baseModelRL = tireLike.model().get();
            Integer tireColor = stack.get(BigTiresComponents.TIRE_COLOR);
            Integer rimColor  = stack.get(BigTiresComponents.RIM_COLOR);

            bigtires$renderTintedPart(ms, vc,
                    tireColor != null ? BigTiresPartialModels.tireVariantDyed(baseModelRL)
                                       : BigTiresPartialModels.tireVariant(baseModelRL),
                    tireColor);
            bigtires$renderTintedPart(ms, vc,
                    rimColor != null ? BigTiresPartialModels.rimVariantDyed(baseModelRL)
                                     : BigTiresPartialModels.rimVariant(baseModelRL),
                    rimColor);
            bigtires$renderTintedPart(ms, vc, BigTiresPartialModels.neutralVariant(baseModelRL), null);
        } else {
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
}
