package ughdhdh.bigtires.index;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import dev.simulated_team.simulated.service.SimInventoryService;
import ughdhdh.bigtires.BigTires;
import ughdhdh.bigtires.content.blocks.motorcycle_suspension.MotorcycleWheelSuspensionBlockEntity;
import ughdhdh.bigtires.content.blocks.motorcycle_suspension.MotorcycleWheelSuspensionRenderer;

public class BigTiresBlockEntityTypes {

    private static final SimulatedRegistrate REGISTRATE = BigTires.getRegistrate();

    // ── Motorcycle Wheel Suspension ───────────────────────────────────────────
    public static final BlockEntityEntry<MotorcycleWheelSuspensionBlockEntity> MOTORCYCLE_WHEEL_SUSPENSION =
        REGISTRATE.blockEntity("motorcycle_wheel_suspension",
                MotorcycleWheelSuspensionBlockEntity::new)
            // Регистрируем инвентарь (нужно для Create Schematics и контейнерного API)
            .onRegister(SimInventoryService.INSTANCE
                .registerInventory((be, dir) -> be.getInventory()))
            // Блоки, для которых используется этот тип BE
            .validBlocks(BigTiresBlocks.MOTORCYCLE_WHEEL_SUSPENSION)
            // Клиентский рендерер (регистрируется только на Dist.CLIENT)
            .renderer(() -> MotorcycleWheelSuspensionRenderer::new)
            .register();

    public static void init() {}
}
