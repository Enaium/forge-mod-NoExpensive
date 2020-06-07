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
        int i = 0;
        int j = 0;
        int k = 0;
        if (itemstack == null) {
            this.outputSlot.setInventorySlotContents(0, (ItemStack) null);
            this.maximumCost = 0;
        } else {
            ItemStack itemstack1 = itemstack.copy();
            ItemStack itemstack2 = this.inputSlots.getStackInSlot(1);
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack1);
            j = j + itemstack.getRepairCost() + (itemstack2 == null ? 0 : itemstack2.getRepairCost());
            this.materialCost = 0;
            boolean flag = false;
            int i2;
            if (itemstack2 != null) {
                if (!onAnvilChange(itemstack, itemstack2, this.outputSlot, this.repairedItemName, j)) {
                    return;
                }

                flag = itemstack2.getItem() == Items.ENCHANTED_BOOK && !Items.ENCHANTED_BOOK.getEnchantments(itemstack2).hasNoTags();
                int i1;
                int j1;
                if (itemstack1.isItemStackDamageable() && itemstack1.getItem().getIsRepairable(itemstack, itemstack2)) {
                    i2 = Math.min(itemstack1.getItemDamage(), itemstack1.getMaxDamage() / 4);
                    if (i2 <= 0) {
                        this.outputSlot.setInventorySlotContents(0, (ItemStack) null);
                        this.maximumCost = 0;
                        return;
                    }

                    for (i1 = 0; i2 > 0 && i1 < itemstack2.stackSize; ++i1) {
                        j1 = itemstack1.getItemDamage() - i2;
                        itemstack1.setItemDamage(j1);
                        ++i;
                        i2 = Math.min(itemstack1.getItemDamage(), itemstack1.getMaxDamage() / 4);
                    }

                    this.materialCost = i1;
                } else {
                    if (!flag && (itemstack1.getItem() != itemstack2.getItem() || !itemstack1.isItemStackDamageable())) {
                        this.outputSlot.setInventorySlotContents(0, (ItemStack) null);
                        this.maximumCost = 0;
                        return;
                    }

                    int i3;
                    int j3;
                    if (itemstack1.isItemStackDamageable() && !flag) {
                        i2 = itemstack.getMaxDamage() - itemstack.getItemDamage();
                        i1 = itemstack2.getMaxDamage() - itemstack2.getItemDamage();
                        j1 = i1 + itemstack1.getMaxDamage() * 12 / 100;
                        i3 = i2 + j1;
                        j3 = itemstack1.getMaxDamage() - i3;
                        if (j3 < 0) {
                            j3 = 0;
                        }

                        if (j3 < itemstack1.getMetadata()) {
                            itemstack1.setItemDamage(j3);
                            i += 2;
                        }
                    }

                    Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(itemstack2);
                    Iterator var19 = map1.keySet().iterator();

                    label178:
                    while (true) {
                        Enchantment enchantment1;
                        do {
                            if (!var19.hasNext()) {
                                break label178;
                            }

                            enchantment1 = (Enchantment) var19.next();
                        } while (enchantment1 == null);

                        i3 = map.containsKey(enchantment1) ? (Integer) map.get(enchantment1) : 0;
                        j3 = (Integer) map1.get(enchantment1);
                        j3 = i3 == j3 ? j3 + 1 : Math.max(j3, i3);
                        boolean flag1 = enchantment1.canApply(itemstack);
                        if (this.thePlayer.capabilities.isCreativeMode || itemstack.getItem() == Items.ENCHANTED_BOOK) {
                            flag1 = true;
                        }

                        Iterator var15 = map.keySet().iterator();

                        while (true) {
                            Enchantment enchantment;
                            do {
                                do {
                                    if (!var15.hasNext()) {
                                        if (flag1) {
                                            if (j3 > enchantment1.getMaxLevel()) {
                                                j3 = enchantment1.getMaxLevel();
                                            }

                                            map.put(enchantment1, j3);
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

                                            i += k3 * j3;
                                        }
                                        continue label178;
                                    }

                                    enchantment = (Enchantment) var15.next();
                                } while (enchantment == enchantment1);
                            } while (canApplyTogether(enchantment1, enchantment) && canApplyTogether(enchantment, enchantment1));

                            flag1 = false;
                            ++i;
                        }
                    }
                }
            }

            if (flag && !itemstack1.getItem().isBookEnchantable(itemstack1, itemstack2)) {
                itemstack1 = null;
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

            this.maximumCost = j + i;
            if (i <= 0) {
                itemstack1 = null;
            }

            if (k == i && k > 0 && this.maximumCost >= 40) {
                this.maximumCost = 39;
            }

            if (this.maximumCost >= 40 && !this.thePlayer.capabilities.isCreativeMode) {
                this.maximumCost = 39;
            }

            if (itemstack1 != null) {
                i2 = itemstack1.getRepairCost();
                if (itemstack2 != null && i2 < itemstack2.getRepairCost()) {
                    i2 = itemstack2.getRepairCost();
                }

                if (k != i || k == 0) {
                    i2 = i2 * 2 + 1;
                }

                itemstack1.setRepairCost(i2);
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
        } else if (e.getOutput() == null) {
            return true;
        } else {
            outputSlot.setInventorySlotContents(0, e.getOutput());
            this.maximumCost = e.getCost();
            this.materialCost = e.getMaterialCost();
            return false;
        }
    }

    private boolean canApplyTogether(Enchantment enchantment1, Enchantment enchantment2) {
        if ((enchantment1 instanceof EnchantmentArrowInfinite && enchantment2 instanceof EnchantmentMending) || (enchantment2 instanceof EnchantmentArrowInfinite && enchantment1 instanceof EnchantmentMending)) {
            return true;
        }
        return enchantment1.canApplyTogether(enchantment2);
    }

}