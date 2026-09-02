package com.flansmod.common.driveables.mechas;

import java.util.ArrayList;

import com.flansmod.client.model.ModelMecha;
import com.flansmod.common.FlansMod;
import com.flansmod.common.driveables.DriveableType;
import com.flansmod.common.types.TypeFile;
import com.flansmod.common.vector.Vector3f;
import com.flansmod.utils.ConfigMap;
import com.flansmod.utils.ConfigUtils;

public class MechaType extends DriveableType 
{
	/** Movement modifiers */
	public float turnLeftModifier = 1F, turnRightModifier = 1F, moveSpeed = 1F;
	/** If true, this will crush any living entity under the wheels */
	public boolean squashMobs = false;
	/** How many blocks can be stepped up when walking */
	public int stepHeight = 0;
	/** Jump Height (set 0 for no jump) */
	public float jumpHeight = 1F;
	public float jumpVelocity = 1F;
	/** Speed of Rotation */
	public float rotateSpeed = 10F;
	/** Origin of the mecha arms */
	public Vector3f leftArmOrigin, rightArmOrigin;
	/** Length of the mecha arms and legs */
	public float armLength = 1F, legLength = 1F;
	/** The amount to scale the held items / tools by when rendering */
	public float heldItemScale = 1F;
	/** Height and Width of the world collision box */
	public float height = 3F, width = 2F;
	/** The height of chassis above the ground; for use when legs are gone */
	public float chassisHeight = 1F;
	
	/** The default reach of tools. Tools can multiply this base reach as they wish */
	public float reach = 10F;
	
	//Falling
	/** Whether the mecha damages blocks when falling. Can be overriden by upgrades */
	public boolean damageBlocksFromFalling = true;
	/** The size of explosion to cause, per fall damage */
	public float blockDamageFromFalling = 1F;
	
	/** Whether the mecha takes fall damage. Can be overriden by upgrades */
	public boolean takeFallDamage = true;
	/** How much fall damage the mecha takes by default */
	public float fallDamageMultiplier = 1F;
	
	/** Leg Swing Limit */
	public float legSwingLimit = 2F;	

	// Limiting head turning
	public boolean limitHeadTurn = false;
	public float limitHeadTurnValue = 90F;

	// Speed of Leg movement
	public float legSwingTime = 5;

	// Upper/Lower Arm Limit
	public float upperArmLimit = 90;
	public float lowerArmLimit = 90;

	// Modifier for Weapons in Hand
	public float leftHandModifierX = 0;
	public float leftHandModifierY = 0;
	public float leftHandModifierZ = 0;
	public float rightHandModifierX = 0;
	public float rightHandModifierY = 0;
	public float rightHandModifierZ = 0;

	public ArrayList<LegNode> legNodes = new ArrayList<LegNode>();
		
	public float legAnimSpeed = 0;

	public String stompSound = "";	
	public int stompSoundLength = 0;
	public float stompRangeLower = 0F;
	public float stompRangeUpper = 0F;

	public boolean restrictInventoryInput = false;
	public boolean allowMechaToolsInRestrictedInv = true;

	public static ArrayList<MechaType> types = new ArrayList<MechaType>();

