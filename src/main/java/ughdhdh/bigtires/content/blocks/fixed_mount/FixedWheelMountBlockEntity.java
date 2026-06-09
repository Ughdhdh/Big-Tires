package ughdhdh.bigtires.content.blocks.fixed_mount;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * BlockEntity для Fixed Wheel Mount.
 *
 * Поведение:
 *  • addBehaviours: вызывает super во временный список, чтобы инициализировать
 *    приватное поле this.strength (иначе sable$physicsTick падает с NPE).
 *    Устанавливает жёсткость пружины на максимум → нет пружинистости.
 *    Не добавляет поведение в реальный список → нет UI-виджета.
 *  • sable$physicsTick: не переопределяется — родительский метод работает нормально.
 *    Пружина поддерживает транспорт над землёй, но с макс. жёсткостью — без отскока.
 *  • getLerpedExtension: всегда 0 — визуально колесо не двигается.
 *  • getLerpedYaw: поднят до public для рендерера.
 */
public class FixedWheelMountBlockEntity extends WheelMountBlockEntity {

    public FixedWheelMountBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        // Инициализируем this.strength через super, но в отдельный список.
        // Без этого sable$physicsTick обращается к null и падает с NPE.
        final List<BlockEntityBehaviour> temp = new ArrayList<>();
        super.addBehaviours(temp);

        // Устанавливаем максимальную жёсткость пружины — нет пружинистости.
        // SuspensionStrengthValueBehaviour наследует ScrollValueBehaviour.
        //for (BlockEntityBehaviour b : temp) {
        //    if (b instanceof ScrollValueBehaviour scroll) {
        //        scroll.setValue(scroll.maxValue);
        //   }
        //}

        // НЕ добавляем в реальный список → нет UI-виджета жёсткости.
    }

    /** Колесо визуально всегда в нейтральной позиции. */
    @Override
    public double getLerpedExtension(float partialTick) {
        return 0.0;
    }

    /** Поднимаем getLerpedYaw до public для рендерера (другой пакет). */
    @Override
    public double getLerpedYaw(double partialTick) {
        return super.getLerpedYaw(partialTick);
    }
}
