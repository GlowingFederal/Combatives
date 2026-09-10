package com.glowingfederal.combatives.movement;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.entity.player.EntityPlayer;

/** Optional common-side jump policies, registered during mod initialization. */
public final class JumpRestrictions {
    private static final Map<String, Predicate<EntityPlayer>> BLOCKERS =
            new LinkedHashMap<String, Predicate<EntityPlayer>>();

    private JumpRestrictions() { }

    /** Providers must use server-owned policy and must not mutate player movement. */
    public static void register(String id, Predicate<EntityPlayer> blocker) {
        if (id == null || blocker == null) throw new IllegalArgumentException("Missing jump policy");
        BLOCKERS.put(id, blocker);
    }

    public static boolean shouldRejectJump(EntityPlayer player) {
        if (player == null) return false;
        for (Predicate<EntityPlayer> blocker : BLOCKERS.values()) {
            if (blocker.test(player)) return true;
        }
        return false;
    }
}
