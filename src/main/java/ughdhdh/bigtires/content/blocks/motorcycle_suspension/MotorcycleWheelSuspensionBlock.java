package ughdhdh.bigtires.content.blocks.motorcycle_suspension;

import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlock;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import ughdhdh.bigtires.index.BigTiresBlockEntityTypes;

/**
 * Motorcycle Wheel Suspension block.
 * Наследует всё поведение WheelMount (приём шин, физика, рулёжка, kraton signal).
 * Единственное отличие — свой BlockEntityType (чтобы иметь свой рендерер).
 */
public class MotorcycleWheelSuspensionBlock extends WheelMountBlock {

    public MotorcycleWheelSuspensionBlock(Properties properties) {
        super(properties);
    }

    // Возвращаем наш собственный тип блок-сущности.
    // Сигнатура совместима: BlockEntityType<? extends WheelMountBlockEntity>.
    @Override
    public BlockEntityType<? extends WheelMountBlockEntity> getBlockEntityType() {
        return BigTiresBlockEntityTypes.MOTORCYCLE_WHEEL_SUSPENSION.get();
    }
}
