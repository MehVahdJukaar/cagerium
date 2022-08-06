package net.mehvahdjukaar.cagerium.delete;

import net.mehvahdjukaar.cagerium.Cagerium;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class MagnetTableTile extends RandomizableContainerBlockEntity implements WorldlyContainer {

    private NonNullList<ItemStack> stacks;
    private final LazyOptional<? extends IItemHandler>[] handlers;


    public MagnetTableTile(BlockPos pos, BlockState state) {
        super(Cagerium.MAGNET_TABLE_TILE.get(), pos, state );
        this.handlers = SidedInvWrapper.create(this, Direction.values());
        this.stacks = NonNullList.withSize(4, ItemStack.EMPTY);
    }

    @Override
    protected Component getDefaultName() {
        return new TextComponent("aa");
    }

    @Override
    public void setChanged() {
        if (this.level != null) {
             this.updateTileOnInventoryChanged();
             if (this.needsToUpdateClientWhenChanged()) {
                 this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
             }
            super.setChanged();
        }
    }

    public void updateTileOnInventoryChanged() {
    }

    public boolean needsToUpdateClientWhenChanged() {
        return true;
    }

    public void updateClientVisualsOnLoad() {
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (!this.tryLoadLootTable(compound)) {
            this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        }

        ContainerHelper.loadAllItems(compound, this.stacks);
        if (this.level != null) {
            if (this.level.isClientSide) {
                this.updateClientVisualsOnLoad();
            } else {
                this.updateTileOnInventoryChanged();
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        if (!this.trySaveLootTable(compound)) {
            ContainerHelper.saveAllItems(compound, this.stacks);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public int getContainerSize() {
        return this.stacks.size();
    }

    public AbstractContainerMenu createMenu(int id, Inventory player) {
        return new MagnetTableContainerMenu(id, player, this);
    }

    protected NonNullList<ItemStack> getItems() {
        return this.stacks;
    }

    public void setItems(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    public boolean canPlaceItem(int index, ItemStack stack) {
        return this.isEmpty();
    }

    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return false;
    }

    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }

    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, this.getContainerSize()).toArray();
    }

    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        return !this.remove && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? this.handlers[facing.ordinal()].cast() : super.getCapability(capability, facing);
    }

    public void setRemoved() {
        super.setRemoved();
        for (LazyOptional<? extends IItemHandler> handler : this.handlers) {
            handler.invalidate();
        }

    }
}

