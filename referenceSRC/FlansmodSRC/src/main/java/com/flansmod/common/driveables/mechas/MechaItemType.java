package com.flansmod.common.driveables.mechas;

import java.util.ArrayList;
import java.util.HashMap;

import com.flansmod.utils.ConfigMap;
import com.flansmod.utils.ConfigUtils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;

import com.flansmod.client.model.ModelMechaTool;
import com.flansmod.common.FlansMod;
import com.flansmod.common.types.InfoType;
import com.flansmod.common.types.TypeFile;

public class MechaItemType extends InfoType 
{
	//public static ArrayList<MechaItemType> types = new ArrayList<MechaItemType>();
	
	/** The type of item */
	public EnumMechaItemType type;
	/** If this is a tool, then what type of tool is this? Axe? Pick? */
	public EnumMechaToolType function = EnumMechaToolType.sword;
	/** How quickly this tool works */
	public float speed = 1F;
	/** The maximum block hardness you can break with this tool */
	public float toolHardness = 1F;
	/** This is multiplied by the mecha reach to calculate the total reach */
	public float reach = 1F;
	/** This makes the mecha float towards the surface when underwater, because apparently people prefer limited functionality */
	public boolean floater = false;
	/** This allows an upgrade to affect the mecha's move speed */
	public float speedMultiplier = 1F;
	/** This allows upgrades to reduce incoming damage */
	public float damageResistance = 0F;
	/** This allows a sound to be played upon use (RocketPack only for the moment) */	
	public String soundEffect = "";
	public String detectSound = "";
	public float soundTime = 0;
	public int energyShield = 0;
	public int lightLevel = 0;
	/** The following are a ton of upgrade flags and modifiers. The mecha will iterate over all upgrades in its
		inventory multiplying multipliers and looking for true booleans in order to decide if things should happen
		or what certain values should take
	*/
	public boolean stopMechaFallDamage = false, forceBlockFallDamage = false, vacuumItems = false, refineIron = false, autoCoal = false, autoRepair = false, rocketPack = false, diamondDetect = false, infiniteAmmo = false, forceDark = false, wasteCompact = false, flameBurst = false;
	
	public float autoRepairAmount = 1F;

	/** The drop rate of these items are multiplied by this float. They stack between items too. 
	 * Once dropRate has been calculated, each block then gives floor(dropRate) items with a 
	 * dropRate - floor(dropRate) chance of getting one more */
	public float fortuneDiamond = 1F, fortuneRedstone = 1F, fortuneCoal = 1F, fortuneEmerald = 1F, fortuneIron = 1F;
	
	/** The power of any attached jet pack is multiplied by this float */
	public float rocketPower = 1F;
	
	/** The model */
	@SideOnly(Side.CLIENT)
	public ModelMechaTool model;
	
	public MechaItemType(TypeFile file)
	{
		super(file);
	}
	
	@Override
	protected void preRead(TypeFile file) { }

	@Override
	protected void postRead(TypeFile file) { }
	
    @Override
	protected void read(ConfigMap config, TypeFile file) {
		super.read(config, file);
		try {
			if (FMLCommonHandler.instance().getSide().isClient()) {
				model = FlansMod.proxy.loadModel(modelString, shortName, ModelMechaTool.class);
			}

			texture = ConfigUtils.configString(config, "Texture", texture);

			String typeString = ConfigUtils.configString(config, "Type", "nothing");
			if (!typeString.isEmpty())
				type = EnumMechaItemType.getToolType(typeString);

			String toolTypeString = ConfigUtils.configString(config, "ToolType", "nothing");
			if(!toolTypeString.isEmpty())
				function = EnumMechaToolType.getToolType(toolTypeString);

			speed = ConfigUtils.configFloat(config, "Speed", speed);
			toolHardness = ConfigUtils.configFloat(config, "ToolHardness", toolHardness);
			reach = ConfigUtils.configFloat(config, "Reach", reach);

			/** The following are the upgrade booleans and multipliers, which
			 *  are alphabetised. Mess with the order at your peril*/

			autoCoal = ConfigUtils.configBool(config, "AutoFuel", autoCoal);
			damageResistance = ConfigUtils.configFloat(config, "Armour", damageResistance);
			fortuneCoal = ConfigUtils.configFloat(config, "CoalMultiplier", fortuneCoal);
			diamondDetect = ConfigUtils.configBool(config, "DiamondDetect", diamondDetect);
			fortuneDiamond = ConfigUtils.configFloat(config, "DiamondMultiplier", fortuneDiamond);
			fortuneEmerald = ConfigUtils.configFloat(config, "EmeraldMultiplier", fortuneEmerald);
			flameBurst = ConfigUtils.configBool(config, "FlameBurst", flameBurst);
			floater = ConfigUtils.configBool(config, "Floatation", floater);
			forceBlockFallDamage = ConfigUtils.configBool(config, "ForceBlockFallDamage", forceBlockFallDamage);
			forceDark = ConfigUtils.configBool(config, "ForceDark", forceDark);
			infiniteAmmo = ConfigUtils.configBool(config, "InfiniteAmmo", infiniteAmmo);
			fortuneIron = ConfigUtils.configFloat(config, "IronMultiplier", fortuneIron);
			refineIron = ConfigUtils.configBool(config, "IronRefine", refineIron);
			vacuumItems = ConfigUtils.configBool(config, "ItemVacuum", vacuumItems);
			lightLevel = ConfigUtils.configInt(config, "LightLevel", lightLevel);
			autoRepair = ConfigUtils.configBool(config, "Nanorepair", autoRepair);
			autoRepairAmount = ConfigUtils.configFloat(config, "NanorepairAmount", autoRepairAmount);
			fortuneRedstone = ConfigUtils.configFloat(config, "RedstoneMultiplier", fortuneRedstone);
			rocketPack = ConfigUtils.configBool(config, "RocketPack", rocketPack);
			rocketPower = ConfigUtils.configFloat(config, "RocketPower", rocketPower);
			soundEffect = ConfigUtils.configString(config, "SoundEffect", soundEffect);
			soundTime = ConfigUtils.configFloat(config, "SoundTime", soundTime);
			speedMultiplier = ConfigUtils.configFloat(config, "SpeedMultiplier", speedMultiplier);
			stopMechaFallDamage = ConfigUtils.configBool(config, "StopMechaFallDamage", stopMechaFallDamage);
			wasteCompact = ConfigUtils.configBool(config, "WasteCompact", wasteCompact);
		} catch (Exception ex) {
			FlansMod.logPackError(file.name, packName, shortName, "Fatal error occurred while reading Mecha Item file", null, ex);
			isValid = false;
		}
    }
	
	public void reloadModel() {
		model = FlansMod.proxy.loadModel(modelString, shortName, ModelMechaTool.class);
	}

	@Override
	public float GetRecommendedScale() 
	{
		return 0.0f;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBase GetModel() 
	{
		return null;
	}

}
