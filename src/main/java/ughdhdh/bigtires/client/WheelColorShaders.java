package ughdhdh.bigtires.client;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import ughdhdh.bigtires.BigTires;

import java.io.IOException;

/**
 * Регистрирует два overlay-шейдера для покраски колёс.
 * <p>
 * {@link #TIRE_OVERLAY} — читает R-канал маски, применяет цвет шины.<br>
 * {@link #RIM_OVERLAY}  — читает G-канал маски, применяет цвет диска.
 * <p>
 * Оба шейдера используют общий вершинный шейдер
 * {@code assets/bigtires/shaders/core/wheel_color_overlay.vsh}.
 */
@EventBusSubscriber(modid = BigTires.MOD_ID, value = Dist.CLIENT)
public class WheelColorShaders {

    /** Шейдер покраски шины (резиновая часть). Null до загрузки ресурсов. */
    public static ShaderInstance TIRE_OVERLAY = null;

    /** Шейдер покраски диска (металлический обод). Null до загрузки ресурсов. */
    public static ShaderInstance RIM_OVERLAY = null;

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(BigTires.MOD_ID, "wheel_tire_color_overlay"),
                        DefaultVertexFormat.BLOCK
                ),
                shader -> TIRE_OVERLAY = shader
        );

        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(BigTires.MOD_ID, "wheel_rim_color_overlay"),
                        DefaultVertexFormat.BLOCK
                ),
                shader -> RIM_OVERLAY = shader
        );
    }
}
