package com.aleksiyflekssiy.cursedworld.command;

import com.aleksiyflekssiy.cursedworld.CursedWorld;
import com.aleksiyflekssiy.cursedworld.capability.CursedEnergyCapability;
import com.aleksiyflekssiy.cursedworld.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.cursedworld.cursed_technique.TenShadowsTechnique;
import com.aleksiyflekssiy.cursedworld.cursed_technique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.cursedworld.cursed_technique.skill.Skill;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@Mod.EventBusSubscriber(modid = CursedWorld.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCommands {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(literal("settechnique")
                .then(argument("player", EntityArgument.player())
                .then(argument("technique", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            builder.suggest("ten_shadows");
                            builder.suggest("limitless");
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                            String technique = StringArgumentType.getString(context, "technique");
                            CursedTechniqueCapability.setCursedTechnique(player, technique);
                            context.getSource().sendSuccess(() -> Component.literal("Successfully set the technique! " + CursedTechniqueCapability.getCursedTechnique(player).getName()), true);
                            return 1;
                        }))));
        dispatcher.register(literal("gettechnique")
                .then(argument("player", EntityArgument.player())
                        .executes(context -> {
                            Player player = EntityArgument.getPlayer(context, "player");
                            context.getSource().sendSystemMessage(Component.literal(CursedTechniqueCapability.getCursedTechnique(player).getName()));
                            return 1;
                        })));
        dispatcher.register(literal("tameshikigami")
                .executes(context -> {
                    Player owner = context.getSource().getPlayerOrException();
                    if (CursedTechniqueCapability.getCursedTechnique(owner) instanceof TenShadowsTechnique technique){
                        List<Skill> skills = technique.getSkillSet().stream().filter(ModCommands::isShikigamiSkill).toList();
                        skills.forEach(skill -> {
                            ShikigamiSkill shikigamiSkill = (ShikigamiSkill) skill;
                            if (!shikigamiSkill.isDead() && !shikigamiSkill.isTamed()){
                                shikigamiSkill.setTamed(true);
                                if (shikigamiSkill.isActive()) shikigamiSkill.getShikigami().forEach(shikigami -> shikigami.tame(owner));
                            }
                        });
                    }
                    context.getSource().sendSuccess(() -> Component.literal("Your shikigami have been tamed"), true);
                    return 1;
                }));
        dispatcher.register(literal("regainshikigami")
                .executes(context -> {
                    Player owner = context.getSource().getPlayerOrException();
                    if (CursedTechniqueCapability.getCursedTechnique(owner) instanceof TenShadowsTechnique technique){
                        List<Skill> skills = technique.getSkillSet().stream().filter(ModCommands::isShikigamiSkill).toList();
                        skills.forEach(skill -> {
                            ShikigamiSkill shikigamiSkill = (ShikigamiSkill) skill;
                            if (shikigamiSkill.isDead()) {
                                shikigamiSkill.setDead(false);
                                shikigamiSkill.setTamed(false);
                            }
                        });
                    }
                    context.getSource().sendSuccess(() -> Component.literal("Your shikigami have been regained"), true);
                    return 1;
                }));
        dispatcher.register(Commands.literal("cursedworld")
                .then(Commands.argument("var", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            builder.suggest("ce");
                            builder.suggest("maxce");
                            builder.suggest("regenspeed");
                            builder.suggest("regenamount");
                            return builder.buildFuture();
                        })
                        .then(Commands.literal("get")
                                .executes(ctx -> {
                                    Player player = ctx.getSource().getPlayerOrException();
                                    String var = StringArgumentType.getString(ctx, "var");

                                    switch (var) {
                                        case "ce" -> player.sendSystemMessage(
                                                Component.literal("CE: " + CursedEnergyCapability.getCursedEnergy(player)));
                                        case "maxce" -> player.sendSystemMessage(
                                                Component.literal("Max CE: " + CursedEnergyCapability.getMaxCursedEnergy(player)));
                                        case "regenspeed" -> player.sendSystemMessage(
                                                Component.literal("Regen Speed: " + CursedEnergyCapability.getRegenerationSpeed(player)));
                                        case "regenamount" -> player.sendSystemMessage(
                                                Component.literal("Regen Amount: " + CursedEnergyCapability.getRegenerationAmount(player)));
                                    }
                                    return 1;
                                })
                        )
                        .then(Commands.literal("set")
                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                        .executes(ctx -> {
                                            Player player = ctx.getSource().getPlayerOrException();
                                            String var = StringArgumentType.getString(ctx, "var");
                                            int value = IntegerArgumentType.getInteger(ctx, "value");

                                            switch (var) {
                                                case "ce" -> CursedEnergyCapability.setCursedEnergy(player, value);
                                                case "maxce" -> CursedEnergyCapability.setMaxCursedEnergy(player, value);
                                                case "regenspeed" -> CursedEnergyCapability.setRegenerationSpeed(player, value);
                                                case "regenamount" -> CursedEnergyCapability.setRegenerationAmount(player, value);
                                            }

                                            player.sendSystemMessage(Component.literal("§aУстановлено " + var + " = " + value));
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }

    private static boolean isShikigamiSkill(Skill skill) {
        return skill instanceof ShikigamiSkill;
    }
}
