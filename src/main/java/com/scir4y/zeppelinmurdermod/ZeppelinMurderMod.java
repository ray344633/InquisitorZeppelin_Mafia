package com.scir4y.zeppelinmurdermod;

import com.scir4y.zeppelinmurdermod.registry.ModBlocks;
import com.scir4y.zeppelinmurdermod.registry.ModBlockEntities;
import com.scir4y.zeppelinmurdermod.registry.ModTasks;
import com.scir4y.zeppelinmurdermod.system.ModAttachments;
import com.scir4y.zeppelinmurdermod.registry.ModEntities;
import com.scir4y.zeppelinmurdermod.registry.ModDataComponents;
import com.scir4y.zeppelinmurdermod.registry.ModItems;
import com.scir4y.zeppelinmurdermod.registry.ModCreativeTabs;
import com.scir4y.zeppelinmurdermod.client.renderer.elevator.SelectionRenderer;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.scir4y.zeppelinmurdermod.config.Config;
import com.scir4y.zeppelinmurdermod.client.renderer.elevator.SelectionRenderer;


@Mod(ZeppelinMurderMod.MODID)
public class ZeppelinMurderMod {

    public static final String MODID = "zeppelinmurder";

    public static final Logger LOGGER = LogUtils.getLogger();

    public ZeppelinMurderMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        //Registering mod creative tabs
        ModCreativeTabs.register(modEventBus);

        //Registering mod data components (custom simplified alternative to WrittenBookContent, used by "Note")
        ModDataComponents.register(modEventBus);

        //Registering mod items & mod blocks
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModTasks.register();
        ModBlockEntities.register(modEventBus);
        ModEntities.register(modEventBus);


        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(SelectionRenderer.class);

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
}
