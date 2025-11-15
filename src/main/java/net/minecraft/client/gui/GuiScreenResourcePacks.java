package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.CommonResourceElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.Sys;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class GuiScreenResourcePacks extends GuiScreen {
    private static final Logger logger = LogManager.getLogger();

    private final GuiScreen parentScreen;
    private List<ResourcePackListEntry> availableResourcePacks;
    private List<ResourcePackListEntry> selectedResourcePacks;
    private GuiResourcePackAvailable availableResourcePacksList;
    private GuiResourcePackSelected selectedResourcePacksList;
    private boolean changed = false;

    private volatile ResourceLocation previewWool;
    private volatile ResourceLocation previewPlanks;
    private volatile ResourceLocation previewSword;

    // Manager to handle async reading + main-thread texture creation/unloading
    private final PreviewManager previewManager;

    public GuiScreenResourcePacks(GuiScreen parentScreenIn) {
        this.parentScreen = parentScreenIn;
        this.previewManager = new PreviewManager(Minecraft.getMinecraft().getTextureManager(), Minecraft.getMinecraft());
    }

    @Override
    public void initGui() {
        buttonList.add(new GuiOptionButton(2, this.width / 2 - 154, this.height - 48, LocalizationHelper.translate("resourcePack.openFolder")));
        buttonList.add(new GuiOptionButton(1, this.width / 2 + 4, this.height - 48, LocalizationHelper.translate("gui.done")));

        if (!this.changed) {
            this.availableResourcePacks = Lists.newArrayList();
            this.selectedResourcePacks = Lists.newArrayList();
            ResourcePackRepository resourcepackrepository = CommonResourceElement.Companion.getResourcePackRepository();

            CompletableFuture.runAsync(resourcepackrepository::updateRepositoryEntriesAll);

            List<ResourcePackRepository.Entry> list = Lists.newArrayList(resourcepackrepository.getRepositoryEntriesAll());
            list.removeAll(resourcepackrepository.getRepositoryEntries());

                for (ResourcePackRepository.Entry entry : list) {
                    this.availableResourcePacks.add(new ResourcePackListEntryFound(this, entry));
                }

                for (ResourcePackRepository.Entry entry : Lists.reverse(resourcepackrepository.getRepositoryEntries())) {
                    this.selectedResourcePacks.add(new ResourcePackListEntryFound(this, entry));
                }

            this.selectedResourcePacks.add(new ResourcePackListEntryDefault(this));
        }

        this.availableResourcePacksList = new GuiResourcePackAvailable(this.mc, 200, this.height, this.availableResourcePacks);
        this.availableResourcePacksList.setSlotXBoundsFromLeft(this.width / 2 - 4 - 200);
        this.availableResourcePacksList.registerScrollButtons(7, 8);

        this.selectedResourcePacksList = new GuiResourcePackSelected(this.mc, 200, this.height, this.selectedResourcePacks);
        this.selectedResourcePacksList.setSlotXBoundsFromLeft(this.width / 2 + 4);
        this.selectedResourcePacksList.registerScrollButtons(7, 8);
    }

    @Override
    public void handleMouseInput() throws java.io.IOException {
        super.handleMouseInput();
        this.selectedResourcePacksList.handleMouseInput();
        this.availableResourcePacksList.handleMouseInput();
    }

    public boolean hasResourcePackEntry(ResourcePackListEntry entry) {
        return this.selectedResourcePacks.contains(entry);
    }

    public List<ResourcePackListEntry> getListContaining(ResourcePackListEntry entry) {
        return this.hasResourcePackEntry(entry) ? this.selectedResourcePacks : this.availableResourcePacks;
    }

    public List<ResourcePackListEntry> getAvailableResourcePacks() {
        return this.availableResourcePacks;
    }

    public List<ResourcePackListEntry> getSelectedResourcePacks() {
        return this.selectedResourcePacks;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws java.io.IOException {
        if (button.enabled) {
            if (button.id == 2) {
                File file1 = CommonResourceElement.Companion.getResourcePackRepository().getDirResourcepacks();
                String path = file1.getAbsolutePath();

                try {
                    Util.EnumOS os = Util.getOSType();

                    if (os == Util.EnumOS.OSX) {
                        Runtime.getRuntime().exec(new String[]{"/usr/bin/open", path});
                        return;
                    }

                    if (os == Util.EnumOS.WINDOWS) {
                        new ProcessBuilder(
                                "cmd.exe",
                                "/C",
                                "start",
                                "\"OpenFolder\"",
                                path
                        ).start();
                        return;
                    }

                    Runtime.getRuntime().exec(new String[]{"xdg-open", path});
                    return;

                } catch (Exception e) {
                    logger.error("Couldn't open folder directly, fallback to Desktop API", e);
                }

                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(file1);
                    } else {
                        throw new UnsupportedOperationException("Desktop unsupported");
                    }
                } catch (Throwable t) {
                    logger.error("Desktop API failed, fallback to Sys.openURL", t);
                    Sys.openURL("file://" + path);
                }

            } else if (button.id == 1) {
                if (this.changed) {
                    List<ResourcePackRepository.Entry> list = Lists.newArrayList();

                    for (ResourcePackListEntry entry : this.selectedResourcePacks) {
                        if (entry instanceof net.minecraft.client.resources.ResourcePackListEntryFound) {
                            list.add(((net.minecraft.client.resources.ResourcePackListEntryFound) entry).func_148318_i());
                        }
                    }

                    Collections.reverse(list);
                    CommonResourceElement.Companion.getResourcePackRepository().setRepositories(list);
                    this.mc.gameSettings.resourcePacks.clear();
                    this.mc.gameSettings.incompatibleResourcePacks.clear();

                    for (ResourcePackRepository.Entry entry : list) {
                        this.mc.gameSettings.resourcePacks.add(entry.getResourcePackName());

                        if (entry.func_183027_f() != 1) {
                            this.mc.gameSettings.incompatibleResourcePacks.add(entry.getResourcePackName());
                        }
                    }

                    this.mc.gameSettings.saveOptions();
                    this.mc.refreshResources();
                }

                this.mc.displayGuiScreen(this.parentScreen);
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws java.io.IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.availableResourcePacksList.mouseClicked(mouseX, mouseY, mouseButton);
        this.selectedResourcePacksList.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawBackground(0);
        this.availableResourcePacksList.drawScreen(mouseX, mouseY, partialTicks);
        this.selectedResourcePacksList.drawScreen(mouseX, mouseY, partialTicks);

        // Title & folder info
        this.drawCenteredString(this.mc.fontRendererObj, LocalizationHelper.translate("resourcePack.title"), this.width / 2, 16, 16777215);
        this.drawCenteredString(this.mc.fontRendererObj, LocalizationHelper.translate("resourcePack.folderInfo"), this.width / 2 - 77, this.height - 26, 8421504);

        // Right-side preview area
        drawPreviewArea(mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawPreviewArea(int mouseX, int mouseY) {
        int areaWidth = 150;
        int areaX = this.width - areaWidth - 8;
        int areaY = 40;

        // background box
        drawRect(areaX - 6, areaY - 18, areaX + areaWidth + 6, areaY + 176, 0x66000000);
        this.drawCenteredString(this.mc.fontRendererObj, "材质预览", areaX + areaWidth / 2, areaY - 14, 0xFFFFFF);

        int x = areaX + 8;
        int y = areaY;
        int spacing = 20;

        // wool
        if (previewWool != null) {
            drawTextureAt(previewWool, x, y, 16, 16);
        } else if (previewManager.isLoading("wool")) {
            this.drawString(this.mc.fontRendererObj, "wool: loading...", x + 20, y + 4, 0xAAAAAA);
        } else {
            this.drawString(this.mc.fontRendererObj, "wool: -", x + 20, y + 4, 0xAAAAAA);
        }
        this.drawString(this.mc.fontRendererObj, "羊毛 (白)", x + 36, y + 4, 0xFFFFFF);
        y += spacing;

        // planks
        if (previewPlanks != null) {
            drawTextureAt(previewPlanks, x, y, 16, 16);
        } else if (previewManager.isLoading("planks")) {
            this.drawString(this.mc.fontRendererObj, "planks: loading...", x + 20, y + 4, 0xAAAAAA);
        } else {
            this.drawString(this.mc.fontRendererObj, "planks: -", x + 20, y + 4, 0xAAAAAA);
        }
        this.drawString(this.mc.fontRendererObj, "木板 (橡木)", x + 36, y + 4, 0xFFFFFF);
        y += spacing;

        // sword
        if (previewSword != null) {
            drawTextureAt(previewSword, x, y, 16, 16);
        } else if (previewManager.isLoading("sword")) {
            this.drawString(this.mc.fontRendererObj, "sword: loading...", x + 20, y + 4, 0xAAAAAA);
        } else {
            this.drawString(this.mc.fontRendererObj, "sword: -", x + 20, y + 4, 0xAAAAAA);
        }
        this.drawString(this.mc.fontRendererObj, "钻石剑", x + 36, y + 4, 0xFFFFFF);
    }

    private void drawTextureAt(ResourceLocation loc, int x, int y, int w, int h) {
        if (loc == null) return;
        Minecraft.getMinecraft().getTextureManager().bindTexture(loc);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, w, h, w, h);
    }

    public void requestPreviewFor(ResourcePackRepository.Entry entry) {
        previewManager.loadPreviews(entry).thenAccept(map -> {
            this.previewWool = map.get("wool");
            this.previewPlanks = map.get("planks");
            this.previewSword = map.get("sword");
        }).exceptionally(throwable -> {
            logger.warn("Failed to load preview: " + throwable.getMessage());
            return null;
        });
    }

    public void markChanged() {
        this.changed = true;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        previewManager.cleanup();
    }

    private static class PreviewManager {
        private final TextureManager textureManager;
        private final Minecraft mc;
        private final Map<String, Boolean> loading = new ConcurrentHashMap<>();
        private final Map<String, ResourceLocation> created = new ConcurrentHashMap<>();

        PreviewManager(TextureManager textureManager, Minecraft mc) {
            this.textureManager = textureManager;
            this.mc = mc;
        }

        boolean isLoading(String key) {
            Boolean b = loading.get(key);
            return b != null && b;
        }

        CompletableFuture<Map<String, ResourceLocation>> loadPreviews(ResourcePackRepository.Entry entry) {
            final Map<String, String> paths = new HashMap<>();
            paths.put("wool", "textures/blocks/wool_colored_white.png");
            paths.put("planks", "textures/blocks/planks_oak.png");
            paths.put("sword", "textures/items/diamond_sword.png");

            for (String k : paths.keySet()) loading.put(k, true);

            return CompletableFuture.supplyAsync(() -> {
                Map<String, BufferedImage> images = new HashMap<>();
                IResourcePack pack = getPackFromEntry(entry);

                if (pack != null) {
                    for (Map.Entry<String, String> e : paths.entrySet()) {
                        String key = e.getKey();
                        String path = e.getValue();
                        try (InputStream is = pack.getInputStream(new ResourceLocation(path))) {
                            if (is != null) {
                                BufferedImage img = ImageIO.read(is);
                                if (img != null) images.put(key, img);
                            }
                        } catch (Exception ex) {
                            // ignore?
                        }
                    }
                }

                return images;
            }).thenCompose(images -> {
                // schedule on main thread to register DynamicTexture
                CompletableFuture<Map<String, ResourceLocation>> uiFuture = new CompletableFuture<>();
                mc.addScheduledTask(() -> {
                    try {
                        Map<String, ResourceLocation> result = new HashMap<>();
                        for (String k : paths.keySet()) {
                            BufferedImage img = images.get(k);
                            if (img != null) {
                                DynamicTexture dt = new DynamicTexture(img);
                                ResourceLocation rl = new ResourceLocation("preview/" + UUID.randomUUID().toString());
                                textureManager.loadTexture(rl, dt);
                                created.put(k, rl);
                                result.put(k, rl);
                            } else {
                                result.put(k, null);
                            }
                        }
                        uiFuture.complete(result);
                    } catch (Throwable t) {
                        uiFuture.completeExceptionally(t);
                    } finally {
                        for (String kk : paths.keySet()) loading.put(kk, false);
                    }
                });
                return uiFuture;
            });
        }

        private IResourcePack getPackFromEntry(ResourcePackRepository.Entry entry) {
            return entry.getResourcePack();
        }

        void cleanup() {
            for (ResourceLocation rl : created.values()) {
                textureManager.deleteTexture(rl);
            }
            created.clear();
        }
    }
}
