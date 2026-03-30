package org.vmstudio.washingitems.fabric;

import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.washingitems.core.client.WashingItemsAddonClient;
import org.vmstudio.washingitems.core.server.WashingItemsAddonServer;
import net.fabricmc.api.ModInitializer;

public class WashingItemsMod implements ModInitializer {
    @Override
    public void onInitialize() {
        if (ModLoader.get().isDedicatedServer()) {
            VisorAPI.registerAddon(
                    new WashingItemsAddonServer()
            );
        } else {
            VisorAPI.registerAddon(
                    new WashingItemsAddonClient()
            );
        }
    }
}
