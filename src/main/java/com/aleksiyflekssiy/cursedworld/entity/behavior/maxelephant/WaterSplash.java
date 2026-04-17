package com.aleksiyflekssiy.cursedworld.entity.behavior.maxelephant;

import com.aleksiyflekssiy.cursedworld.entity.MaxElephantEntity;
import com.aleksiyflekssiy.cursedworld.network.AddOBBToRenderPacket;
import com.aleksiyflekssiy.cursedworld.network.ModMessages;
import com.aleksiyflekssiy.cursedworld.phys.OBB;
import com.aleksiyflekssiy.cursedworld.util.RotationUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WaterSplash extends Behavior<MaxElephantEntity> {
    private static final int WATER_DURATION = 100;
    private int waterTicks = 0;
    private final List<Vec3> particlePositions = new ArrayList<>();
    private OBB area;

    public WaterSplash(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition) {
        super(pEntryCondition);
    }

    public WaterSplash(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition, int pDuration) {
        super(pEntryCondition, pDuration);
    }

    public WaterSplash(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition, int pMinDuration, int pMaxDuration) {
        super(pEntryCondition, pMinDuration, pMaxDuration);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MaxElephantEntity maxElephant) {
        if (maxElephant.getOwner() == null) return false;
        if (!maxElephant.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) return false;
        return true;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MaxElephantEntity maxElephant, long pGameTime) {
        if (!checkExtraStartConditions(level, maxElephant)) return false;
        return waterTicks < WATER_DURATION;
    }

    @Override
    protected void start(ServerLevel level, MaxElephantEntity maxElephant, long pGameTime) {
        System.out.println("Start");
        setOBB(maxElephant);
    }

    @Override
    protected void tick(ServerLevel level, MaxElephantEntity maxElephant, long pGameTime) {

        ModMessages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) maxElephant.getOwner()), new AddOBBToRenderPacket(area, true));
        float rot = maxElephant.getViewYRot(1) + 1;
        maxElephant.setYBodyRot(rot);
        maxElephant.setYRot(rot);
        maxElephant.setYHeadRot(rot);
        waterTicks++;
        ModMessages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) maxElephant.getOwner()), new AddOBBToRenderPacket(area, false));
        setOBB(maxElephant);
        List<Entity> entities = level.getEntities(maxElephant, area.toAABB());
        entities.forEach(entity -> {
                if (area.contains(entity.position())) entity.setDeltaMovement(maxElephant.getViewVector(1).scale(2));
                });
    }

    private void setOBB(MaxElephantEntity maxElephant) {
        Vec3 center = maxElephant.position().add(RotationUtil.getOffsetLookPosition(maxElephant, maxElephant.getLookAngle(), 0, 0, 25));

        area = new OBB(
                center.add(-5, 0, -25),
                center.add(5, 3, 25),
                new Vector3f(0, maxElephant.getViewYRot(1), 0)
        );
    }

    @Override
    protected void stop(ServerLevel level, MaxElephantEntity maxElephant, long pGameTime) {
        waterTicks = 0;
        ModMessages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) maxElephant.getOwner()), new AddOBBToRenderPacket(area, false));
        area = null;
        particlePositions.clear();
    }

}
