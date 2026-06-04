package ughdhdh.bigtires.content.blocks.motorcycle_suspension;

import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockEntity для Motorcycle Wheel Suspension.
 * Вся физика (подвеска, рулёжка, вращение колеса) работает из родителя.
 *
 * getLerpedYaw() поднят до public — это нужно потому что рендерер находится
 * в другом пакете и не может обратиться к protected методу напрямую.
 * В Java повышение видимости в override — корректная операция.
 */
public class MotorcycleWheelSuspensionBlockEntity extends WheelMountBlockEntity {

    public MotorcycleWheelSuspensionBlockEntity(BlockEntityType<?> type,
                                                BlockPos pos,
                                                BlockState state) {
        super(type, pos, state);
    }

    /**
     * Интерполированный угол рулёжки (в радианах).
     * Поднят до public для доступа из рендерера.
     */
    @Override
    public double getLerpedYaw(double partialTick) {
        return super.getLerpedYaw(partialTick);
    }
}
