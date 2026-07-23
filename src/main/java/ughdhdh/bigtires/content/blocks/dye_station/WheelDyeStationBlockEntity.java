package ughdhdh.bigtires.content.blocks.dye_station;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import ughdhdh.bigtires.WheelColorData;
import ughdhdh.bigtires.index.BigTiresComponents;

/**
 * BlockEntity покрасочной станции.
 * <p>
 * Слоты: 0 — колесо, 1 — краситель шины, 2 — краситель диска.
 * TIRE_COLOR и RIM_COLOR обновляются <b>независимо</b>: пустой слот не трогает
 * соответствующий компонент. Отсутствие компонента = не крашено.
 */
public class WheelDyeStationBlockEntity extends BlockEntity implements MenuProvider, Container {

    public static final int SLOT_WHEEL    = 0;
    public static final int SLOT_TIRE_DYE = 1;
    public static final int SLOT_RIM_DYE  = 2;
    public static final int CONTAINER_SIZE = 3;

    private final ItemStack[] items = new ItemStack[CONTAINER_SIZE];

    public WheelDyeStationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        for (int i = 0; i < items.length; i++) items[i] = ItemStack.EMPTY;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.bigtires.wheel_dye_station");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return WheelDyeStationMenu.forServer(containerId, playerInventory, this,
                ContainerLevelAccess.create(level, worldPosition));
    }

    public void applyDyes() {
        ItemStack wheelStack = items[SLOT_WHEEL];
        if (wheelStack.isEmpty()) return;

        boolean changed = false;

        ItemStack tireDye = items[SLOT_TIRE_DYE];
        if (!tireDye.isEmpty() && tireDye.getItem() instanceof DyeItem dyeItem) {
            int dyeColor = dyeItem.getDyeColor().getFireworkColor();
            Integer existing = wheelStack.get(BigTiresComponents.TIRE_COLOR);
            int newColor = (existing == null)
                    ? WheelColorData.firstDye(dyeColor)
                    : WheelColorData.mix(existing, dyeColor);
            wheelStack.set(BigTiresComponents.TIRE_COLOR, newColor);
            tireDye.shrink(1);
            if (tireDye.isEmpty()) items[SLOT_TIRE_DYE] = ItemStack.EMPTY;
            changed = true;
        }

        ItemStack rimDye = items[SLOT_RIM_DYE];
        if (!rimDye.isEmpty() && rimDye.getItem() instanceof DyeItem dyeItem) {
            int dyeColor = dyeItem.getDyeColor().getFireworkColor();
            Integer existing = wheelStack.get(BigTiresComponents.RIM_COLOR);
            int newColor = (existing == null)
                    ? WheelColorData.firstDye(dyeColor)
                    : WheelColorData.mix(existing, dyeColor);
            wheelStack.set(BigTiresComponents.RIM_COLOR, newColor);
            rimDye.shrink(1);
            if (rimDye.isEmpty()) items[SLOT_RIM_DYE] = ItemStack.EMPTY;
            changed = true;
        }

        if (changed) setChanged();
    }

    public void resetColor() {
        ItemStack wheelStack = items[SLOT_WHEEL];
        if (wheelStack.isEmpty()) return;
        wheelStack.remove(BigTiresComponents.TIRE_COLOR);
        wheelStack.remove(BigTiresComponents.RIM_COLOR);
        setChanged();
    }

    @Override public int getContainerSize()  { return CONTAINER_SIZE; }
    @Override public boolean isEmpty()       { for (var s : items) if (!s.isEmpty()) return false; return true; }
    @Override public ItemStack getItem(int slot) { return items[slot]; }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack s = items[slot];
        if (s.isEmpty()) return ItemStack.EMPTY;
        if (s.getCount() <= amount) { items[slot] = ItemStack.EMPTY; setChanged(); return s; }
        ItemStack out = s.split(amount); setChanged(); return out;
    }

    @Override public ItemStack removeItemNoUpdate(int slot) { ItemStack s = items[slot]; items[slot] = ItemStack.EMPTY; return s; }
    @Override public void setItem(int slot, ItemStack stack) { items[slot] = stack; setChanged(); }
    @Override public boolean stillValid(Player player)       { return Container.stillValidBlockEntity(this, player); }
    @Override public void clearContent()                     { for (int i = 0; i < items.length; i++) items[i] = ItemStack.EMPTY; setChanged(); }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == SLOT_WHEEL) return true;
        return (slot == SLOT_TIRE_DYE || slot == SLOT_RIM_DYE) && stack.getItem() instanceof DyeItem;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        for (int i = 0; i < items.length; i++)
            if (!items[i].isEmpty()) tag.put("Item" + i, items[i].save(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for (int i = 0; i < items.length; i++)
            items[i] = tag.contains("Item" + i)
                    ? ItemStack.parseOptional(registries, tag.getCompound("Item" + i))
                    : ItemStack.EMPTY;
    }
}
