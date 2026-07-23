package ughdhdh.bigtires.client;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;


public final class WheelColorOverlayRegistry {

    private static final Map<ResourceLocation, Entry> REGISTRY = new HashMap<>();

    /** Полная регистрация — и шина, и диск красятся (используется для колёс BigTires).
     */
    public static void register(ResourceLocation itemId,
                                PartialModel tireModel, ResourceLocation tireMaskTex,
                                PartialModel rimModel, ResourceLocation rimMaskTex) {
        REGISTRY.put(itemId, new Entry(tireModel, tireMaskTex, rimModel, rimMaskTex));
    }

    public static void registerTireOnly(ResourceLocation itemId,
                                        PartialModel tireModel, ResourceLocation tireMaskTex) {
        REGISTRY.put(itemId, new Entry(tireModel, tireMaskTex, null, null));
    }

    @Nullable
    public static PartialModel getTireModel(ResourceLocation itemId) {
        Entry e = REGISTRY.get(itemId);
        return e != null ? e.tireModel() : null;
    }

    @Nullable
    public static ResourceLocation getTireMaskTexture(ResourceLocation itemId) {
        Entry e = REGISTRY.get(itemId);
        return e != null ? e.tireMaskTex() : null;
    }

    @Nullable
    public static PartialModel getRimModel(ResourceLocation itemId) {
        Entry e = REGISTRY.get(itemId);
        return e != null ? e.rimModel() : null;
    }

    @Nullable
    public static ResourceLocation getRimMaskTexture(ResourceLocation itemId) {
        Entry e = REGISTRY.get(itemId);
        return e != null ? e.rimMaskTex() : null;
    }

    public static boolean has(ResourceLocation itemId) {
        return REGISTRY.containsKey(itemId);
    }

    private record Entry(PartialModel tireModel, ResourceLocation tireMaskTex,
                         @Nullable PartialModel rimModel, @Nullable ResourceLocation rimMaskTex) {}

    private WheelColorOverlayRegistry() {}
}
