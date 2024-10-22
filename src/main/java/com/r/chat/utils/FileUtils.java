package com.r.chat.utils;

import com.r.chat.entity.constants.Constants;
import com.r.chat.exception.FileNotExistException;
import com.r.chat.exception.FileSaveFailedException;
import com.r.chat.properties.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Slf4j
public class FileUtils {
    /**
     * 保存图片到文件夹中
     */
    public static void saveAvatarFile(AvatarOwner avatarOwner) {
        if (avatarOwner.getAvatarFile() == null || avatarOwner.getAvatarCover() == null) {
            log.warn("尝试保存为null的文件 {}", avatarOwner);
            return;
        }
        saveFile(
                AppProperties.projectFolder,
                Constants.FILE_FOLDER_AVATAR,
                avatarOwner.getIdentityName(),
                Constants.FILE_SUFFIX_AVATAR,  // .png
                avatarOwner.getAvatarFile()
        );
        saveFile(
                AppProperties.projectFolder,
                Constants.FILE_FOLDER_AVATAR,
                avatarOwner.getIdentityName(),
                Constants.FILE_SUFFIX_COVER,  // _cover.png
                avatarOwner.getAvatarCover()
        );
    }

    /**
     * 保存app的exe文件
     */
    public static void saveExeFile(MultipartFile file, String version) {
        if (file == null || version == null) {
            log.warn("尝试保存为null的文件/文件名可能为null file: {}, version: {}", file, version);
            return;
        }
        saveFile(
                AppProperties.projectFolder,
                Constants.FILE_FOLDER_EXE,
                version.replace(".", "_"),
                Constants.FILE_SUFFIX_EXE,
                file
        );
    }

    /**
     * 根据传入的app版本获取对应的exe文件
     */
    public static File getExeFile(String version) {
        if (version == null) {
            log.warn("尝试获取文件, 但提供的文件夹为null");
            return null;
        }
        return getFile(
                AppProperties.projectFolder,
                Constants.FILE_FOLDER_EXE,
                version.replace(".", "_"),
                Constants.FILE_SUFFIX_EXE
        );
    }

    /**
     * 保存文件
     * @param rootPath 根目录
     * @param folder 文件夹，不存在的话会创建
     * @param fileName 文件名
     * @param suffix 文件后缀
     * @param cacheFile 文件
     */
    public static void saveFile(String rootPath, String folder, String fileName, String suffix, MultipartFile cacheFile) {
        log.info("保存文件 rootPath: {}, folder: {}, fileName: {}, suffix: {}", rootPath, folder, fileName, suffix);
        File targetFolder = new File(rootPath, folder);
        if (!targetFolder.exists()) {
            if (targetFolder.mkdirs()) {
                log.info("创建目录成功: {}", targetFolder.getAbsolutePath());
            } else {
                log.warn("创建目录失败: {}", targetFolder.getAbsolutePath());
                throw new FileSaveFailedException(Constants.MESSAGE_FAILED_TO_CREATE_FOLDER);
            }
        }
        try {
            File localFile = new File(targetFolder, fileName + suffix);
            cacheFile.transferTo(localFile);
            log.info("保存文件成功: {}", localFile.getAbsolutePath());
        } catch (Exception e) {
            // 保存文件失败
            log.error("保存文件失败: {}", e.getMessage());
            throw new FileSaveFailedException(Constants.MESSAGE_FAILED_TO_SAVE_FILE);
        }
    }

    /**
     * 获取文件
     */
    public static File getFile(String rootPath, String folder, String fileName, String suffix) {
        File targetFolder = new File(rootPath, folder);
        File localFile = new File(targetFolder, fileName + suffix);
        if (!localFile.exists()) {
            log.error("打开文件失败: 文件不存在 {}", localFile.getAbsolutePath());
            throw new FileNotExistException(Constants.MESSAGE_FILE_NOT_EXIST);
        }
        log.info("获取到文件 {}", localFile);
        return localFile;
    }
}
