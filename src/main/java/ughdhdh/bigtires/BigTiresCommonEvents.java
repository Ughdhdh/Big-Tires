package ughdhdh.bigtires;

import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;

/**
 * Analog of OffroadCommonEvents.
 * The physics tick hook is needed to apply batched forces from BigTires tires
 * in sync with offroad (after all WheelMounts have been processed).
 *
 * Currently BigTires applies forces directly inside physicsTick via Mixin,
 * so this hook is empty — reserved for future batch operations.
 */
public class BigTiresCommonEvents {

    public static void physicsTick(final SubLevelPhysicsSystem physicsSystem, final double timeStep) {

    }
}
