package ughdhdh.bigtires.mixin;

import ughdhdh.bigtires.content.blocks.motorcycle_suspension.MotorcycleWheelSuspensionBlockEntity;
import ughdhdh.bigtires.index.BigTiresComponents;
import ughdhdh.bigtires.physics.BigTiresPhysics;
import ughdhdh.bigtires.physics.BuoyantTireData;
import ughdhdh.bigtires.physics.TirePhysicsData;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlock;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.index.OffroadDataComponents;
import dev.ryanhcode.sable.api.physics.force.ForceTotal;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(value = WheelMountBlockEntity.class, remap = false)
public abstract class MixinWheelMountBlockEntity {

    @Shadow @Final private Vector3d queuedForce;
    @Shadow @Final private ForceTotal forceTotal;
    @Shadow private static Collection<WheelMountBlockEntity> queuedWheelMounts;
    @Shadow private double chasingYaw;

    @Shadow public abstract ItemStack getHeldItem();

    // ── 1. Buoyancy ───────────────────────────────────────────────────────────

    @Inject(method = "sable$physicsTick", at = @At("HEAD"))
    private void bigtires$applyBuoyancy(
            final ServerSubLevel subLevel,
            final RigidBodyHandle handle,
            final double timeStep,
            final CallbackInfo ci
    ) {
        final WheelMountBlockEntity self = (WheelMountBlockEntity)(Object)this;
        final ItemStack item = getHeldItem();
        if (item.isEmpty()) return;

        final BuoyantTireData buoyancy = item.get(BigTiresComponents.BUOYANCY);
        if (buoyancy == null) return;

        BigTiresPhysics.applyBuoyancy(self, subLevel, handle, timeStep, buoyancy, forceTotal, chasingYaw);
        queuedWheelMounts.add(self);
    }

    // ── 2. Extend raycast for large tires ─────────────────────────────────────

