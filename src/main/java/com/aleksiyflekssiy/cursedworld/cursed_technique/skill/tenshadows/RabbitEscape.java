package com.aleksiyflekssiy.cursedworld.cursed_technique.skill.tenshadows;

import com.aleksiyflekssiy.cursedworld.CursedWorld;
import com.aleksiyflekssiy.cursedworld.client.renderer.RabbitSwarmRenderer;
import com.aleksiyflekssiy.cursedworld.cursed_technique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.cursedworld.entity.ModEntities;
import com.aleksiyflekssiy.cursedworld.entity.RabbitEscapeEntity;
import com.aleksiyflekssiy.cursedworld.entity.Shikigami;
import com.aleksiyflekssiy.cursedworld.network.ModMessages;
import com.aleksiyflekssiy.cursedworld.network.SwarmRenderPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import oshi.util.tuples.Pair;

import java.util.*;

public class RabbitEscape extends ShikigamiSkill {
    private int orderIndex = 0;
    private List<Shikigami> rabbits = new ArrayList<>(10);
    private boolean isMoving = false;

    @Override
    public void use(LivingEntity entity, UseType type, int charge) {
        if (entity.level().isClientSide) return;
        switch (type) {
            case ACTIVATION -> this.activate(entity);
            case CHARGING -> this.charge(entity, charge);
            case RELEASING -> this.release(entity);
        }
    }

    @Override
    public void activate(LivingEntity entity) {
        if (isDead) return;
        if (!entity.isCrouching()){
            if (!isActive){
                rabbits.clear();
                BlockPos spawnPos = entity.blockPosition();
                Random random = new Random();
                for (int i = 0; i < 10; i++) {
                    RabbitEscapeEntity rabbit = new RabbitEscapeEntity(ModEntities.RABBIT_ESCAPE.get(), entity.level());
                    rabbits.add(rabbit);
                    rabbit.setPos(spawnPos.getX() + random.nextDouble(-5, 5), spawnPos.getY(), spawnPos.getZ() + random.nextDouble(-5, 5));
                    if (isTamed) rabbit.tame((Player) entity);
                    entity.level().addFreshEntity(rabbits.get(i));
                }
                isActive = !isActive;
            }
            else if (isActive && isTamed){
                setTarget(entity,
                        blockPos -> {
                            Random random = new Random();
                            for (Shikigami rabbit : rabbits) {
                                BlockPos finalBlockPos = blockPos.offset(random.nextInt(-5, 5), 0, random.nextInt(-5, 5));
                                rabbit.followOrder(null, finalBlockPos, RabbitEscapeEntity.RabbitEscapeOrder.values()[orderIndex]);
                            }
                },
                        target -> {
                            if (orderIndex == 2) this.surround(target);
                            else {
                                for (Shikigami rabbit : rabbits) {
                                    rabbit.followOrder(target, null, RabbitEscapeEntity.RabbitEscapeOrder.values()[orderIndex]);
                                }
                            }
                        });
            }
        }
        else {
            if (isActive && isTamed){
                System.out.println("DEACTIVATE");
                for (Shikigami rabbit : rabbits){
                    rabbit.discard();
                }
                rabbits.clear();
                shikigamiUUIDList.clear();
                isActive = !isActive;
            }
        }
    }

    private void surround(LivingEntity target){
        boolean isContained = RabbitSwarmRenderer.ID_RENDER_TARGETS.containsKey(target.getId());
        ModMessages.INSTANCE.send(PacketDistributor.ALL.noArg(), new SwarmRenderPacket(target.getId(), !isContained, false));
    }

    private final Map<Shikigami, Pair<Double, Double>> coords = new HashMap<>();

    @Override
    public void charge(LivingEntity entity, int charge) {
        if (isDead) return;
        if (!entity.isCrouching()){
            if (isActive && isTamed && !coords.isEmpty()){
                for (Shikigami rabbit : rabbits) {
                    oshi.util.tuples.Pair<Double, Double> values = coords.get(rabbit);
                    double radius = values.getA();
                    // радиус круга
                    double angularSpeed = 0.2;
                    double angle = values.getB() + angularSpeed;

                    // Вычисляем новые координаты
                    double centerX = entity.position().x; // центр круга (например, spawnPos.getX() + 0.5)
                    double centerZ = entity.position().z; // центр круга

                    double newX = centerX + radius * Math.cos(angle);
                    double newZ = centerZ + radius * Math.sin(angle);

                    // Перемещаем сущность
                    rabbit.setPos(newX, rabbit.position().y, newZ);
                    coords.put(rabbit, new Pair<>(radius, angle));
                }
            }
        }
        else {
            if (isTamed) {
                Vec3 lookAngle = entity.getViewVector(1).scale(0.5);
                entity.setDeltaMovement(lookAngle.x, lookAngle.y, lookAngle.z);
                entity.hurtMarked = true;
                if (!isMoving) {
                    ModMessages.INSTANCE.send(PacketDistributor.ALL.noArg(), new SwarmRenderPacket(entity.getId(), true, true));
                }
                isMoving = true;
            }
        }
    }

    @Override
    public void release(LivingEntity entity) {
        if (isTamed && isMoving) {
            ModMessages.INSTANCE.send(PacketDistributor.ALL.noArg(), new SwarmRenderPacket(entity.getId(), false, true));
            isMoving = false;
        }
    }

    @Override
    public List<Shikigami> getShikigami() {
        return rabbits;
    }

    @Override
    public void setShikigami(List<Shikigami> shikigamiList) {

    }

    @Override
    public void switchOrder(LivingEntity owner, int direction) {
        if (isTamed) {
            switch (direction){
                case -1 -> {
                    if (--orderIndex <= -1) orderIndex = 3;
                }
                case 1 -> {
                    if (++orderIndex >= 4) orderIndex = 0;
                }
            }
            switch (orderIndex) {
                case 0 -> owner.sendSystemMessage(Component.literal("NONE"));
                case 1 -> owner.sendSystemMessage(Component.literal("ATTACK"));
                case 2 -> owner.sendSystemMessage(Component.literal("SURROUND"));
                case 3 -> owner.sendSystemMessage(Component.literal("MOVE"));
            }
        }
    }

    @Override
    public String getName() {
        return "rabbit_escape";
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return ResourceLocation.fromNamespaceAndPath(CursedWorld.MOD_ID, "textures/gui/rabbit_escape.png");
    }
}
