package net.mehvahdjukaar.cagerium.client;


import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.cagerium.common.CageriumBlock;
import net.mehvahdjukaar.cagerium.common.MobData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.EmptyModel;
import net.minecraftforge.client.model.data.ModelData;


public class CageItemRenderer extends BlockEntityWithoutLevelRenderer {

    public CageItemRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
        super(pBlockEntityRenderDispatcher, pEntityModelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        //render block
        matrixStackIn.pushPose();
        BlockItem item = ((BlockItem) stack.getItem());
        BlockState state = item.getBlock().defaultBlockState();
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, ModelData.EMPTY,
                RenderType.translucent());
        matrixStackIn.popPose();

        CompoundTag tag = BlockItem.getBlockEntityData(stack);
        if (tag != null) {
            //render mob
            var l = Minecraft.getInstance().level;
            ResourceLocation id = new ResourceLocation(tag.getString("EntityType"));
            int i = tag.getByte("UpgradeLevel");

            var data = MobData.getOrCreate(id, l, BlockPos.ZERO);
            if (data != null) {
                CageBlockTileRenderer.renderMobs((h) -> data, ((CageriumBlock) state.getBlock()).getTier(),
                        i, 0, matrixStackIn, bufferIn, combinedLightIn,
                        Minecraft.getInstance().getEntityRenderDispatcher(), Direction.NORTH);
            }
        }

    }
}

