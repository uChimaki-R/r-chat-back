package com.r.chat.entity.enums;

import lombok.Getter;

@Getter
public enum IdPrefixEnum {
    USER(0, 'U', "用户"),
    GROUP(1, 'G', "群聊");
    private final int id;
    private final Character prefix;
    private final String desc;

    IdPrefixEnum(int id, Character prefix, String desc) {
        this.id = id;
        this.prefix = prefix;
        this.desc = desc;
    }

    public static IdPrefixEnum getByName(String name) {
        for (IdPrefixEnum e : IdPrefixEnum.values()) {
            if (e.name().equals(name)) {
                return e;
            }
        }
        return null;
    }

    public static IdPrefixEnum getByPrefix(Character prefix) {
        for (IdPrefixEnum e : IdPrefixEnum.values()) {
            if (e.prefix.equals(prefix)) {
                return e;
            }
        }
        return null;
    }

    public UserContactTypeEnum getUserContactTypeEnum() {
        return this == USER ? UserContactTypeEnum.USER : UserContactTypeEnum.GROUP;
    }
}
