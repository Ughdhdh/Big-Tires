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

    /** Позиции вершин ({@code v x y z}), индексация с 0 (в файле — с 1, парсер уже пересчитал). */
    public final List<Vector3f> positions = new ArrayList<>();

    /** UV-координаты ({@code vt u v}), индексация с 0. */
    public final List<Vector2f> texCoords = new ArrayList<>();

    /** Нормали ({@code vn x y z}), индексация с 0. Может быть пустым, если модель их не содержит. */
    public final List<Vector3f> normals = new ArrayList<>();

    /** Все грани модели, в порядке появления в файле. */
    public final List<Face> faces = new ArrayList<>();

    /**
     * Одна грань (после triangulation остаётся ровно 4 вершины — см. {@link BigTiresObjParser}).
     *
     * @param materialName активный материал ({@code usemtl}) на момент этой грани; может быть {@code null},
     *                     если в файле не было ни одного {@code usemtl} до этой грани.
     * @param groupName    активная группа ({@code o}/{@code g}) на момент этой грани; может быть {@code null}.
     * @param vertices     ровно 4 вершины грани (квад). Если исходная грань была треугольником,
     *                     последняя вершина продублирована (см. {@link BigTiresObjParser#toQuad}).
     */
    public record Face(String materialName, String groupName, VertexRef[] vertices) {}

    /**
     * Ссылка на одну вершину грани — тройка индексов {@code position/texCoord/normal}
     * в соответствующие списки {@link BigTiresObjData}. {@code texCoordIndex}/{@code normalIndex}
     * равны {@code -1}, если в файле для этой вершины они не заданы (формат {@code v//vn} или просто {@code v}).
     */
    public record VertexRef(int positionIndex, int texCoordIndex, int normalIndex) {}
}
