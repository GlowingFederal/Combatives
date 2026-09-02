package com.flansmod.common.teams;

import java.util.ArrayList;

import com.flansmod.client.model.ModelCustomArmour;
import com.flansmod.common.FlansMod;
import com.flansmod.common.types.InfoType;
import com.flansmod.common.types.TypeFile;

import com.flansmod.utils.ConfigMap;
import com.flansmod.utils.ConfigUtils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;

public class ArmourType extends InfoType {
    public static ArrayList<ArmourType> armours = new ArrayList<ArmourType>();

    /**
     * 0 = Helmet, 1 = Chestplate, 2 = Legs, 3 = Shoes
     */
    public int type;
    /**
     * The amount of damage to absorb. From 0 to 1. Stacks additively between armour pieces
     */
    public double defence;

    /**
     * The amount of damage to absorb. From 0 to 1. Stacks additively between armour
     * pieces. For bullet damage specifically.
     */
    public double bulletDefence;

    /**
     * How good the armour is at stopping bullets. Same units as bullet penetration. Default 0 to emulate previous behaviour
     */
    public float penetrationResistance = 0;
    /**
     * The name for the armour texture. Texture path/name is assets/flansmod/armor/<armourTextureName>_1.png or _2 for legs
     */
    public String armourTextureName;
    /**
     * Modifiers for various player stats
     */
    public float moveSpeedModifier = 1F, knockbackModifier = 0.2F, jumpModifier = 1F;
    /**
     * If true, then the player gets a night vision buff every couple of seconds
     */
    public boolean nightVision = false;
    /**
     * If true, then the player gets a invisiblity buff every couple of seconds
     */
    public boolean invisible = false;
    /**
     * The overlay to display when using this helmet. Textures are pulled from the scopes directory
     */
    public String overlay = null;
    /**
     * If true, then smoke effects from grenades will have no effect on players wearing this
     */
    public boolean smokeProtection = false;
    /**
     * If true, the player will not receive fall damage
     */
    public boolean negateFallDamage = false;
    /**
     * If true, the player will not receive fire damage
     */
    public boolean fireResistance = false;
    /**
     * If true, the player can breath under water
     */
    public boolean waterBreathing = false;
    /**
     * If true, the player can walk on water
     */
    public boolean onWaterWalking = false;
    /**
     * If true, the armor has durability
     */
    public boolean hasDurability = false;
    /**
     * The durability for the piece of armor
     */
    public int durability = 0;

    public ModelCustomArmour model;

    public ArmourType(TypeFile file) {
        super(file);
    }

    @Override
    protected void preRead(TypeFile file) {
    }

    @Override
    public void postRead(TypeFile file) {
        if (this.shortName != null && isValid) {
            armours.add(this);
        }
    }

    @Override
    protected void read(ConfigMap config, TypeFile file) {
        super.read(config, file);
        try {

            if (FMLCommonHandler.instance().getSide().isClient()) {
                model = FlansMod.proxy.loadModel(modelString, shortName, ModelCustomArmour.class);
            }

            if (model != null) {
                model.type = this;
            }

            String typeName = ConfigUtils.configString(config, "Type", null);
            if (typeName == null) {
                type = 0;
                FlansMod.log("Armour %s in pack %s with unknown type detected. Assuming Helmet.", shortName, packName);
            } else {
                switch (typeName) {
                    case "Hat": case "Helmet":
                        type=0; break;

                    case "Chest": case "Body":
                        type=1; break;

                    case "Legs": case "Pants":
                        type=2; break;

                    case "Shoes": case "Boots":
                        type=3; break;
                }
            }

            // If bulletDefence is set, we allow the normal defence to be overriden
            defence = ConfigUtils.configFloat(config, new String[] {"OtherDefence", "Defence", "DamageReduction"}, (float)defence);
            bulletDefence = ConfigUtils.configFloat(config, "BulletDefence", (float)defence);


            moveSpeedModifier = ConfigUtils.configFloat(config, new String[]{"MoveSpeedModifier", "Slowness"}, moveSpeedModifier);
            jumpModifier = ConfigUtils.configFloat(config, "JumpModifier", jumpModifier);
            knockbackModifier = ConfigUtils.configFloat(config, new String[]{"KnockbackReduction", "KnockbackModifier"}, knockbackModifier);
            penetrationResistance = ConfigUtils.configFloat(config, "PenetrationResistance", penetrationResistance);
            nightVision = ConfigUtils.configBool(config, "NightVision", nightVision);
            invisible = ConfigUtils.configBool(config, "Invisible", invisible);
            negateFallDamage = ConfigUtils.configBool(config, "NegateFallDamage", negateFallDamage);
            fireResistance = ConfigUtils.configBool(config, "FireResistance", fireResistance);
            waterBreathing = ConfigUtils.configBool(config, "WaterBreathing", waterBreathing);
            overlay = ConfigUtils.configString(config, "Overlay", overlay);
            smokeProtection = ConfigUtils.configBool(config, "SmokeProtection", smokeProtection);
            onWaterWalking = ConfigUtils.configBool(config, "OnWaterWalking", onWaterWalking);

            durability = ConfigUtils.configInt(config, "Durability", durability);
            hasDurability = durability > 0;

            armourTextureName = ConfigUtils.configString(config, new String[]{"ArmourTexture", "ArmorTexture"}, armourTextureName);
        } catch (Exception ex) {
            FlansMod.logPackError(file.name, packName, shortName, "Fatal error occurred while reading armour file", null, ex);
            isValid = false;
        }
    }

    public static ArmourType getArmourType(String string) {
        for (ArmourType armour : armours) {
            if (armour.shortName.equals(string))
                return armour;
        }
        return null;
    }

    /**
     * To be overriden by subtypes for model reloading
     */
    public void reloadModel() {
        model = FlansMod.proxy.loadModel(modelString, shortName, ModelCustomArmour.class);

        if (model != null)
            model.type = this;
    }

    @Override
    public float GetRecommendedScale() {
        return 50.0f;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBase GetModel() {
        return model;
    }
}
