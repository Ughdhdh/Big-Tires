package ughdhdh.bigtires.client;

import com.mojang.logging.LogUtils;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.index.OffroadDataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import ughdhdh.bigtires.BigTires;

import java.util.HashMap;
import java.util.Map;

/**
 * Автоматически регистрирует color overlay для ЛЮБОГО колеса (BigTires, Offroad, другие моды).
 *
 * <h2>Как это работает</h2>
 * <ol>
 *   <li>{@link #preRegister()} — при загрузке клиента сканирует ВСЕ предметы с компонентом
 *       {@code TireLike} и pre-регистрирует {@link PartialModel} для потенциальных масок.</li>
 *   <li>{@link #onResourcesLoaded(ResourceManager)} — после загрузки ресурсов проверяет,
 *       у каких колёс действительно есть файл {@code color_mask.json},
 *       и только для них финализирует регистрацию в {@link WheelColorOverlayRegistry}.</li>
 * </ol>
 *
 * <h2>Соглашение об именах файлов</h2>
 *
 * <h3>Для шин из BigTires ({@code bigtires:item/drift_tire/block})</h3>
 * <pre>
 *   assets/bigtires/models/item/drift_tire/color_mask.json
 *   assets/bigtires/textures/block/drift_tire_color_mask.png
 * </pre>
 *
 * <h3>Для шин из Offroad или других модов ({@code offroad:item/fat_tire/block})</h3>
 * <pre>
 *   assets/bigtires/models/item/offroad/fat_tire/color_mask.json
 *   assets/bigtires/textures/block/offroad/fat_tire_color_mask.png
 * </pre>
 * Namespace стороннего мода добавляется как подпапка, чтобы избежать конфликтов имён.
 *
 * <h2>Добавление поддержки нового колеса</h2>
 * <ol>
 *   <li>Создай {@code color_mask.json} по шаблону ниже.</li>
 *   <li>Нарисуй {@code color_mask.png}: R-канал = шина, G-канал = диск.</li>
 *   <li>Больше ничего — Java код менять не нужно.</li>
 * </ol>
 *
 * <h3>Шаблон color_mask.json для шины Offroad</h3>
 * <pre>
 * {
 *   "parent": "block/air",
 *   "ambientocclusion": false,
 *   "textures": {
 *     "fat_tire": "bigtires:block/offroad/fat_tire_color_mask",
 *     "particle": "offroad:block/fat_tire"
 *   },
 *   "loader": "neoforge:obj",
 *   "flip_v": true,
 *   "model": "offroad:models/item/fat_tire/fat_tire.obj"
 * }
 * </pre>
 * Имя ключа текстуры ({@code "fat_tire"}) берётся из MTL-файла модели:
 * строка {@code map_Kd #fat_tire} → ключ {@code fat_tire}.
 *
 * <h2>Отладка: найти имена моделей Offroad</h2>
 * Включи уровень логирования DEBUG для пакета {@code ughdhdh.bigtires} — авторегистратор
 * выведет ResourceLocation каждой найденной шины.
 */
public final class WheelColorAutoRegistrar {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Все потенциальные маски: base model RL → данные для регистрации. */
    private static final Map<ResourceLocation, PendingEntry> PENDING = new HashMap<>();

    private record PendingEntry(PartialModel overlayModel,
                                ResourceLocation maskModelRL,
                                ResourceLocation maskTextureRL) {}

    // ── Шаг 1: до загрузки ресурсов ─────────────────────────────────────────

    /**
     * Сканирует все предметы с компонентом {@link TireLike} и pre-регистрирует
     * {@link PartialModel} для маски каждого (по naming convention).
     * <p>
     * Вызывается в {@code FMLClientSetupEvent.enqueueWork()} — до первого рендер-reload.
     */
    public static void preRegister() {
        PENDING.clear();

        BuiltInRegistries.ITEM.forEach(item -> {
            TireLike tireLike = item.components().get(OffroadDataComponents.TIRE);
            if (tireLike == null || tireLike.model().isEmpty()) return;

            ResourceLocation baseModelRL = tireLike.model().get();
            String wheelName = extractWheelName(baseModelRL);
            if (wheelName == null) return;

            // Отладочный вывод: позволяет найти имена шин из других модов
            LOGGER.debug("[WheelColor] Found tire: item={} model={}",
                    BuiltInRegistries.ITEM.getKey(item), baseModelRL);

            // Путь к маске учитывает namespace чтобы не было конфликтов:
            //   bigtires → "item/drift_tire/color_mask"
            //   offroad  → "item/offroad/fat_tire/color_mask"
            String ns = baseModelRL.getNamespace();
            String folder = ns.equals(BigTires.MOD_ID) ? "" : (ns + "/");

            ResourceLocation maskModelRL   = BigTires.path("item/" + folder + wheelName + "/color_mask");
            ResourceLocation maskTextureRL = BigTires.path("block/" + folder + wheelName + "_color_mask");

            // Регистрируем PartialModel — Flywheel испечёт её при загрузке ресурсов.
            // Если color_mask.json не существует, Flywheel загрузит missing model (без краша).
            PartialModel overlayModel = PartialModel.of(maskModelRL);
            PENDING.put(baseModelRL, new PendingEntry(overlayModel, maskModelRL, maskTextureRL));
        });

        LOGGER.debug("[WheelColor] Pre-registered {} potential overlay slots", PENDING.size());
    }

    // ── Шаг 2: после загрузки ресурсов ──────────────────────────────────────

    /**
     * Проверяет у каких колёс существует {@code color_mask.json} и финализирует
     * регистрацию в {@link WheelColorOverlayRegistry}.
     * <p>
     * Вызывается в {@code ResourceManagerReloadListener} — после каждого reload ресурсов
     * (включая {@code /reload}).
     */
    public static void onResourcesLoaded(ResourceManager rm) {
        WheelColorOverlayRegistry.clear();
        int active = 0;

        for (var e : PENDING.entrySet()) {
            ResourceLocation baseModelRL = e.getKey();
            PendingEntry pe = e.getValue();

            // Файл color_mask.json должен быть в assets/<ns>/models/<path>.json
            ResourceLocation jsonCheck = ResourceLocation.fromNamespaceAndPath(
                    pe.maskModelRL().getNamespace(),
                    "models/" + pe.maskModelRL().getPath() + ".json"
            );

            if (rm.getResource(jsonCheck).isPresent()) {
                WheelColorOverlayRegistry.register(baseModelRL, pe.overlayModel(), pe.maskTextureRL());
                LOGGER.debug("[WheelColor] ✓ Overlay active for {}", baseModelRL);
                active++;
            }
        }

        LOGGER.info("[WheelColor] Active color overlays: {}/{} wheels",
                active, PENDING.size());
    }

    // ── Утилиты ──────────────────────────────────────────────────────────────

    /**
     * Извлекает имя колеса из RL модели.
     * {@code "bigtires:item/drift_tire/block"} → {@code "drift_tire"}
     * {@code "offroad:item/fat_tire/block"}    → {@code "fat_tire"}
     */
    @Nullable
    private static String extractWheelName(ResourceLocation modelRL) {
        String path = modelRL.getPath(); // "item/drift_tire/block"
        String[] parts = path.split("/");
        // Берём второй с конца: [..., "drift_tire", "block"] → "drift_tire"
        return (parts.length >= 2) ? parts[parts.length - 2] : null;
    }

    private WheelColorAutoRegistrar() {}
}
