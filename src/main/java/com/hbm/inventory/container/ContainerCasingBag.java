package com.hbm.inventory.container;

import com.hbm.items.tool.ItemCasingBag;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerCasingBag extends Container {

    private ItemCasingBag.InventoryCasingBag bag;
    private EnumHand hand;

    public ContainerCasingBag(InventoryPlayer invPlayer, ItemCasingBag.InventoryCasingBag bag, EnumHand hand) {
        this.bag = bag;
        this.hand = hand;

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 5; j++) {
                this.addSlotToContainer(new SlotItemHandler(bag, j + i * 5, 44 + j * 18, 18 + i * 18));
            }
        }

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 100 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 158));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot) this.inventorySlots.get(index);

        if(slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if(index <= bag.getSlots() - 1) {
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
        if(hand == EnumHand.MAIN_HAND) {
            if(clickTypeIn == ClickType.SWAP && dragType == player.inventory.currentItem) return ItemStack.EMPTY;
            int heldSlot = bag.getSlots() + 27 + player.inventory.currentItem;
            if(slotId == heldSlot) return ItemStack.EMPTY;
        }
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
