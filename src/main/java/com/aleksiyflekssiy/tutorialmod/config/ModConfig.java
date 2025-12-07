package com.aleksiyflekssiy.tutorialmod.config;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfig {
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    public static final ForgeConfigSpec SERVER_SPEC;
    public static final Server SERVER;

    static {
        var clientPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = clientPair.getRight();
        CLIENT = clientPair.getLeft();

        // COMMON
        var commonPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = commonPair.getRight();
        COMMON = commonPair.getLeft();

        // SERVER
        var serverPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER_SPEC = serverPair.getRight();
        SERVER = serverPair.getLeft();
    }

    public static void register(FMLJavaModLoadingContext context){
        context.registerConfig(net.minecraftforge.fml.config.ModConfig.Type.CLIENT, CLIENT_SPEC);
        context.registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, COMMON_SPEC);
        context.registerConfig(net.minecraftforge.fml.config.ModConfig.Type.SERVER, SERVER_SPEC);
    }

    // ← Регистрация
    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == CLIENT_SPEC) CLIENT.bake();
        if (event.getConfig().getSpec() == COMMON_SPEC) COMMON.bake();
        if (event.getConfig().getSpec() == SERVER_SPEC) SERVER.bake();
    }

    public static class Client {

        Client(ForgeConfigSpec.Builder builder) {
            builder.comment("Клиентские настройки").push("client");

            builder.pop();
        }

        void bake() {}
    }

    public static class Common {
        public final ForgeConfigSpec.IntValue DOMAIN_DURATION;
        public final ForgeConfigSpec.DoubleValue DOMAIN_RADIUS;

        Common(ForgeConfigSpec.Builder builder) {
            builder.comment("Common setting").push("domain");
            DOMAIN_DURATION = builder.defineInRange("domain_duration", 300, 0, Integer.MAX_VALUE);
            DOMAIN_RADIUS = builder.defineInRange("domain_radius", 15.0, 0, 200);
            builder.pop();
        }

        void bake() {}
    }

    public static class Server {
        public final ForgeConfigSpec.BooleanValue FAST_CHANT;
        public final ForgeConfigSpec.BooleanValue NO_CE_COST;

        Server(ForgeConfigSpec.Builder builder) {
            builder.comment("Server settings").push("server");
            FAST_CHANT = builder.comment("Makes you perform one chant at one tick").define("fast_chant", false);
            NO_CE_COST = builder.comment("You don't spend CE for jujutsu").define("no_ce_cost", false);

            builder.pop();
        }

        void bake() {}
    }
}
