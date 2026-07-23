package ughdhdh.bigtires.client.tintedobj;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Парсер текста {@code .mtl} (material library, спутник {@code .obj}).
 * <p>
 * Разбирает полный стандартный набор директив ({@code Ka/Kd/Ks/Ns/d/Tr/illum/map_*}),
 * но для запекания реально используется только {@code map_Kd} — Minecraft не поддерживает
 * PBR-свойства материалов (Ka/Ks/Ns и т.п.), это то же ограничение, что и у официального
 * {@code neoforge:obj} loader.
 * <p>
 * {@code map_Kd} обычно ссылается на текстурный ключ вида {@code #huge_tire} — префикс
 * {@code #} означает "смотри в блоке {@code textures} модели JSON", ровно та же конвенция,
 * что использует вся ванильная система блок-моделей для {@code #texture_variable}.
 */
public final class BigTiresMtlParser {

    private BigTiresMtlParser() {}

    public record Material(String name, String diffuseTextureKey) {}

    public static Map<String, Material> parse(String text) {
        Map<String, Material> result = new LinkedHashMap<>();

        String currentName = null;
        String diffuseKey = null;

        for (String rawLine : text.split("\n", -1)) {
            String line = stripComment(rawLine).trim();
            if (line.isEmpty()) continue;

            String[] tok = line.split("\\s+", 2);
            if (tok.length == 0) continue;

            switch (tok[0]) {
                case "newmtl" -> {
                    // Сохраняем предыдущий материал перед началом нового
                    if (currentName != null) {
                        result.put(currentName, new Material(currentName, diffuseKey));
                    }
                    currentName = tok.length > 1 ? tok[1].trim() : null;
                    diffuseKey  = null;
                }
                case "map_Kd" -> {
                    if (tok.length > 1) {
                        diffuseKey = stripHash(tok[1].trim());
                    }
                }
                case "Ka", "Ks", "Ns", "d", "Tr", "illum",
                     "map_Ka", "map_Ks", "map_Ns", "map_Bump", "map_d" -> { /* игнорируем осознанно */ }
                default -> { /* неизвестная директива — пропускаем */ }
            }
        }

        if (currentName != null) {
            result.put(currentName, new Material(currentName, diffuseKey));
        }

        return result;
    }

    private static String stripHash(String s) {
        return s.startsWith("#") ? s.substring(1) : s;
    }

    private static String stripComment(String line) {
        int hash = line.indexOf('#');
        if (line.trim().startsWith("map_Kd")) return line;
        return hash >= 0 ? line.substring(0, hash) : line;
    }
}
