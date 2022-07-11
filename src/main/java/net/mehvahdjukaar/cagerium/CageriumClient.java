package net.mehvahdjukaar.cagerium;

import net.mehvahdjukaar.cagerium.client.CageBlockTileRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

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


}
