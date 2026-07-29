package ughdhdh.bigtires.client.tintedobj;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Unbaked-геометрия для {@code bigtires:tinted_obj} loader'а.
 * <p>
 * Хранит распарсенные {@code .obj}+{@code .mtl} данные (см. {@link BigTiresObjData},
 * {@link BigTiresMtlParser}) плюс настройки из model JSON: связку материал→текстурный
 * ключ ({@code textures}), связку имя группы→tintIndex ({@code tint_groups}), флаги
 * ({@code automatic_culling}, {@code shade_quads}, {@code flip_v}) и опциональный
 * фильтр {@code only_tint_index} (см. {@link #onlyTintIndex}) — позволяет запечь из
 * одного {@code .obj} несколько независимо красящихся под-моделей (используется
 * для покраски колёс на WheelMount, см. {@code ughdhdh.bigtires.index.BigTiresPartialModels}).
 * <p>
 * {@link #bake} превращает всё это в список {@link BakedQuad}, готовых к рендеру —
 * ОДИН РАЗ при запекании модели, не на каждый кадр.
 */
public final class BigTiresTintedObjGeometry implements IUnbakedGeometry<BigTiresTintedObjGeometry> {

    private final BigTiresObjData objData;
    private final Map<String, BigTiresMtlParser.Material> materials;
    /** JSON-ключ "textures": материал (из .mtl, после {@code map_Kd}) → текстурный ключ модели. */
    private final Map<String, String> materialToTextureKey;
    /** JSON-ключ "tint_groups": имя группы {@code o}/{@code g} → tintIndex. Регистронезависимо. */
    private final Map<String, Integer> tintGroups;
    private final boolean automaticCulling;
    private final boolean shadeQuads;
    private final boolean flipV;
    /** Если не null — при запекании остаются только грани с этим резолвленным tintIndex. */
    @Nullable
    private final Integer onlyTintIndex;

    public BigTiresTintedObjGeometry(BigTiresObjData objData,
                                     Map<String, BigTiresMtlParser.Material> materials,
                                     Map<String, String> materialToTextureKey,
                                     Map<String, Integer> tintGroups,
                                     boolean automaticCulling,
                                     boolean shadeQuads,
                                     boolean flipV,
                                     @Nullable Integer onlyTintIndex) {
        this.objData = objData;
        this.materials = materials;
        this.materialToTextureKey = materialToTextureKey;
        this.tintGroups = tintGroups;
        this.automaticCulling = automaticCulling;
        this.shadeQuads = shadeQuads;
        this.flipV = flipV;
        this.onlyTintIndex = onlyTintIndex;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter,
                           ModelState modelState, ItemOverrides overrides) {
        Transformation transform = modelState.getRotation();

        List<BakedQuad> quads = new ArrayList<>(objData.faces.size());

        for (BigTiresObjData.Face face : objData.faces) {
            int tintIndex = resolveTintIndex(face.groupName());
            if (onlyTintIndex != null && tintIndex != onlyTintIndex) continue;

            TextureAtlasSprite sprite = resolveSprite(context, spriteGetter, face.materialName());
            quads.add(bakeQuad(context, face, sprite, tintIndex, transform));
        }

        TextureAtlasSprite particle = spriteGetter.apply(context.getMaterial("particle"));

        return new BigTiresTintedObjBakedModel(
                quads,
                context.useAmbientOcclusion(),
                context.isGui3d(),
                context.useBlockLight(),
                particle,
                overrides,
                context.getTransforms()
        );
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        // Наша геометрия не ссылается на другие модели — резолвить нечего.
    }

    // ── Резолвинг текстуры и тинта

    private TextureAtlasSprite resolveSprite(IGeometryBakingContext context,
                                             Function<Material, TextureAtlasSprite> spriteGetter,
                                             @Nullable String materialName) {
        String textureKey = null;
        if (materialName != null) {
            BigTiresMtlParser.Material mat = materials.get(materialName);
            if (mat != null && mat.diffuseTextureKey() != null) {
                // map_Kd в .mtl обычно ссылается на #<ключ>, но materialToTextureKey из JSON
                // может явно переопределить это соответствие — приоритет у явного JSON-маппинга.
                textureKey = materialToTextureKey.getOrDefault(mat.diffuseTextureKey(), mat.diffuseTextureKey());
            }
        }
        if (textureKey == null) textureKey = "particle"; // разумный fallback

        Material material = context.getMaterial(textureKey);
        return spriteGetter.apply(material);
    }

    private int resolveTintIndex(@Nullable String groupName) {
        if (groupName == null) return -1;
        Integer idx = tintGroups.get(groupName.toLowerCase(java.util.Locale.ROOT));
        return idx != null ? idx : -1;
    }

    // ── Baking одной грани (уже гарантированно квад — см. BigTiresObjParser)

    private BakedQuad bakeQuad(IGeometryBakingContext context, BigTiresObjData.Face face, TextureAtlasSprite sprite,
                               int tintIndex, Transformation transform) {
        Vector3f faceNormal = computeFaceNormal(face);

        net.minecraft.core.Direction direction = net.minecraft.core.Direction.getNearest(
                faceNormal.x, faceNormal.y, faceNormal.z);

        QuadBakingVertexConsumer consumer = new QuadBakingVertexConsumer();
        consumer.setSprite(sprite);
        consumer.setDirection(direction);
        consumer.setTintIndex(tintIndex);
        consumer.setShade(shadeQuads);
        consumer.setHasAmbientOcclusion(context.useAmbientOcclusion());

        for (BigTiresObjData.VertexRef ref : face.vertices()) {
            Vector3f rawPos = objData.positions.get(ref.positionIndex());
            // В отличие от ванильных block-model "elements" (диапазон 0-16 на блок),
            // экспорт этого .obj уже в блоках (1 единица = 1 блок) — координаты
            // используются как есть, без масштабирования.
            Vector4f pos4 = new Vector4f(rawPos.x, rawPos.y, rawPos.z, 1.0f);
            transform.transformPosition(pos4);

            Vector3f normal;
            if (shadeQuads && ref.normalIndex() >= 0 && !objData.normals.isEmpty()) {
                normal = new Vector3f(objData.normals.get(ref.normalIndex()));
                transform.transformNormal(normal);
            } else {
                normal = new Vector3f(faceNormal);
            }

            float u = 0f, v = 0f;
            if (ref.texCoordIndex() >= 0) {
                Vector2f uv = objData.texCoords.get(ref.texCoordIndex());
                u = uv.x;
                v = flipV ? 1.0f - uv.y : uv.y;
            }

            // sprite.getU/getV в этой версии API принимают диапазон 0-1 напрямую,
            // не 0-16 (та старая конвенция была для более ранней версии, до overhaul'а
            // системы вершин между 1.20.6 и 1.21).
            float atlasU = sprite.getU(u);
            float atlasV = sprite.getV(v);

            consumer.addVertex(pos4.x, pos4.y, pos4.z);
            consumer.setColor(255, 255, 255, 255); // белый — тинт применяется отдельно через tintIndex
            consumer.setUv(atlasU, atlasV);
            consumer.setUv1(0, 0);   // overlay — не используется
            consumer.setUv2(15 << 4, 15 << 4); // полная яркость по умолчанию; реальная светимость
                                                // пересчитывается движком динамически при рендере блока/айтема
            consumer.setNormal(normal.x, normal.y, normal.z);
        }

        return consumer.bakeQuad();
    }

    private Vector3f computeFaceNormal(BigTiresObjData.Face face) {
        Vector3f a = objData.positions.get(face.vertices()[0].positionIndex());
        Vector3f b = objData.positions.get(face.vertices()[1].positionIndex());
        Vector3f c = objData.positions.get(face.vertices()[2].positionIndex());
        Vector3f edge1 = new Vector3f(b).sub(a);
        Vector3f edge2 = new Vector3f(c).sub(a);
        Vector3f normal = new Vector3f(edge1).cross(edge2);
        if (normal.lengthSquared() < 1e-10f) return new Vector3f(0, 1, 0); // вырожденная грань — фолбэк
        return normal.normalize();
    }
}
