package valkyrienwarfare.addon.control.block.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.addon.control.MultiblockRegistry;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;

import java.util.ArrayList;
import java.util.List;

public class EthereumCompressorMultiblockSchematic implements IMulitblockSchematic {

    private final List<BlockPosBlockPair> structureRelativeToCenter;
    private String schematicID;
    private EnumMultiblockRotation multiblockRotation;

    public EthereumCompressorMultiblockSchematic() {
        this.structureRelativeToCenter = new ArrayList<BlockPosBlockPair>();
        this.schematicID = MultiblockRegistry.EMPTY_SCHEMATIC_ID;
    }

    @Override
    public void initializeMultiblockSchematic(String schematicID) {
        Block enginePart = ValkyrienWarfareControl.INSTANCE.vwControlBlocks.etherCompressorPanel;
        for (int x = 0; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = 0; z <= 1; z++) {
                    structureRelativeToCenter.add(new BlockPosBlockPair(new BlockPos(x, y, z), enginePart));
                }
            }
        }
        this.schematicID = schematicID;
    }

    @Override
    public List<BlockPosBlockPair> getStructureRelativeToCenter() {
        return structureRelativeToCenter;
    }

    @Override
    public String getSchematicID() {
        return this.schematicID;
    }

    @Override
    public void applyMultiblockCreation(World world, BlockPos tilePos, BlockPos relativePos) {
        TileEntity tileEntity = world.getTileEntity(tilePos);
        if (!(tileEntity instanceof TileEntityEthereumCompressorPart)) {
            throw new IllegalStateException();
        }
        TileEntityEthereumCompressorPart enginePart = (TileEntityEthereumCompressorPart) tileEntity;
        enginePart.assembleMultiblock(this, relativePos);
    }

    @Override
    public String getSchematicPrefix() {
        return "multiblock_ether_compressor";
    }

    @Override
    public List<IMulitblockSchematic> generateAllVariants() {
        List<IMulitblockSchematic> varients = new ArrayList<IMulitblockSchematic>();

        for (EnumMultiblockRotation potentialRotation : EnumMultiblockRotation.values()) {
            EthereumCompressorMultiblockSchematic varient = new EthereumCompressorMultiblockSchematic();

            varient.initializeMultiblockSchematic(getSchematicPrefix() + ":rot:" + potentialRotation.toString());

            List<BlockPosBlockPair> rotatedPairs = new ArrayList<BlockPosBlockPair>();
            for (BlockPosBlockPair unrotatedPairs : varient.structureRelativeToCenter) {
                BlockPos rotatedPos = potentialRotation.rotatePos(unrotatedPairs.getPos());
                rotatedPairs.add(new BlockPosBlockPair(rotatedPos, unrotatedPairs.getBlock()));
            }
            varient.structureRelativeToCenter.clear();
            varient.structureRelativeToCenter.addAll(rotatedPairs);
            varient.multiblockRotation = potentialRotation;
            varients.add(varient);
        }
        return varients;
    }

    @Override
    public EnumMultiblockRotation getMultiblockRotation() {
        return multiblockRotation;
    }

}
