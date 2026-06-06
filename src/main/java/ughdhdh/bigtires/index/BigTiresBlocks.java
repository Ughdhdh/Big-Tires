package ughdhdh.bigtires.index;

import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import ughdhdh.bigtires.BigTires;
import ughdhdh.bigtires.content.blocks.fixed_mount.FixedWheelMountBlock;
import ughdhdh.bigtires.content.blocks.motorcycle_suspension.MotorcycleWheelSuspensionBlock;

import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class BigTiresBlocks {

    private static final SimulatedRegistrate REGISTRATE = BigTires.getRegistrate();

    @SuppressWarnings("deprecation")
    public static final BlockEntry<MotorcycleWheelSuspensionBlock> MOTORCYCLE_WHEEL_SUSPENSION =
            REGISTRATE.block("motorcycle_wheel_suspension", MotorcycleWheelSuspensionBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p
                            .mapColor(MapColor.COLOR_GRAY)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()
                            .isRedstoneConductor((state, level, pos) -> false))
                    .transform(pickaxeOnly())
                    .addLayer(() -> RenderType::cutoutMipped)
                    .item()
                    .build()
                    .register();

    @SuppressWarnings("deprecation")
    public static final BlockEntry<FixedWheelMountBlock> FIXED_WHEEL_MOUNT =
            REGISTRATE.block("fixed_wheel_mount", FixedWheelMountBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p
                            .mapColor(MapColor.COLOR_GRAY)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()
                            .isRedstoneConductor((state, level, pos) -> false))
                    .transform(pickaxeOnly())
                    .addLayer(() -> RenderType::cutoutMipped)
                    .item()
                    .build()
                    .register();

    public static void init() {}
}