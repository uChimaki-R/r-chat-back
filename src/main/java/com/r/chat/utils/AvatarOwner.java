package com.r.chat.utils;

import org.springframework.web.multipart.MultipartFile;

public interface AvatarOwner {
    /**
     * 获取标识图片文件的名称
     */
    String getIdentityName();

    // 下面的两个方法正好对应有avatarFile和coverFile属性的类的get方法，实现类中加了lombok注解就不需要手动实现

    /**
     * 获取图片文件
     */
    MultipartFile getAvatarFile();

    /**
     * 获取压缩过的图片文件
     */
    MultipartFile getCoverFile();
}
