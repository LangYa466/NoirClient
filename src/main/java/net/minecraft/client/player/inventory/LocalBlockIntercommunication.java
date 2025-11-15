package net.minecraft.client.player.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.IInteractionObject;

public record LocalBlockIntercommunication(String guiID, IChatComponent displayName) implements IInteractionObject {

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        return this.displayName.getUnformattedText();
    }

    public boolean hasCustomName() {
        return true;
    }
}
