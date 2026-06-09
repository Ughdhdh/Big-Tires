package ughdhdh.bigtires.content.blocks.fixed_mount;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.index.OffroadDataComponents;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import ughdhdh.bigtires.index.BigTiresComponents;

/**
 * Рендерер Fixed Wheel Mount.
 *
 * Отличия от стандартного offroad WheelMountRenderer:
 *  • Нет анимации подвески (extension всегда 0)
 *  • Колесо на 0.5 блока выше (Y offset = +0.5 в начальном translate)
 *  • Нет рендера телескопа/пружины
 */
public class FixedWheelMountRenderer
        extends KineticBlockEntityRenderer<FixedWheelMountBlockEntity> {

    // Расстояние от центра блока до центра колеса (стандарт offroad)
    private static final double H_WHEEL = 22.0 / 16.0;

    public FixedWheelMountRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(FixedWheelMountBlockEntity be, float partialTicks,
                              PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {

        final BlockState state = getRenderedBlockState(be);
        renderRotatingBuffer(be, getRotatedModel(be, state), ms,
                buffer.getBuffer(getRenderType(be, state)), light);

        final VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());
        final Direction direction = be.getBlockState()
                .getValue(BlockStateProperties.HORIZONTAL_FACING)
                .getOpposite();

        ms.pushPose();
        TransformStack.of(ms)
                .center()
                .rotateYDegrees(AngleHelper.horizontalAngle(direction))
                .rotateXDegrees(AngleHelper.verticalAngle(direction))
                .uncenter();

        ms.pushPose();

        ms.translate(0.0, 0.0, 26.0 / 16.0 - H_WHEEL);

        // Рулёжка (steering from redstone signal)
        final float pivotZ = (float) (-H_WHEEL + 6.0 / 16.0);
        ms.translate(0.5, 0.5, 0.5);
        ms.rotateAround(
                Axis.YP.rotation((float) be.getLerpedYaw(partialTicks)),
                0.0F, 0.0F, pivotZ);
        ms.translate(-0.5, -0.5, -0.5);

        // Финальная позиция у оси колеса
        ms.translate(0.5, 0.5, 0.5);
        ms.translate(0.0, 0.0, -26.0f / 16.0f);

        // Вращение колеса
        final double wheelAngle = -be.getLerpedAngle(partialTicks)
                * (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0 : -1.0)
                * (direction.getAxis() == Direction.Axis.X ? 1.0 : -1.0);
        ms.mulPose(Axis.ZP.rotation((float) wheelAngle));

        // Рендер шины
        final ItemStack itemStack = be.getHeldItem();
        final TireLike tireLike = itemStack.get(OffroadDataComponents.TIRE);
        if (tireLike != null) {
            final var rot = tireLike.rotation();
            ms.mulPose(Axis.XP.rotation((float) Math.toRadians(rot.x)));
            ms.mulPose(Axis.YP.rotation((float) Math.toRadians(rot.y)));
            ms.mulPose(Axis.ZP.rotation((float) Math.toRadians(rot.z)));
            ms.translate(tireLike.offset().x, tireLike.offset().y, tireLike.offset().z);

            if (tireLike.model().isPresent()) {
                final SuperByteBuffer wheel = CachedBuffers.partial(
                        PartialModel.of(tireLike.model().get()), state);
                wheel.light(light).translate(-0.5f, 0f, -0.5f);
                if (Boolean.TRUE.equals(itemStack.get(BigTiresComponents.FLIPPED))) {
                    ms.mulPose(Axis.ZP.rotationDegrees(180.0f));
                    if (itemStack.has(BigTiresComponents.TIRE_PHYSICS)) {
                        ms.translate(1, 0, 0);
                    }
                }
                wheel.renderInto(ms, vb);
            } else {
                if (Boolean.TRUE.equals(itemStack.get(BigTiresComponents.FLIPPED))) {
                    ms.mulPose(Axis.ZP.rotationDegrees(180.0f));
                    if (itemStack.has(BigTiresComponents.TIRE_PHYSICS)) {
                        ms.translate(1, 0, 0);
                    }
                }
                Minecraft.getInstance().getItemRenderer().renderStatic(
                        itemStack, ItemDisplayContext.NONE,
                        light, overlay, ms, buffer, be.getLevel(), 0);
            }
        }

        ms.popPose();
        ms.popPose();
    }

    @Override
    protected SuperByteBuffer getRotatedModel(FixedWheelMountBlockEntity te, BlockState state) {
        return CachedBuffers.partialFacing(
                AllPartialModels.SHAFT_HALF, te.getBlockState(),
                te.getBlockState()
                        .getValue(BlockStateProperties.HORIZONTAL_FACING)
                        .getOpposite());
    }

    @Override
    public int getViewDistance() { return 512; }
}
