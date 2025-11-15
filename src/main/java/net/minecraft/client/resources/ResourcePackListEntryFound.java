package net.minecraft.client.resources;

import cn.langya.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreenResourcePacks;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ResourcePackListEntryFound extends ResourcePackListEntry {

    private final GuiScreenResourcePacks parent;
    private final ResourcePackRepository.Entry entry;
    private final Minecraft mc = Minecraft.getMinecraft();

    private ResourceLocation iconLocation;

    public ResourcePackListEntryFound(GuiScreenResourcePacks parentIn, ResourcePackRepository.Entry entryIn) {
        super(parentIn);
        this.parent = parentIn;
        this.entry = entryIn;
        try {
            loadIcon();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void func_148313_c()
    {
        this.entry.bindTexturePackIcon(this.mc.getTextureManager());
    }

    protected int func_183019_a()
    {
        return this.entry.func_183027_f();
    }

    protected String func_148311_a()
    {
        return this.entry.getTexturePackDescription();
    }

    protected String func_148312_b()
    {
        return this.entry.getResourcePackName();
    }

    public ResourcePackRepository.Entry func_148318_i()
    {
        return this.entry;
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int entryWidth, int entryHeight,
                          int mouseX, int mouseY, boolean isSelected) {

        // Draw pack icon
        if (iconLocation != null) {
            mc.getTextureManager().bindTexture(iconLocation);
            RenderUtil.INSTANCE.resetColor();
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 32, 32, 32, 32);
        } else {
            drawGreyBox(x, y, 32, 32);
        }

        // Draw pack name
        mc.fontRendererObj.drawString(
                entry.getResourcePackName(),
                x + 40, y + 6, 0xFFFFFF
        );

        // Draw description (1–2 lines)
        String desc = func_148311_a();
        if (desc != null && !desc.isEmpty()) {
            mc.fontRendererObj.drawSplitString(desc, x + 40, y + 18, 150, 0xAAAAAA);
        }

        // Hover detection
        boolean hover = mouseX >= x && mouseX <= x + entryWidth &&
                mouseY >= y && mouseY <= y + entryHeight;

        if (hover) {
            parent.requestPreviewFor(entry);
            Gui.drawRect(x, y, x + entryWidth, y + entryHeight, 0x22000000);
        }
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent,
                                int relativeX, int relativeY) {
        if (parent.getListContaining(this) == parent.getSelectedResourcePacks()) {
            parent.getSelectedResourcePacks().remove(this);
            parent.getAvailableResourcePacks().add(this);
        } else {
            parent.getAvailableResourcePacks().remove(this);
            parent.getSelectedResourcePacks().addFirst(this);
        }

        parent.markChanged();
        return true;
    }


    private void drawGreyBox(int x, int y, int w, int h) {
        Gui.drawRect(x, y, x + w, y + h, 0xFF555555);
        Gui.drawRect(x + 2, y + 2, x + w - 2, y + h - 2, 0xFF333333);
    }

    private void loadIcon() throws IOException {
        if (entry == null) return;
        BufferedImage image = entry.getResourcePack().getPackImage();
        if (image != null) {
            DynamicTexture dt = new DynamicTexture(image);
            iconLocation = new ResourceLocation("pack_icon/" + entry.getResourcePackName().hashCode());
            mc.getTextureManager().loadTexture(iconLocation, dt);
        }
    }
}
