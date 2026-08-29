package dev.lumareth.moremissiles.item.item;

import dev.lumareth.moremissiles.block.ModBlockTags;
import dev.lumareth.moremissiles.utils.ModDataComponents;
import dev.lumareth.moremissiles.utils.itemstates.ToolkitMode;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RadarToolkitItem extends Item {
    public RadarToolkitItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand)  {
        ItemStack stack = player.getItemInHand(usedHand);
        ToolkitMode currentMode = stack.getOrDefault(ModDataComponents.MODE.get(), ToolkitMode.LINKING);
        ToolkitMode newMode = currentMode.next();
        stack.set(ModDataComponents.MODE.get(), newMode);
        return InteractionResultHolder.success(stack);
    }
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState blockState = level.getBlockState(clickedPos);
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        ToolkitMode currentMode = stack.getOrDefault(ModDataComponents.MODE.get(), ToolkitMode.LINKING);
        BlockEntity be = level.getBlockEntity(clickedPos);

        if (be == null) {
            return InteractionResult.PASS;
        }

        if (currentMode == ToolkitMode.LINKING) {
            if (player != null && !player.isShiftKeyDown()) {
                // COPY step
                stack.set(ModDataComponents.LINKER_POS, clickedPos);
                level.playSound(null, clickedPos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            } else if (player != null && player.isShiftKeyDown()) {
                // WRITE step — use the PREVIOUSLY copied position, not clickedPos
                BlockPos pendingPos = stack.get(ModDataComponents.LINKER_POS.get());
                if (pendingPos == null) {
                    return InteractionResult.PASS; // nothing copied yet
                }
                if (blockState.is(ModBlockTags.LINKABLE)) {
                    be.getPersistentData().putDouble("ControlX", pendingPos.getX());
                    be.getPersistentData().putDouble("ControlY", pendingPos.getY());
                    be.getPersistentData().putDouble("ControlZ", pendingPos.getZ());
                } else {
                    be.getPersistentData().putDouble("RadarX", pendingPos.getX());
                    be.getPersistentData().putDouble("RadarY", pendingPos.getY());
                    be.getPersistentData().putDouble("RadarZ", pendingPos.getZ());
                }
                level.playSound(null, clickedPos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
        } else {
            // CONFIGURING mode — unchanged from your version, just returning SUCCESS instead of falling to PASS
            if (blockState.is(ModBlockTags.LINKABLE)) {
                double channel = be.getPersistentData().getDouble("Channel");
                channel++;
                be.getPersistentData().putDouble("Channel", channel);
                level.playSound(null, clickedPos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

}
