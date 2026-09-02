package com.flansmod.common.driveables;

import java.util.ArrayList;

import com.flansmod.utils.ConfigMap;
import com.flansmod.utils.ConfigUtils;
import net.minecraft.item.ItemStack;

import com.flansmod.client.model.ModelPlane;
import com.flansmod.common.FlansMod;
import com.flansmod.common.parts.PartType;
import com.flansmod.common.types.TypeFile;
import com.flansmod.common.vector.Vector3f;

public class PlaneType extends DriveableType
{
	/** What type of flying vehicle is this? */
	public EnumPlaneMode mode = EnumPlaneMode.PLANE;	
	//Control Modifiers
	public float lookDownModifier = 1F, lookUpModifier = 1F;
	public float rollLeftModifier = 1F, rollRightModifier = 1F;
	public float turnLeftModifier = 1F, turnRightModifier = 1F;
	//vehicle settings
    public float restingPitch = 0F;
    public boolean spinWithoutTail = false;
	public boolean heliThrottlePull = true;
	//Does this use the new flight controller?
	public boolean newFlightControl = false;
	//Physics Modifiers
	public float lift = 1F;
	public float takeoffSpeed = 0.5F;
	public float maxSpeed = 2.0F;
	public boolean supersonic = false;
	public float wingArea = 1F;
	//Max thrust in 10s of kgf
	public float maxThrust = 50.0F;
	//mass in kg
    public float mass = 1000.0F;
	public float emptyDrag = 1F;
	//Weapon system variables
	public int planeShootDelay;
	public int planeBombDelay;
	//Aesthetic features
	//Wing Animations
	public Vector3f wingPos1 = new Vector3f(0,0,0);
	public Vector3f wingPos2 = new Vector3f(0,0,0);
	public Vector3f wingRot1 = new Vector3f(0,0,0);
	public Vector3f wingRot2 = new Vector3f(0,0,0);
	public Vector3f wingRate = new Vector3f(0,0,0);
	public Vector3f wingRotRate = new Vector3f(0,0,0);
	//Wing Wheel Animations
	public Vector3f wingWheelPos1 = new Vector3f(0,0,0);
	public Vector3f wingWheelPos2 = new Vector3f(0,0,0);
	public Vector3f wingWheelRot1 = new Vector3f(0,0,0);
	public Vector3f wingWheelRot2 = new Vector3f(0,0,0);
	public Vector3f wingWheelRate = new Vector3f(0,0,0);
	public Vector3f wingWheelRotRate = new Vector3f(0,0,0);
	//Body Wheel Animations
	public Vector3f bodyWheelPos1 = new Vector3f(0,0,0);
	public Vector3f bodyWheelPos2 = new Vector3f(0,0,0);
	public Vector3f bodyWheelRot1 = new Vector3f(0,0,0);
	public Vector3f bodyWheelRot2 = new Vector3f(0,0,0);
	public Vector3f bodyWheelRate = new Vector3f(0,0,0);
	public Vector3f bodyWheelRotRate = new Vector3f(0,0,0);
	//Tail Wheel Animations
	public Vector3f tailWheelPos1 = new Vector3f(0,0,0);
	public Vector3f tailWheelPos2 = new Vector3f(0,0,0);
	public Vector3f tailWheelRot1 = new Vector3f(0,0,0);
	public Vector3f tailWheelRot2 = new Vector3f(0,0,0);
	public Vector3f tailWheelRate = new Vector3f(0,0,0);
	public Vector3f tailWheelRotRate = new Vector3f(0,0,0);
	//Door animations
	public Vector3f doorPos1 = new Vector3f(0,0,0);
	public Vector3f doorPos2 = new Vector3f(0,0,0);
	public Vector3f doorRot1 = new Vector3f(0,0,0);
	public Vector3f doorRot2 = new Vector3f(0,0,0);
	public Vector3f doorRate = new Vector3f(0,0,0);
	public Vector3f doorRotRate = new Vector3f(0,0,0);
	
