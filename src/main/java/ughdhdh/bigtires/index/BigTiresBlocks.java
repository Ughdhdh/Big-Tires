package ughdhdh.bigtires.index;

import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import ughdhdh.bigtires.BigTires;
import ughdhdh.bigtires.content.blocks.dye_station.WheelDyeStationBlock;
import ughdhdh.bigtires.content.blocks.motorcycle_mount.MotorcycleWheelMountBlock;

import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class BigTiresBlocks {

    private static final SimulatedRegistrate REGISTRATE = BigTires.getRegistrate();

    public static final BlockEntry<MotorcycleWheelMountBlock> MOTORCYCLE_WHEEL_MOUNT =
            REGISTRATE.block("motorcycle_wheel_mount", MotorcycleWheelMountBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p
                            .mapColor(MapColor.COLOR_GRAY)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()
                            .isRedstoneConductor((state, level, pos) -> false))
                    .transform(pickaxeOnly())
                    .item()
                    .build()
                    .register();

    /** Покрасочная станция для колёс (краска шины + краска диска). */
    public static final BlockEntry<WheelDyeStationBlock> WHEEL_DYE_STATION =
            REGISTRATE.block("wheel_dye_station", WheelDyeStationBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p
                            .mapColor(MapColor.COLOR_GRAY)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops()
                            .noOcclusion())
                    .transform(pickaxeOnly())
                    .item()
                    .build()
                    .register();

    public static void init() {}
}
