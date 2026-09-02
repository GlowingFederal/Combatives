package com.flansmod.common.teams;

import java.util.ArrayList;
import java.util.HashMap;

import com.flansmod.utils.ConfigMap;
import com.flansmod.utils.ConfigUtils;
import net.minecraft.client.model.ModelBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.InfoType;
import com.flansmod.common.types.TypeFile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ArmourBoxType extends InfoType
{
	//Textures
	public String topTexturePath;
	public String sideTexturePath;
	public String bottomTexturePath;
	public IIcon top;
	public IIcon side;
	public IIcon bottom;

	public BlockArmourBox block;

	public ArrayList<ArmourBoxEntry> pages = new ArrayList<>();

	/** The static box map. Indexed by shortName for server ~ client syncing */
	public static HashMap<String, ArmourBoxType> boxes = new HashMap<>();

	public ArmourBoxType(TypeFile file)
	{
		super(file);
	}

	@Override
	public void preRead(TypeFile file) { }

	@Override
	public void postRead(TypeFile file)
	{
		if (this.shortName != null && this.isValid)
		{
			boxes.put(shortName, this);
		}

	}

	@Override
	protected void readLine(String[] split, TypeFile file) {
		if (split.length > 0 && (split[0].equalsIgnoreCase("AddArmour") || split[0].equalsIgnoreCase("AddArmor"))) {
			try {
				StringBuilder name = new StringBuilder(split[2]);

				for(int i = 3; i < split.length; i++)
					name.append(" ").append(split[i]);
				ArmourBoxEntry entry = new ArmourBoxEntry(split[1], name.toString());
				//Read the next 4 lines for each armour piece
				for (int i = 0; i < 4; i++)
				{
					String line;
					line = file.readLine();
					if(line == null)
						continue;
					if(line.startsWith("//"))
					{
						i--;
						continue;
					}
					String[] lineSplit = line.split(" ");

					ArmourType armourType = ArmourType.getArmourType(lineSplit[0]);

					if (armourType != null) {
						entry.armours[i] = armourType;
						for(int j = 0; j < (lineSplit.length - 1) / 2; j++)
						{
							ItemStack recipeElement = null;
							if(lineSplit[j * 2 + 1].contains("."))
								recipeElement = getRecipeElement(lineSplit[j * 2 + 1].split("\\.")[0], Integer.parseInt(lineSplit[j * 2 + 2]), Integer.parseInt(lineSplit[j * 2 + 1].split("\\.")[1]), shortName);
							else
								recipeElement = getRecipeElement(lineSplit[j * 2 + 1], Integer.parseInt(lineSplit[j * 2 + 2]), 0, shortName);

							if(recipeElement != null) {
								entry.requiredStacks[i].add(recipeElement);
							} else {
								FlansMod.logPackError(file.name, packName, shortName, "Could not find item for armour recipe", split, null);
							}
						}
					} else {
						FlansMod.logPackError(file.name, packName, shortName, "Couldn't find armour type for armour box", lineSplit, null);
					}
				}

				pages.add(entry);
			} catch (Exception ex) {
				FlansMod.logPackError(file.name, packName, shortName, "Adding armour to box failed", split, ex);
			}
		}
	}

	@Override
	protected void read(ConfigMap config, TypeFile file)
	{
		super.read(config, file);
		try {
			topTexturePath = ConfigUtils.configString(config, "TopTexture", topTexturePath);
			bottomTexturePath = ConfigUtils.configString(config, "BottomTexture", bottomTexturePath);
			sideTexturePath = ConfigUtils.configString(config, "SideTexture", sideTexturePath);
		} catch (Exception ex) {
			FlansMod.logPackError(file.name, packName, shortName, "Fatal error occurred while reading armour box", null, ex);
			isValid = false;
		}
	}

	/** Each instance of this class refers to one page full of recipes, that is, one full set of armour */
	public class ArmourBoxEntry
	{
		public String shortName;
		public String name;
		public ArmourType[] armours;
		public ArrayList<ItemStack>[] requiredStacks;

		public ArmourBoxEntry(String s, String s1)
		{
			shortName = s;
			name = s1;

			//Prep arrays and lists
			armours = new ArmourType[4];
			requiredStacks = new ArrayList[4];
			for(int i = 0; i < 4; i++)
				requiredStacks[i] = new ArrayList<>();
		}
	}

	public static ArmourBoxType getBox(String boxShortName)
	{
		return boxes.get(boxShortName);
	}

	@Override
	public float GetRecommendedScale()
	{
		return 50.0f;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBase GetModel()
	{
		return null;
	}
}
