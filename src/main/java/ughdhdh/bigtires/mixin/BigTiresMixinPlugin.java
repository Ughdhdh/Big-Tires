package ughdhdh.bigtires.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Отфильтровывает опциональные compat-миксины на классы других модов, которых
 * может не быть установлено:

 *   <li>{@link MixinAdjustableWheelMountRenderer} — Suspension Wrench</li>
 *   <li>{@link MixinTracksPlusWheelMountRenderer} — Create Tracks Plus</li>

 */
public class BigTiresMixinPlugin implements IMixinConfigPlugin {

    private static final String SUSPENSION_WRENCH_RENDERER =
            "dev.ughdhdh.suspension_wrench.renderer.AdjustableWheelMountRenderer";
    private static final String TRACKS_PLUS_RENDERER =
            "dev.qwxon.tracks.content.blocks.wheel_mount.AdjustableWheelMountRenderer";

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("MixinAdjustableWheelMountRenderer")) {
            return targetClassName.equals(SUSPENSION_WRENCH_RENDERER);
        }
        if (mixinClassName.endsWith("MixinTracksPlusWheelMountRenderer")) {
            return targetClassName.equals(TRACKS_PLUS_RENDERER);
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}