package ughdhdh.bigtires.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;


public final class WheelColorRenderType {

    private static final Map<ResourceLocation, RenderType> CACHE = new HashMap<>();

    private WheelColorRenderType() {}

    public static RenderType overlay(ResourceLocation maskTexture) {
        return CACHE.computeIfAbsent(maskTexture, rl ->
                RenderType.entityTranslucent(rl, true)
        );
    }

    @Deprecated
    public static RenderType tire(ResourceLocation maskTexture) {
        return overlay(maskTexture);
    }

    @Deprecated
    public static RenderType rim(ResourceLocation maskTexture) {
        return overlay(maskTexture);
    }
}
