package com.hbm.inventory.container;

import com.hbm.items.tool.ItemPlasticBag;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerPlasticBag extends Container {

    private ItemPlasticBag.InventoryPlasticBag bag;

    public ContainerPlasticBag(InventoryPlayer invPlayer, ItemPlasticBag.InventoryPlasticBag bag) {
        this.bag = bag;

        this.addSlotToContainer(new SlotItemHandler(bag, 0, 80, 65));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 134 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 192));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot) this.inventorySlots.get(index);

        if(slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if(index < bag.getSlots()) {
                if(!InventoryUtil.mergeItemStack(this.inventorySlots, itemstack1, bag.getSlots(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if(!InventoryUtil.mergeItemStack(this.inventorySlots, itemstack1, 0, bag.getSlots(), false)) {
                return ItemStack.EMPTY;
            }

            if(itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        // prevents the player from moving around the currently open box
        if(clickTypeIn == ClickType.SWAP && dragType == player.inventory.currentItem) return ItemStack.EMPTY;
        if(slotId == player.inventory.currentItem + 28) return ItemStack.EMPTY;
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
    }
}
