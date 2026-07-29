package ughdhdh.bigtires.client.tintedobj;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class BigTiresObjParser {

    private BigTiresObjParser() {}

    public static BigTiresObjData parse(String text) {
        BigTiresObjData data = new BigTiresObjData();

        String currentMaterial = null;
        String currentGroup = null;

        for (String rawLine : text.split("\n", -1)) {
            String line = stripComment(rawLine).trim();
            if (line.isEmpty()) continue;

            String[] tok = line.split("\\s+");
            if (tok.length == 0) continue;

            switch (tok[0]) {
                case "v" -> {
                    if (tok.length < 4) continue;
                    data.positions.add(new Vector3f(
                            parseFloat(tok[1]), parseFloat(tok[2]), parseFloat(tok[3])));
                }
                case "vt" -> {
                    if (tok.length < 3) continue;
                    data.texCoords.add(new Vector2f(parseFloat(tok[1]), parseFloat(tok[2])));
                }
                case "vn" -> {
                    if (tok.length < 4) continue;
                    data.normals.add(new Vector3f(
                            parseFloat(tok[1]), parseFloat(tok[2]), parseFloat(tok[3])));
                }
                case "usemtl" -> currentMaterial = tok.length > 1 ? tok[1] : null;
                case "o", "g" -> currentGroup = tok.length > 1 ? tok[1] : null;
                case "f" -> {
                    if (tok.length < 4) continue; // грань должна иметь минимум 3 вершины
                    BigTiresObjData.VertexRef[] verts = new BigTiresObjData.VertexRef[tok.length - 1];
                    for (int i = 1; i < tok.length; i++) {
                        verts[i - 1] = parseVertexRef(tok[i], data);
                    }
                    addFaceTriangulated(data, currentMaterial, currentGroup, verts);
                }
                // mtllib / s / любые прочие директивы — сознательно игнорируются:
                // mtllib грузится отдельным вызовом (см. BigTiresTintedObjLoader),
                // smoothing groups (s) не используются — нормали берутся из vn или считаются по грани.
                default -> { /* пропускаем */ }
            }
        }

        return data;
    }

    /** Разбивает n-угольник (5+ вершин) на треугольники веером, затем каждый треугольник — в квад. */
    private static void addFaceTriangulated(BigTiresObjData data, String material, String group,
                                             BigTiresObjData.VertexRef[] verts) {
        if (verts.length == 3) {
            data.faces.add(new BigTiresObjData.Face(material, group, toQuad(verts[0], verts[1], verts[2])));
        } else if (verts.length == 4) {
            data.faces.add(new BigTiresObjData.Face(material, group, verts));
        } else {
            // Fan triangulation для 5+ вершин
            for (int i = 1; i < verts.length - 1; i++) {
                data.faces.add(new BigTiresObjData.Face(
                        material, group, toQuad(verts[0], verts[i], verts[i + 1])));
            }
        }
    }

    /**
     * Треугольник → вырожденный квад (последняя вершина дублируется). BakedQuad в Minecraft
     * всегда четырёхвершинный; дублирование последней вершины схлопывает одно ребро в ноль
     * длины, визуально давая ровно треугольник без искажений текстуры/нормалей.
     */
    private static BigTiresObjData.VertexRef[] toQuad(BigTiresObjData.VertexRef a,
                                                       BigTiresObjData.VertexRef b,
                                                       BigTiresObjData.VertexRef c) {
        return new BigTiresObjData.VertexRef[] { a, b, c, c };
    }

    /** Разбирает одну вершину грани формата {@code pos}, {@code pos/uv}, {@code pos//normal} или {@code pos/uv/normal}. */
    private static BigTiresObjData.VertexRef parseVertexRef(String token, BigTiresObjData data) {
        String[] parts = token.split("/", -1);
        int posIdx = resolveIndex(parts[0], data.positions.size());
        int uvIdx  = (parts.length > 1 && !parts[1].isEmpty()) ? resolveIndex(parts[1], data.texCoords.size()) : -1;
        int nrmIdx = (parts.length > 2 && !parts[2].isEmpty()) ? resolveIndex(parts[2], data.normals.size()) : -1;
        return new BigTiresObjData.VertexRef(posIdx, uvIdx, nrmIdx);
    }

    /**
     * OBJ-индексы 1-based; отрицательные — relative (-1 = последний добавленный элемент).
     * Возвращает 0-based индекс для использования в наших списках.
     */
    private static int resolveIndex(String raw, int currentListSize) {
        int i = Integer.parseInt(raw);
        return i < 0 ? currentListSize + i : i - 1;
    }

    private static String stripComment(String line) {
        int hash = line.indexOf('#');
        return hash >= 0 ? line.substring(0, hash) : line;
    }

    private static float parseFloat(String s) {
        return Float.parseFloat(s);
    }
}
