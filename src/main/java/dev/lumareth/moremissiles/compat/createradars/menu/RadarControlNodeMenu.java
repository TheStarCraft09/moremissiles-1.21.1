package dev.lumareth.moremissiles.compat.createradars.menu;

import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import dev.lumareth.moremissiles.compat.createradars.block.entities.RadarControlNodeBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Frequency configuration for a Radar Control Node: two ghost slots (first, last frequency) per link. Changes are
 * applied to the node when the screen is closed.
 */
public class RadarControlNodeMenu extends GhostItemMenu<RadarControlNodeBlockEntity> {
    public static final int RESET_BUTTON_ID = 0;

    // Positions match textures/gui/radar_control_node.png (slot frame + 1)
    public static final int FIRST_SLOT_X = 117;
    public static final int LAST_SLOT_X = 139;
    public static final int[] ROW_Y = {20, 41, 61, 81, 101};
    public static final int BACKGROUND_HEIGHT = 141;
    public static final int PLAYER_INVENTORY_Y = BACKGROUND_HEIGHT + 4;

    public RadarControlNodeMenu(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    public RadarControlNodeMenu(MenuType<?> type, int id, Inventory inv, RadarControlNodeBlockEntity be) {
        super(type, id, inv, be);
    }

    public static RadarControlNodeMenu create(int id, Inventory inv, RadarControlNodeBlockEntity be) {
        return new RadarControlNodeMenu(RadarsCompatModMenuTypes.RADAR_CONTROL_NODE.get(), id, inv, be);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected RadarControlNodeBlockEntity createOnClient(RegistryFriendlyByteBuf extraData) {
        BlockPos pos = extraData.readBlockPos();
        BlockEntity be = Minecraft.getInstance().level.getBlockEntity(pos);
        return be instanceof RadarControlNodeBlockEntity node ? node : null;
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        ItemStackHandler handler = new ItemStackHandler(RadarControlNodeBlockEntity.LINK_COUNT * 2);
        if (contentHolder != null) {
            fill(handler, contentHolder.getFrequencyItems());
        }
        return handler;
    }

    @Override
    protected void addSlots() {
        addPlayerSlots(8, PLAYER_INVENTORY_Y + 18);
        for (int row = 0; row < RadarControlNodeBlockEntity.LINK_COUNT; row++) {
            int y = ROW_Y[row] + 1;
            addSlot(new SlotItemHandler(ghostInventory, row * 2, FIRST_SLOT_X, y));
            addSlot(new SlotItemHandler(ghostInventory, row * 2 + 1, LAST_SLOT_X, y));
        }
    }

    @Override
    protected boolean allowRepeats() {
        return true;
    }

    public void resetToDefaults() {
        fill(ghostInventory, RadarControlNodeBlockEntity.getDefaultFrequencyItems());
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == RESET_BUTTON_ID) {
            resetToDefaults();
            return true;
        }
        return false;
    }

    @Override
    protected void saveData(RadarControlNodeBlockEntity contentHolder) {
        if (contentHolder == null || player.level().isClientSide()) {
            return;
        }
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < ghostInventory.getSlots(); i++) {
            items.add(ghostInventory.getStackInSlot(i));
        }
        contentHolder.setFrequencyItems(items);
    }

    private static void fill(ItemStackHandler handler, List<ItemStack> items) {
        for (int i = 0; i < handler.getSlots() && i < items.size(); i++) {
            handler.setStackInSlot(i, items.get(i));
        }
    }
}
