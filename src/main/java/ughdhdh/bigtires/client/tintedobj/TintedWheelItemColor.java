package ughdhdh.bigtires.client.tintedobj;

import net.minecraft.world.item.ItemStack;
import net.minecraft.client.color.item.ItemColor;
import ughdhdh.bigtires.index.BigTiresComponents;

/**
 * {@link ItemColor} для колёс, использующих {@code bigtires:tinted_obj} loader.
 * <p>
 * {@code tintIndex} приходит из группы {@code .obj}-модели (см. {@link BigTiresTintedObjLoader}
 * — {@code "tint_groups": {"tube": 0, "cylinder": 1}}):
 * <ul>
 *   <li>0 → {@link BigTiresComponents#TIRE_COLOR} (резина)</li>
 *   <li>1 → {@link BigTiresComponents#RIM_COLOR} (диск)</li>
 *   <li>иначе, или компонент отсутствует → {@code 0xFFFFFF} (белый = текстура без изменений)</li>
 * </ul>
 * Не требует ни overlay-геометрии, ни второй текстуры — цвет умножается движком
 * прямо на вершины базовой модели при рендере, тем же механизмом, что красит
 * траву/листву/кожаную броню.
 */
public final class TintedWheelItemColor implements ItemColor {

    public static final TintedWheelItemColor INSTANCE = new TintedWheelItemColor();

    private TintedWheelItemColor() {}

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        Integer color = switch (tintIndex) {
            case 0 -> stack.get(BigTiresComponents.TIRE_COLOR);
            case 1 -> stack.get(BigTiresComponents.RIM_COLOR);
            default -> null;
        };
        return color != null ? (color | 0xFF000000) : 0xFFFFFFFF;
    }
}
