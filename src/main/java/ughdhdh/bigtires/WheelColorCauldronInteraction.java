package ughdhdh.bigtires;

import dev.ryanhcode.offroad.content.items.tire.TireItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import ughdhdh.bigtires.index.BigTiresComponents;

/**
 * Взаимодействие шины с котлом с водой: смывает {@link BigTiresComponents#WHEEL_COLOR}.
 * <p>
 * Поведение аналогично смыванию краски с кожаной брони в ванильном Minecraft:
 * <ol>
 *   <li>Правый клик шиной по котлу с водой.</li>
 *   <li>Компонент {@code WHEEL_COLOR} удаляется со стека.</li>
 *   <li>Уровень воды снижается на 1.</li>
 *   <li>Играет звук всплеска воды.</li>
 * </ol>
 * Если у шины нет компонента {@code WHEEL_COLOR} — взаимодействие пропускается.
 * <p>
 * ВАЖНО (1.21.1): интерфейс {@link CauldronInteraction} находится в пакете
 * {@code net.minecraft.core.cauldron}, а его метод {@code interact} возвращает
 * {@link ItemInteractionResult}, а не {@code InteractionResult} — это отдельный
 * enum специально для предмет-на-блок взаимодействий (см. NeoForge "Interaction Pipeline").
 *
 * <h3>Регистрация</h3>
 * Вызови {@link #registerAll()} один раз в {@code FMLCommonSetupEvent}.
 */
public final class WheelColorCauldronInteraction implements CauldronInteraction {

    public static final WheelColorCauldronInteraction INSTANCE = new WheelColorCauldronInteraction();

    private WheelColorCauldronInteraction() {}

    @Override
    public ItemInteractionResult interact(BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, ItemStack stack) {
        // Пропускаем, если на шине нет краски — пусть сработает другое поведение котла
        if (!stack.has(BigTiresComponents.WHEEL_COLOR)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        // Удаляем цвет
        stack.remove(BigTiresComponents.WHEEL_COLOR);
        player.setItemInHand(hand, stack);

        // Звук и игровое событие (как в ванильном cauldron washing)
        level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1.0f, 1.0f);
        level.gameEvent(null, GameEvent.FLUID_PLACE, pos);

        // Снижаем уровень воды на 1
        LayeredCauldronBlock.lowerFillLevel(state, level, pos);

        return ItemInteractionResult.SUCCESS;
    }

    /**
     * Регистрирует {@link #INSTANCE} для КАЖДОГО {@link TireItem} в игре —
     * независимо от того, какой мод его зарегистрировал (включая Offroad).
     * <p>
     * Вызывать в {@code FMLCommonSetupEvent.enqueueWork()} — после того, как
     * ВСЕ моды завершили регистрацию предметов.
     */
    public static void registerAll() {
        BuiltInRegistries.ITEM.forEach(item -> {
            if (item instanceof TireItem) {
                CauldronInteraction.WATER.map().put(item, INSTANCE);
            }
        });
    }
}
