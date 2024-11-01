package com.r.chat.utils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CastUtils {
    public static <T> List<T> castList(Object obj, Class<T> clazz) {
        if (obj instanceof Collection<?>) {
            Collection<?> list = (Collection<?>) obj;
            return list.stream().map(clazz::cast).collect(Collectors.toList());
        }
        return null;
    }
}
