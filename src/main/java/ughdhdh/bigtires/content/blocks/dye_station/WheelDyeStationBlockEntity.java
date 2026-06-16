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
 * Слоты:
 * <ul>
 *   <li>0 — колесо/шина (вход/выход)</li>
 *   <li>1 — краситель для шины (резиновая часть, R-канал маски)</li>
 *   <li>2 — краситель для диска (металлическая часть, G-канал маски)</li>
 * </ul>
 *
 * <h3>Смешивание</h3>
 * Работает как кожаная броня в Minecraft:
 * <ul>
 *   <li>Первое нанесение: цвет красителя становится цветом шины/диска.</li>
 *   <li>Последующие: ванильный алгоритм усреднения с сохранением яркости.</li>
 *   <li>Можно красить только шину, только диск, или оба сразу.</li>
 * </ul>
 *
 * <h3>Смывание</h3>
 * Правый клик колесом по котлу с водой → компонент {@code WHEEL_COLOR} удаляется.
 * Реализовано в {@link ughdhdh.bigtires.WheelColorCauldronInteraction}.
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

    // ── MenuProvider ─────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.bigtires.wheel_dye_station");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return WheelDyeStationMenu.forServer(containerId, playerInventory, this,
                ContainerLevelAccess.create(level, worldPosition));
    }

    // ── Логика покраски ───────────────────────────────────────────────────────

    /**
     * Применяет красители к колесу.
     * <p>
     * Смешивание — как у кожаной брони ({@link WheelColorData#mix}).
     * Первое нанесение просто устанавливает цвет ({@link WheelColorData#firstDye}).
     * Каждый краситель расходуется по 1 штуке.
     */
    public void applyDyes() {
        ItemStack wheelStack = items[SLOT_WHEEL];
        if (wheelStack.isEmpty()) return;

        ItemStack tireDyeStack = items[SLOT_TIRE_DYE];
        ItemStack rimDyeStack  = items[SLOT_RIM_DYE];

        boolean hasTireDye = !tireDyeStack.isEmpty() && tireDyeStack.getItem() instanceof DyeItem;
        boolean hasRimDye  = !rimDyeStack.isEmpty()  && rimDyeStack.getItem()  instanceof DyeItem;
        if (!hasTireDye && !hasRimDye) return;

        WheelColorData existing = wheelStack.get(BigTiresComponents.WHEEL_COLOR);

        int newTireColor;
        int newRimColor;

        if (hasTireDye) {
            int dyeColor = ((DyeItem) tireDyeStack.getItem()).getDyeColor().getFireworkColor();
            newTireColor = (existing == null)
                    ? WheelColorData.firstDye(dyeColor)
                    : WheelColorData.mix(existing.tireColor(), dyeColor);
            tireDyeStack.shrink(1);
            if (tireDyeStack.isEmpty()) items[SLOT_TIRE_DYE] = ItemStack.EMPTY;
        } else {
            // Диск красится, шина остаётся
            newTireColor = (existing != null) ? existing.tireColor() : 0;
        }

        if (hasRimDye) {
            int dyeColor = ((DyeItem) rimDyeStack.getItem()).getDyeColor().getFireworkColor();
            newRimColor = (existing == null)
                    ? WheelColorData.firstDye(dyeColor)
                    : WheelColorData.mix(existing.rimColor(), dyeColor);
            rimDyeStack.shrink(1);
            if (rimDyeStack.isEmpty()) items[SLOT_RIM_DYE] = ItemStack.EMPTY;
        } else {
            // Шина красится, диск остаётся
            newRimColor = (existing != null) ? existing.rimColor() : 0;
        }

        wheelStack.set(BigTiresComponents.WHEEL_COLOR, new WheelColorData(newTireColor, newRimColor));
        setChanged();
    }

    /**
     * Сбрасывает цвет колеса к заводским настройкам (удаляет компонент).
     * Аналог «смывания» без котла — кнопка «Сбросить» в GUI.
     */
    public void resetColor() {
        ItemStack wheelStack = items[SLOT_WHEEL];
        if (wheelStack.isEmpty()) return;
        wheelStack.remove(BigTiresComponents.WHEEL_COLOR);
        setChanged();
    }

    // ── Container ─────────────────────────────────────────────────────────────

    @Override public int getContainerSize()  { return CONTAINER_SIZE; }
    @Override public boolean isEmpty()       { for (var s : items) if (!s.isEmpty()) return false; return true; }
    @Override public ItemStack getItem(int slot) { return items[slot]; }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack s = items[slot];
        if (s.isEmpty()) return ItemStack.EMPTY;
        if (s.getCount() <= amount) { items[slot] = ItemStack.EMPTY; setChanged(); return s; }
        ItemStack out = s.split(amount);
        setChanged();
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack s = items[slot]; items[slot] = ItemStack.EMPTY; return s;
    }

    @Override public void setItem(int slot, ItemStack stack) { items[slot] = stack; setChanged(); }
    @Override public boolean stillValid(Player player)       { return Container.stillValidBlockEntity(this, player); }
    @Override public void clearContent()                     { for (int i = 0; i < items.length; i++) items[i] = ItemStack.EMPTY; setChanged(); }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == SLOT_WHEEL)    return true;
        return (slot == SLOT_TIRE_DYE || slot == SLOT_RIM_DYE) && stack.getItem() instanceof DyeItem;
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        for (int i = 0; i < items.length; i++) {
            if (!items[i].isEmpty()) tag.put("Item" + i, items[i].save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for (int i = 0; i < items.length; i++) {
            items[i] = tag.contains("Item" + i)
                    ? ItemStack.parseOptional(registries, tag.getCompound("Item" + i))
                    : ItemStack.EMPTY;
        }
    }
}
