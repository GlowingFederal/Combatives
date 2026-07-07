package com.glowingfederal.combatives.proxy;

import com.glowingfederal.combatives.client.ClientMovementInputHandler;
import com.glowingfederal.combatives.client.CombativesKeyBindings;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        CombativesKeyBindings.register();
        FMLCommonHandler.instance().bus().register(new ClientMovementInputHandler());
    }
}
