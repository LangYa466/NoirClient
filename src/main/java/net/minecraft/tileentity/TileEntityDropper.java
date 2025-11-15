package net.minecraft.tileentity;

public class TileEntityDropper extends TileEntityDispenser
{
    public String getName()
    {
        return this.hasCustomName() ? this.customName : "container.dropper";
    }

    public String guiID()
    {
        return "minecraft:dropper";
    }
}
