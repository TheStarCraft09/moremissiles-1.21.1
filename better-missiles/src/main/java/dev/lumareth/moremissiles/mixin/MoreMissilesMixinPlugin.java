package dev.lumareth.moremissiles.mixin;

import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Skips mixins into optional mods that aren't installed. Mixins for a mod live in a sub-package named after it.
 */
public class MoreMissilesMixinPlugin implements IMixinConfigPlugin {
    private static final String CREATE_RADARS_PACKAGE = "dev.lumareth.moremissiles.mixin.createradars.";

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith(CREATE_RADARS_PACKAGE)) {
            return isLoaded("create_radar") && isLoaded("create");
        }
        return true;
    }

    private static boolean isLoaded(String modId) {
        // ModList isn't available this early, but the loading mod list is
        return LoadingModList.get().getModFileById(modId) != null;
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
