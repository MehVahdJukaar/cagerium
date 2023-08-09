package net.mehvahdjukaar.cagerium.common;


import com.mojang.blaze3d.platform.InputConstants;
import net.mehvahdjukaar.cagerium.CageriumClient;
import net.mehvahdjukaar.cagerium.client.CageItemRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.util.NonNullLazy;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class CageItem extends BlockItem {


    public CageItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        var c = pStack.getTagElement("BlockEntityTag");
        if( isKeyDown(Minecraft.getInstance().options.keyShift)){
            pTooltip.add(new TextComponent("aaaa"));
        }

        if (c != null) {
            boolean burning = c.getBoolean("Burning");
            byte upgrade = c.getByte("UpgradeLevel");
            if (burning)
                pTooltip.add(new TranslatableComponent("tooltip.cagerium.burning").withStyle(ChatFormatting.GRAY));

            if (upgrade != 0)
                pTooltip.add(new TranslatableComponent("tooltip.cagerium.upgrade", upgrade).withStyle(ChatFormatting.GRAY));

        }
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
    }

    @Override
    public void onCraftedBy(ItemStack pStack, Level pLevel, Player pPlayer) {
        super.onCraftedBy(pStack, pLevel, pPlayer);
    }


    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        CageriumClient.registerISTER(consumer, CageItemRenderer::new);
    }

    private static boolean isKeyDown(KeyMapping keyBinding) {
        InputConstants.Key key = keyBinding.getKey();
        int keyCode = key.getValue();
        if (keyCode != InputConstants.UNKNOWN.getValue()) {
            long windowHandle = Minecraft.getInstance().getWindow().getWindow();
            try {
                if (key.getType() == InputConstants.Type.KEYSYM) {
                    return InputConstants.isKeyDown(windowHandle, keyCode);
                } else if (key.getType() == InputConstants.Type.MOUSE) {
                    return GLFW.glfwGetMouseButton(windowHandle, keyCode) == GLFW.GLFW_PRESS;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }
}