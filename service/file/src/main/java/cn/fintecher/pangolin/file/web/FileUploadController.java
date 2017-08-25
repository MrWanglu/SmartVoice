package cn.fintecher.pangolin.file.web;

import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.entity.message.ImportFileUploadSuccessMessage;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.file.model.UnZipCaseFileRequest;
import cn.fintecher.pangolin.file.repository.UploadFileRepository;
import cn.fintecher.pangolin.file.service.UploadFileService;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

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

    @CrossOrigin(origins = "*", maxAge = 3600)
    @RequestMapping(value = "/upload", method = RequestMethod.POST, headers = {"content-type=multipart/mixed", "content-type=multipart/form-data"}, consumes = {"multipart/form-data"})
    @ResponseBody
    @ApiOperation(value = "上传文件", notes = "返回JSON data 为UploadFile 对象")
    ResponseEntity<UploadFile> uploadFile(@RequestParam("file") MultipartFile file, @RequestHeader(value = "X-UserToken") String token) throws Exception {
        if (file.isEmpty()) {
            throw new RuntimeException("MultipartFile是空的");
        }
        ResponseEntity<User> entity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        if (file.isEmpty()) {
            throw new RuntimeException("请先登录");
        }
        UploadFile uploadFile = uploadFileService.uploadFile(file, entity.getBody().getUserName());
        return new ResponseEntity<>(uploadFile, HttpStatus.OK);
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
        ResponseEntity<User> userResponseEntity=null;
        try {
            userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        }catch (Exception e){
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
        message.setCaseNumber(request.getCaseNumber());
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
}
