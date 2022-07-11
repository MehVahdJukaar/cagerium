package net.mehvahdjukaar.cagerium.common;


import net.mehvahdjukaar.cagerium.client.CageItemRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.network.chat.Component;
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
        registerISTER(consumer, CageItemRenderer::new);
    }

    public static void registerISTER(Consumer<IItemRenderProperties> consumer, BiFunction<BlockEntityRenderDispatcher, EntityModelSet, BlockEntityWithoutLevelRenderer> factory) {
        consumer.accept(new IItemRenderProperties() {
            final NonNullLazy<BlockEntityWithoutLevelRenderer> renderer = NonNullLazy.of(
                    () -> factory.apply(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                            Minecraft.getInstance().getEntityModels()));

            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                return renderer.get();
            }
        });
    }

}
