package com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.domainexpansion;

import com.aleksiyflekssiy.tutorialmod.capability.CursedEnergyCapability;
import com.aleksiyflekssiy.tutorialmod.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.tutorialmod.config.ModConfig;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.Skill;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public abstract class DomainExpansionSkill extends Skill {
    public static int DOMAIN_DURATION;
    public static double DOMAIN_RADIUS;
    protected static final float BOUNDARY_THICKNESS = 1F;
    protected static final BlockState DEFAULT_BARRIER_BLOCK = Blocks.BLACK_CONCRETE.defaultBlockState();
    protected static final BlockState CLASHING_BARRIER_BLOCK = Blocks.WHITE_CONCRETE.defaultBlockState();

    protected final Map<BlockPos, BlockState> originalBlocks = new HashMap<>();
    protected final List<BlockPos> barrierBlocks = new ArrayList<>();
    protected final Set<LivingEntity> affectedEntities = new HashSet<>();

    public BlockPos startPos;
    protected int domainTicks;
    protected double currentDomainRadius;
    protected int charge;
    protected boolean isActive;
    protected boolean isOpen = false;
    protected BlockState domainBlock = DEFAULT_BARRIER_BLOCK;
    protected AABB domainArea = null;
    protected Vec3 domainCenter = null;
    protected Player domainOwner = null;

    @Override
    public void use(LivingEntity entity, UseType type, int charge) {
        if (!(entity instanceof ServerPlayer)) return;
        this.domainOwner = (Player) entity;
        switch (type){
            case ACTIVATION -> {
                if (!isActive) this.activate((Player) entity, entity.level());
                else this.deactivate(entity);
            }
            case CHARGING -> this.charge(entity, charge);
            case RELEASING -> this.release(entity);
        }
    }

    public void activate(Player player, Level level) {
        if (level.isClientSide()) return;

        if (!isActive) {
            if (!spendCursedEnergy(player, 50)) return;
            // Центр сферы над игроком, пол под ногами
            if (player.isCrouching()) deployBarrierMultithread((ServerLevel) level, player);
            else deployBarrier((ServerLevel) level, player);
            if (!DomainExpansionManager.isClashing(this)) setupActivation(player);
            player.sendSystemMessage(Component.literal("Domain Expansion: "+ getName() +" activated!"));
            System.out.println(domainOwner.getScoreboardName() + " is activated");
            isActive = true;

            DomainExpansionManager.addDomainExpansion(this);
        }
    }

    protected abstract void setupActivation(Player player);
    protected abstract void setupDeactivation(Player player);

    public void deactivate(LivingEntity entity) {
        if (isActive) {
            System.out.println(domainOwner.getScoreboardName() + " is deactivated");
            setupDeactivation((Player) entity);
            applyTechniqueBurnout(domainOwner);
            restoreOriginalBlocks((ServerLevel) entity.level());
            isActive = false;
        }
    }

    public void charge(LivingEntity entity, int charge){
        if (charge <= 200){
            if (charge % 100 == 0) {
                this.charge++;
                entity.sendSystemMessage(Component.literal("Charge: " + this.charge));
            }
        }
    }

    public void release(LivingEntity entity){
        if (!isActive) this.activate((Player) entity, entity.level());
        this.charge = 0;
    }

    public double getCurrentDomainRadius() {
        return currentDomainRadius;
    }

    public AABB getDomainArea() {
        return domainArea;
    }

    public Vec3 getDomainCenter() {
        return domainCenter;
    }

    protected void deployBarrier(ServerLevel level, Player player) {
        double diameter = DOMAIN_RADIUS * 2 * Math.max(1, charge);
        currentDomainRadius = diameter / 2;

        // Пол — ровно на уровне ног игрока
        double floorY = player.blockPosition().getY()-1;

        Vec3 eyePos = player.getEyePosition(1.0F); // Позиция глаз игрока
        Vec3 lookVec = player.getViewVector(1.0F); // Вектор взгляда
        Vec3 endPos = eyePos.add(lookVec.x * currentDomainRadius / 2, lookVec.y * currentDomainRadius / 2, lookVec.z * currentDomainRadius / 2);

        // Центр всей сферы — ровно на уровне пола (игрок в центре сферы!)
        Vec3 center = new Vec3(endPos.x, floorY, endPos.z);

        domainCenter = center;
        domainTicks = 0;

        BlockPos centerPos = BlockPos.containing(center);
        startPos = centerPos;
        int scanRadius = (int) Math.ceil(currentDomainRadius + (double) BOUNDARY_THICKNESS + 3);

        if (!DomainExpansionManager.willBeDomainClash(this)) {
            domainBlock = DEFAULT_BARRIER_BLOCK;
            int floorBlockY = (int) floorY;
            int realtotal = 0;
            for (int x = -scanRadius; x <= scanRadius; x++) {
                for (int z = -scanRadius; z <= scanRadius; z++) {
                    for (int y = -scanRadius; y <= scanRadius; y++) {
                        BlockPos pos = centerPos.offset(x, y, z);
                        realtotal++;
                        double dx = pos.getX() + 0.5 - center.x;
                        double dy = pos.getY() + 0.5 - center.y;
                        double dz = pos.getZ() + 0.5 - center.z;
                        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                        // === ПОЛ — ровная плоскость на уровне игрока ===
                        if (pos.getY() == floorBlockY) {
                            double horizDist = Math.sqrt(dx * dx + dz * dz);
                            if (horizDist <= currentDomainRadius + (double) BOUNDARY_THICKNESS) {
                                originalBlocks.put(pos.immutable(), level.getBlockState(pos));
                                if (!isOpen) {
                                    level.setBlock(pos, domainBlock, 3);
                                    barrierBlocks.add(pos.immutable());
                                }
                            }
                            continue;
                        }

                        // === Стенки сферы (верхняя и нижняя полусферы) ===
                        if (distance <= currentDomainRadius + (double) BOUNDARY_THICKNESS && distance > currentDomainRadius - (double) BOUNDARY_THICKNESS) {
                            originalBlocks.put(pos.immutable(), level.getBlockState(pos));
                            if (!isOpen) {
                                level.setBlock(pos, domainBlock, 3);
                                barrierBlocks.add(pos.immutable());
                            }
                        }
                        // === Внутри сферы — воздух (кроме пола) ===
                        else if (distance <= currentDomainRadius - (double) BOUNDARY_THICKNESS) {
                            if (!originalBlocks.containsKey(pos.immutable())) {
                                originalBlocks.put(pos.immutable(), level.getBlockState(pos));
                            }
                            if (!isOpen) {
                                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                            }
                        }
                    }
                }
            }
            System.out.println(realtotal);
            domainArea = new AABB(
                    center.x - currentDomainRadius - (double) BOUNDARY_THICKNESS - 2, center.y - currentDomainRadius - (double) BOUNDARY_THICKNESS - 2, center.z - currentDomainRadius - (double) BOUNDARY_THICKNESS - 2,
                    center.x + currentDomainRadius + (double) BOUNDARY_THICKNESS + 2, center.y + currentDomainRadius + (double) BOUNDARY_THICKNESS + 2, center.z + currentDomainRadius + (double) BOUNDARY_THICKNESS + 2
            );

            // Все сущности — на пол
            if (!isOpen) {
                for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, domainArea.expandTowards(0, currentDomainRadius * 2, 0))) {
                    if (entity != player && canAffect(entity)) {
                        entity.teleportTo(entity.getX(), floorY + 1.1, entity.getZ());
                        affectedEntities.add(entity);
                    }
                }
            }
        }
        else {
            invadeDomain(level, player);
        }
    }

    protected void deployBarrierMultithread(ServerLevel level, Player player) {
        double diameter = DOMAIN_RADIUS * 2 * Math.max(1, charge);
        currentDomainRadius = diameter / 2;
        double thickness = BOUNDARY_THICKNESS;

        // Пол — ровно на уровне ног игрока
        double floorY = player.blockPosition().getY()-1;

        Vec3 eyePos = player.getEyePosition(1.0F); // Позиция глаз игрока
        Vec3 lookVec = player.getViewVector(1.0F); // Вектор взгляда
        Vec3 endPos = eyePos.add(lookVec.x * currentDomainRadius / 2, lookVec.y * currentDomainRadius / 2, lookVec.z * currentDomainRadius / 2);

        // Центр всей сферы — ровно на уровне пола (игрок в центре сферы!)
        Vec3 center = new Vec3(endPos.x, floorY, endPos.z);

        domainCenter = center;
        domainTicks = 0;

        BlockPos centerPos = BlockPos.containing(center);
        startPos = centerPos;
        int scanRadius = (int) Math.ceil(currentDomainRadius + thickness + 3);

        if (!DomainExpansionManager.willBeDomainClash(this)) {
            domainBlock = DEFAULT_BARRIER_BLOCK;
            int floorBlockY = (int) floorY;
            int totalBlocks = 0;
            for (int x = -scanRadius; x <= scanRadius; x++) {
                for (int z = -scanRadius; z <= scanRadius; z++) {
                    for (int y = -scanRadius; y <= scanRadius; y++) {
                        totalBlocks++;
                    }
                }
            }
            int blocksPerTick = totalBlocks / 100;

            System.out.println("Total blocks: " + totalBlocks + " Blocks per tick: " + blocksPerTick);

            int mustBeReal = 0;
            try {
                for (int i = 0; i < 10; i++) {
                    int finalI = i;
                    Thread thread = new Thread(() -> {
                        for (int x = -scanRadius + (scanRadius * finalI); x <= scanRadius - (scanRadius * finalI); x++) {
                            for (int z = -scanRadius + (scanRadius * finalI); z <= scanRadius - (scanRadius * finalI); z++) {
                                for (int y = -scanRadius + (scanRadius * finalI); y <= scanRadius - (scanRadius * finalI); y++) {
                                    BlockPos pos = centerPos.offset(x, y, z);
                                    double dx = pos.getX() + 0.5 - center.x;
                                    double dy = pos.getY() + 0.5 - center.y;
                                    double dz = pos.getZ() + 0.5 - center.z;
                                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                                    // === ПОЛ — ровная плоскость на уровне игрока ===
                                    if (pos.getY() == floorBlockY) {
                                        double horizDist = Math.sqrt(dx * dx + dz * dz);
                                        if (horizDist <= currentDomainRadius + thickness) {
                                            originalBlocks.put(pos.immutable(), level.getBlockState(pos));
                                            if (!isOpen) {
                                                level.setBlock(pos, domainBlock, 3);
                                                barrierBlocks.add(pos.immutable());
                                            }
                                        }
                                        continue;
                                    }

                                    // === Стенки сферы (верхняя и нижняя полусферы) ===
                                    if (distance <= currentDomainRadius + thickness && distance > currentDomainRadius - thickness) {
                                        originalBlocks.put(pos.immutable(), level.getBlockState(pos));
                                        if (!isOpen) {
                                            level.setBlock(pos, domainBlock, 3);
                                            barrierBlocks.add(pos.immutable());
                                        }
                                    }
                                    // === Внутри сферы — воздух (кроме пола) ===
                                    else if (distance <= currentDomainRadius - thickness) {
                                        if (!originalBlocks.containsKey(pos.immutable())) {
                                            originalBlocks.put(pos.immutable(), level.getBlockState(pos));
                                        }
                                        if (!isOpen && !domainOwner.level().getBlockState(pos).is(Blocks.AIR)) {
                                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                                        }
                                    }
                                }
                            }
                        }
                    });
                    DomainExpansionManager.toGenerate(this, thread);
                }
                System.out.println("Real total: " + mustBeReal);
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        else {
            invadeDomain(level, player);
        }
    }

    private void invadeDomain(ServerLevel level, Player player) {
        System.out.println("INVASION");

        DomainClash clash = DomainExpansionManager.getDomainClash(this);

        domainBlock = CLASHING_BARRIER_BLOCK;

        DomainExpansionSkill victim = clash.getDomains().get(0);

        Vec3 victimCenter = victim.getDomainCenter();
        double invadeRadius = victim.getCurrentDomainRadius() * 0.4; // сколько "захватываем"

        player.teleportTo(player.getX(), victimCenter.y + 1.1, player.getZ());
        startPos = player.blockPosition().below();

        BlockPos playerPos = player.blockPosition();
        int r = (int) Math.ceil(invadeRadius);

        // Используем immutable ключи!
        List<BlockPos> capturedPositions = new ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.offset(-r, -r, -r),
                playerPos.offset(r, r, r))) {

            BlockPos immutable = pos.immutable();

            // Проверяем, что это действительно барьер жертвы
            if (victim.barrierBlocks.contains(immutable)) {
                capturedPositions.add(immutable);

                // 1. Сохраняем оригинальный блок (если ещё нет)
                BlockState original = victim.originalBlocks.get(immutable);
                if (original != null) {
                    this.originalBlocks.put(immutable, original);
                }

                // 2. Заменяем блок на наш барьер
                level.setBlock(pos, domainBlock, 3);

                // 3. Удаляем из списка жертвы БЕЗОПАСНО
                victim.barrierBlocks.remove(immutable);
                victim.originalBlocks.remove(immutable); // теперь ключ совпадает!
            }
        }

        // 4. Добавляем в свой список
        this.barrierBlocks.addAll(capturedPositions);

        System.out.println(domainOwner.getScoreboardName() + " захвачено " + capturedPositions.size() +
                " блоков у " + victim.domainOwner.getScoreboardName());

        // Опционально: эффект вторжения
        level.sendParticles(ParticleTypes.DRAGON_BREATH,
                player.getX(), player.getY() + 1, player.getZ(),
                50, 2, 2, 2, 0.1);
    }


    protected abstract void applySureHitEffect();

    public void tick(){
        if (isActive && domainCenter != null) {
            domainTicks++;
                //domainOwner.sendSystemMessage(Component.literal("Ticks: " + domainTicks));
                if (!DomainExpansionManager.isClashing(this)) {
                    applySureHitEffect();
                    //System.out.println("SURE-HIT " + domainOwner.getScoreboardName());
                }
            }
            //else System.out.println("CLASH " + domainOwner.getScoreboardName());
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isExpired(){
        boolean bool = domainTicks >= DOMAIN_DURATION;
        if (bool) {
            System.out.println(domainOwner.getScoreboardName() + " is expired");
            return true;
        }
        return false;
    }

    public boolean checkBarrierDamage() {
        if (isOpen || DomainExpansionManager.isClashing(this)) return false;
        int blocks = barrierBlocks.size();
        int brokenBlocks = 0;
//        for (BlockPos barrierBlock : barrierBlocks) {
//            if (domainOwner.level().getBlockState(barrierBlock).getBlock() != Blocks.BLACK_CONCRETE
//            && domainOwner.level().getBlockState(barrierBlock).getBlock() != Blocks.WHITE_CONCRETE) brokenBlocks++;
//        }
        boolean bool = ((float) brokenBlocks / blocks) >= 0.35f;
        if (bool) {
            System.out.println(domainOwner.getScoreboardName() + " is broken");
            return true;
        }
        return false;
    }

    protected void restoreOriginalBlocks(ServerLevel level) {
        if (!isOpen) {
            if (!DomainExpansionManager.isClashing(this)) {
                for (Map.Entry<BlockPos, BlockState> entry : originalBlocks.entrySet()) {
                    level.setBlock(entry.getKey(), entry.getValue(), 3);
                }
                System.out.println("Returned blocks to original");
            }
        }
        affectedEntities.clear();
        if (!DomainExpansionManager.isClashing(this)) {
            barrierBlocks.clear();
            originalBlocks.clear();
            domainArea = null;
            startPos = null;
            currentDomainRadius = 0;
            System.out.println(domainOwner.getScoreboardName() + " isn't clashed");
        }
        domainCenter = null;
        domainBlock = DEFAULT_BARRIER_BLOCK;
        System.out.println("Restoration is skill");
    }

    protected void applyTechniqueBurnout(Player player){
        player.getCapability(CursedTechniqueCapability.CURSED_TECHNIQUE).ifPresent(cursedTechnique -> {
            List<Skill> skills = cursedTechnique.getSkillSet();
            skills.forEach(skill -> {
                skill.setCooldown(player, 1200);
            });
        });
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (obj instanceof DomainExpansionSkill domain) {
            return this.domainOwner.equals(domain.domainOwner);
        }
        return false;
    }
}
