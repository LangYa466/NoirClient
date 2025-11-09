package net.minecraft.client.gui;

import java.io.IOException;

import net.minecraft.client.gui.guimainmenu.GuiMainMenu;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.LocalizationHelper;
import net.minecraft.util.EnumChatFormatting;

public class GuiGameOver extends GuiScreen implements GuiYesNoCallback
{
    private int enableButtonsTimer;
    private final boolean field_146346_f = false;

    public void initGui()
    {
        buttonList.clear();

        if (this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled())
        {
            if (this.mc.isIntegratedServerRunning())
            {
                buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, LocalizationHelper.translate("deathScreen.deleteWorld")));
            }
            else
            {
                buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, LocalizationHelper.translate("deathScreen.leaveServer")));
            }
        }
        else
        {
            buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 72, LocalizationHelper.translate("deathScreen.respawn")));
            buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, LocalizationHelper.translate("deathScreen.titleScreen")));

            if (this.mc.getSession() == null)
            {
                buttonList.get(1).enabled = false;
            }
        }

        for (GuiButton guibutton : buttonList)
        {
            guibutton.enabled = false;
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        switch (button.id)
        {
            case 0:
                this.mc.thePlayer.respawnPlayer();
                this.mc.displayGuiScreen(null);
                break;

            case 1:
                if (this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled())
                {
                    this.mc.displayGuiScreen(new GuiMainMenu());
                }
                else
                {
                    GuiYesNo guiyesno = new GuiYesNo(this, LocalizationHelper.translate("deathScreen.quit.confirm"), "", LocalizationHelper.translate("deathScreen.titleScreen"), LocalizationHelper.translate("deathScreen.respawn"), 0);
                    this.mc.displayGuiScreen(guiyesno);
                    guiyesno.setButtonDelay(20);
                }
        }
    }

    public void confirmClicked(boolean result, int id)
    {
        if (result)
        {
            this.mc.theWorld.sendQuittingDisconnectingPacket();
            this.mc.loadWorld(null);
            this.mc.displayGuiScreen(new GuiMainMenu());
        }
        else
        {
            this.mc.thePlayer.respawnPlayer();
            this.mc.displayGuiScreen(null);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawGradientRect(0, 0, this.width, this.height, 1615855616, -1602211792);
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        boolean flag = this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled();
        String s = flag ? LocalizationHelper.translate("deathScreen.title.hardcore") : LocalizationHelper.translate("deathScreen.title");
        this.drawCenteredString(this.fontRendererObject, s, this.width / 2 / 2, 30, 16777215);
        GlStateManager.popMatrix();

        if (flag)
        {
            this.drawCenteredString(this.fontRendererObject, LocalizationHelper.translate("deathScreen.hardcoreInfo"), this.width / 2, 144, 16777215);
        }

        this.drawCenteredString(this.fontRendererObject, LocalizationHelper.translate("deathScreen.score") + ": " + EnumChatFormatting.YELLOW + this.mc.thePlayer.getScore(), this.width / 2, 100, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public boolean doesGuiPauseGame()
    {
        return false;
    }

    public void updateScreen()
    {
        super.updateScreen();
        ++this.enableButtonsTimer;

        if (this.enableButtonsTimer == 20)
        {
            for (GuiButton guibutton : buttonList)
            {
                guibutton.enabled = true;
            }
        }
    }
}
