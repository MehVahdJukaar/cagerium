package net.mehvahdjukaar.cagerium.client;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.mehvahdjukaar.cagerium.CageriumClient;
import net.mehvahdjukaar.cagerium.client.texture_renderer.RenderedTexturesManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.EmptyModelData;


public class UpgradeItemRenderer extends BlockEntityWithoutLevelRenderer {

    private final BlockRenderDispatcher blockRenderer;

    public UpgradeItemRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
        super(pBlockEntityRenderDispatcher, pEntityModelSet);
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }


    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType,
                             PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        //render block
        poseStack.pushPose();
        var tag = stack.getTag();

        poseStack.pushPose();
        //RenderedTexturesManager.aaa();

        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(), buffer.getBuffer(Sheets.cutoutBlockSheet()), null,
                blockRenderer.getBlockModelShaper().getModelManager().getModel(CageriumClient.UPGRADE_BASE),
                1.0F, 1.0F, 1.0F,
                light, overlay, EmptyModelData.INSTANCE);

        poseStack.popPose();


        poseStack.popPose();
        if (tag != null) {
            //render mob

            ResourceLocation id = new ResourceLocation(tag.getString("EntityType"));

            var dynamicTexture = RenderedTexturesManager.getFlatMobTexture(id, 512, null);
            if (dynamicTexture == null) return;

            ResourceLocation tex = dynamicTexture.getTextureLocation();

            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(tex));

            Matrix4f tr = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();

            float offset = 1 / 30f;
            float z1 = 0.5f + offset;
            float z2 = 0.5f - offset;
            float s = 0.5f;
            poseStack.translate(0.5, 0.5, 0);

            vertexConsumer.vertex(tr, -s, s, z1).color(1f, 1f, 1f, 1f).uv(0f, 1f).overlayCoords(overlay).uv2(light).normal(normal, 0f, 0f, 1f).endVertex();
            vertexConsumer.vertex(tr, -s, -s, z1).color(1f, 1f, 1f, 1f).uv(0f, 0f).overlayCoords(overlay).uv2(light).normal(normal, 0f, 0f, 1f).endVertex();
            vertexConsumer.vertex(tr, s, -s, z1).color(1f, 1f, 1f, 1f).uv(1f, 0f).overlayCoords(overlay).uv2(light).normal(normal, 0f, 0f, 1f).endVertex();
            vertexConsumer.vertex(tr, s, s, z1).color(1f, 1f, 1f, 1f).uv(1f, 1f).overlayCoords(overlay).uv2(light).normal(normal, 0f, 0f, 1f).endVertex();

            vertexConsumer.vertex(tr, -s, s, z2).color(1f, 1f, 1f, 1f).uv(1f, 1f).overlayCoords(overlay).uv2(light).normal(normal, 0f, 0f, 1f).endVertex();
            vertexConsumer.vertex(tr, s, s, z2).color(1f, 1f, 1f, 1f).uv(0f, 1f).overlayCoords(overlay).uv2(light).normal(normal, 0f, 0f, 1f).endVertex();
            vertexConsumer.vertex(tr, s, -s, z2).color(1f, 1f, 1f, 1f).uv(0f, 0f).overlayCoords(overlay).uv2(light).normal(normal, 0f, 0f, 1f).endVertex();
            vertexConsumer.vertex(tr, -s, -s, z2).color(1f, 1f, 1f, 1f).uv(1f, 0f).overlayCoords(overlay).uv2(light).normal(normal, 0f, 0f, 1f).endVertex();


        }

    }
}

