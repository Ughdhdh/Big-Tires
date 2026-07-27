package ughdhdh.bigtires.content.blocks.motorcycle_mount;

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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import ughdhdh.bigtires.index.BigTiresComponents;
import ughdhdh.bigtires.index.BigTiresPartialModels;

public class MotorcycleWheelMountRenderer
        extends KineticBlockEntityRenderer<MotorcycleWheelMountBlockEntity> {

    public MotorcycleWheelMountRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(MotorcycleWheelMountBlockEntity be, float partialTicks,
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

        final ItemStack itemStack = be.getHeldItem();
        final TireLike tireLike = itemStack.get(OffroadDataComponents.TIRE);

        // Центр колеса = radius + 1 блок от лицевой части
        // F1_Z = 0.5 - H_WHEEL  =>  H_WHEEL = radius + 1.5
        final double H_WHEEL = (tireLike != null)
                ? tireLike.radius() + 1.5 : 22.0 / 16.0;
        final double verticalPos = -be.getLerpedExtension(partialTicks);

        ms.pushPose();

        // начальный translate к позиции колеса
        ms.translate(0.0, verticalPos, 26.0 / 16.0 - H_WHEEL);

        // Рулёжка
        final float pivotZ = (float) (-H_WHEEL + 6.0 / 16.0);
        ms.translate(0.5, 0.5, 0.5);
        ms.rotateAround(
                Axis.YP.rotation((float) be.getLerpedYaw(partialTicks)),
                0.0F, 0.0F, pivotZ);
        ms.translate(-0.5, -0.5, -0.5);

        // Переход к оси колеса
        ms.translate(0.5, 0.5, 0.5);
        ms.translate(0.0, 0.0, -26.0f / 16.0f);
        ms.mulPose(Axis.YP.rotationDegrees(-90f));

        // Вращение колеса
        final double wheelAngle = -be.getLerpedAngle(partialTicks)
                * (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0 : -1.0)
                * (direction.getAxis() == Direction.Axis.X ? 1.0 : -1.0);
        ms.mulPose(Axis.ZP.rotation((float) wheelAngle));

        // Рендер шины
        if (tireLike != null) {
            final var rot = tireLike.rotation();
            ms.mulPose(Axis.XP.rotation((float) Math.toRadians(rot.x)));
            ms.mulPose(Axis.YP.rotation((float) Math.toRadians(rot.y)));
            ms.mulPose(Axis.ZP.rotation((float) Math.toRadians(rot.z)));
            ms.translate(tireLike.offset().x, tireLike.offset().y, tireLike.offset().z);

            if (tireLike.model().isPresent()) {
                final ResourceLocation baseModelRL = tireLike.model().get();
                final Integer tireColor = itemStack.get(BigTiresComponents.TIRE_COLOR);
                final Integer rimColor  = itemStack.get(BigTiresComponents.RIM_COLOR);

                // Flip если нужно — ДО построения буферов, чтобы координаты трансформации
                // совпали у всех трёх под-моделей.
                if (Boolean.TRUE.equals(itemStack.get(BigTiresComponents.FLIPPED))) {
                    ms.mulPose(Axis.ZP.rotationDegrees(180.0f));
                    if (itemStack.has(BigTiresComponents.TIRE_PHYSICS)) {
                        ms.translate(1, 0, 0);
                    }
                }

                // Раздельные под-буферы (см. BigTiresPartialModels): если цвет задан —
                // рендерим ДЕСАТУРИРОВАННЫЙ ("_dyed") вариант текстуры с тинтом (чистый
                // colorize-эффект без смешивания с родным оттенком текстуры); если нет —
                // обычный вариант с оригинальной текстурой, без тинта (естественный вид).
                // "neutral" (например ось у huge_rowing_tire) — всегда как есть, без тинта.
                renderTintedPart(ms, vb,
                        tireColor != null ? BigTiresPartialModels.tireVariantDyed(baseModelRL)
                                           : BigTiresPartialModels.tireVariant(baseModelRL),
                        state, light, tireColor);
                renderTintedPart(ms, vb,
                        rimColor != null ? BigTiresPartialModels.rimVariantDyed(baseModelRL)
                                         : BigTiresPartialModels.rimVariant(baseModelRL),
                        state, light, rimColor);
                renderTintedPart(ms, vb, BigTiresPartialModels.neutralVariant(baseModelRL), state, light, null);

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

    /**
     * Рендерит одну под-модель (шина/диск/нейтральная часть), опционально
     * умножая её цвет на {@code colorArgb} (RGB без альфы — альфа всегда 255).
     * {@code null} = без покраски (естественный цвет текстуры, множитель белый).
     */
    private static void renderTintedPart(PoseStack ms, VertexConsumer vb, PartialModel part,
                                         BlockState state, int light, Integer colorArgb) {
        final SuperByteBuffer buf = CachedBuffers.partial(part, state);
        buf.light(light).translate(-0.5f, 0f, -0.5f);
        if (colorArgb != null) {
            buf.color((colorArgb >> 16) & 0xFF, (colorArgb >> 8) & 0xFF, colorArgb & 0xFF, 255);
        }
        buf.renderInto(ms, vb);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(MotorcycleWheelMountBlockEntity te, BlockState state) {
        return CachedBuffers.partialFacing(
                AllPartialModels.SHAFT_HALF, te.getBlockState(),
                te.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite());
    }

    @Override
    public int getViewDistance() { return 512; }
}
