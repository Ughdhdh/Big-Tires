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

/**
 * Рендерер Motorcycle Wheel Suspension.
 *
 * Что рисует:
 *   1. Вращающийся шафт (стандартный полушафт Create)
 *   2. Колесо — с анимацией подвески, рулёжки и вращения
 *
 * Что НЕ рисует:
 *   - телескоп и пружину из offroad (они не нужны — визуал вилки
 *     уже встроен в статичную модель блока block.json)
 *
 * Позиция колеса:
 *   horizontalWheelPosition = 22/16 блока от центра блока по оси
 *   verticalWheelPosition   = -getLerpedExtension() (ход подвески, 0..0.65)
 *
 * Ось рулёжки:
 *   pivotZ = -horizontalWheelPosition + 6/16  → вращение вокруг основания вилки
 */
public class MotorcycleWheelSuspensionRenderer
        extends KineticBlockEntityRenderer<MotorcycleWheelSuspensionBlockEntity> {

    // Расстояние центра колеса от центра блока (вдоль оси facing)
    private static final double H_WHEEL = 22.0 / 16.0;

    public MotorcycleWheelSuspensionRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void renderSafe(MotorcycleWheelSuspensionBlockEntity be, float partialTicks,
                              PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {

        final BlockState state = getRenderedBlockState(be);

        // 1. ── Шафт (вращается с кинетической сетью) ─────────────────────────
        renderRotatingBuffer(be,
                getRotatedModel(be, state),
                ms, buffer.getBuffer(getRenderType(be, state)), light);

        final VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());

        // direction — сторона, КУДА смотрит facing (туда и едет колесо)
        // getOpposite() нужен для rotateYDegrees → local +Z = направление facing
        final Direction direction = be.getBlockState()
                .getValue(BlockStateProperties.HORIZONTAL_FACING)
                .getOpposite();

        // 2. ── Глобальный поворот pose по facing ──────────────────────────────
        ms.pushPose();
        TransformStack.of(ms)
                .center()
                .rotateYDegrees(AngleHelper.horizontalAngle(direction))
                .rotateXDegrees(AngleHelper.verticalAngle(direction))
                .uncenter();

        // 3. ── Вычисляем позицию колеса ───────────────────────────────────────
        // extension идёт от 0 (колесо прижато) до 0.65 (колесо в воздухе)
        final double verticalPos = -be.getLerpedExtension(partialTicks);

        // Сдвигаем позу к позиции колеса по горизонтали и по высоте подвески:
        //   Z = 26/16 - H_WHEEL  → ось колеса в local space
        //   Y = verticalPos      → ход подвески (опускается вниз)
        ms.pushPose();
        ms.translate(0.0, verticalPos, 26.0 / 16.0 - H_WHEEL);

        // 4. ── Рулёжка: поворот вокруг основания вилки ───────────────────────
        // pivotZ отрицательный → точка поворота "позади" колеса (у основания вилки)
        // Offroad использует (-H_WHEEL + 6/16), мы делаем то же самое
        final float pivotZ = (float) (-H_WHEEL + 6.0 / 16.0);
        ms.translate(0.5, 0.5, 0.5);
        ms.rotateAround(
                Axis.YP.rotation((float) be.getLerpedYaw(partialTicks)),
                0.0F, 0.0F, pivotZ
        );
        ms.translate(-0.5, -0.5, -0.5);

        // 5. ── Вращение колеса ────────────────────────────────────────────────
        // Переносимся к оси колеса, применяем угол вращения из getLerpedAngle
        ms.translate(0.5, 0.5, 0.5);
        ms.translate(0.0, 0.0, -26.0f / 16.0f);

        // Знак зависит от оси и направления facing (чтобы колесо крутилось правильно)
        final double wheelAngle = -be.getLerpedAngle(partialTicks)
                * (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0 : -1.0)
                * (direction.getAxis() == Direction.Axis.X ? 1.0 : -1.0);
        ms.mulPose(Axis.ZP.rotation((float) wheelAngle));

        // 6. ── Рендер шины ────────────────────────────────────────────────────
        final ItemStack itemStack = be.getHeldItem();
        final TireLike tireLike = itemStack.get(OffroadDataComponents.TIRE);

        if (tireLike != null) {
            // Применяем rotation/offset из TireLike
            final var rot = tireLike.rotation();
            ms.mulPose(Axis.XP.rotation((float) Math.toRadians(rot.x)));
            ms.mulPose(Axis.YP.rotation((float) Math.toRadians(rot.y)));
            ms.mulPose(Axis.ZP.rotation((float) Math.toRadians(rot.z)));
            ms.translate(tireLike.offset().x, tireLike.offset().y, tireLike.offset().z);

            if (tireLike.model().isPresent()) {
                // Partial model шины (OBJ)
                final SuperByteBuffer wheel = CachedBuffers.partial(
                        PartialModel.of(tireLike.model().get()), state);
                // translate(-0.5, 0, -0.5): модели шин отцентрированы по X и Z
                wheel.light(light).translate(-0.5f, 0f, -0.5f).renderInto(ms, vb);
            } else {
                // Fallback: рендер как item (если модели нет)
                Minecraft.getInstance().getItemRenderer().renderStatic(
                        itemStack, ItemDisplayContext.NONE,
                        light, overlay, ms, buffer,
                        be.getLevel(), 0);
            }
        }

        ms.popPose(); // ← закрываем translate к оси колеса + рулёжку
        ms.popPose(); // ← закрываем глобальный поворот по facing
    }

    // ─────────────────────────────────────────────────────────────────────────

    /** Полушафт Create — вращается вместе с кинетической сетью. */
    @Override
    protected SuperByteBuffer getRotatedModel(MotorcycleWheelSuspensionBlockEntity te,
                                               BlockState state) {
        return CachedBuffers.partialFacing(
                AllPartialModels.SHAFT_HALF, te.getBlockState(),
                te.getBlockState()
                        .getValue(BlockStateProperties.HORIZONTAL_FACING)
                        .getOpposite());
    }

    /** Дистанция рендера — такая же как у стандартного WheelMount. */
    @Override
    public int getViewDistance() {
        return 512;
    }
}
