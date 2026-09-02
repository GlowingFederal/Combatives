package com.flansmod.common.guns;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.InfoType;
import com.flansmod.common.types.TypeFile;
import com.flansmod.utils.ConfigMap;
import com.flansmod.utils.ConfigUtils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;

import java.util.HashMap;

public abstract class ShootableType extends InfoType {
    //Aesthetics
    /**
     * The model to render for this grenade in the world
     */
    public ModelBase model;
    /**
     * Whether trail particles are given off
     */
    public boolean trailParticles = false;
    /**
     * Trail particles given off by this while being thrown
     */
    public String trailParticleType = "smoke";

    // hasLight controls whether it has full luminescence.
    // hasDynamicLight controls if it lights up the area around it.
    public boolean hasLight = false;
    public boolean hasDynamicLight = false;

    //Item Stuff
    /**
     * The maximum number of grenades that can be stacked together
     */
    public int maxStackSize = 1;
    /**
     * Items dropped on various events
     */
    public String dropItemOnReload = null, dropItemOnShoot = null, dropItemOnHit = null;
    /**
     * The number of rounds fired by a gun per item
     */
    public int roundsPerItem = 1;

    //Physics and Stuff
    /**
     * The speed at which the grenade should fall
     */
    public float fallSpeed = 1.0F;
    /**
     * The speed at which to throw the grenade. 0 will just drop it on the floor
     */
    public float throwSpeed = 1.0F;
    /**
     * Hit box size
     */
    public float hitBoxSize = 0.5F;
    /** Upon hitting a block or entity, the grenade will be deflected and its motion will be multiplied by this constant */
    public float bounciness = 0.9F;

    //Damage to hit entities
    /**
     * Amount of damage to impart upon various entities
     */
    public float damageVsPlayer = 1.0F;
    public float damageVsEntity = 1.0F;
    public float damageVsLiving = 1.0F;
    public float damageVsVehicles = 1.0F;
    public float damageVsPlanes = 1.0F;
    public boolean readDamageVsPlayer = false;
    public boolean readDamageVsEntity = false;
    public boolean readDamageVsPlanes = false;
    /**
     * Whether this grenade will break glass when thrown against it
     */
    public boolean breaksGlass = false;
    public float ignoreArmorProbability = 0;
    public float ignoreArmorDamageFactor = 0;

    private float blockPenetrationModifier = -1;

    //Detonation Conditions
    /**
     * If 0, then the grenade will last until some other detonation condition is met, else the grenade will detonate after this time (in ticks)
     */
    public int fuse = 0;
    /**
     * After this time the grenade will despawn quietly. 0 means no despawn time
     */
    public int despawnTime = 0;
    /**
     * If true, then this will explode upon hitting something
     */
    public boolean explodeOnImpact = false;

    //Detonation Stuff
    /**
     * The radius in which to spread fire
     */
    public float fireRadius = 0F;
    /**
     * The radius of explosion upon detonation
     */
    public float explosionRadius = 0F;
    /**
     * Power of explosion. Multiplier, 1 = vanilla behaviour
     */
    public float explosionPower = 1F;
    /**
     * Whether the explosion can destroy blocks
     */
    public boolean explosionBreaksBlocks = true;
    /**
     * Explosion damage vs various classes of entities
     */
    public float explosionDamageVsLiving = 1.0F;
    public float explosionDamageVsPlayer = 1.0F;
    public float explosionDamageVsPlane = 1.0F;
    public float explosionDamageVsVehicle = 1.0F;
    /**
     * The name of the item to drop upon detonating
     */
    public String dropItemOnDetonate = null;
    /**
     * Sound to play upon detonation
     */
    public String detonateSound = "";

    public boolean hasSubmunitions = false;
    public String submunition = "";
    public int numSubmunitions = 0;
    public int subMunitionTimer = 0;
    public float submunitionSpread = 1;
    public boolean destroyOnDeploySubmunition = false;

    public int smokeParticleCount = 0;
    public int debrisParticleCount = 0;

    /**
     * The static list of all shootable types
     */
    public static HashMap<String, ShootableType> shootables = new HashMap<String, ShootableType>();

    public ShootableType(TypeFile file) {
        super(file);
    }

    @Override
    protected void preRead(TypeFile file) {
    }

    @Override
    public void postRead(TypeFile file) {
        if (shootables.containsKey(shortName)) {
            FlansMod.logPackError(file.name, packName, shortName, "Shootable with shortname already exists!", null, null);
        }

        if (this.shortName != null && isValid) {
            shootables.put(shortName, this);
        }

        if (!readDamageVsPlayer) {
            damageVsPlayer = damageVsLiving;
        } if (!readDamageVsEntity) {
            damageVsEntity = damageVsVehicles;
        } if (!readDamageVsPlanes) {
            damageVsPlanes = damageVsVehicles;
        }
    }

