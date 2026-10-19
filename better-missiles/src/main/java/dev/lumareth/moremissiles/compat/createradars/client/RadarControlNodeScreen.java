package dev.lumareth.moremissiles.compat.createradars.client;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import dev.lumareth.moremissiles.WariumBetterMissiles;
import dev.lumareth.moremissiles.compat.createradars.menu.RadarControlNodeMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class RadarControlNodeScreen extends AbstractSimiContainerScreen<RadarControlNodeMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(WariumBetterMissiles.MOD_ID, "textures/gui/radar_control_node.png");
    private static final int BACKGROUND_WIDTH = 177;
    private static final int BUTTON_Y = 121;
    private static final int TEXT_COLOR = 0xFFFFFF;
    // Row labels in the same order as the menu rows
    private static final String[] ROW_KEYS = {"next_target", "previous_target", "fire", "channel_up", "channel_down"};

    public RadarControlNodeScreen(RadarControlNodeMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        setWindowSize(BACKGROUND_WIDTH, RadarControlNodeMenu.PLAYER_INVENTORY_Y + 108);
        super.init();

        IconButton resetButton = new IconButton(leftPos + RadarControlNodeMenu.FIRST_SLOT_X - 1, topPos + BUTTON_Y, AllIcons.I_REFRESH);
        resetButton.setToolTip(Component.translatable("gui.more_missiles.rcn.reset"));
        resetButton.withCallback(() -> {
            menu.resetToDefaults();
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, RadarControlNodeMenu.RESET_BUTTON_ID);
        });

        IconButton doneButton = new IconButton(leftPos + RadarControlNodeMenu.LAST_SLOT_X - 1, topPos + BUTTON_Y, AllIcons.I_CONFIRM);
        doneButton.setToolTip(Component.translatable("gui.more_missiles.rcn.done"));
        doneButton.withCallback(() -> minecraft.player.closeContainer());

        addRenderableWidgets(resetButton, doneButton);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        renderPlayerInventory(graphics, leftPos, topPos + RadarControlNodeMenu.PLAYER_INVENTORY_Y);
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, BACKGROUND_WIDTH, RadarControlNodeMenu.BACKGROUND_HEIGHT);
        graphics.drawString(font, title, leftPos + 8, topPos + 6, TEXT_COLOR, false);
        for (int row = 0; row < ROW_KEYS.length; row++) {
            graphics.drawString(font, Component.translatable("gui.more_missiles.rcn." + ROW_KEYS[row]),
                    leftPos + 8, topPos + RadarControlNodeMenu.ROW_Y[row] + 5, TEXT_COLOR, false);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int x, int y) {
        // Frequency slots: explain which half of the pair they are, like Create's Linked Controller
        if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.container != menu.playerInventory) {
            List<Component> lines = new ArrayList<>();
            if (hoveredSlot.hasItem()) {
                lines.addAll(getTooltipFromContainerItem(hoveredSlot.getItem()));
            }
            boolean first = hoveredSlot.getSlotIndex() % 2 == 0;
            lines.add(Component.translatable(first ? "gui.more_missiles.rcn.first_frequency" : "gui.more_missiles.rcn.second_frequency")
                    .withStyle(ChatFormatting.GOLD));
            graphics.renderComponentTooltip(font, lines, x, y);
            return;
        }
        super.renderTooltip(graphics, x, y);
    }
}
