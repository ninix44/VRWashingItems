package org.vmstudio.washingitems.core.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
//import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import org.vmstudio.washingitems.core.network.NetworkHelper;

public final class AddonNetworking {
    public static final ResourceLocation WASH_ITEM_C2S = new ResourceLocation(VisorWashingItems.MOD_ID, "wash_item");

    private static boolean initialized;

    private AddonNetworking() {
    }

    public static void initCommon() {
        if (initialized) {
            return;
        }
        initialized = true;

        NetworkHelper.registerServerReceiver(WASH_ITEM_C2S, (buf, player) -> {
            InteractionHand hand = buf.readEnum(InteractionHand.class);
            var pos = buf.readBlockPos();

            if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > AddonUtils.MAX_PLAYER_TO_CAULDRON_DISTANCE_SQR) {
                return;
            }

            var state = player.level().getBlockState(pos);
            if (!state.is(Blocks.WATER_CAULDRON) || !state.hasProperty(LayeredCauldronBlock.LEVEL)) {
                return;
            }

            ItemStack heldStack = player.getItemInHand(hand);
            boolean cleaned = false;

            if (heldStack.getItem() instanceof DyeableLeatherItem dyeableItem && dyeableItem.hasCustomColor(heldStack)) {
                dyeableItem.clearColor(heldStack);
                player.awardStat(Stats.CLEAN_ARMOR);
                cleaned = true;
            } else if (AddonUtils.removeLastBannerPattern(heldStack)) {
                player.awardStat(Stats.CLEAN_BANNER);
                cleaned = true;
            }

            if (!cleaned) {
                return;
            }

            LayeredCauldronBlock.lowerFillLevel(state, player.level(), pos);
            player.level().playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.55F, 1.05F);
            player.level().gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        });
    }
}
