package com.aleksiyflekssiy.tutorialmod.event;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.TenShadowsTechnique;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.Skill;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.domainexpansion.DomainExpansionSkill;
import com.aleksiyflekssiy.tutorialmod.entity.Shikigami;
import com.aleksiyflekssiy.tutorialmod.item.custom.WheelOfHarmonyItem;
import com.aleksiyflekssiy.tutorialmod.network.ModMessages;
import com.aleksiyflekssiy.tutorialmod.network.TechniqueSyncPacket;
import com.aleksiyflekssiy.tutorialmod.util.AdaptationUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        player.getCapability(CursedTechniqueCapability.CURSED_TECHNIQUE).ifPresent(technique -> {
            CompoundTag nbt = technique.serializeNbtToNetwork();
            System.out.println("Server NBT: " + nbt);
            ModMessages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new TechniqueSyncPacket(nbt));
            if (technique.getTechnique() instanceof TenShadowsTechnique tenShadowsTechnique) {
                for (Skill skill : tenShadowsTechnique.getSkillSet()){
                    if (skill instanceof ShikigamiSkill shikigamiSkill) {
                        List<UUID> shikigamiUUIDList = shikigamiSkill.getShikigamiUUID();
                        if (shikigamiUUIDList.isEmpty()) continue;
                        List<Shikigami> shikigamiList = shikigamiUUIDList
                                .stream()
                                .map(shikigamiUUID -> ShikigamiSkill.getShikigamiFromUUID(shikigamiUUID, (ServerLevel) player.level()))
                                .toList();
                        List<Shikigami> mutableShikigamiList = new ArrayList<>(shikigamiList);
                        shikigamiList.forEach(shikigami -> {
                            if (shikigami != null){
                                if (shikigamiSkill.isTamed()) {
                                    shikigami.tame(player);
                                    System.out.println(skill.getName() + " has tamed");
                                }
                                else {
                                    shikigami.setOwner(player);
                                    System.out.println(skill.getName() + " has gained the owner");
                                }
                            }
                            else System.out.println(skill.getName() + " is null");
                        });
                        shikigamiSkill.setShikigami(mutableShikigamiList);
                    }
                    else System.out.println(skill.getName() + " isn't shikigami");
                }
            } else System.out.println("Wrong technique");
        });
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        player.getCapability(CursedTechniqueCapability.CURSED_TECHNIQUE).ifPresent(technique -> {
            CompoundTag nbt = technique.serializeNbtToNetwork();
            System.out.println("Server NBT: " + nbt);
            ModMessages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new TechniqueSyncPacket(nbt));
        });
    }

    @SubscribeEvent
    public static void onPlayerHitSkill(SkillEvent.Hit event) {
        if (event.getTarget() instanceof Player player) {
            if (!AdaptationUtil.checkAdaptation(event.getSkill(), player)) {
                AdaptationUtil.addOrSpeedUpAdaptationToSkill(event.getSkill(), player);
            }
        }
    }

    @SubscribeEvent
    public static void playerAboutToTakeDamage(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide) {
            if (!AdaptationUtil.checkAdaptation(event.getSource().type(), player)) {
                AdaptationUtil.addOrSpeedUpAdaptationToDamage(event.getSource().type(), event.getAmount(), player);
            }
            else {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void beforeEffectApplication(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide) {
            if (!event.getEffectInstance().getEffect().isBeneficial()){
                if (!AdaptationUtil.checkAdaptation(event.getEffectInstance().getEffect(), player)) {
                    AdaptationUtil.addOrSpeedUpAdaptationToEffect(event.getEffectInstance(), player);
                }
                else event.setResult(Event.Result.DENY);
            }
        }
    }



    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        CursedTechniqueCapability.setCursedTechnique(event.getEntity(), CursedTechniqueCapability.getCursedTechnique(oldPlayer).getName());
    }
}
