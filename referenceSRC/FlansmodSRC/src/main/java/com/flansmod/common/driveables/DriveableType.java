package com.flansmod.common.driveables;

import com.flansmod.client.model.ModelDriveable;
import com.flansmod.common.FlansMod;
import com.flansmod.common.driveables.collisions.CollisionShapeBox;
import com.flansmod.common.driveables.mechas.MechaType;
import com.flansmod.common.guns.BulletType;
import com.flansmod.common.guns.EnumFireMode;
import com.flansmod.common.paintjob.PaintableType;
import com.flansmod.common.parts.PartType;
import com.flansmod.common.types.TypeFile;
import com.flansmod.common.vector.Vector3f;
import com.flansmod.utils.ConfigMap;
import com.flansmod.utils.ConfigUtils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.model.ModelBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DriveableType extends PaintableType {
    /**
     * The plane model
     */
    public ModelDriveable model;

    //Health and recipe
    /**
     * Health of each driveable part
     */
    public HashMap<EnumDriveablePart, CollisionBox> health = new HashMap<>();
    public HashMap<EnumDriveablePart, BoxExplosion> partDeathExplosions = new HashMap<>();
    /**
     * Recipe parts associated to each driveable part
     */
    public HashMap<EnumDriveablePart, ItemStack[]> partwiseRecipe = new HashMap<>();
    /**
     * Recipe parts as one complete list
     */
    public ArrayList<ItemStack> driveableRecipe = new ArrayList<>();

    //Ammo
    /**
     * If true, then all ammo is accepted. Default is true to minimise backwards compatibility issues
     */
    public boolean acceptAllAmmo = true;
    /**
     * The list of bullet types that can be used in this driveable for the main gun (tank shells, plane bombs etc)
     */
    public List<BulletType> ammo = new ArrayList<>();

    //Harvesting variables
    /**
     * If true, then this vehicle harvests blocks from the harvester hitbox and places them in the inventory
     */
    public boolean harvestBlocks = false;
    /**
     * What materials this harvester eats
     */
    public ArrayList<Material> materialsHarvested = new ArrayList<>();
    public boolean collectHarvest = false;
    public boolean dropHarvest = false;
    public Vector3f harvestBoxSize = new Vector3f(0, 0, 0);
    public Vector3f harvestBoxPos = new Vector3f(0, 0, 0);
    public int reloadSoundTick = 15214541;
    public float fallDamageFactor = 1.0F;

    public int engineStartTime = 0;

    //Weapon variables
    /**
     * The weapon type assigned to left mouse
     */
    public EnumWeaponType primary = EnumWeaponType.NONE, secondary = EnumWeaponType.NONE;
    /**
     * Whether to alternate weapons or fire all at once
     */
    public boolean alternatePrimary = false, alternateSecondary = false;
    /**
     * Delays. Can override gun delays
     */
    public float shootDelayPrimary = -1, shootDelaySecondary = -1;
    /**
     * Damage multiplies for primary and secondary guns.
     */
    public float damageMultiplierPrimary = 1, damageMultiplierSecondary = 1;
    /**
     * Firing modes for primary and secondary guns. Minigun also an option
     */
    public EnumFireMode modePrimary = EnumFireMode.FULLAUTO, modeSecondary = EnumFireMode.FULLAUTO;
    /**
     * Sounds
     */
    public String shootSoundPrimary, shootSoundSecondary, shootReloadSound;
    /**
     * Positions of primary and secondary weapons
     */
    public ArrayList<ShootPoint> shootPointsPrimary = new ArrayList<>(), shootPointsSecondary = new ArrayList<>();
    /**
     * Pilot guns also have their own separate array so ammo handling can be done
     */
    public ArrayList<PilotGun> pilotGuns = new ArrayList<>();
    public int reloadTimePrimary = 0, reloadTimeSecondary = 0;
    public String reloadSoundPrimary = "", reloadSoundSecondary = "";
    public int placeTimePrimary = 5, placeTimeSecondary = 5;
    public String placeSoundPrimary = "", placeSoundSecondary = "";
    //Passengers
    /**
     * The number of passengers, not including the pilot
     */
    public int numPassengers = 0;
    /**
     * Seat objects for holding information about the position and gun setup of each seat
     */
    public Seat[] seats;
    /**
     * Automatic counter used to setup ammo inventory for gunners
     */
    public int numPassengerGunners = 0;

    public float vehicleGunModelScale = 1f;

    public boolean filterAmmunition = false;

    public boolean worksUnderWater = false;

    public static class ShootParticle {
        public ShootParticle(String s, float x1, float y1, float z1) {
            x = x1;
            y = y1;
            z = z1;
            name = s;
        }

        float x, y, z;
        String name;
    }

    public ArrayList<ShootParticle> shootParticlesPrimary = new ArrayList<>();
    public ArrayList<ShootParticle> shootParticlesSecondary = new ArrayList<>();

    //Inventory + Pilot guns
    /**
     * Inventory sizes
     */
    public int numCargoSlots, numBombSlots, numMissileSlots;
    /**
     * The fuel tank size
     */
    public int fuelTankSize = 100;

    //Rendering variables
    /**
     * The yOffset of the model. Shouldn't be needed if you made your model properly
     */
    public float yOffset = 10F / 16F;
    /**
     * Third person render distance
     */
    public float cameraDistance = 5F;

    //Particle system
    /**
     * A list of ambient particle emitters on this vehicle
     */
    public ArrayList<ParticleEmitter> emitters = new ArrayList<>();

    //Movement variables
    /**
     * Generic movement modifiers, no longer repeated for plane and vehicle
     */
    public float maxThrottle = 1F, maxNegativeThrottle = 0F;
    public float clutchBrake = 0F;
    /**
     * The origin of the tank turret
     */
    public Vector3f turretOrigin = new Vector3f();
    public Vector3f turretOriginOffset = new Vector3f();

    /**
     * Wheel positions
     */
    public DriveablePosition[] wheelPositions = new DriveablePosition[0];
    /**
     * Strength of springs connecting car to wheels
     */
    public float wheelSpringStrength = 0.5F;
    /**
     * The wheel radius for onGround checks
     */
    public float wheelStepHeight = 1.0F;
    /**
     * Whether or not the vehicle rolls
     */
    public boolean canRoll = true;

    /**
     * Collision points for block based collisions
     */
    public ArrayList<DriveablePosition> collisionPoints = new ArrayList<>();

    /**
     * Coefficient of drag
     */
    public float drag = 1F;

    //Boat Stuff
    /**
     * If true, then the vehicles wheels float on water
     */
    public boolean floatOnWater = false;
    /**
     * Defines where you can place this vehicle
     */
    public boolean placeableOnLand = true, placeableOnWater = false, placeableOnSponge = false;
    /**
     * The upwards force to apply to the vehicle per wheel when on water
     */
    public float buoyancy = 0.0165F;

    public float floatOffset = 0;

    /**
     * The radius within which to check for bullets
     */
    public float bulletDetectionRadius = -1F;

    /**
     * Plane is shown on ICBM Radar and engaged by AA Guns
     */
    public boolean onRadar = false;

    /**
     * Track animation frames
     */
    public int animFrames = 2;

    /**
     * Sounds
     */
    public int startSoundRange = 50;
    public String startSound = "";
    public int startSoundLength;
    public int engineSoundRange = 50;
    public String engineSound = "";
    public int engineSoundLength;
    public int backSoundRange = 50;
    public String exitSound = "";
    public int exitSoundLength = 50;
    public String idleSound = "";
    public int idleSoundLength = 50;
    public String backSound = "";
    public int backSoundLength;

    public boolean collisionDamageEnable = false;
    public float collisionDamageThrottle = 0;
    public float collisionDamageTimes = 0;

    // Appears to be unused
    //public boolean enableReloadTime = false;

    public boolean canMountEntity = false;

    public float bulletSpread = 0F;
    public float bulletSpeed = 3F;
    public boolean rangingGun = false;

    public boolean isExplosionWhenDestroyed = false;
    //allows control over death explosion
    public float deathFireRadius = 0F;
    public float deathExplosionRadius = 4F;
    public float deathExplosionPower = 1F;
    public boolean deathExplosionBreaksBlocks = false;
    public float deathExplosionDamageVsLiving = 1.0F;
    public float deathExplosionDamageVsPlayer = 1.0F;
    public float deathExplosionDamageVsPlane = 1.0F;
    public float deathExplosionDamageVsVehicle = 1.0F;
    //
    public String lockedOnSound = "";
    public int soundTime = 0;
    public int canLockOnAngle = 10;
    public int lockOnSoundTime = 60;
    public String lockOnSound = "";
    public int maxRangeLockOn = 500;
    public int lockedOnSoundRange = 5;
    public String lockingOnSound = "";

    public boolean lockOnToPlanes = false, lockOnToVehicles = false, lockOnToMechas = false, lockOnToPlayers = false, lockOnToLivings = false;

    //flares
    public boolean hasFlare = false;
    public int flareDelay = 20 * 10;
    public String flareSound = "";
    public int timeFlareUsing = 1;

    /**
     * Barrel Recoil stuff
     */
    // Appears to be unused
    //public float recoilDist = 5F;
    public float recoilTime = 5F;

    /**
     * more nonsense
     */
    public boolean fixedPrimaryFire = false;
    public Vector3f primaryFireAngle = new Vector3f(0, 0, 0);

    public boolean fixedSecondaryFire = false;
    public Vector3f secondaryFireAngle = new Vector3f(0, 0, 0);
    /**
     * backwards compatibility attempt
     */
    // Appears to be unused
    //public float gunLength = 0;


    public boolean setPlayerInvisible = false;

    public float maxThrottleInWater = 0.5F;
    public int maxDepth = 3;

    public ArrayList<Vector3f> leftTrackPoints = new ArrayList<>();
    public ArrayList<Vector3f> rightTrackPoints = new ArrayList<>();
    public float trackLinkLength = 0;

    /**
     * activator boolean for IT-1 reloads
     */
    public boolean IT1 = false;

    public static ArrayList<DriveableType> types = new ArrayList<>();

    public ArrayList<CollisionShapeBox> collisionBox = new ArrayList<>();
    public boolean fancyCollision = false;

    public DriveableType(TypeFile file) {
        super(file);
    }

    @Override
    public void preRead(TypeFile file) {
        super.preRead(file);
    }

    @Override
    public void postRead(TypeFile file) {
        super.postRead(file);

        if (bulletDetectionRadius == -1F) {
            for (CollisionBox box : health.values()) {
                bulletDetectionRadius = Math.max(bulletDetectionRadius, box.getRootPosition().length() + box.getRadius());
            }

            bulletDetectionRadius += 1;
        }

        if (this.shortName != null && isValid) {
            types.add(this);
        }
    }

    @Override
    protected void read(ConfigMap config, TypeFile file) {
        super.read(config, file);

        // Must be read first.
        try {
            try {
                ArrayList<String[]> splits = ConfigUtils.getSplitsFromKey(config, new String[]{"Passenger"});
                // numpassengers from config isn't really needed, we can just count instead.
                numPassengers = splits.size();

                //if (splits.size() < numPassengers) {
                //    FlansMod.logPackError(file.name, packName, shortName, "Fewer passenger definitions than NumPassengers. You should check this.", null, null);
                //    numPassengers = splits.size();
                //} else if (splits.size() > numPassengers) {
                //    FlansMod.logPackError(file.name, packName, shortName, "More passenger definitions than NumPassengers. You should check this.", null, null);
                //    numPassengers = splits.size();
                //}

                seats = new Seat[numPassengers + 1];

                for (String[] split : splits) {
                    Seat seat = new Seat(split);
                    if (seat.id < seats.length) {
                        seats[seat.id] = seat;
                        if (seat.gunType != null) {
                            seat.gunnerID = numPassengerGunners++;
                            driveableRecipe.add(new ItemStack(seat.gunType.item));
                        }
                    }
                }
            } catch (Exception ex) {
                FlansMod.logPackError(file.name, packName, shortName, "Fatal error: error parsing Passenger definitions", null, ex);

                throw new Exception("Invalid Passenger Definitions");
                // this should be a "disable item" situation
            }

            // numWheels from config can just be ignored at this point.
            //int numWheels = ConfigUtils.configInt(config, "NumWheels", 0);

            //Wheels
            // This is bad design, but Mechas do not have or need wheels.
            if (!(this instanceof MechaType)) {
                try {
                    ArrayList<String[]> splits = ConfigUtils.getSplitsFromKey(config, new String[]{"Wheel", "WheelPosition"});

                    int numWheels = splits.size();

                    if (numWheels < 2) {
                        FlansMod.logPackError(file.name, packName, shortName, "Fatal error: Either 3 or 4 wheels are required", null, null);
                        throw new Exception("Invalid Wheel Configuration");
                    } else {
                        if (numWheels > 4) {
                            FlansMod.logPackError(file.name, packName, shortName, "Too many wheel definitions, the maximum is 4", null, null);
                        }

                        wheelPositions = new DriveablePosition[Math.min(4, numWheels)];
                    }

                    int counter = 0;
                    for (String[] split : splits) {
                        int wheelIndex = Integer.parseInt(split[1]);
                        if (wheelIndex >= numWheels) {
                            FlansMod.logPackError(file.name, packName, shortName, "Incorrect wheel index given, defaulting to counting.", split, null);
                            wheelIndex = counter;
                        }

                        float x = Float.parseFloat(split[2]) / 16F;
                        float y = Float.parseFloat(split[3]) / 16F;
                        float z = Float.parseFloat(split[4]) / 16F;

                        EnumDriveablePart part = EnumDriveablePart.coreWheel;
                        if (split.length > 5) {
                            part = EnumDriveablePart.getPart(split[5]);
                        }

                        DriveablePosition wheelPosition = new DriveablePosition(new Vector3f(x, y, z), part);
                        wheelPositions[wheelIndex] = wheelPosition;

                        counter++;

                        if (counter >= 4) {
                            // Skip any extra wheel definitions given
                            break;
                        }
                    }
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Fatal error thrown while parsing wheels", null, ex);

                    throw new Exception("Invalid Wheel Configuration");
                }
            }


            try {
                ArrayList<String[]> splits = ConfigUtils.getSplitsFromKey(config, new String[]{"Driver", "Pilot"});
                if (splits.isEmpty()) {
                    FlansMod.logPackError(file.name, packName, shortName, "No Driver or Pilot configured", null, null);
                    throw new Exception("No Driver/Pilot Configured!");
                } else {
                    if (splits.size() > 1) {
                        FlansMod.logPackError(file.name, packName, shortName, "Multiple Drivers or Pilots configured. Using first.", null, null);
                    }

                    String[] split = splits.get(0);

                    if (split.length > 4)
                        seats[0] = new Seat(Integer.parseInt(split[1]), Integer.parseInt(split[2]),
                                Integer.parseInt(split[3]), Float.parseFloat(split[4]), Float.parseFloat(split[5]),
                                Float.parseFloat(split[6]), Float.parseFloat(split[7]));
                    else
                        seats[0] = new Seat(Integer.parseInt(split[1]), Integer.parseInt(split[2]),
                                Integer.parseInt(split[3]));
                }
            } catch (Exception ex) {
                FlansMod.logPackError(file.name, packName, shortName, "Errored while reading Driver/Pilot", null, ex);
                throw new Exception("Invalid Driver/Pilot Definitions");
                // this should be a "disable item" situation
            }

            if (FMLCommonHandler.instance().getSide().isClient()) {
                model = FlansMod.proxy.loadModel(modelString, shortName, ModelDriveable.class);
            }

            vehicleGunModelScale = ConfigUtils.configFloat(config, "VehicleGunModelScale", vehicleGunModelScale);
            reloadSoundTick = ConfigUtils.configInt(config, "VehicleGunReloadTick", reloadSoundTick);
            texture = ConfigUtils.configString(config, "Texture", texture);
            isExplosionWhenDestroyed = ConfigUtils.configBool(config, "IsExplosionWhenDestroyed", isExplosionWhenDestroyed);

            //Explosion alterations
            deathFireRadius = ConfigUtils.configFloat(config, new String[]{"DeathFireRadius", "DeathFire"}, deathFireRadius);
            deathExplosionRadius = ConfigUtils.configFloat(config, new String[]{"DeathExplosionRadius", "DeathExplosion"}, deathExplosionRadius);
            deathExplosionPower = ConfigUtils.configFloat(config, "DeathExplosionPower", deathExplosionPower);
            deathExplosionBreaksBlocks = ConfigUtils.configBool(config, "DeathExplosionBreaksBlocks", deathExplosionBreaksBlocks);
            deathExplosionDamageVsLiving = ConfigUtils.configFloat(config, "DeathExplosionDamageVsLiving", deathExplosionDamageVsLiving);
            deathExplosionDamageVsPlayer = ConfigUtils.configFloat(config, "DeathExplosionDamageVsPlayer", deathExplosionDamageVsPlayer);
            deathExplosionDamageVsPlane = ConfigUtils.configFloat(config, "DeathExplosionDamageVsPlane", deathExplosionDamageVsPlane);
            deathExplosionDamageVsVehicle = ConfigUtils.configFloat(config, "DeathExplosionDamageVsVehicle", deathExplosionDamageVsVehicle);

            fallDamageFactor = ConfigUtils.configFloat(config, "FallDamageFactor", fallDamageFactor);

            //Movement Variables
            maxThrottle = ConfigUtils.configFloat(config, "MaxThrottle", maxThrottle);
            maxNegativeThrottle = ConfigUtils.configFloat(config, "MaxNegativeThrottle", maxNegativeThrottle);
            clutchBrake = ConfigUtils.configFloat(config, "ClutchBrake", clutchBrake);
            maxThrottleInWater = ConfigUtils.configFloat(config, "MaxThrottleInWater", maxThrottleInWater);
            maxDepth = ConfigUtils.configInt(config, "MaxDepth", maxDepth);
            drag = ConfigUtils.configFloat(config, "Drag", drag);

            turretOrigin = ConfigUtils.configVector(config, "TurretOrigin", turretOrigin, 1F / 16F);
            turretOriginOffset = ConfigUtils.configVector(config, "TurretOrigin", turretOriginOffset, 1F / 16F);

            ArrayList<String[]> splits = ConfigUtils.getSplitsFromKey(config, new String[]{"CollisionPoint", "AddCollisionPoint"});
            for (String[] split : splits) {
                try {
                    collisionPoints.add(new DriveablePosition(split));
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Error thrown while adding collision point", split, ex);
                }
            }


            collisionDamageEnable = ConfigUtils.configBool(config, "CollisionDamageEnable", collisionDamageEnable);
            collisionDamageThrottle = ConfigUtils.configFloat(config, "CollisionDamageThrottle", collisionDamageThrottle);
            collisionDamageTimes = ConfigUtils.configFloat(config, "CollisionDamageTimes", collisionDamageTimes);
            canLockOnAngle = ConfigUtils.configInt(config, "CanLockAngle", canLockOnAngle);
            lockOnSoundTime = ConfigUtils.configInt(config, "LockOnSoundTime", lockOnSoundTime);

            lockOnToPlanes = lockOnToVehicles = lockOnToMechas = ConfigUtils.configBool(config, "LockOnToDriveables", false);
            lockOnToVehicles = ConfigUtils.configBool(config, "LockOnToVehicles", lockOnToVehicles);
            lockOnToPlanes = ConfigUtils.configBool(config, "LockOnToPlanes", lockOnToPlanes);
            lockOnToMechas = ConfigUtils.configBool(config, "LockOnToMechas", lockOnToMechas);
            lockOnToPlayers = ConfigUtils.configBool(config, "LockOnToPlayers", lockOnToPlayers);
            lockOnToLivings = ConfigUtils.configBool(config, "LockOnToLivings", lockOnToLivings);

            lockedOnSoundRange = ConfigUtils.configInt(config, "LockedOnSoundRange", lockedOnSoundRange);
            canRoll = ConfigUtils.configBool(config, "CanRoll", canRoll);

            //Flares
            hasFlare = ConfigUtils.configBool(config, "HasFlare", hasFlare);

            flareDelay = Math.max(1, ConfigUtils.configInt(config, "FlareDelay", flareDelay));
            timeFlareUsing = Math.max(1, ConfigUtils.configInt(config, "TimeFlareUsing", timeFlareUsing));

            //Boats
            if (config.containsKey("Boat")) {
                placeableOnLand = false;
                placeableOnWater = true;
                floatOnWater = true;
                wheelStepHeight = 0F;
            }

            placeableOnLand = ConfigUtils.configBool(config, "PlaceableOnLand", placeableOnLand);
            placeableOnWater = ConfigUtils.configBool(config, "PlaceableOnWater", placeableOnWater);
            worksUnderWater = ConfigUtils.configBool(config, "WorksUnderwater", worksUnderWater);
            placeableOnSponge = ConfigUtils.configBool(config, "PlaceableOnSponge", placeableOnSponge);
            floatOnWater = ConfigUtils.configBool(config, "FloatOnWater", floatOnWater);
            buoyancy = ConfigUtils.configFloat(config, "Buoyancy", buoyancy);
            floatOffset = ConfigUtils.configFloat(config, "FloatOffset", floatOffset);
            canMountEntity = ConfigUtils.configBool(config, "CanMountEntity", canMountEntity);


            wheelStepHeight = ConfigUtils.configFloat(config, new String[]{"WheelRadius", "WheelStepHeight"}, wheelStepHeight);
            wheelSpringStrength = ConfigUtils.configFloat(config, new String[]{"WheelSpringStrength", "SpringStrength"}, wheelSpringStrength);
            animFrames = ConfigUtils.configInt(config, "TrackFrames", animFrames);

            //Harvesting
            harvestBlocks = ConfigUtils.configBool(config, "Harvester", harvestBlocks);
            collectHarvest = ConfigUtils.configBool(config, "CollectHarvest", collectHarvest);
            dropHarvest = ConfigUtils.configBool(config, "DropHarvest", dropHarvest);

            String[] asplit = ConfigUtils.getSplitFromKey(config, "HarvestBox");
            if (asplit != null) {
                try {
                    harvestBoxSize = new Vector3f(asplit[1]);
                    harvestBoxPos = new Vector3f(asplit[2]);
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Error thrown while adding harvester", asplit, ex);
                }
            }

            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"HarvestMaterial"});
            for (String[] split : splits) { // This currently doesn't work, see todo
                try {
                    Material m = getMaterial(split[1]);
                    if (m != null) {
                        materialsHarvested.add(m);
                    } else {
                        FlansMod.logPackError(file.name, packName, shortName, "Couldn't find material for HarvestMaterial", split, null);
                    }
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Error thrown while adding HarvestMaterial", split, ex);
                }
            }

            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"HarvestToolType"});
            for (String[] split : splits) {
                try {
                    switch (split[1]) {
                        case "Axe":
                            materialsHarvested.add(Material.wood);
                            materialsHarvested.add(Material.plants);
                            materialsHarvested.add(Material.vine);
                            break;
                        case "Pickaxe":
                        case "Drill":
                            materialsHarvested.add(Material.iron);
                            materialsHarvested.add(Material.anvil);
                            materialsHarvested.add(Material.rock);
                            break;
                        case "Spade":
                        case "Shovel":
                        case "Excavator":
                            materialsHarvested.add(Material.ground);
                            materialsHarvested.add(Material.grass);
                            materialsHarvested.add(Material.sand);
                            materialsHarvested.add(Material.snow);
                            materialsHarvested.add(Material.clay);
                            break;
                        case "Hoe":
                        case "Combine":
                            materialsHarvested.add(Material.plants);
                            materialsHarvested.add(Material.leaves);
                            materialsHarvested.add(Material.vine);
                            materialsHarvested.add(Material.cactus);
                            materialsHarvested.add(Material.gourd);
                            break;
                        case "Tank":
                            materialsHarvested.add(Material.leaves);
                            materialsHarvested.add(Material.cactus);
                            materialsHarvested.add(Material.wood);
                            materialsHarvested.add(Material.plants);
                            break;
                    }
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Setting HarvestToolType failed", split, ex);
                }
            }

            //Cargo / Payload
            numCargoSlots = ConfigUtils.configInt(config, "CargoSlots", numCargoSlots);
            numBombSlots = ConfigUtils.configInt(config, new String[]{"BombSlots", "MineSlots"}, numBombSlots);
            numMissileSlots = ConfigUtils.configInt(config, new String[]{"MissileSlots", "ShellSlots"}, numMissileSlots);

            fuelTankSize = ConfigUtils.configInt(config, "FuelTankSize", fuelTankSize);
            engineStartTime = ConfigUtils.configInt(config, "EngineStartTime", engineStartTime);
            filterAmmunition = ConfigUtils.configBool(config, "FilterAmmunitionInput", filterAmmunition);

            bulletDetectionRadius = ConfigUtils.configFloat(config, "BulletDetection", bulletDetectionRadius);

            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"AddAmmo"});

            for (String[] split : splits) {
                try {
                    if (split.length == 2) {
                        BulletType bullet = BulletType.getBullet(split[1]);
                        if (bullet != null) {
                            ammo.add(bullet);
                        } else {
                            FlansMod.logPackError(file.name, packName, shortName, "Could not find bullet for AddAmmo", split, null);
                        }
                    } else {
                        FlansMod.logPackError(file.name, packName, shortName, "Incorrect format supplied to AddAmmo", split, null);
                    }
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Error thrown from AddAmmo", split, ex);
                }
            }


            acceptAllAmmo = ConfigUtils.configBool(config, new String[]{"AllowAllAmmo", "AcceptAllAmmo"}, acceptAllAmmo);

            String line = ConfigUtils.configString(config, "Primary", "NONE");
            try {
                if (line != null) {
                    primary = EnumWeaponType.valueOf(line.toUpperCase());
                }
            } catch (Exception ex) {
                FlansMod.logPackError(file.name, packName, shortName, "Primary weapon type not known", new String[]{"Primary", line}, ex);
            }

            line = ConfigUtils.configString(config, "Secondary", "NONE");
            try {
                if (line != null) {
                    secondary = EnumWeaponType.valueOf(line.toUpperCase());
                }
            } catch (Exception ex) {
                FlansMod.logPackError(file.name, packName, shortName, "Secondary weapon type not known", new String[]{"Secondary", line}, ex);
            }

            damageMultiplierPrimary = ConfigUtils.configFloat(config, "DamageMultiplierPrimary", damageMultiplierPrimary);
            damageMultiplierSecondary = ConfigUtils.configFloat(config, "DamageMultiplierSecondary", damageMultiplierSecondary);

            shootDelayPrimary = ConfigUtils.configFloat(config, new String[]{"ShootDelayPrimary", "ShellDelay", "BombDelay"}, shootDelayPrimary);
            shootDelaySecondary = ConfigUtils.configFloat(config, new String[]{"ShootDelaySecondary", "ShootDelay"}, shootDelaySecondary);


            try {
                if (shootDelayPrimary == -1) {
                    shootDelayPrimary = Math.max(1200F / ConfigUtils.configFloat(config, "RoundsPerMinPrimary", 60), 1);
                }

                if (shootDelaySecondary == -1) {
                    shootDelaySecondary = Math.max(1200F / ConfigUtils.configFloat(config, "RoundsPerMinSecondary", 60), 1);
                }
            } catch (Exception ex) {
                FlansMod.logPackError(file.name, packName, shortName, "Error thrown setting RoundsPerMin", null, ex);
            }

            placeTimePrimary = ConfigUtils.configInt(config, "PlaceTimePrimary", placeTimePrimary);
            placeTimeSecondary = ConfigUtils.configInt(config, "PlaceTimeSecondary", placeTimeSecondary);
            reloadTimePrimary = ConfigUtils.configInt(config, "ReloadTimePrimary", reloadTimePrimary);
            reloadTimeSecondary = ConfigUtils.configInt(config, "ReloadTimeSecondary", reloadTimeSecondary);
            alternatePrimary = ConfigUtils.configBool(config, "AlternatePrimary", alternatePrimary);
            alternateSecondary = ConfigUtils.configBool(config, "AlternateSecondary", alternateSecondary);

            line = ConfigUtils.configString(config, "ModePrimary", null);
            try {
                if (line != null) {
                    modePrimary = EnumFireMode.valueOf(line.toUpperCase());
                }
            } catch (Exception ex) {
                FlansMod.logPackError(file.name, packName, shortName, "Primary weapon fire mode not known", new String[]{"PrimaryMode", line}, ex);
            }


            line = ConfigUtils.configString(config, "ModeSecondary", null);
            try {
                if (line != null) {
                    modeSecondary = EnumFireMode.valueOf(line.toUpperCase());
                }
            } catch (Exception ex) {
                FlansMod.logPackError(file.name, packName, shortName, "Secondary weapon fire mode not known", new String[]{"SecondaryMode", line}, ex);
            }

            bulletSpeed = ConfigUtils.configFloat(config, "BulletSpeed", bulletSpeed);

            bulletSpread = ConfigUtils.configFloat(config, "BulletSpread", bulletSpread);
            rangingGun = ConfigUtils.configBool(config, "RangingGun", rangingGun);
            //gunLength = ConfigUtils.configFloat(config, "GunLength", gunLength);
            //recoilDist = ConfigUtils.configFloat(config, "RecoilDistance", recoilDist);
            recoilTime = ConfigUtils.configFloat(config, "RecoilTime", recoilTime);

            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"ShootPointPrimary"});

            for (String[] split : splits) {
                try {
                    // TODO: Refactor this

                    DriveablePosition rootPos;
                    Vector3f offPos;
                    String[] gun;
                    if (split.length == 9) {
                        gun = new String[]{split[0], split[1], split[2], split[3], split[4], split[5]};
                        offPos = new Vector3f(Float.parseFloat(split[6]) / 16F, Float.parseFloat(split[7]) / 16F, Float.parseFloat(split[8]) / 16F);
                    } else if (split.length == 8) {
                        gun = new String[]{split[0], split[1], split[2], split[3], split[4]};
                        offPos = new Vector3f(Float.parseFloat(split[5]) / 16F, Float.parseFloat(split[6]) / 16F, Float.parseFloat(split[7]) / 16F);
                    } else {
                        gun = split;
                        offPos = new Vector3f(0, 0, 0);
                    }
                    rootPos = getShootPoint(gun);
                    ShootPoint sPoint = new ShootPoint(rootPos, offPos);
                    shootPointsPrimary.add(sPoint);
                    if (rootPos instanceof PilotGun)
                        pilotGuns.add((PilotGun) sPoint.rootPos);
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Error thrown while adding ShootPointPrimary", split, ex);
                }
            }


            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"ShootPointSecondary"});

            for (String[] split : splits) {
                try {
                    // TODO: Refactor this

                    DriveablePosition rootPos;
                    Vector3f offPos;
                    String[] gun;
                    if (split.length == 9) {
                        gun = new String[]{split[0], split[1], split[2], split[3], split[4], split[5]};
                        offPos = new Vector3f(Float.parseFloat(split[6]) / 16F, Float.parseFloat(split[7]) / 16F, Float.parseFloat(split[8]) / 16F);
                    } else if (split.length == 8) {
                        gun = new String[]{split[0], split[1], split[2], split[3], split[4]};
                        offPos = new Vector3f(Float.parseFloat(split[5]) / 16F, Float.parseFloat(split[6]) / 16F, Float.parseFloat(split[7]) / 16F);
                    } else {
                        gun = split;
                        offPos = new Vector3f(0, 0, 0);
                    }
                    rootPos = getShootPoint(gun);
                    ShootPoint sPoint = new ShootPoint(rootPos, offPos);
                    shootPointsSecondary.add(sPoint);
                    if (rootPos instanceof PilotGun)
                        pilotGuns.add((PilotGun) sPoint.rootPos);
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Error thrown while adding ShootPointSecondary", split, ex);
                }
            }


            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"ShootParticlesPrimary"});

            for (String[] split : splits) {
                try {
                    if (split.length == 5) { //TODO validate the particle exists
                        shootParticlesPrimary.add(
                                new ShootParticle(split[1], Float.parseFloat(split[2]), Float.parseFloat(split[3]), Float.parseFloat(split[4]))
                        );
                    } else {
                        FlansMod.logPackError(file.name, packName, shortName, "ShootParticlesPrimary has invalid format", split, null);
                    }
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Error thrown while adding ShootParticlesPrimary", split, ex);
                }
            }

            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"ShootParticlesSecondary"});

            for (String[] split : splits) {
                try {
                    if (split.length == 5) { //TODO validate the particle exists
                        shootParticlesSecondary.add(
                                new ShootParticle(split[1], Float.parseFloat(split[2]), Float.parseFloat(split[3]), Float.parseFloat(split[4]))
                        );
                    } else {
                        FlansMod.logPackError(file.name, packName, shortName, "ShootParticlesSecondary has invalid format", split, null);
                    }
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Error thrown while adding ShootParticlesSecondary", split, ex);
                }
            }


            setPlayerInvisible = ConfigUtils.configBool(config, "SetPlayerInvisible", setPlayerInvisible);
            IT1 = ConfigUtils.configBool(config, "IT1", IT1);
            fixedPrimaryFire = ConfigUtils.configBool(config, "FixedPrimary", fixedPrimaryFire);
            fixedSecondaryFire = ConfigUtils.configBool(config, "FixedSecondary", fixedSecondaryFire);


            primaryFireAngle = ConfigUtils.configVector(config, "PrimaryAngle", primaryFireAngle);
            secondaryFireAngle = ConfigUtils.configVector(config, "SecondaryAngle", secondaryFireAngle);


            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"AddGun"});

            for (String[] split : splits) {
                try {
                    secondary = EnumWeaponType.GUN;

                    if (split.length == 6 || split.length == 9) {
                        PilotGun pilotGun = new PilotGun(Arrays.copyOfRange(split, 0, 6));
                        Vector3f offPos = split.length == 6 ? Vector3f.Zero : new Vector3f(Float.parseFloat(split[6]) / 16F, Float.parseFloat(split[7]) / 16F, Float.parseFloat(split[8]) / 16F);

                        if (pilotGun.type == null) {
                            // The gun type is not found, skip.
                            FlansMod.logPackError(file.name, packName, shortName, "Could not find gun type for AddGun. Skipping.", split, null);
                        } else {
                            ShootPoint sPoint = new ShootPoint(pilotGun, offPos);
                            shootPointsSecondary.add(sPoint);
                            pilotGuns.add(pilotGun);
                            driveableRecipe.add(new ItemStack(pilotGun.type.item));
                        }
                    } else {
                        FlansMod.logPackError(file.name, packName, shortName, "AddGun not in correct format. Skipping.", split, null);
                    }
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Error thrown while adding pilot gun with AddGun", split, ex);
                }
            }


            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"BombPosition"});
            if (splits.size() > 1) {
                primary = EnumWeaponType.BOMB;
            }

            for (String[] split : splits) {
                try {
                    if (split.length == 4)
                        shootPointsPrimary.add(new ShootPoint(new DriveablePosition(new Vector3f(Float.parseFloat(split[1]) / 16F, Float.parseFloat(split[2]) / 16F, Float.parseFloat(split[3]) / 16F), EnumDriveablePart.core), new Vector3f(0, 0, 0)));
                    else if (split.length == 7)
                        shootPointsPrimary.add(new ShootPoint(new DriveablePosition(new Vector3f(Float.parseFloat(split[1]) / 16F, Float.parseFloat(split[2]) / 16F, Float.parseFloat(split[3]) / 16F), EnumDriveablePart.core), new Vector3f(Float.parseFloat(split[4]) / 16F, Float.parseFloat(split[5]) / 16F, Float.parseFloat(split[6]) / 16F)));
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Error thrown while setting BombPosition", split, ex);
                }
            }


            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"BarrelPosition"});

            if (splits.size() >= 1) {
                primary = EnumWeaponType.SHELL;
            }

            for (String[] split : splits) {
                try {
                    if (split.length == 4)
                        shootPointsPrimary.add(new ShootPoint(new DriveablePosition(new Vector3f(Float.parseFloat(split[1]) / 16F, Float.parseFloat(split[2]) / 16F, Float.parseFloat(split[3]) / 16F), EnumDriveablePart.turret), new Vector3f(0, 0, 0)));
                    else if (split.length == 7)
                        shootPointsPrimary.add(new ShootPoint(new DriveablePosition(new Vector3f(Float.parseFloat(split[1]) / 16F, Float.parseFloat(split[2]) / 16F, Float.parseFloat(split[3]) / 16F), EnumDriveablePart.turret), new Vector3f(Float.parseFloat(split[4]) / 16F, Float.parseFloat(split[5]) / 16F, Float.parseFloat(split[6]) / 16F)));
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Error thrown while setting BarrelPosition", split, ex);
                }
            }


            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"AddRecipeParts"});
            for (String[] split : splits) {
                try {
                    EnumDriveablePart part = EnumDriveablePart.getPart(split[1]);
                    ArrayList<ItemStack> stacks = new ArrayList<>();
                    for (int i = 0; i < (split.length - 2) / 2; i++) {
                        int amount = Integer.parseInt(split[2 * i + 2]);
                        boolean damaged = split[2 * i + 3].contains(".");
                        String itemName = damaged ? split[2 * i + 3].split("\\.")[0] : split[2 * i + 3];
                        int damage = damaged ? Integer.parseInt(split[2 * i + 3].split("\\.")[1]) : 0;

                        // Only add part if it is NOT null. (Seems obvious?)
                        ItemStack potentialPart = getRecipeElement(itemName, amount, damage, shortName);
                        if (potentialPart != null) {
                            stacks.add(potentialPart);
                            driveableRecipe.add(potentialPart);
                        } else {
                            FlansMod.logPackError(file.name, packName, shortName, "Item not found for AddRecipeParts", split, null);
                        }

                    }
                    ItemStack[] items = new ItemStack[stacks.size()];
                    items = stacks.toArray(items);
                    partwiseRecipe.put(part, items);
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "AddRecipeParts threw an error", split, ex);
                }
            }


            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"AddDye"});
            for (String[] split : splits) {
                try {
                    int amount = Integer.parseInt(split[1]);
                    int damage = -1;
                    for (int i = 0; i < ItemDye.field_150923_a.length; i++) {
                        if (ItemDye.field_150923_a[i].equals(split[2]))
                            damage = i;
                    }
                    if (damage == -1) {
                        FlansMod.logPackError(file.name, packName, shortName, "Failed to find dye colour in AddDye", split, null);
                        return;
                    }
                    driveableRecipe.add(new ItemStack(Items.dye, amount, damage));
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "AddDye threw an error", split, ex);
                }
            }

            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"SetupPart"});

            for (String[] split : splits) {
                try {
                    EnumDriveablePart part = EnumDriveablePart.getPart(split[1]);
                    CollisionBox box;
                    if (split.length > 9) {
                        box = new CollisionBox(Integer.parseInt(split[2]), Integer.parseInt(split[3]), Integer.parseInt(split[4]), Integer.parseInt(split[5]), Integer.parseInt(split[6]), Integer.parseInt(split[7]), Integer.parseInt(split[8]), Float.parseFloat(split[9]));
                    } else {
                        box = new CollisionBox(Integer.parseInt(split[2]), Integer.parseInt(split[3]), Integer.parseInt(split[4]), Integer.parseInt(split[5]), Integer.parseInt(split[6]), Integer.parseInt(split[7]), Integer.parseInt(split[8]));
                    }
                    health.put(part, box);
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "SetupPart threw an error", split, ex);
                }
            }


            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"PartDeathExplosion"});
            for (String[] split : splits) {
                try {

                    EnumDriveablePart part = EnumDriveablePart.getPart(split[1]);

                    BoxExplosion exp;
                    if (split.length > 5) {
                        exp = new BoxExplosion(Float.parseFloat(split[2]), Float.parseFloat(split[3]), Boolean.parseBoolean(split[4]), Float.parseFloat(split[5]), Float.parseFloat(split[6]), Float.parseFloat(split[6]), Float.parseFloat(split[7]));
                    } else {
                        exp = new BoxExplosion(Float.parseFloat(split[2]), Float.parseFloat(split[3]), Boolean.parseBoolean(split[4]));
                    }

                    partDeathExplosions.put(part, exp);
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "PartDeathExplosion threw an error", split, ex);
                }
            }


            if (seats.length > 0 && seats[0] != null) {
                seats[0].part = EnumDriveablePart.getPart(ConfigUtils.configString(config, "DriverPart", "core"));

                seats[0].gunName = ConfigUtils.configString(config, new String[]{"DriverGun", "PilotGun"}, seats[0].gunName);
                seats[0].gunOrigin = ConfigUtils.configVector(config, "DriverGunOrigin", seats[0].gunOrigin, 1F / 16);
                seats[0].rotatedOffset = ConfigUtils.configVector(config, "RotatedDriverOffset", seats[0].rotatedOffset, 1F / 16);
                seats[0].aimingSpeed = ConfigUtils.configVector(config, "DriverAimSpeed", seats[0].aimingSpeed);
                seats[0].legacyAiming = ConfigUtils.configBool(config, "DriverLegacyAiming", seats[0].legacyAiming);
                seats[0].yawBeforePitch = ConfigUtils.configBool(config, "DriverYawBeforePitch", seats[0].yawBeforePitch);
                seats[0].latePitch = ConfigUtils.configBool(config, "DriverLatePitch", seats[0].latePitch);
                seats[0].traverseSounds = ConfigUtils.configBool(config, "DriverTraverseSounds", seats[0].traverseSounds);
            } else {
                FlansMod.logPackError(file.name, packName, shortName, "Driver is not defined!", null, null);

                throw new Exception("Driver is not defined! Cannot proceed further.");
            }

            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"RotatedPassengerOffset"});
            for (String[] split : splits) {
                try {
                    seats[Integer.parseInt(split[1])].rotatedOffset = new Vector3f(Integer.parseInt(split[2]) / 16F, Integer.parseInt(split[3]) / 16F, Integer.parseInt(split[4]) / 16F);
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Could not set RotatedPassengerOffset", split, ex);
                }
            }

            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"PassengerAimSpeed"});
            for (String[] split : splits) {
                try {
                    seats[Integer.parseInt(split[1])].aimingSpeed = new Vector3f(Float.parseFloat(split[2]), Float.parseFloat(split[3]), Float.parseFloat(split[4]));
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Could not set PassengerAimSpeed", split, ex);
                }
            }

            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"PassengerLegacyAiming"});
            for (String[] split : splits) {
                try {
                    seats[Integer.parseInt(split[1])].legacyAiming = Boolean.parseBoolean(split[2]);
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Could not set PassengerLegacyAiming", split, ex);
                }
            }

            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"PassengerYawBeforePitch"});
            for (String[] split : splits) {
                try {
                    seats[Integer.parseInt(split[1])].yawBeforePitch = Boolean.parseBoolean(split[2]);
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Could not set PassengerYawBeforePitch", split, ex);
                }
            }

            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"PassengerLatePitch"});
            for (String[] split : splits) {
                try {
                    seats[Integer.parseInt(split[1])].latePitch = Boolean.parseBoolean(split[2]);
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Could not set PassengerLatePitch", split, ex);
                }
            }

            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"PassengerTraverseSounds"});
            for (String[] split : splits) {
                try {
                    seats[Integer.parseInt(split[1])].traverseSounds = Boolean.parseBoolean(split[2]);
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Could not set PassengerTraverseSounds", split, ex);
                }
            }

            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"PassengerYawSoundLength"});
            for (String[] split : splits) {
                try {
                    seats[Integer.parseInt(split[1])].yawSoundLength = Integer.parseInt(split[2]);
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Could not set PassengerYawSoundLength", split, ex);
                }
            }

            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"PassengerPitchSoundLength"});
            for (String[] split : splits) {
                try {
                    seats[Integer.parseInt(split[1])].pitchSoundLength = Integer.parseInt(split[2]);
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Could not set PassengerPitchSoundLength", split, ex);
                }
            }

            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"PassengerYawSound"});
            for (String[] split : splits) {
                try {
                    seats[Integer.parseInt(split[1])].yawSound = split[2];
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Could not set PassengerYawSound", split, ex);
                }
            }

            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"PassengerPitchSound"});
            for (String[] split : splits) {
                try {
                    seats[Integer.parseInt(split[1])].pitchSound = split[2];
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Could not set PassengerPitchSound", split, ex);
                }
            }


            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"GunOrigin"});

            for (String[] split : splits) {
                try {
                    int seatId = Integer.parseInt(split[1]);

                    float x = Float.parseFloat(split[2]) / 16F;
                    float y = Float.parseFloat(split[3]) / 16F;
                    float z = Float.parseFloat(split[4]) / 16F;
                    if (seatId < seats.length && seats[seatId] != null) {
                        seats[Integer.parseInt(split[1])].gunOrigin = new Vector3f(x, y, z);
                    } else {
                        FlansMod.logPackError(file.name, packName, shortName, "Passenger seat not found for GunOrigin", split, null);
                    }
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Could not set GunOrigin", split, ex);
                }
            }


            //Y offset for badly built models :P
            yOffset = ConfigUtils.configFloat(config, "YOffset", yOffset);
            //Third person camera distance
            cameraDistance = ConfigUtils.configFloat(config, "CameraDistance", cameraDistance);

            //Sound
            startSoundRange = ConfigUtils.configInt(config, "StartSoundRange", startSoundRange);
            startSoundLength = ConfigUtils.configInt(config, "StartSoundLength", startSoundLength);
            engineSoundRange = ConfigUtils.configInt(config, "EngineSoundRange", engineSoundRange);
            engineSoundLength = ConfigUtils.configInt(config, "EngineSoundLength", engineSoundLength);
            idleSoundLength = ConfigUtils.configInt(config, "IdleSoundLength", idleSoundLength);
            exitSoundLength = ConfigUtils.configInt(config, "ExitSoundLength", exitSoundLength);
            backSoundRange = ConfigUtils.configInt(config, "BackSoundRange", backSoundRange);
            backSoundLength = ConfigUtils.configInt(config, "BackSoundLength", backSoundLength);
            soundTime = ConfigUtils.configInt(config, "SoundTime", soundTime);


            // By here we assume that driver is known
            seats[0].yawSoundLength = ConfigUtils.configInt(config, "YawSoundLength", seats[0].yawSoundLength);
            seats[0].pitchSoundLength = ConfigUtils.configInt(config, "PitchSoundLength", seats[0].pitchSoundLength);
            seats[0].yawSound = ConfigUtils.configDriveableSound(packName, config, "YawSound", seats[0].yawSound);
            seats[0].pitchSound = ConfigUtils.configDriveableSound(packName, config, "PitchSound", seats[0].pitchSound);


            startSound = ConfigUtils.configDriveableSound(packName, config, "StartSound", startSound);
            engineSound = ConfigUtils.configDriveableSound(packName, config, "EngineSound", engineSound);
            idleSound = ConfigUtils.configDriveableSound(packName, config, "IdleSound", idleSound);
            exitSound = ConfigUtils.configDriveableSound(packName, config, "ExitSound", exitSound);
            backSound = ConfigUtils.configDriveableSound(packName, config, "BackSound", backSound);


            shootSoundPrimary = ConfigUtils.configDriveableSound(packName, config, new String[]{"ShootMainSound", "BombSound", "ShootSoundPrimary", "ShellSound"}, shootSoundPrimary);
            shootReloadSound = ConfigUtils.configDriveableSound(packName, config, "ShootReloadSound", shootReloadSound);
            shootSoundSecondary = ConfigUtils.configDriveableSound(packName, config, new String[]{"ShootSecondarySound", "ShootSoundSecondary"}, shootSoundSecondary);


            placeSoundPrimary = ConfigUtils.configDriveableSound(packName, config, "PlaceSoundPrimary", placeSoundPrimary);
            placeSoundSecondary = ConfigUtils.configDriveableSound(packName, config, "PlaceSoundSecondary", placeSoundSecondary);
            reloadSoundPrimary = ConfigUtils.configDriveableSound(packName, config, "ReloadSoundPrimary", reloadSoundPrimary);
            reloadSoundSecondary = ConfigUtils.configDriveableSound(packName, config, "ReloadSoundSecondary", reloadSoundSecondary);
            lockedOnSound = ConfigUtils.configDriveableSound(packName, config, "LockedOnSound", lockedOnSound);
            lockOnSound = ConfigUtils.configGunSound(packName, config, "LockOnSound", lockOnSound);
            lockingOnSound = ConfigUtils.configGunSound(packName, config, "LockingOnSound", lockingOnSound);
            flareSound = ConfigUtils.configDriveableSound(packName, config, "FlareSound", flareSound);

            fancyCollision = ConfigUtils.configBool(config, "FancyCollision", fancyCollision);

            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"AddCollisionMesh", "AddTurretCollisionMesh"});

            for (String[] split : splits) {
                try {
                    CollisionShapeBox box = new CollisionShapeBox(new Vector3f(split[1]), new Vector3f(split[2]),
                            new Vector3f(split[3]), new Vector3f(split[4]), new Vector3f(split[5]),
                            new Vector3f(split[6]), new Vector3f(split[7]), new Vector3f(split[8]),
                            new Vector3f(split[9]), new Vector3f(split[10]), split[0].contains("Turret") ? "turret" : "core");
                    collisionBox.add(box);
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Adding collision mesh failed", split, ex);
                }
            }

            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"AddCollisionMeshRaw", "AddTurretCollisionMeshRaw"});

            for (String[] split : splits) {
                try {

                    Vector3f pos = new Vector3f(Float.parseFloat(split[1]), Float.parseFloat(split[2]), Float.parseFloat(split[3]));
                    Vector3f size = new Vector3f(Float.parseFloat(split[4]), Float.parseFloat(split[5]), Float.parseFloat(split[6]));
                    Vector3f p1 = new Vector3f(Float.parseFloat(split[8]), Float.parseFloat(split[9]), Float.parseFloat(split[10]));
                    Vector3f p2 = new Vector3f(Float.parseFloat(split[11]), Float.parseFloat(split[12]), Float.parseFloat(split[13]));
                    Vector3f p3 = new Vector3f(Float.parseFloat(split[14]), Float.parseFloat(split[15]), Float.parseFloat(split[16]));
                    Vector3f p4 = new Vector3f(Float.parseFloat(split[17]), Float.parseFloat(split[18]), Float.parseFloat(split[19]));
                    Vector3f p5 = new Vector3f(Float.parseFloat(split[20]), Float.parseFloat(split[21]), Float.parseFloat(split[22]));
                    Vector3f p6 = new Vector3f(Float.parseFloat(split[23]), Float.parseFloat(split[24]), Float.parseFloat(split[25]));
                    Vector3f p7 = new Vector3f(Float.parseFloat(split[26]), Float.parseFloat(split[27]), Float.parseFloat(split[28]));
                    Vector3f p8 = new Vector3f(Float.parseFloat(split[29]), Float.parseFloat(split[30]), Float.parseFloat(split[31]));
                    CollisionShapeBox box = new CollisionShapeBox(pos, size, p1, p2, p3, p4, p5, p6, p7, p8, split[0].contains("Turret") ? "turret" : "core");
                    collisionBox.add(box);
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Adding collision mesh failed", split, ex);
                }
            }


            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"LeftLinkPoint"});
            for (String[] split : splits) {
                try {
                    leftTrackPoints.add(new Vector3f(split[1]));
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Adding LeftLinkPoint failed", split, ex);
                }
            }

            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"RightLinkPoint"});
            for (String[] split : splits) {
                try {
                    rightTrackPoints.add(new Vector3f(split[1]));
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Adding RightLinkPoint failed", split, ex);
                }
            }


            trackLinkLength = ConfigUtils.configFloat(config, "TrackLinkLength", trackLinkLength);

            // ICBM Mod Radar
            onRadar = ConfigUtils.configBool(config, "OnRadar", onRadar);


            splits = ConfigUtils.getSplitsFromKey(config, new String[]{"AddParticle", "AddEmitter"});

            for (String[] split : splits) {
                try {
                    ParticleEmitter emitter = new ParticleEmitter();
                    emitter.effectType = split[1];
                    emitter.emitRate = Integer.parseInt(split[2]);
                    emitter.origin = new Vector3f(split[3]);
                    emitter.extents = new Vector3f(split[4]);
                    emitter.velocity = new Vector3f(split[5]);
                    emitter.minThrottle = Float.parseFloat(split[6]);
                    emitter.maxThrottle = Float.parseFloat(split[7]);
                    emitter.minHealth = Float.parseFloat(split[8]);
                    emitter.maxHealth = Float.parseFloat(split[9]);
                    emitter.part = split[10];
                    //Scale from model coords to world coords
                    emitter.origin.scale(1.0f / 16.0f);
                    emitter.extents.scale(1.0f / 16.0f);
                    emitter.velocity.scale(1.0f / 16.0f);
                    emitters.add(emitter);
                } catch (Exception ex) {
                    FlansMod.logPackError(file.name, packName, shortName, "Adding particle failed", split, ex);
                }
            }

        } catch (Exception ex) {
            FlansMod.logPackError(file.name, packName, shortName, "Fatal Error! Reading DriveableType failed", null, ex);
            isValid = false;
        }
    }

    private DriveablePosition getShootPoint(String[] split) {
        //Its a gun with a type
        if (split.length == 6) {
            return new PilotGun(split);
        } else if (split.length == 5) {
            return new DriveablePosition(split);
        }
        return new DriveablePosition(new Vector3f(), EnumDriveablePart.core);
    }

    public ArrayList<ShootPoint> shootPoints(boolean s) {
        return s ? shootPointsSecondary : shootPointsPrimary;
    }

    public boolean alternate(boolean s) {
        return s ? alternateSecondary : alternatePrimary;
    }

    public EnumWeaponType weaponType(boolean s) {
        return s ? secondary : primary;
    }

    public float shootDelay(boolean s) {
        return s ? shootDelaySecondary : shootDelayPrimary;
    }

    public String shootSound(boolean s) {
        return s ? shootSoundSecondary : shootSoundPrimary;
    }

    public ArrayList<ShootParticle> shootParticle(boolean s) {
        return s ? shootParticlesSecondary : shootParticlesPrimary;
    }

    public int numEngines() {
        return 1;
    }

    public int ammoSlots() {
        return numPassengerGunners + pilotGuns.size();
    }

    public boolean isValidAmmo(BulletType bulletType, EnumWeaponType weaponType) {
        return (acceptAllAmmo || ammo.contains(bulletType)) && bulletType.weaponType == weaponType;
    }

    /**
     * Find the items needed to rebuild a part. The returned array is disconnected from the template items it has looked up
     */
    public ArrayList<ItemStack> getItemsRequired(DriveablePart part, PartType engine) {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        //Start with the items required to build this part
        if (partwiseRecipe.get(part.type) != null) {
            for (ItemStack stack : partwiseRecipe.get(part.type)) {
                if (stack != null) {
                    stacks.add(stack.copy());
                } else {
                    FlansMod.logPackError("", packName, shortName, "Failed to drop item on death of part " + part.type.getShortName(), null, null);
                }
            }
        }
        //Add the items required for the guns connected to this part
        for (PilotGun gun : pilotGuns) {
            if (gun != null && gun.part == part.type)
                stacks.add(new ItemStack(gun.type.item));
        }
        for (Seat seat : seats) {
            if (seat != null && seat.part == part.type && seat.gunType != null)
                stacks.add(new ItemStack(seat.gunType.item));
        }
        return stacks;
    }

    public static DriveableType getDriveable(String find) {
        for (DriveableType type : types) {
            if (type.shortName.equals(find))
                return type;
        }
        return null;
    }

    @Override
    public float GetRecommendedScale() {
        return 100.0f / cameraDistance;
    }

    public class ParticleEmitter {
        /**
         * The name of the effect
         */
        public String effectType;
        /**
         * The rate of emission
         */
        public int emitRate;
        /**
         * The centre of the effect emitter
         */
        public Vector3f origin;
        /**
         * The size of the box in which it emits
         */
        public Vector3f extents;
        /**
         * The velocity of the particle
         */
        public Vector3f velocity;
        /**
         * Lower throttle bound
         */
        public float minThrottle;
        /**
         * Upper throttle bound
         */
        public float maxThrottle;
        /**
         * Model part the emitter is bound to
         */
        public String part;
        /**
         * Minimum health for the emitter to work
         */
        public float minHealth;
        /**
         * Maximum health for the emitter to work
         */
        public float maxHealth;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBase GetModel() {
        return model;
    }
}
