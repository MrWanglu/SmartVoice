package cn.fintecher.pangolin.file.web.rest;


import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.file.repository.UploadFileRepository;
import cn.fintecher.pangolin.file.service.UploadFileService;
import com.google.common.collect.Lists;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by ChenChang on 2017/3/10.
 */
@RestController
@RequestMapping("/api/uploadFile")
@Api(value = "", description = "上传文件信息")
public class UploadFileResource {
    private final Logger log = LoggerFactory.getLogger(UploadFileResource.class);
    @Autowired
    private UploadFileService uploadFileService;
    @Autowired
    private UploadFileRepository uploadFileRepository;

    @GetMapping("/{id}")
    @ApiOperation(value = "获取文件信息", notes = "获取文件信息")
    public ResponseEntity<UploadFile> getUploadFile(@PathVariable @ApiParam(value = "文件ID") String id) {
        log.debug("REST request to get UploadFile : {}", id);
        UploadFile UploadFile = uploadFileRepository.findOne(id);
        return Optional.ofNullable(UploadFile)
                .map(result -> new ResponseEntity<>(
                        result,
                        HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    @ApiOperation(value = "查询文件信息", notes = "查询文件信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<UploadFile>> getAllUploadFile(Pageable pageable)
            throws URISyntaxException {
        log.debug("REST request to get all of UploadFile");
        Page<UploadFile> allList = uploadFileRepository.findAll(pageable);
        return new ResponseEntity<>(allList, HttpStatus.OK);
    }

    @GetMapping("/getAllUploadFileByIds")
    @ApiOperation(value = "查询文件信息", notes = "查询文件信息")
    public ResponseEntity<List<UploadFile>> getAllUploadFileByIds(String ids)
            throws URISyntaxException {
        log.debug("REST request to get all of UploadFile");
        String[] fileids = ids.split(",");
        List<UploadFile> allList = Lists.newArrayList(uploadFileRepository.findAll(Arrays.asList(fileids)));
        return new ResponseEntity<>(allList, HttpStatus.OK);
    }

    @PostMapping("/addUploadFileUrl")
    @ApiOperation(value = "上传文件返回文件地址", notes = "上传文件返回文件地址")
    public ResponseEntity<String> addUploadFileUrl(@RequestParam("file") MultipartFile file) throws IOException {
        String url = uploadFileService.uploadFileUrl(file);
        return new ResponseEntity<>(url, HttpStatus.OK);
    }
}
