package org.vmstudio.washingitems.core.common;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.vmstudio.visor.api.common.player.VRPose;

public final class AddonUtils {
    public static final Vector3f WASH_ITEM_OFFSET = new Vector3f(0.0F, -0.04F, -0.16F);
    public static final double MAX_TIP_TO_CAULDRON_DISTANCE = 0.26D;
    public static final double MAX_PLAYER_TO_CAULDRON_DISTANCE_SQR = 25.0D;
    public static final double MIN_SWIRL_MOVE = 0.005D;
    public static final double MAX_SWIRL_MOVE = 0.20D;
    public static final double MIN_SWIRL_IMPULSE = 0.00035D;
    public static final double WASH_PROGRESS_REQUIRED = 0.78D;
    public static final double WASH_PROGRESS_DECAY = 0.012D;
    public static final double WASH_PROGRESS_PER_MOVE = 0.62D;
    public static final double WASH_PROGRESS_PER_SPIN = 4.2D;
    public static final int WASH_COOLDOWN_TICKS = 10;
    public static final int REQUIRED_WASH_TICKS = 42;

    private AddonUtils() {
    }

    public static @NotNull Vec3 getWashProbe(VRPose handPose) {
        Vector3f probe = handPose.getCustomVector(new Vector3f(WASH_ITEM_OFFSET)).add(handPose.getPosition());
        return new Vec3(probe.x(), probe.y(), probe.z());
    }

    public static boolean isWashable(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (stack.getItem() instanceof DyeableLeatherItem dyeableItem && dyeableItem.hasCustomColor(stack)) {
            return true;
        }

        return getBannerPatternCount(stack) > 0;
    }

    public static int getBannerPatternCount(ItemStack stack) {
        if (!(stack.getItem() instanceof BannerItem)) {
            return 0;
        }

        CompoundTag blockEntityTag = stack.getTagElement("BlockEntityTag");
        if (blockEntityTag == null || !blockEntityTag.contains("Patterns", 9)) {
            return 0;
        }

        return blockEntityTag.getList("Patterns", 10).size();
    }

    public static boolean removeLastBannerPattern(ItemStack stack) {
        CompoundTag blockEntityTag = stack.getTagElement("BlockEntityTag");
        if (blockEntityTag == null || !blockEntityTag.contains("Patterns", 9)) {
            return false;
        }

        ListTag patterns = blockEntityTag.getList("Patterns", 10);
        if (patterns.isEmpty()) {
            return false;
        }

        patterns.remove(patterns.size() - 1);
        if (patterns.isEmpty()) {
            blockEntityTag.remove("Patterns");
        }
        if (blockEntityTag.isEmpty()) {
            stack.removeTagKey("BlockEntityTag");
        }
        return true;
    }

    public static @Nullable WashContact findWashContact(Level level, Vec3 probe) {
        BlockPos centerPos = BlockPos.containing(probe);
        WashContact bestContact = null;
        double bestDistance = MAX_TIP_TO_CAULDRON_DISTANCE;

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos pos = centerPos.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.is(Blocks.WATER_CAULDRON) || !state.hasProperty(LayeredCauldronBlock.LEVEL)) {
                        continue;
                    }

                    AABB waterBox = getWaterBox(pos, state);
                    if (!waterBox.contains(probe)) {
                        continue;
                    }

                    double distance = distanceToWaterBox(waterBox, probe);
                    if (distance > bestDistance) {
                        continue;
                    }

                    bestDistance = distance;
                    bestContact = new WashContact(pos.immutable(), state, waterBox);
                }
            }
        }

        return bestContact;
    }

    public static AABB getWaterBox(BlockPos pos, BlockState state) {
        int level = state.getValue(LayeredCauldronBlock.LEVEL);
        double surfaceY = pos.getY() + (6.0D + level * 3.0D) / 16.0D;
        double topPadding = switch (level) {
            case 3 -> 0.18D;
            case 2 -> 0.05D;
            default -> 0.04D;
        };
        double expandedTop = Math.min(pos.getY() + 0.98D, surfaceY + topPadding);
        return new AABB(
                pos.getX() + 0.14D,
                pos.getY() + 0.08D,
                pos.getZ() + 0.14D,
                pos.getX() + 0.86D,
                expandedTop,
                pos.getZ() + 0.86D
        );
    }

    public static double calculateSwirlImpulse(BlockPos cauldronPos, Vec3 probe, Vec3 delta) {
        Vec3 center = new Vec3(cauldronPos.getX() + 0.5D, probe.y, cauldronPos.getZ() + 0.5D);
        Vec3 radial = new Vec3(probe.x - center.x, 0.0D, probe.z - center.z);
        return radial.x * delta.z - radial.z * delta.x;
    }

    public static Vec3 getCauldronCenter(BlockPos pos, AABB waterBox) {
        return new Vec3(pos.getX() + 0.5D, (waterBox.minY + waterBox.maxY) * 0.5D, pos.getZ() + 0.5D);
    }

    private static double distanceToWaterBox(AABB waterBox, Vec3 point) {
        double dx = Math.max(Math.max(waterBox.minX - point.x, 0.0D), point.x - waterBox.maxX);
        double dy = Math.max(Math.max(waterBox.minY - point.y, 0.0D), point.y - waterBox.maxY);
        double dz = Math.max(Math.max(waterBox.minZ - point.z, 0.0D), point.z - waterBox.maxZ);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static record WashContact(BlockPos pos, BlockState state, AABB waterBox) {
    }
}
