package org.vmstudio.washingitems.fabric;

import net.fabricmc.api.ModInitializer;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.washingitems.core.client.WashingItemsAddonClient;
import org.vmstudio.washingitems.core.common.AddonNetworking;
import org.vmstudio.washingitems.core.network.NetworkHelper;
import org.vmstudio.washingitems.core.server.WashingItemsAddonServer;
import org.vmstudio.washingitems.fabric.network.FabricNetworkChannel;

public class WashingItemsMod implements ModInitializer {
    @Override
    public void onInitialize() {
        NetworkHelper.setChannel(new FabricNetworkChannel());
        AddonNetworking.initCommon();

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
