package ughdhdh.bigtires.client.tintedobj;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Результат парсинга Wavefront {@code .obj} файла.
 * <p>
 * Хранит "сырые" данные ровно так, как они появляются в файле — без привязки
 * к текстурам/материалам-из-JSON и без запекания в {@link net.minecraft.client.renderer.block.model.BakedQuad}.
 * Baking (с учётом {@code tintIndex} по группе, спрайтов атласа и трансформаций)
 * делает отдельно {@link BigTiresTintedObjGeometry#bake}.
 */
public final class BigTiresObjData {


    public final List<Vector3f> positions = new ArrayList<>();

    /** UV-координаты ({@code vt u v}), индексация с 0. */
    public final List<Vector2f> texCoords = new ArrayList<>();

    public final List<Vector3f> normals = new ArrayList<>();

    public final List<Face> faces = new ArrayList<>();

    public record Face(String materialName, String groupName, VertexRef[] vertices) {}

    /**
     * Ссылка на одну вершину грани — тройка индексов {@code position/texCoord/normal}
     * в соответствующие списки {@link BigTiresObjData}. {@code texCoordIndex}/{@code normalIndex}
     * равны {@code -1}, если в файле для этой вершины они не заданы (формат {@code v//vn} или просто {@code v}).
     */
    public record VertexRef(int positionIndex, int texCoordIndex, int normalIndex) {}
}
