package ughdhdh.bigtires.content.blocks.dye_station;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import ughdhdh.bigtires.index.BigTiresMenuTypes;

/**
 * Меню покрасочной станции.
 * <p>
 * Позиции слотов относительно верхнего-левого угла GUI (176×166):
 * <pre>
 *   [Краситель шины]  [Колесо]  [Краситель диска]
 *       (44, 35)     (80, 35)     (116, 35)
 * </pre>
 * Кнопка «Покрасить»: {@link #clickMenuButton(Player, int)} с id=0.<br>
 * Кнопка «Сбросить»:  {@link #clickMenuButton(Player, int)} с id=1.
 */
public class WheelDyeStationMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess access;

    // ── Серверный конструктор (используется BlockEntity) ─────────────────────

    public static WheelDyeStationMenu forServer(int containerId, Inventory playerInventory,
                                                WheelDyeStationBlockEntity be,
                                                ContainerLevelAccess access) {
        return new WheelDyeStationMenu(containerId, playerInventory, be, access);
    }

    // ── Клиентский конструктор (используется MenuType factory) ───────────────

    public WheelDyeStationMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, makeClientContainer(), ContainerLevelAccess.NULL);
    }

    private static Container makeClientContainer() {
        return new net.minecraft.world.SimpleContainer(WheelDyeStationBlockEntity.CONTAINER_SIZE);
    }

    // ── Общий конструктор ────────────────────────────────────────────────────

    private WheelDyeStationMenu(int containerId, Inventory playerInventory,
                                Container container, ContainerLevelAccess access) {
        super(BigTiresMenuTypes.WHEEL_DYE_STATION.get(), containerId);
        this.access = access;

        checkContainerSize(container, WheelDyeStationBlockEntity.CONTAINER_SIZE);

        // ── Слот колеса (центр) ──────────────────────────────────────────────
        addSlot(new Slot(container, WheelDyeStationBlockEntity.SLOT_WHEEL, 80, 35));

        // ── Слот красителя шины (слева) ──────────────────────────────────────
        addSlot(new Slot(container, WheelDyeStationBlockEntity.SLOT_TIRE_DYE, 44, 35) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.getItem() instanceof DyeItem; }
        });

        // ── Слот красителя диска (справа) ────────────────────────────────────
        addSlot(new Slot(container, WheelDyeStationBlockEntity.SLOT_RIM_DYE, 116, 35) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.getItem() instanceof DyeItem; }
        });

        // ── Инвентарь игрока ─────────────────────────────────────────────────
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // Хотбар
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    // ── Кнопки GUI ───────────────────────────────────────────────────────────

    /**
     * Обрабатывает нажатие кнопок:
     * <ul>
     *   <li>id=0 → применить красители</li>
     *   <li>id=1 → сбросить цвет к заводским</li>
     * </ul>
     */
    @Override
    public boolean clickMenuButton(Player player, int id) {
        access.execute((level, pos) -> {
            if (level.getBlockEntity(pos) instanceof WheelDyeStationBlockEntity be) {
                if (id == 0) be.applyDyes();
                else if (id == 1) be.resetColor();
                broadcastChanges();
            }
        });
        return true;
    }

    // ── Shift-клик ────────────────────────────────────────────────────────────

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return result;

        ItemStack stack = slot.getItem();
        result = stack.copy();

        // Из блока → в инвентарь
        if (index < 3) {
            if (!moveItemStackTo(stack, 3, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            // Из инвентаря → красители в соответствующий слот, остальное в слот колеса
            if (stack.getItem() instanceof DyeItem) {
                if (!moveItemStackTo(stack, 1, 3, false)) return ItemStack.EMPTY;
            } else {
                if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();

        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate(
                (level, pos) -> Container.stillValidBlockEntity(level.getBlockEntity(pos), player),
                true);
    }
}
