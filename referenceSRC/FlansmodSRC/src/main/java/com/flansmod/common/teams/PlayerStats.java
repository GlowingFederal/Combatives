package com.flansmod.common.teams;

import com.flansmod.common.FlansMod;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Saves player stats for teams events.
 */
@SuppressWarnings("DataFlowIssue")
public class PlayerStats {
    public static final File STATS_DIR = new File(MinecraftServer.getServer().getEntityWorld().getSaveHandler().getWorldDirectory() + "\\FlansMod players statistics\\");

    public String nickname = "NaN";
    public int kills = 0; //done
    public int deaths = 0; //done
    public int exp = 0; //done
    public int totalExp = 0; //done
    public int rank = 1; //done
    public double avg = 0; //done
    public double longestKill = 0; //done
    public int playedRounds = 0; //done
    public double playTime = 0;
    public int MVPCount = 0; //done
    public int capturedFlags = 0; //done
    public int savedFlags = 0; //done
    public int vehiclesDestroyed = 0; //done 75%

    /**
     * Creates a new stats instance for a player.
     *
     * @param player Player whose stats to track.
     */
    public PlayerStats(EntityPlayerMP player) {
        nickname = player.getCommandSenderName();
        savePlayerStats();
    }

    private PlayerStats() {
    }

    /**
     * Gets a player from a username.
     *
     * @param username String username.
     * @return Returns an {@link EntityPlayerMP} instance or null if not found.
     */
    public EntityPlayerMP getPlayer(String username) {
        return MinecraftServer.getServer().getConfigurationManager().func_152612_a(username);
    }

