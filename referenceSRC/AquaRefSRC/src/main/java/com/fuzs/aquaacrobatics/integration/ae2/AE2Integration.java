package com.fuzs.aquaacrobatics.integration.ae2;

import net.minecraft.entity.item.EntityItem;

import appeng.api.implementations.items.IGrowableCrystal;

public class AE2Integration {

    public static boolean isGrowingCrystal(EntityItem item) {
        return item.getEntityItem()
            .getItem() instanceof IGrowableCrystal;
    }
}
