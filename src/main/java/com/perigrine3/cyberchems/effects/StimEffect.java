package com.perigrine3.cyberchems.effects;

import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.cyberchems.Cyberchems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

public class StimEffect extends MobEffect {

    private static final String HUMANITY_KEY = "cc_drug_penalty_stim";

    private static final int HUMANITY_PENALTY_PER_LEVEL = 5;

    private static final int BUFF_REFRESH_TICKS = 40;

    private static final int AFTER_SLOWNESS_TICKS = 20 * 60 * 5;
    private static final int AFTER_SLOWNESS_AMP = 0;

    private static final float BASE_DODGE_CHANCE = 0.20f;
    private static final float MAX_DODGE_CHANCE = 0.90f;

    private static final int DODGE_COOLDOWN_TICKS = 10;
    private static final String NBT_LAST_DODGE_TICK = "cc_stim_last_dodge_tick";

    private static final int SPEED_AMP_CAP = 4;
    private static final int SPEED_AMP_PER_LEVEL = 1;

    private static final float DODGE_PUSH_BASE = 0.65f;
    private static final float DODGE_PUSH_PER_LEVEL = 0.25f;
    private static final float DODGE_PUSH_MAX = 1.60f;

    public StimEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity living, int amplifier) {
        if (!(living instanceof Player player)) {
            return true;
        }

        if (player.level().isClientSide) {
            return true;
        }

        applyHumanityPenalty(player, amplifier);

        int speedAmp = Math.min(SPEED_AMP_CAP, (amplifier + 1) * SPEED_AMP_PER_LEVEL);
        ensureEffect(player, speedAmp);

        return true;
    }

    @Override
    public void onMobRemoved(LivingEntity living, int amplifier, Entity.RemovalReason reason) {
        if (!(living instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide) {
            return;
        }

        clearHumanityPenalty(player);

        if (player.isDeadOrDying() || player.isRemoved() || reason == Entity.RemovalReason.KILLED) {
            return;
        }

        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                AFTER_SLOWNESS_TICKS,
                AFTER_SLOWNESS_AMP,
                false,
                false,
                false
        ));
    }

    private static void applyHumanityPenalty(Player player, int amplifier) {
        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) {
            return;
        }

        int level = amplifier + 1;
        int penalty = level * HUMANITY_PENALTY_PER_LEVEL;

        data.setHumanityPenalty(player, HUMANITY_KEY, penalty);
    }

    private static void clearHumanityPenalty(Player player) {
        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) {
            return;
        }

        data.clearHumanityPenalty(player, HUMANITY_KEY);
    }

    private static void ensureEffect(Player player, int amplifier) {
        MobEffectInstance cur = player.getEffect(MobEffects.MOVEMENT_SPEED);
        if (cur != null && cur.getAmplifier() == amplifier && cur.getDuration() > (BUFF_REFRESH_TICKS / 2)) {
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, BUFF_REFRESH_TICKS, amplifier, false, true, true));
    }

    private static float dodgeChance(int amplifier) {
        float chance = (float) (BASE_DODGE_CHANCE * Math.pow(2.0, amplifier));
        return Mth.clamp(chance, 0f, MAX_DODGE_CHANCE);
    }

    private static void doDodge(Player player, int amplifier, Entity attacker) {
        float push = DODGE_PUSH_BASE + (amplifier * DODGE_PUSH_PER_LEVEL);
        push = Mth.clamp(push, 0f, DODGE_PUSH_MAX);

        double yawRad = Math.toRadians(player.getYRot());
        double lx = -Math.sin(yawRad);

        double sx = Math.cos(yawRad);
        double sz = -lx;

        if (attacker != null) {
            double ax = attacker.getX() - player.getX();
            double az = attacker.getZ() - player.getZ();
            double len = Math.sqrt(ax * ax + az * az);

            if (len > 0.0001) {
                ax /= len;
                az /= len;

                sx = -az;
                sz = ax;

                if (player.getRandom().nextBoolean()) {
                    sx = -sx;
                    sz = -sz;
                }
            }
        }

        player.push(sx * push, 0.02, sz * push);
        player.hurtMarked = true;
        player.resetFallDistance();
    }

    @EventBusSubscriber(modid = Cyberchems.MODID)
    public static final class StimHooks {

        private static final String NBT_STACK_GUARD_TICK = "cc_stim_stack_guard_tick";
        private static final int MAX_AMP = 255;

        private StimHooks() {}

        @SubscribeEvent
        public static void onIncomingDamage(LivingIncomingDamageEvent event) {
            LivingEntity target = event.getEntity();
            if (!(target instanceof Player player)) {
                return;
            }

            if (player.level().isClientSide) {
                return;
            }

            MobEffectInstance stim = player.getEffect(ModEffects.STIM);
            if (stim == null) {
                return;
            }

            long now = player.level().getGameTime();
            CompoundTag pd = player.getPersistentData();

            long last = pd.getLong(NBT_LAST_DODGE_TICK);
            if ((now - last) < DODGE_COOLDOWN_TICKS) {
                return;
            }

            float chance = dodgeChance(stim.getAmplifier());
            if (player.getRandom().nextFloat() >= chance) {
                return;
            }

            event.setCanceled(true);
            pd.putLong(NBT_LAST_DODGE_TICK, now);

            doDodge(player, stim.getAmplifier(), event.getSource().getEntity());
        }

        @SubscribeEvent
        public static void onEffectAdded(MobEffectEvent.Added event) {
            LivingEntity entity = event.getEntity();
            if (entity.level().isClientSide) {
                return;
            }

            MobEffectInstance added = event.getEffectInstance();
            if (added == null || !added.is(ModEffects.STIM)) {
                return;
            }

            MobEffectInstance old = event.getOldEffectInstance();
            if (old == null) {
                return;
            }

            long now = entity.level().getGameTime();
            CompoundTag pd = entity.getPersistentData();
            if (pd.getLong(NBT_STACK_GUARD_TICK) == now) {
                return;
            }

            int desiredAmp = Math.min(old.getAmplifier() + 1, MAX_AMP);
            if (added.getAmplifier() == desiredAmp) {
                return;
            }

            pd.putLong(NBT_STACK_GUARD_TICK, now);

            entity.addEffect(new MobEffectInstance(
                    added.getEffect(),
                    added.getDuration(),
                    desiredAmp,
                    added.isAmbient(),
                    added.isVisible(),
                    added.showIcon()
            ));
        }

        @SubscribeEvent
        public static void onEffectExpired(MobEffectEvent.Expired event) {
            MobEffectInstance inst = event.getEffectInstance();
            if (inst == null) {
                return;
            }

            if (!inst.is(ModEffects.STIM)) {
                return;
            }

            if (!(event.getEntity() instanceof Player player)) {
                return;
            }

            if (player.level().isClientSide) {
                return;
            }

            clearHumanityPenalty(player);
        }

        @SubscribeEvent
        public static void onEffectRemoved(MobEffectEvent.Remove event) {
            MobEffectInstance inst = event.getEffectInstance();
            if (inst == null) {
                return;
            }

            if (!inst.is(ModEffects.STIM)) {
                return;
            }

            if (!(event.getEntity() instanceof Player player)) {
                return;
            }

            if (player.level().isClientSide) {
                return;
            }

            clearHumanityPenalty(player);
        }
    }
}