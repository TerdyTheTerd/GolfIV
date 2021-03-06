package org.samo_lego.golfiv.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.samo_lego.golfiv.casts.Golfer;
import org.samo_lego.golfiv.utils.BallLogger;
import org.samo_lego.golfiv.utils.CheatType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixinCast_golfer implements Golfer {

    private final PlayerEntity player = (PlayerEntity) (Object) this;

    @Unique
    private boolean blockCollisions, entityCollisions, hasOpenScreen;

    /**
     * Real onGround value, which isn't affected
     * by the client packet.
     *
     * @return true if player is near ground (0.5001 block tolerance), otherwise false.
     */
    @Override
    public boolean isNearGround() {
        return blockCollisions || entityCollisions;
    }

    /**
     * Sets whether player has block collisions.
     *
     * @param blockCollisions whether player has block collisions.
     */
    @Override
    public void setBlockCollisions(boolean blockCollisions) {
        this.blockCollisions = blockCollisions;
    }

    /**
     * Sets whether player has entity collisions (e. g. boat collisions).
     *
     * @param entityCollisions whether player has entity collisions.
     */
    @Override
    public void setEntityCollisions(boolean entityCollisions) {
        this.entityCollisions = entityCollisions;
    }

    /**
     * Tells whether player has entity collisions.
     *
     * @return true if player has entity collisions, otherwise false.
     */
    @Override
    public boolean hasEntityCollisions() {
        return entityCollisions;
    }

    /**
     * Reports player for cheating / kicks them.
     * (will be changed in future)
     *
     * @param cheatType type of the cheat player has used.
     */
    @Override
    public void report(CheatType cheatType) {
        if(player instanceof ServerPlayerEntity) {
            final ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
            if(golfConfig.logging.toConsole) {
                BallLogger.logInfo(player.getGameProfile().getName() + " is probably using " + cheatType.getCheat() + " hack(s).");
            }
        }

        if(player instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) player).networkHandler.disconnect(new LiteralText(
                    "§3[GolfIV]\n§a" +
                            golfConfig.kickMessages.get(new Random().nextInt(golfConfig.kickMessages.size()
                            ))
            ));
        }
        int meesages = golfConfig.kickMessages.size();
        if(meesages > 0)
            player.sendMessage(
                    new LiteralText(
                            "§3[GolfIV]\n§a" +
                                    golfConfig.kickMessages.get(
                                            new Random().nextInt(meesages)
                                    )
                    ),
                    false
            );
    }

    /**
     * Sets whether player has opened GUI.
     * Doesn't catch opening their own inventory.
     *
     * @param openGui whether player has opened the GUI.
     */
    @Override
    public void setOpenGui(boolean openGui) {
        System.out.println("inv. Was open: " + this.hasOpenScreen + ", setting to " + openGui);
        this.hasOpenScreen = openGui;
    }

    /**
     * Tells whether player has open GUI.
     * Doesn't catch their own inventory being open.
     *
     * @return true if player has open GUI, otherwise false
     */
    @Override
    public boolean hasOpenGui() {
        return this.hasOpenScreen;
    }

    /**
     * Checks for entity collisions.
     *
     * @param entity colliding entity
     * @param ci callbackInfo
     */
    @Inject(method = "collideWithEntity(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"))
    private void updateCollision(Entity entity, CallbackInfo ci) {
        if(entity.isCollidable()) {
            if(entity.equals(this.player.getVehicle())) {
                this.setEntityCollisions(false);
            }
            else if(!this.entityCollisions && !this.player.hasVehicle()) {
                this.setEntityCollisions(true);
            }
        }
    }
}