	public MechaType(TypeFile file)
	{
		super(file);
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
			//Movement modifiers
			turnLeftModifier = ConfigUtils.configFloat(config, "TurnLeftSpeed", turnLeftModifier);
			turnRightModifier = ConfigUtils.configFloat(config, "TurnRightSpeed", turnRightModifier);
			moveSpeed = ConfigUtils.configFloat(config, "MoveSpeed", moveSpeed);
			squashMobs = ConfigUtils.configBool(config, "SquashMobs", squashMobs);
			stepHeight = ConfigUtils.configInt(config, "StepHeight", stepHeight);

			jumpHeight = ConfigUtils.configFloat(config, "JumpHeight", -99F);
			jumpVelocity = (jumpHeight == -99F) ? 1F : (float) Math.sqrt(Math.abs(9.81F * (jumpHeight + 0.2F) / 200F));

			rotateSpeed = ConfigUtils.configFloat(config, "RotateSpeed", rotateSpeed);
			stompSound = ConfigUtils.configDriveableSound(packName, config, "StompSound", stompSound);
			stompSoundLength = ConfigUtils.configInt(config, "StompSoundLength", stompSoundLength);
			stompRangeLower = ConfigUtils.configFloat(config, "StompRangeLower", stompRangeLower);
			stompRangeUpper = ConfigUtils.configFloat(config, "StompRangeUpper", stompRangeUpper);

			String[] split = null;
			try {
				split = ConfigUtils.getSplitFromKey(config, new String[] { "LeftArmOrigin" });
				if(split != null) {
					leftArmOrigin = new Vector3f(Float.parseFloat(split[1]) / 16F, Float.parseFloat(split[2]) / 16F, Float.parseFloat(split[3]) / 16F);
				}

				split = ConfigUtils.getSplitFromKey(config, new String[] { "RightArmOrigin" });
				if(split != null) {
					rightArmOrigin = new Vector3f(Float.parseFloat(split[1]) / 16F, Float.parseFloat(split[2]) / 16F, Float.parseFloat(split[3]) / 16F);
				}
			} catch (Exception ex) {
				FlansMod.logPackError(file.name, packName, shortName, "Adding arm origin failed", split, ex);
			}


			armLength = ConfigUtils.configFloat(config, "ArmLength", armLength) / 16F;
			legLength = ConfigUtils.configFloat(config, "LegLength", legLength) / 16F;
			heldItemScale = ConfigUtils.configFloat(config, "HeldItemScale", heldItemScale);
			height = ConfigUtils.configFloat(config, "Height", height*16F)/16F;
			width = ConfigUtils.configFloat(config, "Width", width*16F)/16F;

			chassisHeight = (float)Math.floor(ConfigUtils.configFloat(config, "ChassisHeight", chassisHeight*16F))/16F;

			fallDamageMultiplier = ConfigUtils.configFloat(config, "FallDamageMultiplier", fallDamageMultiplier);
			blockDamageFromFalling = ConfigUtils.configFloat(config, "BlockDamageFromFalling", blockDamageFromFalling);
			reach = ConfigUtils.configFloat(config, "Reach", reach);
			takeFallDamage = ConfigUtils.configBool(config, "TakeFallDamage", takeFallDamage);
			damageBlocksFromFalling = ConfigUtils.configBool(config, "DamageBlocksFromFalling", damageBlocksFromFalling);
			legSwingLimit = ConfigUtils.configFloat(config, "LegSwingLimit", legSwingLimit);

			try {
				split = ConfigUtils.getSplitFromKey(config, "LimitHeadTurn");
				if(split != null) {
					limitHeadTurn = Boolean.parseBoolean(split[1].toLowerCase());
					limitHeadTurnValue = Float.parseFloat(split[2]);
				}
			} catch (Exception ex) {
				FlansMod.logPackError(file.name, packName, shortName, "Setting head turn limit failed", split, ex);

			}

			legSwingTime = ConfigUtils.configFloat(config, "LegSwingTime", legSwingTime);
			upperArmLimit = ConfigUtils.configFloat(config, "UpperArmLimit", upperArmLimit);
			lowerArmLimit = ConfigUtils.configFloat(config, "LowerArmLimit", lowerArmLimit);

			try {
				split = ConfigUtils.getSplitFromKey(config, "LeftHandModifier");
				if(split != null) {

					leftHandModifierX = Float.parseFloat(split[1])/16F;
					leftHandModifierY = Float.parseFloat(split[2])/16F;
					leftHandModifierZ = Float.parseFloat(split[3])/16F;
				}

				split = ConfigUtils.getSplitFromKey(config, "RightHandModifier");
				if(split != null) {
					rightHandModifierX = Float.parseFloat(split[1])/16F;
					rightHandModifierY = Float.parseFloat(split[2])/16F;
					rightHandModifierZ = Float.parseFloat(split[3])/16F;
				}
			} catch (Exception ex) {
				FlansMod.logPackError(file.name, packName, shortName, "Setting Hand Modifiers failed", split, ex);
			}

			ArrayList<String[]> splits = ConfigUtils.getSplitsFromKey(config, new String[] { "LegNode"} );
			for (String[] ssplit : splits) {
				try {
					LegNode node = new LegNode();
					node.rotation = Integer.parseInt(ssplit[1]);
					node.lowerBound = Float.parseFloat(ssplit[2]);
					node.upperBound = Float.parseFloat(ssplit[3]);
					node.speed = Integer.parseInt(ssplit[4]);
					node.legPart = Integer.parseInt(ssplit[5]);
					legNodes.add(node);
				} catch (Exception ex) {
					FlansMod.logPackError(file.name, packName, shortName, "Adding LegNode failed", ssplit, ex);
				}
			}

			legAnimSpeed = ConfigUtils.configFloat(config, "LegAnimSpeed", legAnimSpeed);
			restrictInventoryInput = ConfigUtils.configBool(config, "RestrictInventoryInput", restrictInventoryInput);
			allowMechaToolsInRestrictedInv = ConfigUtils.configBool(config, "AllowMechaToolsInRestrictedInv", allowMechaToolsInRestrictedInv);

		} catch (Exception ex) {
			FlansMod.logPackError(file.name, packName, shortName, "Fatal error occurred while reading Mecha Type", null, ex);
			isValid = false;
		}
    }
    
	/** To be overriden by subtypes for model reloading */
	public void reloadModel()
	{
		model = FlansMod.proxy.loadModel(modelString, shortName, ModelMecha.class);
	}
	
	public static MechaType getMecha(String find) {
		for(MechaType type : types) {
			if(type.shortName.equals(find))
				return type;
		}
		return null;
	}
	
	
	public class LegNode {
		public int rotation;
		public float lowerBound;
		public float upperBound;
		public int speed;
		public int legPart;
	}
}
