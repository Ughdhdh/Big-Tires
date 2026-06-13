package ughdhdh.bigtires.mixin;

import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import ughdhdh.bigtires.content.blocks.motorcycle_mount.MotorcycleWheelMountBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Сдвигает окно регулировки силы подвески на 2 пикселя влево.
 *
 * SuspensionStrengthValueBox создаётся как new SuspensionStrengthValueBox(hOffset).
 * По умолчанию hOffset=0 (X=8 в voxel space).
 * Меняем на hOffset=-2 (X=6) — два пикселя влево.
 */
@Mixin(value = WheelMountBlockEntity.class, remap = false)
public abstract class MixinWheelMountValueBox {

    @ModifyArg(
            method = "addBehaviours",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/ryanhcode/offroad/content/blocks/wheel_mount/WheelMountBlockEntity$SuspensionStrengthValueBox;<init>(I)V",
                    remap = false
            ),
            index = 0,
            remap = false
    )
    private int bigtires$moveValueBoxLeft(int original) {
        // Только для мотоциклетного маунта, -3 пикселя
        if ((Object)this instanceof MotorcycleWheelMountBlockEntity) {
            return original - 3;
        }
        return original; // остальные WheelMount — без изменений
    }
}
