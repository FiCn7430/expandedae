package lu.kolja.expandedae.util;

import appeng.api.upgrades.IUpgradeInventory;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Iterator;

/**
 * 组合升级槽 - 将两个 IUpgradeInventory 合并为一个
 *
 * 用于在 AppFlux 已添加升级槽的情况下，额外添加自动合成卡槽位
 */
public class CombinedUpgradeInventory implements IUpgradeInventory {

    private final IUpgradeInventory primary;   // AppFlux 提供的槽位（槽位 0）
    private final IUpgradeInventory secondary; // 我们的额外槽位（槽位 1）

    public CombinedUpgradeInventory(IUpgradeInventory primary, IUpgradeInventory secondary) {
        this.primary = primary != null ? primary : new EmptyUpgradeInventory();
        this.secondary = secondary != null ? secondary : new EmptyUpgradeInventory();
    }

    @Override
    public int size() {
        return primary.size() + secondary.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < primary.size()) {
            return primary.getStackInSlot(slot);
        } else {
            return secondary.getStackInSlot(slot - primary.size());
        }
    }

    @Override
    public void setItemDirect(int slot, ItemStack stack) {
        if (slot < primary.size()) {
            primary.setItemDirect(slot, stack);
        } else {
            secondary.setItemDirect(slot - primary.size(), stack);
        }
    }

    @Override
    public ItemLike getUpgradableItem() {
        // 返回主升级槽的可升级物品
        return primary.getUpgradableItem();
    }

    @Override
    public boolean isInstalled(ItemLike upgrade) {
        return primary.isInstalled(upgrade) || secondary.isInstalled(upgrade);
    }

    @Override
    public int getInstalledUpgrades(ItemLike u) {
        return primary.getInstalledUpgrades(u) + secondary.getInstalledUpgrades(u);
    }

    @Override
    public int getMaxInstalled(ItemLike u) {
        // 返回两个槽位中的最大值，而不是总和
        // 这样可以确保每种升级卡最多只能安装一张（在任一槽位中）
        int primaryMax = primary.getMaxInstalled(u);
        int secondaryMax = secondary.getMaxInstalled(u);
        return Math.max(primaryMax, secondaryMax);
    }

    @Override
    public void readFromNBT(CompoundTag data, String subtag, HolderLookup.Provider registries) {
        // 分别读取两个槽位的数据
        primary.readFromNBT(data, subtag + "_primary", registries);
        secondary.readFromNBT(data, subtag + "_secondary", registries);
    }

    @Override
    public void writeToNBT(CompoundTag data, String subtag, HolderLookup.Provider registries) {
        // 分别写入两个槽位的数据
        primary.writeToNBT(data, subtag + "_primary", registries);
        secondary.writeToNBT(data, subtag + "_secondary", registries);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        // 检查该升级卡是否已经在另一个槽位中安装
        if (!stack.isEmpty() && isInstalled(stack.getItem())) {
            // 如果已经安装，拒绝插入
            return stack;
        }
        
        if (slot < primary.size()) {
            return primary.insertItem(slot, stack, simulate);
        } else {
            return secondary.insertItem(slot - primary.size(), stack, simulate);
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot < primary.size()) {
            return primary.extractItem(slot, amount, simulate);
        } else {
            return secondary.extractItem(slot - primary.size(), amount, simulate);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot < primary.size()) {
            return primary.getSlotLimit(slot);
        } else {
            return secondary.getSlotLimit(slot - primary.size());
        }
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        // 检查该升级卡是否已经在另一个槽位中安装
        if (!stack.isEmpty() && isInstalled(stack.getItem())) {
            // 如果已经安装，该物品无效（不能再次插入）
            return false;
        }
        
        if (slot < primary.size()) {
            return primary.isItemValid(slot, stack);
        } else {
            return secondary.isItemValid(slot - primary.size(), stack);
        }
    }

    @Override
    public void clear() {
        primary.clear();
        secondary.clear();
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return new CombinedIterator(primary.iterator(), secondary.iterator());
    }

    /**
     * 获取 AppFlux 的原始升级槽
     */
    public IUpgradeInventory getPrimary() {
        return primary;
    }

    /**
     * 获取我们的额外升级槽
     */
    public IUpgradeInventory getSecondary() {
        return secondary;
    }

    /**
     * 空升级槽实现
     */
    private static class EmptyUpgradeInventory implements IUpgradeInventory {
        @Override
        public int size() { return 0; }

        @Override
        public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }

        @Override
        public void setItemDirect(int slot, ItemStack stack) {}

        @Override
        public ItemLike getUpgradableItem() { return null; }

        @Override
        public boolean isInstalled(ItemLike upgrade) { return false; }

        @Override
        public int getInstalledUpgrades(ItemLike u) { return 0; }

        @Override
        public int getMaxInstalled(ItemLike u) { return 0; }

        @Override
        public void readFromNBT(CompoundTag data, String subtag, HolderLookup.Provider registries) {}

        @Override
        public void writeToNBT(CompoundTag data, String subtag, HolderLookup.Provider registries) {}

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }

        @Override
        public int getSlotLimit(int slot) { return 0; }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) { return false; }

        @Override
        public void clear() {}

        @Override
        public Iterator<ItemStack> iterator() {
            return new Iterator<>() {
                @Override
                public boolean hasNext() { return false; }
                @Override
                public ItemStack next() { return ItemStack.EMPTY; }
            };
        }
    }

    /**
     * 组合迭代器
     */
    private static class CombinedIterator implements Iterator<ItemStack> {
        private final Iterator<ItemStack> first;
        private final Iterator<ItemStack> second;

        CombinedIterator(Iterator<ItemStack> first, Iterator<ItemStack> second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean hasNext() {
            return first.hasNext() || second.hasNext();
        }

        @Override
        public ItemStack next() {
            if (first.hasNext()) {
                return first.next();
            } else {
                return second.next();
            }
        }
    }
}
