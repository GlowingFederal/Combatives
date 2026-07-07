package com.fuzs.aquaacrobatics.mixins.early.minecraft.accessor;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.eventhandler.IEventListener;

@SuppressWarnings("unused")
@Mixin(EventBus.class)
public interface IEventBusAccessor {

    @Accessor(remap = false)
    ConcurrentHashMap<Object, ArrayList<IEventListener>> getListeners();

}
