package net.dualwielding.network;

import net.dualwielding.DualWieldingMain;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AttackEntityPayload(int entityId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<AttackEntityPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(DualWieldingMain.MODID, "attack_entity"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AttackEntityPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            AttackEntityPayload::entityId,
            AttackEntityPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
