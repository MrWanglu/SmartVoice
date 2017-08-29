package cn.fintecher.pangolin.dataimp.service;

import cn.fintecher.pangolin.dataimp.entity.DataInfoExcel;
import cn.fintecher.pangolin.dataimp.entity.DataInfoExcelFile;
import cn.fintecher.pangolin.dataimp.repository.DataInfoExcelFileRepository;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.entity.message.ProgressMessage;
import cn.fintecher.pangolin.entity.message.UnReduceFileMessage;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;


/**
 * 附件上传成功队列消息
 * Created by ChenChang on 2017/3/31.
 */
@Component
@RabbitListener(queues = "mr.cui.file.unReduce.success")
public class UnReduceSuccessReceiver {

    private final Logger log = LoggerFactory.getLogger(UnReduceSuccessReceiver.class);
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DataInfoExcelService dataInfoExcelService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    DataInfoExcelFileRepository dataInfoExcelFileRepository;

    @RabbitHandler
    public void receive(UnReduceFileMessage message) {
        try {
            log.debug("收到附件上传成功消息 {}", message);
            UploadFile uploadFile = message.getUploadFile();
            String batchNum = message.getBatchNum();
            String path = message.getPath();
            String userId = message.getUserId();
            ResponseEntity<User> forEntity = null;
            try {
                forEntity = restTemplate.getForEntity(Constants.USERBYID_SERVICE_URL.concat(userId), User.class);
            } catch (RestClientException e) {
                log.error("获取用户信息失败");
            }
            if (!forEntity.hasBody()) {
                log.error("获取用户信息失败");
            } else {
                User user = forEntity.getBody();
                if (StringUtils.isBlank(user.getCompanyCode())) {
                    user.setCompanyCode(message.getCompanyCode());
                }
                //解析文件文件路径
                //通过文件名字获取案件信息(客户姓名身份证号产品名称)
                String[] fileNameArr = path.split("_");
                if (Objects.isNull(fileNameArr) || fileNameArr.length != 3) {
                    log.error("文件夹名称 {} 未按规定命名,无法与案件匹配", path);
                } else {
                    DataInfoExcel dataInfoExcel = new DataInfoExcel();
                    dataInfoExcel.setPersonalName(fileNameArr[0]);
                    dataInfoExcel.setIdCard(fileNameArr[1]);
                    if (fileNameArr[2].endsWith("/")) {
                        dataInfoExcel.setProductName(fileNameArr[2].replaceAll("/", ""));
                    } else {
                        dataInfoExcel.setProductName(fileNameArr[2]);
                    }
                    dataInfoExcel.setOperator(user.getId());
                    dataInfoExcel.setBatchNumber(batchNum);
                    dataInfoExcel.setCompanyCode(user.getCompanyCode());
                    List<DataInfoExcel> dataInfoExcelList = null;
                    try {
                        dataInfoExcelList = dataInfoExcelService.queryDataInfoExcelListNoPage(dataInfoExcel, user);
                        if (dataInfoExcelList.isEmpty()) {
                            log.warn("未匹配上与文件名称 {}相符的案件", path);
                        } else {
                            for (DataInfoExcel obj : dataInfoExcelList) {
                                //数据导入附件
                                DataInfoExcelFile dataInfoExcelFile = new DataInfoExcelFile();
                                dataInfoExcelFile.setBatchNumber(batchNum);
                                dataInfoExcelFile.setFileId(uploadFile.getId());
                                dataInfoExcelFile.setFileName(uploadFile.getRealName());
                                dataInfoExcelFile.setOperator(user.getId());
                                dataInfoExcelFile.setOperatorTime(ZWDateUtil.getNowDateTime());
                                dataInfoExcelFile.setFileUrl(uploadFile.getUrl());
                                dataInfoExcelFile.setOperatorName(user.getRealName());
                                dataInfoExcelFile.setCompanyCode(user.getCompanyCode());
                                dataInfoExcelFile.setCaseId(obj.getId());
                                dataInfoExcelFile.setCaseNumber(obj.getCaseNumber());
                                dataInfoExcelFileRepository.save(dataInfoExcelFile);
                            }
                        }
                    } catch (Exception e) {
                        log.error("查询案件信息失败", e);
                    }
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        log.info("发送第 {} 个文件", message.getCurrent());
        ProgressMessage progressMessage = new ProgressMessage();
        progressMessage.setUserId(message.getUserId());
        progressMessage.setTotal(message.getTotal());
        progressMessage.setCurrent(message.getCurrent());
        progressMessage.setText("正在处理文件");
        rabbitTemplate.convertAndSend("mr.cui.file.unReduce.progress", progressMessage);
    }

}
