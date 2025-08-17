package com.hbm.tileentity.network.energy;

import com.hbm.blocks.BlockDummyable;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.util.math.Vec3d;

@AutoRegister
public class TileEntityPylonLarge extends TileEntityPylonBase {

	@Override
	public ConnectionType getConnectionType() {
		return ConnectionType.QUAD;
	}

	@Override
	public Vec3d[] getMountPos() {
		double topOff = 0.75D + 0.0625D;
		double sideOff = 3.375D;

		Vec3d vec = new Vec3d(sideOff, 0, 0);

		switch (getBlockMetadata() - BlockDummyable.offset) {
			case 2 -> vec.rotateYaw((float) Math.PI * 0.0F);
			case 4 -> vec.rotateYaw((float) Math.PI * 0.25F);
			case 3 -> vec.rotateYaw((float) Math.PI * 0.5F);
			case 5 -> vec.rotateYaw((float) Math.PI * 0.75F);
		}
		
		return new Vec3d[] {
				new Vec3d(0.5D + vec.x, 11.5D + topOff, 0.5D + vec.z),
				new Vec3d(0.5D + vec.x, 11.5D - topOff, 0.5D + vec.z),
				new Vec3d(0.5D - vec.x, 11.5D + topOff, 0.5D - vec.z),
				new Vec3d(0.5D - vec.x, 11.5D - topOff, 0.5D - vec.z),
		};
	}

	@Override
	public double getMaxWireLength() {
		return 100;
	}
}