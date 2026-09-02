package com.flansmod.common.guns;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.flansmod.utils.ConfigMap;
import com.flansmod.utils.ConfigUtils;
import net.minecraft.client.model.ModelBase;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;

import com.flansmod.common.FlansMod;
import com.flansmod.common.driveables.EnumWeaponType;
import com.flansmod.common.types.TypeFile;

public class BulletType extends ShootableType
{
	public float speedMultiplier = 1F;
	/** The number of flak particles to spawn upon exploding */
	public int flak = 0;
	/** The type of flak particles to spawn */
	public String flakParticles = "largesmoke";

	/** If true then this bullet will burn entites it hits */
	public boolean setEntitiesOnFire = false;

	/** If > 0 this will act like a mine and explode when a living entity comes within this radius of the grenade */
	public float livingProximityTrigger = -1F;
	/** If > 0 this will act like a mine and explode when a driveable comes within this radius of the grenade */
	public float driveableProximityTrigger = -1F;
	/** How much damage to deal to the entity that triggered it */
	public float damageToTriggerer = 0F;
	/** Detonation will not occur until after this time */
	public int primeDelay = 0;
	/** Particles given off in the detonation */
	public int explodeParticles = 0;
	public String explodeParticleType = "largesmoke";

	/** Exclusively for driveable usage. Replaces old isBomb and isShell booleans with something more flexible */
	public EnumWeaponType weaponType = EnumWeaponType.NONE;

	public String hitSound;
	public float hitSoundRange = 64;
	public boolean hitSoundEnable = false;
	public boolean entityHitSoundEnable = false;

	public float penetratingPower = 1F;
	// In % of penetration to remove per tick.
	public float penetrationDecay = 0F;

	/*
	 * How much the loss of penetration power affects the damage of the bullet. 0 = damage not affected by that kind of penetration,
	 * 1 = damage is fully affected by bullet penetration of that kind
	 */
	public float playerPenetrationEffectOnDamage = 0F;
	public float entityPenetrationEffectOnDamage = 0F;
	public float blockPenetrationEffectOnDamage = 0F;
	public float penetrationDecayEffectOnDamage = 0F;

	// Knocback modifier. less gives less kb, more gives more kb, 1 = normal kb.
	public float knockbackModifier;
	/** Lock on variables. If true, then the bullet will search for a target at the moment it is fired */
	public boolean lockOnToPlanes = false, lockOnToVehicles = false, lockOnToMechas = false, lockOnToPlayers = false, lockOnToLivings = false;
	/** Lock on maximum angle for finding a target */
	public float maxLockOnAngle = 45F;
	/** Lock on force that pulls the bullet towards its prey */
	public float lockOnForce = 1F;
	public int maxDegreeOfMissile = 20;
	public int tickStartHoming = 5;
	public boolean enableSACLOS = false;
	public int maxDegreeOfSACLOS = 5;
	public int maxRangeOfMissile = 150;
	//public int maxDegreeOfMissileXAxis = 10;
	//public int maxDegreeOfMissileYAxis = 10;
	//public int maxDegreeOfMissileZAxis = 10;

	public boolean manualGuidance = false;
	public int lockOnFuse = 10;

	public ArrayList<PotionEffect> hitEffects = new ArrayList<PotionEffect>();

	/** Number of bullets to fire per shot if allowNumBulletsByBulletType = true */
	public int numBullets = -1;
	/** Ammo based spread setting if allowSpreadByBullet = true */
	public float bulletSpread = -1;

	public float dragInAir   = 0.99F;
	public float dragInWater = 0.80F;

	public boolean canSpotEntityDriveable = false;

	public int maxRange = -1;

	public boolean shootForSettingPos = false;
	public int shootForSettingPosHeight = 100;

	public boolean isDoTopAttack = false;
	
	
	//Smoke
	/** Time to remain after detonation */
	public int smokeTime = 0;
	/** Particles given off after detonation */
	
	public String smokeParticleType = "explode";
	/** The effects to be given to people coming too close */
	
	public ArrayList<PotionEffect> smokeEffects = new ArrayList<PotionEffect>();
	/** The radius for smoke effects to take place in */
	
	public float smokeRadius = 5F;
	public boolean TVguide = true;
	
	//Other stuff
	public boolean VLS = false;
	public int VLSTime = 0;
	public boolean fixedDirection = false;
	public float turnRadius = 3;
	public String boostPhaseParticle;
	public float trackPhaseSpeed = 2;
	public float trackPhaseTurn = 0.2F;
	
	public boolean torpedo = false;

	public boolean fancyDescription = true;
	
	public boolean laserGuidance = false;

	// 0 = disable, otherwise sets velocity scale on block hit particle fx
	public float blockHitFXScale;

	/** The static bullets list */
	
	public static List<BulletType> bullets = new ArrayList<BulletType>();


    public BulletType(TypeFile file)
	{
		super(file);
		texture = "defaultBullet";
		bounciness = 0f;
	}

	@Override
	public void postRead(TypeFile file) {
		super.postRead(file);

		if (this.shortName != null && isValid) {
			bullets.add(this);
		}
	}

