package com.r.chat.utils;

import com.r.chat.entity.constants.Constants;
import com.r.chat.exception.FileSaveFailedException;
import com.r.chat.properties.AppProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class FileUtils {
    /**
     * 保存图片到文件夹中
     */
    public static void saveAvatarFile(AvatarOwner avatarOwner) {
        if (avatarOwner.getAvatarFile() == null || avatarOwner.getAvatarCover() == null) {
            return;
        }
        File targetFolder = new File(AppProperties.projectFolder, Constants.FILE_FOLDER_AVATAR);
        if (!targetFolder.exists()) {
            if (targetFolder.mkdirs()) {
                log.info("创建目录: {}", targetFolder.getAbsolutePath());
            } else {
                log.warn("创建目录失败: {}", targetFolder.getAbsolutePath());
            }
        }
        try {
            File avatarFile = new File(targetFolder, avatarOwner.getIdentityName() + Constants.FILE_SUFFIX_AVATAR);
            File coverFile = new File(targetFolder, avatarOwner.getIdentityName() + Constants.FILE_SUFFIX_COVER);
            avatarOwner.getAvatarFile().transferTo(avatarFile);
            log.info("保存图片文件: {}", avatarFile.getAbsolutePath());
            avatarOwner.getAvatarCover().transferTo(coverFile);
            log.info("保存图片文件: {}", coverFile.getAbsolutePath());
        } catch (Exception e) {
            // 保存文件失败
            log.error("头像文件保存失败: {}", e.getMessage());
            throw new FileSaveFailedException(Constants.MESSAGE_FAILED_TO_SAVE_AVATAR_FILE);
        }
    }
}
