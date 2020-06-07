package cn.enaium.noexpensive.injection.mixins;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentArrowInfinite;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentMending;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.Map;

/**
 * Project: NoExpensive
 * -----------------------------------------------------------
 * Copyright © 2020 | Enaium | All rights reserved.
 */
@Mixin(ContainerRepair.class)
public abstract class ContainerRepairMixin extends Container {

    @Shadow
    public int maximumCost;

    @Shadow
    @Final
    private IInventory inputSlots;

    @Shadow
    @Final
    private IInventory outputSlot;

    @Shadow
    public int materialCost;

    @Shadow
    private String repairedItemName;

    @Shadow
    @Final
    private EntityPlayer player;

    /**
     * @author Enaium
     */
    @Overwrite
    public void updateRepairOutput() {
        ItemStack itemstack = this.inputSlots.getStackInSlot(0);
        this.maximumCost = 1;
        int i = 0;
        int j = 0;
        int k = 0;
        if (itemstack.isEmpty()) {
            this.outputSlot.setInventorySlotContents(0, ItemStack.EMPTY);
            this.maximumCost = 0;
        } else {
            ItemStack itemstack1 = itemstack.copy();
            ItemStack itemstack2 = this.inputSlots.getStackInSlot(1);
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack1);
            j = j + itemstack.getRepairCost() + (itemstack2.isEmpty() ? 0 : itemstack2.getRepairCost());
            this.materialCost = 0;
            boolean flag = false;
            int k2;
            if (!itemstack2.isEmpty()) {
                if (!onAnvilChange(itemstack, itemstack2, this.outputSlot, this.repairedItemName, j)) {
                    return;
                }

                flag = itemstack2.getItem() == Items.ENCHANTED_BOOK && !ItemEnchantedBook.getEnchantments(itemstack2).isEmpty();
                int i1;
                int j1;
                if (itemstack1.isItemStackDamageable() && itemstack1.getItem().getIsRepairable(itemstack, itemstack2)) {
                    k2 = Math.min(itemstack1.getItemDamage(), itemstack1.getMaxDamage() / 4);
                    if (k2 <= 0) {
                        this.outputSlot.setInventorySlotContents(0, ItemStack.EMPTY);
                        this.maximumCost = 0;
                        return;
                    }

                    for (i1 = 0; k2 > 0 && i1 < itemstack2.getCount(); ++i1) {
                        j1 = itemstack1.getItemDamage() - k2;
                        itemstack1.setItemDamage(j1);
                        ++i;
                        k2 = Math.min(itemstack1.getItemDamage(), itemstack1.getMaxDamage() / 4);
                    }

                    this.materialCost = i1;
                } else {
                    if (!flag && (itemstack1.getItem() != itemstack2.getItem() || !itemstack1.isItemStackDamageable())) {
                        this.outputSlot.setInventorySlotContents(0, ItemStack.EMPTY);
                        this.maximumCost = 0;
                        return;
                    }

                    if (itemstack1.isItemStackDamageable() && !flag) {
                        k2 = itemstack.getMaxDamage() - itemstack.getItemDamage();
                        i1 = itemstack2.getMaxDamage() - itemstack2.getItemDamage();
                        j1 = i1 + itemstack1.getMaxDamage() * 12 / 100;
                        int k1 = k2 + j1;
                        int l1 = itemstack1.getMaxDamage() - k1;
                        if (l1 < 0) {
                            l1 = 0;
                        }

                        if (l1 < itemstack1.getItemDamage()) {
                            itemstack1.setItemDamage(l1);
                            i += 2;
                        }
                    }

                    Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(itemstack2);
                    boolean flag2 = false;
                    boolean flag3 = false;
                    Iterator var23 = map1.keySet().iterator();

                    label177:
                    while (true) {
                        Enchantment enchantment1;
                        do {
                            if (!var23.hasNext()) {
                                if (flag3 && !flag2) {
                                    this.outputSlot.setInventorySlotContents(0, ItemStack.EMPTY);
                                    this.maximumCost = 0;
                                    return;
                                }
                                break label177;
                            }

                            enchantment1 = (Enchantment) var23.next();
                        } while (enchantment1 == null);

                        int i2 = map.containsKey(enchantment1) ? (Integer) map.get(enchantment1) : 0;
                        int j2 = (Integer) map1.get(enchantment1);
                        j2 = i2 == j2 ? j2 + 1 : Math.max(j2, i2);
                        boolean flag1 = enchantment1.canApply(itemstack);
                        if (this.player.capabilities.isCreativeMode || itemstack.getItem() == Items.ENCHANTED_BOOK) {
                            flag1 = true;
                        }

                        Iterator var17 = map.keySet().iterator();

                        while (var17.hasNext()) {
                            Enchantment enchantment = (Enchantment) var17.next();
                            if (enchantment != enchantment1 && !isCompatibleWith(enchantment, enchantment1)) {
                                flag1 = false;
                                ++i;
                            }
                        }

                        if (!flag1) {
                            flag3 = true;
                        } else {
                            flag2 = true;
                            if (j2 > enchantment1.getMaxLevel()) {
                                j2 = enchantment1.getMaxLevel();
                            }

                            map.put(enchantment1, j2);
                            int k3 = 0;
                            switch (enchantment1.getRarity()) {
                                case COMMON:
                                    k3 = 1;
                                    break;
                                case UNCOMMON:
                                    k3 = 2;
                                    break;
                                case RARE:
                                    k3 = 4;
                                    break;
                                case VERY_RARE:
                                    k3 = 8;
                            }

                            if (flag) {
                                k3 = Math.max(1, k3 / 2);
                            }

                            i += k3 * j2;
                            if (itemstack.getCount() > 1) {
                                i = 40;
                            }
                        }
                    }
                }
            }

            if (StringUtils.isBlank(this.repairedItemName)) {
                if (itemstack.hasDisplayName()) {
                    k = 1;
                    i += k;
                    itemstack1.clearCustomName();
                }
            } else if (!this.repairedItemName.equals(itemstack.getDisplayName())) {
                k = 1;
                i += k;
                itemstack1.setStackDisplayName(this.repairedItemName);
            }

            if (flag && !itemstack1.getItem().isBookEnchantable(itemstack1, itemstack2)) {
                itemstack1 = ItemStack.EMPTY;
            }

            this.maximumCost = j + i;
            if (i <= 0) {
                itemstack1 = ItemStack.EMPTY;
            }

            if (k == i && k > 0 && this.maximumCost >= 40) {
                this.maximumCost = 39;
            }

            if (this.maximumCost >= 40 && !this.player.capabilities.isCreativeMode) {
                this.maximumCost = 39;
            }

            if (!itemstack1.isEmpty()) {
                k2 = itemstack1.getRepairCost();
                if (!itemstack2.isEmpty() && k2 < itemstack2.getRepairCost()) {
                    k2 = itemstack2.getRepairCost();
                }

                if (k != i || k == 0) {
                    k2 = k2 * 2 + 1;
                }

                itemstack1.setRepairCost(k2);
                EnchantmentHelper.setEnchantments(map, itemstack1);
            }

            this.outputSlot.setInventorySlotContents(0, itemstack1);
            this.detectAndSendChanges();
        }
    }

    private boolean onAnvilChange(ItemStack left, ItemStack right, IInventory outputSlot, String name, int baseCost) {
        AnvilUpdateEvent e = new AnvilUpdateEvent(left, right, name, baseCost);
        if (MinecraftForge.EVENT_BUS.post(e)) {
            return false;
        } else if (e.getOutput().isEmpty()) {
            return true;
        } else {
            outputSlot.setInventorySlotContents(0, e.getOutput());
            this.maximumCost = e.getCost();
            this.materialCost = e.getMaterialCost();
            return false;
        }
    }

    private boolean isCompatibleWith(Enchantment enchantment1, Enchantment enchantment2) {
        if ((enchantment1 instanceof EnchantmentArrowInfinite && enchantment2 instanceof EnchantmentMending) || (enchantment2 instanceof EnchantmentArrowInfinite && enchantment1 instanceof EnchantmentMending)) {
            return true;
        }
        return enchantment1.isCompatibleWith(enchantment2);
    }
}