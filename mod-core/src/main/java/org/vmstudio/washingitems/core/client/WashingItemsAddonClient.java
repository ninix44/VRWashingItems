package org.vmstudio.washingitems.core.client;

import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.washingitems.core.client.overlays.VROverlayWashingItems;
import org.vmstudio.washingitems.core.client.overlays.VROverlayTemplateWashingItems;
import org.vmstudio.washingitems.core.common.VisorWashingItems;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WashingItemsAddonClient implements VisorAddon {
    @Override
    public void onAddonLoad() {
        VisorAPI.addonManager().getRegistries()
                .overlays()
                .registerComponents(
                        List.of(
                                new VROverlayWashingItems(
                                        this,
                                        VROverlayWashingItems.ID
                                ),
                                new VROverlayTemplateWashingItems(
                                        this,
                                        VROverlayTemplateWashingItems.ID
                                )
                        )
                );
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
