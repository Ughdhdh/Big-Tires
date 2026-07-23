package ughdhdh.bigtires.client;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

public final class OffroadWheelColorCompat {

    public static void init() {
        register("small_tire");
        register("tire");
        register("large_tire");
        register("monstrous_tire");
    }

    private static void register(String wheelName) {
        WheelColorOverlayRegistry.register(
                ResourceLocation.fromNamespaceAndPath("offroad", wheelName), // item registry id, НЕ RL модели
                PartialModel.of(ResourceLocation.fromNamespaceAndPath(
                        "bigtires", "item/offroad_compat/" + wheelName + "_tire_mask")),
                ResourceLocation.fromNamespaceAndPath("bigtires", "block/tire_0_tire_overlay"),
                PartialModel.of(ResourceLocation.fromNamespaceAndPath(
                        "bigtires", "item/offroad_compat/" + wheelName + "_rim_mask")),
                ResourceLocation.fromNamespaceAndPath("bigtires", "block/tire_0_rim_overlay")
        );
    }

    private OffroadWheelColorCompat() {}
}
