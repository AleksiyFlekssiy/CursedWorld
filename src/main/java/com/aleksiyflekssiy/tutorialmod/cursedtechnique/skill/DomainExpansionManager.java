package com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.DomainExpansionSkill.CLASHING_BARRIER_BLOCK;
import static com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.DomainExpansionSkill.DEFAULT_BARRIER_BLOCK;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DomainExpansionManager {
    public static final List<DomainExpansionSkill> domains = new ArrayList<>();

    //Первый в паре - старший домен, второй - младший (вторженец)
    public static final List<Pair<DomainExpansionSkill, DomainExpansionSkill>> domainClashes = new ArrayList<>();

    public static void addDomainExpansion(DomainExpansionSkill domainExpansionSkill) {
        domains.add(domainExpansionSkill);
    }

    public static boolean willBeDomainClash(DomainExpansionSkill newDomain) {
        Vec3 center = newDomain.domainCenter;
        double radius = newDomain.domainRadius;

        AABB potentialAABB = new AABB(
                center.x - radius - 2, center.y - radius - 2, center.z - radius - 2,
                center.x + radius + 2, center.y + radius + 2, center.z + radius + 2
        );
        for (DomainExpansionSkill oldDomain : domains) {
            if (oldDomain.equals(newDomain)) continue;
            if (oldDomain.getDomainArea().intersects(potentialAABB)) {
                domainClashes.add(new Pair<>(oldDomain, newDomain));
                System.out.println(oldDomain.domainOwner.getScoreboardName() + " will clash with " + newDomain.domainOwner.getScoreboardName());
                return true;
            }
        }
        return false;
    }

    public static boolean isClashing(DomainExpansionSkill domain){
        if (domain == null) return false;
        for (Pair<DomainExpansionSkill, DomainExpansionSkill> domainClash : domainClashes) {
            if (domainClash.getFirst().equals(domainClash.getSecond())) throw new IllegalStateException("Both domains have the same owner");

            if (domain.equals(domainClash.getFirst()) && domainClash.getSecond() != null) {
                //System.out.println("Invaded: " + domain.domainOwner.getScoreboardName() + "; Invader: " + domainClash.getSecond().domainOwner.getScoreboardName());
                return true;
            }
            else if (domain.equals(domainClash.getSecond()) && domainClash.getFirst() != null) {
                //System.out.println("Invaded: " + domainClash.getFirst().domainOwner.getScoreboardName() + "; Invader: " + domain.domainOwner.getScoreboardName());
                return true;
            }
        }
        return false;
    }

    public static Pair<DomainExpansionSkill, DomainExpansionSkill> getClashingPair(DomainExpansionSkill domain){
        for (Pair<DomainExpansionSkill, DomainExpansionSkill> domainClash : domainClashes) {
            if (domain.equals(domainClash.getFirst())) {
                return domainClash;
            }
            else if (domain.equals(domainClash.getSecond())) {
                return domainClash;
            }
        }
        return null;
    }

    public static void transferDomainBarrier(Pair<DomainExpansionSkill, DomainExpansionSkill> clashingPair, DomainExpansionSkill lostDomain){
        DomainExpansionSkill domainWinner;
        //First domain in the pair is the first opened.
        if (lostDomain.equals(clashingPair.getFirst())){
            domainWinner = clashingPair.getSecond();
            domainWinner.originalBlocks.putAll(lostDomain.originalBlocks);
            domainWinner.barrierBlocks.addAll(lostDomain.barrierBlocks);
            for (BlockPos barrierBlock : domainWinner.barrierBlocks) {
                lostDomain.domainOwner.level().setBlock(barrierBlock, CLASHING_BARRIER_BLOCK, 3);
            }
            domainWinner.domainArea = lostDomain.domainArea;
            lostDomain.barrierBlocks.clear();
            lostDomain.originalBlocks.clear();
            lostDomain.domainArea = null;
            System.out.println("Restoration in Manager");
            System.out.println("Invader " + domainWinner.domainOwner.getScoreboardName() + " has won");
        }
        //Second domain in the pair is the invading one.
        else if (lostDomain.equals(clashingPair.getSecond())){
            domainWinner = clashingPair.getFirst();
            domainWinner.originalBlocks.putAll(lostDomain.originalBlocks);
            domainWinner.barrierBlocks.addAll(lostDomain.barrierBlocks);
            for (BlockPos barrierBlock : domainWinner.barrierBlocks) {
                lostDomain.domainOwner.level().setBlock(barrierBlock, DEFAULT_BARRIER_BLOCK, 3);
            }
            lostDomain.barrierBlocks.clear();
            lostDomain.originalBlocks.clear();
            lostDomain.domainArea = null;
            System.out.println("Restoration in Manager");
            System.out.println("Invaded " + domainWinner.domainOwner.getScoreboardName() + " has won");
        }
    }

    @SubscribeEvent
    public static void tickDomains(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        //System.out.println("Domains: " + domains.size() + "  Domain Clashes: " + domainClashes.size());
        domains.removeIf(domain -> {
            domain.tick();
            if (!domain.isActive() || domain.isExpired() || domain.checkBarrierDamage()){
                if (isClashing(domain)){
                    Pair<DomainExpansionSkill, DomainExpansionSkill> domains = getClashingPair(domain);
                    transferDomainBarrier(domains, domain);
                    domainClashes.remove(domains);
                    System.out.println("Clash removed");
                }
                domain.deactivate(domain.domainOwner);
                return true;
            }
            return false;
        });
    }
}
