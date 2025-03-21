package com.aleksiyflekssiy.tutorialmod.item.custom;

import com.aleksiyflekssiy.tutorialmod.util.ModTags;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MetalDetectorItem extends Item {
    public MetalDetectorItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        performDash(pPlayer);
        return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        if(!pContext.getLevel().isClientSide()) {
            BlockPos pos = pContext.getClickedPos();
            Player player = pContext.getPlayer();
            boolean foundBlock = false;

            for (int i = 0; i <= pos.getY() + 64; i++) {
                BlockState state = pContext.getLevel().getBlockState(pos.below(i));
                if (isValuableBlock(state)){
                    outputValueableCoordinates(pos.below(i), player, state.getBlock());
                    foundBlock = true;
                    break;
                }
            }
            if (!foundBlock) {
                player.sendSystemMessage(Component.literal("No valuable block found!"));
            }
        }
        pContext.getItemInHand().hurtAndBreak(1, pContext.getPlayer(), player -> player.broadcastBreakEvent(player.getUsedItemHand()));
        return InteractionResult.SUCCESS;
    }

    private void outputValueableCoordinates(BlockPos blockPos, Player player, Block block) {
        player.sendSystemMessage(Component.literal("Found " + I18n.get(block.getDescriptionId()) + " at position " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ()));
    }

    private boolean isValuableBlock(BlockState state) {
        return state.is(ModTags.Blocks.METAL_DETECTOR_VALUABLES);
    }
    private void performDash(Player player) {
        double motionStrength = 15; // Сила рывка (можешь настроить)
        double speed = motionStrength * 0.1; // Коэффициент скорости

        // Получаем направление взгляда игрока
        var lookVector = player.getLookAngle();

        // Применяем импульс к игроку
        player.push(lookVector.x * speed, lookVector.y * speed, lookVector.z * speed);

        // Добавляем эффект (например, звук или частицы)
        player.playSound(net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, 1.0F);
    }
}
