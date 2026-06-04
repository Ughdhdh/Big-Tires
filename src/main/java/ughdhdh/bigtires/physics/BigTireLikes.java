package ughdhdh.bigtires.physics;

import ughdhdh.bigtires.BigTires;
import dev.ryanhcode.offroad.content.components.TireLike;
import net.minecraft.world.phys.Vec3;

/**
 * TireLike presets. All tires use offset = Vec3(0.5, Y, 0.5).
 *
 * WHEEL OFFSET IN WORLD SPACE (BigTireLikes.java):
 *   Vec3(0.5, Y, 0.5)
 *   - X=0.5, Z=0.5: DO NOT CHANGE — compensates the renderer's translate(-0.5,0,-0.5)
 *   - Y: vertical offset in blocks relative to the WheelMount axis
 *     0.0  = wheel center exactly on the axis (perfect rotation)
 *     +0.5 = wheel raised half a block above the axis
 *
 * ICON OFFSET (models/item/<name>/item.json):
 *   "display" -> "gui" -> "translation": [Xpx, Ypx, 0]   (in pixels)
 *                          "scale": [s, s, s]              (icon size)
 */
public class BigTireLikes {

    public static final TireLike HUGE_TIRE =
            new TireLike(5.0f, new Vec3(90,0,0), new Vec3(0.5, -1.0, 0.5),
                    BigTires.path("item/huge_tire/block"));

    public static final TireLike HUGE_WIDE_TIRE =
            new TireLike(5.0f, new Vec3(90,0,0), new Vec3(0.5, -1.5, 0.5),
                    BigTires.path("item/huge_wide_tire/block"));

    public static final TireLike HUGE_ROWING_TIRE =
            new TireLike(6.0f, new Vec3(90,0,0), new Vec3(0.5, -1.5, 0.5),
                    BigTires.path("item/huge_rowing_tire/block"));

    public static final TireLike HUGE_ROWING_WIDE_TIRE =
            new TireLike(6.0f, new Vec3(90,0,0), new Vec3(0.5, -2.5, 0.5),
                    BigTires.path("item/huge_rowing_wide_tire/block"));

    public static final TireLike BIG_TRACTOR_TIRE =
            new TireLike(1.5f, new Vec3(90,0,0), new Vec3(0.5, 0.0, 0.5),
                    BigTires.path("item/big_tractor_tire/block"));

    public static final TireLike TRACTOR_TIRE =
            new TireLike(1.0f, new Vec3(90,0,0), new Vec3(0.5, 0.0, 0.5),
                    BigTires.path("item/tractor_tire/block"));

    public static final TireLike TRUCK_TIRE =
            new TireLike(1.5f, new Vec3(90,0,0), new Vec3(0.5, 0.0, 0.5),
                    BigTires.path("item/truck_tire/block"));

    public static final TireLike SMALL_TRUCK_TIRE =
            new TireLike(1.0f, new Vec3(90,0,0), new Vec3(0.5, 0.0, 0.5),
                    BigTires.path("item/small_truck_tire/block"));

    public static final TireLike MONSTER_JAM_TIRE =
            new TireLike(2.5f, new Vec3(90,0,0), new Vec3(0.5, -0.05, 0.5),
                    BigTires.path("item/monster_jam_tire/block"));

    public static final TireLike BAMBOO_TIRE =
            new TireLike(1.95f, new Vec3(90,0,0), new Vec3(0.5, -0.4, 0.5),
                    BigTires.path("item/bamboo_tire/block"));

    public static final TireLike VINTAGE_TIRE =
            new TireLike(1.0f, new Vec3(90,0,0), new Vec3(0.5, 0.05, 0.5),
                    BigTires.path("item/vintage_tire/block"));

    public static final TireLike DRIFT_TIRE =
            new TireLike(0.76f, new Vec3(90,0,0), new Vec3(0.5, 0.0, 0.5),
                    BigTires.path("item/drift_tire/block"));
}
