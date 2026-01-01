package io.github.squid233.remembermymods.mixin;

import io.github.squid233.remembermymods.RememberMyMods;
import io.github.squid233.remembermymods.screen.ModIncompatibleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract void openWorldLoadLevelData(LevelStorageSource.LevelStorageAccess levelStorageAccess, Runnable runnable);

    @Redirect(
        method = "openWorld",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/worldselection/WorldOpenFlows;openWorldLoadLevelData(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Ljava/lang/Runnable;)V")
    )
    private void checkModCompatibility(WorldOpenFlows instance, LevelStorageSource.LevelStorageAccess levelStorageAccess, Runnable runnable) {
        var results = RememberMyMods.checkModCompatibility(levelStorageAccess.getLevelPath(LevelResource.ROOT));
        if (!results.isEmpty()) {
            minecraft.setScreen(new ModIncompatibleScreen(
                results,
                yes -> {
                    if (yes) {
                        openWorldLoadLevelData(levelStorageAccess, runnable);
                    } else {
                        levelStorageAccess.safeClose();
                        runnable.run();
                    }
                }
            ));
        } else {
            openWorldLoadLevelData(levelStorageAccess, runnable);
        }
    }
}
