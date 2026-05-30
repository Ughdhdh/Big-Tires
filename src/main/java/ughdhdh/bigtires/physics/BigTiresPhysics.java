package ughdhdh.bigtires.physics;

import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlock;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.index.OffroadDataComponents;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.math.OrientedBoundingBox3d;
import dev.ryanhcode.sable.api.physics.force.ForceTotal;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;

public class BigTiresPhysics {

    // ── TirePhysics ───────────────────────────────────────────────────────────

    public static void applyTirePhysics(
            final WheelMountBlockEntity be,
            final ServerSubLevel subLevel,
            final RigidBodyHandle handle,
            final double timeStep,
            final TirePhysicsData data,
            final ForceTotal out
    ) {
        final Level level = be.getLevel();
        if (level == null) return;

        final Direction facing = be.getBlockState().getValue(WheelMountBlock.HORIZONTAL_FACING);
        final net.minecraft.world.phys.Vec3 localPos = be.getBlockPos().relative(facing).getCenter();
        final Pose3dc pose = subLevel.logicalPose();

        final Vector3d velocity = Sable.HELPER.getVelocity(level, JOMLConversion.toJOML(localPos));
        final Vector3d localVelocity = pose.transformNormalInverse(velocity);

        final Direction.Axis axis = facing.getAxis();
        final Vec3i fwdNormal = Direction.get(Direction.AxisDirection.POSITIVE, axis).getNormal();
        final Vec3i sideNormal = new Vec3i(fwdNormal.getZ(), 0, fwdNormal.getX());
        final Vector3d fwdD = new Vector3d(fwdNormal.getX(), 0, fwdNormal.getZ()).normalize();
        final Vector3d sideD = new Vector3d(sideNormal.getX(), 0, sideNormal.getZ()).normalize();
        final Vector3d localPos3d = new Vector3d(localPos.x, localPos.y, localPos.z);

        final double lateralExtra = (data.lateralStiffness() - 1.0f);
        if (Math.abs(lateralExtra) > 0.01) {
            final double lateralVel = localVelocity.dot(sideD);
            out.applyImpulseAtPoint(subLevel, localPos3d,
                    new Vector3d(sideD).mul(lateralVel * -lateralExtra * 0.6 * timeStep));
        }

        final double driveExtra = (data.driveForce() - 1.0f);
        if (Math.abs(driveExtra) > 0.01) {
            final float kineticSpeed = axis == Direction.Axis.X ? be.getSpeed() : -be.getSpeed();
            out.applyImpulseAtPoint(subLevel, localPos3d,
                    new Vector3d(fwdD).mul(kineticSpeed * driveExtra * 1.75 * timeStep));
        }

        if (data.rollingResistance() > 0.01f) {
            final double fwdVel = localVelocity.dot(fwdD);
            out.applyImpulseAtPoint(subLevel, localPos3d,
                    new Vector3d(fwdD).mul(fwdVel * -data.rollingResistance() * timeStep * 3.0));
        }
    }

    // ── Buoyancy ──────────────────────────────────────────────────────────────

    public static void applyBuoyancy(
            final WheelMountBlockEntity be,
            final ServerSubLevel subLevel,
            final RigidBodyHandle handle,
            final double timeStep,
            final BuoyantTireData data,
            final ForceTotal out,
            final double chasingYaw
    ) {
        final Level level = be.getLevel();
        if (level == null) return;

        final ItemStack item = be.getHeldItem();
        if (item.isEmpty()) return;

        final TireLike tire = item.get(OffroadDataComponents.TIRE);
        if (tire == null) return;

        final Direction facing = be.getBlockState().getValue(WheelMountBlock.HORIZONTAL_FACING);
        final net.minecraft.world.phys.Vec3 localPos = be.getBlockPos().relative(facing).getCenter();
        final Pose3dc pose = subLevel.logicalPose();

        final Vector3d globalPos3d = new Vector3d(localPos.x, localPos.y, localPos.z);
        pose.transformPosition(globalPos3d);
        final BlockPos globalWheelBlockPos = new BlockPos(
                (int) Math.floor(globalPos3d.x),
                (int) Math.floor(globalPos3d.y + tire.offset().y),
                (int) Math.floor(globalPos3d.z)
        );

        final double submersion = computeSubmersion(level, globalWheelBlockPos, tire.radius());
        if (submersion <= 0.0) return;

        final Vector3d localPos3d = new Vector3d(localPos.x, localPos.y, localPos.z);

        double inverseMass = subLevel.getMassTracker()
                .getInverseNormalMass(localPos3d, OrientedBoundingBox3d.UP);
        if (inverseMass <= 0.0) inverseMass = 1.0 / subLevel.getMassTracker().getMass();
        if (inverseMass <= 0.0) return;

        // Buoyancy lift force
        final double buoyForce = data.buoyancy() * submersion * 9.81 / inverseMass * timeStep * 3.5;
        final Vector3d localUp = pose.transformNormalInverse(new Vector3d(0, buoyForce, 0));
        out.applyImpulseAtPoint(subLevel, localPos3d, localUp);

        // Velocity in world space
        final Vector3d worldVelocity = Sable.HELPER.getVelocity(level, JOMLConversion.toJOML(localPos));

        // Vertical drag
        final Vector3d vertDrag = pose.transformNormalInverse(
                new Vector3d(0, -worldVelocity.y * submersion * 10.0 * timeStep, 0));
        out.applyImpulseAtPoint(subLevel, localPos3d, vertDrag);

        // Horizontal drag
        final Vector3d horizDrag = pose.transformNormalInverse(
                new Vector3d(
                        -worldVelocity.x * submersion * 10.0 * timeStep,
                        0,
                        -worldVelocity.z * submersion * 10.0 * timeStep
                ));
        out.applyImpulseAtPoint(subLevel, localPos3d, horizDrag);

        // Paddle thrust considering chasingYaw (steering angle)
        final float speed = be.getSpeed();
        if (Math.abs(speed) > 0.01f) {
            final Direction.Axis axis = facing.getAxis();
            final Vec3i fwdNormal = Direction.get(Direction.AxisDirection.POSITIVE, axis).getNormal();
            // Base movement vector (perpendicular to facing)
            final Vector3d fwdD = new Vector3d(fwdNormal.getZ(), 0, -fwdNormal.getX());
            // Apply steering rotation
            fwdD.rotateY(chasingYaw);
            fwdD.normalize();
            final double thrust = Math.signum(speed) * data.paddleForce() * submersion * 15.0 * timeStep;
            out.applyImpulseAtPoint(subLevel, localPos3d, new Vector3d(fwdD).mul(thrust));
        }
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private static double computeSubmersion(final Level level, final BlockPos globalCenterPos, float radius) {
        int water = 0;
        int range = Math.max(2, (int) Math.ceil(radius));
        int totalCheck = 0;
        for (int dy = -range; dy <= range; dy++) {
            totalCheck++;
            if (level.getFluidState(globalCenterPos.above(dy)).is(FluidTags.WATER)) water++;
        }
        return (double) water / totalCheck;
    }
}
