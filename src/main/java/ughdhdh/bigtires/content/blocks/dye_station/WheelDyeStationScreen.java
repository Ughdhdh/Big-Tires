package ughdhdh.bigtires.content.blocks.dye_station;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import ughdhdh.bigtires.WheelColorData;
import ughdhdh.bigtires.index.BigTiresComponents;
import net.minecraft.world.item.ItemStack;

/**
 * Экран покрасочной станции.
 * <p>
 * Макет (176 × 166 px):
 * <pre>
 * ┌────────────────────────────────────────────┐
 * │           Покрасочная станция              │
 * │                                            │
 * │  [🎨 Шина]    [🔧 Колесо]  [🎨 Диск]     │
 * │   slot 1       slot 0        slot 2        │
 * │                                            │
 * │  [Покрасить]           [Сбросить цвет]     │
 * │                                            │
 * │  Цвет шины: ████  Цвет диска: ████         │
 * │                                            │
 * │           [инвентарь игрока]               │
 * └────────────────────────────────────────────┘
 * </pre>
 * Кнопки отправляют {@code handleInventoryButtonClick} на сервер через ванильный механизм.
 */
public class WheelDyeStationScreen extends AbstractContainerScreen<WheelDyeStationMenu> {

    public WheelDyeStationScreen(WheelDyeStationMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth  = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 94; // сдвигаем лейбл инвентаря выше
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos;
        int y = topPos;

        // Кнопка «Покрасить» (button id = 0)
        addRenderableWidget(Button.builder(
                Component.translatable("gui.bigtires.wheel_dye_station.apply"),
                btn -> Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, 0)
        ).bounds(x + 20, y + 60, 70, 18).build());

        // Кнопка «Сбросить цвет» (button id = 1)
        addRenderableWidget(Button.builder(
                Component.translatable("gui.bigtires.wheel_dye_station.reset"),
                btn -> Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, 1)
        ).bounds(x + 98, y + 60, 58, 18).build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        // ── Фон окна ─────────────────────────────────────────────────────────
        // Внешняя рамка (тёмная)
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF444444);
        // Внутренний фон (светло-серый)
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFFC6C6C6);

        // ── Фоны слотов ───────────────────────────────────────────────────────
        drawSlot(graphics, x + 43, y + 34);  // краситель шины
        drawSlot(graphics, x + 79, y + 34);  // колесо
        drawSlot(graphics, x + 115, y + 34); // краситель диска

        // ── Разделитель инвентаря ─────────────────────────────────────────────
        graphics.fill(x + 7, y + 81, x + imageWidth - 7, y + 82, 0xFF888888);

        // ── Превью цветов ─────────────────────────────────────────────────────
        ItemStack wheelStack = menu.slots.get(0).getItem();
        if (!wheelStack.isEmpty()) {
            var colorData = wheelStack.get(BigTiresComponents.WHEEL_COLOR);
            if (colorData != null) {
                // Цвет шины
                int tc = colorData.tireColor() | 0xFF000000;
                graphics.fill(x + 20, y + 87, x + 36, y + 95, tc);
                graphics.fill(x + 19, y + 86, x + 37, y + 96, tc & 0x44FFFFFF); // тонкая рамка
                graphics.fill(x + 20, y + 87, x + 36, y + 95, tc);

                // Цвет диска
                int rc = colorData.rimColor() | 0xFF000000;
                graphics.fill(x + 110, y + 87, x + 126, y + 95, rc);
                graphics.fill(x + 109, y + 86, x + 127, y + 96, rc & 0x44FFFFFF);
                graphics.fill(x + 110, y + 87, x + 126, y + 95, rc);
            }
        }

        // ── Стрелки (визуальные подсказки) ────────────────────────────────────
        // Слева → в слот колеса
        graphics.fill(x + 63, y + 42, x + 77, y + 44, 0xFF666666);
        // Справа → из слота колеса
        graphics.fill(x + 99, y + 42, x + 113, y + 44, 0xFF666666);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Заголовок окна
        graphics.drawString(font,
                Component.translatable("block.bigtires.wheel_dye_station"),
                imageWidth / 2 - font.width(Component.translatable("block.bigtires.wheel_dye_station")) / 2,
                6, 0x404040, false);

        // Подписи слотов
        graphics.drawString(font,
                Component.translatable("gui.bigtires.wheel_dye_station.tire"),
                27, 25, 0x404040, false);
        graphics.drawString(font,
                Component.translatable("gui.bigtires.wheel_dye_station.rim"),
                110, 25, 0x404040, false);

        // Лейбл инвентаря игрока
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);

        // Превью цветов — подписи
        ItemStack wheelStack = menu.slots.get(0).getItem();
        if (!wheelStack.isEmpty() && wheelStack.has(BigTiresComponents.WHEEL_COLOR)) {
            graphics.drawString(font,
                    Component.translatable("gui.bigtires.wheel_dye_station.tire_color"),
                    38, 84, 0x404040, false);
            graphics.drawString(font,
                    Component.translatable("gui.bigtires.wheel_dye_station.rim_color"),
                    128, 84, 0x404040, false);
        }
    }

    // ── Вспомогательные ───────────────────────────────────────────────────────

    /** Рисует фон слота в ванильном стиле (тёмно-серый квадрат 18×18). */
    private void drawSlot(GuiGraphics graphics, int x, int y) {
        // Тёмная рамка
        graphics.fill(x,      y,      x + 18, y + 18, 0xFF373737);
        // Левый + верхний выступ (светлые)
        graphics.fill(x,      y,      x + 17, y + 1,  0xFFAAAAAA);
        graphics.fill(x,      y,      x + 1,  y + 17, 0xFFAAAAAA);
        // Внутренний фон
        graphics.fill(x + 1,  y + 1,  x + 17, y + 17, 0xFF8B8B8B);
    }

    @Override
    protected boolean isHovering(int slotX, int slotY, int width, int height, double mouseX, double mouseY) {
        return super.isHovering(slotX, slotY, width, height, mouseX, mouseY);
    }
}
