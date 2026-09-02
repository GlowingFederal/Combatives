package com.flansmod.common.guns;

import java.util.HashMap;
import java.util.LinkedHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class QueuedReload {

	private World world;
	private Entity entity;
	private IInventory inventory;
	private boolean creative, combineAmmoOnReload, ammoToUpperInventory;
	private boolean forceReload;
	
	public final ItemStack gunStack;
	
	private boolean didReload = false;
	private float reloadTime = 0;
	
	
	public QueuedReload(ItemStack gunStack, float reloadTime, World world,
			Entity entity, IInventory inventory, boolean creative, boolean forceReload, boolean combineAmmoOnReload, boolean ammoToUpperInventory) {
		this.gunStack = gunStack;
		
		this.reloadTime = reloadTime;
		
		this.world = world;
		this.entity = entity;
		this.inventory = inventory;
		this.creative = creative;
		this.forceReload = forceReload;
		this.combineAmmoOnReload = combineAmmoOnReload;
		this.ammoToUpperInventory = ammoToUpperInventory;
	}
	
	public void decrementReloadTime() {
		if(reloadTime > 0) reloadTime--;
	}
	
	public float getReloadTime() {
		return reloadTime;
	}
	
	public void doReload() {
		if(didReload || gunStack == null) return;	
		didReload = true;
		ItemGun gun = ((ItemGun)gunStack.getItem());
		
		gun.reload(gunStack, gun.type, world, entity, inventory, creative, forceReload, combineAmmoOnReload, ammoToUpperInventory, reloadTime, false);
	}
	
}
