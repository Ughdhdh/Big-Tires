package ughdhdh.bigtires.item;

import ughdhdh.bigtires.index.BigTiresComponents;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class WheelWrenchItem extends Item {

    public WheelWrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof WheelMountBlockEntity wheelMount)) {
            return InteractionResult.PASS;
        }

        ItemStack tireStack = wheelMount.getHeldItem();
        if (tireStack.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // Переключаем флаг FLIPPED на стаке шины
        boolean wasFlipped = Boolean.TRUE.equals(tireStack.get(BigTiresComponents.FLIPPED));
        if (wasFlipped) {
            tireStack.remove(BigTiresComponents.FLIPPED);
        } else {
            tireStack.set(BigTiresComponents.FLIPPED, Boolean.TRUE);
        }

        wheelMount.getInventory().setItem(0, tireStack);
        wheelMount.onStackChanged();
        wheelMount.sendData();
        wheelMount.setChanged();

        level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS,
                0.4f, wasFlipped ? 0.9f : 1.1f);

        if (player != null) {
            player.displayClientMessage(
                    Component.translatable(wasFlipped
                            ? "item.bigtires.wheel_wrench.flipped"
                            : "item.bigtires.wheel_wrench.unflipped"),
                    true);
        }

        return InteractionResult.CONSUME;
    }
}
