package com.r.chat.utils;

import java.util.HashMap;
import java.util.Map;

public class URLUtils {
    public static class URLEntity {
        public String baseUrl;
        public Map<String, String> params;
    }

    /**
     * 解析url，转换为URLEntity对象
     */
    public static URLEntity parse(String url) {
        URLEntity entity = new URLEntity();
        if (url == null) {
            return entity;
        }
        url = url.trim();
        if (url.isEmpty()) {
            return entity;
        }
        String[] urlParts = url.split("\\?");
        entity.baseUrl = urlParts[0];
        // 没有参数
        if (urlParts.length == 1) {
            return entity;
        }
        // 有参数
        String[] params = urlParts[1].split("&");
        entity.params = new HashMap<>();
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length > 1) {
                entity.params.put(keyValue[0], keyValue[1]);
            }
        }
        return entity;
    }

    /**
     * 获取url指定参数值
     */
    public static String getParamsByKey(String url, String key) {
        URLEntity entity = parse(url);
        return entity.params.get(key);
    }
}
