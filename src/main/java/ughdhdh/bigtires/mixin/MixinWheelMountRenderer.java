package ughdhdh.bigtires.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ughdhdh.bigtires.index.BigTiresComponents;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountRenderer;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.index.OffroadDataComponents;

@Mixin(value = WheelMountRenderer.class, remap = false)
public class MixinWheelMountRenderer {

    private void applyFlip(PoseStack poseStack, Vec3 offset) {
        // 2. поворот
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
        // 3. возвращаемся обратно (двойной обратный offset)
        poseStack.translate(offset.x*2, 0, 0);
    }

    @Inject(
            method = "renderSafe",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/render/SuperByteBuffer;renderInto(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V",
                    remap = false,
                    ordinal = 3
            )
    )
    private void bigtires$flipForPartialModel(
            WheelMountBlockEntity be, float partialTicks,
            PoseStack poseStack, MultiBufferSource buffer, int light, int overlay,
            CallbackInfo ci
    ) {
        ItemStack stack = be.getHeldItem();
        if (stack.isEmpty() || !Boolean.TRUE.equals(stack.get(BigTiresComponents.FLIPPED))) return;
        TireLike tire = stack.get(OffroadDataComponents.TIRE);
        Vec3 offset = tire != null ? tire.offset() : Vec3.ZERO;
        applyFlip(poseStack, offset);
    }

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
            CallbackInfo ci
    ) {
        ItemStack stack = be.getHeldItem();
        if (stack.isEmpty() || !Boolean.TRUE.equals(stack.get(BigTiresComponents.FLIPPED))) return;
        TireLike tire = stack.get(OffroadDataComponents.TIRE);
        Vec3 offset = tire != null ? tire.offset() : Vec3.ZERO;
        applyFlip(poseStack, offset);
    }
}