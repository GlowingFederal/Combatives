package com.flansmod.common.guns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.flansmod.utils.ConfigMap;
import com.flansmod.utils.ConfigUtils;
import net.minecraft.client.model.ModelBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import com.flansmod.client.model.ModelAttachment;
import com.flansmod.common.FlansMod;
import com.flansmod.common.paintjob.PaintableType;
import com.flansmod.common.types.InfoType;
import com.flansmod.common.types.TypeFile;

public class AttachmentType extends PaintableType implements IScope
{
	public static ArrayList<AttachmentType> attachments = new ArrayList<AttachmentType>();
	
	/** The type of attachment. Each gun can have one barrel, one scope, one grip, one stock and some number of generics up to a limit set by the gun */
	public EnumAttachmentType type = EnumAttachmentType.generic;

	//Attachment Function add-ons
	/** This variable controls whether or not bullet sounds should be muffled */
	public boolean silencer = false;
	/** If true, then this attachment will act like a flashlight */
	public boolean flashlight = false;
	/** Flashlight range. How far away it lights things up */
	public float flashlightRange = 10F;
	/** Flashlight strength between 0 and 15 */
	public int flashlightStrength = 12;
	/** If true, disable the muzzle flash model */
	public boolean disableMuzzleFlash = false;
	
	//Gun behaviour modifiers
	/** These stack between attachments and apply themselves to the gun's default spread */
	public float spreadMultiplier = 1F;
	/** Likewise these stack and affect recoil */
	public float recoilMultiplier = 1F;
	/** The return to center force LOWER = BETTER */
	public float recoilControlMultiplier = 1F;
	public float recoilControlMultiplierSneaking = 1F;
	public float recoilControlMultiplierSprinting = 1F;
	/** Another stacking variable for damage */
	public float damageMultiplier = 1F;
	/** Melee damage modifier */
	public float meleeDamageMultiplier = 1F;
	/** Bullet speed modifier */
	public float bulletSpeedMultiplier = 1F;
	/** This modifies the reload time, which is then rounded down to the nearest tick */
	public float reloadTimeMultiplier = 1F;
	/** Movement speed modifier */
	public float moveSpeedMultiplier = 1F;
	/** If set to anything other than null, then this attachment will override the weapon's default firing mode */
	public EnumFireMode modeOverride = null;

	//Underbarrel functions
	/** This variable controls whether the underbarrel is enabled */
	public boolean secondaryFire = false;
	/** The list of bullet types that can be used in the secondary mode */
	public List<String> secondaryAmmo = new ArrayList<String>();
	/** The delay between shots in ticks (1/20ths of seconds) */
	public float secondaryDamage = 1;
	/** The delay between shots in ticks (1/20ths of seconds) */
	public float secondarySpread = 1;
	/** The speed of bullets upon leaving this gun */
	public float secondarySpeed = 5.0F;
	/** The time (in ticks) it takes to reload this gun */
	public int secondaryReloadTime = 1;
	/** The delay between shots in ticks (1/20ths of seconds) */
	public int secondaryShootDelay = 1;
	/** The sound played upon shooting */
	public String secondaryShootSound;
	/** The sound to play upon reloading */
	public String secondaryReloadSound;
	/** The firing mode of the gun. One of semi-auto, full-auto, minigun or burst */
	public EnumFireMode secondaryFireMode = EnumFireMode.SEMIAUTO;
	/** The sound to play if toggling between primary and underbarrel */
	public String toggleSound;
	/** The number of bullet entities created by each shot */
	public int secondaryNumBullets = 1;
	/** The number of bullet stacks in the magazine */
	public int numSecAmmoItems = 1;

	//Scope variables (These variables only come into play for scope attachments)
	/** The zoomLevel of this scope */
	public float zoomLevel = 1F;
	/** The FOV zoom level of this scope */
	public float FOVZoomLevel = 1F;
	/** The overlay to render when using this scope */
	public String zoomOverlay;
	/** Whether to overlay a texture or not */
	public boolean hasScopeOverlay = false;
	/** If true, then this scope will active night vision potion effect*/
	public boolean hasNightVision = false;
	
	@SideOnly(Side.CLIENT)
	/** Model. Only applicable when the attachment is added to 3D guns */
	public ModelAttachment model;

	
	//Some more mundane variables
	/** The max stack size in the inventory */
	public int maxStackSize = 1;

