package com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.domainexpansion;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DomainExpansionManager {
    public static final List<DomainExpansionSkill> domains = new ArrayList<>();
    public static final List<DomainClash> domainClashesNew = new ArrayList<>();
    public static final Map<DomainExpansionSkill, List<Thread>> threads = new HashMap<>();

    public static void addDomainExpansion(DomainExpansionSkill domainExpansionSkill) {
        domains.add(domainExpansionSkill);
    }

    public static boolean willBeDomainClash(DomainExpansionSkill newDomain) {
        Vec3 center = newDomain.domainCenter;
        double radius = newDomain.getCurrentDomainRadius();

        AABB potentialAABB = new AABB(
                center.x - radius - 2, center.y - radius - 2, center.z - radius - 2,
                center.x + radius + 2, center.y + radius + 2, center.z + radius + 2
        );
        for (DomainExpansionSkill oldDomain : domains) {
            if (oldDomain.equals(newDomain)) continue;
            if (oldDomain.getDomainArea().intersects(potentialAABB)) {
                domainClashesNew.add(new DomainClash(oldDomain, newDomain));
                System.out.println(oldDomain.domainOwner.getScoreboardName() + " will clash with " + newDomain.domainOwner.getScoreboardName());
                return true;
            }
        }
        return false;
    }

    public static void toGenerate(DomainExpansionSkill domain, Thread thread){
        if (threads.containsKey(domain)){
            threads.get(domain).add(thread);
        }
        else {
            List<Thread> domainThreads = new ArrayList<>();
            domainThreads.add(thread);
            threads.put(domain, domainThreads);
        }
    }

    public static boolean isClashing(DomainExpansionSkill domain){
        if (domain == null) return false;
        for (DomainClash clash : domainClashesNew) {
            if (clash.isClashing(domain)) {
                return true;
            }
        }
        return false;
    }

    public static DomainClash getDomainClash(DomainExpansionSkill domain){
        for (DomainClash clash : domainClashesNew) {
            if (clash.isClashing(domain)) {
                return clash;
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void tickDomains(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        //System.out.println("Domains: " + domains.size() + "  Domain Clashes: " + domainClashes.size());
        threads.forEach((domain, thread) -> {
            if (!thread.isEmpty()){
                Thread t1 = thread.get(0);
                t1.start();
                thread.remove(t1);
            }
        });
        domains.removeIf(domain -> {
            domain.tick();
            if (!domain.isActive() || domain.isExpired() || domain.checkBarrierDamage()){
                System.out.println("Domain removed");
                domain.deactivate(domain.domainOwner);
                return true;
            }
            return false;
        });
        //Если удалить второй домен, то в списке останется лишь победитель. Точнее один = победитель
        domainClashesNew.removeIf(domainClash -> {
            domainClash.tick();
            if (domainClash.isGoing()) return false;
            else {
                System.out.println("Clash ended");
            }
            return true;
        });
    }
}
