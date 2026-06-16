package ughdhdh.bigtires;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import ughdhdh.bigtires.content.blocks.dye_station.WheelDyeStationScreen;
import ughdhdh.bigtires.index.BigTiresMenuTypes;

@Mod(value = BigTires.MOD_ID, dist = Dist.CLIENT)
public class BigTiresNeoForgeClient {

    public BigTiresNeoForgeClient(final IEventBus modBus, final ModContainer container) {
        BigTires.clientInit(); // → BigTiresPartialModels.init() → WheelColorOverlayRegistry.register()

        // WheelColorShaders регистрируется автоматически через @EventBusSubscriber

        // Экран покрасочной станции
        modBus.addListener((RegisterMenuScreensEvent event) ->
                event.register(BigTiresMenuTypes.WHEEL_DYE_STATION.get(), WheelDyeStationScreen::new));
    }
}
