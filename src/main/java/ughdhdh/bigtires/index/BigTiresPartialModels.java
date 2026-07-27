package ughdhdh.bigtires.index;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;
import ughdhdh.bigtires.BigTires;

import java.util.ArrayList;
import java.util.List;

/**
 * PartialModel-регистрация для BigTires.
 * <p>
 * <b>Колёса BigTires покрашены через {@code bigtires:tinted_obj} loader
 * (tintIndex по группам {@code tire}/{@code rim} в .obj — см. пакет
 * {@code ughdhdh.bigtires.client.tintedobj}), но на {@code WheelMount} они всё
 * равно рендерятся через Flywheel {@link PartialModel}
 * (см. {@code MotorcycleWheelMountRenderer}/{@code MixinWheelMountRenderer}).</b>
 * <p>
 * {@link PartialModel#of} использует {@code computeIfAbsent}: если модель уже
 * была зарегистрирована ЗАРАНЕЕ (до {@code ModelBakeEvent}), она попадает в
 * очередь допзапекания Flywheel, и после бака её {@code bakedModel} поле
 * заполняется. Модели, никогда не зарегистрированные через {@link PartialModel#of}
 * до бака, остаются с {@code bakedModel == null} навсегда — отсюда
 * placeholder-блок вместо колеса на WheelMount, если модель тут не заведена.
 * <p>
 * <b>ВАЖНО — {@code PartialModel.ALL} хранит значения через {@code WeakReference}
 * ({@code new MapMaker().weakValues().makeMap()}).</b> Если ни один объект в
 * программе не держит обычную (сильную) ссылку на результат {@link PartialModel#of},
 * GC рано или поздно соберёт эту запись — и следующий вызов {@code .of()} для
 * того же {@code ResourceLocation} создаст НОВЫЙ объект с {@code bakedModel == null},
 * даже если модель БЫЛА зарегистрирована заранее и успешно запечена. Поэтому все
 * результаты {@link PartialModel#of} обязаны быть сохранены в {@link #KEPT_ALIVE}.
 * <p>
 * <h3>Покраска на WheelMount — раздельные под-модели на колесо</h3>
 * {@code SuperByteBuffer.renderInto(...)} (используется и в
 * {@code MotorcycleWheelMountRenderer}, и в {@code MixinWheelMountRenderer})
 * рендерит буфер как есть, БЕЗ учёта tintIndex — это не полноценный блок-рендер
 * с {@code BlockColors}, а прямая заливка предзапечённых вершин. tintIndex,
 * работающий для айтемов через {@code ItemColor}, тут никак не резолвится.
 * <p>
 * Единственный способ покрасить часть модели через существующий движок —
 * замешать цвет ДО рендера через {@code SuperByteBuffer.color(...)}, а он
 * красит буфер целиком одним цветом. Значит шину и диск, которым нужны РАЗНЫЕ
 * цвета, нельзя красить одним {@code renderInto} вызова — они должны быть
 * ОТДЕЛЬНЫМИ баферами.
 * <p>
 * Поэтому для каждого колеса, помимо базовой {@code .../block} модели (используется
 * только как fallback/для получения bounding-геометрии, не рендерится напрямую),
 * зарегистрированы под-модели с {@code only_tint_index} в JSON (см.
 * {@code BigTiresTintedObjLoader}), содержащие каждая только часть исходного
 * {@code .obj}:
 * <ul>
 *   <li>{@code block_tire} — грани группы {@code tire} (tintIndex 0), оригинальная текстура</li>
 *   <li>{@code block_rim} — грани группы {@code rim} (tintIndex 1), оригинальная текстура</li>
 *   <li>{@code block_neutral} — все остальные грани (например {@code shaft} у
 *       {@code huge_rowing_tire}/{@code huge_rowing_wide_tire}) — без тинта,
 *       у большинства колёс эта под-модель пустая (0 квадов) и просто не рендерит
 *       ничего, что безвредно.</li>
 *   <li>{@code block_tire_dyed} / {@code block_rim_dyed} — ТЕ ЖЕ грани, но с
 *       десатурированной (полностью обесцвеченной, см. {@code *_dyed_base.png})
 *       версией текстуры. Используются ТОЛЬКО когда реально выбран цвет
 *       ({@code TIRE_COLOR}/{@code RIM_COLOR} присутствует) — иначе рендер шёл бы
 *       по оригинальной, местами уже не нейтральной текстуре (дерево, ржавчина
 *       и т.п.), и умножение на выбранный цвет давало бы грязный/неправильный
 *       оттенок. Десатурация здесь — на уровне ассета (PNG), а не кода: обычный
 *       tintIndex-множитель в стандартном шейдере блоков и так делает
 *       per-pixel {@code texture × vertexColor} — достаточно, чтобы текстура
 *       была нейтральной, чтобы получить чистый "colorize"-эффект.</li>
 * </ul>
 * Рендерер выбирает: цвет задан → {@code *_dyed} вариант + tint; цвет не задан →
 * обычный вариант без тинта (естественный вид). {@code block_neutral} рендерится
 * как есть в любом случае.
 * <p>
 * {@link #registerModels()} вызывается из конструктора мода (до запекания).
 */
