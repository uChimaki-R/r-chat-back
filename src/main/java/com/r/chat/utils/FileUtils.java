package com.r.chat.utils;

import cn.hutool.core.date.DateUtil;
import com.r.chat.context.UserTokenInfoContext;
import com.r.chat.entity.constants.Constants;
import com.r.chat.entity.enums.FileTypeEnum;
import com.r.chat.exception.FileNotExistException;
import com.r.chat.exception.FileSaveFailedException;
import com.r.chat.properties.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;

@Slf4j
public class FileUtils {
    /**
     * 保存图片到文件夹中
     */
    public static void saveAvatarFile(AvatarOwner avatarOwner) {
        if (avatarOwner.getAvatarFile() == null || avatarOwner.getCoverFile() == null || StringUtils.isEmpty(avatarOwner.getIdentityName())) {
            log.warn("保存头像失败: 文件信息不完整 {}", avatarOwner);
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
                Constants.FILE_SUFFIX_COVER + Constants.FILE_SUFFIX_AVATAR,  // _cover.png
                avatarOwner.getCoverFile()
        );
    }

    /**
     * 保存app的exe文件
     */
    public static void saveExeFile(MultipartFile file, String version) {
        if (file == null || StringUtils.isEmpty(version)) {
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
     * 保存聊天中的文件
     * 最外层文件夹为 projectFolder + 用户id，里面还有两层文件夹
     * cover可以为空，除了图片和视频文件，其他文件是没有缩略图的
     *
     * @param sendTime  发送时间，用来生成第一层文件夹
     * @param fileType  文件类型，用来生成第二层文件夹
     * @param messageId 消息id，用来生成文件名
     */
    public static void saveChatFile(MultipartFile file, MultipartFile cover, Long sendTime, FileTypeEnum fileType, Long messageId) {
        if (file == null || sendTime == null || fileType == null || messageId == null) {
            log.warn("聊天文件信息不全, 无法保存文件 file: {}, sendTime: {}, fileType: {}, messageId: {}", file, sendTime, fileType, messageId);
            return;
        }
        // 按时间分目录保存
        String month = DateUtil.format(new Date(sendTime), Constants.FORMAT_DATE_yyyyMM);
        saveFile(
                AppProperties.projectFolder,
                Constants.FILE_FOLDER_CHAT + File.separator
                        + month + File.separator
                        + fileType.name().toLowerCase(),
                // 对文件进行重新命名防止用户间的文件重名，直接使用消息id命名
                String.valueOf(messageId),
                StringUtils.getFileSuffix(file.getOriginalFilename()),
                file
        );
        if (cover != null) {
            saveFile(
                    AppProperties.projectFolder,
                    Constants.FILE_FOLDER_CHAT + File.separator
                            + month + File.separator + fileType.name().toLowerCase(),
                    // 对文件进行重新命名防止用户间的文件重名，直接使用消息id命名
                    String.valueOf(messageId),
                    Constants.FILE_SUFFIX_COVER + Constants.FILE_SUFFIX_AVATAR,  // _cover.png // 所有的缩略图都用这个结尾，用源文件后缀在传视频的情况下会是_cover.mp4，就不是缩略图了
                    cover
            );
        }
    }

    /**
     * 根据传入的app版本获取对应的exe文件
     */
    public static File getExeFile(String version) {
        if (StringUtils.isEmpty(version)) {
            log.warn("尝试获取exe文件, 但提供的version为null");
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
     * 获取头像文件
     */
    public static File getAvatarFile(String identityName, Boolean isCover) {
        if (StringUtils.isEmpty(identityName) || isCover == null) {
            log.warn("尝试获取头像文件, 但提供的文件名为null或未指明是否获取缩略图 identityName: {}, isCover: {}", identityName, isCover);
            return null;
        }
        return getFile(
                AppProperties.projectFolder,
                Constants.FILE_FOLDER_AVATAR,
                identityName,
                (isCover ? Constants.FILE_SUFFIX_COVER : "") + Constants.FILE_SUFFIX_AVATAR
        );
    }

    /**
     * 获取聊天文件
     * 存放文件的目录最外层文件夹为 projectFolder + 用户id，里面还有两层文件夹
     *
     * @param sendTime  发送时间，转换为第一层文件夹
     * @param fileType  文件类型，转换为第二层文件夹
     * @param fileName  数据库里保存的原文件名全名，用于获取实际保存的文件名的后缀
     * @param messageId 消息id，实际保存的文件名
     * @param isCover   是否是缩略图，是则文件还有一个后缀
     */
    public static File getChatFile(Long sendTime, FileTypeEnum fileType, String fileName, Long messageId, Boolean isCover) {
        if (sendTime == null || fileType == null || fileName == null || isCover == null) {
            log.warn("聊天信息不全, 无法获取文件 sendTime: {}, fileType: {}, fileName: {}, isCover: {}", sendTime, fileType, fileName, isCover);
            return null;
        }
        String fileSuffix = StringUtils.getFileSuffix(fileName);
        String month = DateUtil.format(new Date(sendTime), Constants.FORMAT_DATE_yyyyMM);
        return getFile(
                AppProperties.projectFolder,
                Constants.FILE_FOLDER_CHAT + File.separator
                        + month + File.separator + fileType.name().toLowerCase(),
                String.valueOf(messageId),
                (isCover ? Constants.FILE_SUFFIX_COVER  + Constants.FILE_SUFFIX_AVATAR : fileSuffix)
        );
    }

    /**
     * 保存文件
     *
     * @param rootPath  根目录
     * @param folder    文件夹，不存在的话会创建
     * @param fileName  文件名
     * @param suffix    文件后缀
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
            log.warn("打开文件失败: 文件不存在 {}", localFile.getAbsolutePath());
            throw new FileNotExistException(Constants.MESSAGE_FILE_NOT_EXIST);
        }
        log.info("获取到文件 {}", localFile);
        return localFile;
    }
}
