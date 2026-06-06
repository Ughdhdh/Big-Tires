package ughdhdh.bigtires.index;

import ughdhdh.bigtires.BigTires;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

/**
        Flywheel PartialModels for all BigTires tires.
 */

public class BigTiresPartialModels {

    public static final PartialModel
            HUGE_TIRE             = item("huge_tire/block"),
            HUGE_WIDE_TIRE        = item("huge_wide_tire/block"),
            HUGE_ROWING_TIRE      = item("huge_rowing_tire/block"),
            HUGE_ROWING_WIDE_TIRE = item("huge_rowing_wide_tire/block"),
            BIG_TRACTOR_TIRE      = item("big_tractor_tire/block"),
            TRACTOR_TIRE          = item("tractor_tire/block"),
            TRUCK_TIRE            = item("truck_tire/block"),
            NARROW_TRUCK_TIRE     = item("narrow_truck_tire/block"),
            SMALL_TRUCK_TIRE      = item("small_truck_tire/block"),
            VINTAGE_TIRE          = item("vintage_tire/block"),
            MONSTER_JAM_TIRE      = item("monster_jam_tire/block"),
            BAMBOO_TIRE           = item("bamboo_tire/block"),
            DRIFT_TIRE            = item("drift_tire/block"),
            WOODEN_WHEEL          = item("wooden_wheel/block");

    private static PartialModel item(final String path) {
        return PartialModel.of(BigTires.path("item/" + path));
    }

    public static void init() {}
}