	/** Default spread of the underbarrel. Do not modify. */
	public float secondaryDefaultSpread = 0F;

	public boolean hasVariableZoom = false;
	private float minZoom = 1;
	private float maxZoom = 4;
	private float zoomAugment = 1;

	
	public AttachmentType(TypeFile file)
	{
		super(file);


	}

	@Override
	public void postRead(TypeFile file) {
		super.postRead(file);

		if (this.shortName != null && isValid) {
			attachments.add(this);
		}
	}
	
	@Override
	protected void read(ConfigMap config, TypeFile file) {
		try {
			super.read(config, file);

			String typeString = ConfigUtils.configString(config, "AttachmentType", type.toString());
			type = EnumAttachmentType.get(typeString);

			if (FMLCommonHandler.instance().getSide().isClient()) {
				model = FlansMod.proxy.loadModel(modelString, shortName, ModelAttachment.class);
			}

			// This is read in InfoType
			//modelScale = ConfigUtils.configFloat(config, "ModelScale", modelScale);
			texture = ConfigUtils.configString(config, "Texture", texture);
			silencer = ConfigUtils.configBool(config, "Silencer", silencer);
			disableMuzzleFlash = ConfigUtils.configBool(config, new String[]{"DisableMuzzleFlash", "DisableFlash"}, disableMuzzleFlash);

			//Flashlight settings
			flashlight = ConfigUtils.configBool(config, "Flashlight", flashlight);
			flashlightRange = ConfigUtils.configFloat(config, "FlashlightRange", flashlightRange);
			flashlightStrength = ConfigUtils.configInt(config, "FlashlightStrength", flashlightStrength);

			//Mode override
			String modeOverrideString = ConfigUtils.configString(config, "ModeOverride", null);
			if (modeOverrideString != null) {
				modeOverride = EnumFireMode.getFireMode(modeOverrideString);
			}

			//Secondary Stuff
			secondaryFire = ConfigUtils.configBool(config, "SecondaryMode", secondaryFire);

			ArrayList<String[]> splits = ConfigUtils.getSplitsFromKey(config, new String[] { "SecondaryAmmo" });
			try {
				for (String[] split : splits) {
					if (split.length == 2) {
						secondaryAmmo.add(split[1]);
					} else {
						FlansMod.logPackError(file.name, packName, shortName, "SecondaryAmmo in unknown format, skipping", split, null);
					}
				}
			} catch (Exception ex) {
				FlansMod.logPackError(file.name, packName, shortName, "Error thrown while parsing SecondaryMode", null, ex);
			}

			secondaryDamage = ConfigUtils.configFloat(config, "SecondaryDamage", secondaryDamage);
			secondarySpread = ConfigUtils.configFloat(config, new String[]{"SecondarySpread", "SecondaryAccuracy"}, secondarySpread);
			secondarySpeed = ConfigUtils.configFloat(config, "SecondaryBulletSpeed", secondarySpeed);
			secondaryShootDelay = ConfigUtils.configInt(config, "SecondaryShootDelay", secondaryShootDelay);
			secondaryReloadTime = ConfigUtils.configInt(config, "SecondaryReloadTime", secondaryReloadTime);
			secondaryShootDelay = ConfigUtils.configInt(config, "SecondaryShootDelay", secondaryShootDelay);
			secondaryNumBullets = ConfigUtils.configInt(config, "SecondaryNumBullets", secondaryNumBullets);
			numSecAmmoItems = ConfigUtils.configInt(config, "LoadSecondaryIntoGun", numSecAmmoItems);

			String secondaryFireModeString = ConfigUtils.configString(config, "SecondaryFireMode", null);
			if (secondaryFireModeString != null) {
				secondaryFireMode = EnumFireMode.getFireMode(secondaryFireModeString);
			}

			secondaryShootSound = ConfigUtils.configGunSound(packName, config, "SecondaryShootSound", secondaryShootSound);
			secondaryReloadSound = ConfigUtils.configGunSound(packName, config, "SecondaryReloadSound", secondaryReloadSound);
			toggleSound = ConfigUtils.configGunSound(packName, config, "ModeSwitchSound", toggleSound);

			//Multipliers
			meleeDamageMultiplier = ConfigUtils.configFloat(config, "MeleeDamageMultiplier", meleeDamageMultiplier);
			damageMultiplier = ConfigUtils.configFloat(config, "DamageMultiplier", damageMultiplier);
			spreadMultiplier = ConfigUtils.configFloat(config, "SpreadMultiplier", spreadMultiplier);
			recoilMultiplier = ConfigUtils.configFloat(config, "RecoilMultiplier", recoilMultiplier);
			recoilControlMultiplier = ConfigUtils.configFloat(config, "RecoilControlMultiplier", recoilControlMultiplier);
			recoilControlMultiplierSneaking = ConfigUtils.configFloat(config, "RecoilControlMultiplierSneaking", recoilControlMultiplierSneaking);
			recoilControlMultiplierSprinting = ConfigUtils.configFloat(config, "RecoilControlMultiplierSprinting", recoilControlMultiplierSprinting);
			bulletSpeedMultiplier = ConfigUtils.configFloat(config, "BulletSpeedMultiplier", bulletSpeedMultiplier);
			recoilControlMultiplierSprinting = ConfigUtils.configFloat(config, "RecoilControlMultiplierSprinting", recoilControlMultiplierSprinting);
			moveSpeedMultiplier = ConfigUtils.configFloat(config, new String[]{"MovementSpeedMultiplier", "MoveSpeedModifier"}, moveSpeedMultiplier);

			//Scope Variables
			minZoom = ConfigUtils.configFloat(config, "MinZoom", minZoom);
			maxZoom = ConfigUtils.configFloat(config, "MaxZoom", maxZoom);
			zoomAugment = ConfigUtils.configFloat(config, "ZoomAugment", zoomAugment);
			zoomLevel = ConfigUtils.configFloat(config, "ZoomLevel", zoomLevel);
			FOVZoomLevel = ConfigUtils.configFloat(config, "FOVZoomLevel", FOVZoomLevel);

			String zoomOverlayString = ConfigUtils.configString(config, "ZoomOverlay", null);
			if (zoomOverlayString == null || zoomOverlayString.isEmpty() || zoomOverlayString.equalsIgnoreCase("None")) {
				hasScopeOverlay = false;
			} else {
				hasScopeOverlay = true;
				zoomOverlay = zoomOverlayString;
			}
			hasNightVision = ConfigUtils.configBool(config, "HasNightVision", hasNightVision);

		} catch (Exception e) {
			FlansMod.logPackError(file.name, packName, shortName, "Fatal error reading attachment config", null, e);
			isValid = false;
		}
	}
	
