package com.perigrine3.cyberchems;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.cyberchems.effects.ModEffects;
import com.perigrine3.cyberchems.item.ModItems;
import com.perigrine3.cyberchems.potions.ModPotions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(Cyberchems.MODID)
public class Cyberchems {
    public static final String MODID = "cyberchems";

    private static final ResourceKey<CreativeModeTab> CREATE_CYBERNETICS_TAB_KEY =
            ResourceKey.create(
                    Registries.CREATIVE_MODE_TAB,
                    ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "create_cybernetics_tab")
            );

    public Cyberchems(IEventBus modEventBus, ModContainer modContainer) {
        ModEffects.register(modEventBus);
        ModPotions.register(modEventBus);
        ModItems.register(modEventBus);

        //modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(CREATE_CYBERNETICS_TAB_KEY)) {
            return;
        }

        event.insertAfter(
                com.perigrine3.createcybernetics.item.ModItems.NEUROPOZYNE_AUTOINJECTOR.get().getDefaultInstance(),
                ModItems.ROID_AUTOINJECTOR.get().getDefaultInstance(),
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
        );
        event.insertAfter(
                ModItems.ROID_AUTOINJECTOR.get().getDefaultInstance(),
                ModItems.STIM_AUTOINJECTOR.get().getDefaultInstance(),
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
        );
        event.insertAfter(
                ModItems.STIM_AUTOINJECTOR.get().getDefaultInstance(),
                ModItems.BLACKLACE_AUTOINJECTOR.get().getDefaultInstance(),
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
        );
        event.insertAfter(
                ModItems.BLACKLACE_AUTOINJECTOR.get().getDefaultInstance(),
                ModItems.IMMUNOBOOST_AUTOINJECTOR.get().getDefaultInstance(),
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
        );
        event.insertAfter(
                ModItems.IMMUNOBOOST_AUTOINJECTOR.get().getDefaultInstance(),
                ModItems.WARP_AUTOINJECTOR.get().getDefaultInstance(),
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
        );
        event.insertAfter(
                ModItems.WARP_AUTOINJECTOR.get().getDefaultInstance(),
                ModItems.ADDICTOL_AUTOINJECTOR.get().getDefaultInstance(),
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
        );
    }
}