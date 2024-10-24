package com.r.chat.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.r.chat.entity.constants.Constants;
import com.r.chat.exception.JsonException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class JsonUtils {
    public static SerializerFeature[] FEATURES = new SerializerFeature[]{SerializerFeature.WriteMapNullValue};

    /**
     * 任意类序列化为Json字符串
     */
    public static String obj2Json(Object obj) {
        return JSON.toJSONString(obj, FEATURES);
    }

    /**
     * Json字符串反序列化为指定的类
     */
    public static <T> T json2Obj(String json, Class<T> clazz) {
        try {
            return JSON.parseObject(json, clazz);
        } catch (Exception e) {
            log.error("jsonObject解析异常 json: {}, clazz: {}, error: {}", json, clazz, e.getCause().getMessage());
            throw new JsonException(Constants.MESSAGE_JSON_PARSE_ERROR);
        }
    }

    /**
     * Json字符串反序列化为指定类的列表
     */
    public static <T> List<T> json2List(String json, Class<T> clazz) {
        try {
            return JSONArray.parseArray(json, clazz);
        } catch (Exception e) {
            log.error("jsonArray解析异常 json: {}, clazz: {}, error: {}", json, clazz, e.getCause().getMessage());
            throw new JsonException(Constants.MESSAGE_JSON_PARSE_ERROR);
        }
    }
}
