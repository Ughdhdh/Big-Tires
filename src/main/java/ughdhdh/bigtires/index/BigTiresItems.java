package ughdhdh.bigtires.index;

import com.simibubi.create.foundation.data.AssetLookup;
import ughdhdh.bigtires.BigTires;
import ughdhdh.bigtires.physics.BigTireLikes;
import ughdhdh.bigtires.physics.BuoyantTireData;
import ughdhdh.bigtires.physics.TirePhysicsData;
import dev.ryanhcode.offroad.content.items.tire.TireItem;
import dev.ryanhcode.offroad.index.OffroadDataComponents;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;

public class BigTiresItems {

    private static final SimulatedRegistrate REGISTRATE = BigTires.getRegistrate();

    static {

        // ── Huge Tire 10×10×3 ─────────────────────────────────────────────────
        REGISTRATE.item("huge_tire", TireItem::new)
                .properties(x -> x
                        .component(OffroadDataComponents.TIRE,      BigTireLikes.HUGE_TIRE)
                        .component(BigTiresComponents.TIRE_PHYSICS, TirePhysicsData.HUGE))
                .model(AssetLookup.itemModelWithPartials()).register();

        // ── Huge Wide Tire 10×10×4 ────────────────────────────────────────────
        REGISTRATE.item("huge_wide_tire", TireItem::new)
                .properties(x -> x
                        .component(OffroadDataComponents.TIRE,      BigTireLikes.HUGE_WIDE_TIRE)
                        .component(BigTiresComponents.TIRE_PHYSICS, TirePhysicsData.HUGE_WIDE))
                .model(AssetLookup.itemModelWithPartials()).register();

        // ── Huge Rowing Tire 12×12×4 — floats and paddles ────────────────────
        REGISTRATE.item("huge_rowing_tire", TireItem::new)
                .properties(x -> x
                        .component(OffroadDataComponents.TIRE,      BigTireLikes.HUGE_ROWING_TIRE)
                        .component(BigTiresComponents.TIRE_PHYSICS, TirePhysicsData.ROWING)
                        .component(BigTiresComponents.BUOYANCY,     BuoyantTireData.ROWING))
                .model(AssetLookup.itemModelWithPartials()).register();

        // ── Huge Rowing Wide Tire 12×12×6 — wider paddles, more thrust ────────
        REGISTRATE.item("huge_rowing_wide_tire", TireItem::new)
                .properties(x -> x
                        .component(OffroadDataComponents.TIRE,      BigTireLikes.HUGE_ROWING_WIDE_TIRE)
                        .component(BigTiresComponents.TIRE_PHYSICS, TirePhysicsData.ROWING_WIDE)
                        .component(BigTiresComponents.BUOYANCY,     BuoyantTireData.ROWING_WIDE))
                .model(AssetLookup.itemModelWithPartials()).register();

        // ── Big Tractor Tire 3×3×1 ────────────────────────────────────────────
        REGISTRATE.item("big_tractor_tire", TireItem::new)
                .properties(x -> x
                        .component(OffroadDataComponents.TIRE,      BigTireLikes.BIG_TRACTOR_TIRE)
                        .component(BigTiresComponents.TIRE_PHYSICS, TirePhysicsData.BIG_TRACTOR))
                .model(AssetLookup.itemModelWithPartials()).register();

        // ── Tractor Tire 2×2×1 ────────────────────────────────────────────────
        REGISTRATE.item("tractor_tire", TireItem::new)
                .properties(x -> x
                        .component(OffroadDataComponents.TIRE,      BigTireLikes.TRACTOR_TIRE)
                        .component(BigTiresComponents.TIRE_PHYSICS, TirePhysicsData.TRACTOR))
                .model(AssetLookup.itemModelWithPartials()).register();

        // ── Truck Tire 3×3×1.5 ───────────────────────────────────────────────
        REGISTRATE.item("truck_tire", TireItem::new)
                .properties(x -> x
                        .component(OffroadDataComponents.TIRE,      BigTireLikes.TRUCK_TIRE)
                        .component(BigTiresComponents.TIRE_PHYSICS, TirePhysicsData.TRUCK))
                .model(AssetLookup.itemModelWithPartials()).register();

        // ── Small Truck Tire 2×2×1 ───────────────────────────────────────────
        REGISTRATE.item("small_truck_tire", TireItem::new)
                .properties(x -> x
                        .component(OffroadDataComponents.TIRE,      BigTireLikes.SMALL_TRUCK_TIRE)
                        .component(BigTiresComponents.TIRE_PHYSICS, TirePhysicsData.SMALL_TRUCK))
                .model(AssetLookup.itemModelWithPartials()).register();
        // ── Vintage Tire 2×2×1 ───────────────────────────────────────────
        REGISTRATE.item("vintage_tire", TireItem::new)
                .properties(x -> x
                        .component(OffroadDataComponents.TIRE,      BigTireLikes.VINTAGE_TIRE)
                        .component(BigTiresComponents.TIRE_PHYSICS, TirePhysicsData.VINTAGE))
                .model(AssetLookup.itemModelWithPartials()).register();
        // ── Monster Jam Tire 5x5x3.5───────────────────────────────────────────
        REGISTRATE.item("monster_jam_tire", TireItem::new)
                .properties(x -> x
                        .component(OffroadDataComponents.TIRE,      BigTireLikes.MONSTER_JAM_TIRE)
                        .component(BigTiresComponents.TIRE_PHYSICS, TirePhysicsData.MONSTER_JAM))
                .model(AssetLookup.itemModelWithPartials()).register();
        // ── Bamboo Tire 4x4x1.5───────────────────────────────────────────
        REGISTRATE.item("bamboo_tire", TireItem::new)
                .properties(x -> x
                        .component(OffroadDataComponents.TIRE,      BigTireLikes.BAMBOO_TIRE)
                        .component(BigTiresComponents.TIRE_PHYSICS, TirePhysicsData.BAMBOO)
                        .component(BigTiresComponents.BUOYANCY,     BuoyantTireData.BAMBOO))
                .model(AssetLookup.itemModelWithPartials()).register();
    }

    public static void init() {}
}
