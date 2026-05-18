package com.aleksiyflekssiy.cursedworld.entity;

import com.aleksiyflekssiy.cursedworld.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.cursedworld.cursed_technique.CursedTechnique;
import com.aleksiyflekssiy.cursedworld.cursed_technique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.cursedworld.cursed_technique.skill.Skill;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public abstract class Shikigami extends PathfinderMob {
    protected Player owner;
    protected boolean isTamed = false;
    protected ShikigamiOrder currentOrder = ShikigamiOrder.NONE;
    protected boolean requiresBinding = false;

    public Shikigami(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.owner = null;
    }

    public Shikigami(EntityType<? extends PathfinderMob> pEntityType, Level pLevel, Player owner) {
        super(pEntityType, pLevel);
        this.owner = owner;
    }

    public void tame(Player owner){
        this.isTamed = true;
    }

    public boolean isTamed() {
        return isTamed;
    }

    public Player getOwner() {
        return owner;
    }

    public ShikigamiOrder getOrder(){
        return this.currentOrder;
    }

    public void setOrder(ShikigamiOrder order){
        this.currentOrder = order;
    }

    public boolean followOrder(LivingEntity target, BlockPos blockPos, ShikigamiOrder order){
        if (this.isTamed() && this.owner != null) {
            setOrder(order);
            return true;
        }
        return false;
    }

    public void clearOrder(){
        this.setOrder(null);
    }

    protected Vec3 getRiddenInput(Player player, Vec3 travelVector) {
        byte x = 0;
        byte y = 0;
        byte z = 0;
        Minecraft client = Minecraft.getInstance();
        if (client.options.keyUp.isDown()) z += 1;
        if (client.options.keyDown.isDown()) z -= 1;
        if (client.options.keyLeft.isDown()) x += 1;
        if (client.options.keyRight.isDown()) x -= 1;
        if (client.options.keyJump.isDown()) y += 1;
        if (client.options.keySprint.isDown()) y -= 1;
        return new Vec3(x, y, z);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (getOwner() != null) {
            tag.putUUID("ownerUUID", owner.getUUID());
            System.out.println("UUID: " + owner.getUUID());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("ownerUUID")) {
            UUID ownerUUID = tag.getUUID("ownerUUID");
            owner = level().getPlayerByUUID(ownerUUID);
        }
        if (owner != null){
            requiresBinding = true;
        }
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (this.getCorrespondingSkill() != null && requiresBinding) bindShikigamiToSkill();
    }

    protected Skill getCorrespondingSkill(){
        return null;
    }

    private void bindShikigamiToSkill(){
        owner.getCapability(CursedTechniqueCapability.CURSED_TECHNIQUE).ifPresent(capability -> {
            CursedTechnique technique = capability.getTechnique();
            for (Skill skill : technique.getSkillSet()){
                if (skill.equals(getCorrespondingSkill())){
                    ShikigamiSkill shikigamiSkill = (ShikigamiSkill) skill;
                    if (shikigamiSkill.isTamed()) tame(owner);
                    shikigamiSkill.setShikigami(List.of(this));
                    System.out.println(shikigamiSkill.getName() + " was initialized");
                }
            }
        });
    }
}
