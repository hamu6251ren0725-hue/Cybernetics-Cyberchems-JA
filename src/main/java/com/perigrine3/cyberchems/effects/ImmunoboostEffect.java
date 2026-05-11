package com.perigrine3.cyberchems.effects;

import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;

public class ImmunoboostEffect extends MobEffect {

    public static final String HUMANITY_KEY = "cc_drug_penalty_immunoboost";
    private static final int HUMANITY_PENALTY = 25;

    private static final int ROLL_INTERVAL_TICKS = 20;
    private static final float DAMAGE_AMOUNT = 3.0f;

    public ImmunoboostEffect(MobEffectCategory category, int color) {
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

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) {
            return true;
        }

        data.setHumanityPenalty(player, HUMANITY_KEY, HUMANITY_PENALTY);

        if ((player.tickCount % ROLL_INTERVAL_TICKS) != 0) {
            return true;
        }

        int implants = countCybernetics(data);
        cleanseHarmfulEffects(player, implants);
        rollImplantBacklashDamage(player, implants);

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
    }

    private static int countCybernetics(PlayerCyberwareData data) {
        int count = 0;

        for (var entry : data.getAll().entrySet()) {
            var arr = entry.getValue();
            if (arr == null) {
                continue;
            }

            for (var installed : arr) {
                if (installed == null) {
                    continue;
                }

                var st = installed.getItem();
                if (st == null || st.isEmpty()) {
                    continue;
                }

                if (st.is(ModTags.Items.CYBERWARE_ITEM)) {
                    count++;
                }
            }
        }

        return Math.max(0, count);
    }

    private static void cleanseHarmfulEffects(Player player, int implants) {
        float chance = 0.90f - 0.05f * implants;
        if (chance < 0.10f) {
            chance = 0.10f;
        }
        if (chance > 0.90f) {
            chance = 0.90f;
        }

        Collection<MobEffectInstance> effects = player.getActiveEffects();
        if (effects.isEmpty()) {
            return;
        }

        for (MobEffectInstance inst : effects.toArray(new MobEffectInstance[0])) {
            if (inst == null) {
                continue;
            }

            if (inst.is(ModEffects.IMMUNOBOOST)) {
                continue;
            }

            if (inst.getEffect().value().getCategory() != MobEffectCategory.HARMFUL) {
                continue;
            }

            if (player.getRandom().nextFloat() < chance) {
                player.removeEffect(inst.getEffect());
            }
        }
    }

    private static void rollImplantBacklashDamage(Player player, int implants) {
        if (implants <= 2) {
            return;
        }

        float chance = (implants - 2) * 0.02f;
        if (chance > 1.0f) {
            chance = 1.0f;
        }

        if (player.getRandom().nextFloat() < chance) {
            player.hurt(player.damageSources().magic(), DAMAGE_AMOUNT);
        }
    }

    public static void clearHumanityPenalty(Player player) {
        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) {
            return;
        }

        data.clearHumanityPenalty(player, HUMANITY_KEY);
    }
}