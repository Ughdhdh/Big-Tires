package ughdhdh.bigtires.content.blocks.fixed_mount;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * BlockEntity для Fixed Wheel Mount.
 *
 * Отличия от WheelMountBlockEntity:
 *  1. addBehaviours пуст — нет виджета настройки жёсткости подвески
 *  2. sable$physicsTick пуст — нет пружинных/демпферных сил
 *  3. getLerpedExtension всегда 0 — колесо не двигается по Y
 *
 * Тяговые/боковые силы применяются отдельно через BigTiresCommonEvents.
 */
public class FixedWheelMountBlockEntity extends WheelMountBlockEntity {

    public FixedWheelMountBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Поднимаем getLerpedYaw до public — рендерер в другом пакете
     * не может обратиться к protected методу напрямую.
     */
    @Override
    public double getLerpedYaw(double partialTick) {
        return super.getLerpedYaw(partialTick);
    }

    /**
     * Убираем виджет настройки жёсткости —
     * у фиксированного маунта нет амортизации.
     */
    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        // Намеренно пусто: не вызываем super,
        // чтобы не добавлять SuspensionStrengthValueBehaviour.
    }

    /**
     * Нет пружины: колесо жёстко закреплено.
     * BigTiresCommonEvents всё равно применит тяговые/боковые силы
     * через свой отдельный хук (instanceof WheelMountBlockEntity проходит).
     */
    @Override
    public void sable$physicsTick(ServerSubLevel subLevel,
                                  RigidBodyHandle handle,
                                  double timeStep) {
        // Намеренно пусто: пружина и демпфер не применяются.
    }

    /**
     * Колесо всегда в нейтральной позиции (нет хода подвески).
     */
    @Override
    public double getLerpedExtension(float partialTick) {
        return 0.0;
    }
}