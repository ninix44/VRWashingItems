package org.vmstudio.washingitems.core.client.tasks;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseClient;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.tasks.RegisterVisorTask;
import org.vmstudio.visor.api.client.tasks.TaskType;
import org.vmstudio.visor.api.client.tasks.VisorTask;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.washingitems.core.common.AddonNetworking;
import org.vmstudio.washingitems.core.common.AddonUtils;
import org.vmstudio.washingitems.core.network.NetworkHelper;

import java.util.EnumMap;

@RegisterVisorTask
public class TaskWashItems extends VisorTask {
    public static final String ID = "wash_items";

    private final EnumMap<HandType, HandState> handStates = new EnumMap<>(HandType.class);

    public TaskWashItems(@NotNull VisorAddon owner) {
        super(owner);
        for (HandType handType : HandType.values()) {
            handStates.put(handType, new HandState());
        }
    }

    @Override
    protected void onRun(@Nullable LocalPlayer player) {
        Minecraft minecraft = Minecraft.getInstance();
        if (player == null || player.level() == null || minecraft.isPaused()) {
            return;
        }

        PlayerPoseClient pose = VisorAPI.client().getVRLocalPlayer().getPoseData(PlayerPoseType.TICK);
        for (HandType handType : HandType.values()) {
            tickHand(player, player.level(), pose, handType);
        }
    }

    private void tickHand(LocalPlayer player, Level level, PlayerPoseClient pose, HandType handType) {
        HandState handState = handStates.get(handType);
        handState.tickCooldown();

        InteractionHand interactionHand = handType.asInteractionHand();
        ItemStack heldStack = player.getItemInHand(interactionHand);
        if (!AddonUtils.isWashable(heldStack)) {
            handState.reset();
            return;
        }

        Vec3 probe = AddonUtils.getWashProbe(pose.getGripHand(handType));
        AddonUtils.WashContact contact = AddonUtils.findWashContact(level, probe);
        if (contact == null) {
            handState.loseContact();
            return;
        }

        if (!handState.matches(contact.pos())) {
            handState.startTracking(contact.pos(), probe);
            return;
        }

        handState.restoreContact();
        Vec3 delta = probe.subtract(handState.lastProbe);
        handState.lastProbe = probe;

        double horizontalSpeed = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        if (horizontalSpeed < AddonUtils.MIN_SWIRL_MOVE || horizontalSpeed > AddonUtils.MAX_SWIRL_MOVE) {
            handState.soften();
            return;
        }

        double swirlImpulse = Math.abs(AddonUtils.calculateSwirlImpulse(contact.pos(), probe, delta));
        if (swirlImpulse < AddonUtils.MIN_SWIRL_IMPULSE) {
            handState.soften();
            return;
        }

        handState.washTicks++;
        handState.progress = Math.min(
                AddonUtils.WASH_PROGRESS_REQUIRED,
                handState.progress + horizontalSpeed * AddonUtils.WASH_PROGRESS_PER_MOVE + swirlImpulse * AddonUtils.WASH_PROGRESS_PER_SPIN
        );

        if (handState.effectCooldown <= 0) {
            spawnWashEffects(level, contact, probe, delta, handType);
            handState.effectCooldown = 4;
        }

        if (handState.progress < AddonUtils.WASH_PROGRESS_REQUIRED || handState.washTicks < AddonUtils.REQUIRED_WASH_TICKS) {
            return;
        }

        sendWashPacket(interactionHand, contact.pos());
        VisorAPI.client().getInputManager().triggerHapticPulse(handType, 0.12F);
        spawnCompletionEffects(level, contact, delta);
        handState.onWashComplete();
    }

