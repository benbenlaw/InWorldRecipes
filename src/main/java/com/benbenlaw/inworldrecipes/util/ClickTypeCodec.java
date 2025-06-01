package com.benbenlaw.inworldrecipes.util;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.Locale;


//Move to core 1.22
public class ClickTypeCodec {

    public static final Codec<ClickType> CLICK_TYPE_CODEC = Codec.STRING.xmap(

            s -> switch (s.toLowerCase(Locale.ROOT)) {
                case "left", "left_click" -> ClickType.LEFT_CLICK;
                case "right", "right_click" -> ClickType.RIGHT_CLICK;
                default -> throw new IllegalArgumentException("Unknown ClickType: " + s);
            },

            ClickType::name
    );

    public static void writeToBuffer(RegistryFriendlyByteBuf buffer, ClickType clickType) {
        buffer.writeEnum(clickType);
    }

    public static ClickType readFromBuffer(RegistryFriendlyByteBuf buffer) {
        return buffer.readEnum(ClickType.class);
    }

}
