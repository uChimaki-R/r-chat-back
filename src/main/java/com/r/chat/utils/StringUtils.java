package com.r.chat.utils;

import com.r.chat.entity.enums.IdPrefixEnum;
import com.r.chat.properties.AppProperties;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Arrays;

public class StringUtils {
    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 随机获取一个id
     */
    public static String getRandomId() {
        return RandomStringUtils.random(AppProperties.idLength, false, true);
    }

    /**
     * 随机获取一个用户id
     */
    public static String getRandomUserId() {
        return IdPrefixEnum.USER.getPrefix() + getRandomId();
    }

    /**
     * 随机获取一个群聊id
     */
    public static String getRandomGroupId() {
        return IdPrefixEnum.GROUP.getPrefix() + getRandomId();
    }

    /**
     * 获取会话id
     * 使用两个用户的id排序拼接并md5后的结果，保证两个用户之间的会话id唯一且不变
     * 如果是群聊的话就传一个群聊的id即可
     */
    public static String getSessionId(String[] ids) {
        Arrays.sort(ids);
        return encodeMd5(String.join("", ids));
    }

    /**
     * 对字符串进行md5加密
     */
    public static String encodeMd5(String str) {
        return DigestUtils.md5Hex(str);
    }

    /**
     * 获取token
     */
    public static String generateToken(String userId) {
        return encodeMd5(userId + getRandomChars(AppProperties.tokenRandomCharsLength));
    }

    /**
     * 获取指定长度的随机字符序列
     */
    public static String getRandomChars(Integer length) {
        return RandomStringUtils.random(length, true, false);
    }

    /**
     * 判断v1版本号是否大于等于v2版本号
     */
    public static Boolean versionGTE(String v1, String v2) {
        // 一者为空另一者大
        if (isEmpty(v2)) return true;
        if (isEmpty(v1)) return false;
        // 根据.划分成数组，然后逐个比较
        String[] v1s = v1.split("\\.");
        String[] v2s = v2.split("\\.");
        int len = Math.min(v1s.length, v2s.length);
        for (int i = 0; i < len; i++) {
            // 一样就继续比
            if (v1s[i].equals(v2s[i])) continue;
            // 不一样就可以得出结果
            return Integer.parseInt(v1s[i]) >= Integer.parseInt(v2s[i]);
        }
        // 前缀相同看长度
        return v1s.length >= v2s.length;
    }

    /**
     * 将字符串中的html字符转义，防止注入，更换换行符（前端使用br标签显示换行效果）
     */
    public static String transStrForFront(String origin) {
        if (isEmpty(origin)) return origin;
        return origin
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\r\n", "<br>")
                .replace("\n", "<br>");
    }

    /**
     * 获取文件名的后缀
     */
    public static String getFileSuffix(String fileName) {
        if (isEmpty(fileName)) return null;
        return fileName.substring(fileName.lastIndexOf("."));  // 包括.
    }
}
