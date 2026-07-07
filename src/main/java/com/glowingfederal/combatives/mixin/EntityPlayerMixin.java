package com.glowingfederal.combatives.mixin;

import java.util.EnumMap;
import java.util.Map;

import com.glowingfederal.combatives.entity.EntitySize;
import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityLivingBase implements ICombativesPlayerPose {
    private static final int POSE_WATCHER_ID = 28;
    private static final EntitySize STANDING_SIZE = EntitySize.flexible(0.6F, 1.8F);
    private static final Map<Pose, EntitySize> SIZE_BY_POSE = new EnumMap<Pose, EntitySize>(Pose.class);

    static {
        SIZE_BY_POSE.put(Pose.STANDING, STANDING_SIZE);
        SIZE_BY_POSE.put(Pose.SLEEPING, EntitySize.fixed(0.2F, 0.2F));
        SIZE_BY_POSE.put(Pose.FALL_FLYING, EntitySize.flexible(0.6F, 0.6F));
        SIZE_BY_POSE.put(Pose.SWIMMING, EntitySize.flexible(0.6F, 0.6F));
        SIZE_BY_POSE.put(Pose.SPIN_ATTACK, EntitySize.flexible(0.6F, 0.6F));
        SIZE_BY_POSE.put(Pose.CROUCHING, EntitySize.flexible(0.6F, 1.5F));
        SIZE_BY_POSE.put(Pose.DYING, EntitySize.fixed(0.2F, 0.2F));
    }

    @Shadow public PlayerCapabilities capabilities;
    @Shadow public float prevCameraYaw;
    @Shadow public float cameraYaw;
    @Shadow(remap = false) public float eyeHeight;
    @Shadow public abstract void addMovementStat(double x, double y, double z);

    private boolean eyesInWater;
    private boolean eyesInWaterPlayer;
    private EntitySize combativesSize;
    private float combativesEyeHeight;
    private float previousEyeHeight;
    private float swimAnimation;
    private float lastSwimAnimation;
    private float timeUnderwater;
    private Pose lastLoggedPose = Pose.STANDING;
    private boolean lastLoggedSwimming;
    private boolean crawlKeyDown;

    public EntityPlayerMixin(World world) {
        super(world);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void combatives$constructed(CallbackInfo ci) {
        this.combativesSize = STANDING_SIZE;
        this.combativesEyeHeight = this.getEyeHeight(Pose.STANDING, this.combativesSize);
        this.getDataWatcher().addObject(POSE_WATCHER_ID, Pose.STANDING.ordinal());
    }

    @Override
    public void func_145781_i(int key) {
        if (key == POSE_WATCHER_ID && this.worldObj.isRemote && !this.isRiding()) {
            this.recalculateEyeHeight();
            this.recalculateSize();
        }
        super.func_145781_i(key);
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        if (this.isInWater()) {
            this.timeUnderwater = MathHelper.clamp_float(this.timeUnderwater + 1, 0, 600);
        } else if (this.timeUnderwater > 0) {
            this.timeUnderwater = MathHelper.clamp_float(this.timeUnderwater - 10, 0, 600);
        }
        this.eyesInWater = this.isInsideOfMaterial(Material.water);
        this.updateSwimming();
    }

    @Override
    public boolean canSwim() { return this.eyesInWater && this.isInWater(); }

    @Override
    public void updateSwimming() {
        boolean next = !this.capabilities.isFlying && this.isSprinting() && this.isInWater() && !this.isRiding()
            && (this.isSwimming() || this.canSwim());
        if (next != this.isSwimming()) {
            MovementDiagnostics.debug(this.getPlayer(), next ? "entering swim" : "leaving swim");
        }
        this.setSwimming(next);
    }

    @Override
    public boolean getEyesInWaterPlayer() { return this.eyesInWaterPlayer; }

    @Override
    public float getWaterVision() {
        if (!this.isInWater()) return 0.0F;
        if (this.timeUnderwater >= 600.0F) return 1.0F;
        float fadeIn = MathHelper.clamp_float(this.timeUnderwater / 100.0F, 0.0F, 1.0F);
        float longFade = this.timeUnderwater < 100.0F ? 0.0F : MathHelper.clamp_float((this.timeUnderwater - 100.0F) / 500.0F, 0.0F, 1.0F);
        return fadeIn * 0.6F + longFade * 0.4F;
    }

    @Override public float getPoseWidth() { return this.combativesSize.width; }
    @Override public float getPoseHeight() { return this.combativesSize.height; }
    @Override public EntitySize getSize(Pose pose) { return SIZE_BY_POSE.containsKey(pose) ? SIZE_BY_POSE.get(pose) : STANDING_SIZE; }

    @Override
    public void recalculateSize() {
        EntitySize oldSize = this.combativesSize == null ? STANDING_SIZE : this.combativesSize;
        EntitySize newSize = this.getSize(this.getPose());
        if (this.isResizingAllowed()) {
            this.recalculateSize(oldSize, newSize);
            this.width = newSize.width;
            this.height = newSize.height;
            MovementDiagnostics.debug(this.getPlayer(), "collision/pose state changed to " + this.getPose());
        }
        this.combativesSize = newSize;
    }

    private void recalculateSize(EntitySize oldSize, EntitySize newSize) {
        if (newSize.width < oldSize.width) {
            double half = newSize.width / 2.0D;
            this.boundingBox.setBB(AxisAlignedBB.getBoundingBox(this.posX - half, this.posY, this.posZ - half, this.posX + half, this.posY + newSize.height, this.posZ + half));
        } else {
            AxisAlignedBB box = this.boundingBox;
            this.boundingBox.setBB(AxisAlignedBB.getBoundingBox(box.minX, box.minY, box.minZ, box.minX + newSize.width, box.minY + newSize.height, box.minZ + newSize.width));
            if (newSize.width > oldSize.width && !this.worldObj.isRemote && this.ticksExisted > 0) {
                float distance = oldSize.width - newSize.width;
                this.moveEntity(distance, 0.0D, distance);
            }
        }
    }

    private void recalculateEyeHeight() {
        Pose pose = this.getPose();
        this.combativesEyeHeight = this.getEyeHeight(pose, this.getSize(pose));
        this.previousEyeHeight = this.eyeHeight;
    }

    @Override
    public boolean isResizingAllowed() {
        float delta = 0.025F;
        AxisAlignedBB box = this.boundingBox;
        if (this.width < delta || this.height < delta || box.maxX - box.minX < delta || box.maxY - box.minY < delta) return true;
        return Math.abs(this.width / this.getPoseWidth() - 1.0F) < delta && Math.abs(this.height / this.getPoseHeight() - 1.0F) < delta
            && Math.abs((box.maxX - box.minX) / this.getPoseWidth() - 1.0F) < delta && Math.abs((box.maxY - box.minY) / this.getPoseHeight() - 1.0F) < delta;
    }

    private float getEyeHeight(Pose pose, EntitySize size) { return pose == Pose.SLEEPING || pose == Pose.DYING ? 0.2F : this.getStandingEyeHeight(pose, size); }
    @Override public boolean isActuallySneaking() { return this.isSneaking(); }
    @Override public float getStandingEyeHeight(Pose pose, EntitySize size) { return pose == Pose.CROUCHING ? 0.35F : this.eyeHeight; }

    @Override public void setPose(Pose pose) { this.getDataWatcher().updateObject(POSE_WATCHER_ID, pose.ordinal()); }
    @Override public Pose getPose() { int id = this.getDataWatcher().getWatchableObjectInt(POSE_WATCHER_ID); return id >= 0 && id < Pose.values().length ? Pose.values()[id] : Pose.STANDING; }
    @Override public boolean isPoseClear(Pose pose) { return this.worldObj.getCollidingBoundingBoxes(this, this.getBoundingBox(pose)).isEmpty(); }
    @Override public boolean getShouldBeDead() { return this.deathTime > 0; }
    @Override public boolean isSwimming() { return !this.capabilities.isFlying && this.getFlag(6); }
    @Override public boolean isActuallySwimming() { return this.getPose() == Pose.SWIMMING || this.getPose() == Pose.FALL_FLYING; }
    @SideOnly(Side.CLIENT) @Override public boolean isVisuallySwimming() { return this.isActuallySwimming() && !this.isInWater(); }
    @Override public void setSwimming(boolean swimming) { this.setFlag(6, swimming); }
    @Override public float getSwimAnimation(float partialTicks) { return this.lastSwimAnimation + partialTicks * (this.swimAnimation - this.lastSwimAnimation); }
    @Override public boolean canCrawl() { return !this.isRiding() && !this.capabilities.isFlying && !this.isOnLadder() && !this.getShouldBeDead() && !this.isPlayerSleeping(); }
    @Override public boolean isCrawlKeyDown() { return this.canCrawl() && this.crawlKeyDown; }
    @Override public void setCrawlKeyDown(boolean down) {
        if (down && !this.canCrawl()) {
            MovementDiagnostics.debug(this.getPlayer(), "crawl rejected: player state disallows crawling");
            this.crawlKeyDown = false;
            return;
        }
        if (this.crawlKeyDown != down) {
            MovementDiagnostics.debug(this.getPlayer(), "crawl request " + (down ? "accepted" : "released"));
        }
        this.crawlKeyDown = down;
    }

    @Inject(method = "getEyeHeight", at = @At("HEAD"), cancellable = true)
    private void combatives$getEyeHeight(CallbackInfoReturnable<Float> cir) {
        if (this.combativesEyeHeight > 0.0F) {
            cir.setReturnValue(this.combativesEyeHeight);
        }
    }

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "cpw/mods/fml/common/FMLCommonHandler.onPlayerPostTick(Lnet/minecraft/entity/player/EntityPlayer;)V", shift = At.Shift.BEFORE, remap = false))
    private void combatives$prePostTick(CallbackInfo ci) {
        this.lastSwimAnimation = this.swimAnimation;
        this.swimAnimation = this.isActuallySwimming() ? Math.min(1.0F, this.swimAnimation + 0.09F) : Math.max(0.0F, this.swimAnimation - 0.09F);
        this.eyesInWaterPlayer = this.isInsideOfMaterial(Material.water);
    }

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "cpw/mods/fml/common/FMLCommonHandler.onPlayerPostTick(Lnet/minecraft/entity/player/EntityPlayer;)V", shift = At.Shift.AFTER, remap = false))
    private void combatives$postPostTick(CallbackInfo ci) {
        this.updatePose();
        if (this.eyeHeight != this.previousEyeHeight) this.recalculateEyeHeight();
    }

    private void updatePose() {
        Pose pose = this.getPose();
        if (this.getShouldBeDead()) pose = Pose.DYING;
        else if (this.isPlayerSleeping()) pose = Pose.SLEEPING;
        else if (this.isPoseClear(Pose.SWIMMING)) {
            if (this.isCrawlKeyDown() || this.isSwimming()) {
                pose = Pose.SWIMMING;
                if (this.worldObj.isRemote) this.yOffset = 0.28F;
            } else if (!this.isPoseClear(Pose.STANDING)) {
                pose = this.isPoseClear(Pose.CROUCHING) ? Pose.CROUCHING : Pose.SWIMMING;
                MovementDiagnostics.debug(this.getPlayer(), "pose blocked by collision; keeping low pose");
            } else if (this.isActuallySneaking() && !this.capabilities.isFlying && (this.onGround || !this.isInWater()) && !this.isOnLadder()) {
                pose = Pose.CROUCHING;
                if (this.worldObj.isRemote) this.yOffset = 1.62F;
            } else if (this.isPoseClear(Pose.STANDING)) {
                if (!this.worldObj.isRemote) {
                    this.removePotionEffect(Potion.moveSlowdown.id);
                    this.removePotionEffect(Potion.digSlowdown.id);
                }
                pose = Pose.STANDING;
                if (this.worldObj.isRemote) this.yOffset = 1.62F;
            }
            if (!this.noClip && !this.isRiding() && this.isResizingAllowed() && !this.isPoseClear(pose)) {
                MovementDiagnostics.debug(this.getPlayer(), "pose blocked by collision: " + pose);
                pose = this.isPoseClear(Pose.CROUCHING) ? Pose.CROUCHING : Pose.SWIMMING;
            }
        }
        if (pose != this.getPose()) {
            if (pose == Pose.SWIMMING) MovementDiagnostics.debug(this.getPlayer(), this.isSwimming() ? "entering swim" : "entering crawl");
            if (this.getPose() == Pose.SWIMMING && pose != Pose.SWIMMING) MovementDiagnostics.debug(this.getPlayer(), this.lastLoggedSwimming ? "leaving swim" : "leaving crawl");
        }
        boolean poseChanged = pose != this.getPose();
        this.lastLoggedSwimming = this.isSwimming();
        this.setPose(pose);
        if (poseChanged) MovementDiagnostics.debug(this.getPlayer(), "pose synced " + (this.worldObj.isRemote ? "client" : "server") + ": " + pose);
        this.lastLoggedPose = pose;
        this.recalculateSize();
    }

    private AxisAlignedBB getBoundingBox(Pose pose) {
        EntitySize size = this.getSize(pose);
        float half = size.width / 2.0F;
        return AxisAlignedBB.getBoundingBox(this.posX - half, this.posY - this.yOffset + this.ySize, this.posZ - half, this.posX + half, this.posY - this.yOffset + this.ySize + size.height, this.posZ + half);
    }

    @Inject(method = "moveEntityWithHeading", at = @At("HEAD"), cancellable = true)
    private void combatives$moveEntityWithHeading(float strafe, float forward, CallbackInfo ci) {
        double startX = this.posX, startY = this.posY, startZ = this.posZ;
        if (this.isSwimming() && !this.isRiding()) {
            double lookY = this.getLookVec().yCoord;
            double factor = lookY < -0.2D ? 0.085D : 0.06D;
            Block block = this.worldObj.getBlock((int)this.posX, (int)(this.posY + 0.9D), (int)this.posZ);
            if (lookY <= 0.0D || this.isJumping || block instanceof BlockLiquid || block instanceof IFluidBlock) this.motionY += (lookY - this.motionY) * factor;
        }
        double savedMotionY = this.motionY;
        float savedJumpMovement = this.jumpMovementFactor;
        if (this.capabilities.isFlying && !this.isRiding()) this.jumpMovementFactor = this.capabilities.getFlySpeed() * (this.isSprinting() ? 2.0F : 1.0F);
        if (!this.capabilities.isFlying && this.isInWater()) {
            float drag = this.isSprinting() ? 0.9F : 0.8F;
            this.moveFlying(strafe, forward, 0.02F);
            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            if (this.isCollidedHorizontally && this.isOnLadder()) this.motionY = 0.2D;
            this.motionX *= drag;
            this.motionY *= 0.8D;
            this.motionZ *= drag;
            if (!this.isSprinting()) this.motionY -= 0.005D;
            this.updateCombativesLimbSwing();
        } else {
            super.moveEntityWithHeading(strafe, forward);
        }
        if (this.capabilities.isFlying && !this.isRiding()) {
            this.motionY = savedMotionY * 0.6D;
            this.jumpMovementFactor = savedJumpMovement;
            this.fallDistance = 0.0F;
        }
        this.addMovementStat(this.posX - startX, this.posY - startY, this.posZ - startZ);
        ci.cancel();
    }

    private void updateCombativesLimbSwing() {
        this.prevLimbSwingAmount = this.limbSwingAmount;
        double dx = this.posX - this.prevPosX;
        double dz = this.posZ - this.prevPosZ;
        float amount = MathHelper.sqrt_double(dx * dx + dz * dz) * 4.0F;
        if (amount > 1.0F) amount = 1.0F;
        this.limbSwingAmount += (amount - this.limbSwingAmount) * 0.4F;
        this.limbSwing += this.limbSwingAmount;
    }

    @Inject(method = "onLivingUpdate", at = @At("TAIL"))
    private void combatives$onLivingUpdate(CallbackInfo ci) {
        float yaw = 0.0F;
        if (this.onGround && !this.getShouldBeDead() && !this.isSwimming()) yaw = Math.min(0.1F, MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ));
        this.cameraYaw = this.prevCameraYaw + (yaw - this.prevCameraYaw) * 0.4F;
        this.cameraPitch = 0.0F;
    }

    @Redirect(method = "sleepInBedAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;setSize(FF)V"))
    private void combatives$sleepSize(EntityPlayer player, float width, float height) {
        this.setPose(Pose.SLEEPING);
    }

    private EntityPlayer getPlayer() { return (EntityPlayer)(Object)this; }
}
