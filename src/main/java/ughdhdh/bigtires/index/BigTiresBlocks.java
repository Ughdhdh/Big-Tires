package ughdhdh.bigtires.index;

import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import ughdhdh.bigtires.BigTires;
import ughdhdh.bigtires.content.blocks.motorcycle_suspension.MotorcycleWheelSuspensionBlock;

import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class BigTiresBlocks {

    private static final SimulatedRegistrate REGISTRATE = BigTires.getRegistrate();

    // ── Motorcycle Wheel Suspension ───────────────────────────────────────────
    public static final BlockEntry<MotorcycleWheelSuspensionBlock> MOTORCYCLE_WHEEL_SUSPENSION =
        REGISTRATE.block("motorcycle_wheel_suspension", MotorcycleWheelSuspensionBlock::new)
            // Материал: мягкий металл (прочность как у WheelMount из offroad)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p
                .mapColor(MapColor.COLOR_GRAY)
                .sound(SoundType.NETHERITE_BLOCK)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                // Не проводит редстоун (как и WheelMount)
                .isRedstoneConductor((state, level, pos) -> false))
            // Требует кирку для добычи
            .transform(pickaxeOnly())
            // cutoutMipped — нужен для корректного рендера модели с прозрачными пикселями
            .addLayer(() -> RenderType::cutoutMipped)
            // Blockstate и модель предоставлены вручную в ресурсах:
            //   assets/bigtires/blockstates/motorcycle_wheel_suspension.json
            //   assets/bigtires/models/block/motorcycle_wheel_suspension/block.json
            .item()
                // Модель предмета: assets/bigtires/models/item/motorcycle_wheel_suspension.json
                .build()
            .register();

    public static void init() {}
}
