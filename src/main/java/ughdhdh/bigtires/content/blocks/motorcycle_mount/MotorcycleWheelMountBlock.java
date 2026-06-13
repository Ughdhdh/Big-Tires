package ughdhdh.bigtires.content.blocks.motorcycle_mount;

import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlock;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import ughdhdh.bigtires.index.BigTiresBlockEntityTypes;

/**
 * Motorcycle Wheel Suspension block.
 * Наследует всё поведение WheelMount (приём шин, физика, рулёжка, kraton signal).
 */
public class MotorcycleWheelMountBlock extends WheelMountBlock {

    public MotorcycleWheelMountBlock(Properties properties) {
        super(properties);
    }

    // Возвращаем наш собственный тип блок-сущности.
    // Сигнатура совместима: BlockEntityType<? extends WheelMountBlockEntity>.
    @Override
    public BlockEntityType<? extends WheelMountBlockEntity> getBlockEntityType() {
        // ВРЕМЕННО ОТКЛЮЧЕНО — BigTiresBlockEntityTypes.MOTORCYCLE_WHEEL_MOUNT не зарегистрирован
        throw new UnsupportedOperationException("MotorcycleWheelMount is temporarily disabled");
        // return BigTiresBlockEntityTypes.MOTORCYCLE_WHEEL_MOUNT.get();
    }
}
