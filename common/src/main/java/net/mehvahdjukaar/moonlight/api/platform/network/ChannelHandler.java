package net.mehvahdjukaar.moonlight.api.platform.network;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.function.Function;

/**
 * Your main network channel instance.
 */
//TODO: rename
public abstract class ChannelHandler {

    @ExpectPlatform
    public static ChannelHandler createChannel(ResourceLocation channelMame, int version) {
        throw new AssertionError();
    }

    public static ChannelHandler createChannel(ResourceLocation channelMame) {
        return createChannel(channelMame, 1);
    }

    protected final ResourceLocation channelName;

    protected ChannelHandler(ResourceLocation channelName) {
        this.channelName = channelName;
    }

    public abstract <M extends Message> void register(
            NetworkDir direction,
            Class<M> messageClass,
            Function<FriendlyByteBuf, M> decoder);

    public void setVersion(int version){}


    public interface Context {
        NetworkDir getDirection();

        Player getSender();
    }


    public abstract void sendToClientPlayer(ServerPlayer serverPlayer, Message message);

    public abstract void sendToAllClientPlayers(Message message);

    public abstract void sendToAllClientPlayersInRange(Level level, BlockPos pos, double radius, Message message);

    public abstract void sentToAllClientPlayersTrackingEntity(Entity target, Message message);

    public abstract void sentToAllClientPlayersTrackingEntityAndSelf(Entity target, Message message);

    public abstract void sendToServer(Message message);

}
