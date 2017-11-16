package cn.fintecher.pangolin.file.web;

import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.entity.message.ImportFileUploadSuccessMessage;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.file.model.UnZipCaseFileRequest;
import cn.fintecher.pangolin.file.repository.UploadFileRepository;
import cn.fintecher.pangolin.file.service.UploadFileCridFsService;
import cn.fintecher.pangolin.file.service.UploadFileService;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.google.common.collect.Lists;
import com.mongodb.gridfs.GridFSDBFile;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Created by ChenChang on 2017/3/10.
 */
@RestController
@RequestMapping("/api/fileUploadController")
@Api(value = "", description = "文件上传")
public class FileUploadController {
    @Autowired
    private UploadFileService uploadFileService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private UploadFileRepository uploadFileRepository;
    @Autowired
    UploadFileCridFsService uploadFileCridFsService;

    private org.slf4j.Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @CrossOrigin(origins = "*", maxAge = 3600)
    @RequestMapping(value = "/upload", method = RequestMethod.POST, headers = {"content-type=multipart/mixed", "content-type=multipart/form-data"}, consumes = {"multipart/form-data"})
    @ResponseBody
    @ApiOperation(value = "上传文件", notes = "返回JSON data 为UploadFile 对象")
    ResponseEntity<UploadFile> uploadFile(@RequestParam("file") MultipartFile file, @RequestHeader(value = "X-UserToken") String token) throws Exception {
        if (file.isEmpty()) {
            throw new RuntimeException("MultipartFile是空的");
        }
        ResponseEntity<User> entity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        if (Objects.isNull(entity)) {
            throw new RuntimeException("请先登录");
        }
        UploadFile uploadFile = uploadFileService.uploadFile(file, entity.getBody().getUserName());
        return new ResponseEntity<>(uploadFile, HttpStatus.OK);
    }

    @CrossOrigin(origins = "*", maxAge = 3600)
    @RequestMapping(value = "/uploadFileGrid", method = RequestMethod.POST, headers = {"content-type=multipart/mixed", "content-type=multipart/form-data"}, consumes = {"multipart/form-data"})
    @ResponseBody
    @ApiOperation(value = "Grid方式上传文件", notes = "返回JSON data 为UploadFile 对象")
    ResponseEntity<UploadFile> uploadFileGrid(@RequestParam("file") MultipartFile file, @RequestHeader(value = "X-UserToken") String token) throws Exception {
        try {
            if (Objects.isNull(file)) {
                throw new RuntimeException("MultipartFile是空的");
            }
            ResponseEntity<User> entity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
            if (Objects.isNull(entity)) {
                throw new RuntimeException("请先登录");
            }
            UploadFile uploadFile = uploadFileCridFsService.uploadFile(file);
            return new ResponseEntity<>(uploadFile, HttpStatus.OK);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "上传失败")).body(null);
        }
    }


    @PostMapping("/unZipCaseFile")
    @ResponseBody
    @ApiOperation(value = "上传压缩文件，后台进行解压缩", notes = "返回的为文件记录对象")
    public ResponseEntity<UploadFile> unZipCaseFile(@RequestBody UnZipCaseFileRequest request,
                                                    @RequestHeader(value = "X-UserToken") String token) throws Exception {
        if (StringUtils.isBlank(request.getUploadFile())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createAlert("文件是空的", "")).body(null);
        }
        if (StringUtils.isBlank(request.getBatchNum())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createAlert("批次号是空的", "")).body(null);
        }
        ResponseEntity<User> userResponseEntity = null;
        try {
            userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user", "")).body(null);
        }
        User user = userResponseEntity.getBody();
        if (Objects.isNull(user.getCompanyCode())) {
            if (StringUtils.isNotBlank(request.getCompanyCode())) {
                user.setCompanyCode(request.getCompanyCode());
            } else {
                return ResponseEntity.badRequest().headers(HeaderUtil.createAlert("请选择公司", "")).body(null);
            }
        }
        UploadFile uploadFile = uploadFileRepository.findOne(request.getUploadFile());
        ImportFileUploadSuccessMessage message = new ImportFileUploadSuccessMessage();
        message.setBatchNum(request.getBatchNum());
        message.setUploadFile(uploadFile);
        message.setUserName(user.getUserName());
        message.setUserId(user.getId());
        message.setCompanyCode(user.getCompanyCode());
        rabbitTemplate.convertAndSend("mr.cui.file.import.upload.success", message);
        return ResponseEntity.ok(uploadFile);
    }

    @GetMapping("/getAllUploadFileByIdList")
    @ResponseBody
    @ApiOperation(value = "查询文件信息", notes = "查询文件信息")
    public ResponseEntity<List<UploadFile>> getAllUploadFileByIds(@RequestParam(required = false) @ApiParam(value = "文件id集合") List<String> fileIds)
            throws Exception {
        List<UploadFile> uploadFiles = Lists.newArrayList(uploadFileRepository.findAll(fileIds));
        return ResponseEntity.ok(uploadFiles);
    }

    /**
     * 在线显示文件
     *
     * @param id
     * @return
     */
    @GetMapping("/view/{id}")
    @ResponseBody
    @ApiOperation(value = "在线显示文件", notes = "在线显示文件")
    public ResponseEntity<Object> serveFileOnline(@PathVariable String id) throws IOException {
        UploadFile file = uploadFileCridFsService.getFileById(id);
        if (file != null) {
            GridFSDBFile gridFSDBFile = uploadFileCridFsService.getFileContent(id);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            gridFSDBFile.writeTo(os);
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "fileName=\"" + new String(file.getRealName().getBytes("UTF-8"), "ISO-8859-1") + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/".concat(file.getType()))
                    .header(HttpHeaders.CONTENT_LENGTH, file.getSize() + "")
                    .header("Connection", "close")
                    .body(os.toByteArray());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File was not fount");
        }
    }

    /**
     * 下载文件
     *
     * @param id
     * @return
     */
    @GetMapping("/file/{id}")
    @ResponseBody
    @ApiOperation(value = "下载文件", notes = "下载文件")
    public ResponseEntity<Object> downFile(@PathVariable String id) throws IOException {
        UploadFile file = uploadFileCridFsService.getFileById(id);
        if (file != null) {
            GridFSDBFile gridFSDBFile = uploadFileCridFsService.getFileContent(id);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            gridFSDBFile.writeTo(os);
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=\"" + file.getRealName() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                    .header(HttpHeaders.CONTENT_LENGTH, file.getSize() + "")
                    .header("Connection", "close")
                    .body(os.toByteArray());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File was not fount");
        }
    }

    /**
     * 删除文件
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "删除文件", notes = "删除文件")
    public ResponseEntity<String> deleteFile(@PathVariable String id) {
        try {
            uploadFileCridFsService.removeFile(id);
            return ResponseEntity.status(HttpStatus.OK).body("DELETE Success!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