public class BigTiresPartialModels {

    /**
     * Список путей (relative к {@code models/item/}) всех колёс BigTires,
     * у которых есть {@code TireLike.model()} (см. {@code BigTireLikes}).
     * Из каждого пути {@code <wheel>/block} выводятся {@code <wheel>/block_tire},
     * {@code <wheel>/block_rim}, {@code <wheel>/block_neutral},
     * {@code <wheel>/block_tire_dyed}, {@code <wheel>/block_rim_dyed} — см. {@link #variantOf}.
     */
    private static final List<String> WHEEL_BASE_PATHS = List.of(
            "huge_tire/block",
            "huge_wide_tire/block",
            "huge_rowing_tire/block",
            "huge_rowing_wide_tire/block",
            "big_tractor_tire/block",
            "tractor_tire/block",
            "truck_tire/block",
            "narrow_truck_tire/block",
            "small_truck_tire/block",
            "monster_jam_tire/block",
            "bamboo_wheel/block",
            "vintage_tire/block",
            "drift_tire/block",
            "wooden_wheel/block",
            "iron_wheel/block",
            "traction_engine_wheel/block",
            "small_traction_engine_wheel/block",
            "steel_traction_engine_wheel/block",
            "small_steel_traction_engine_wheel/block"
    );

    // Сильные ссылки на ВСЕ зарегистрированные PartialModel — живут всё время
    // работы клиента, не дают GC собрать записи из PartialModel.ALL (weakValues!)
    // до того, как Flywheel успеет заполнить их bakedModel после запекания.
    private static final List<PartialModel> KEPT_ALIVE = new ArrayList<>();

    // ── Этап 1: статическая регистрация ДО запекания ──────────────────────────

    public static void registerModels() {
        for (String basePath : WHEEL_BASE_PATHS) {
            item(basePath);
            item(basePath + "_tire");
            item(basePath + "_rim");
            item(basePath + "_neutral");
            item(basePath + "_tire_dyed");
            item(basePath + "_rim_dyed");
        }
    }

    // ── Этап 2: (в данный момент не используется) ──────────────────────────

    public static void init() {
        // Пусто: у BigTires-колёс нет overlay-регистрации, они красятся через
        // tintIndex кастомного tinted_obj loader'а (см. javadoc класса).
    }

    // ── Доступ из рендереров ────────────────────────────────────────────────

    /** {@code .../block_tire} под-модель (естественная текстура, без тинта) для {@code baseModelRL}. */
    public static PartialModel tireVariant(ResourceLocation baseModelRL) {
        return PartialModel.of(variantOf(baseModelRL, "_tire"));
    }

    /** {@code .../block_rim} под-модель (естественная текстура, без тинта) для {@code baseModelRL}. */
    public static PartialModel rimVariant(ResourceLocation baseModelRL) {
        return PartialModel.of(variantOf(baseModelRL, "_rim"));
    }

    /** {@code .../block_neutral} под-модель для {@code baseModelRL} (нетонированные грани). */
    public static PartialModel neutralVariant(ResourceLocation baseModelRL) {
        return PartialModel.of(variantOf(baseModelRL, "_neutral"));
    }

    /** {@code .../block_tire_dyed} — десатурированная текстура, использовать вместе с {@code .color(tireColor)}. */
    public static PartialModel tireVariantDyed(ResourceLocation baseModelRL) {
        return PartialModel.of(variantOf(baseModelRL, "_tire_dyed"));
    }

    /** {@code .../block_rim_dyed} — десатурированная текстура, использовать вместе с {@code .color(rimColor)}. */
    public static PartialModel rimVariantDyed(ResourceLocation baseModelRL) {
        return PartialModel.of(variantOf(baseModelRL, "_rim_dyed"));
    }

    private static ResourceLocation variantOf(ResourceLocation baseModelRL, String suffix) {
        return ResourceLocation.fromNamespaceAndPath(baseModelRL.getNamespace(), baseModelRL.getPath() + suffix);
    }

    private static PartialModel item(final String path) {
        PartialModel model = PartialModel.of(BigTires.path("item/" + path));
        KEPT_ALIVE.add(model);
        return model;
    }
}
