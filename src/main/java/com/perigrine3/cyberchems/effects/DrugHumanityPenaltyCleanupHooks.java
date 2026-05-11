package com.perigrine3.cyberchems.effects;

import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.cyberchems.Cyberchems;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Cyberchems.MODID)
public final class DrugHumanityPenaltyCleanupHooks {

    private static final String ROID_KEY = "cc_drug_penalty_roid";
    private static final String STIM_KEY = "cc_drug_penalty_stim";
    private static final String BLACKLACE_KEY = "cc_drug_penalty_black_lace";
    private static final String IMMUNOBOOST_KEY = "cc_drug_penalty_immunoboost";
    private static final String ADDICTION_KEY = "cc_drug_penalty_addiction";

    private DrugHumanityPenaltyCleanupHooks() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) {
            return;
        }

        if (!player.hasEffect(ModEffects.ROID)) {
            data.clearHumanityPenalty(player, ROID_KEY);
        }

        if (!player.hasEffect(ModEffects.STIM)) {
            data.clearHumanityPenalty(player, STIM_KEY);
        }

        if (!player.hasEffect(ModEffects.BLACKLACE)) {
            data.clearHumanityPenalty(player, BLACKLACE_KEY);
        }

        if (!player.hasEffect(ModEffects.IMMUNOBOOST)) {
            data.clearHumanityPenalty(player, IMMUNOBOOST_KEY);
        }

        if (!player.hasEffect(ModEffects.ADDICTION)) {
            data.clearHumanityPenalty(player, ADDICTION_KEY);
        }
    }
}