    private void spawnWashEffects(Level level, AddonUtils.WashContact contact, Vec3 probe, Vec3 delta, HandType handType) {
        Vec3 center = AddonUtils.getCauldronCenter(contact.pos(), contact.waterBox());
        level.addParticle(
                ParticleTypes.SPLASH,
                probe.x,
                Math.min(probe.y, contact.waterBox().maxY),
                probe.z,
                delta.x * 0.4D,
                0.06D,
                delta.z * 0.4D
        );
        if (level.random.nextInt(2) == 0) {
            level.addParticle(
                    ParticleTypes.BUBBLE,
                    center.x + (level.random.nextDouble() - 0.5D) * 0.25D,
                    contact.waterBox().minY + 0.08D,
                    center.z + (level.random.nextDouble() - 0.5D) * 0.25D,
                    0.0D,
                    0.03D,
                    0.0D
            );
        }

        VisorAPI.client().getInputManager().triggerHapticPulse(handType, 0.03F);
        if (level.random.nextInt(5) == 0) {
            level.playLocalSound(center.x, center.y, center.z, SoundEvents.WATER_AMBIENT, SoundSource.PLAYERS, 0.2F, 1.1F, false);
        }
    }

    private void spawnCompletionEffects(Level level, AddonUtils.WashContact contact, Vec3 delta) {
        Vec3 center = AddonUtils.getCauldronCenter(contact.pos(), contact.waterBox());
        for (int i = 0; i < 8; i++) {
            level.addParticle(
                    ParticleTypes.SPLASH,
                    center.x + (level.random.nextDouble() - 0.5D) * 0.35D,
                    contact.waterBox().maxY,
                    center.z + (level.random.nextDouble() - 0.5D) * 0.35D,
                    delta.x * 0.35D + (level.random.nextDouble() - 0.5D) * 0.06D,
                    0.11D,
                    delta.z * 0.35D + (level.random.nextDouble() - 0.5D) * 0.06D
            );
        }
        level.playLocalSound(center.x, center.y, center.z, SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.4F, 1.0F, false);
    }

    private void sendWashPacket(InteractionHand hand, BlockPos pos) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeEnum(hand);
        buf.writeBlockPos(pos);
        NetworkHelper.sendToServer(AddonNetworking.WASH_ITEM_C2S, buf);
    }

    @Override
    protected void onClear(@Nullable LocalPlayer player) {
        handStates.values().forEach(HandState::reset);
    }

    @Override
    public boolean isActive(@Nullable LocalPlayer player) {
        return player != null && VisorAPI.clientState().stateMode().isActive();
    }

    @Override
    public @NotNull TaskType getType() {
        return TaskType.VR_PLAYER_TICK;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }

    private static final class HandState {
        private static final int CONTACT_GRACE_TICKS = 8;

        private BlockPos targetPos;
        private Vec3 lastProbe = Vec3.ZERO;
        private double progress;
        private int washTicks;
        private int cooldown;
        private int effectCooldown;
        private int missTicks;

        private void tickCooldown() {
            if (cooldown > 0) {
                cooldown--;
            }
            if (effectCooldown > 0) {
                effectCooldown--;
            }
        }

        private boolean matches(BlockPos pos) {
            return cooldown <= 0 && pos.equals(targetPos);
        }

        private void startTracking(BlockPos pos, Vec3 probe) {
            targetPos = pos.immutable();
            lastProbe = probe;
            progress = 0.0D;
            washTicks = 0;
            effectCooldown = 0;
            missTicks = 0;
        }

        private void restoreContact() {
            missTicks = 0;
        }

        private void soften() {
            if (progress > 0.0D) {
                progress = Math.max(0.0D, progress - AddonUtils.WASH_PROGRESS_DECAY);
            }
            if (washTicks > 0) {
                washTicks--;
            }
        }

        private void loseContact() {
            missTicks++;
            soften();
            if (missTicks <= CONTACT_GRACE_TICKS) {
                return;
            }

            targetPos = null;
            lastProbe = Vec3.ZERO;
            effectCooldown = 0;
            missTicks = 0;
        }

        private void onWashComplete() {
            targetPos = null;
            lastProbe = Vec3.ZERO;
            progress = 0.0D;
            washTicks = 0;
            cooldown = AddonUtils.WASH_COOLDOWN_TICKS;
            effectCooldown = 0;
            missTicks = 0;
        }

        private void reset() {
            targetPos = null;
            lastProbe = Vec3.ZERO;
            progress = 0.0D;
            washTicks = 0;
            cooldown = 0;
            effectCooldown = 0;
            missTicks = 0;
        }
    }
}