	/** The positions, parent parts and recipe items of the propellers, used to calculate forces and render the plane correctly */
	public ArrayList<Propeller> propellers = new ArrayList<Propeller>();
	/** The positions, parent parts and recipe items of the helicopter propellers, used to calculate forces and render the plane correctly */
	public ArrayList<Propeller> heliPropellers = new ArrayList<Propeller>(), heliTailPropellers = new ArrayList<Propeller>();
	//Wheter to enable variable gear, doors, or wings 
    public boolean hasGear = false, hasDoor = false, hasWing = false;
    //Do wings fold when Gear are deployed?
    public boolean foldWingForLand = false;
    //Can it fly with oor open?
	public boolean flyWithOpenDoor = false;
	//Do these automatically deploy when near ground?
	public boolean autoOpenDoorsNearGround = true;
	public boolean autoDeployLandingGearNearGround = true;
	//Is this a valkyrie? "very specific"
    public boolean valkyrie = false;
    //Can you access the inventory in flight?
    public boolean invInflight = true;

	public static ArrayList<PlaneType> types = new ArrayList<PlaneType>();
	
    public PlaneType(TypeFile file)
    {
		super(file);
    }
    
    @Override
    public void preRead(TypeFile file)
    {
    	super.preRead(file);
    }

	@Override
	public void postRead(TypeFile file) {
		super.postRead(file);

		if (this.shortName != null && isValid) {
			types.add(this);
		}
	}
    
    @Override
	protected void read(ConfigMap config, TypeFile file) {
		super.read(config, file);
		try {
			mode = EnumPlaneMode.getMode(ConfigUtils.configString(config, "Mode", "Plane"));

			newFlightControl = ConfigUtils.configBool(config, "NewFlightControl", newFlightControl);

			//Yaw modifiers
			turnLeftModifier = ConfigUtils.configFloat(config, "TurnLeftSpeed", turnLeftModifier);
			turnRightModifier = ConfigUtils.configFloat(config, "TurnRightSpeed", turnRightModifier);

			//Pitch modifiers
			lookUpModifier = ConfigUtils.configFloat(config, "LookUpSpeed", lookUpModifier);
			lookDownModifier = ConfigUtils.configFloat(config, "LookDownSpeed", lookDownModifier);

			//Roll modifiers
			rollLeftModifier = ConfigUtils.configFloat(config, "RollLeftSpeed", rollLeftModifier);
			rollRightModifier = ConfigUtils.configFloat(config, "RollRightSpeed", rollRightModifier);

			// Below are only used when NewFlightControl set.
			//Lift
			lift = ConfigUtils.configFloat(config, "Lift", lift);
			//Flight variables
			takeoffSpeed = ConfigUtils.configFloat(config, "TakeoffSpeed", takeoffSpeed);
			maxSpeed = ConfigUtils.configFloat(config, "MaxSpeed", maxSpeed);
			//Is it Supersonic?
			supersonic = ConfigUtils.configBool(config, "Supersonic", supersonic);
			maxThrust = ConfigUtils.configFloat(config, "MaxThrust", maxThrust);
			mass = ConfigUtils.configFloat(config, "Mass", mass);
			wingArea = ConfigUtils.configFloat(config, "WingArea", wingArea);


			heliThrottlePull = ConfigUtils.configBool(config, "HeliThrottlePull", heliThrottlePull);
			emptyDrag = ConfigUtils.configFloat(config, "EmptyDrag", emptyDrag);

			//Propellers and Armaments
			planeShootDelay = ConfigUtils.configInt(config, "ShootDelay", planeShootDelay);
			planeBombDelay = ConfigUtils.configInt(config, "BombDelay", planeBombDelay);

			ArrayList<String[]> splits = ConfigUtils.getSplitsFromKey(config, new String[] { "HeliPropeller", "Propeller", "HeliTailPropeller" });
			for (String[] split : splits) {
				try {
					Propeller propeller = new Propeller(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]), Integer.parseInt(split[4]), EnumDriveablePart.getPart(split[5]), PartType.getPart(split[6]));

					if (split[0].contains("HeliTailPropeller")) {
						heliTailPropellers.add(propeller);
					} else if (split[0].contains("HeliPropeller")) {
						heliPropellers.add(propeller);
					} else {
						propellers.add(propeller);
					}

					if (propeller.itemType == null) {
						FlansMod.logPackError(file.name, packName, shortName, "Couldn't find item for propeller, not adding to recipe.", split, null);
					} else {
						driveableRecipe.add(new ItemStack(propeller.itemType.item));
					}
				} catch (Exception ex) {
					FlansMod.logPackError(file.name, packName, shortName, "Adding propeller failed", split, ex);
				}
			}


