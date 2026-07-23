package ughdhdh.bigtires.client;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import ughdhdh.bigtires.BigTires;
import ughdhdh.bigtires.index.BigTiresComponents;

import java.util.List;

@EventBusSubscriber(modid = BigTires.MOD_ID)
public class WheelColorTooltip {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Integer tireColor = event.getItemStack().get(BigTiresComponents.TIRE_COLOR);
        Integer rimColor  = event.getItemStack().get(BigTiresComponents.RIM_COLOR);
        if (tireColor == null && rimColor == null) return;

        List<Component> tooltip = event.getToolTip();
        String registryId = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()).toString();

        int insertIndex = -1;
        for (int i = 0; i < tooltip.size(); i++) {
            if (tooltip.get(i).getString().equals(registryId)) { insertIndex = i; break; }
        }

        if (rimColor != null) {
            Component line = Component.translatable("tooltip.bigtires.rim_color",
                    String.format("#%06X", rimColor & 0xFFFFFF)).withStyle(ChatFormatting.GRAY);
            if (insertIndex >= 0) tooltip.add(insertIndex, line); else tooltip.add(line);
        }
        if (tireColor != null) {
            Component line = Component.translatable("tooltip.bigtires.tire_color",
                    String.format("#%06X", tireColor & 0xFFFFFF)).withStyle(ChatFormatting.GRAY);
            if (insertIndex >= 0) tooltip.add(insertIndex, line); else tooltip.add(line);
        }
    }
}
