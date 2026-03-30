package org.vmstudio.washingitems.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.washingitems.core.client.WashingItemsAddonClient;
import org.vmstudio.washingitems.core.common.AddonNetworking;
import org.vmstudio.washingitems.core.common.VisorWashingItems;
import org.vmstudio.washingitems.core.network.NetworkHelper;
import org.vmstudio.washingitems.core.server.WashingItemsAddonServer;
import org.vmstudio.washingitems.forge.network.ForgeNetworkChannel;

@Mod(VisorWashingItems.MOD_ID)
public class WashingItemsMod {
    public WashingItemsMod() {
        NetworkHelper.setChannel(new ForgeNetworkChannel(new ResourceLocation(VisorWashingItems.MOD_ID, "network")));
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
