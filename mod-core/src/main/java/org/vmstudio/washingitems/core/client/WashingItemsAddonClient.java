package org.vmstudio.washingitems.core.client;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.washingitems.core.common.AddonNetworking;
import org.vmstudio.washingitems.core.common.VisorWashingItems;

public class WashingItemsAddonClient implements VisorAddon {
    @Override
    public void onAddonLoad() {
        AddonNetworking.initCommon();
    }

    @Override
    public @Nullable String getAddonPackagePath() {
        return "org.vmstudio.washingitems.core.client";
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
