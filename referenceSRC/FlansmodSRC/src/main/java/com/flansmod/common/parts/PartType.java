package com.flansmod.common.parts;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.EnumType;
import com.flansmod.common.types.InfoType;
import com.flansmod.common.types.TypeFile;
import com.flansmod.utils.ConfigMap;
import com.flansmod.utils.ConfigUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PartType extends InfoType {
    /**
     * Category (TODO : Replace with Enum)
     */
    public int category = 0;
    /**
     * Max stack size of item
     */
    public int stackSize = 0;
    /**
     * (Engine) Multiplier applied to the thrust of the driveable
     */
    public float engineSpeed = 1.0F;
    /**
     * (Engine) Rate at which this engine consumes fuel
     */
    public float fuelConsumption = 1.0F;
    /**
     * (Engine) Power output of the engine - if using realistic acceleration.
     */
    public float enginePower = 10F;
    /**
     * (Fuel) The amount of fuel this fuel tank gives
     */
    public int fuel = 0;
    /**
     * The types of driveables that this engine works with. Used to designate some engines as mecha CPUs and whatnot
     */
    public List<EnumType> worksWith = Arrays.asList(EnumType.mecha, EnumType.plane, EnumType.vehicle);

    public ArrayList<ItemStack> partBoxRecipe = new ArrayList<>();

    //------- RedstoneFlux -------
    /**
     * If true, then this engine will draw from RedstoneFlux power source items such as power cubes. Otherwise it will draw from Flan's Mod fuel items
     */
    public boolean useRFPower = false;
    /**
     * The power draw rate for RF (per tick)
     */
    public int RFDrawRate = 1;
    //-----------------------------

    /**
     * The default engine (normally the first one read by the type loader) for driveables with corrupt nbt or those spawned in creative
     */
    public static HashMap<EnumType, PartType> defaultEngines = new HashMap<>();
    /**
     * The list of all PartTypes
     */
    public static List<PartType> parts = new ArrayList<>();

    public PartType(TypeFile file) {
        super(file);
    }

    @Override
    protected void preRead(TypeFile file) {

    }


    @Override
    public void postRead(TypeFile file) {
        if (category == 2 && !useRFPower && isValid) {
            for (EnumType type : worksWith) {
                //If there is already a default engine for this type, compare and see if this one is better
                if (defaultEngines.containsKey(type)) {
                    PartType possiblyInferiorEngine = defaultEngines.get(type);
                    if (isInferiorEngine(possiblyInferiorEngine))
                        defaultEngines.put(type, this);
                } else {
                    defaultEngines.put(type, this);
                }
            }
        }

        if (this.shortName != null && isValid) {
            parts.add(this);
        }
    }

    @Override
    protected void read(ConfigMap config, TypeFile file) {
        super.read(config, file);
        try {
            // Generic
            category = getCategory(ConfigUtils.configString(config, "Category", "Cockpit"));
            stackSize = ConfigUtils.configInt(config, "StackSize", stackSize);

            // Engine
            fuelConsumption = ConfigUtils.configFloat(config, "FuelConsumption", engineSpeed);
            engineSpeed = ConfigUtils.configFloat(config, "EngineSpeed", engineSpeed);
            enginePower = ConfigUtils.configFloat(config, "EnginePower", enginePower);
            //RedstoneFlux, for engines
            useRFPower = ConfigUtils.configBool(config, new String[]{"UseRF", "UseRFPower"}, useRFPower);
            RFDrawRate = ConfigUtils.configInt(config, "RFDrawRate", RFDrawRate);
            // Engine compatibility

            ArrayList<String[]> splits = ConfigUtils.getSplitsFromKey(config, new String[] { "WorksWith" });
            try {
                if (splits.size() > 0) {
                    worksWith = new ArrayList<EnumType>();
                }
                for (String[] split : splits) {
                    for (int i=1; i<split.length; i++) {
                        EnumType type = EnumType.get(split[i]);
                        if (type == null) {
                            FlansMod.logPackError(file.name, packName, shortName, "type not found for part WorksWith", split, null);
                        } else {
                            worksWith.add(type);
                        }
                    }
                }
            } catch (Exception ex) {
                FlansMod.logPackError(file.name, packName, shortName, "Error thrown wile processing WorksWith", null, ex);
            }

            // Fuel cans
            fuel = ConfigUtils.configInt(config, "Fuel", fuel);

            //Recipe
            String[] split = ConfigUtils.getSplitFromKey(config, "PartBoxRecipe");
            try {
                if (split != null) {
                    ItemStack[] stacks = new ItemStack[(split.length - 2) / 2];
                    for (int i = 0; i < (split.length - 2) / 2; i++) {
                        int amount = Integer.parseInt(split[2 * i + 2]);
                        boolean damaged = split[2 * i + 3].contains(".");
                        String itemName = damaged ? split[2 * i + 3].split("\\.")[0] : split[2 * i + 3];
                        int damage = damaged ? Integer.parseInt(split[2 * i + 3].split("\\.")[1]) : 0;

                        ItemStack recipeElement = getRecipeElement(itemName, amount, damage, shortName);

                        if (recipeElement == null) {
                            FlansMod.logPackError(file.name, packName, shortName, "Could not find item for PartBoxRecipe", split, null);
                        }

                        stacks[i] = recipeElement;
                    }
                    partBoxRecipe.addAll(Arrays.asList(stacks));
                }
            } catch (Exception ex) {
                FlansMod.logPackError(file.name, packName, shortName, "Error thrown while constructing PartBoxRecipe for part", split, ex);
            }

        } catch (Exception ex) {
            FlansMod.logPackError(file.name, packName, shortName, "Fatal error thrown while reading part", null, ex);
            isValid = false;
        }
    }

    public boolean isInferiorEngine(PartType quitePossiblyAnInferiorEngine) {
        return engineSpeed > quitePossiblyAnInferiorEngine.engineSpeed;
    }

    public static PartType getPart(String s) {
        for (PartType part : parts) {
            if (part.shortName.equals(s))
                return part;
        }
        return null;
    }

    private int getCategory(String s) {
        switch (s) {
            case "Cockpit":
                return 0;
            case "Wing":
                return 1;
            case "Engine":
                return 2;
            case "Propeller":
                return 3;
            case "Bay":
                return 4;
            case "Tail":
                return 5;
            case "Wheel":
                return 6;
            case "Chassis":
                return 7;
            case "Turret":
                return 8;
            case "Fuel":
                return 9;
            default: // "Misc"
                return 10;
        }
    }

    @Override
    public float GetRecommendedScale() {
        return 50.0f;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBase GetModel() {
        return null;
    }
}