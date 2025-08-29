package com.hbm.tileentity.machine.rbmk;

import com.hbm.api.fluid.IFluidStandardSender;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.entity.projectile.EntityRBMKDebris.DebrisType;
import com.hbm.handler.neutron.NeutronStream;
import com.hbm.handler.neutron.RBMKNeutronHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerRBMKOutgasser;
import com.hbm.inventory.control_panel.DataValue;
import com.hbm.inventory.control_panel.DataValueFloat;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIRBMKOutgasser;
import com.hbm.inventory.recipes.RBMKOutgasserRecipes;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKConsole.ColumnType;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.Tuple;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;

@AutoRegister
public class TileEntityRBMKOutgasser extends TileEntityRBMKSlottedBase implements IRBMKFluxReceiver, IFluidStandardSender, IRBMKLoadable, IGUIProvider {

	public FluidTankNTM gas;
	public double progress = 0;
	public int duration = 10000;
	private ItemStack previousStack = ItemStack.EMPTY;

	public TileEntityRBMKOutgasser() {
		super(2);
		gas = new FluidTankNTM(Fluids.TRITIUM, 64000);
	}

	@Override
	public String getName() {
		return "container.rbmkOutgasser";
	}

	@Override
	public void update() {

		if(!world.isRemote) {
			// reset timer when item changes. does not exist in 1.7.
			if(!canProcess() || !previousStack.isItemEqual(inventory.getStackInSlot(0))) {
				this.progress = 0;
			}

			for(DirPos pos : getOutputPos()) {
				if(this.gas.getFill() > 0) this.sendFluid(gas, world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
			}
			previousStack = inventory.getStackInSlot(0).copy();
		}
		
		super.update();
	}

	protected DirPos[] getOutputPos() {

		if(world.getBlockState(pos.add(0, -1, 0)).getBlock() == ModBlocks.rbmk_loader) {
			return new DirPos[] {
					new DirPos(this.pos.getX(), this.pos.getY() + RBMKDials.getColumnHeight(world) + 1, this.pos.getZ(), Library.POS_Y),
					new DirPos(this.pos.getX() + 1, this.pos.getY() - 1, this.pos.getZ(), Library.POS_X),
					new DirPos(this.pos.getX() - 1, this.pos.getY() - 1, this.pos.getZ(), Library.NEG_X),
					new DirPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ() + 1, Library.POS_Z),
					new DirPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ() - 1, Library.NEG_Z),
					new DirPos(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ(), Library.NEG_Y)
			};
		} else if(world.getBlockState(pos.add(0, -2, 0)).getBlock() == ModBlocks.rbmk_loader) {
			return new DirPos[] {
					new DirPos(this.pos.getX(), this.pos.getY() + RBMKDials.getColumnHeight(world) + 1, this.pos.getZ(), Library.POS_Y),
					new DirPos(this.pos.getX() + 1, this.pos.getY() - 2, this.pos.getZ(), Library.POS_X),
					new DirPos(this.pos.getX() - 1, this.pos.getY() - 2, this.pos.getZ(), Library.NEG_X),
					new DirPos(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ() + 1, Library.POS_Z),
					new DirPos(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ() - 1, Library.NEG_Z),
					new DirPos(this.pos.getX(), this.pos.getY() - 3, this.pos.getZ(), Library.NEG_Y)
			};
		} else {
			return new DirPos[] {
					new DirPos(this.pos.getX(), this.pos.getY() + RBMKDials.getColumnHeight(world) + 1, this.pos.getZ(), Library.POS_Y)
			};
		}
	}

	@Override
	public void receiveFlux(NeutronStream stream) {
		
		if(canProcess()) {
			double efficiency = Math.min(1 - stream.fluxRatio * 0.8, 1);
			progress += stream.fluxQuantity * efficiency * RBMKDials.getOutgasserMod(world);

			if(progress > duration) {
				process();
				this.markDirty();
			}
		} else if(!inventory.getStackInSlot(0).isEmpty()){
			double efficiency = Math.min(1 - stream.fluxRatio * 0.8, 1);

			ContaminationUtil.neutronActivateItem(inventory.getStackInSlot(0), (float)(stream.fluxQuantity * efficiency * 0.001), 1F);
			this.markDirty();
		}
	}



	public boolean canProcess() {

		if(inventory.getStackInSlot(0).isEmpty())
			return false;

		Tuple.Pair<ItemStack, FluidStack> output = RBMKOutgasserRecipes.getOutput(inventory.getStackInSlot(0));

		if(output == null)
			return false;

		FluidStack fluid = output.getValue();

		if(fluid != null) {
			if(gas.getTankType() != fluid.type && gas.getFill() > 0) return false;
			gas.setTankType(fluid.type);
			if(gas.getFill() + fluid.fill > gas.getMaxFill()) return false;
		}

		ItemStack out = output.getKey();

		if(inventory.getStackInSlot(1).isEmpty() || out == null)
			return true;

		return inventory.insertItemUnchecked(1, out.copy(), true).isEmpty();
	}


	private void process() {

		Tuple.Pair<ItemStack, FluidStack> output = RBMKOutgasserRecipes.getOutput(inventory.getStackInSlot(0));
		inventory.extractItemUnchecked(0, 1, false);
		this.progress = 0;

		if(output.getValue() != null) {
			gas.setFill(gas.getFill() + output.getValue().fill);
		}

		ItemStack out = output.getKey();

		if(out != null) {
			inventory.insertItemUnchecked(1, out.copy(), false);
		}
	}
	
	@Override
	public void onMelt(int reduce) {
		
		int count = 4 + world.rand.nextInt(2);
		
		for(int i = 0; i < count; i++) {
			spawnDebris(DebrisType.BLANK);
		}
		
		super.onMelt(reduce);
	}

	@Override
	public RBMKNeutronHandler.RBMKType getRBMKType() {
		return RBMKNeutronHandler.RBMKType.OUTGASSER;
	}

	@Override
	public ColumnType getConsoleType() {
		return ColumnType.OUTGASSER;
	}

	@Override
	public NBTTagCompound getNBTForConsole() {
		NBTTagCompound data = new NBTTagCompound();
		data.setInteger("gas", this.gas.getFill());
		data.setInteger("maxGas", this.gas.getMaxFill());
		data.setShort("type", (short)this.gas.getTankType().getID());
		data.setDouble("progress", this.progress);
		return data;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		this.progress = nbt.getDouble("progress");
		this.gas.readFromNBT(nbt, "gas");
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setDouble("progress", this.progress);
		this.gas.writeToNBT(nbt, "gas");
		return nbt;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		this.gas.serialize(buf);
		buf.writeDouble(this.progress);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.gas.deserialize(buf);
		this.progress = buf.readDouble();
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return RBMKOutgasserRecipes.getOutput(itemStack) != null && i == 0;
	}

	@Override
	public boolean canLoad(ItemStack toLoad) {
		return toLoad != null && inventory.insertItem(0, toLoad.copy(), true).isEmpty();
	}

	@Override
	public void load(ItemStack toLoad) {
		inventory.insertItem(0, toLoad.copy(), false);
		this.markDirty();
	}

	@Override
	public boolean canUnload() {
		return !inventory.getStackInSlot(1).isEmpty();
	}

	@Override
	public ItemStack provideNext() {
		return inventory.getStackInSlot(1);
	}

	@Override
	public void unload() {
		inventory.setStackInSlot(1, ItemStack.EMPTY);
		this.markDirty();
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[] {gas};
	}

	@Override
	public FluidTankNTM[] getSendingTanks() {
		return new FluidTankNTM[] {gas};
	}

	// control panel
	@Override
	public Map<String, DataValue> getQueryData() {
		Map<String, DataValue> data = super.getQueryData();

		data.put("gas", new DataValueFloat(this.gas.getFill()));
		data.put("progress", new DataValueFloat((float) this.progress));
		data.put("maxProgress", new DataValueFloat((float) this.duration));

		return data;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(
					new NTMFluidHandlerWrapper(this)
			);
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerRBMKOutgasser(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIRBMKOutgasser(player.inventory, this);
	}
}