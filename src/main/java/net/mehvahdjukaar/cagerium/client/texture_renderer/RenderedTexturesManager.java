package net.mehvahdjukaar.cagerium.client.texture_renderer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.cagerium.Cagerium;
import net.mehvahdjukaar.cagerium.common.MobData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RenderedTexturesManager {

    private record RenderingData(
            ResourceLocation id,
            Consumer<FrameBufferBackedDynamicTexture> textureDrawingFunction,
            boolean animated) {
    }

    private static final List<RenderingData> REQUESTED_FOR_RENDERING = new ArrayList<>();

    private static final LoadingCache<ResourceLocation, FrameBufferBackedDynamicTexture> TEXTURE_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .removalListener(i -> {
                FrameBufferBackedDynamicTexture value = (FrameBufferBackedDynamicTexture) i.getValue();
                if (value != null) value.close();
            })
            .build(new CacheLoader<>() {
                @Override
                public FrameBufferBackedDynamicTexture load(ResourceLocation key) {
                    return null;
                }
            });

    public static void aaa() {
        ResourceLocation res = new ResourceLocation("ender_dragon");
        var t = TEXTURE_CACHE.getIfPresent(res);
        Minecraft.getInstance().getTextureManager().register(res, t);
    }

    //clears the texture cache and forge all to be re-rendered
    public static void clearCache() {
        TEXTURE_CACHE.invalidateAll();
    }

    @Nullable
    public static FrameBufferBackedDynamicTexture getFlatMobTexture(ResourceLocation res, int size, @Nullable Consumer<NativeImage> postProcessing) {
        //texture id for item size pair

        FrameBufferBackedDynamicTexture texture = TEXTURE_CACHE.getIfPresent(res);
        // new FrameBufferBackedDynamicTexture(new ResourceLocation(System.currentTimeMillis()+""), 1);
        if (texture == null) {
            texture = new FrameBufferBackedDynamicTexture(res, size);

            TEXTURE_CACHE.put(res, texture);
            //add to queue which will render them next rendering cycle. Returned texture will be blank
            Consumer<FrameBufferBackedDynamicTexture> factory = t -> {

                drawEntity(t, res);

                if (postProcessing != null) {
                    t.download();
                    NativeImage img = t.getPixels();
                    postProcessing.accept(img);
                    t.upload();
                }
            };
            REQUESTED_FOR_RENDERING.add(new RenderingData(res, factory, false));
            return null;
        }
        return texture;
    }

    //called each rendering tick
    public static void updateTextures() {
        ListIterator<RenderingData> iter = REQUESTED_FOR_RENDERING.listIterator();
        while (iter.hasNext()) {
            var data = iter.next();
            var texture = TEXTURE_CACHE.getIfPresent(data.id);
            if (texture != null) {
                if (!texture.isInitialized()) texture.initialize();
                data.textureDrawingFunction.accept(texture);
            }
            if (!data.animated || texture == null) {
                iter.remove();
            }
        }
    }

    public static Matrix4f getProjectionMatrix(double pFov, int size, int renderDistance) {
        PoseStack posestack = new PoseStack();
        posestack.last().pose().setIdentity();
        float zoom = 1;
        float zoomX = 1;
        float zoomY = 1;
        if (zoom != 1.0F) {
            posestack.translate((double) zoomX, (double) (-zoomY), 0.0D);
            posestack.scale(zoom, zoom, 1.0F);
        }

        posestack.last().pose().multiply(Matrix4f.perspective(pFov, (float) size / size, 0.05F, renderDistance * 4f));
        return posestack.last().pose();
    }

    public static void drawEntity(FrameBufferBackedDynamicTexture tex, ResourceLocation id) {

        Minecraft mc = Minecraft.getInstance();
        RenderTarget frameBuffer = tex.getFrameBuffer();
        frameBuffer.clear(Minecraft.ON_OSX);
        //render to this one
        frameBuffer.bindWrite(true);

        int size = 16;
        //gui setup code
        //RenderSystem.clear(256, Minecraft.ON_OSX);
        Matrix4f oldProjection = RenderSystem.getProjectionMatrix();
        Matrix4f matrix4f = Matrix4f.orthographic(0.0F,
                size, 0, size, 1000.0F, 3000); //ForgeHooksClient.getGuiFarPlane()
        RenderSystem.setProjectionMatrix(matrix4f);

        //model view stuff
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.setIdentity();

        posestack.translate(0.0D, 0.0D, 1000F - 3000); //ForgeHooksClient.getGuiFarPlane()
        //apply new model view transformation
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
        //end gui setup code

        //render stuff
        var data = MobData.getOrCreate(id, DummyWorld.INSTANCE, BlockPos.ZERO);
        if (data != null) {
            if(data.getEntity()==null){
                Cagerium.LOGGER.warn("Trying to render null entity for cagerium upgrade. skipping");
                return;
            }
            posestack.pushPose();


            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            PoseStack pose = RenderSystem.getModelViewStack();
            pose.pushPose();
            pose.translate(0, 0, 100.0F + 0);
            pose.translate(8.0D, 8.0D, 0.0D);
            pose.scale(16.0F, -16.0F, 16.0F);
            RenderSystem.applyModelViewMatrix();
            PoseStack modelStack = new PoseStack();

            MultiBufferSource.BufferSource bs = mc.renderBuffers().bufferSource();

            float yRot = 225 + (id.getPath().equals("ender_dragon") ? -180 : 0);

            var tr = new Transformation(Vector3f.ZERO, new Quaternion(30, yRot, 0, true), new Vector3f(0.625f, 0.625f, 0.625f), null);
            tr.push(modelStack);

            float s = data.getScale(0);
            float y = data.getYOffset(0);

            s*=16/12f;

            modelStack.translate(0, y - 0.5D, 0);

            modelStack.mulPose(Vector3f.YN.rotationDegrees(180));

            modelStack.scale(s, s, s);
            Lighting.setupForFlatItems();
            EntityRenderer renderer = mc.getEntityRenderDispatcher().getRenderer(data.getEntity());
            renderer.render(data.getEntity(), 0.0f, 1.0f, modelStack,
                    bs, LightTexture.FULL_BRIGHT);

            bs.endBatch();

            RenderSystem.enableDepthTest();

            posestack.popPose();
            RenderSystem.applyModelViewMatrix();


            posestack.popPose();
        }


        //Minecraft.getInstance().gui.render(posestack,1);


        //reset stuff
        posestack.popPose();
        //reset model view
        RenderSystem.applyModelViewMatrix();

        //reset projection
        RenderSystem.setProjectionMatrix(oldProjection);
        //RenderSystem.clear(256, Minecraft.ON_OSX);
        //returns render calls to main render target
        mc.getMainRenderTarget().bindWrite(true);
    }



    /*
        RenderSystem.setShaderTexture(0,
                new ResourceLocation("textures/gui/container/villager2.png")
       );
        Gui.blit(posestack,0,0,1000,0,0,
                256,256,16,16);
*/
}