			hasFlare = ConfigUtils.configBool(config, "HasFlare", hasFlare);
			flareDelay = Math.max(1, ConfigUtils.configInt(config, "FlareDelay", flareDelay));

			timeFlareUsing = ConfigUtils.configInt(config, "TimeFlareUsing", timeFlareUsing);
			timeFlareUsing = timeFlareUsing <= 0 ? 1 : timeFlareUsing;

			//Sound
			engineSoundLength = ConfigUtils.configInt(config, "PropSoundLength", engineSoundLength);
			engineSound = ConfigUtils.configDriveableSound(packName, config, "PropSound", engineSound);
			shootSoundPrimary = ConfigUtils.configDriveableSound(packName, config, "ShootSound", shootSoundPrimary);
			shootSoundSecondary = ConfigUtils.configDriveableSound(packName, config, "BombSound", shootSoundSecondary);


			//Aesthetics
			hasGear = ConfigUtils.configBool(config, "HasGear", hasGear);
			hasDoor = ConfigUtils.configBool(config, "HasDoor", hasDoor);
			hasWing = ConfigUtils.configBool(config, "HasWing", hasWing);
			foldWingForLand = ConfigUtils.configBool(config, "FoldWingForLand", foldWingForLand);
			flyWithOpenDoor = ConfigUtils.configBool(config, "FlyWithOpenDoor", flyWithOpenDoor);
			autoOpenDoorsNearGround = ConfigUtils.configBool(config, "AutoOpenDoorsNearGround", autoOpenDoorsNearGround);
			autoDeployLandingGearNearGround = ConfigUtils.configBool(config, "AutoDeployLandingGearNearGround", autoDeployLandingGearNearGround);
			restingPitch = ConfigUtils.configFloat(config, "RestingPitch", restingPitch);
			spinWithoutTail = ConfigUtils.configBool(config, "SpinWithoutTail", spinWithoutTail);
			valkyrie = ConfigUtils.configBool(config, "Valkyrie", valkyrie);

            //Animations
            //Wings
			wingPos1 = ConfigUtils.configVector(config, "WingPosition1", wingPos1);
			wingPos2 = ConfigUtils.configVector(config, "WingPosition2", wingPos2);
			wingRot1 = ConfigUtils.configVector(config, "WingRotation1", wingRot1);
			wingRot2 = ConfigUtils.configVector(config, "WingRotation2", wingRot2);
			wingRate = ConfigUtils.configVector(config, "WingRate", wingRate);
			wingRotRate = ConfigUtils.configVector(config, "WingRotRate", wingRotRate);

            //Wing Wheels
			wingWheelPos1 = ConfigUtils.configVector(config, "WingWheelPosition1", wingWheelPos1);
			wingWheelPos2= ConfigUtils.configVector(config, "WingWheelPosition2", wingWheelPos2);
			wingWheelRot1 = ConfigUtils.configVector(config, "WingWheelRotation1", wingWheelRot1);
			wingWheelRot2 = ConfigUtils.configVector(config, "WingWheelRotation2", wingWheelRot2);
			wingWheelRate = ConfigUtils.configVector(config, "WingWheelRate", wingWheelRate);
			wingWheelRotRate = ConfigUtils.configVector(config, "WingWheelRotRate", wingWheelRotRate);