	/** To be overriden by subtypes for model reloading */
	public void reloadModel()
	{
		model = FlansMod.proxy.loadModel(modelString, shortName, ModelAttachment.class);
	}
	
	public static AttachmentType getFromNBT(NBTTagCompound tags)
	{
		ItemStack stack = ItemStack.loadItemStackFromNBT(tags);
		if(stack != null && stack.getItem() instanceof ItemAttachment)
			return ((ItemAttachment)stack.getItem()).type;
		return null;
	}

	@Override
	public float getZoomFactor() 
	{
		return zoomLevel;
	}

	@Override
	public boolean hasZoomOverlay() 
	{
		return hasScopeOverlay;
	}

	@Override
	public String getZoomOverlay() 
	{
		return zoomOverlay;
	}

	@Override
	public float getFOVFactor() 
	{ 
		return FOVZoomLevel;
	}

	public static AttachmentType getAttachment(String s)
	{
		for(AttachmentType attachment : attachments)
		{
			if(attachment.shortName.equals(s))
				return attachment;
		}
		return null;
	}
	
	@Override
	public float GetRecommendedScale()
	{
		return 100.0f;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBase GetModel() 
	{
		return model;
	}

	/**Variable Zoom*/

	@Override
	public float getMinZoom(){
		return hasVariableZoom?minZoom:-1F;
	}
	@Override
	public float getMaxZoom(){
		return hasVariableZoom?maxZoom:-1F;
	}
	@Override
	public float getZoomAugment(){
		return hasVariableZoom?zoomAugment:-1F;
	}
	@Override
	public boolean hasVariableZoom() {
		return hasVariableZoom;
	}
}
