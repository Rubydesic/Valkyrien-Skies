package valkyrienwarfare.mod.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Used to tell the server when a client has pressed a button from a VW TileEntity with a gui.
 */
public class VWGuiButtonMessage implements IMessage {

    private BlockPos tileEntityPos;
    private int buttonId;

    /**
     * Server constructor
     */
    @SuppressWarnings("unused")
    public VWGuiButtonMessage() {
    }

    /**
     * Client constructor
     *
     * @param tileEntity
     * @param buttonId
     */
    public VWGuiButtonMessage(TileEntity tileEntity, int buttonId) {
        this.tileEntityPos = tileEntity.getPos();
        this.buttonId = buttonId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        tileEntityPos = packetBuffer.readBlockPos();
        buttonId = packetBuffer.readVarInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeBlockPos(tileEntityPos);
        packetBuffer.writeVarInt(buttonId);
    }

    public BlockPos getTileEntityPos() {
        return tileEntityPos;
    }

    public int getButtonId() {
        return buttonId;
    }
}
