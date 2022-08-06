package net.mehvahdjukaar.cagerium.delete;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;


public class MagnetTableScreen extends AbstractContainerScreen<MagnetTableContainerMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("cagerium", "textures/ui.png");

    public MagnetTableScreen(MagnetTableContainerMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY += 56;
    }

    @Override
    public void init() {
        super.init();
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        this.addRenderableWidget(new PackButton(i + 10, j + 19));
        this.addRenderableWidget(new PackButton(i + 10, j + 19 + 22));
        this.addRenderableWidget(new PackButton(i + 10, j + 19 + 44));
        this.addRenderableWidget(new PackButton(i + 10, j + 19 + 66));
        this.addRenderableWidget(new PackButton(i + 10, j + 19 + 88));
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int x, int y) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        this.blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);

        var stack = this.menu.slots.get(0).getItem();
        int s = Math.round(36.0F - (float) stack.getDamageValue() * 36.0F / (float) stack.getMaxDamage());

        this.blit(poseStack, k + 42, l + 63, 0, 222, s, 3);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);


    }

    @Override
    public boolean keyPressed(int key, int b, int c) {
        if (key == 256) {
            this.minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(key, b, c);
    }

    @Override
    public void removed() {
        super.removed();
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
    }


    public class PackButton extends AbstractButton {
        private boolean packed;

        protected PackButton(int x, int y) {
            super(x, y, 18, 18, TextComponent.EMPTY);
        }

        @Override
        public void renderButton(PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTick) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int v = 0;
            int u = 176;
            if (!this.active) {
                u += this.width;
            } else if (this.isHovered) {
                u += this.width * 2;
            }

            this.blit(poseStack, this.x, this.y, u, v, this.width, this.height);
        }

        public void setState(boolean hasItem, boolean packed) {
            this.packed = packed;
            this.active = hasItem;
        }

        @Override
        public void renderToolTip(PoseStack matrixStack, int x, int y) {

        }

        @Override
        public void onPress() {

        }

        @Override
        public void updateNarration(NarrationElementOutput output) {
        }
    }
}