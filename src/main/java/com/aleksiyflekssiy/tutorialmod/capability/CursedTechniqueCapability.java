package com.aleksiyflekssiy.tutorialmod.capability;

import com.aleksiyflekssiy.tutorialmod.cursed_technique.CursedTechnique;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.LimitlessCursedTechnique;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.TenShadowsTechnique;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.*;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.limitless.*;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.tenshadows.DivineDogs;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.tenshadows.GreatSerpent;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.tenshadows.Nue;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.tenshadows.Toad;
import com.aleksiyflekssiy.tutorialmod.network.ModMessages;
import com.aleksiyflekssiy.tutorialmod.network.TechniqueSyncPacket;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CursedTechniqueCapability {
    public static final Capability<ICursedTechnique> CURSED_TECHNIQUE = CapabilityManager.get(new CapabilityToken<>() {});

    public interface ICursedTechnique {
        CursedTechnique getTechnique();
        void setTechnique(CursedTechnique technique);
        List<Skill> getSkillSet();
        Skill getCurrentSkill();
        void setCurrentSkill(Skill skill);
        Skill getFirstSkill();
        void nextSkill(); // Переключение на следующий скилл
        void previousSkill(); // Переключение на предыдущий скилл
        CompoundTag serializeNBT();

        CompoundTag serializeNbtToNetwork();

        void deserializeNBT(CompoundTag nbt);
    }

    public static class TechniqueHolder implements ICursedTechnique {
        private CursedTechnique technique;
        private Skill currentSkill;

        public TechniqueHolder() {
            this.technique = new LimitlessCursedTechnique(); // По умолчанию
            this.currentSkill = technique.getSkillSet().get(0); // Первый скилл техники
        }

        @Override
        public CursedTechnique getTechnique() {
            return technique;
        }

        @Override
        public void setTechnique(CursedTechnique technique) {
            this.technique = technique != null ? technique : new LimitlessCursedTechnique();
            // При смене техники сбрасываем текущий скилл на первый в новом наборе
            this.currentSkill = this.technique.getSkillSet().isEmpty() ? null : this.technique.getSkillSet().get(0);
        }

        @Override
        public List<Skill> getSkillSet() {
            return technique.getSkillSet();
        }

        @Override
        public Skill getCurrentSkill() {
            return currentSkill;
        }

        @Override
        public void setCurrentSkill(Skill skill) {
            // Проверяем, что скилл принадлежит текущей технике
            if (skill != null) {
                for (Skill existingSkill : technique.getSkillSet()) {
                    if (existingSkill.getName().equals(skill.getName())) this.currentSkill = skill;
                }
            } else if (!technique.getSkillSet().isEmpty()) {
                this.currentSkill = technique.getSkillSet().get(0); // Запасной вариант — первый скилл
            }
        }

        @Override
        public Skill getFirstSkill() {
            return technique.getSkillSet().isEmpty() ? null : technique.getSkillSet().get(0);
        }

        @Override
        public void nextSkill() {
            List<Skill> skillSet = getSkillSet();
            if (skillSet.isEmpty()) return;

            int currentIndex = skillSet.indexOf(currentSkill);
            if (currentIndex == -1) {
                currentSkill = skillSet.get(0); // Если текущего скилла нет в списке
            } else {
                int nextIndex = (currentIndex + 1) % skillSet.size(); // Циклическое переключение
                currentSkill = skillSet.get(nextIndex);
            }
//            System.out.println("Current Skill: " + currentSkill.getName());
//            System.out.println("Current Technique: " + technique.getName());
        }

        @Override
        public void previousSkill() {
            List<Skill> skillSet = getSkillSet();
            if (skillSet.isEmpty()) return;

            int currentIndex = skillSet.indexOf(currentSkill);
            if (currentIndex == -1) {
                currentSkill = skillSet.get(0); // Если текущего скилла нет в списке
            } else {
                int prevIndex = (currentIndex - 1 + skillSet.size()) % skillSet.size(); // Циклическое переключение
                currentSkill = skillSet.get(prevIndex);
            }
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            if (technique != null) {
                tag.putString("technique_name", technique.getName());
                //System.out.println("Serialized: " + technique.getName());
                ListTag skillsTag = new ListTag();
                for (Skill skill : technique.getSkillSet()) {
                    if (currentSkill.equals(skill)) {
                        CompoundTag cTag = skill.save();
                        cTag.putBoolean("current_skill", true);
                        skillsTag.add(cTag);
                        //System.out.println("Serialized: " + skill.getName());
                    }
                    else skillsTag.add(skill.save());
                }
                tag.put("skills", skillsTag);
            }
            return tag;
        }

        @Override
        public CompoundTag serializeNbtToNetwork() {
            CompoundTag tag = new CompoundTag();
            if (technique != null) {
                tag.putString("technique_name", technique.getName());
                //System.out.println("Serialized: " + technique.getName());
                ListTag skillsTag = new ListTag();
                for (Skill skill : technique.getSkillSet()) {
                    if (currentSkill.equals(skill)) {
                        CompoundTag cTag = skill.save();
                        cTag.putBoolean("current_skill", true);
                        skillsTag.add(cTag);
                        //System.out.println("Serialized: " + skill.getName());
                    }
                    else skillsTag.add(skill.save());
                }
                tag.put("skills", skillsTag);
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (!tag.contains("technique_name")) {
                setTechnique(new LimitlessCursedTechnique());
                return;
            }

            String techniqueName = tag.getString("technique_name");
            CursedTechnique newTechnique = Provider.createTechniqueByName(techniqueName);
            setTechnique(newTechnique); // ← заменили технику

            if (tag.contains("skills")) {
                ListTag skillsTag = tag.getList("skills", Tag.TAG_COMPOUND);
                List<Skill> currentSkills = newTechnique.getSkillSet();

                for (int i = 0; i < skillsTag.size(); i++) {
                    CompoundTag skillTag = skillsTag.getCompound(i);
                    String skillName = skillTag.getString("skill_name");

                    // Находим скилл с таким же именем в текущей технике
                    Skill targetSkill = currentSkills.stream()
                            .filter(s -> s.getName().equals(skillName))
                            .findFirst()
                            .orElse(null);

                    if (targetSkill != null) {
                        targetSkill.load(skillTag); // ← ВОССТАНАВЛИВАЕМ СОСТОЯНИЕ В СУЩЕСТВУЮЩИЙ СКИЛЛ!

                        if (skillTag.contains("current_skill") && skillTag.getBoolean("current_skill")) {
                            setCurrentSkill(targetSkill);
                        }
                    }
                }
            }
        }
    }

    public static class Provider implements ICapabilitySerializable<CompoundTag> {
        private final ICursedTechnique techniqueHolder = new TechniqueHolder();
        private final LazyOptional<ICursedTechnique> holder = LazyOptional.of(() -> techniqueHolder);

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
            return capability == CURSED_TECHNIQUE ? holder.cast() : LazyOptional.empty();
        }

        public static CursedTechnique createTechniqueByName(String name) {
            return switch (name) {
                case "limitless" -> new LimitlessCursedTechnique();
                case "ten_shadows" -> new TenShadowsTechnique();
                default -> new LimitlessCursedTechnique();
            };
        }

        public static Skill createSkillByName(String name) {
            return switch (name) {
                case "infinity" -> new Infinity();
                case "blue" -> new Blue();
                case "red" -> new Red();
                case "hollow_purple" -> new HollowPurple();
                case "unlimited_void" -> new UnlimitedVoid();
                case "divine_dogs" -> new DivineDogs();
                case "nue" -> new Nue();
                case "toad" -> new Toad();
                case "great_serpent" -> new GreatSerpent();
                default -> null; // Оставляем null, так как проверка происходит позже
            };
        }

        @Override
        public CompoundTag serializeNBT() {
            return techniqueHolder.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            techniqueHolder.deserializeNBT(nbt);
        }
    }

    // Утилитные методы
    public static CursedTechnique getCursedTechnique(Player player) {
        return player.getCapability(CURSED_TECHNIQUE)
                .map(ICursedTechnique::getTechnique)
                .orElse(new LimitlessCursedTechnique());
    }

    public static void setCursedTechnique(Player player, String techniqueName) {
        player.getCapability(CURSED_TECHNIQUE).ifPresent(cursedTechnique -> {
            cursedTechnique.setTechnique(Provider.createTechniqueByName(techniqueName));
            CompoundTag nbt = cursedTechnique.serializeNbtToNetwork();
            ModMessages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new TechniqueSyncPacket(nbt));
        });
    }

    public static Skill getCurrentSkill(Player player) {
        return player.getCapability(CURSED_TECHNIQUE)
                .map(ICursedTechnique::getCurrentSkill)
                .orElseGet(() -> player.getCapability(CURSED_TECHNIQUE)
                        .map(ICursedTechnique::getFirstSkill)
                        .orElse(null));
    }

    public static void nextSkill(Player player) {
        player.getCapability(CURSED_TECHNIQUE).ifPresent(ICursedTechnique::nextSkill);
    }

    public static void previousSkill(Player player) {
        player.getCapability(CURSED_TECHNIQUE).ifPresent(ICursedTechnique::previousSkill);
    }
}