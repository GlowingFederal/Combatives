package com.flansmod.common.guns;

import java.util.ArrayList;
import java.util.HashMap;

import com.flansmod.utils.ConfigMap;
import com.flansmod.utils.ConfigUtils;
import net.minecraft.client.model.ModelBase;
import net.minecraft.potion.PotionEffect;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.TypeFile;

public class GrenadeType extends ShootableType
{
	public static ArrayList<GrenadeType> grenades = new ArrayList<GrenadeType>();

	//Misc
	/** The damage imparted by smacking someone over the head with this grenade */
	public int meleeDamage = 1;

	//Throwing
	/** The delay between subsequent grenade throws */
	public int throwDelay = 0;
	/** The sound to play upon throwing this grenade */
	public String throwSound = "";
	/** The name of the item to drop (if any) when throwing the grenade */
	public String dropItemOnThrow = null;
	/** Whether you can throw this grenade by right clicking */
	public boolean canThrow = true;

	//Physics
	/** Whether this grenade may pass through entities or blocks */
	public boolean penetratesEntities = false, penetratesBlocks = false;
	/** The sound to play upon bouncing off a surface */
	public String bounceSound = "";
	/** Whether the grenade should stick to surfaces */
	public boolean sticky = false;
	/** If true, then the grenade will stick to the player that threw it. Used to make delayed self destruct weapons */
	public boolean stickToThrower = false;

	public boolean stickToEntity = false;
	public boolean stickToDriveable = false;
	public boolean stickToEntityAfter = false;
	public boolean allowStickSound = false;
	public int stickSoundRange = 10;
	public String stickSound;

	public boolean flashBang = false;
	public int flashTime = 200;
	public int flashRange = 8;

	public boolean flashSoundEnable = false;
	public int flashSoundRange = 16;
	public String flashSound;

	public boolean flashDamageEnable = false;
	public float flashDamage;

	public boolean flashEffects = false;
	public int flashEffectsID;
	public int flashEffectsDuration;
	public int flashEffectsLevel;

	public boolean motionSensor = false;
	public float motionSensorRange = 5.0F;
	public float motionSoundRange = 20.0F;
	public String motionSound;
	public int motionTime = 20;

	//Conditions for detonation
	/** If > 0 this will act like a mine and explode when a living entity comes within this radius of the grenade */
	public float livingProximityTrigger = -1F;
	/** If > 0 this will act like a mine and explode when a driveable comes within this radius of the grenade */
	public float driveableProximityTrigger = -1F;
	/**  If true, then anything attacking this entity will detonate it */
	public boolean detonateWhenShot = false;
	/** If true, then this grenade can be detonated by any remote detonator tool */
	public boolean remote = false;
	/** How much damage to deal to the entity that triggered it */
	public float damageToTriggerer = 0F;

	//Detonation
	/** Detonation will not occur until after this time */
	public int primeDelay = 0;
	//public boolean antiAirMine = true;
	//public int antiAirMineAngle = 10;

	//Aesthetics
	/** Particles given off in the detonation */
	public int explodeParticles = 0;
	public String explodeParticleType = "largesmoke";
	/** Whether the grenade should spin when thrown. Generally false for mines or things that should lie flat */
	public boolean spinWhenThrown = true;

	//Smoke
	/** Time to remain after detonation */
	public int smokeTime = 0;
	/** Particles given off after detonation */
	public String smokeParticleType = "explode";
	/** The effects to be given to people coming too close */
	public ArrayList<PotionEffect> smokeEffects = new ArrayList<PotionEffect>();
	/** The radius for smoke effects to take place in */
	public float smokeRadius = 5F;

	//Deployed bag functionality
	/** If true, then right clicking this "grenade" will give the player health or buffs or ammo as defined below */
	public boolean isDeployableBag = false;
	/** The number of times players can use this bag before it runs out */
	public int numUses = 1;
	/** The amount to heal the player using this bag */
	public float healAmount = 0;
	/** The potion effects to apply to users of this bag */
	public ArrayList<PotionEffect> potionEffects = new ArrayList<PotionEffect>();
	/** The number of clips to give to the player when using this bag
	 * When they right click with a gun, they will get this number of clips for that gun.
	 * They get the first ammo type, as listed in the gun type file
	 * The number of clips they get is multiplied by numBulletsInGun too
	 */
	public int numClips = 0;

	public GrenadeType(TypeFile file) {
		super(file);
	}

	@Override
	protected void preRead(TypeFile file) 
	{
		super.preRead(file);
	}

	@Override
	public void postRead(TypeFile file) {
		super.postRead(file);

		if (this.shortName != null && isValid) {
			grenades.add(this);
		}
	}

