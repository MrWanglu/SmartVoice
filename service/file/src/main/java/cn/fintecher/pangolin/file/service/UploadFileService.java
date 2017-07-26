package cn.fintecher.pangolin.file.service;

import cn.fintecher.pangolin.entity.file.UploadFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ChenChang on 2017/3/31.
 */
public interface UploadFileService {

    UploadFile uploadFile(MultipartFile file, String creator) throws IOException;

    UploadFile uploadFile(InputStream inputStream, long fileSize, String fileName, String fileExtName, String creator);

    void uploadCaseFileReduce(InputStream inputStream, String userId, String userName, String batchNum);

    String uploadFileUrl(MultipartFile file) throws IOException;
}
