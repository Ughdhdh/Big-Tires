package ughdhdh.bigtires;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(BigTires.MOD_ID)
public class BigTiresNeoForge {
    public BigTiresNeoForge(final IEventBus modBus, final ModContainer container) {
        BigTires.getRegistrate().registerEventListeners(modBus);
        BigTires.init();
    }
}
