package com.atl.vitalstats.enums;

import net.minecraft.util.Identifier;

public enum TotemTypeEnum {
    TOTEM("totem_of_undying");

    public final Identifier icon;
    public final Identifier vanillaIcon;

    TotemTypeEnum(String totemIcon) {
        icon = Identifier.of("minecraft", "textures/item/" + totemIcon + ".png");
        vanillaIcon = Identifier.of("vitalstats", "textures/gui/totem/" + totemIcon + ".png");
    }
}
