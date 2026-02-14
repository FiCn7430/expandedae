package lu.kolja.expandedae.network.implementations;

import lu.kolja.expandedae.helper.misc.IMaxAmount;
import lu.kolja.expandedae.network.ExpPacket;
import lu.kolja.expandedae.network.PacketInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@PacketInfo(NetworkDirection.PLAY_TO_CLIENT)
public record SetMaxAmountPacket(long maxAmount) implements ExpPacket<SetMaxAmountPacket> {
    public SetMaxAmountPacket() {
        this(0);
    }

    @Override
    public void encode(SetMaxAmountPacket packet, FriendlyByteBuf buf) {
        buf.writeVarLong(packet.maxAmount);
    }

    @Override
    public SetMaxAmountPacket decode(FriendlyByteBuf buf) {
        return new SetMaxAmountPacket(buf.readVarLong());
    }

    @Override
    public void handle(SetMaxAmountPacket packet, Supplier<NetworkEvent.Context> context) {
        var ctx = context.get();
        ctx.enqueueWork(() -> {
            var screen = Minecraft.getInstance().screen;
            if (screen instanceof IMaxAmount maxAmount) {
                maxAmount.eae$setMaxAmount(packet.maxAmount);
            }
        });
    }
}
