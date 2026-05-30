package ughdhdh.bigtires;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(BigTires.MOD_ID)
public class BigTiresNeoForge {
    public BigTiresNeoForge(final IEventBus modBus, final ModContainer container) {
        // SimulatedRegistrate цепляется к mod event bus
        BigTires.getRegistrate().registerEventListeners(modBus);
        // RegistrationProvider из Veil регистрируется автоматически по MOD_ID
        BigTires.init();
    }
}
