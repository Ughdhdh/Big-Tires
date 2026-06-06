package ughdhdh.bigtires.index;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import dev.simulated_team.simulated.service.SimInventoryService;
import ughdhdh.bigtires.BigTires;
import ughdhdh.bigtires.content.blocks.fixed_mount.FixedWheelMountBlockEntity;
import ughdhdh.bigtires.content.blocks.fixed_mount.FixedWheelMountRenderer;
import ughdhdh.bigtires.content.blocks.motorcycle_suspension.MotorcycleWheelSuspensionBlockEntity;
import ughdhdh.bigtires.content.blocks.motorcycle_suspension.MotorcycleWheelSuspensionRenderer;

public class BigTiresBlockEntityTypes {

    private static final SimulatedRegistrate REGISTRATE = BigTires.getRegistrate();

    // ── Motorcycle Wheel Suspension ───────────────────────────────────────────
    public static final BlockEntityEntry<MotorcycleWheelSuspensionBlockEntity> MOTORCYCLE_WHEEL_SUSPENSION =
        REGISTRATE.blockEntity("motorcycle_wheel_suspension",
                MotorcycleWheelSuspensionBlockEntity::new)
            .onRegister(SimInventoryService.INSTANCE
                .registerInventory((be, dir) -> be.getInventory()))
            .validBlocks(BigTiresBlocks.MOTORCYCLE_WHEEL_SUSPENSION)
            .renderer(() -> MotorcycleWheelSuspensionRenderer::new)
            .register();

    // ── Fixed Wheel Mount ─────────────────────────────────────────────────────
    public static final BlockEntityEntry<FixedWheelMountBlockEntity> FIXED_WHEEL_MOUNT =
        REGISTRATE.blockEntity("fixed_wheel_mount",
                FixedWheelMountBlockEntity::new)
            .onRegister(SimInventoryService.INSTANCE
                .registerInventory((be, dir) -> be.getInventory()))
            .validBlocks(BigTiresBlocks.FIXED_WHEEL_MOUNT)
            .renderer(() -> FixedWheelMountRenderer::new)
            .register();

    public static void init() {}
}
