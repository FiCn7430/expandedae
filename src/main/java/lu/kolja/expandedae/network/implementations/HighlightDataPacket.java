package lu.kolja.expandedae.network.implementations;

import appeng.api.stacks.AEKey;
import lu.kolja.expandedae.definition.ExpLang;
import lu.kolja.expandedae.helper.cpu.IHighlightMenu;
import lu.kolja.expandedae.highlight.BlockHighlightHandler;
import lu.kolja.expandedae.network.ExpPacket;
import lu.kolja.expandedae.network.PacketInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@PacketInfo(NetworkDirection.PLAY_TO_CLIENT)
public record HighlightDataPacket(BlockPos pos, ResourceKey<Level> level, long time) implements ExpPacket<HighlightDataPacket> {
    public HighlightDataPacket() {
        this(null, null, 0);
    }

    @Override
    public void encode(HighlightDataPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeResourceKey(packet.level);
        buf.writeLong(packet.time);
    }

    @Override
    public HighlightDataPacket decode(FriendlyByteBuf buf) {
        return new HighlightDataPacket(buf.readBlockPos(), buf.readResourceKey(Registries.DIMENSION), buf.readLong());
    }

    @Override
    public void handle(HighlightDataPacket packet, Supplier<NetworkEvent.Context> context) {
        var ctx = context.get();
        var pos = packet.pos;
        var level = packet.level;
        ctx.enqueueWork(() -> {
            BlockHighlightHandler.highlight(packet.pos, packet.level, packet.time);
            Minecraft.getInstance().player.sendSystemMessage(ExpLang.HIGHLIGHTED_BLOCK.text(
                    Component.literal("§b" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()),
                    Component.literal("§a" + level.location()))
            );
        });
        ctx.setPacketHandled(true);
    }

    @PacketInfo(NetworkDirection.PLAY_TO_SERVER)
    public record HighlightWhat(AEKey key) implements ExpPacket<HighlightWhat> {
        public HighlightWhat() {
            this(null);
        }

        @Override
        public void encode(HighlightWhat packet, FriendlyByteBuf buf) {
            AEKey.writeKey(buf, packet.key);
        }

        @Override
        public HighlightWhat decode(FriendlyByteBuf buf) {
            return new HighlightWhat(AEKey.readKey(buf));
        }

        @Override
        public void handle(HighlightWhat packet, Supplier<NetworkEvent.Context> context) {
            var ctx = context.get();
            ctx.enqueueWork(() -> {
                var sp = ctx.getSender();
                if (sp != null && sp.containerMenu instanceof IHighlightMenu highlightMenu) {
                    highlightMenu.eae$highlight(packet.key);
                }
            });
        }
    }
}