    /**
     * Saves the current stats into an NBT file.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void savePlayerStats() {
        STATS_DIR.mkdirs();
        STATS_DIR.setReadable(true);
        STATS_DIR.setWritable(true);
        File file = new File(STATS_DIR, nickname + " " + getPlayer(nickname).getUniqueID().toString() + ".dat");
        STATS_DIR.mkdirs();
        STATS_DIR.setReadable(true);
        STATS_DIR.setWritable(true);
        TeamsManager.checkFileExists(file);
        try {
            NBTTagCompound tags = new NBTTagCompound();

            tags.setString("Nickname", nickname);
            tags.setInteger("Kills", kills);
            tags.setInteger("Deaths", deaths);
            tags.setInteger("Exp", exp);
            tags.setInteger("Total Exp", totalExp);
            tags.setInteger("Rank", rank);
            tags.setDouble("AVG", avg);
            tags.setDouble("Longest Kill", longestKill);
            tags.setInteger("Rounds Played", playedRounds);
            tags.setDouble("Play Time", playTime);
            tags.setInteger("MVP Count", MVPCount);
            tags.setInteger("Flags Captured", capturedFlags);
            tags.setInteger("Flags Saved", savedFlags);
            tags.setInteger("Vehicles Destroyed", vehiclesDestroyed);


            CompressedStreamTools.write(tags, new DataOutputStream(Files.newOutputStream(file.toPath())));
        } catch (Exception e) {
            FlansMod.log("Failed to save to teams.dat");
            FlansMod.logger.error(e);
        }
    }

    //TODO: Check if XP should be saved as an int or float.
    public void addExp(float a) {
        exp += a;
        totalExp += a;
        if (exp >= 1000) {
            rank++;
            exp = exp - 1000;
        }
    }

    public void updateLongestKill(float distance) {
        addExp(distance / 10);
        if (distance > longestKill) {
            longestKill = distance;
        }
    }

    public void updateAVG() {
        avg = (double) kills / playedRounds;
    }

    public static PlayerStats getPlayerStatsFromFile(String name) {
        PlayerStats toSend = new PlayerStats();
        for (File file : STATS_DIR.listFiles()) {
            if (file.getName().startsWith(name)) {
                TeamsManager.checkFileExists(file);
                try {
                    NBTTagCompound tags = CompressedStreamTools.read(new DataInputStream(Files.newInputStream(file.toPath())));
                    toSend.nickname = tags.getString("Nickname");
                    toSend.kills = tags.getInteger("Kills");
                    toSend.deaths = tags.getInteger("Deaths");
                    toSend.exp = tags.getInteger("Exp");
                    toSend.totalExp = tags.getInteger("Total Exp");
                    toSend.rank = tags.getInteger("Rank");
                    toSend.avg = tags.getDouble("AVG");
                    toSend.longestKill = tags.getDouble("Longest Kill");
                    toSend.playedRounds = tags.getInteger("Rounds Played");
                    toSend.playTime = tags.getDouble("Play Time");
                    toSend.MVPCount = tags.getInteger("MVP Count");
                    toSend.capturedFlags = tags.getInteger("Flags Captured");
                    toSend.savedFlags = tags.getInteger("Flags Saved");
                    toSend.vehiclesDestroyed = tags.getInteger("Vehicles Destroyed");
                    return toSend;
                } catch (Exception e) {
                    FlansMod.log("Failed to save to teams.dat");
                    FlansMod.logger.error(e);
                    return null;
                }
            }
        }
        return null;
    }

    public static void printLeaderboardExp(ICommandSender sender) {
        List<String> nameList = new ArrayList<>();
        File dir = new File(MinecraftServer.getServer().getEntityWorld().getSaveHandler().getWorldDirectory() + "\\FlansMod players statistics\\");
        for (File file : dir.listFiles()) {
            TeamsManager.checkFileExists(file);
            try {
                NBTTagCompound tags = CompressedStreamTools.read(new DataInputStream(Files.newInputStream(file.toPath())));
                String nickname = tags.getString("Nickname");
                nameList.add(nickname);
            } catch (Exception e) {
                FlansMod.log("Failed to print leaderboard");
                FlansMod.logger.error(e);
            }
        }
        Collections.sort(nameList, new ComparatorExp());
        int counter = 0;
        for (String name : nameList) {
            PlayerStats stats = getPlayerStatsFromFile(name);
            if (stats == null) continue;
            switch (counter) {
                case 0:
                    sender.addChatMessage(new ChatComponentText("\u00a76\u00a7l1. " + name + " - Rank " + stats.rank + "(" + stats.totalExp + " Exp)"));
                    break;
                case 1:
                    sender.addChatMessage(new ChatComponentText("\u00a74\u00a7l2. " + name + " - Rank " + stats.rank + "(" + stats.totalExp + " Exp)"));
                    break;
                case 2:
                    sender.addChatMessage(new ChatComponentText("\u00a7a\u00a7l3. " + name + " - Rank " + stats.rank + "(" + stats.totalExp + " Exp)"));
                    break;
                case 3:
                    sender.addChatMessage(new ChatComponentText("\u00a7l4. " + name + " - Rank " + stats.rank + "(" + stats.totalExp + " Exp)"));
                    break;
                case 4:
                    sender.addChatMessage(new ChatComponentText("\u00a7l5. " + name + " - Rank " + stats.rank + "(" + stats.totalExp + " Exp)"));
                    break;
                case 5:
                    sender.addChatMessage(new ChatComponentText("\u00a7l6. " + name + " - Rank " + stats.rank + "(" + stats.totalExp + " Exp)"));
                    break;
                case 6:
                    sender.addChatMessage(new ChatComponentText("\u00a7l7. " + name + " - Rank " + stats.rank + "(" + stats.totalExp + " Exp)"));
                    break;
                case 7:
                    sender.addChatMessage(new ChatComponentText("\u00a7l8. " + name + " - Rank " + stats.rank + "(" + stats.totalExp + " Exp)"));
                    break;
                case 8:
                    sender.addChatMessage(new ChatComponentText("\u00a7l9. " + name + " - Rank " + stats.rank + "(" + stats.totalExp + " Exp)"));
                    break;
                case 9:
                    sender.addChatMessage(new ChatComponentText("\u00a7l10. " + name + " - Rank " + stats.rank + "(" + stats.totalExp + " Exp)"));
                    break;
                case 10:
                    sender.addChatMessage(new ChatComponentText("\u00a7b\u00a7l" + nameList.indexOf(sender.getCommandSenderName()) + 1 + ". " + sender.getCommandSenderName() + " - Rank " + stats.rank + "(" + stats.totalExp + " Exp)"));
                    break;
            }
            counter++;
            if (counter >= 11) break;
        }
    }

    public static List<PlayerStats> getAllPlayersStats() {
        List<PlayerStats> listToSend = new ArrayList<>();
        if (STATS_DIR.listFiles() == null) return null;
        for (File file : STATS_DIR.listFiles()) {
            TeamsManager.checkFileExists(file);
            try {
                PlayerStats toSend = new PlayerStats();
                NBTTagCompound tags = CompressedStreamTools.read(new DataInputStream(Files.newInputStream(file.toPath())));
                toSend.nickname = tags.getString("Nickname");
                toSend.kills = tags.getInteger("Kills");
                toSend.deaths = tags.getInteger("Deaths");
                toSend.exp = tags.getInteger("Exp");
                toSend.totalExp = tags.getInteger("Total Exp");
                toSend.rank = tags.getInteger("Rank");
                toSend.avg = tags.getDouble("AVG");
                toSend.longestKill = tags.getDouble("Longest Kill");
                toSend.playedRounds = tags.getInteger("Rounds Played");
                toSend.playTime = tags.getDouble("Play Time");
                toSend.MVPCount = tags.getInteger("MVP Count");
                toSend.capturedFlags = tags.getInteger("Flags Captured");
                toSend.savedFlags = tags.getInteger("Flags Saved");
                toSend.vehiclesDestroyed = tags.getInteger("Vehicles Destroyed");
                listToSend.add(toSend);
            } catch (Exception e) {
                FlansMod.logException("Failed to save to teams.dat", e);
                return null;
            }
        }
        return listToSend;
    }

    public static int getPlayerLvl(EntityPlayerMP player) {
        PlayerStats stats = getPlayerStatsFromFile(player.getCommandSenderName());
        if (stats != null) {
            return stats.totalExp / 1000;
        } else {
            stats = new PlayerStats(player);
            ;
        }
        return stats.totalExp / 1000;
    }
}

class ComparatorExp implements Comparator<String> {
    @Override
    public int compare(String a, String b) {
        PlayerStats info1 = PlayerStats.getPlayerStatsFromFile(a);
        PlayerStats info2 = PlayerStats.getPlayerStatsFromFile(b);
        if (info1 == null || info2 == null)
            return 0;
        return info2.totalExp - info1.totalExp;
    }
}
