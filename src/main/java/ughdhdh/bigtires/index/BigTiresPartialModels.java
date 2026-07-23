package ughdhdh.bigtires.index;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import ughdhdh.bigtires.BigTires;
import ughdhdh.bigtires.client.WheelColorOverlayRegistry;

/**
 * PartialModel-регистрация для BigTires.
 * <p>
 * <b>Колёса BigTires покрашены через {@code bigtires:tinted_obj} loader
 * (tintIndex по группам {@code tire}/{@code rim} в .obj — см. пакет
 * {@code ughdhdh.bigtires.client.tintedobj}), они НЕ используют overlay-модели
 * и не нуждаются в предрегистрации через {@link PartialModel#of} — их геометрия
 * запекается напрямую собственным loader'ом.</b>
 * <p>
 * Overlay-система (tire_mask/rim_mask + {@link WheelColorOverlayRegistry}) теперь
 * актуальна ТОЛЬКО для колёс Offroad (см. {@code OffroadWheelColorCompat}) — их
 * .obj-файлы не в нашем распоряжении для перегруппировки на tire/rim, поэтому
 * для них старая раздельная geometry-overlay покраска остаётся единственным
 * рабочим способом.
 * <p>
 * {@link #registerModels()} вызывается из конструктора мода (до запекания) —
 * сейчас регистрирует только overlay-модели Offroad-совместимости.
 */
public class BigTiresPartialModels {

    // ── Этап 1: статическая регистрация ДО запекания ──────────────────────────

    public static void registerModels() {
        // Offroad compat overlay-модели (tire + rim) — единственное, что ещё
        // нуждается в PartialModel-предрегистрации; колёса BigTires запекаются
        // своим tinted_obj loader'ом и в этом списке не участвуют.
        item("offroad_compat/small_tire_tire_mask");
        item("offroad_compat/small_tire_rim_mask");
        item("offroad_compat/tire_tire_mask");
        item("offroad_compat/tire_rim_mask");
        item("offroad_compat/large_tire_tire_mask");
        item("offroad_compat/large_tire_rim_mask");
        item("offroad_compat/monstrous_tire_tire_mask");
        item("offroad_compat/monstrous_tire_rim_mask");
    }

    // ── Этап 2: регистрация в оверлей-реестре после загрузки предметов ────────

    public static void init() {
        // Пусто: у BigTires-колёс больше нет overlay-регистрации (см. javadoc класса).
        // OffroadWheelColorCompat.init() регистрирует Offroad-совместимость отдельно —
        // вызывается сразу следом в BigTiresNeoForgeClient.
    }

    private static PartialModel item(final String path) {
        return PartialModel.of(BigTires.path("item/" + path));
    }
}