	@Override
	protected void read(ConfigMap config, TypeFile file) {
		super.read(config, file);
		try {
			meleeDamage = ConfigUtils.configInt(config, "MeleeDamage", meleeDamage);
			//Grenade Throwing
			throwDelay = ConfigUtils.configInt(config, "ThrowDelay", throwDelay);
			meleeDamage = ConfigUtils.configInt(config, "MeleeDamage", meleeDamage);
			throwSound = ConfigUtils.configString(config, "ThrowSound", throwSound);
			dropItemOnThrow = ConfigUtils.configString(config, "DropItemOnThrow", dropItemOnThrow);
			canThrow = ConfigUtils.configBool(config, "CanThrow", canThrow);

			//Grenade Physics
			penetratesEntities = ConfigUtils.configBool(config, "PenetratesEntities", penetratesEntities);
			penetratesBlocks = ConfigUtils.configBool(config, "PenetratesBlocks", penetratesBlocks);

			bounceSound = ConfigUtils.configString(config, "BounceSound", bounceSound);
			livingProximityTrigger = ConfigUtils.configFloat(config, "LivingProximityTrigger", livingProximityTrigger);
			driveableProximityTrigger = ConfigUtils.configFloat(config, "VehicleProximityTrigger", driveableProximityTrigger);
			damageToTriggerer = ConfigUtils.configFloat(config, "DamageToTriggerer", damageToTriggerer);
			primeDelay = ConfigUtils.configInt(config, new String[]{"PrimeDelay", "TriggerDelay"}, primeDelay);

			//Sticky settings
			sticky = ConfigUtils.configBool(config, "Sticky", sticky);
			stickToThrower = ConfigUtils.configBool(config, "StickToThrower", stickToThrower);
			stickToEntity = ConfigUtils.configBool(config, "StickToEntity", stickToEntity);
			stickToDriveable = ConfigUtils.configBool(config, "StickToDriveable", stickToDriveable);
			stickToEntityAfter = ConfigUtils.configBool(config, "StickToEntityAfter", stickToEntityAfter);
			allowStickSound = ConfigUtils.configBool(config, "AllowStickSound", allowStickSound);
			stickSoundRange = ConfigUtils.configInt(config, "StickSoundRange", stickSoundRange);
			stickSound = ConfigUtils.configSound(packName, config, "StickSound", stickSound);

			explodeParticles = ConfigUtils.configInt(config, "NumExplodeParticles", explodeParticles);
			explodeParticleType = ConfigUtils.configString(config, "ExplodeParticles", explodeParticleType);
			smokeTime = ConfigUtils.configInt(config, "SmokeTime", smokeTime);
			explodeParticles = ConfigUtils.configInt(config, "NumExplodeParticles", explodeParticles);
			smokeParticleType = ConfigUtils.configString(config, "SmokeParticles", smokeParticleType);

			ArrayList<String[]> splits = ConfigUtils.getSplitsFromKey(config, new String[] { "SmokeEffect" });
			for (String[] split : splits) {
				try {
					smokeEffects.add(getPotionEffect(split));
				} catch (Exception ex) {
					FlansMod.logPackError(file.name, packName, shortName, "Couldn't add SmokeEffect for grenade", split, ex);
				}
			}

			smokeRadius = ConfigUtils.configFloat(config, "SmokeRadius", smokeRadius);
			spinWhenThrown = ConfigUtils.configBool(config, "SpinWhenThrown", spinWhenThrown);
			remote = ConfigUtils.configBool(config, "Remote", remote);

			flashBang = ConfigUtils.configBool(config, "FlashBang", flashBang);
			flashTime = ConfigUtils.configInt(config, "FlashTime", flashTime);
			flashRange = ConfigUtils.configInt(config, "FlashRange", flashRange);
			flashSoundEnable = ConfigUtils.configBool(config, "FlashSoundEnable", flashSoundEnable);
			flashSoundRange = ConfigUtils.configInt(config, "FlashSoundRange", flashSoundRange);
			flashSound = ConfigUtils.configSound(packName, config, "FlashSound", flashSound);
			flashDamageEnable = ConfigUtils.configBool(config, "FlashDamageEnable", flashDamageEnable);
			flashDamage = ConfigUtils.configFloat(config, "FlashDamage", flashDamage);
			flashEffects = ConfigUtils.configBool(config, "FlashEffects", flashEffects);
			flashEffectsID = ConfigUtils.configInt(config, "FlashEffectsID", flashEffectsID);
			flashEffectsDuration = ConfigUtils.configInt(config, "FlashEffectsDuration", flashEffectsDuration);
			flashEffectsLevel = ConfigUtils.configInt(config, "FlashEffectsLevel", flashEffectsLevel);
			flashBang = ConfigUtils.configBool(config, "FlashBang", flashBang);

			motionSensor = ConfigUtils.configBool(config, "MotionSensor", motionSensor);
			motionSensorRange = ConfigUtils.configFloat(config, "MotionSensorRange", motionSensorRange);
			motionSoundRange = ConfigUtils.configFloat(config, "MotionSoundRange", motionSoundRange);
			motionSound = ConfigUtils.configSound(packName, config, "MotionSound", motionSound);
			motionTime = ConfigUtils.configInt(config, "MotionTime", motionTime);

			detonateWhenShot = ConfigUtils.configBool(config, "DetonateWhenShot", detonateWhenShot);


			//Deployable Bag Stuff
			if(config.containsKey("DeployableBag"))
				isDeployableBag = true;

			numUses = ConfigUtils.configInt(config, "NumUses", numUses);
			healAmount = ConfigUtils.configFloat(config, "HealAmount", healAmount);

			splits = ConfigUtils.getSplitsFromKey(config, new String[] { "AddPotionEffect", "PotionEffect" });
			for (String[] split : splits) {
				try {
					potionEffects.add(getPotionEffect(split));
				} catch (Exception ex) {
					FlansMod.logPackError(file.name, packName, shortName, "Couldn't add PotionEffect", split, ex);
				}
			}

			numClips = ConfigUtils.configInt(config, "NumClips", numClips);
		} catch (Exception ex) {
			FlansMod.logPackError(file.name, packName, shortName, "Fatal error occurred while reading grenade file", null, ex);
			isValid = false;
		}
	}

	public static GrenadeType getGrenade(String s) {
		for(GrenadeType grenade : grenades) {
			if(grenade.shortName.equals(s))
				return grenade;
		}
		return null;
	}

	/** To be overriden by subtypes for model reloading */
	public void reloadModel()
	{
		model = FlansMod.proxy.loadModel(modelString, shortName, ModelBase.class);
	}
}
