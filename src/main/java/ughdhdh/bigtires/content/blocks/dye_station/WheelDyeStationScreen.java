package ughdhdh.bigtires.content.blocks.dye_station;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import ughdhdh.bigtires.BigTires;
import ughdhdh.bigtires.index.BigTiresComponents;

/**
 * Экран покрасочной станции.
 * <p>
 * Фон рисуется текстурой {@link #TEXTURE} (176×166, как у ванильных GUI).
 * Позиции слотов в {@link WheelDyeStationMenu} обязаны совпадать с тем местом,
 * где на текстуре реально нарисованы "дырки" слотов.
 * <p>
 * Кнопки отправляют {@code handleInventoryButtonClick} на сервер через ванильный механизм.
 */
public class WheelDyeStationScreen extends AbstractContainerScreen<WheelDyeStationMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(BigTires.MOD_ID, "textures/gui/wheel_dye_station.png");

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
        ).bounds(x + 64, y + 55, 48, 15).build());

        // Кнопка «Сбросить цвет» (button id = 1)
        addRenderableWidget(Button.builder(
                Component.translatable("gui.bigtires.wheel_dye_station.reset"),
                btn -> Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, 1)
        ).bounds(x + 1, y + 1, 1, 1).build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        // Превью цветов (заливки) — рисуются ПОВЕРХ текстуры
        ItemStack wheelStack = menu.slots.get(0).getItem();
        if (!wheelStack.isEmpty()) {
            Integer tireColor = wheelStack.get(BigTiresComponents.TIRE_COLOR);
            Integer rimColor  = wheelStack.get(BigTiresComponents.RIM_COLOR);
            if (tireColor != null) {
                graphics.fill(leftPos + 34, topPos + 55, leftPos + 52, topPos + 64, tireColor | 0xFF000000);
            }
            if (rimColor != null) {
                graphics.fill(leftPos + 124, topPos + 55, leftPos + 142, topPos + 64, rimColor | 0xFF000000);
            }
        }
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
                33, 19, 0x404040, false);
        graphics.drawString(font,
                Component.translatable("gui.bigtires.wheel_dye_station.rim"),
                123, 19, 0x404040, false);

        // Лейбл инвентаря игрока
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }
}