package net.mehvahdjukaar.cagerium;

import net.mehvahdjukaar.cagerium.client.CageBlockTileRenderer;
import net.mehvahdjukaar.cagerium.client.texture_renderer.RenderedTexturesManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.function.BiFunction;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = Cagerium.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CageriumClient {

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void init(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {

            ItemBlockRenderTypes.setRenderLayer(Cagerium.CAGE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(Cagerium.TERRARIUM.get(), RenderType.translucent());


        });
    }


    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {

        //tiles
        event.registerBlockEntityRenderer(Cagerium.TILE.get(), CageBlockTileRenderer::new);

    }

    @Mod.EventBusSubscriber(modid = Cagerium.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientEvents {


        @SubscribeEvent
        public static void tick(TickEvent.RenderTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                RenderedTexturesManager.updateTextures();
            }
        }
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        ForgeModelBakery.addSpecialModel(UPGRADE_BASE);
    }

    public static final ResourceLocation UPGRADE_BASE = Cagerium.res("item/upgrade_base");

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
