package ughdhdh.bigtires.index;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ughdhdh.bigtires.BigTires;
import ughdhdh.bigtires.content.blocks.dye_station.WheelDyeStationMenu;

public class BigTiresMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, BigTires.MOD_ID);

    /**
     * Тип меню покрасочной станции.
     * <p>
     * ВАЖНО (1.21.1): конструктор {@code MenuType<>} принимает
     * {@code (MenuSupplier<T>, FeatureFlagSet)} — НЕ {@code StreamCodec}
     * (тот вариант появился в более поздних версиях для синхронизации
     * дополнительных данных меню по сети). {@code FeatureFlagSet.of()} —
     * пустой набор, то есть меню доступно без каких-либо experimental-флагов.
     */
    public static final DeferredHolder<MenuType<?>, MenuType<WheelDyeStationMenu>> WHEEL_DYE_STATION =
            MENU_TYPES.register("wheel_dye_station",
                    () -> new MenuType<>(WheelDyeStationMenu::new, FeatureFlagSet.of()));

    public static void init(IEventBus modBus) {
        MENU_TYPES.register(modBus);
    }
}
