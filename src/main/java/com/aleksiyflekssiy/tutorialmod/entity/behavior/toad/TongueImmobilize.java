package com.aleksiyflekssiy.tutorialmod.entity.behavior.toad;

import com.aleksiyflekssiy.tutorialmod.entity.ToadEntity;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomMemoryModuleTypes;
import com.aleksiyflekssiy.tutorialmod.util.MovementUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

public class TongueImmobilize extends Behavior<ToadEntity> {
    public static final float IMMOBILIZATION_TICKS = 100F;
    private LivingEntity caughtEntity;
    private int catchTick = 0;
    private float initialSpeed;

    public TongueImmobilize(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition) {
        super(pEntryCondition, 0, 72000);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, ToadEntity toad) {
        boolean bool = false;
        if (toad.getBrain().hasMemoryValue(CustomMemoryModuleTypes.GRABBED_ENTITY.get())) {
            if (toad.getOrder() == ToadEntity.ToadOrder.IMMOBILIZE) bool = true;
            else bool = toad.isCooldownOff() && toad.getBrain().getMemory(CustomMemoryModuleTypes.ATTACK_TYPE.get()).get().equals("IMMOBILIZE");
        }
        System.out.println("CHECK IMMOBILIZE: " + bool);
        return bool;
    }

    @Override
    protected boolean canStillUse(ServerLevel pLevel, ToadEntity toad, long pGameTime) {
        if (caughtEntity != null && !caughtEntity.isSpectator() && catchTick <= IMMOBILIZATION_TICKS){
            return toad.getOrder() == ToadEntity.ToadOrder.NONE || toad.getOrder() == ToadEntity.ToadOrder.IMMOBILIZE;
        }
        return false;
    }

    @Override
    protected void start(ServerLevel level, ToadEntity toad, long pGameTime) {
        toad.setLastTickUse();
        System.out.println("START IMMOBILIZE");
        caughtEntity = toad.getBrain().getMemory(CustomMemoryModuleTypes.GRABBED_ENTITY.get()).get();
        immobilize();
    }

    @Override
    protected void tick(ServerLevel level, ToadEntity toad, long pGameTime) {
        if (toad.level().isClientSide() || caughtEntity == null) return;
        toad.setLastTickUse();
        toad.lookAt(EntityAnchorArgument.Anchor.EYES, caughtEntity.position());
        caughtEntity.setDeltaMovement(Vec3.ZERO);
        System.out.println("IMMOBILIZE: " + caughtEntity.getClass().getSimpleName());
        catchTick++;
    }

    @Override
    protected void stop(ServerLevel level, ToadEntity toad, long pGameTime) {
        if (caughtEntity == null) return;
        System.out.println("STOP IMMOBILIZE");
        toad.setDistance(0);
        mobilize();
        caughtEntity = null;
        catchTick = 0;
        toad.setLastTickUse();
        toad.getBrain().eraseMemory(CustomMemoryModuleTypes.GRABBED_ENTITY.get());
    }

//    @SubscribeEvent
//    public void disableMovement(LivingEvent.LivingJumpEvent event) {
//        //Вся система - ебучий костыль.
//        //Необходимо написать систему управления вводом игрока
//        if (event.getEntity().equals(caughtEntity)) {
//            event.getEntity().setDeltaMovement(0, 0, 0);
//        }
//    }

    @SubscribeEvent
    public void disablePlayerInput(MovementInputUpdateEvent event){
        if (event.getEntity().equals(caughtEntity)) {
            MovementUtils.immobilize(event.getInput());
        }
    }

    private void immobilize(){
        initialSpeed = (float) caughtEntity.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();
        caughtEntity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
    }

    private void mobilize(){
        caughtEntity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(initialSpeed);
    }
}
