package cn.enaium.noexpensive.injection.mixins;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
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
    private IInventory inputSlots;

    @Shadow
    public int maximumCost;

    @Shadow
    private IInventory outputSlot;

    @Shadow
    public int materialCost;

    @Shadow
    @Final
    private EntityPlayer thePlayer;

    @Shadow
    private String repairedItemName;

    /**
     * @author Enaium
     */
    @Overwrite
    public void updateRepairOutput() {
        ItemStack itemstack = this.inputSlots.getStackInSlot(0);
        this.maximumCost = 1;
        int l1 = 0;
        int i2 = 0;
        int j2 = 0;
        if (itemstack == null) {
            this.outputSlot.setInventorySlotContents(0, (ItemStack) null);
            this.maximumCost = 0;
        } else {
            ItemStack itemstack1 = itemstack.copy();
            ItemStack itemstack2 = this.inputSlots.getStackInSlot(1);
            Map<Integer, Integer> map = EnchantmentHelper.getEnchantments(itemstack1);
            boolean flag = false;
            i2 = i2 + itemstack.getRepairCost() + (itemstack2 == null ? 0 : itemstack2.getRepairCost());
            this.materialCost = 0;
            int k4;
            if (itemstack2 != null) {
                if (!onAnvilChange(itemstack, itemstack2, this.outputSlot, this.repairedItemName, i2)) {
                    return;
                }

                flag = itemstack2.getItem() == Items.enchanted_book && Items.enchanted_book.getEnchantments(itemstack2).tagCount() > 0;
                int l2;
                int i5;
                if (itemstack1.isItemStackDamageable() && itemstack1.getItem().getIsRepairable(itemstack, itemstack2)) {
                    k4 = Math.min(itemstack1.getItemDamage(), itemstack1.getMaxDamage() / 4);
                    if (k4 <= 0) {
                        this.outputSlot.setInventorySlotContents(0, (ItemStack) null);
                        this.maximumCost = 0;
                        return;
                    }

                    for (l2 = 0; k4 > 0 && l2 < itemstack2.stackSize; ++l2) {
                        i5 = itemstack1.getItemDamage() - k4;
                        itemstack1.setItemDamage(i5);
                        ++l1;
                        k4 = Math.min(itemstack1.getItemDamage(), itemstack1.getMaxDamage() / 4);
                    }

                    this.materialCost = l2;
                } else {
                    if (!flag && (itemstack1.getItem() != itemstack2.getItem() || !itemstack1.isItemStackDamageable())) {
                        this.outputSlot.setInventorySlotContents(0, (ItemStack) null);
                        this.maximumCost = 0;
                        return;
                    }

                    int k5;
                    if (itemstack1.isItemStackDamageable() && !flag) {
                        k4 = itemstack.getMaxDamage() - itemstack.getItemDamage();
                        l2 = itemstack2.getMaxDamage() - itemstack2.getItemDamage();
                        i5 = l2 + itemstack1.getMaxDamage() * 12 / 100;
                        int j3 = k4 + i5;
                        k5 = itemstack1.getMaxDamage() - j3;
                        if (k5 < 0) {
                            k5 = 0;
                        }

                        if (k5 < itemstack1.getMetadata()) {
                            itemstack1.setItemDamage(k5);
                            l1 += 2;
                        }
                    }

                    Map<Integer, Integer> map1 = EnchantmentHelper.getEnchantments(itemstack2);
                    Iterator iterator1 = map1.keySet().iterator();

                    label165:
                    while (true) {
                        Enchantment enchantment;
                        do {
                            if (!iterator1.hasNext()) {
                                break label165;
                            }

                            i5 = (Integer) iterator1.next();
                            enchantment = Enchantment.getEnchantmentById(i5);
                        } while (enchantment == null);

                        k5 = map.containsKey(i5) ? (Integer) map.get(i5) : 0;
                        int l3 = (Integer) map1.get(i5);
                        int i6;
                        if (k5 == l3) {
                            ++l3;
                            i6 = l3;
                        } else {
                            i6 = Math.max(l3, k5);
                        }

                        l3 = i6;
                        boolean flag1 = enchantment.canApply(itemstack);
                        if (this.thePlayer.capabilities.isCreativeMode || itemstack.getItem() == Items.enchanted_book) {
                            flag1 = true;
                        }

                        Iterator iterator = map.keySet().iterator();

                        while (true) {
                            int l5;
                            Enchantment e2;
                            do {
                                do {
                                    if (!iterator.hasNext()) {
                                        if (flag1) {
                                            if (i6 > enchantment.getMaxLevel()) {
                                                l3 = enchantment.getMaxLevel();
                                            }

                                            map.put(i5, l3);
                                            l5 = 0;
                                            switch (enchantment.getWeight()) {
                                                case 1:
                                                    l5 = 8;
                                                    break;
                                                case 2:
                                                    l5 = 4;
                                                case 3:
                                                case 4:
                                                case 6:
                                                case 7:
                                                case 8:
                                                case 9:
                                                default:
                                                    break;
                                                case 5:
                                                    l5 = 2;
                                                    break;
                                                case 10:
                                                    l5 = 1;
                                            }

                                            if (flag) {
                                                l5 = Math.max(1, l5 / 2);
                                            }

                                            l1 += l5 * l3;
                                        }
                                        continue label165;
                                    }

                                    l5 = (Integer) iterator.next();
                                    e2 = Enchantment.getEnchantmentById(l5);
                                } while (l5 == i5);
                            } while (enchantment.canApplyTogether(e2) && e2.canApplyTogether(enchantment));

                            flag1 = false;
                            ++l1;
                        }
                    }
                }
            }

            if (flag && !itemstack1.getItem().isBookEnchantable(itemstack1, itemstack2)) {
                itemstack1 = null;
            }

            if (StringUtils.isBlank(this.repairedItemName)) {
                if (itemstack.hasDisplayName()) {
                    j2 = 1;
                    l1 += j2;
                    itemstack1.clearCustomName();
                }
            } else if (!this.repairedItemName.equals(itemstack.getDisplayName())) {
                j2 = 1;
                l1 += j2;
                itemstack1.setStackDisplayName(this.repairedItemName);
            }

            this.maximumCost = i2 + l1;
            if (l1 <= 0) {
                itemstack1 = null;
            }

            if (j2 == l1 && j2 > 0 && this.maximumCost >= 40) {
                this.maximumCost = 39;
            }

            if (this.maximumCost >= 40 && !this.thePlayer.capabilities.isCreativeMode) {
                this.maximumCost = 39;
            }

            if (itemstack1 != null) {
                k4 = itemstack1.getRepairCost();
                if (itemstack2 != null && k4 < itemstack2.getRepairCost()) {
                    k4 = itemstack2.getRepairCost();
                }

                k4 = k4 * 2 + 1;
                itemstack1.setRepairCost(k4);
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
        } else if (e.output == null) {
            return true;
        } else {
            outputSlot.setInventorySlotContents(0, e.output);
            this.maximumCost = e.cost;
            this.materialCost = e.materialCost;
            return false;
        }
    }

}