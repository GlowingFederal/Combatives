package com.glowingfederal.combatives.proxy;

import com.glowingfederal.combatives.network.NetworkHandler;
import com.glowingfederal.combatives.network.PoseSyncEvents;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
    }

    public void init(FMLInitializationEvent event) {
        NetworkHandler.register();
        FMLCommonHandler.instance().bus().register(new PoseSyncEvents());
    }
}