	@Override
	protected void read(ConfigMap config, TypeFile file) {
		super.read(config, file);
		try {
			flak = ConfigUtils.configInt(config, "FlakParticles", flak);
			flakParticles = ConfigUtils.configString(config, "FlakParticleType", flakParticles);
			setEntitiesOnFire = ConfigUtils.configBool(config, "SetEntitiesOnFire", setEntitiesOnFire);
			hitSoundEnable = ConfigUtils.configBool(config, "HitSoundEnable", hitSoundEnable);
			entityHitSoundEnable = ConfigUtils.configBool(config, "EntityHitSoundEnable", entityHitSoundEnable);
			hitSound = ConfigUtils.configSound(packName, config, "HitSound", hitSound);
			hitSoundRange = ConfigUtils.configFloat(config, "HitSoundRange", hitSoundRange);

			boolean pens = ConfigUtils.configBool(config, "Penetrates", true);
			penetratingPower = pens ? 1F : 0.7F;

			penetratingPower = ConfigUtils.configFloat(config, new String[]{"Penetration", "PenetratingPower"}, penetratingPower);
			penetrationDecay = ConfigUtils.configFloat(config, "PenetrationDecay", penetrationDecay);

			playerPenetrationEffectOnDamage = ConfigUtils.configFloat(config, "PlayerPenetrationDamageEffect", playerPenetrationEffectOnDamage);
			entityPenetrationEffectOnDamage = ConfigUtils.configFloat(config, "EntityPenetrationDamageEffect", entityPenetrationEffectOnDamage);
			blockPenetrationEffectOnDamage = ConfigUtils.configFloat(config, "BlockPenetrationDamageEffect", blockPenetrationEffectOnDamage);
			penetrationDecayEffectOnDamage = ConfigUtils.configFloat(config, "PenetrationDecayDamageEffect", penetrationDecayEffectOnDamage);


			dragInAir = ConfigUtils.configFloat(config, "DragInAir", dragInAir);
			dragInAir = Math.max(0, Math.min(1, dragInAir)); // Clamp to [0, 1]

			dragInWater = ConfigUtils.configFloat(config, "DragInWater", dragInWater);
			dragInWater = Math.max(0, Math.min(1, dragInWater)); // Clamp to [0, 1]

			numBullets = ConfigUtils.configInt(config, "NumBullets", numBullets);
			bulletSpread = ConfigUtils.configFloat(config, new String[]{"Accuracy", "Spread"}, bulletSpread);
			livingProximityTrigger = ConfigUtils.configFloat(config, "LivingProximityTrigger", livingProximityTrigger);
			driveableProximityTrigger = ConfigUtils.configFloat(config, "VehicleProximityTrigger", driveableProximityTrigger);
			damageToTriggerer = ConfigUtils.configFloat(config, "DamageToTriggerer", damageToTriggerer);
			primeDelay = ConfigUtils.configInt(config, new String[]{ "PrimeDelay", "TriggerDelay" }, primeDelay);
			explodeParticles = ConfigUtils.configInt(config, "NumExplodeParticles", explodeParticles);
			explodeParticleType = ConfigUtils.configString(config, "ExplodeParticles", explodeParticleType);
			smokeTime = ConfigUtils.configInt(config, "SmokeTime", smokeTime);
			smokeParticleType = ConfigUtils.configString(config, "SmokeParticles", smokeParticleType);

			ArrayList<String[]> lines = ConfigUtils.getSplitsFromKey(config, new String[] { "SmokeEffect" });
			for (String[] split : lines) {
				try {
					smokeEffects.add(getPotionEffect(split));
				} catch (Exception ex) {
					FlansMod.logPackError(file.name, packName, shortName, "Couldn't read PotionEffect for bullet", split, ex);
				}
			}

			smokeRadius = ConfigUtils.configFloat(config, "SmokeRadius", smokeRadius);
			VLS = ConfigUtils.configBool(config, new String[]{"VLS", "HasDeadZone"}, VLS);
			VLSTime = ConfigUtils.configInt(config, "DeadZoneTime", VLSTime);
			fixedDirection = ConfigUtils.configBool(config, "FixedTrackDirection", fixedDirection);
			turnRadius = ConfigUtils.configFloat(config, "GuidedTurnRadius", turnRadius);
			trackPhaseSpeed = ConfigUtils.configFloat(config, "GuidedPhaseSpeed", trackPhaseSpeed);
			trackPhaseTurn = ConfigUtils.configFloat(config, "GuidedPhaseTurnSpeed", trackPhaseTurn);
			boostPhaseParticle = ConfigUtils.configString(config, "BoostParticle", boostPhaseParticle);
			torpedo = ConfigUtils.configBool(config, "Torpedo", torpedo);


			// Some content packs use 'true' and false after this, which confuses things...
			ArrayList<String[]> splits = ConfigUtils.getSplitsFromKey(config, new String[] { "Bomb" });
			for (String[] split : splits) {
				if (split.length == 1 || (split.length == 2 && split[1].equalsIgnoreCase("true"))) {
					weaponType = EnumWeaponType.BOMB;
				}
			}

			splits = ConfigUtils.getSplitsFromKey(config, new String[] { "Shell" });
			for (String[] split : splits) {
				if (split.length == 1 || (split.length == 2 && split[1].equalsIgnoreCase("true"))) {
					weaponType = EnumWeaponType.SHELL;
				}
			}

			splits = ConfigUtils.getSplitsFromKey(config, new String[] { "Missile" });
			for (String[] split : splits) {
				if (split.length == 1 || (split.length == 2 && split[1].equalsIgnoreCase("true"))) {
					weaponType = EnumWeaponType.MISSILE;
				}
			}

			if (config.containsKey("WeaponType")) {
				String line = ConfigUtils.configString(config, "WeaponType", "Bomb");
				try {
					if (line != null) {
						weaponType = EnumWeaponType.valueOf(line.toUpperCase());
					}
				} catch (Exception ex) {
					FlansMod.logPackError(file.name, packName, shortName, "WeaponType not known in BulletType", new String[] { "WeaponType", line}, ex);
				}
			}

			if (config.containsKey("LockOnToDriveables"))
				lockOnToPlanes = lockOnToVehicles = lockOnToMechas =  ConfigUtils.configBool(config, "LockOnToDriveables", lockOnToVehicles);

			lockOnToVehicles = ConfigUtils.configBool(config, "LockOnToVehicles", lockOnToVehicles);
			lockOnToPlanes = ConfigUtils.configBool(config, "LockOnToPlanes", lockOnToPlanes);
			lockOnToMechas = ConfigUtils.configBool(config, "LockOnToMechas", lockOnToMechas);
			lockOnToPlayers = ConfigUtils.configBool(config, "LockOnToPlayers", lockOnToPlayers);
			lockOnToLivings = ConfigUtils.configBool(config, "LockOnToLivings", lockOnToLivings);

			maxLockOnAngle = ConfigUtils.configFloat(config, "MaxLockOnAngle", maxLockOnAngle);
			lockOnForce = ConfigUtils.configFloat(config, new String[]{"LockOnForce", "TurningForce"}, lockOnForce);
			maxDegreeOfMissile = ConfigUtils.configInt(config, "MaxDegreeOfLockOnMissile", maxDegreeOfMissile);
			tickStartHoming = ConfigUtils.configInt(config, "TickStartHoming", tickStartHoming);
			enableSACLOS = ConfigUtils.configBool(config, "EnableSACLOS", enableSACLOS);
			maxDegreeOfSACLOS = ConfigUtils.configInt(config, "MaxDegreeOFSACLOS", maxDegreeOfSACLOS);
			maxRangeOfMissile = ConfigUtils.configInt(config, "MaxRangeOfMissile", maxRangeOfMissile);
			canSpotEntityDriveable = ConfigUtils.configBool(config, "CanSpotEntityDriveable", canSpotEntityDriveable);
			shootForSettingPos = ConfigUtils.configBool(config, "ShootForSettingPos", shootForSettingPos);
			shootForSettingPosHeight = ConfigUtils.configInt(config, "ShootForSettingPosHeight", shootForSettingPosHeight);
			isDoTopAttack = ConfigUtils.configBool(config, "IsDoTopAttack", isDoTopAttack);
			knockbackModifier = ConfigUtils.configFloat(config, "KnockbackModifier", knockbackModifier);

			lines = ConfigUtils.getSplitsFromKey(config, new String[] { "PotionEffect" });
			for (String[] split : lines) {
				try {
					hitEffects.add(getPotionEffect(split));
				} catch (Exception ex) {
					FlansMod.logPackError(file.name, packName, shortName, "Couldn't read PotionEffect for bullet", split, ex);
				}
			}

			manualGuidance = ConfigUtils.configBool(config, "ManualGuidance", manualGuidance);
			laserGuidance = ConfigUtils.configBool(config, "LaserGuidance", laserGuidance);
			lockOnFuse = ConfigUtils.configInt(config, "LockOnFuse", lockOnFuse);
			maxRange = ConfigUtils.configInt(config, "MaxRange", maxRange);
			fancyDescription = ConfigUtils.configBool(config, "FancyDescription", fancyDescription);
			speedMultiplier = ConfigUtils.configFloat(config, "BulletSpeedMultiplier", speedMultiplier);

			blockHitFXScale = ConfigUtils.configFloat(config, "BlockHitFXScale", (float)((Math.log(explosionRadius + 2) / Math.log(2.15)) + 0.05));

		} catch (Exception e) {
			FlansMod.logPackError(file.name, packName, shortName, "Fatal error reading bullet config", null, e);
			isValid = false;
		}
	}

	public static BulletType getBullet(String s)
	{
		for(BulletType bullet : bullets)
		{
			if(bullet.shortName.equals(s))
				return bullet;
		}
		return null;
	}

	public static BulletType getBullet(Item item)
	{
		for(BulletType bullet : bullets)
		{
			if(bullet.item == item)
				return bullet;
		}
		return null;
	}

	/** To be overriden by subtypes for model reloading */
	public void reloadModel()
	{
		model = FlansMod.proxy.loadModel(modelString, shortName, ModelBase.class);
	}
}
