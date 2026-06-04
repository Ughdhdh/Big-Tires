package ughdhdh.bigtires;

import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import ughdhdh.bigtires.index.BigTiresBlockEntityTypes;
import ughdhdh.bigtires.index.BigTiresBlocks;
import ughdhdh.bigtires.index.BigTiresComponents;
import ughdhdh.bigtires.index.BigTiresItems;
import ughdhdh.bigtires.index.BigTiresPartialModels;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import dev.ryanhcode.sable.platform.SableEventPlatform;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

public class BigTires {

    public static final String MOD_ID = "bigtires";

    private static final NonNullSupplier<SimulatedRegistrate> REGISTRATE =
            NonNullSupplier.lazy(() ->
                    (SimulatedRegistrate) new SimulatedRegistrate(
                            BigTires.path(MOD_ID), MOD_ID
                    ).defaultCreativeTab((ResourceKey<CreativeModeTab>) null));

    public static void init() {
        setTooltips();

        BigTiresComponents.init();

        // Блоки должны быть до BlockEntityTypes (BE ссылается на блок)
        BigTiresBlocks.init();
        BigTiresBlockEntityTypes.init();

        BigTiresItems.init();

        listenCommonEvents();
    }

    public static void setTooltips() {
        getRegistrate().setTooltipModifierFactory(item ->
                new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                        .andThen(TooltipModifier.mapNull(KineticStats.create(item))));
    }

    private static void listenCommonEvents() {
        SableEventPlatform.INSTANCE.onPhysicsTick(BigTiresCommonEvents::physicsTick);
    }

    public static void clientInit() {
        BigTiresPartialModels.init();
    }

    public static SimulatedRegistrate getRegistrate() {
        return REGISTRATE.get();
    }

    public static ResourceLocation path(final String path) {
        return ResourceLocation.tryBuild(MOD_ID, path);
    }
}