    @Redirect(
            method = "computeMaxExtensionToTerrain",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;subtract(DDD)Lnet/minecraft/world/phys/Vec3;",
                    remap = true
            ),
            remap = false
    )
    private Vec3 bigtires$extendRaycastDepth(Vec3 instance, double x, double y, double z) {
        final ItemStack item = getHeldItem();
        final TireLike tire = item.get(OffroadDataComponents.TIRE);
        if (tire != null && tire.radius() > 4.0f) {
            return instance.subtract(0, tire.radius() + 2.0, 0);
        }
        return instance.subtract(x, y, z);
    }

    @ModifyConstant(
            method = "computeMaxExtensionToTerrain",
            constant = @Constant(doubleValue = 5.0, ordinal = 0),
            remap = false
    )
    private double bigtires$fixMinExtensionInit(double original) {
        final ItemStack item = getHeldItem();
        final TireLike tire = item.get(OffroadDataComponents.TIRE);
        if (tire != null && tire.radius() > 4.0f) {
            return tire.radius() + 2.0;
        }
        return original;
    }

    // ── 3. Fix liftoff for large tires ────────────────────────────────────────

    @ModifyVariable(
            method = "sable$physicsTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;lerp(DDD)D",
                    shift = At.Shift.AFTER,
                    ordinal = 0,
                    remap = true
            ),
            name = "maxExtension",
            remap = false
    )
    private double bigtires$fixMaxExtension(double maxExtension) {
        final ItemStack item = getHeldItem();
        final TireLike tire = item.get(OffroadDataComponents.TIRE);
        if (tire == null || tire.radius() <= 4.0f) return maxExtension;

        final float radius = tire.radius();
        final double noHitValue = radius + 2.0;
        final double threshold = 0.65 + radius + 0.25;
        if (maxExtension >= noHitValue - 0.1) {
            return threshold + 1.0;
        }
        return maxExtension;
    }

    // ── 4a. Motorcycle: перенаправить тягу/торможение вдоль оси вала ─────────
    //
    //  offroad ordinal=0: fma( kineticSpeed*drive + velocity·normalD*braking, normalD )
    //    normalD = перпендикуляр к валу = стандартное направление качения
    //  Для мотоцикла нам нужно sideD (ось вала = перпендикуляр к face):
    //    sideD = Direction.get(POSITIVE, axis).getNormal(), повёрнутый на chasingYaw

    @Redirect(
            method = "sable$physicsTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/joml/Vector3d;fma(DLorg/joml/Vector3dc;)Lorg/joml/Vector3d;",
                    ordinal = 0
            ),
            remap = false
    )
    private Vector3d bigtires$redirectMotorcycleDrive(
            Vector3d queuedForce, double factor, Vector3dc normalD
    ) {
        if (!((Object)this instanceof MotorcycleWheelSuspensionBlockEntity)) {
            return queuedForce.fma(factor, normalD);
        }
        // Мотоцикл: тяга/торможение вдоль sideD (ось вала = перпендикулярно face)
        // sideD = Direction.get(POSITIVE, axis).getNormal(), rotateY(chasingYaw)
        final Direction facing = ((WheelMountBlockEntity)(Object)this)
                .getBlockState().getValue(WheelMountBlock.HORIZONTAL_FACING);
        final Vec3i sideVec = Direction.get(Direction.AxisDirection.POSITIVE, facing.getAxis()).getNormal();
        final Vector3d motoSideD = new Vector3d(sideVec.getX(), 0, sideVec.getZ());
        motoSideD.rotateY(chasingYaw);
        return queuedForce.fma(factor, motoSideD);
    }

    // ── 4b. Motorcycle: перенаправить боковую коррекцию вдоль normalD ─────────
    //  + масштабировать по lateralStiffness для всех маунтов
    //
    //  offroad ordinal=1: fma( velocity·sideD * -0.6 * ... , sideD )
    //    sideD = ось вала = для стандарта это боковое скольжение ✓
    //  Для мотоцикла sideD — это направление качения (вдоль оси),
    //  боковая коррекция должна быть в normalD (перпендикуляр к валу = E-W для N-facing).

    @Redirect(
            method = "sable$physicsTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/joml/Vector3d;fma(DLorg/joml/Vector3dc;)Lorg/joml/Vector3d;",
                    ordinal = 1
            ),
            remap = false
    )
    private Vector3d bigtires$scaleLateralCorrection(
            Vector3d instance, double factor, Vector3dc sideD
    ) {
        final ItemStack item = getHeldItem();

        if ((Object)this instanceof MotorcycleWheelSuspensionBlockEntity) {
            // Мотоцикл: боковая коррекция вдоль normalD (перпендикуляр к валу)
            // normalD = new Vec3i(sideVec.Z, 0, sideVec.X), rotateY(chasingYaw)
            final Direction facing = ((WheelMountBlockEntity)(Object)this)
                    .getBlockState().getValue(WheelMountBlock.HORIZONTAL_FACING);
            final Vec3i sideVec = Direction.get(Direction.AxisDirection.POSITIVE, facing.getAxis()).getNormal();
            final Vec3i normalVec = new Vec3i(sideVec.getZ(), 0, sideVec.getX());
            final Vector3d motoNormalD = new Vector3d(normalVec.getX(), 0, normalVec.getZ());
            motoNormalD.rotateY(chasingYaw);

            if (!item.isEmpty()) {
                final TirePhysicsData physics = item.get(BigTiresComponents.TIRE_PHYSICS);
                if (physics != null) {
                    return instance.fma(factor / 0.6 * physics.lateralStiffness(), motoNormalD);
                }
            }
            return instance.fma(factor, motoNormalD);
        }

        // Стандарт: масштабируем по lateralStiffness, направление без изменений
        if (!item.isEmpty()) {
            final TirePhysicsData physics = item.get(BigTiresComponents.TIRE_PHYSICS);
            if (physics != null) {
                return instance.fma(factor / 0.6 * physics.lateralStiffness(), sideD);
            }
        }
        return instance.fma(factor, sideD);
    }

    // ── 5. TirePhysics (extra driveForce / rollingResistance / drift) ─────────

    @Inject(
            method = "sable$physicsTick",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/ryanhcode/sable/api/physics/force/ForceTotal;applyImpulseAtPoint(Ldev/ryanhcode/sable/sublevel/ServerSubLevel;Lorg/joml/Vector3dc;Lorg/joml/Vector3dc;)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void bigtires$applyGroundPhysics(
            final ServerSubLevel subLevel,
            final RigidBodyHandle handle,
            final double timeStep,
            final CallbackInfo ci
    ) {
        final WheelMountBlockEntity self = (WheelMountBlockEntity)(Object)this;
        final ItemStack item = getHeldItem();
        if (item.isEmpty()) return;

        final TirePhysicsData physics = item.get(BigTiresComponents.TIRE_PHYSICS);
        if (physics == null) return;

        if (self instanceof MotorcycleWheelSuspensionBlockEntity) {
            // Мотоцикл: extra тяга/сопротивление качению вдоль sideD (ось вала),
            // drift вдоль normalD
            BigTiresPhysics.applyMotorcycleTirePhysics(self, subLevel, handle, timeStep, physics, forceTotal);
        } else {
            BigTiresPhysics.applyTirePhysics(self, subLevel, handle, timeStep, physics, forceTotal);
        }
    }
}