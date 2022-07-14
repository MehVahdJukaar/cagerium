package net.mehvahdjukaar.cagerium.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.cagerium.common.CageriumBlockTile;
import net.mehvahdjukaar.cagerium.common.MobData;
import net.mehvahdjukaar.cagerium.common.Tier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.function.Function;


public class CageBlockTileRenderer<T extends CageriumBlockTile> implements BlockEntityRenderer<T> {
    private final EntityRenderDispatcher entityRenderer;
    private final BlockRenderDispatcher blockRenderer;

    public CageBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher();
        blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public int getViewDistance() {
        return 80;
    }

    @Override
    public void render(T tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        if (!tile.isEmpty()) {
            renderMobs(tile::getRenderData, tile.getTier(),
                    tile.getUpgradeLevel(), partialTicks, poseStack, bufferIn, combinedLightIn, entityRenderer, tile.getDirection());
        }
        var ground = tile.getHabitat();
        if (ground != null) {
            poseStack.pushPose();
            poseStack.translate(2 / 16f, tile.getTier().getHeight() - 1 / 16f + 0.005, 2 / 16f);
            poseStack.scale(12 / 16f, 0.125f, 12 / 16f);
            renderBlockState(ground, poseStack, bufferIn, blockRenderer, tile.getLevel(), tile.getBlockPos());
            poseStack.popPose();
        }

    }

    private static void renderMob(float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn,
                                  int combinedLightIn, Entity entity, float scale, EntityRenderDispatcher renderDispatcher) {
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);

        renderDispatcher.setRenderShadow(false);
        renderDispatcher.render(entity, 0, 0, 0, 0.0F, 0, poseStack, bufferIn, combinedLightIn);
        renderDispatcher.setRenderShadow(true);
        poseStack.popPose();
    }

    public static void renderMobs(Function<Integer, MobData> dataGetter, Tier tier,
                                  int level, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn,
                                  EntityRenderDispatcher renderDispatcher, Direction direction) {

        poseStack.pushPose();

        var data0 = dataGetter.apply(0);

        float s = data0.getScale(level);
        float y = data0.getYOffset(level);

        poseStack.translate(0.5, y + tier.getHeight(), 0.5);


        Entity entity0 = data0.getEntity();

        poseStack.mulPose(Vector3f.YN.rotationDegrees(direction.toYRot() + ((entity0 instanceof EnderDragon) ? 180 : 0)));
        float d = 0.2325f;
        switch (level) {
            default -> renderMob(partialTicks, poseStack, bufferIn, combinedLightIn, entity0, s, renderDispatcher);
            case 1 -> {
                poseStack.translate(d, 0, 0);
                renderMob(partialTicks, poseStack, bufferIn, combinedLightIn, entity0, s, renderDispatcher);
                poseStack.translate(-2 * d, 0, 0);
                var data1 = dataGetter.apply(1);
                renderMob(partialTicks, poseStack, bufferIn, combinedLightIn, data1.getEntity(), s, renderDispatcher);
            }
            case 2 -> {
                poseStack.translate(0, 0, d);
                renderMob(partialTicks, poseStack, bufferIn, combinedLightIn, entity0, s, renderDispatcher);
                poseStack.translate(-0.866 * d, 0, -1.5 * d);
                var data1 = dataGetter.apply(1);
                renderMob(partialTicks, poseStack, bufferIn, combinedLightIn, data1.getEntity(), s, renderDispatcher);
                poseStack.translate(2 * 0.866 * d, 0, 0);
                var data2 = dataGetter.apply(1);
                renderMob(partialTicks, poseStack, bufferIn, combinedLightIn, data2.getEntity(), s, renderDispatcher);
            }
            case 3 -> {
                poseStack.translate(d, 0, d);
                renderMob(partialTicks, poseStack, bufferIn, combinedLightIn, entity0, s, renderDispatcher);
                poseStack.translate(-2 * d, 0, 0);
                var data1 = dataGetter.apply(1);
                renderMob(partialTicks, poseStack, bufferIn, combinedLightIn, data1.getEntity(), s, renderDispatcher);
                poseStack.translate(0, 0, -2 * d);
                var data2 = dataGetter.apply(1);
                renderMob(partialTicks, poseStack, bufferIn, combinedLightIn, data2.getEntity(), s, renderDispatcher);
                poseStack.translate(2 * d, 0, 0);
                var data3 = dataGetter.apply(1);
                renderMob(partialTicks, poseStack, bufferIn, combinedLightIn, data3.getEntity(), s, renderDispatcher);
            }
        }
        poseStack.popPose();

    }

    public static void renderBlockState(BlockState state, PoseStack matrixStack, MultiBufferSource buffer,
                                        BlockRenderDispatcher blockRenderer, Level world, BlockPos pos) {
        try {
            for (RenderType type : RenderType.chunkBufferLayers()) {
                if (ItemBlockRenderTypes.canRenderInLayer(state, type)) {
                    renderBlockState(state, matrixStack, buffer, blockRenderer, world, pos, type);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void renderBlockState(BlockState state, PoseStack matrixStack, MultiBufferSource buffer,
                                        BlockRenderDispatcher blockRenderer, Level world, BlockPos pos, RenderType type) {

        ForgeHooksClient.setRenderType(type);
        blockRenderer.getModelRenderer().tesselateBlock(world,
                blockRenderer.getBlockModel(state), state, pos, matrixStack,
                buffer.getBuffer(type), false, world.random, 0,
                OverlayTexture.NO_OVERLAY);
        ForgeHooksClient.setRenderType(null);
    }

}

