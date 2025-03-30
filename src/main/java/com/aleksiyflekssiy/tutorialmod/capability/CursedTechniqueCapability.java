package com.aleksiyflekssiy.tutorialmod.capability;

import com.aleksiyflekssiy.tutorialmod.cursed_technique.CursedTechnique;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.LimitlessCursedTechnique;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.TenShadowsTechnique;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.*;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
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
            System.out.println("CURRENT TECHNIQUE: "+ this.technique.getName());
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
            if (skill != null && technique.getSkillSet().contains(skill)) {
                this.currentSkill = skill;
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
            System.out.println("Current Skill: " + currentSkill.getName());
            System.out.println("Current Technique: " + technique.getName());
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
    }

    public static class Provider implements ICapabilitySerializable<CompoundTag> {
        private static final ICursedTechnique techniqueHolder = new TechniqueHolder();
        private final LazyOptional<ICursedTechnique> holder = LazyOptional.of(() -> techniqueHolder);

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
            return capability == CURSED_TECHNIQUE ? holder.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag nbt = new CompoundTag();
            if (techniqueHolder.getTechnique() != null) {
                System.out.println("Serialized: " + techniqueHolder.getTechnique().getName());
                nbt.putString("technique_name", techniqueHolder.getTechnique().getName());
                if (techniqueHolder.getCurrentSkill() != null) {
                    nbt.putString("skill_name", techniqueHolder.getCurrentSkill().getName());
                }
            }
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            String techniqueName = nbt.getString("technique_name");
            String skillName = nbt.getString("skill_name");

            if (!techniqueName.isEmpty()) {
                System.out.println("Deserialized: " + techniqueName);
                CursedTechnique technique = createTechniqueByName(techniqueName);
                techniqueHolder.setTechnique(technique); // Устанавливаем технику
                if (!skillName.isEmpty()) {
                    Skill skill = createSkillByName(skillName);
                    if (skill != null && technique.getSkillSet().contains(skill)) {
                        techniqueHolder.setCurrentSkill(skill);
                    } else {
                        techniqueHolder.setCurrentSkill(technique.getFirstSkill()); // Запасной вариант
                    }
                }
            } else {
                // Если данных нет, используем значения по умолчанию
                techniqueHolder.setTechnique(new LimitlessCursedTechnique());
                techniqueHolder.setCurrentSkill(techniqueHolder.getFirstSkill());
            }
        }

        public static CursedTechnique createTechniqueByName(String name) {
            return switch (name) {
                case "Limitless" -> new LimitlessCursedTechnique();
                // Добавь другие техники здесь
                case "TenShadows" -> new TenShadowsTechnique();
                default -> new LimitlessCursedTechnique(); // Запасной вариант вместо null
            };
        }

        private Skill createSkillByName(String name) {
            return switch (name) {
                case "Infinity" -> new Infinity();
                case "Blue" -> new Blue();
                case "Red" -> new Red();
                case "Hollow Purple" -> new HollowPurple();
                case "Unlimited Void" -> new UnlimitedVoid();
                case "Divine Dogs" -> new DivineDogs();
                case "Nue" -> new Nue();
                default -> null; // Оставляем null, так как проверка происходит позже
            };
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
        });
    }

    public static Skill getSkill(Player player) {
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