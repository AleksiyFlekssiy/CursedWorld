package com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.tenshadows;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.tutorialmod.entity.ModEntities;
import com.aleksiyflekssiy.tutorialmod.entity.RabbitEscapeEntity;
import com.aleksiyflekssiy.tutorialmod.entity.Shikigami;
import com.aleksiyflekssiy.tutorialmod.network.ModMessages;
import com.aleksiyflekssiy.tutorialmod.network.SkillRenderPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import oshi.util.tuples.Pair;

import java.util.*;

public class RabbitEscape extends ShikigamiSkill {
    private List<Shikigami> rabbits = new ArrayList<>(100);
    private boolean isMoving = false;
    public static final EntityDataAccessor<Boolean> IS_IN_VORTEX = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);

    @Override
    public void use(LivingEntity entity, UseType type, int charge) {
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
                for (int i = 0; i < 100; i++) {
                    RabbitEscapeEntity rabbit = new RabbitEscapeEntity(ModEntities.RABBIT_ESCAPE.get(), entity.level());
                    rabbits.add(rabbit);
                    rabbit.setPos(spawnPos.getX() + random.nextDouble(-1, 1), spawnPos.getY() + random.nextDouble(0, 1), spawnPos.getZ() + random.nextDouble(0, 1));
                    if (isTamed) rabbit.tame((Player) entity);
                    entity.level().addFreshEntity(rabbits.get(i));
                }
                isActive = !isActive;
            }
            else if (isActive && isTamed){
                Level level = entity.level();
                int MAX_RADIUS = 5;
                for (Shikigami rabbit : rabbits) {
                    // Равномерное распределение по сфере с использованием случайных углов
                    double theta = 2 * Math.PI * level.random.nextDouble(); // 0..2π
                    double phi = Math.acos(2 * level.random.nextDouble() - 1); // 0..π

                    double x = MAX_RADIUS * Math.sin(phi) * Math.cos(theta);
                    double y = Math.abs(MAX_RADIUS * Math.sin(phi) * Math.sin(theta));
                    double z = MAX_RADIUS * Math.cos(phi);

                    Vec3 particlePos = entity.position().add(x, y, z);

                    ClipContext clipContext = new ClipContext(entity.position(), particlePos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
                    BlockHitResult hitResult = level.clip(clipContext);

                    if (hitResult.getType() == BlockHitResult.Type.BLOCK) {
                        // Если есть столкновение, берём точку перед блоком
                        particlePos = hitResult.getLocation();
                    }
                    rabbit.setPos(particlePos);
                    double dx = particlePos.x - entity.position().x;
                    double dz = particlePos.z - entity.position().z;

                    double radius = Math.sqrt(dx * dx + dz * dz);
                    double angle = Math.atan2(dz, dx);
                    coords.put(rabbit, new Pair<>(radius, angle));
                }
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
                Vec3 lookAngle = entity.getViewVector(20).scale(0.5);
                entity.setDeltaMovement(lookAngle.x, lookAngle.y, lookAngle.z);
                entity.hurtMarked = true;
                if (!isMoving) ModMessages.INSTANCE.send(PacketDistributor.NEAR.with(
                        () -> new PacketDistributor.TargetPoint(
                                entity.getX(),
                                entity.getY(),
                                entity.getZ(),
                                32,
                                entity.level().dimension())
                ), new SkillRenderPacket(entity.getUUID(), true));
                isMoving = true;
            }
        }
    }

    @Override
    public void release(LivingEntity entity) {
        if (isTamed && isMoving) {
            ModMessages.INSTANCE.send(PacketDistributor.NEAR.with(
                    () -> new PacketDistributor.TargetPoint(
                            entity.getX(),
                            entity.getY(),
                            entity.getZ(),
                            32,
                            entity.level().dimension())
            ), new SkillRenderPacket(entity.getUUID(), false));
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

    }

    @Override
    public String getName() {
        return "rabbit_escape";
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "textures/gui/rabbit_escape.png");
    }
}
