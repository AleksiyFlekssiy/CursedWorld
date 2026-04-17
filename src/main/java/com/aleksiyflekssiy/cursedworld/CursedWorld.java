package com.aleksiyflekssiy.cursedworld;

import com.aleksiyflekssiy.cursedworld.block.ModBlocks;
import com.aleksiyflekssiy.cursedworld.capability.CursedEnergyCapability;
import com.aleksiyflekssiy.cursedworld.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.cursedworld.client.model.*;
import com.aleksiyflekssiy.cursedworld.client.particle.BluePullParticle;
import com.aleksiyflekssiy.cursedworld.client.particle.LaunchRingParticle;
import com.aleksiyflekssiy.cursedworld.client.renderer.*;
import com.aleksiyflekssiy.cursedworld.client.screen.JujutsuHUD;
import com.aleksiyflekssiy.cursedworld.client.screen.KeyHandler;
import com.aleksiyflekssiy.cursedworld.config.ModConfig;
import com.aleksiyflekssiy.cursedworld.effect.ModEffects;
import com.aleksiyflekssiy.cursedworld.entity.ModEntities;
import com.aleksiyflekssiy.cursedworld.entity.behavior.CustomMemoryModuleTypes;
import com.aleksiyflekssiy.cursedworld.entity.behavior.CustomSensorTypes;
import com.aleksiyflekssiy.cursedworld.item.ModCreativeModeTabs;
import com.aleksiyflekssiy.cursedworld.item.ModItems;
import com.aleksiyflekssiy.cursedworld.loot.ModLootModifiers;
import com.aleksiyflekssiy.cursedworld.network.ModMessages;
import com.aleksiyflekssiy.cursedworld.particle.ModParticles;
import com.aleksiyflekssiy.cursedworld.registry.Skills;
import com.aleksiyflekssiy.cursedworld.sound.ModSoundEvents;
import com.mojang.logging.LogUtils;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CursedWorld.MOD_ID)
public class CursedWorld
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "cursedworld";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CursedWorld(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();
        ModCreativeModeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        ModEntities.register(modEventBus);
        ModSoundEvents.register(modEventBus);
        ModParticles.register(modEventBus);
        ModEffects.register(modEventBus);
        CustomMemoryModuleTypes.register(modEventBus);
        CustomSensorTypes.register(modEventBus);
        Skills.register(modEventBus);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);

        ModConfig.register(context);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new JujutsuHUD());

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
        ModMessages.register();
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {}

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS){
            event.accept(ModItems.RUBY);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {}

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(CursedTechniqueCapability.ICursedTechnique.class);
    }



    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent<Entity> event){
        if (event.getObject() instanceof Player){
            event.addCapability(ResourceLocation.fromNamespaceAndPath(MOD_ID, "cursed_energy"), new CursedEnergyCapability.Provider());
            event.addCapability(ResourceLocation.fromNamespaceAndPath(MOD_ID, "cursed_technique"), new CursedTechniqueCapability.Provider());
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {


        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            event.enqueueWork(() -> MinecraftForge.EVENT_BUS.register(new KeyHandler()));

            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "animation"),
                    42,
                    ClientModEvents::registerPlayerAnimation
            );
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(KeyHandler.NEXT_SKILL);
            event.register(KeyHandler.PREVIOUS_SKILL);
            event.register(KeyHandler.PRIMARY_SKILL_ACTIVATION);
            event.register(KeyHandler.SECONDARY_SKILL_ACTIVATION);
        }

        private static IAnimation registerPlayerAnimation(AbstractClientPlayer player) {
            //This will be invoked for every new player
            return new ModifierLayer<>();
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.BLUE_ENTITY.get(), BlueRenderer::new);
            event.registerEntityRenderer(ModEntities.RED_ENTITY.get(), RedRenderer::new);
            event.registerEntityRenderer(ModEntities.HOLLOW_PURPLE_ENTITY.get(), HollowPurpleRenderer::new);
            event.registerEntityRenderer(ModEntities.ANIMATION_BLUE_ENTITY.get(), AnimationBlueRenderer::new);
            event.registerEntityRenderer(ModEntities.ANIMATION_RED_ENTITY.get(), AnimationRedRenderer::new);
            event.registerEntityRenderer(ModEntities.DIVINE_DOG.get(), DivineDogRenderer::new);
            event.registerEntityRenderer(ModEntities.NUE.get(), NueRenderer::new);
            event.registerEntityRenderer(ModEntities.TOAD.get(), ToadRenderer::new);
            event.registerEntityRenderer(ModEntities.GREAT_SERPENT.get(), GreatSerpentRenderer::new);
            event.registerEntityRenderer(ModEntities.GREAT_SERPENT_SEGMENT.get(), GreatSerpentSegmentRenderer::new);
            event.registerEntityRenderer(ModEntities.RABBIT_ESCAPE.get(), RabbitEscapeRenderer::new);
            event.registerEntityRenderer(ModEntities.MAX_ELEPHANT.get(), MaxElephantRenderer::new);
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(ModModelLayers.BLUE_LAYER, BlueModel::createBodyLayer);
            event.registerLayerDefinition(ModModelLayers.RED_LAYER, RedModel::createBodyLayer);
            event.registerLayerDefinition(ModModelLayers.HOLLOW_PURPLE_LAYER, HollowPurpleModel::createBodyLayer);
            event.registerLayerDefinition(ModModelLayers.DIVINE_DOG_LAYER, DivineDogModel::createBodyLayer);
            event.registerLayerDefinition(ModModelLayers.NUE_LAYER, NueModel::createBodyLayer);
            event.registerLayerDefinition(ModModelLayers.TOAD_LAYER, ToadModel::createBodyLayer);
            event.registerLayerDefinition(ModModelLayers.GREAT_SERPENT_HEAD_LAYER, GreatSerpentHeadModel::createBodyLayer);
            event.registerLayerDefinition(ModModelLayers.GREAT_SERPENT_SEGMENT_LAYER, GreatSerpentSegmentModel::createBodyLayer);
            event.registerLayerDefinition(ModModelLayers.RABBIT_ESCAPE_LAYER, RabbitEscapeEntityModel::createBodyLayer);
            event.registerLayerDefinition(ModModelLayers.MAX_ELEPHANT_LAYER, MaxElephantModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.LAUNCH_RING.get(), LaunchRingParticle.Provider::new);
            event.registerSpriteSet(ModParticles.BLUE_PULL.get(), BluePullParticle.Provider::new);
        }
    }
}