    @Override
    protected void read(ConfigMap config, TypeFile file) {
        super.read(config, file);

        try {
            //Model and Texture
            if (FMLCommonHandler.instance().getSide().isClient()) {
                model = FlansMod.proxy.loadModel(modelString, shortName, ModelBase.class);
            }

            texture = ConfigUtils.configString(config, "Texture", texture);

            //Item Stuff
            maxStackSize = ConfigUtils.configInt(config, new String[]{"StackSize", "MaxStackSize"}, maxStackSize);
            dropItemOnShoot = ConfigUtils.configString(config, "DropItemOnShoot", dropItemOnShoot);
            dropItemOnReload = ConfigUtils.configString(config, "DropItemOnReload", dropItemOnReload);
            dropItemOnHit = ConfigUtils.configString(config, "DropItemOnHit", dropItemOnHit);
            roundsPerItem = ConfigUtils.configInt(config, "RoundsPerItem", roundsPerItem);

            //Physics
            fallSpeed = ConfigUtils.configFloat(config, "FallSpeed", fallSpeed);
            throwSpeed = ConfigUtils.configFloat(config, new String[]{"ThrowSpeed", "ShootSpeed"}, throwSpeed);
            hitBoxSize = ConfigUtils.configFloat(config, "HitBoxSize", hitBoxSize);

            //Hit stuff - goofy logic, idk why.
            if (config.containsKey("Damage")) {
                damageVsLiving = damageVsPlayer = damageVsEntity = damageVsPlanes = damageVsVehicles = ConfigUtils.configFloat(config, "Damage", 0F);
            }

            damageVsLiving = ConfigUtils.configFloat(config, "DamageVsLiving", damageVsLiving);

            if (config.containsKey("DamageVsPlayer")) {
                damageVsPlayer = ConfigUtils.configFloat(config, "DamageVsPlayer", damageVsPlayer);
                readDamageVsPlayer = true;
            }

            if (config.containsKey("DamageVsEntity")) {
                damageVsEntity = ConfigUtils.configFloat(config, "DamageVsEntity", damageVsEntity);
                readDamageVsEntity = true;
            }

            damageVsVehicles = ConfigUtils.configFloat(config, "DamageVsVehicles", damageVsVehicles);

            if (config.containsKey("DamageVsPlanes")) {
                damageVsPlanes = ConfigUtils.configFloat(config, "DamageVsPlanes", damageVsPlanes);
                readDamageVsPlanes = true;
            }


            blockPenetrationModifier = ConfigUtils.configFloat(config, "BlockPenetrationModifier", blockPenetrationModifier);
            ignoreArmorProbability = ConfigUtils.configFloat(config, "IgnoreArmorProbability", ignoreArmorProbability);
            ignoreArmorDamageFactor = ConfigUtils.configFloat(config, "IgnoreArmorDamageFactor", ignoreArmorDamageFactor);
            breaksGlass = ConfigUtils.configBool(config, "BreaksGlass", breaksGlass);
            bounciness = ConfigUtils.configFloat(config, "Bounciness", bounciness);
            hasLight = ConfigUtils.configBool(config, "HasLight", hasLight);
            hasDynamicLight = ConfigUtils.configBool(config, "HasDynamicLight", hasDynamicLight);

            //Detonation conditions etc
            fuse = ConfigUtils.configInt(config, "Fuse", fuse);
            despawnTime = ConfigUtils.configInt(config, "DespawnTime", despawnTime);
            explodeOnImpact = ConfigUtils.configBool(config, new String[]{"ExplodeOnImpact", "DetonateOnImpact"}, explodeOnImpact);

            //Detonation
            fireRadius = ConfigUtils.configFloat(config, new String[]{"FireRadius", "Fire"}, fireRadius);
            explosionRadius = ConfigUtils.configFloat(config, new String[]{"ExplosionRadius", "Explosion"}, explosionRadius);
            explosionPower = ConfigUtils.configFloat(config, "ExplosionPower", explosionPower);
            explosionBreaksBlocks = ConfigUtils.configBool(config, new String[]{"ExplosionBreaksBlocks", "ExplosionsBreaksBlocks", "ExplosionBreakBlocks", "ExplosionsBreakBlocks"}, explosionBreaksBlocks);
            explosionDamageVsLiving = ConfigUtils.configFloat(config, "ExplosionDamageVsLiving", explosionDamageVsLiving);
            explosionDamageVsPlayer = ConfigUtils.configFloat(config, "ExplosionDamageVsPlayer", explosionDamageVsPlayer);
            explosionDamageVsPlane = ConfigUtils.configFloat(config, "ExplosionDamageVsPlane", explosionDamageVsPlane);
            explosionDamageVsVehicle = ConfigUtils.configFloat(config, "ExplosionDamageVsVehicle", explosionDamageVsVehicle);
            dropItemOnDetonate = ConfigUtils.configString(config, "DropItemOnDetonate", dropItemOnDetonate);
            detonateSound = ConfigUtils.configString(config, "DetonateSound", detonateSound);

            //Submunitions
            hasSubmunitions = ConfigUtils.configBool(config, "HasSubmunitions", hasSubmunitions);
            submunition = ConfigUtils.configString(config, "Submunition", submunition);
            numSubmunitions = ConfigUtils.configInt(config, "NumSubmunitions", numSubmunitions);
            subMunitionTimer = ConfigUtils.configInt(config, "SubmunitionDelay", subMunitionTimer);
            submunitionSpread = ConfigUtils.configFloat(config, "SubmunitionSpread", submunitionSpread);
            smokeParticleCount = ConfigUtils.configInt(config, "FlareParticleCount", smokeParticleCount);
            debrisParticleCount = ConfigUtils.configInt(config, "DebrisParticleCount", debrisParticleCount);

            //Particles
            trailParticles = ConfigUtils.configBool(config, new String[]{"TrailParticles", "SmokeTrail"}, trailParticles);
            trailParticleType = ConfigUtils.configString(config, "TrailParticleType", trailParticleType);
        } catch (Exception ex) {
            FlansMod.logPackError(file.name, packName, shortName, "Fatal error while reading Shootable", null, ex);
            isValid = false;
        }
    }

    public static ShootableType getShootableType(String string) {
        return shootables.get(string);
    }

    @Override
    public float GetRecommendedScale() {
        return 0.0f;
    }

    public float getBlockPenetrationModifier() {
    	return blockPenetrationModifier < 0 ? FlansMod.masterBlockPenetrationModifier : blockPenetrationModifier;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBase GetModel() {
        return model;
    }
}
