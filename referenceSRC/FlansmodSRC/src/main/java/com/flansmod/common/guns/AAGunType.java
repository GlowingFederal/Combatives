package com.flansmod.common.guns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.flansmod.utils.ConfigMap;
import com.flansmod.utils.ConfigUtils;
import net.minecraft.client.model.ModelBase;
import net.minecraft.item.ItemStack;

import com.flansmod.client.model.ModelAAGun;
import com.flansmod.common.FlansMod;
import com.flansmod.common.types.InfoType;
import com.flansmod.common.types.TypeFile;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AAGunType extends InfoType
{
	/** The ammo types used by this gun */
	public List<BulletType> ammo = new ArrayList<BulletType>();
	public int reloadTime;
	public int recoil = 5;
	public int accuracy;
	public int damage;
	public int shootDelay;
	public int numBarrels;
	public boolean fireAlternately;
	public int health;
	public int gunnerX, gunnerY, gunnerZ;
	public String shootSound;
	public String reloadSound;
	public ModelAAGun model;
	public float topViewLimit = 75F;
	public float bottomViewLimit = 0F;
	public int[] barrelX, barrelY, barrelZ;
	/** Sentry mode. If target players is true then it either targets everyone on the other team, or everyone other than the owner when not playing with teams */
	public boolean targetMobs = false, targetPlayers = false, targetVehicles = false, targetPlanes = false, targetMechas = false;
	/** Targeting radius */
	public float targetRange = 10F;
	/** If true, then all barrels share the same ammo slot */
	public boolean shareAmmo = false;
	
	public boolean canShootHomingMissile = false;
	public int countExplodeAfterShoot = -1;
	public boolean isDropThis = true;

	public static List<AAGunType> infoTypes = new ArrayList<AAGunType>();

	public AAGunType(TypeFile file) {
		super(file);
	}

	@Override
	protected void preRead(TypeFile file) { }

	@Override
	public void postRead(TypeFile file) {
		if (this.shortName != null && this.isValid) {
			infoTypes.add(this);
		}
	}
	
	@Override
	protected void read(ConfigMap config, TypeFile file) {
		super.read(config, file);
		try {
			if (FMLCommonHandler.instance().getSide().isClient()) {
				model = FlansMod.proxy.loadModel(modelString, shortName, ModelAAGun.class);
			}

			texture = ConfigUtils.configString(config, "Texture", texture);
			damage = ConfigUtils.configInt(config, "Damage", damage);
			reloadTime = ConfigUtils.configInt(config, "ReloadTime", reloadTime);
			recoil = ConfigUtils.configInt(config, "Recoil", recoil);
			accuracy = ConfigUtils.configInt(config, "Accuracy", accuracy);
			shootDelay = ConfigUtils.configInt(config, "ShootDelay", shootDelay);
			shootSound = ConfigUtils.configAASound(packName, config, "ShootSound", shootSound);
			reloadSound = ConfigUtils.configAASound(packName, config, "ReloadSound", reloadSound);
			fireAlternately = ConfigUtils.configBool(config, "FireAlternately", fireAlternately);

			numBarrels = ConfigUtils.configInt(config, "NumBarrels", numBarrels);
			barrelX = new int[numBarrels];
			barrelY = new int[numBarrels];
			barrelZ = new int[numBarrels];

			try {
				ArrayList<String[]> splits = ConfigUtils.getSplitsFromKey(config, new String[] { "Barrel"} );

				if (splits.size() != numBarrels) {
					FlansMod.logPackError(file.name, packName, shortName, "Unexpected number of Barrel positions given", null, null);
				}

				for (String[] split : splits) {
					try {
						int id = Integer.parseInt(split[1]);
						barrelX[id] = Integer.parseInt(split[2]);
						barrelY[id] = Integer.parseInt(split[3]);
						barrelZ[id] = Integer.parseInt(split[4]);
					} catch (Exception ex) {
						FlansMod.logPackError(file.name, packName, shortName, "Reading barrel failed", split, ex);
					}
				}
			} catch (Exception ex) {
				FlansMod.logPackError(file.name, packName, shortName, "Configuring barrels failed", null, ex);
			}

			health = ConfigUtils.configInt(config, "Health", health);
			topViewLimit = ConfigUtils.configFloat(config, "TopViewLimit", topViewLimit);
			bottomViewLimit = ConfigUtils.configFloat(config, "BottomViewLimit", bottomViewLimit);

			try {
				ArrayList<String[]> splits = ConfigUtils.getSplitsFromKey(config, new String[] { "Ammo" });

				for (String[] split : splits) {
					if (split.length == 2 && split[1] != null) {
						BulletType type = BulletType.getBullet(split[1]);

						if (type != null) {
							ammo.add(type);
						} else {
							FlansMod.logPackError(file.name, packName, shortName, "Unrecognised ammo type", split, null);
						}
					} else {
						FlansMod.logPackError(file.name, packName, shortName, "Invalid Ammo line", split, null);
					}
				}
			} catch (Exception ex) {
				FlansMod.logPackError(file.name, packName, shortName, "Adding ammunition failed", null, ex);
			}

			try {
				String[] split = ConfigUtils.getSplitFromKey(config, "GunnerPos");

				if (split != null) {
					gunnerX = Integer.parseInt(split[1]);
					gunnerY = Integer.parseInt(split[2]);
					gunnerZ = Integer.parseInt(split[3]);
				}
			} catch (Exception ex) {
				FlansMod.logPackError(file.name, packName, shortName, "Setting gunner position failed", null, ex);
			}

			// Sentry stuff
			targetMobs = ConfigUtils.configBool(config, "TargetMobs", targetMobs);
			targetPlayers = ConfigUtils.configBool(config, "TargetPlayers", targetPlayers);
			targetVehicles = ConfigUtils.configBool(config, "TargetVehicles", targetVehicles);
			targetPlanes = ConfigUtils.configBool(config, "TargetPlanes", targetPlanes);
			targetMechas = ConfigUtils.configBool(config, "TargetMechas", targetMechas);

			if(config.containsKey("TargetDriveables"))
				targetMechas = targetPlanes = targetVehicles = ConfigUtils.configBool(config, "TargetDriveables", false);

			shareAmmo = ConfigUtils.configBool(config, "ShareAmmo", shareAmmo);
			targetRange = ConfigUtils.configFloat(config, "TargetRange", targetRange);
			canShootHomingMissile = ConfigUtils.configBool(config, "CanShootHomingMissile", canShootHomingMissile);
			countExplodeAfterShoot = ConfigUtils.configInt(config, "CountExplodeAfterShoot", countExplodeAfterShoot);
			isDropThis = ConfigUtils.configBool(config, "IsDropThis", isDropThis);

		} catch (Exception ex) {
			FlansMod.logPackError(file.name, packName, shortName, "Fatal error occurred when reading AAGun", null, ex);
			isValid = false;
		}
	}

	public boolean isAmmo(BulletType type)
	{
		return ammo.contains(type);
	}

	public boolean isAmmo(ItemStack stack) {
		if (stack == null)
			return false;
		return stack.getItem() instanceof ItemBullet && isAmmo(((ItemBullet) stack.getItem()).type);
	}

	public static AAGunType getAAGun(String s) {
		for (AAGunType gun : infoTypes) {
			if (gun.shortName.equals(s))
				return gun;
		}
		return null;
	}
	
	/** To be overriden by subtypes for model reloading */
	public void reloadModel()
	{
		model = FlansMod.proxy.loadModel(modelString, shortName, ModelAAGun.class);
	}

	@Override
	public float GetRecommendedScale() 
	{
		return 50.0f;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBase GetModel()
	{
		return model;
	}
}
