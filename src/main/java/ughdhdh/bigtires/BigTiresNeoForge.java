package ughdhdh.bigtires;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import ughdhdh.bigtires.index.BigTiresMenuTypes;

@Mod(BigTires.MOD_ID)
public class BigTiresNeoForge {

    public BigTiresNeoForge(final IEventBus modBus, final ModContainer container) {
        BigTires.getRegistrate().registerEventListeners(modBus);
        BigTires.init();

        // Регистрация типов меню (покрасочная станция)
        BigTiresMenuTypes.init(modBus);

        // Регистрация cauldron-взаимодействий после завершения регистрации предметов
        modBus.addListener((FMLCommonSetupEvent event) ->
                event.enqueueWork(WheelColorCauldronInteraction::registerAll));
    }
}
