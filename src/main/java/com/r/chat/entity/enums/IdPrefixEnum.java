package com.r.chat.entity.enums;

import com.r.chat.utils.StringUtils;
import lombok.Getter;

@Getter
public enum IdPrefixEnum {
    USER(0, "U", "用户"),
    GROUP(1, "G", "群聊");
    private final int id;
    private final String prefix;
    private final String desc;

    IdPrefixEnum(int id, String prefix, String desc) {
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

    public static IdPrefixEnum getPrefix(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        for (IdPrefixEnum e : IdPrefixEnum.values()) {
            int length = e.getPrefix().length();
            if (id.length() < length) continue;
            if (e.prefix.equals(id.substring(0, length))) {
                return e;
            }
        }
        return null;
    }

    public UserContactTypeEnum getUserContactTypeEnum() {
        return this == USER ? UserContactTypeEnum.USER : UserContactTypeEnum.GROUP;
    }
}
