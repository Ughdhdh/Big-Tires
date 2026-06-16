package ughdhdh.bigtires.client;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Реестр overlay-моделей для покраски колёс.
 * <p>
 * Ключ = RL базовой модели шины (из {@code TireLike.model()}),
 * например {@code bigtires:item/drift_tire/block}.
 * <p>
 * Заполняется {@link WheelColorAutoRegistrar} после каждого reload ресурсов.
 */
public final class WheelColorOverlayRegistry {

    private static final Map<ResourceLocation, Entry> REGISTRY = new HashMap<>();

    public record Entry(PartialModel overlayModel, ResourceLocation maskTexture) {}

    /** Регистрирует overlay для колеса. Вызывается из {@link WheelColorAutoRegistrar}. */
    public static void register(ResourceLocation baseModelRL,
                                PartialModel overlayModel,
                                ResourceLocation maskTexture) {
        REGISTRY.put(baseModelRL, new Entry(overlayModel, maskTexture));
    }

    /** Сбрасывает все регистрации (вызывается перед каждым reload). */
    public static void clear() {
        REGISTRY.clear();
    }

    @Nullable
    public static PartialModel getOverlayModel(ResourceLocation baseModelRL) {
        Entry e = REGISTRY.get(baseModelRL);
        return e != null ? e.overlayModel() : null;
    }

    @Nullable
    public static ResourceLocation getMaskTexture(ResourceLocation baseModelRL) {
        Entry e = REGISTRY.get(baseModelRL);
        return e != null ? e.maskTexture() : null;
    }

    public static boolean has(ResourceLocation baseModelRL) {
        return REGISTRY.containsKey(baseModelRL);
    }

    private WheelColorOverlayRegistry() {}
}
