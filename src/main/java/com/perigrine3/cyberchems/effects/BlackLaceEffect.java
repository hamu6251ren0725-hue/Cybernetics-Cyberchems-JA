package com.perigrine3.cyberchems.effects;

import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class BlackLaceEffect extends MobEffect {

    public static final String HUMANITY_KEY = "cc_drug_penalty_black_lace";

    private static final int HUMANITY_PENALTY_BASE = 15;
    private static final int HUMANITY_PENALTY_PER_EXTRA = 5;

    public BlackLaceEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity living, int amplifier) {
        if (living.level().isClientSide) {
            return true;
        }

        if (!(living instanceof Player player)) {
            return true;
        }

        applyHumanityPenalty(player, amplifier);
        return true;
    }

    @Override
    public void onMobRemoved(LivingEntity living, int amplifier, Entity.RemovalReason reason) {
        if (living.level().isClientSide) {
            return;
        }

        if (!(living instanceof Player player)) {
            return;
        }

        clearHumanityPenalty(player);
    }

    private static void applyHumanityPenalty(Player player, int amplifier) {
        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) {
            return;
        }

        int penalty = HUMANITY_PENALTY_BASE + (amplifier * HUMANITY_PENALTY_PER_EXTRA);
        data.setHumanityPenalty(player, HUMANITY_KEY, penalty);
    }

    public static void clearHumanityPenalty(Player player) {
        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) {
            return;
        }

        data.clearHumanityPenalty(player, HUMANITY_KEY);
    }
}