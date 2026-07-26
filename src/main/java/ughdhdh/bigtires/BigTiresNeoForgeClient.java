package ughdhdh.bigtires;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import ughdhdh.bigtires.client.tintedobj.BigTiresTintedObjLoader;
import ughdhdh.bigtires.client.tintedobj.TintedWheelItemColor;
import ughdhdh.bigtires.content.blocks.dye_station.WheelDyeStationScreen;
import ughdhdh.bigtires.index.BigTiresMenuTypes;
import ughdhdh.bigtires.index.BigTiresPartialModels;

import java.util.ArrayList;
import java.util.List;

@Mod(value = BigTires.MOD_ID, dist = Dist.CLIENT)
public class BigTiresNeoForgeClient {

    public BigTiresNeoForgeClient(final IEventBus modBus, final ModContainer container) {
        // Регистрируем PartialModel-объекты СЕЙЧАС, до запекания моделей.
        // Flywheel должен знать о них до ModelBakeEvent — иначе текстура будет пустой.
        BigTiresPartialModels.registerModels();

        // Собственный geometry loader — tintIndex-покраска колёс без overlay-геометрии.
        modBus.addListener((ModelEvent.RegisterGeometryLoaders event) ->
                event.register(BigTiresTintedObjLoader.ID, BigTiresTintedObjLoader.INSTANCE));

        // ItemColor: tintIndex 0 → TIRE_COLOR, 1 → RIM_COLOR. Регистрируется на все
        // предметы неймспейса bigtires.
        modBus.addListener((RegisterColorHandlersEvent.Item event) -> {
            List<Item> wheelItems = new ArrayList<>();
            BuiltInRegistries.ITEM.forEach(item -> {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                if (id.getNamespace().equals(BigTires.MOD_ID)) wheelItems.add(item);
            });
            event.register(TintedWheelItemColor.INSTANCE, wheelItems.toArray(new Item[0]));
        });

        // Зарезервировано для пост-регистрационной инициализации (см. BigTiresPartialModels.init()).
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(
                BigTiresPartialModels::init));

        modBus.addListener((RegisterMenuScreensEvent event) ->
                event.register(BigTiresMenuTypes.WHEEL_DYE_STATION.get(), WheelDyeStationScreen::new));
    }
}
