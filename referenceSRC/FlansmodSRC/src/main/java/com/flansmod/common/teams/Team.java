package com.flansmod.common.teams;

import java.util.*;

import com.flansmod.utils.ConfigMap;
import com.flansmod.utils.ConfigUtils;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.flansmod.common.FlansMod;
import com.flansmod.common.PlayerData;
import com.flansmod.common.PlayerHandler;
import com.flansmod.common.types.EnumType;
import com.flansmod.common.types.InfoType;
import com.flansmod.common.types.TypeFile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Team extends InfoType
{
	public static List<Team> teams = new ArrayList<>();
	public List<String> members = new ArrayList<>();
	//public List<ITeamBase> bases = new ArrayList<ITeamBase>();
	public List<PlayerClass> classes = new ArrayList<>();
	
	public static Team spectators;

	public int score = 0;
	
	public int teamColour = 0xffffff;
	public char textColour = 'f';
	public boolean allowedForRoundsGenerator = false;
	
	public ItemStack hat;
	public ItemStack chest;
	public ItemStack legs;
	public ItemStack shoes;
	
	public Team(String s, String s1, int teamCol, char textCol) {
		super(new TypeFile(EnumType.team, s, "", false));
		shortName = s;
		name = s1;
		teamColour = teamCol;
		textColour = textCol;
	}
	
	public Team(TypeFile file) {
		super(file);

	}

	@Override
	protected void preRead(TypeFile file) {

	}
	@Override
	protected void postRead(TypeFile file)
	{
		if (this.shortName != null && isValid) {
			teams.add(this);
		}
	}

	@Override
	protected void read(ConfigMap config, TypeFile file)
	{
		super.read(config, file);
		try
		{
			String[] split = ConfigUtils.getSplitFromKey(config, "TeamColour");
			if (split != null) {
				teamColour = (Integer.parseInt(split[1]) << 16) + ((Integer.parseInt(split[2])) << 8) + ((Integer.parseInt(split[3])));
			}

			textColour = getColourCode(ConfigUtils.configString(config, "TextColour", "Black"));

			String hatShortName = ConfigUtils.configString(config, new String[] { "Hat", "Helmet" }, null);
			if(hatShortName != null && !hatShortName.equalsIgnoreCase("None")) {
				for(ItemTeamArmour item : FlansMod.armourItems)
				{
					ArmourType armour = item.type;
					if(armour != null && armour.shortName.equals(hatShortName))
						hat = new ItemStack(item);
				}
			}

			String chestShortName = ConfigUtils.configString(config, new String[] { "Chest", "Top" }, null);
			if(chestShortName != null && !chestShortName.equalsIgnoreCase("None")) {
				for(ItemTeamArmour item : FlansMod.armourItems)
				{
					ArmourType armour = item.type;
					if(armour != null && armour.shortName.equals(chestShortName))
						chest = new ItemStack(item);
				}
			}

			String legsShortName = ConfigUtils.configString(config, new String[] { "Legs", "Bottom" }, null);
			if(legsShortName != null && !legsShortName.equalsIgnoreCase("None")) {
				for(ItemTeamArmour item : FlansMod.armourItems)
				{
					ArmourType armour = item.type;
					if(armour != null && armour.shortName.equals(legsShortName))
						legs = new ItemStack(item);
				}
			}

			String shoesShortName = ConfigUtils.configString(config, new String[] { "Shoes", "Boots" }, null);
			if(shoesShortName != null && !shoesShortName.equalsIgnoreCase("None")) {
				for(ItemTeamArmour item : FlansMod.armourItems)
				{
					ArmourType armour = item.type;
					if(armour != null && armour.shortName.equals(shoesShortName))
						shoes = new ItemStack(item);
				}
			}

			List<String[]> splits = ConfigUtils.getSplitsFromKey(config, new String [] { "AddDefaultClass", "AddClass"});
			for (String[] aSplit : splits) {
				try {
					if (aSplit.length == 2) {
						PlayerClass playerClass = PlayerClass.getClass(aSplit[1]);

						if (playerClass != null)
						{
							classes.add(playerClass);
						}
						else
						{
							FlansMod.logPackError(file.name, packName, shortName, "Could not find PlayerClass for AddClass", split, null);
						}
					}
					else
					{
						FlansMod.logPackError(file.name, packName, shortName, "Wrong number of arguments given to AddClass", split, null);
					}
				} catch (Exception ex) {
					FlansMod.logPackError(file.name, packName, shortName, "Adding AddClass failed", split, ex);
				}
			}

			allowedForRoundsGenerator = ConfigUtils.configBool(config, "AllowedForRoundsGenerator", allowedForRoundsGenerator);
		} catch (Exception e)
		{
			FlansMod.log("Reading team file failed.");
			e.printStackTrace();
			isValid = false;
		}
	}
	
	public static Team getTeam(String s)
	{
		for(Team team : teams)
		{
			if(team.shortName.equals(s))
				return team;
		}

		if (FlansMod.spectators.shortName.equals(s)) {
			return  FlansMod.spectators;
		}

		return null;
	}

	private static char getColourCode(String colour) {
		char textCode;
		switch(colour) {
			case "Blue":
				textCode = '1';
				break;
			case "Green":
				textCode = '2';
				break;
			case "Aqua":
				textCode = '3';
				break;
			case "Purple":
				textCode = '5';
				break;
			case "Orange":
				textCode = '6';
				break;
			case "LGrey":
				textCode = '7';
				break;
			case "Grey":
				textCode = '8';
				break;
			case "LBlue":
				textCode = '9';
				break;
			case "LGreen":
				textCode = 'a';
				break;
			case "LAqua":
				textCode = 'b';
				break;
			case "Red":
				textCode = 'c';
				break;
			case "Pink":
				textCode = 'd';
				break;
			case "Yellow":
				textCode = 'e';
				break;
			case "White":
				textCode = 'f';
				break;
			default: // Black
				textCode = '0';
				break;
		}

		return textCode;
	}
	/*
	//Called both by ops and the gametype
	public void addBase(ITeamBase base)
	{
		bases.add(base);
	}
	
	//Called both by ops and the gametype
	public void removeBase(ITeamBase base)
	{
		bases.remove(base);
	}
	*/
	
	public void removePlayer(EntityPlayer player) {
		removePlayer(player.getCommandSenderName());
	}
	
	public String removePlayer(String username) {
		members.remove(username);
		if(PlayerHandler.getPlayerData(username) != null)
			PlayerHandler.getPlayerData(username).team = null;
		return username;
	}
	
	public EntityPlayer addPlayer(EntityPlayer player) {
		addPlayer(player.getCommandSenderName());
		return player;
	}
	
	public String addPlayer(String username) {
		ArrayList<String> list = new ArrayList<>();
		list.add(username);
		for(Team team : teams)
		{
			team.members.removeAll(list);
		}
		members.add(username);
		PlayerHandler.getPlayerData(username).newTeam = PlayerHandler.getPlayerData(username).team = this;
		return username;
	}
	
	public String removeWorstPlayer() {
		sortPlayers();
		if(members.size() == 0)
			return null;
		else return removePlayer(members.get(members.size() - 1));
	}
	
	public void sortPlayers() {
		Collections.sort(members, new ComparatorScore());
	}
	
	public static class ComparatorScore implements Comparator<String> {
		@Override
		public int compare(String a, String b) {
			PlayerData dataA = PlayerHandler.getPlayerData(a);
			PlayerData dataB = PlayerHandler.getPlayerData(b);
			if(dataA == null || dataB == null)
				return 0;			
			return dataB.score - dataA.score;
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
