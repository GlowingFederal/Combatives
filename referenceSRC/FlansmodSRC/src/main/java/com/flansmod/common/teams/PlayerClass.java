package com.flansmod.common.teams;

import com.flansmod.common.FlansMod;
import com.flansmod.common.guns.AttachmentType;
import com.flansmod.common.guns.GunType;
import com.flansmod.common.guns.ItemGun;
import com.flansmod.common.guns.ShootableType;
import com.flansmod.common.paintjob.Paintjob;
import com.flansmod.common.types.InfoType;
import com.flansmod.common.types.TypeFile;
import com.flansmod.utils.ConfigMap;
import com.flansmod.utils.ConfigUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class PlayerClass extends InfoType {
    public static List<PlayerClass> classes = new ArrayList<>();

    public List<String[]> startingItemStrings = new ArrayList<>();
    public List<ItemStack> startingItems = new ArrayList<>();
    public int lvl = 0;

    /**
     * Override armour. If this is set, then it will override the team armour
     */
    public ItemStack hat, chest, legs, shoes;

    public PlayerClass(TypeFile file) {
        super(file);
    }

    @Override
    protected void read(ConfigMap config, TypeFile file) {
        super.read(config, file);

        try {
            startingItemStrings = ConfigUtils.getSplitsFromKey(config, new String[]{"AddItem"});

            lvl = ConfigUtils.configInt(config, "UnlockLevel", lvl);
            texture = ConfigUtils.configString(config, "SkinOverride", texture);

            try {
                String hatShortName = ConfigUtils.configString(config, new String[]{"Hat", "Helmet"}, null);
                if (hatShortName != null && !hatShortName.equalsIgnoreCase("None")) {
                    ArmourType potentialHat = ArmourType.getArmourType(hatShortName);
                    if (potentialHat == null) {
                        FlansMod.logPackError(file.name, packName, shortName, "Hat type not found for PlayerClass", new String[]{"Hat/Helmet", hatShortName}, null);
                    } else {
                        hat = new ItemStack(potentialHat.item);
                    }
                }
            } catch (Exception ex) {
                FlansMod.logPackError(file.name, packName, shortName, "Error thrown while adding Helmet to player class", null, ex);
            }

            try {
                String chestShortName = ConfigUtils.configString(config, new String[]{"Chest", "Top"}, null);
                if (chestShortName != null && !chestShortName.equalsIgnoreCase("None")) {
                    ArmourType potentialChest = ArmourType.getArmourType(chestShortName);
                    if (potentialChest == null) {
                        FlansMod.logPackError(file.name, packName, shortName, "Chest/Top type not found for PlayerClass", new String[]{"Chest/Top", chestShortName}, null);
                    } else {
                        chest = new ItemStack(potentialChest.item);
                    }
                }
            } catch (Exception ex) {
                FlansMod.logPackError(file.name, packName, shortName, "Error thrown while adding Chest/Top to player class", null, ex);
            }

            try {
                String legsShortName = ConfigUtils.configString(config, new String[]{"Legs", "Bottom"}, null);
                if (legsShortName != null && !legsShortName.equalsIgnoreCase("None")) {
                    ArmourType potentialLegs = ArmourType.getArmourType(legsShortName);
                    if (potentialLegs == null) {
                        FlansMod.logPackError(file.name, packName, shortName, "Legs/Bottom type not found for PlayerClass", new String[]{"Legs/Bottom", legsShortName}, null);
                    } else {
                        legs = new ItemStack(potentialLegs.item);
                    }
                }
            } catch (Exception ex) {
                FlansMod.logPackError(file.name, packName, shortName, "Error thrown while adding Legs/Bottom to player class", null, ex);
            }

            try {
                String shoesShortName = ConfigUtils.configString(config, new String[]{"Shoes", "Boots"}, null);
                if (shoesShortName != null && !shoesShortName.equalsIgnoreCase("None")) {
                    ArmourType potentialShoes = ArmourType.getArmourType(shoesShortName);
                    if (potentialShoes == null) {
                        FlansMod.logPackError(file.name, packName, shortName, "Shoes/Boots type not found for PlayerClass", new String[]{"Shoes/Boots", shoesShortName}, null);
                    } else {
                        shoes = new ItemStack(potentialShoes.item);
                    }
                }
            } catch (Exception ex) {
                FlansMod.logPackError(file.name, packName, shortName, "Error thrown while adding Shoes/Boots to player class", null, ex);
            }
        } catch (Exception ex) {
            FlansMod.logPackError(file.name, packName, shortName, "Fatal error thrown while reading player class", null, ex);
            isValid = false;
        }
    }


    @Override
    protected void preRead(TypeFile file) {
    }

    /**
     * This loads the items once for clients connecting to remote servers, since the clients can't tell what attachments a gun has in the GUI and they need to load it at least once
     */
    @Override
    protected void postRead(TypeFile file) {
        if (this.shortName != null && isValid) {
            classes.add(this);

            onWorldLoad(null);
        }

    }

    /**
     * In the loading phase item IDs are all up in the air and so too are NBT tags regarding ItemStacks
     * So to avoid guns with attachments having their attachments replaced with incorrect ones,
     * random guns and other silly things, we read the relevant lines here, as the world loads
     */
    @Override
    public void onWorldLoad(World world) {
        if (world != null && world.isRemote)
            return;
        try {
            startingItems.clear();
            long start = System.currentTimeMillis();
            for (String[] split : startingItemStrings) {
                Item matchingItem = null;
                int amount = 1;
                int damage = 0;
                String[] itemNames = split[1].split("\\+");

                // Attempt to match item from Flan's Mod or vanilla
                Item vanillaMatch = (Item) Item.itemRegistry.getObject(itemNames[0]);
                if (vanillaMatch != null)
                    matchingItem = vanillaMatch;
                else {
                    Item flansMatch = (Item) Item.itemRegistry.getObject(FlansMod.MODID + ":" + itemNames[0]);
                    if (flansMatch != null)
                        matchingItem = flansMatch;
                }

                // If no match found (not all items match their registry name with their unlocalized name),
                // iterate through the item registry and compare unlocalized names.
                if (matchingItem == null) {
                    for (Object object : Item.itemRegistry) {
                        Item item = (Item) object;
                        if (item != null && item.getUnlocalizedName() != null && (item.getUnlocalizedName().equals(itemNames[0]) || (item.getUnlocalizedName().split("\\.").length > 1 && item.getUnlocalizedName().split("\\.")[1].equals(itemNames[0]))))
                            matchingItem = item;
                    }
                }
                for (InfoType type : InfoType.infoTypes) {
                    if (type.shortName.equals(itemNames[0]) && type.item != null) {
                        matchingItem = type.item;
                    }
                }
                if (matchingItem == null) {
                    FlansMod.log("Tried to add " + split[1] + " to player class " + shortName + " but the item did not exist");
                    return;
                }
                if (split.length > 2) {
                    amount = Integer.parseInt(split[2]);
                }
                if (split.length > 3) {
                    damage = Integer.parseInt(split[3]);
                }
                ItemStack stack = new ItemStack(matchingItem, amount, damage);
                if (itemNames.length > 1 && matchingItem instanceof ItemGun) {
                    ItemGun itemGun = (ItemGun) matchingItem;
                    GunType gunType = itemGun.type;
                    NBTTagCompound tags = new NBTTagCompound();
                    NBTTagCompound attachmentTags = new NBTTagCompound();
                    int genericID = 0;
                    for (int i = 0; i < itemNames.length - 1; i++) {
                        AttachmentType attachment = AttachmentType.getAttachment(itemNames[i + 1]);
                        if (attachment != null) {
                            String tagName = null;
                            switch (attachment.type) {
                                case sights:
                                    tagName = "scope";
                                    break;
                                case barrel:
                                    tagName = "barrel";
                                    break;
                                case stock:
                                    tagName = "stock";
                                    break;
                                case grip:
                                    tagName = "grip";
                                    break;
                                case generic:
                                    tagName = "generic_" + genericID++;
                                    break;
                                case accessory:
                                    break;
                                case gadget:
                                    break;
                                case pump:
                                    break;
                                case slide:
                                    break;
                            }
                            NBTTagCompound specificAttachmentTags = new NBTTagCompound();
                            new ItemStack(attachment.item).writeToNBT(specificAttachmentTags);
                            attachmentTags.setTag(tagName, specificAttachmentTags);
                        }
                        //Maybe it was a paintjob
                        else {
                            Paintjob paintjob = gunType.getPaintjob(itemNames[i + 1]);
                            if (paintjob != null)
                                tags.setString("Paint", paintjob.iconName);
                        }
                    }
                    tags.setTag("attachments", attachmentTags);
                    stack.stackTagCompound = tags;

                    // Load the gun with default ammo.
                    ShootableType ammo = gunType.getDefaultAmmo();
                    if (ammo != null) {
                        ItemStack ammoStack = new ItemStack(ammo.item);
                        ammoStack.stackSize = 1;
                        itemGun.setBulletItemStack(stack, ammoStack, 0);
                    }
                }
                startingItems.add(stack);
            }
            // Log how long it took to load
            long end = System.currentTimeMillis() - start;
            if (!startingItemStrings.isEmpty())
                FlansMod.log("Loaded PlayerClass for " + name + " (" + packName + ") in " + end + " ms");
        } catch (Exception e) {
            FlansMod.log("Interpreting player class file failed.");
            FlansMod.logger.error(e);
        }
    }

    public static PlayerClass getClass(String s) {
        for (PlayerClass playerClass : classes) {
            if (playerClass.shortName.equals(s))
                return playerClass;
        }
        return null;
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
