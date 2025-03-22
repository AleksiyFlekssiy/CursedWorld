package com.aleksiyflekssiy.tutorialmod.command;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.entity.RedEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCommands {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                literal("redentitymode")
                        .then(argument("mode", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    builder.suggest("spiral");
                                    builder.suggest("single");
                                    builder.suggest("hybrid");
                                    return builder.buildFuture();
                                })
                                .then(argument("spirals", IntegerArgumentType.integer(1, 10))
                                        .then(argument("maxRadius", DoubleArgumentType.doubleArg(0.1, 10.0))
                                                .then(argument("minRadius", DoubleArgumentType.doubleArg(0.01, 5.0))
                                                        .then(argument("numTurns", DoubleArgumentType.doubleArg(0.5, 10.0))
                                                                .then(argument("particlesPerSpiral", IntegerArgumentType.integer(1, 50))
                                                                        .then(argument("animationSpeed", FloatArgumentType.floatArg(0.01f, 1.0f))
                                                                                .then(argument("particleStep", DoubleArgumentType.doubleArg(0.01, 1.0))
                                                                                        .then(argument("particleSize", FloatArgumentType.floatArg(0.01f, 3.0f))
                                                                                        .executes(context -> {
                                                                                            String modeStr = StringArgumentType.getString(context, "mode");
                                                                                            int spirals = IntegerArgumentType.getInteger(context, "spirals");
                                                                                            double maxRadius = DoubleArgumentType.getDouble(context, "maxRadius");
                                                                                            double minRadius = DoubleArgumentType.getDouble(context, "minRadius");
                                                                                            double numTurns = DoubleArgumentType.getDouble(context, "numTurns");
                                                                                            int particlesPerSpiral = IntegerArgumentType.getInteger(context, "particlesPerSpiral");
                                                                                            float animationSpeed = FloatArgumentType.getFloat(context, "animationSpeed");
                                                                                            double particleStep = DoubleArgumentType.getDouble(context, "particleStep");
                                                                                            float particleSize = FloatArgumentType.getFloat(context, "particleSize");

                                                                                            RedEntity.SpiralAnimation.SpiralMode newMode;
                                                                                            switch (modeStr.toLowerCase()) {
                                                                                                case "spiral":
                                                                                                    newMode = RedEntity.SpiralAnimation.SpiralMode.SPIRAL;
                                                                                                    break;
                                                                                                case "single":
                                                                                                    newMode = RedEntity.SpiralAnimation.SpiralMode.SINGLE;
                                                                                                    break;
                                                                                                case "hybrid":
                                                                                                    newMode = RedEntity.SpiralAnimation.SpiralMode.HYBRID;
                                                                                                    break;
                                                                                                default:
                                                                                                    context.getSource().sendFailure(Component.literal("Invalid mode! Use: full, single, combined"));
                                                                                                    return 0;
                                                                                            }

                                                                                            // Устанавливаем значения по умолчанию
                                                                                            RedEntity.SpiralAnimation.setDefaultMode(newMode);
                                                                                            RedEntity.SpiralAnimation.setDefaultSpirals(spirals);
                                                                                            RedEntity.SpiralAnimation.setDefaultMaxRadius(maxRadius);
                                                                                            RedEntity.SpiralAnimation.setDefaultMinRadius(minRadius);
                                                                                            RedEntity.SpiralAnimation.setDefaultNumTurns(numTurns);
                                                                                            RedEntity.SpiralAnimation.setDefaultParticlesPerSpiral(particlesPerSpiral);
                                                                                            RedEntity.SpiralAnimation.setDefaultAnimationSpeed(animationSpeed);
                                                                                            RedEntity.SpiralAnimation.setDefaultParticleStep(particleStep);
                                                                                            RedEntity.SpiralAnimation.setDefaultParticleSize(particleSize);
                                                                                            return 1;
                                                                                        }))))))))
                        )));
    }
}
