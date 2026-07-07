package com.fuzs.aquaacrobatics.mixins.early.minecraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fuzs.aquaacrobatics.config.ConfigHandler;
import com.fuzs.aquaacrobatics.entity.IBubbleColumnInteractable;
import com.fuzs.aquaacrobatics.entity.IRockableBoat;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mixin(EntityBoat.class) // this needs more testing
public abstract class EntityBoatMixin extends Entity implements IBubbleColumnInteractable, IRockableBoat {

    private boolean aqua$rocking;
    private boolean aqua$rockingDownwards;
    private float rockingIntensity;
    private float rockingAngle;
    private float prevRockingAngle;

    public EntityBoatMixin(World worldIn) {
        super(worldIn);
    }

    public void onEnterBubbleColumnWithAirAbove(boolean downwards) {
        if (!worldObj.isRemote) {
            this.aqua$rocking = true;
            this.aqua$rockingDownwards = downwards;
            if (this.getRockingTicks() == 0) {
                this.setRockingTicks(60);
            }
        }

        this.worldObj.spawnParticle(
            "splash",
            this.posX + (double) this.rand.nextFloat(),
            this.posY + 0.7D,
            this.posZ + (double) this.rand.nextFloat(),
            0.0D,
            0.0D,
            0.0D);
        if (this.rand.nextInt(20) == 0) {
            this.worldObj.playSound(
                this.posX,
                this.posY,
                this.posZ,
                this.getSplashSound(),
                1.0F,
                0.8F + 0.4F * this.rand.nextFloat(),
                false);
        }
    }

    @Override
    public void aqua$doRegisterData() {
        this.getDataWatcher()
            .addObject(ConfigHandler.MiscellaneousConfig.BoatId, 0);
    }

    @Inject(
        method = "onUpdate",
        at = @At(value = "INVOKE", target = "net/minecraft/entity/item/EntityBoat.setRotation(FF)V", ordinal = 1))
    private void updateRocking(CallbackInfo ci) {
        if (this.worldObj.isRemote) {
            int i = this.getRockingTicks();
            if (i > 0) {
                this.rockingIntensity += 0.05F;
            } else {
                this.rockingIntensity -= 0.1F;
            }

            this.rockingIntensity = MathHelper.clamp_float(this.rockingIntensity, 0.0F, 1.0F);
            this.prevRockingAngle = this.rockingAngle;
            this.rockingAngle = 10.0F * (float) Math.sin((double) (0.5F * (float) this.worldObj.getTotalWorldTime()))
                * this.rockingIntensity;
        } else {
            if (!this.aqua$rocking) {
                this.setRockingTicks(0);
            }

            int k = this.getRockingTicks();
            if (k > 0) {
                --k;
                this.setRockingTicks(k);
                int j = 60 - k - 1;
                if (j > 0 && k == 0) {
                    this.setRockingTicks(0);
                    if (this.aqua$rockingDownwards) {
                        this.motionY -= 0.7D;
                        this.removePassengers();
                    } else {
                        this.motionY = this.aqua$isPlayerRiding() ? 2.7D : 0.6D;
                    }
                }

                this.aqua$rocking = false;
            }
        }

    }

    private boolean aqua$isPlayerRiding() {
        for (Entity entity : this.getPassengers()) {
            if (EntityPlayer.class.isAssignableFrom(entity.getClass())) {
                return true;
            }
        }

        return false;
    }

    public void setRockingTicks(int p_203055_1_) {
        if (!ConfigHandler.MiscellaneousConfig.bubbleColumns) return;
        this.dataWatcher.updateObject(ConfigHandler.MiscellaneousConfig.BoatId, p_203055_1_);
    }

    public int getRockingTicks() {
        if (!ConfigHandler.MiscellaneousConfig.bubbleColumns) return 0;
        return this.dataWatcher.getWatchableObjectInt(ConfigHandler.MiscellaneousConfig.BoatId);
    }

    @SideOnly(Side.CLIENT)
    public float getRockingAngle(float partialTicks) {
        if (!ConfigHandler.MiscellaneousConfig.bubbleColumns) return 0.0f;
        return this.prevRockingAngle + (this.rockingAngle - this.prevRockingAngle) * partialTicks;
    }

    private void removePassengers() {
        if (this.riddenByEntity != null) {
            this.riddenByEntity.mountEntity(null);
            this.riddenByEntity = null;
        }
    }

    private List<Entity> getPassengers() {
        List<Entity> passengers = new ArrayList<>();
        if (this.riddenByEntity != null) {
            passengers.add(this.riddenByEntity);
        }
        return passengers;
    }
}
