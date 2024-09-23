package com.r.chat.utils;

import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.enums.IdPrefixEnum;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

public class MyStringUtils {
    /**
     * 随机获取一个id
     */
    private static String getRandomId(){
        return RandomStringUtils.random(Constants.ID_LENGTH, false, true);
    }

    /**
     * 随机获取一个用户id
     */
    public static String getRandomUserId(){
        return IdPrefixEnum.USER.getPrefix() + getRandomId();
    }

    /**
     * 随机获取一个群组id
     */
    public static String getRandomGroupId(){
        return IdPrefixEnum.GROUP.getPrefix() + getRandomId();
    }
    /**
     * 对字符串进行md5加密
     */
    public static String encodeMd5(String str){
        return DigestUtils.md5Hex(str);
    }
}
