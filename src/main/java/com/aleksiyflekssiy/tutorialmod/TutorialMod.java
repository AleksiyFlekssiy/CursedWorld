package com.aleksiyflekssiy.tutorialmod;

import com.aleksiyflekssiy.tutorialmod.block.ModBlocks;
import com.aleksiyflekssiy.tutorialmod.capability.CursedEnergyCapability;
import com.aleksiyflekssiy.tutorialmod.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.tutorialmod.client.model.*;
import com.aleksiyflekssiy.tutorialmod.client.particle.BluePullParticle;
import com.aleksiyflekssiy.tutorialmod.client.particle.LaunchRingParticle;
import com.aleksiyflekssiy.tutorialmod.client.renderer.*;
import com.aleksiyflekssiy.tutorialmod.client.screen.JujutsuHUD;
import com.aleksiyflekssiy.tutorialmod.client.screen.KeyHandler;
import com.aleksiyflekssiy.tutorialmod.effect.ModEffects;
import com.aleksiyflekssiy.tutorialmod.entity.DivineDogEntity;
import com.aleksiyflekssiy.tutorialmod.entity.ModEntities;
import com.aleksiyflekssiy.tutorialmod.item.ModCreativeModeTabs;
import com.aleksiyflekssiy.tutorialmod.item.ModItems;
import com.aleksiyflekssiy.tutorialmod.loot.ModLootModifiers;
import com.aleksiyflekssiy.tutorialmod.network.CursedEnergySyncPacket;
import com.aleksiyflekssiy.tutorialmod.network.ModMessages;
import com.aleksiyflekssiy.tutorialmod.particle.ModParticles;
import com.aleksiyflekssiy.tutorialmod.sound.ModSoundEvents;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
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
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TutorialMod.MOD_ID)
public class TutorialMod
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "tutorialmod";
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public TutorialMod(FMLJavaModLoadingContext context)
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

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new JujutsuHUD());


        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
        NETWORK.registerMessage(0, CursedEnergySyncPacket.class, CursedEnergySyncPacket::encode, CursedEnergySyncPacket::decode, CursedEnergySyncPacket::handle);
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
            event.addCapability(new ResourceLocation(MOD_ID, "cursed_energy"), new CursedEnergyCapability.Provider());
            event.addCapability(new ResourceLocation(MOD_ID, "cursed_technique"), new CursedTechniqueCapability.Provider());
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        public static final ModelLayerLocation BLUE_LAYER = new ModelLayerLocation(new ResourceLocation(MOD_ID, "blue_entity"), "main");
        public static final ModelLayerLocation RED_LAYER = new ModelLayerLocation(new ResourceLocation(MOD_ID, "red_entity"), "main");
        public static final ModelLayerLocation HOLLOW_PURPLE_LAYER = new ModelLayerLocation(new ResourceLocation(MOD_ID, "hollow_purple_entity"), "main");
        public static final ModelLayerLocation DIVINE_DOG_LAYER = new ModelLayerLocation(new ResourceLocation(MOD_ID, "divine_dog"), "main");
        public static final ModelLayerLocation NUE_LAYER = new ModelLayerLocation(new ResourceLocation(MOD_ID, "nue"), "main");
        public static final ModelLayerLocation TOAD_LAYER = new ModelLayerLocation(new ResourceLocation(MOD_ID, "toad"), "main");

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            event.enqueueWork(() -> {
                MinecraftForge.EVENT_BUS.register(new KeyHandler());
            });

            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                    new ResourceLocation(MOD_ID, "animation"),
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
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(BLUE_LAYER, BlueModel::createBodyLayer);
            event.registerLayerDefinition(RED_LAYER, RedModel::createBodyLayer);
            event.registerLayerDefinition(HOLLOW_PURPLE_LAYER, HollowPurpleModel::createBodyLayer);
            event.registerLayerDefinition(DIVINE_DOG_LAYER, DivineDogModel::createBodyLayer);
            event.registerLayerDefinition(NUE_LAYER, NueModel::createBodyLayer);
            event.registerLayerDefinition(TOAD_LAYER, ToadModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.LAUNCH_RING.get(), LaunchRingParticle.Provider::new);
            event.registerSpriteSet(ModParticles.BLUE_PULL.get(), BluePullParticle.Provider::new);
        }
    }
}
