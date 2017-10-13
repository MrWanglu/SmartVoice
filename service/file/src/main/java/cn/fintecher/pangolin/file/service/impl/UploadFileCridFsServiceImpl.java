package cn.fintecher.pangolin.file.service.impl;

import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.file.repository.UploadFileRepository;
import cn.fintecher.pangolin.file.service.UploadFileCridFsService;
import cn.fintecher.pangolin.util.ZWDateUtil;
import com.mongodb.gridfs.GridFSDBFile;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @Author: PeiShouWen
 * @Description: GridFS文件服务器
 * @Date 15:45 2017/9/29
 */
@Service("uploadFileCridFsServiceImpl")
public class UploadFileCridFsServiceImpl implements UploadFileCridFsService {
    private final Logger logger = LoggerFactory.getLogger(UploadFileCridFsServiceImpl.class);

    @Value("${gridfs.path}")
    private String path;
    @Value("${gridfs.localPath}")
    private String localPath;

    @Autowired
    UploadFileRepository uploadFileRepository;


    @Autowired
    GridFsTemplate gridFsTemplate;

    /**
     *
     *
     *
     * 获取外网地址
     */
    private String getResAccessUrl(String fid) {
        String fileUrl = "http://".concat(path).concat("/api/fileUploadController/view/").concat(fid);
        return fileUrl;
    }

    /**
     * 获取内网地址
     */
    private String getLocalResAccessUrl(String fid) {
        String fileUrl = "http://".concat(localPath).concat("/api/fileUploadController/view/").concat(fid);
        return fileUrl;
    }

    @Override
    public UploadFile uploadFile(MultipartFile file) throws Exception {
        String realName =file.getOriginalFilename();
        if (realName.endsWith(".do")) {
            realName = realName.replaceAll(".do", "");
        }
        String originalFileName = file.getOriginalFilename();
        if (originalFileName.endsWith(".do")) {
            originalFileName = originalFileName.replaceAll(".do", "");
        }
        UploadFile uploadFile = uploadFile(file.getInputStream(), file.getSize(), realName, FilenameUtils.getExtension(originalFileName));
        gridFsTemplate.store(new ByteArrayInputStream(file.getBytes()), uploadFile.getId(), uploadFile.getType());
        return uploadFile;
    }

    private UploadFile uploadFile(InputStream inputStream, long fileSize, String fileName, String fileExtName) throws Exception {
        UploadFile uploadFile = new UploadFile();
        uploadFile.setCreateTime(ZWDateUtil.getNowDateTime());
        uploadFile.setRealName(fileName);
        uploadFile.setName(fileName);
        uploadFile.setType(fileExtName);
        //uploadFile.setCreator(creator);
        uploadFile.setSize(fileSize);
        uploadFile = uploadFileRepository.save(uploadFile);
        uploadFile.setLocalUrl(getLocalResAccessUrl(uploadFile.getId()));
        uploadFile.setUrl(getResAccessUrl(uploadFile.getId()));
        return uploadFileRepository.save(uploadFile);
    }


    @Override
    public void removeFile(String id) {
        uploadFileRepository.delete(id);
        Query query = Query.query(GridFsCriteria.whereFilename().is(id));
        gridFsTemplate.delete(query);
    }

    @Override
    public UploadFile getFileById(String id) {
        return uploadFileRepository.findOne(id);
    }

    @Override
    public GridFSDBFile getFileContent(String id) {
        Query query = Query.query(GridFsCriteria.whereFilename().is(id));
        return gridFsTemplate.findOne(query);
    }

    @Override
    public void uploadCaseFileReduce(InputStream inputStream, String userId, String userName, String batchNum) {

    }
}
