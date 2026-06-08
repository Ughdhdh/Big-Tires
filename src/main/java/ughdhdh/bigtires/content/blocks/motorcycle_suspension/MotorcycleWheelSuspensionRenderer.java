package ughdhdh.bigtires.content.blocks.motorcycle_suspension;

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

public class MotorcycleWheelSuspensionRenderer
        extends KineticBlockEntityRenderer<MotorcycleWheelSuspensionBlockEntity> {

    public MotorcycleWheelSuspensionRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(MotorcycleWheelSuspensionBlockEntity be, float partialTicks,
                              PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {

        final BlockState state = getRenderedBlockState(be);
        renderRotatingBuffer(be, getRotatedModel(be, state), ms,
                buffer.getBuffer(getRenderType(be, state)), light);

        final VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());

        // direction = противоположная facing: после rotateYDegrees локальный +Z
        // указывает в сторону facing (туда, где находится колесо)
        final Direction direction = be.getBlockState()
                .getValue(BlockStateProperties.HORIZONTAL_FACING)
                .getOpposite();

        // ── Главный поворот по facing (без лишних -90° на весь фрейм!) ────────
        ms.pushPose();
        TransformStack.of(ms)
                .center()
                .rotateYDegrees(AngleHelper.horizontalAngle(direction))
                .rotateXDegrees(AngleHelper.verticalAngle(direction))
                .uncenter();

        final ItemStack itemStack = be.getHeldItem();
        final TireLike tireLike = itemStack.get(OffroadDataComponents.TIRE);

        // ── Динамическая горизонтальная позиция ──────────────────────────────
        // Центр колеса = radius + 1 блок от лицевой части маунта.
        // Лицевая часть на 0.5 блока от центра блока → от центра = radius + 1.5.
        // Формула позиции: F1_Z = 0.5 - H_WHEEL → H_WHEEL = radius + 1.5
        final double H_WHEEL = (tireLike != null)
                ? tireLike.radius() + 1.5
                : 22.0 / 16.0; // дефолт без колеса = offroad-стандарт

        // Ход подвески: 0 = прижато, 0.65 = в воздухе
        final double verticalPos = -be.getLerpedExtension(partialTicks);

        ms.pushPose();

        // Шаг 1: начальная позиция (до рулёжки и финального центрирования)
        ms.translate(0.0, verticalPos, 26.0 / 16.0 - H_WHEEL);

        // ── Рулёжка: поворот вокруг основания вилки ──────────────────────────
        final float pivotZ = (float) (-H_WHEEL + 6.0 / 16.0);
        ms.translate(0.5, 0.5, 0.5);
        ms.rotateAround(
                Axis.YP.rotation((float) be.getLerpedYaw(partialTicks)),
                0.0F, 0.0F, pivotZ);
        ms.translate(-0.5, -0.5, -0.5); // ← без этого центрирование сломано

        // ── Переход к центру колеса (стандарт как в offroad) ─────────────────
        ms.translate(0.5, 0.5, 0.5);
        ms.translate(0.0, 0.0, -26.0f / 16.0f);

        // ── Ориентация для мотоциклетного колеса ─────────────────────────────
        // Применяем ЗДЕСЬ (у центра колеса, не глобально!):
        //   • колесо становится параллельно лицевой части маунта
        //   • ось вращения для анимации катания становится корректной
        ms.mulPose(Axis.YP.rotationDegrees(-90f));

        // ── Вращение колеса ───────────────────────────────────────────────────
        final double wheelAngle = -be.getLerpedAngle(partialTicks)
                * (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0 : -1.0)
                * (direction.getAxis() == Direction.Axis.X ? 1.0 : -1.0);
        ms.mulPose(Axis.ZP.rotation((float) wheelAngle));

        // ── Рендер шины ───────────────────────────────────────────────────────
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
                }
                wheel.renderInto(ms, vb);
            } else {
                if (Boolean.TRUE.equals(itemStack.get(BigTiresComponents.FLIPPED))) {
                    ms.mulPose(Axis.ZP.rotationDegrees(180.0f));
                }
                Minecraft.getInstance().getItemRenderer().renderStatic(
                        itemStack, ItemDisplayContext.NONE,
                        light, overlay, ms, buffer, be.getLevel(), 0);
            }
        }

        ms.popPose(); // позиция колеса
        ms.popPose(); // главный поворот
    }

    @Override
    protected SuperByteBuffer getRotatedModel(MotorcycleWheelSuspensionBlockEntity te,
                                               BlockState state) {
        return CachedBuffers.partialFacing(
                AllPartialModels.SHAFT_HALF, te.getBlockState(),
                te.getBlockState()
                        .getValue(BlockStateProperties.HORIZONTAL_FACING)
                        .getOpposite());
    }

    @Override
    public int getViewDistance() { return 512; }
}
