package org.vmstudio.washingitems.core.server;

import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.washingitems.core.common.VisorWashingItems;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WashingItemsAddonServer implements VisorAddon {
    @Override
    public void onAddonLoad() {

    }

    @Override
    public @Nullable String getAddonPackagePath() {
        return "org.vmstudio.washingitems.core.server";
    }

    @Override
    public @NotNull String getAddonId() {
        return VisorWashingItems.MOD_ID;
    }

    @Override
    public @NotNull Component getAddonName() {
        return Component.literal(VisorWashingItems.MOD_NAME);
    }

    @Override
    public String getModId() {
        return VisorWashingItems.MOD_ID;
    }
}
