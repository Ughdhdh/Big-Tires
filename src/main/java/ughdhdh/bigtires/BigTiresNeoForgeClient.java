package ughdhdh.bigtires;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = BigTires.MOD_ID, dist = Dist.CLIENT)
public class BigTiresNeoForgeClient {
    public BigTiresNeoForgeClient(final IEventBus modBus, final ModContainer container) {
        BigTires.clientInit();
    }
}
