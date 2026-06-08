package ughdhdh.bigtires.mixin;

import ughdhdh.bigtires.index.BigTiresComponents;
import ughdhdh.bigtires.physics.BigTiresPhysics;
import ughdhdh.bigtires.physics.BuoyantTireData;
import ughdhdh.bigtires.physics.TirePhysicsData;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.index.OffroadDataComponents;
import dev.ryanhcode.sable.api.physics.force.ForceTotal;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.world.item.ItemStack;
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

    @Shadow
    public abstract ItemStack getHeldItem();

    // ── 1. Buoyancy

    @Inject(
            method = "sable$physicsTick",
            at = @At("HEAD")
    )
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

    // ── 2. Extend raycast and initial value for large tires

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

    // ── 3. Fix liftoff for large tires

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

    // ── 4. Масштабируем боковую коррекцию по lateralStiffness ─────────────────
    //
    //  Оригинал: queuedForce.fma(lateralVel * -0.6 * touchingFriction * strengthMul * timeStep, sideD)
    //  Мы перехватываем второй вызов fma (ordinal=1) и умножаем коэффициент на lateralStiffness/0.6:
    //    lateralStiffness=0.0 → factor=0  → нет боковой коррекции (дрифт)
    //    lateralStiffness=0.6 → factor без изменений
    //    lateralStiffness=1.0 → коррекция ×1.67 (цепкая резина)
    //    lateralStiffness=2.0 → коррекция ×3.33 (трактор)

    @Redirect(
            method = "sable$physicsTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/joml/Vector3d;fma(DLorg/joml/Vector3dc;)Lorg/joml/Vector3d;",
                    ordinal = 1
            ),
            remap = false
    )
    private Vector3d bigtires$scaleLateralCorrection(Vector3d instance, double factor, Vector3dc vec) {
        final ItemStack item = getHeldItem();
        if (!item.isEmpty()) {
            final TirePhysicsData physics = item.get(BigTiresComponents.TIRE_PHYSICS);
            if (physics != null) {
                return instance.fma(factor / 0.6 * physics.lateralStiffness(), vec);
            }
        }
        return instance.fma(factor, vec);
    }

    // ── 5. TirePhysics — только когда колесо касается земли

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

        // Мотоциклетный маунт: колесо катится ВДОЛЬ facing (перпендикулярно вилке)
        if (self instanceof ughdhdh.bigtires.content.blocks.motorcycle_suspension.MotorcycleWheelSuspensionBlockEntity) {
            BigTiresPhysics.applyMotorcycleTirePhysics(self, subLevel, handle, timeStep, physics, forceTotal);
        } else {
            BigTiresPhysics.applyTirePhysics(self, subLevel, handle, timeStep, physics, forceTotal);
        }
    }
}