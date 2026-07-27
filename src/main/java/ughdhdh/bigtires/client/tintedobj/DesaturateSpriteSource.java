package ughdhdh.bigtires.client.tintedobj;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.FastColor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Кастомный {@link SpriteSource} ({@code "type": "bigtires:desaturate"} в
 * {@code assets/bigtires/atlases/blocks.json}) — читает исходную текстуру
 * ({@code source}), убирает у неё насыщенность (luminance-preserving grayscale,
 * альфа не трогается) и кладёт результат в атлас под ID {@code sprite}.
 * <p>
 * Заменяет собой заранее нарисованные {@code *_dyed_base.png}: та же самая идея
 * ("текстура + полное убирание saturation, потом цвет через tintIndex"), но
 * сгенерированная на лету при стежке атласа, а не как отдельный файл в репозитории.
 * <p>
 * Используется для {@code block_tire_dyed.json}/{@code block_rim_dyed.json} —
 * они ссылаются на {@code bigtires:block/<wheel>_dyed_base}, и раньше это был
 * реальный PNG; теперь это ВИРТУАЛЬНЫЙ спрайт, генерируемый этим классом под тем
 * же самым именем, так что сами JSON-модели менять не пришлось.
 * <p>
 * <b>ВАЖНО:</b> сигнатуры {@link SpriteSource}/{@link SpriteContents} сверены с
 * реальным decompiled-исходником для MC 1.21.1/NeoForge 21.1.228. Единственное,
 * что ещё НЕ подтверждено напрямую (нет исходника под рукой на момент написания):
 * <ol>
 *   <li>Точная форма {@link SpriteSourceType} — предполагается конструктор,
 *       принимающий {@code MapCodec<? extends SpriteSource>}.</li>
 *   <li>Существование {@code ResourceMetadata.EMPTY} как способа получить "пустые"
 *       метаданные (нужно для конструктора {@link SpriteContents}).</li>
 * </ol>
 */
public record DesaturateSpriteSource(ResourceLocation source, Optional<ResourceLocation> sprite) implements SpriteSource {

    public static final MapCodec<DesaturateSpriteSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("source").forGetter(DesaturateSpriteSource::source),
            ResourceLocation.CODEC.optionalFieldOf("sprite").forGetter(DesaturateSpriteSource::sprite)
    ).apply(instance, DesaturateSpriteSource::new));

    /** Регистрируется через {@code RegisterSpriteSourceTypesEvent} (см. BigTiresNeoForgeClient). */
    public static final SpriteSourceType TYPE = new SpriteSourceType(CODEC);

    @Override
    public void run(ResourceManager resourceManager, Output output) {
        ResourceLocation spriteId = sprite.orElse(source);
        ResourceLocation resourceFile = SpriteSource.TEXTURE_ID_CONVERTER.idToFile(source);

        Optional<Resource> resourceOpt = resourceManager.getResource(resourceFile);
        if (resourceOpt.isEmpty()) return;
        Resource resource = resourceOpt.get();

        output.add(spriteId, (SpriteSource.SpriteSupplier) (loader) -> {
            try (InputStream is = resource.open()) {
                NativeImage original = NativeImage.read(is);
                NativeImage desaturated = desaturate(original);
                original.close();
                return new SpriteContents(
                        spriteId,
                        new FrameSize(desaturated.getWidth(), desaturated.getHeight()),
                        desaturated,
                        ResourceMetadata.EMPTY
                );
            } catch (IOException e) {
                throw new RuntimeException("bigtires:desaturate: не удалось прочитать " + resourceFile, e);
            }
        });
    }

    /** Luminance-preserving desaturation (Rec. 601), альфа-канал не изменяется. */
    private static NativeImage desaturate(NativeImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        NativeImage dst = new NativeImage(NativeImage.Format.RGBA, w, h, false);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = src.getPixelRGBA(x, y);
                int a = FastColor.ABGR32.alpha(argb);
                int r = FastColor.ABGR32.red(argb);
                int g = FastColor.ABGR32.green(argb);
                int b = FastColor.ABGR32.blue(argb);
                int gray = Math.round(0.299f * r + 0.587f * g + 0.114f * b);
                dst.setPixelRGBA(x, y, FastColor.ABGR32.color(a, gray, gray, gray));
            }
        }
        return dst;
    }

    @Override
    public SpriteSourceType type() {
        return TYPE;
    }
}
