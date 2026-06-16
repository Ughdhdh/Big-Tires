package ughdhdh.bigtires.index;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;
import ughdhdh.bigtires.BigTires;
import ughdhdh.bigtires.client.WheelColorOverlayRegistry;

/**
 * Flywheel PartialModels для всех шин BigTires + регистрация цветовых масок.
 * <p>
 * Конвенция именования (одинакова для всех колёс BigTires):
 * <ul>
 *   <li>Базовая модель:  {@code item/<name>/block}</li>
 *   <li>Overlay-модель:  {@code item/<name>/color_mask} (тот же .obj, другая текстура)</li>
 *   <li>Маска (текстура): {@code block/<name>_color_mask} (R=шина, G=диск)</li>
 * </ul>
 * Поэтому регистрация нового колеса под этот шаблон — один вызов {@link #registerWheel}.
 * <p>
 * Колёса из Offroad или сторонних аддонов в эту конвенцию НЕ попадают (другой неймспейс,
 * возможно другое именование) — для них регистрируй вручную через
 * {@link WheelColorOverlayRegistry#register} с явными RL. См. гайд в чате.
 */
public class BigTiresPartialModels {

    public static final PartialModel
            HUGE_TIRE             = item("huge_tire/block"),
            HUGE_WIDE_TIRE        = item("huge_wide_tire/block"),
            HUGE_ROWING_TIRE      = item("huge_rowing_tire/block"),
            HUGE_ROWING_WIDE_TIRE = item("huge_rowing_wide_tire/block"),
            BIG_TRACTOR_TIRE      = item("big_tractor_tire/block"),
            TRACTOR_TIRE          = item("tractor_tire/block"),
            TRUCK_TIRE            = item("truck_tire/block"),
            NARROW_TRUCK_TIRE     = item("narrow_truck_tire/block"),
            SMALL_TRUCK_TIRE      = item("small_truck_tire/block"),
            VINTAGE_TIRE          = item("vintage_tire/block"),
            MONSTER_JAM_TIRE      = item("monster_jam_tire/block"),
            BAMBOO_WHEEL          = item("bamboo_wheel/block"),
            DRIFT_TIRE            = item("drift_tire/block"),
            WOODEN_WHEEL          = item("wooden_wheel/block"),
            IRON_WHEEL            = item("iron_wheel/block");

    // ── Overlay-маски  ───────────────
    public static final PartialModel
            HUGE_TIRE_OVERLAY             = item("huge_tire/color_mask"),
            HUGE_WIDE_TIRE_OVERLAY        = item("huge_wide_tire/color_mask"),
            HUGE_ROWING_TIRE_OVERLAY      = item("huge_rowing_tire/color_mask"),
            HUGE_ROWING_WIDE_TIRE_OVERLAY = item("huge_rowing_wide_tire/color_mask"),
            BIG_TRACTOR_TIRE_OVERLAY      = item("big_tractor_tire/color_mask"),
            TRACTOR_TIRE_OVERLAY          = item("tractor_tire/color_mask"),
            TRUCK_TIRE_OVERLAY            = item("truck_tire/color_mask"),
            NARROW_TRUCK_TIRE_OVERLAY     = item("narrow_truck_tire/color_mask"),
            SMALL_TRUCK_TIRE_OVERLAY      = item("small_truck_tire/color_mask"),
            VINTAGE_TIRE_OVERLAY          = item("vintage_tire/color_mask"),
            MONSTER_JAM_TIRE_OVERLAY      = item("monster_jam_tire/color_mask"),
            BAMBOO_WHEEL_OVERLAY          = item("bamboo_wheel/color_mask"),
            DRIFT_TIRE_OVERLAY            = item("drift_tire/color_mask"),
            WOODEN_WHEEL_OVERLAY          = item("wooden_wheel/color_mask");

    // ── Регистрация ───────────────────────────────────────────────────────────

    public static void init() {
        // Все 14 колёс BigTires следуют единой конвенции именования —
        // регистрация через registerWheel() в одну строку на колесо.
        registerWheel("huge_tire",             HUGE_TIRE_OVERLAY);
        registerWheel("huge_wide_tire",        HUGE_WIDE_TIRE_OVERLAY);
        registerWheel("huge_rowing_tire",      HUGE_ROWING_TIRE_OVERLAY);
        registerWheel("huge_rowing_wide_tire", HUGE_ROWING_WIDE_TIRE_OVERLAY);
        registerWheel("big_tractor_tire",      BIG_TRACTOR_TIRE_OVERLAY);
        registerWheel("tractor_tire",          TRACTOR_TIRE_OVERLAY);
        registerWheel("truck_tire",            TRUCK_TIRE_OVERLAY);
        registerWheel("narrow_truck_tire",     NARROW_TRUCK_TIRE_OVERLAY);
        registerWheel("small_truck_tire",      SMALL_TRUCK_TIRE_OVERLAY);
        registerWheel("vintage_tire",          VINTAGE_TIRE_OVERLAY);
        registerWheel("monster_jam_tire",      MONSTER_JAM_TIRE_OVERLAY);
        registerWheel("bamboo_wheel",          BAMBOO_WHEEL_OVERLAY);
        registerWheel("drift_tire",            DRIFT_TIRE_OVERLAY);
        registerWheel("wooden_wheel",          WOODEN_WHEEL_OVERLAY);

        // ── Колёса Offroad / сторонних аддонов ───────────────────────────────
        // Другой неймспейс и, возможно, другое именование — регистрируй вручную:
        //
        // WheelColorOverlayRegistry.register(
        //         ResourceLocation.fromNamespaceAndPath("offroad", "item/some_tire/block"), // RL базовой модели Offroad
        //         item("offroad_compat/some_tire_OVERLAY"),                              // НАША overlay-модель (можно ссылаться на их .obj)
        //         BigTires.path("block/some_tire_OVERLAY")                               // НАША маска-текстура
        // );
    }

    /**
     * Регистрирует overlay для колеса BigTires по стандартной конвенции:
     * базовая модель {@code item/<name>/block}, маска {@code block/<name>_color_mask}.
     *
     * @param wheelName    имя колеса (как в путях файлов), например {@code "drift_tire"}
     * @param overlayModel overlay PartialModel (одна из *_OVERLAY констант выше)
     */
    private static void registerWheel(String wheelName, PartialModel overlayModel) {
        WheelColorOverlayRegistry.register(
                BigTires.path("item/" + wheelName + "/block"),
                overlayModel,
                BigTires.path("block/" + wheelName + "_overlay")
        );
    }

    private static PartialModel item(final String path) {
        return PartialModel.of(BigTires.path("item/" + path));
    }
}
