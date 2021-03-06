package org.samo_lego.golfiv.mixin.illegalActions;

import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.samo_lego.golfiv.casts.Golfer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.OPEN_INVENTORY;
import static org.samo_lego.golfiv.GolfIV.golfConfig;
import static org.samo_lego.golfiv.utils.CheatType.ILLEGAL_ACTIONS;

/**
 * Checks if player is doing impossible actions while having GUI (ScreenHandler) open.
 */
@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_inventoryWalk {

    @Shadow public ServerPlayerEntity player;

    @Unique
    private short illegalActionsMoveAttempts;
    @Unique
    private short illegalActionsLookAttempts;


    /**
     * Sets the status of open GUI to false.
     *
     * @param packet
     * @param ci
     */
    @Inject(
            method = "onCloseHandledScreen(Lnet/minecraft/network/packet/c2s/play/CloseHandledScreenC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;closeScreenHandler()V"
            )
    )
    private void closeHandledScreen(CloseHandledScreenC2SPacket packet, CallbackInfo ci) {
        this.illegalActionsMoveAttempts = 0;
        this.illegalActionsLookAttempts = 0;
        ((Golfer) this.player).setOpenGui(false);
    }

    /**
     * Called when player opens horse inventory.
     * Sets the status of open GUI to true.
     *
     * @param packet
     * @param ci
     */
    @Inject(
            method = "onClientCommand(Lnet/minecraft/network/packet/c2s/play/ClientCommandC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;updateLastActionTime()V"
            )
    )
    private void openScreenHandler(ClientCommandC2SPacket packet, CallbackInfo ci) {
        if(packet.getMode() == OPEN_INVENTORY) {
            ((Golfer) this.player).setOpenGui(golfConfig.main.checkIllegalActions);
        }
    }


    /**
     * Checks for movement while having a GUI open.
     *
     * @param packet
     * @param ci
     */
    @Inject(
            method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void checkInventoryWalk(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        Vec3d packetMovement = new Vec3d(
                packet.getX(this.player.getX()) - this.player.getX(),
                packet.getY(this.player.getY()) - this.player.getY(),
                packet.getZ(this.player.getZ()) - this.player.getZ()
        );
        Vec2f packetLook = new Vec2f(
                packet.getYaw(this.player.yaw) - this.player.yaw,
                packet.getPitch(this.player.pitch) - this.player.pitch
        );
        if(((Golfer) this.player).hasOpenGui() && !player.isFallFlying() && !player.isInsideWaterOrBubbleColumn()) {
            if(packet instanceof PlayerMoveC2SPacket.PositionOnly && packetMovement.getY() == 0 && packetMovement.lengthSquared() != 0) {
                if(++this.illegalActionsMoveAttempts > 40) {
                    ((Golfer) this.player).report(ILLEGAL_ACTIONS);
                    ci.cancel();
                }
                System.out.println("Walk");
            }
            else if(packet instanceof PlayerMoveC2SPacket.LookOnly || packet instanceof PlayerMoveC2SPacket.Both && packetLook.x + packetLook.y != 0) {
                if(++this.illegalActionsLookAttempts > 4) {
                    ((Golfer) this.player).report(ILLEGAL_ACTIONS);
                    ci.cancel();
                }
                System.out.println("Look");
            }
        }
    }

    /**
     * Checks for entity interactions while having a GUI open.
     *
     * @param packet
     * @param ci
     */
    @Inject(
            method = "onPlayerInteractEntity(Lnet/minecraft/network/packet/c2s/play/PlayerInteractEntityC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void entityInteraction(PlayerInteractEntityC2SPacket packet, CallbackInfo ci) {
        if(((Golfer) this.player).hasOpenGui()) {
            ((Golfer) this.player).report(ILLEGAL_ACTIONS);
            ci.cancel();
        }
    }


    /**
     * Checks for messages / commands while having GUI open.
     *
     * @param packet
     * @param ci
     */
    @Inject(
            method = "onGameMessage(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void chatWithInventoryOpened(ChatMessageC2SPacket packet, CallbackInfo ci) {
        if(((Golfer) player).hasOpenGui()) {
            ((Golfer) this.player).report(ILLEGAL_ACTIONS);
            ci.cancel();
        }
    }
}
