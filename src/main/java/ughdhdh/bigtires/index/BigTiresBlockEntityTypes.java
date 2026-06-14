package ughdhdh.bigtires.index;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import dev.simulated_team.simulated.service.SimInventoryService;
import ughdhdh.bigtires.BigTires;
import ughdhdh.bigtires.content.blocks.motorcycle_mount.MotorcycleWheelMountBlockEntity;
import ughdhdh.bigtires.content.blocks.motorcycle_mount.MotorcycleWheelMountRenderer;

public class BigTiresBlockEntityTypes {

    private static final SimulatedRegistrate REGISTRATE = BigTires.getRegistrate();

    // ── Motorcycle Wheel Mount
    public static final BlockEntityEntry<MotorcycleWheelMountBlockEntity> MOTORCYCLE_WHEEL_MOUNT =
        REGISTRATE.blockEntity("motorcycle_wheel_mount",
                MotorcycleWheelMountBlockEntity::new)
            .onRegister(SimInventoryService.INSTANCE
                .registerInventory((be, dir) -> be.getInventory()))
            .validBlocks(BigTiresBlocks.MOTORCYCLE_WHEEL_MOUNT)
            .renderer(() -> MotorcycleWheelMountRenderer::new)
            .register();

    public static void init() {}
}
