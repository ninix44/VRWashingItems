package org.vmstudio.washingitems.forge;

import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.washingitems.core.client.WashingItemsAddonClient;
import org.vmstudio.washingitems.core.common.VisorWashingItems;
import org.vmstudio.washingitems.core.server.WashingItemsAddonServer;
import net.minecraftforge.fml.common.Mod;

@Mod(VisorWashingItems.MOD_ID)
public class WashingItemsMod {
    public WashingItemsMod() {
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
