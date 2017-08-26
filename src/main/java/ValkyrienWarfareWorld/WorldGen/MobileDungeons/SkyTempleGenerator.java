package ValkyrienWarfareWorld.WorldGen.MobileDungeons;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.PhysicsManagement.ShipType;
import ValkyrienWarfareBase.Schematics.SchematicReader;
import ValkyrienWarfareBase.Schematics.SchematicReader.Schematic;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareWorld.TileEntity.TileEntitySkyTempleController;
import ValkyrienWarfareWorld.ValkyrienWarfareWorldMod;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class SkyTempleGenerator {

	final static BlockPos skyControllerPos = new BlockPos(7, 15, 8);
	final static BlockPos skulkerBoxPos = new BlockPos(7, 16, 8);

	public static void runGenerator(World world, int chunkX, int chunkZ, double random) {
		if (random < ValkyrienWarfareMod.shipmobs_spawnrate) {
			Schematic lootGet = SchematicReader.get("flying_temple_real.schemat");

			if (lootGet == null) {
				return;
			}

			PhysicsWrapperEntity wrapperEntity = new PhysicsWrapperEntity(world, chunkX << 4, 150, chunkZ << 4, ShipType.Dungeon_Sky, lootGet);

			runFinishingTouches(wrapperEntity, lootGet);

			//do it
			wrapperEntity.forceSpawn = true;

			wrapperEntity.posY = 50D;

			world.spawnEntity(wrapperEntity);

			wrapperEntity.posY = 50D;
		}
	}

	public static void runFinishingTouches(PhysicsWrapperEntity wrapperEntity, Schematic lootGet) {
		BlockPos centerInSchematic = new BlockPos(-(lootGet.width / 2), 128 - (lootGet.height / 2), -(lootGet.length / 2));

		BlockPos centerDifference = wrapperEntity.wrapping.refrenceBlockPos.subtract(centerInSchematic);

		BlockPos realSkyControllerPos = skyControllerPos.add(centerDifference);
		BlockPos realSkulkerBoxPos = skulkerBoxPos.add(centerDifference);

		wrapperEntity.world.setBlockState(realSkyControllerPos, ValkyrienWarfareWorldMod.instance.skydungeon_controller.getDefaultState());

		wrapperEntity.yaw = Math.random() * 360D;

		TileEntityShulkerBox skulkerTile = (TileEntityShulkerBox) wrapperEntity.world.getTileEntity(realSkulkerBoxPos);

		ItemStack stack = ValkyrienWarfareWorldMod.instance.etheriumCrystal.getDefaultInstance().copy();
		stack.stackSize = 5;

		skulkerTile.setInventorySlotContents(new Random().nextInt(26), stack);

		TileEntitySkyTempleController skyTile = (TileEntitySkyTempleController) wrapperEntity.world.getTileEntity(realSkyControllerPos);
		skyTile.setOriginPos(new Vector(wrapperEntity.posX, wrapperEntity.posY, wrapperEntity.posZ));
	}
}
