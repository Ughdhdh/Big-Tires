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

public final class WheelColorCauldronInteraction implements CauldronInteraction {

    public static final WheelColorCauldronInteraction INSTANCE = new WheelColorCauldronInteraction();
    private WheelColorCauldronInteraction() {}

    @Override
    public ItemInteractionResult interact(BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, ItemStack stack) {
        if (!stack.has(BigTiresComponents.TIRE_COLOR) && !stack.has(BigTiresComponents.RIM_COLOR))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (level.isClientSide) return ItemInteractionResult.SUCCESS;

        stack.remove(BigTiresComponents.TIRE_COLOR);
        stack.remove(BigTiresComponents.RIM_COLOR);
        player.setItemInHand(hand, stack);

        level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1.0f, 1.0f);
        level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        LayeredCauldronBlock.lowerFillLevel(state, level, pos);

        return ItemInteractionResult.SUCCESS;
    }

    public static void registerAll() {
        BuiltInRegistries.ITEM.forEach(item -> {
            if (item instanceof TireItem)
                CauldronInteraction.WATER.map().put(item, INSTANCE);
        });
    }
}
