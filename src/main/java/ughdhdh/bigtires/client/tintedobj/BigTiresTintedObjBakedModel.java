package ughdhdh.bigtires.client.tintedobj;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Baked-модель для {@code bigtires:tinted_obj}. Квады уже посчитаны один раз
 * при запекании ({@link BigTiresTintedObjGeometry#bake}) — {@link #getQuads}
 * просто отдаёт готовый список, без какого-либо пересчёта на каждый кадр.
 * <p>
 * {@code side} (аргумент культинга) сознательно игнорируется — наша геометрия
 * произвольной формы (колёса), а не аккуратные кубы, поэтому "грани, флеш с
 * границей блока" в общем случае отсутствуют; всё возвращается как unculled.
 * Это соответствует поведению большинства колёс/органических моделей и в целом
 * безопасно (не приводит к пропаданию граней, максимум — чуть менее
 * агрессивный face culling, что для предметов вообще не имеет значения).
 */
public final class BigTiresTintedObjBakedModel implements IDynamicBakedModel {

    private final List<BakedQuad> quads;
    private final boolean useAmbientOcclusion;
    private final boolean isGui3d;
    private final boolean usesBlockLight;
    private final TextureAtlasSprite particle;
    private final ItemOverrides overrides;

    public BigTiresTintedObjBakedModel(List<BakedQuad> quads, boolean useAmbientOcclusion, boolean isGui3d,
                                       boolean usesBlockLight, TextureAtlasSprite particle, ItemOverrides overrides) {
        this.quads = quads;
        this.useAmbientOcclusion = useAmbientOcclusion;
        this.isGui3d = isGui3d;
        this.usesBlockLight = usesBlockLight;
        this.particle = particle;
        this.overrides = overrides;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                    RandomSource rand, ModelData extraData,
                                    net.minecraft.client.renderer.RenderType renderType) {
        return side == null ? quads : Collections.emptyList();
    }

    @Override public boolean useAmbientOcclusion() { return useAmbientOcclusion; }
    @Override public boolean isGui3d() { return isGui3d; }
    @Override public boolean usesBlockLight() { return usesBlockLight; }
    @Override public boolean isCustomRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleIcon() { return particle; }
    @Override public ItemOverrides getOverrides() { return overrides; }
}
