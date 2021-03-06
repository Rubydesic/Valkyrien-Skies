package valkyrienwarfare.mod.common.physmanagement.chunk;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public interface IVWWorldDataCapability {

    NBTTagCompound writeToNBT();

    void readFromNBT(NBTTagCompound compound);

    /**
     * Sets the world object of this capability.
     */
    IVWWorldDataCapability setWorld(World world);

    ShipChunkAllocator getChunkAllocator();
}
