package com.r.chat.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 继承自 hutool 的BeanUtil，增加了bean转换时自定义转换器的功能
 */
public class CopyUtils extends BeanUtil {

    /**
     * 将原对象转换成目标对象，对于字段不匹配的字段可以使用转换器处理
     *
     * @param source  源对象
     * @param clazz   目标对象的class
     * @param convert 转换器
     * @param <R>     原对象类型
     * @param <T>     目标对象类型
     * @return 目标对象
     */
    public static <R, T> T copyBean(R source, Class<T> clazz, Convert<R, T> convert) {
        T target = copyBean(source, clazz);
        if (convert != null) {
            convert.convert(source, target);
        }
        return target;
    }

    /**
     * 将原对象转换成目标对象，对于字段不匹配的字段可以使用转换器处理
     *
     * @param source 源对象
     * @param clazz  目标对象的class
     * @param <R>    原对象类型
     * @param <T>    目标对象类型
     * @return 目标对象
     */
    public static <R, T> T copyBean(R source, Class<T> clazz) {
        if (source == null) {
            return null;
        }
        return toBean(source, clazz);
    }

    /**
     * 将源对象中的属性拷贝到目标对象中，对于源对象中为null的属性，保留目标对象中的值
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyPropertiesIgnoreNull(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanUtil.copyProperties(source, target, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
    }

    public static <R, T> List<T> copyList(List<R> list, Class<T> clazz) {
        if (list == null || list.isEmpty()) {
            return CollUtils.emptyList();
        }
        return copyToList(list, clazz);
    }

    public static <R, T> List<T> copyList(List<R> list, Class<T> clazz, Convert<R, T> convert) {
        if (list == null || list.isEmpty()) {
            return CollUtils.emptyList();
        }
        return list.stream().map(r -> copyBean(r, clazz, convert)).collect(Collectors.toList());
    }
}