package com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.domainexpansion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DomainClash {
    private final List<DomainExpansionSkill> clashingDomains = new ArrayList<>();
    private final List<Float> ownersInitialHP = new ArrayList<>();
    private int clashTickCount = 0;
    private List<BlockPos> barrierBlocks;
    private boolean isGoing = true;


    private DomainExpansionSkill winner = null;

    public DomainClash(DomainExpansionSkill firstDomain, DomainExpansionSkill secondDomain) {
        this.clashingDomains.add(firstDomain);
        this.clashingDomains.add(secondDomain);

        this.ownersInitialHP.add(firstDomain.domainOwner.getHealth());
        this.ownersInitialHP.add(secondDomain.domainOwner.getHealth());

        this.barrierBlocks = new ArrayList<>(firstDomain.barrierBlocks);
        this.barrierBlocks.addAll(secondDomain.barrierBlocks);
    }

    public boolean isGoing() {
        return isGoing;
    }

    private void setWinner(DomainExpansionSkill winner) {
        this.winner = winner;
    }

    public void tick(){
        prepareDomains();
        if (isGoing) {
            if (clashTickCount % 20 == 0) {
                updateClashBarrier(this, (ServerLevel) clashingDomains.get(0).domainOwner.level());
            }
            clashTickCount++;
        }

    }

    private void prepareDomains() {
        for (DomainExpansionSkill domain : clashingDomains) {
            if (!domain.isActive() || domain.isExpired()) {
                transferDomainBarrier(domain);
                clashingDomains.remove(domain);
            }
            if (clashingDomains.size() == 1) {
                setWinner(clashingDomains.get(0));
                isGoing = false;
                return;
            }

            domain.domainOwner.sendSystemMessage(Component.literal("Power: " + getDomainPower(domain)));
        }
    }

    public static void updateClashBarrier(DomainClash clash, ServerLevel level) {
        List<DomainExpansionSkill> domains = clash.getDomains();
        if (domains.size() < 2) return;

        // Считаем силу каждого домена
        List<Float> powers = new ArrayList<>();
        float maxPower = 0f;
        float minPower = Float.MAX_VALUE;
        DomainExpansionSkill strongest = null;
        DomainExpansionSkill weakest = null;

        for (DomainExpansionSkill domain : domains) {
            float power = clash.getDomainPower(domain);
            powers.add(power);

            if (power > maxPower) {
                maxPower = power;
                strongest = domain;
            }
            if (power < minPower) {
                minPower = power;
                weakest = domain;
            }
        }

        float INSTANT_DEFEAT_RATIO = 3.0f;
        if (maxPower >= minPower * INSTANT_DEFEAT_RATIO && strongest != null && weakest != null) {
            instantDefeat(weakest, strongest, level);
            return; // битва окончена
        }

        // Обычный захват барьера (если никто не раздавил мгновенно)
        Set<BlockPos> allBarrierBlocks = new HashSet<>();
        for (DomainExpansionSkill d : domains) {
            allBarrierBlocks.addAll(d.barrierBlocks);
        }

        for (BlockPos pos : allBarrierBlocks) {
            DomainExpansionSkill closestOwner = null;
            float bestScore = -1f;

            for (int i = 0; i < domains.size(); i++) {
                DomainExpansionSkill domain = domains.get(i);
                float power = powers.get(i);

                double dist = pos.distToCenterSqr(domain.startPos.getX(), domain.startPos.getY(), domain.startPos.getZ());
                float score = power / (float)(dist + 1);

                if (score > bestScore) {
                    bestScore = score;
                    closestOwner = domain;
                }
            }

            if (closestOwner != null) {
                BlockState current = level.getBlockState(pos);
                BlockState target = closestOwner.domainBlock;

                if (!current.is(target.getBlock())) {

                    level.setBlock(pos, target, 3);

                    for (DomainExpansionSkill d : domains) {
                        if (d != closestOwner) {
                            d.barrierBlocks.remove(pos.immutable());
                        }
                    }
                    closestOwner.barrierBlocks.add(pos.immutable());

                    level.sendParticles(ParticleTypes.WITCH,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            2, 0.2, 0.2, 0.2, 0.05);
                }
            }
        }
    }

    private static void instantDefeat(DomainExpansionSkill loser, DomainExpansionSkill winner, ServerLevel level) {
        level.playSound(null, loser.domainOwner.blockPosition(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 5.0F, 0.9F);

        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                loser.domainOwner.getX(), loser.domainOwner.getY() + 1, loser.domainOwner.getZ(),
                10, 3, 3, 3, 0.3);

        winner.domainOwner.sendSystemMessage(
                Component.literal(loser.domainOwner.getName().getString() + "'s domain was annihilated!"));
        loser.domainOwner.sendSystemMessage(
                Component.literal("Your domain was crushed by overwhelming power!"));

        // Переносим весь барьер
        for (BlockPos pos : new ArrayList<>(loser.barrierBlocks)) {
            BlockPos immutable = pos.immutable();

            level.setBlock(pos, winner.domainBlock, 3);

            loser.barrierBlocks.remove(immutable);

            winner.barrierBlocks.add(immutable);
        }

        loser.deactivate(loser.domainOwner);
    }

    public static void transferDomainBarrier(DomainExpansionSkill lostDomain){
        DomainClash domainClash = DomainExpansionManager.getDomainClash(lostDomain);
        List<DomainExpansionSkill> clashingDomains = new ArrayList<>(domainClash.clashingDomains);
        clashingDomains.remove(lostDomain);
        int domainCount = clashingDomains.size();
        if (domainCount == 1) {
            DomainExpansionSkill wonDomain = clashingDomains.get(0);
            BlockState winnerBlock = wonDomain.domainBlock;
            for (BlockPos barrierBlock : lostDomain.barrierBlocks) {
                lostDomain.domainOwner.level().setBlock(barrierBlock, winnerBlock, 3);
                //Ошибка? Блоки возвращаются, но цвет барьера остался
            }
            wonDomain.originalBlocks.putAll(lostDomain.originalBlocks);
            wonDomain.barrierBlocks.addAll(lostDomain.barrierBlocks);
            if (lostDomain.domainArea != null) wonDomain.domainArea = lostDomain.domainArea;
            lostDomain.barrierBlocks.clear();
            lostDomain.originalBlocks.clear();
            lostDomain.domainArea = null;
        }
        System.out.println("Restoration in Manager");
    }

    public List<DomainExpansionSkill> getDomains(){
        return clashingDomains;
    }

    public boolean isClashing(DomainExpansionSkill domain){
        return clashingDomains.contains(domain);
    }

    private float getDomainPower(DomainExpansionSkill domain){
        float power = 0;
        if (domain.equals(clashingDomains.get(0))) {
            power += 1f;
        }
        else power += 0.75f;
        power *= getDamagePercentage(domain);
        return power;
    }

    private float getDamagePercentage(DomainExpansionSkill domain){
        int index = this.clashingDomains.indexOf(domain);
        return Mth.clamp(domain.domainOwner.getHealth() / ownersInitialHP.get(index), 0.125f, 1f);
    }
}
