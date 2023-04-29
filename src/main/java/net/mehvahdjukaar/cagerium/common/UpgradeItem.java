package net.mehvahdjukaar.cagerium.common;

import net.mehvahdjukaar.cagerium.CageriumClient;
import net.mehvahdjukaar.cagerium.client.UpgradeItemRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class UpgradeItem extends Item {
    public UpgradeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        var tag = pStack.getTag();
        boolean hasMob = false;
        if (tag != null) {
            var s = new ResourceLocation(tag.getString("EntityType"));
            var e = Registry.ENTITY_TYPE.getOptional(s);
            if (e.isPresent()) {
                hasMob = true;
                pTooltipComponents.add(e.get().getDescription());
            }
        }
        if (!hasMob) {
            pTooltipComponents.add(Component.translatable("tooltip.cagerium.invalid").withStyle(ChatFormatting.GRAY));
        }
    }


    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        CageriumClient.registerISTER(consumer, UpgradeItemRenderer::new);
    }
}
