package ughdhdh.bigtires.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Кастомные RenderType для overlay-покраски колёс.
 * <p>
 * Расширяем {@link RenderType}, чтобы получить доступ к
 * {@code protected static} полям ({@code TRANSLUCENT_TRANSPARENCY}, {@code LIGHTMAP},
 * {@code POLYGON_OFFSET_LAYERING}, {@code COLOR_WRITE}).
 * <p>
 * Оба типа:
 * <ul>
 *   <li>Sampler0 = color_mask.png (передаётся как {@code maskTexture})</li>
 *   <li>Sampler2 = lightmap (привязывается рендер-системой автоматически)</li>
 *   <li>Alpha-blend: {@code SRC_ALPHA, ONE_MINUS_SRC_ALPHA} → overlay поверх базы</li>
 *   <li>Polygon offset: небольшой сдвиг к камере, исключает z-fighting</li>
 * </ul>
 */
public final class WheelColorRenderType extends RenderType {

    // Кэши, чтобы не создавать новый объект на каждый кадр
    private static final Map<ResourceLocation, RenderType> TIRE_CACHE = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> RIM_CACHE  = new HashMap<>();

    // Фиктивный конструктор — экземпляры этого класса не создаются
    private WheelColorRenderType(String name) {
        super(name, DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS,
                256, false, false, () -> {}, () -> {});
    }

    // ── Публичные фабричные методы ────────────────────────────────────────────

    /**
     * RenderType для overlay шины.
     * Шейдер читает <b>R-канал</b> маски: чем выше R, тем сильнее применяется цвет шины.
     *
     * @param maskTexture RL текстуры-маски (drift_tire_color_mask.png и т.д.)
     */
    public static RenderType tire(ResourceLocation maskTexture) {
        return TIRE_CACHE.computeIfAbsent(maskTexture, rl ->
                create(
                        "bigtires:wheel_tire_overlay:" + rl,
                        DefaultVertexFormat.BLOCK,
                        VertexFormat.Mode.QUADS,
                        256,
                        false,
                        true,  // sortOnUpload — нужно для прозрачности
                        CompositeState.builder()
                                .setShaderState(new ShaderStateShard(() -> WheelColorShaders.TIRE_OVERLAY))
                                .setTextureState(new TextureStateShard(rl, false, false))
                                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                                .setLightmapState(LIGHTMAP)
                                .setLayeringState(POLYGON_OFFSET_LAYERING)  // z-fighting fix
                                .setWriteMaskState(COLOR_WRITE)              // не пишем в depth
                                .createCompositeState(false)
                )
        );
    }

    /**
     * RenderType для overlay диска.
     * Шейдер читает <b>G-канал</b> маски: чем выше G, тем сильнее применяется цвет диска.
     *
     * @param maskTexture RL текстуры-маски (drift_tire_color_mask.png и т.д.)
     */
    public static RenderType rim(ResourceLocation maskTexture) {
        return RIM_CACHE.computeIfAbsent(maskTexture, rl ->
                create(
                        "bigtires:wheel_rim_overlay:" + rl,
                        DefaultVertexFormat.BLOCK,
                        VertexFormat.Mode.QUADS,
                        256,
                        false,
                        true,
                        CompositeState.builder()
                                .setShaderState(new ShaderStateShard(() -> WheelColorShaders.RIM_OVERLAY))
                                .setTextureState(new TextureStateShard(rl, false, false))
                                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                                .setLightmapState(LIGHTMAP)
                                .setLayeringState(POLYGON_OFFSET_LAYERING)
                                .setWriteMaskState(COLOR_WRITE)
                                .createCompositeState(false)
                )
        );
    }
}
