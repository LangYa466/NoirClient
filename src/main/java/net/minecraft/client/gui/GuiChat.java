package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

public class GuiChat extends GuiScreen {
    private String historyBuffer = "";
    private int sentHistoryCursor = -1;

    private final List<String> suggestionList = Lists.newArrayList();
    private boolean isSuggestionVisible = false;
    private int selectedSuggestionIndex = 0;

    private boolean waitingOnAutocomplete;
    protected GuiTextField inputField;
    private String defaultInputFieldText = "";

    public GuiChat()
    {
    }

    public GuiChat(String defaultText)
    {
        this.defaultInputFieldText = defaultText;
    }

    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        this.sentHistoryCursor = this.mc.ingameGUI.getChatGUI().getSentMessages().size();
        this.inputField = new GuiTextField(0, this.fontRendererObject, 4, this.height - 12, this.width - 4, 12);
        this.inputField.setMaxStringLength(100);
        this.inputField.setEnableBackgroundDrawing(false);
        this.inputField.setFocus(true);
        this.inputField.setText(this.defaultInputFieldText);
        this.inputField.setCanLoseFocus(false);
    }

    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
        this.mc.ingameGUI.getChatGUI().resetScroll();
    }

    public void updateScreen()
    {
        this.inputField.updateCursorCounter();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (this.isSuggestionVisible)
        {
            switch (keyCode)
            {
                case Keyboard.KEY_UP:
                    this.selectedSuggestionIndex = (this.selectedSuggestionIndex - 1 + this.suggestionList.size()) % this.suggestionList.size();
                    return;

                case Keyboard.KEY_DOWN:
                    this.selectedSuggestionIndex = (this.selectedSuggestionIndex + 1) % this.suggestionList.size();
                    return;

                case Keyboard.KEY_TAB:
                case Keyboard.KEY_RETURN:
                case Keyboard.KEY_NUMPADENTER:
                    this.completeSelectedSuggestion();
                    if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
                        return;
                    }
                    break;

                case Keyboard.KEY_ESCAPE: // ESC
                    this.isSuggestionVisible = false;
                    return;
            }
        }

        this.waitingOnAutocomplete = false;

        if (keyCode == Keyboard.KEY_TAB) // 15
        {
            this.sendAutocompleteRequest(this.inputField.getText().substring(0, this.inputField.getCursorPosition()));
        }
        else
        {
            this.isSuggestionVisible = false;
        }

        if (keyCode == Keyboard.KEY_ESCAPE) // 1
        {
            this.mc.displayGuiScreen(null);
        }
        else if (keyCode != Keyboard.KEY_RETURN && keyCode != Keyboard.KEY_NUMPADENTER) // 28/ 156
        {
            if (keyCode == Keyboard.KEY_UP) // 200
            {
                this.getSentHistory(-1);
            }
            else if (keyCode == Keyboard.KEY_DOWN) // 208
            {
                this.getSentHistory(1);
            }
            else if (keyCode == Keyboard.KEY_PRIOR) // 201 (Page Up)
            {
                this.mc.ingameGUI.getChatGUI().scroll(this.mc.ingameGUI.getChatGUI().getLineCount() - 1);
            }
            else if (keyCode == Keyboard.KEY_NEXT) // 209 (Page Down)
            {
                this.mc.ingameGUI.getChatGUI().scroll(-this.mc.ingameGUI.getChatGUI().getLineCount() + 1);
            }
            else
            {
                this.inputField.textboxKeyTyped(typedChar, keyCode);
            }
        }
        else
        {
            String s = this.inputField.getText().trim();

            if (!s.isEmpty())
            {
                this.sendChatMessage(s);
            }

            this.mc.displayGuiScreen(null);
        }
    }

    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();

        if (i != 0)
        {
            if (i > 1)
            {
                i = 1;
            }

            if (i < -1)
            {
                i = -1;
            }

            if (!isShiftKeyDown())
            {
                i *= 7;
            }

            this.mc.ingameGUI.getChatGUI().scroll(i);
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (mouseButton == 0)
        {
            IChatComponent ichatcomponent = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

            if (this.handleComponentClick(ichatcomponent))
            {
                return;
            }
        }

        this.inputField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void setText(String newChatText, boolean shouldOverwrite)
    {
        if (shouldOverwrite)
        {
            this.inputField.setText(newChatText);
        }
        else
        {
            this.inputField.writeText(newChatText);
        }
    }

    private void sendAutocompleteRequest(String p_146405_1_)
    {
        if (!p_146405_1_.isEmpty()) {
            BlockPos blockpos = null;

            if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            {
                blockpos = this.mc.objectMouseOver.getBlockPos();
            }

            this.mc.thePlayer.sendQueue.addToSendQueue(new C14PacketTabComplete(p_146405_1_, blockpos));
            this.waitingOnAutocomplete = true;
        }
    }

    public void getSentHistory(int msgPos) {
        int i = this.sentHistoryCursor + msgPos;
        int j = this.mc.ingameGUI.getChatGUI().getSentMessages().size();
        i = MathHelper.clamp_int(i, 0, j);

        if (i != this.sentHistoryCursor) {
            if (i == j) {
                this.sentHistoryCursor = j;
                this.inputField.setText(this.historyBuffer);
            } else {
                if (this.sentHistoryCursor == j) this.historyBuffer = this.inputField.getText();

                this.inputField.setText(this.mc.ingameGUI.getChatGUI().getSentMessages().get(i));
                this.sentHistoryCursor = i;
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawRect(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);
        this.inputField.drawTextBox();

        this.drawSuggestionBox();

        IChatComponent ichatcomponent = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

        if (ichatcomponent != null && ichatcomponent.getChatStyle().getChatHoverEvent() != null) this.handleComponentHover(ichatcomponent, mouseX, mouseY);


        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void onAutocompleteResponse(String[] completions)
    {
        if (this.waitingOnAutocomplete) {
            this.waitingOnAutocomplete = false;
            this.suggestionList.clear();

            for (String completion : completions) if (!completion.isEmpty()) this.suggestionList.add(completion);

            if (!this.suggestionList.isEmpty()) {
                this.isSuggestionVisible = true;
                this.selectedSuggestionIndex = 0;

                if (this.suggestionList.size() == 1) {
                    String s1 = this.inputField.getText().substring(this.inputField.findPosition(-1, this.inputField.getCursorPosition(), false));
                    if (!this.suggestionList.getFirst().equalsIgnoreCase(s1)) this.completeSelectedSuggestion();
                }
            } else this.isSuggestionVisible = false;
        }
    }

    private void completeSelectedSuggestion() {
        if (this.isSuggestionVisible && this.selectedSuggestionIndex >= 0 && this.selectedSuggestionIndex < this.suggestionList.size()) {
            String selected = this.suggestionList.get(this.selectedSuggestionIndex);

            int i = this.inputField.findPosition(-1, this.inputField.getCursorPosition(), false);
            this.inputField.deleteFromCursor(i - this.inputField.getCursorPosition());
            this.inputField.writeText(selected);

            this.isSuggestionVisible = false;
        }
    }

    private void drawSuggestionBox() {
        if (!this.isSuggestionVisible) return;

        int boxX = 4;
        int boxY = this.height - 14 - 3;
        int maxBoxHeight = 90;
        int boxWidth = 0;
        for (String suggestion : this.suggestionList) boxWidth = Math.max(boxWidth, this.fontRendererObject.getStringWidth(suggestion));

        boxWidth += 6;

        int boxHeight = Math.min(this.suggestionList.size() * 12, maxBoxHeight);
        boxY -= boxHeight;

        drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xD0000000);

        for (int i = 0; i < this.suggestionList.size(); i++) {
            String suggestion = this.suggestionList.get(i);
            int entryY = boxY + 2 + (i * 12);

            if (i == this.selectedSuggestionIndex) drawRect(boxX + 1, entryY - 1, boxX + boxWidth - 1, entryY + 10, 0xFF808080);

            this.fontRendererObject.drawStringWithShadow(suggestion, boxX + 3, entryY, 0xFFFFFFFF);
        }
    }

    public boolean doesGuiPauseGame() {
        return false;
    }
}