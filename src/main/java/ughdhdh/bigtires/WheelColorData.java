package ughdhdh.bigtires;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Хранит цвет шины (резина) и цвет диска (металл).
 * <p>
 * Цвета — целые числа 0xRRGGBB, как в {@code DyeColor.getFireworkColor()}.
 * По умолчанию компонент отсутствует (заводской вид), что значит «не крашено».
 *
 * <h3>Смешивание (leather armor style)</h3>
 * Каждое новое нанесение красителя вызывает {@link #mix(int, int)}.
 * Алгоритм идентичен ванильному {@code LeatherArmorItem.dyeArmor()}:
 * <ol>
 *   <li>Суммируем R, G, B компоненты существующего цвета и нового красителя.</li>
 *   <li>Берём средние значения.</li>
 *   <li>Масштабируем так, чтобы яркость (max-компонента) сохранялась.</li>
 * </ol>
 * Результат: красный + синий → насыщенный фиолетовый, а не тёмно-серый.
 */
public record WheelColorData(int tireColor, int rimColor) {

    public static final Codec<WheelColorData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("tire_color").forGetter(WheelColorData::tireColor),
            Codec.INT.fieldOf("rim_color").forGetter(WheelColorData::rimColor)
    ).apply(inst, WheelColorData::new));

    // ── Смешивание цветов (vanilla leather armor алгоритм) ───────────────────

    /**
     * Смешивает {@code currentColor} с цветом нового красителя {@code dyeColor}.
     * <p>
     * Если {@code currentColor} ещё не задан (первое нанесение), передай
     * {@code dyeColor} в оба аргумента или используй {@link #firstDye(int)}.
     *
     * @param currentColor существующий цвет (0xRRGGBB)
     * @param dyeColor     цвет нового красителя (DyeColor.getFireworkColor())
     * @return новый смешанный цвет (0xRRGGBB)
     */
    public static int mix(int currentColor, int dyeColor) {
        int r1 = (currentColor >> 16) & 0xFF;
        int g1 = (currentColor >> 8)  & 0xFF;
        int b1 =  currentColor        & 0xFF;
        int max1 = Math.max(r1, Math.max(g1, b1));

        int r2 = (dyeColor >> 16) & 0xFF;
        int g2 = (dyeColor >> 8)  & 0xFF;
        int b2 =  dyeColor        & 0xFF;
        int max2 = Math.max(r2, Math.max(g2, b2));

        // Суммируем компоненты и максимумы
        int totalR   = r1   + r2;
        int totalG   = g1   + g2;
        int totalB   = b1   + b2;
        int totalMax = max1 + max2;

        // Средние
        int avgR   = totalR   / 2;
        int avgG   = totalG   / 2;
        int avgB   = totalB   / 2;
        int avgMax = totalMax / 2;

        // Масштабируем, чтобы сохранить яркость
        int currentMax = Math.max(avgR, Math.max(avgG, avgB));
        if (currentMax == 0) return 0;

        int finalR = avgR * avgMax / currentMax;
        int finalG = avgG * avgMax / currentMax;
        int finalB = avgB * avgMax / currentMax;

        // Clamp 0–255
        finalR = Math.min(255, finalR);
        finalG = Math.min(255, finalG);
        finalB = Math.min(255, finalB);

        return (finalR << 16) | (finalG << 8) | finalB;
    }

    /**
     * Первое нанесение — краситель просто становится цветом без смешивания.
     *
     * @param dyeColor цвет красителя
     * @return тот же цвет без изменений
     */
    public static int firstDye(int dyeColor) {
        return dyeColor;
    }

    /** Возвращает копию с изменённым цветом шины. */
    public WheelColorData withTireColor(int color) {
        return new WheelColorData(color, this.rimColor);
    }

    /** Возвращает копию с изменённым цветом диска. */
    public WheelColorData withRimColor(int color) {
        return new WheelColorData(this.tireColor, color);
    }
}