            //Body Wheels
			bodyWheelPos1 = ConfigUtils.configVector(config, "BodyWheelPosition1", bodyWheelPos1);
			bodyWheelPos2 = ConfigUtils.configVector(config, "BodyWheelPosition2", bodyWheelPos2);
			bodyWheelRot1 = ConfigUtils.configVector(config, "BodyWheelRotation1", bodyWheelRot1);
			bodyWheelRot2 = ConfigUtils.configVector(config, "BodyWheelRotation2", bodyWheelRot2);
			bodyWheelRate =  ConfigUtils.configVector(config, "BodyWheelRate", bodyWheelRate);
			bodyWheelRotRate = ConfigUtils.configVector(config, "BodyWheelRotRate", bodyWheelRotRate);

            //Tail Wheels
			tailWheelPos1 = ConfigUtils.configVector(config, "TailWheelPosition1", tailWheelPos1);
			tailWheelPos2 = ConfigUtils.configVector(config, "TailWheelPosition2", tailWheelPos2);
			tailWheelRot1 = ConfigUtils.configVector(config, "TailWheelRotation1", tailWheelRot1);
			tailWheelRot2 = ConfigUtils.configVector(config, "TailWheelRotation2", tailWheelRot2);
			tailWheelRate = ConfigUtils.configVector(config, "TailWheelRate", tailWheelRate);
			tailWheelRotRate = ConfigUtils.configVector(config, "TailWheelRotRate", tailWheelRotRate);

			doorPos1 = ConfigUtils.configVector(config, "DoorPosition1", doorPos1);
			doorPos2 = ConfigUtils.configVector(config, "DoorPosition2", doorPos2);
			doorRot1 = ConfigUtils.configVector(config, "DoorRotation1", doorRot1);
			doorRot2 = ConfigUtils.configVector(config, "DoorRotation2", doorRot2);
			doorRate = ConfigUtils.configVector(config, "DoorRate", doorRate);
			doorRotRate = ConfigUtils.configVector(config, "DoorRotRate", doorRotRate);

            //In-flight inventory
			invInflight = ConfigUtils.configBool(config, "InflightInventory", invInflight);
		}
		catch (Exception ex)
		{
			FlansMod.logPackError(file.name, packName, shortName, "Fatal Error! Reading PlaneType failed", null, ex);
			isValid = true;
		}
	}
    
    @Override
    public int numEngines()
    {
    	switch(mode)
    	{
    		case VTOL: return Math.max(propellers.size(), heliPropellers.size());
    		case PLANE: return propellers.size();
    		case HELI: return heliPropellers.size();
    		default: return 1;
    	}
    }
    
    /** Find the items needed to rebuild a part. The returned array is disconnected from the template items it has looked up */
    @Override
    public ArrayList<ItemStack> getItemsRequired(DriveablePart part, PartType engine)
    {
    	//Get the list of items required by the driveable
    	ArrayList<ItemStack> stacks = super.getItemsRequired(part, engine);
    	//Add the propellers and engines
    	for(Propeller propeller : propellers)
    	{
    		if(propeller.planePart == part.type)
    		{
				if (propeller.itemType != null) {
					stacks.add(new ItemStack(propeller.itemType.item));

				} else {
					FlansMod.logPackError("", packName, shortName, "Couldn't drop propeller! Check it is a valid item", null, null);
				}

				if (engine.item != null) {
					stacks.add(new ItemStack(engine.item));
				} else {
					FlansMod.logPackError("", packName, shortName, "Couldn't drop engine on plane death!", null, null);
				}
    		}
    	}
    	return stacks;
    }
    
	public static PlaneType getPlane(String find)
	{
		for(PlaneType type : types)
		{
			if(type.shortName.equals(find))
				return type;
		}
		return null;
	}
	
	/** To be overriden by subtypes for model reloading */
	public void reloadModel()
	{
		model = FlansMod.proxy.loadModel(modelString, shortName, ModelPlane.class);
	}
}
