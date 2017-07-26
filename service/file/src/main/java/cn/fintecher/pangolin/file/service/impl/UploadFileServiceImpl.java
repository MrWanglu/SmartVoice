package cn.fintecher.pangolin.file.service.impl;

import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.entity.message.ProgressMessage;
import cn.fintecher.pangolin.entity.message.UnReduceFileMessage;
import cn.fintecher.pangolin.entity.util.ShortUUID;
import cn.fintecher.pangolin.file.repository.UploadFileRepository;
import cn.fintecher.pangolin.file.service.UploadFileService;
import cn.fintecher.pangolin.util.UnReduceFile;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.exception.FdfsUnsupportStorePathException;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * Created by ChenChang on 2017/3/10.
 */
@Service("uploadFileService")
public class UploadFileServiceImpl implements UploadFileService {
    private final Logger logger = LoggerFactory.getLogger(UploadFileServiceImpl.class);
    private final UploadFileRepository uploadFileRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    private FastFileStorageClient storageClient;

    @Value("${fdfs.path}")
    private String path;
    @Value("${fdfs.localPath}")
    private String localPath;

    @Autowired
    public UploadFileServiceImpl(UploadFileRepository uploadFileRepository) {
        this.uploadFileRepository = uploadFileRepository;
    }

    @Override
    public UploadFile uploadFile(MultipartFile file, String creator) throws IOException {
        String realName = new String(file.getOriginalFilename().getBytes("iso-8859-1"), "UTF-8");
        if (realName.endsWith(".do")) {
            realName = realName.replaceAll(".do", "");
        }
        String originalFileName = file.getOriginalFilename();
        if (originalFileName.endsWith(".do")) {
            originalFileName = originalFileName.replaceAll(".do", "");
        }
        UploadFile uploadFile = uploadFile(file.getInputStream(), file.getSize(), realName, FilenameUtils.getExtension(originalFileName), creator);
        return uploadFile;
    }

    @Override
    public UploadFile uploadFile(InputStream inputStream, long fileSize, String fileName, String fileExtName, String creator) {
        StorePath storePath = storageClient.uploadFile(inputStream, fileSize, fileExtName, null);
        String url = getResAccessUrl(storePath);
        String localUrl = getLocalResAccessUrl(storePath);
        UploadFile uploadFile = new UploadFile();
        uploadFile.setCreateTime(new Date());
        uploadFile.setRealName(fileName);
        uploadFile.setUrl(url);
        uploadFile.setLocalUrl(localUrl);
        uploadFile.setType(fileExtName);
        uploadFile.setCreator(creator);
        uploadFile.setSize(fileSize);
        uploadFile.setName(FilenameUtils.getName(StorePath.praseFromUrl(url).getPath()));
        uploadFile = uploadFileRepository.save(uploadFile);
        return uploadFile;
    }

    @Override
    public void uploadCaseFileReduce(InputStream inputStream, String userId, String userName, String batchNum) {
        String targetTempFilePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(userName).
                concat(File.separator).concat(ShortUUID.generateShortUuid()).concat(File.separator);

        List<String> directoryList;
        try {
            directoryList = UnReduceFile.unZip(inputStream, targetTempFilePath, "GBK");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return;
        }
        if (Objects.nonNull(directoryList)) {
            int current = 0;
            int total = 0;
            for (String directoryName : directoryList) {
                File file = FileUtils.getFile(targetTempFilePath, directoryName);
                File[] array = file.listFiles();
                total = total + array.length;
            }
            logger.info("文件总数:{}", total);
            ProgressMessage progressMessage = new ProgressMessage();
            progressMessage.setUserId(userId);
            progressMessage.setCurrent(0);
            progressMessage.setTotal(total);
            progressMessage.setText("开始解压缩文件");
            rabbitTemplate.convertAndSend("mr.cui.file.unReduce.progress", progressMessage);
            for (String directoryName : directoryList) {
                File file = FileUtils.getFile(targetTempFilePath, directoryName);
                File[] array = file.listFiles();
                for (File f : array) {
                    try {
                        if (f.isDirectory()) {
                            continue;
                        }
                        InputStream in = new FileInputStream(f);
                        UploadFile uploadFile = uploadFile(in, f.length(), FilenameUtils.getBaseName(f.getName()),
                                FilenameUtils.getExtension(f.getName()), userName);
                        UnReduceFileMessage message = new UnReduceFileMessage();
                        message.setUserId(userId);
                        message.setUploadFile(uploadFile);
                        message.setBatchNum(batchNum);
                        message.setPath(directoryName);
                        current = current + 1;
                        message.setCurrent(current);
                        message.setTotal(total);
                        logger.info("发送第 {} 个文件", current);
                        rabbitTemplate.convertAndSend("mr.cui.file.unReduce.success", message);
                        in.close();
                    } catch (FileNotFoundException e) {
                        logger.error(e.getMessage(), e);
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
            try {
                FileUtils.deleteDirectory(FileUtils.getFile(targetTempFilePath));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }


    /**
     * 获取外网地址
     */
    private String getResAccessUrl(StorePath storePath) {
        String fileUrl = "http://" + path + "/" + storePath.getFullPath();
        return fileUrl;
    }

    /**
     * 获取内网地址
     */
    private String getLocalResAccessUrl(StorePath storePath) {
        String fileUrl = "http://" + localPath + "/" + storePath.getFullPath();
        return fileUrl;
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件访问地址
     */
    private void deleteFile(String fileUrl) {
        if (StringUtils.isEmpty(fileUrl)) {
            return;
        }
        try {
            StorePath storePath = StorePath.praseFromUrl(fileUrl);
            storageClient.deleteFile(storePath.getGroup(), storePath.getPath());
        } catch (FdfsUnsupportStorePathException e) {
            logger.warn(e.getMessage());
        }
    }

    @Override
    public String uploadFileUrl(MultipartFile file) throws IOException {
        StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()), null);
        return getResAccessUrl(storePath);
    }
}
