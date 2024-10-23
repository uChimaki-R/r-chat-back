package com.r.chat.utils;

import java.util.List;
import java.util.stream.Collectors;

public class CastUtils {
    public static <T> List<T> castList(Object obj, Class<T> clazz) {
        if (obj instanceof List<?>) {
            List<?> list = (List<?>) obj;
            return list.stream().map(clazz::cast).collect(Collectors.toList());
        }
        return null;
    }
}
