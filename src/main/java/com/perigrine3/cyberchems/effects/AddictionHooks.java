package com.perigrine3.cyberchems.effects;

import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.cyberchems.Cyberchems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Cyberchems.MODID)
public final class AddictionHooks {

    private static final String HUMANITY_KEY = "cc_drug_penalty_addiction";
    private static final int HUMANITY_PENALTY = 10;

    private static final String NBT_ADDICTED = "cc_addicted";

    private AddictionHooks() {}

    private static boolean isDrugActive(LivingEntity livingEntity) {
        return livingEntity.hasEffect(ModEffects.ROID)
                || livingEntity.hasEffect(ModEffects.STIM)
                || livingEntity.hasEffect(ModEffects.BLACKLACE);
    }

    private static void setAddictionPenalty(Player player) {
        PlayerCyberwareData cyberwareData = player.getData(ModAttachments.CYBERWARE);
        if (cyberwareData == null) {
            return;
        }

        cyberwareData.setHumanityPenalty(player, HUMANITY_KEY, HUMANITY_PENALTY);
    }

    private static void clearAddictionPenalty(Player player) {
        PlayerCyberwareData cyberwareData = player.getData(ModAttachments.CYBERWARE);
        if (cyberwareData == null) {
            return;
        }

        cyberwareData.clearHumanityPenalty(player, HUMANITY_KEY);
    }

    private static void ensureAddiction(LivingEntity livingEntity) {
        if (livingEntity.level().isClientSide) {
            return;
        }

        CompoundTag persistentData = livingEntity.getPersistentData();
        if (!persistentData.getBoolean(NBT_ADDICTED)) {
            return;
        }

        if (isDrugActive(livingEntity)) {
            if (livingEntity.hasEffect(ModEffects.ADDICTION)) {
                livingEntity.removeEffect(ModEffects.ADDICTION);
            }

            if (livingEntity instanceof Player player) {
                clearAddictionPenalty(player);
            }

            return;
        }

        MobEffectInstance currentAddiction = livingEntity.getEffect(ModEffects.ADDICTION);
        if (currentAddiction == null || currentAddiction.getDuration() <= 20) {
            livingEntity.addEffect(new MobEffectInstance(
                    ModEffects.ADDICTION,
                    AddictionEffect.DURATION_7_DAYS_TICKS,
                    0,
                    false,
                    true,
                    true
            ));
        }

        if (livingEntity instanceof Player player) {
            setAddictionPenalty(player);
        }
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity.level().isClientSide) {
            return;
        }

        MobEffectInstance effectInstance = event.getEffectInstance();
        if (effectInstance == null) {
            return;
        }

        if (effectInstance.is(ModEffects.ADDICTION)) {
            livingEntity.getPersistentData().putBoolean(NBT_ADDICTED, true);

            if (livingEntity instanceof Player player) {
                setAddictionPenalty(player);
            }

            return;
        }

        if (effectInstance.is(ModEffects.ROID)
                || effectInstance.is(ModEffects.STIM)
                || effectInstance.is(ModEffects.BLACKLACE)) {
            if (livingEntity.getPersistentData().getBoolean(NBT_ADDICTED)) {
                livingEntity.removeEffect(ModEffects.ADDICTION);

                if (livingEntity instanceof Player player) {
                    clearAddictionPenalty(player);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity.level().isClientSide) {
            return;
        }

        MobEffectInstance effectInstance = event.getEffectInstance();
        if (effectInstance == null) {
            return;
        }

        if (effectInstance.is(ModEffects.ADDICTION) && livingEntity instanceof Player player) {
            clearAddictionPenalty(player);
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity.level().isClientSide) {
            return;
        }

        MobEffectInstance effectInstance = event.getEffectInstance();
        if (effectInstance == null) {
            return;
        }

        if (effectInstance.is(ModEffects.ADDICTION) && livingEntity instanceof Player player) {
            clearAddictionPenalty(player);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) {
            return;
        }

        if (livingEntity.level().isClientSide) {
            return;
        }

        ensureAddiction(livingEntity);
    }
}