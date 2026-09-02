package com.flansmod.common.paintjob;

import java.util.ArrayList;
import java.util.HashMap;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.InfoType;
import com.flansmod.common.types.TypeFile;

import com.flansmod.utils.ConfigMap;
import com.flansmod.utils.ConfigUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public abstract class PaintableType extends InfoType
{
	//Paintjobs
	/** The list of all available paintjobs for this gun */
	public ArrayList<Paintjob> paintjobs = new ArrayList<>();
	/** The default paintjob for this gun. This is created automatically in the load process from existing info */
	public Paintjob defaultPaintjob;
	/** Whether to add this paintjob to the paintjob table, gunmode table e.t.c. */
	public Boolean addAnyPaintjobToTables = true;
	/** Assigns IDs to paintjobs */
	private int nextPaintjobID = 1;

	public PaintableType(TypeFile file)
	{
		super(file);
	}

	@Override
	public void preRead(TypeFile file)
	{
	
	}
	
	@Override
	public void postRead(TypeFile file)
	{
		//After all lines have been read, set up the default paintjob
		defaultPaintjob = new Paintjob(0, "default", iconPath, texture, new ItemStack[0], true);
		//Move to a new list to ensure that the default paintjob is always first
		ArrayList<Paintjob> newPaintjobList = new ArrayList<>();
		newPaintjobList.add(defaultPaintjob);
		newPaintjobList.addAll(paintjobs);
		paintjobs = newPaintjobList;
	}
	
	/** Pack reader */
	protected void read(ConfigMap config, TypeFile file)
	{
		super.read(config, file);
		//iconName textureName [dyeName dyeAmount (dyeDamage)]


		try {
			ArrayList<String[]> lines = ConfigUtils.getSplitsFromKey(config, new String[] { "PaintJob"} );
			for (String[] split : lines) {
				try {
					int numDyes = (split.length - 2) / 2;
					ItemStack[] dyeStacks = new ItemStack[numDyes];
					for(int i = 0; i < numDyes; i++) {
						int dyeID = getDyeDamageValue(split[i * 2 + 3]);

						if (dyeID == -1) {
							FlansMod.logPackError(file.name, packName, shortName, "Unknown dye name for Paintjob", split, null);
						}

						// Do different behaviour here?
						dyeStacks[i] = new ItemStack(Items.dye, Integer.parseInt(split[i * 2 + 4]), dyeID);
					}

					//if(split[1].contains("_"))
					//{
					//	String[] splat = split[1].split("_");
					//	if(splat[0].equals(iconPath))
					//		split[1] = splat[1];
					//}
					paintjobs.add(new Paintjob(nextPaintjobID++, split[1], split[2], dyeStacks, true));
				} catch (Exception e) {
					FlansMod.logPackError(file.name, packName, shortName, "Reading paintjob line failed", split, e);
				}
			}

			lines = ConfigUtils.getSplitsFromKey(config, new String[] { "AdvPaintJob"} );
			for (String[] split : lines) {
				try {
					ItemStack[] dyeStacks = new ItemStack[(split.length - 4) / 2];
					for(int i = 0; i < (split.length - 4) / 2; i++) {
						int dyeID = getDyeDamageValue(split[i * 2 + 4]);

						if (dyeID == -1) {
							FlansMod.logPackError(file.name, packName, shortName, "Unknown dye name for AdvPaintjob", split, null);
						}

						// Do different behaviour here?
						dyeStacks[i] = new ItemStack(Items.dye, Integer.parseInt(split[i * 2 + 5]), dyeID);
					}


					paintjobs.add(new Paintjob(nextPaintjobID++, split[1], split[2], split[3], dyeStacks, true));
				} catch (Exception e) {
					FlansMod.logPackError(file.name, packName, shortName, "Reading advanced paintjob line failed", split, e);
				}
			}

			lines = ConfigUtils.getSplitsFromKey(config, new String[] { "AddPaintableToTables" });

			for (String[] split : lines) {
				try {
					if (split.length == 2) {
						addAnyPaintjobToTables = Boolean.parseBoolean(split[1]);
					} else if (split.length == 3) {
						String paintjobId = split[1];

						for (Paintjob paintjob : paintjobs) {
							if (paintjob.textureName.equals(paintjobId)) {
								paintjob.addToTables = Boolean.parseBoolean(split[2]);
							}
						}
					}
				} catch (Exception e) {
					FlansMod.logPackError(file.name, packName, shortName, "Paintable table configuration failed", split, e);
				}
			}
		} catch (Exception e) {
			FlansMod.logPackError(file.name, packName, shortName, "Fatal error occurred while reading paintjob", null, e);
			isValid = false;
		}
	}
	
	public Paintjob getPaintjob(int i)
	{
		// This needs to be fixed properly, see https://trello.com/c/c8ssBecf
		// Basically, invalid paintjob ID gets saved (ie the original removed from packs), and crashes when trying to load it
		if (i >= paintjobs.size()) {
			return defaultPaintjob;
		}

		return paintjobs.get(i);
	}

	public float GetRecommendedScale()
	{
		return 50.0f;
	}
}