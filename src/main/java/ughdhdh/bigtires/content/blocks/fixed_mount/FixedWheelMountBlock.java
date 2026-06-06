package ughdhdh.bigtires.content.blocks.fixed_mount;

import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlock;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import ughdhdh.bigtires.index.BigTiresBlockEntityTypes;

/**
 * Fixed Wheel Mount — жёсткий маунт без амортизации.
 * Колесо на 0.5 блока выше стандартного WheelMount.
 * Нет пружинных сил, нет настройки жёсткости подвески.
 */
public class FixedWheelMountBlock extends WheelMountBlock {

    public FixedWheelMountBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends WheelMountBlockEntity> getBlockEntityType() {
        return BigTiresBlockEntityTypes.FIXED_WHEEL_MOUNT.get();
    }
}
