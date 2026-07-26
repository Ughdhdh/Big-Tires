package ughdhdh.bigtires.client.tintedobj;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Geometry loader для {@code bigtires:tinted_obj}.
 * <p>
 * Формат model JSON — расширение стандартного {@code neoforge:obj}, с одним
 * дополнительным полем {@code tint_groups}:
 * <pre>{@code
 * {
 *     "loader": "bigtires:tinted_obj",
 *     "model": "bigtires:models/item/huge_tire/huge_tire.obj",
 *     "mtl_override": "bigtires:models/item/huge_tire/huge_tire.mtl",  // опционально
 *     "textures": {
 *         "huge_tire": "bigtires:block/huge_tire",
 *         "particle": "bigtires:block/huge_tire"
 *     },
 *     // Имя группы (o/g в .obj) → tintIndex. Регистронезависимо.
 *     // Группы, не упомянутые здесь, получают tintIndex=-1 (без тинта).
 *     "tint_groups": {
 *         "tube": 0,
 *         "cylinder": 1
 *     },
 *     "automatic_culling": false,
 *     "shade_quads": false,
 *     "flip_v": true,
 *     // Опционально: если указано, при запекании остаются ТОЛЬКО грани, чей
 *     // резолвленный tintIndex (через tint_groups, см. выше) равен этому числу.
 *     // Позволяет получить из одного .obj НЕСКОЛЬКО независимо красящихся
 *     // под-моделей (например "только шина" / "только диск" / "остальное,
 *     // без тинта" — используется для покраски колёс на WheelMount, см.
 *     // ughdhdh.bigtires.index.BigTiresPartialModels).
 *     "only_tint_index": 0
 * }
 * }</pre>
 */
public final class BigTiresTintedObjLoader implements IGeometryLoader<BigTiresTintedObjGeometry> {

    public static final BigTiresTintedObjLoader INSTANCE = new BigTiresTintedObjLoader();
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath("bigtires", "tinted_obj");

    private BigTiresTintedObjLoader() {}

    @Override
    public BigTiresTintedObjGeometry read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        ResourceLocation modelRL = ResourceLocation.parse(json.get("model").getAsString());

        ResourceLocation mtlRL = json.has("mtl_override")
                ? ResourceLocation.parse(json.get("mtl_override").getAsString())
                : withExtension(modelRL, ".mtl");

        String objText = readResourceAsString(modelRL);
        String mtlText = readResourceAsString(mtlRL);

        BigTiresObjData objData = BigTiresObjParser.parse(objText);
        Map<String, BigTiresMtlParser.Material> materials = BigTiresMtlParser.parse(mtlText);

        Map<String, String> materialToTextureKey = new HashMap<>();

        Map<String, Integer> tintGroups = new LinkedHashMap<>();
        if (json.has("tint_groups")) {
            JsonObject tg = json.getAsJsonObject("tint_groups");
            for (Map.Entry<String, JsonElement> e : tg.entrySet()) {
                tintGroups.put(e.getKey().toLowerCase(Locale.ROOT), e.getValue().getAsInt());
            }
        }

        boolean automaticCulling = !json.has("automatic_culling") || json.get("automatic_culling").getAsBoolean();
        boolean shadeQuads       = !json.has("shade_quads") || json.get("shade_quads").getAsBoolean();
        boolean flipV            = json.has("flip_v") && json.get("flip_v").getAsBoolean();

        Integer onlyTintIndex = json.has("only_tint_index") ? json.get("only_tint_index").getAsInt() : null;

        return new BigTiresTintedObjGeometry(
                objData, materials, materialToTextureKey, tintGroups,
                automaticCulling, shadeQuads, flipV, onlyTintIndex
        );
    }

    private static ResourceLocation withExtension(ResourceLocation rl, String ext) {
        String path = rl.getPath();
        int dot = path.lastIndexOf('.');
        String base = dot >= 0 ? path.substring(0, dot) : path;
        return ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), base + ext);
    }

    private static String readResourceAsString(ResourceLocation rl) {
        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResourceOrThrow(rl);
            try (InputStream is = resource.open()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new JsonParseException("bigtires:tinted_obj: не удалось прочитать " + rl, e);
        }
    }
}
