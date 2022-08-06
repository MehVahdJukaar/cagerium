package net.mehvahdjukaar.cagerium.delete;

import net.mehvahdjukaar.cagerium.Cagerium;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;


public class MagnetTableContainerMenu extends AbstractContainerMenu {

    public final Container inventory;


    public MagnetTableContainerMenu(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(id, playerInventory);
    }

    public MagnetTableContainerMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(4));
    }

    public MagnetTableContainerMenu(int id, Inventory playerInventory, Container inventory) {
        super(Cagerium.MAGNET_TABLE_CONTAINER.get(), id);
        //tile inventory
        this.inventory = inventory;
        checkContainerSize(inventory, 4);
        inventory.startOpen(playerInventory.player);

        //magnet
        this.addSlot(new Slot(inventory, 0, 51, 38) {

            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }
        });
        this.addSlot(new Slot(inventory, 1, 50, 86) {

            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }
        });
        this.addSlot(new Slot(inventory, 2, 39, 108) {

            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }
        });
        this.addSlot(new Slot(inventory, 3, 62, 108) {

            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }
        });

        //player stuff
        for (int si = 0; si < 3; ++si)
            for (int sj = 0; sj < 9; ++sj)
                this.addSlot(new Slot(playerInventory, sj + (si + 1) * 9, 8 + sj * 18, 56 + 84 + si * 18));
        for (int si = 0; si < 9; ++si)
            this.addSlot(new Slot(playerInventory, si, 8 + si * 18, 142 + 56));
    }


    @Override
    public boolean stillValid(Player playerIn) {
        return this.inventory.stillValid(playerIn);
    }

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
     * inventory and the other inventory(s).
     */
    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack otherStack = slot.getItem();
            itemstack = otherStack.copy();
            if (index < this.inventory.getContainerSize()) {
                if (!this.moveItemStackTo(otherStack, this.inventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(otherStack, 0, this.inventory.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (otherStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    /**
     * Called when the container is closed.
     */
    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        this.inventory.stopOpen(playerIn);
    }

}