package net.mehvahdjukaar.moonlight.api.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Calendar;

public class LinkButton extends Button {

    public static final ResourceLocation MISC_ICONS = Moonlight.res("textures/gui/misc_icons.png");

    private final Component label;
    private final int u;
    private final int v;
    private final ResourceLocation texture;
    private final int iconW;
    private final int iconH;
    private final int textureW;
    private final int textureH;

    public static LinkButton create(ResourceLocation texture,
                                    Screen parent, int x, int y, int uInd, int vInd, String url, String tooltip) {
        return create(texture, 64, 64, 14, 14, parent, x, y, uInd, vInd, url, tooltip);
    }

    public static LinkButton create(ResourceLocation texture, int textureW, int textureH, int iconW, int iconH,
                                    Screen parent, int x, int y, int uInd, int vInd, String url, String tooltip) {

        String finalUrl = getLink(url);
        OnPress onPress = (op) -> {
            Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, finalUrl));
            parent.handleComponentClicked(style);
        };

        var button = new LinkButton(texture, textureW, textureH, iconW, iconH, x, y, uInd * iconW, vInd * iconH,
                iconW + 6, iconH + 6, CommonComponents.EMPTY, onPress);
        button.setTooltip(Tooltip.create(Component.literal(tooltip)));
        return button;
    }

    //TODO: use builder
    public LinkButton(ResourceLocation texture, int textureW, int textureH, int iconW, int iconH,
                      int x, int y, int u, int v, int width, int height, Component label, OnPress onPress) {
        super(x, y, width, height, CommonComponents.EMPTY, onPress, (c) -> Component.empty());
        this.label = label;
        this.u = u;
        this.v = v;
        this.texture = texture;
        this.iconW = iconW;
        this.iconH = iconH;
        this.textureW = textureW;
        this.textureH = textureH;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(poseStack, mouseX, mouseY, partialTicks);
        Minecraft mc = Minecraft.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        int contentWidth = iconW + mc.font.width(this.label);
        int iconX = (int) (this.getX() + Math.ceil((this.width - contentWidth) / 2f));
        int iconY = (int) (this.getY() + Math.ceil((this.width - iconH) / 2f));
        float brightness = this.active ? 1.0F : 0.5F;
        RenderSystem.setShaderColor(brightness, brightness, brightness, this.alpha);
        blit(poseStack, iconX, iconY, this.getBlitOffset(), this.u, this.v, iconW, iconW, textureH, textureW);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int textColor = this.getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24;
        drawString(poseStack, mc.font, this.label, iconX + 14, iconY + 1, textColor);
    }

    public int getFGColor() {
        return this.active ? 16777215 : 10526880;
    }

    private static String getLink(String original) {
        return LOL ? "https://www.youtube.com/watch?v=dQw4w9WgXcQ" : original;
    }

    private static final boolean LOL;

    static {
        Calendar calendar = Calendar.getInstance();
        LOL = calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DATE) == 1;
    }
}
