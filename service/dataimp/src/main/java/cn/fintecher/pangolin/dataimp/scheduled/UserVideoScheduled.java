package cn.fintecher.pangolin.dataimp.scheduled;

import cn.fintecher.pangolin.dataimp.entity.UserVideo;
import cn.fintecher.pangolin.dataimp.repository.UserVideoRepository;
import cn.fintecher.pangolin.util.VideoSwitchUtil;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.util.ZWStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;


/**
 * Created by qijigui on 2017-11-08.
 */

@Component
@EnableScheduling
public class UserVideoScheduled {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(UserVideoScheduled.class);

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    UserVideoRepository userVideoRepository;

//    @Scheduled(cron = "0 0 19 * * ?")
//    void saveUserVideos() throws Exception {
//        logger.debug("开始生成催收员录音");
//        try {
//            String filePath = "/home/data/";
//            String ffmpeg = "/usr/bin/ffmpeg";
//            //获取所有的催收用户
//            ResponseEntity<Iterable> userIterables = restTemplate.getForEntity("http://business-service/api/userResource/getAllUsers".concat("?companyCode=0001"), Iterable.class);
//            if (Objects.isNull(userIterables) || !userIterables.hasBody()) {
//                return;
//            }
//            //获取所有的催收员的文件夹
//            File file = new File(filePath.concat(ZWDateUtil.getFormatNowDate("yyyyMMdd")));
//            for (Object object : userIterables.getBody()) {
//                Map user = (Map) object;
//                if (Objects.nonNull(file) && file.list().length > 0) {
//                    String[] filelist = file.list();
//                    //遍历催收员文件夹
//                    for (int i = 0; i < filelist.length; i++) {
//                        //匹配催收员
//                        if (user.get("userName").toString().equals(filelist[i].toString())) {
//                            //根据匹配的催收员匹配下一层文件夹 取 WAV文件
//                            File fileNew = new File(filePath.concat(ZWDateUtil.getFormatNowDate("yyyyMMdd")).concat("\\").concat(filelist[i].toString()));
//                            String[] fileNewList = fileNew.list();
//                            if (Objects.nonNull(fileNewList) && fileNewList.length > 0) {
//                                for (int j = 0; j < fileNewList.length; j++) {
//                                    if (fileNewList[j].toString().endsWith(".WAV") || fileNewList[j].toString().endsWith(".wav")) {
//                                        //转化成mp3
//                                        String newPath = VideoSwitchUtil.switchToMp3(filePath.concat(ZWDateUtil.getFormatNowDate("yyyyMMdd")).concat("\\").concat(filelist[i].toString()).concat("\\".concat(fileNewList[j].toString())), ffmpeg);
//                                        //获取时长
//                                        String videoLength = VideoSwitchUtil.getVideoTime(filePath.concat(ZWDateUtil.getFormatNowDate("yyyyMMdd")).concat("\\").concat(filelist[i].toString()).concat("\\".concat(fileNewList[j].toString())), ffmpeg);
//                                        //上传文件服务器
//                                        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
//                                        FileSystemResource fileSystemResource = new FileSystemResource(newPath);
//                                        param.add("file", fileSystemResource);
//                                        String url = restTemplate.postForObject("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class);
//                                        //新增记录
//                                        if (!ZWStringUtils.isEmpty(url)) {
//                                            UserVideo userVideo = new UserVideo();
//                                            userVideo.setCompanyCode("0001");
//                                            userVideo.setVideoName(fileNewList[j].toString());
//                                            userVideo.setDeptCode(((Map) user.get("department")).get("code").toString());
//                                            userVideo.setDeptName(((Map) user.get("department")).get("name").toString());
//                                            userVideo.setOperatorTime(ZWDateUtil.getNowDateTime());
//                                            userVideo.setUserName(user.get("userName").toString());
//                                            userVideo.setUserRealName(user.get("realName").toString());
//                                            userVideo.setVideoUrl(url);
//                                            userVideo.setVideoLength(videoLength);
//                                            userVideoRepository.save(userVideo);
//                                        }
//                                    }
//                                }
//                            } else {
//                                logger.error("该催收员下面没有找到对应的录音文件");
//                            }
//                        }
//                    }
//                } else {
//                    logger.error("日期下面没有找到催收员对应的文件夹");
//                }
//            }
//            //创建新文件 下一天用
//            Calendar calendar = Calendar.getInstance();
//            calendar.add(Calendar.DATE, 1);
//            Date date = calendar.getTime();
//            new File(filePath.concat(ZWDateUtil.fomratterDate(date, "yyyyMMdd"))).mkdirs();
//        } catch (Exception ex) {
//            logger.error(ex.getMessage(), ex);
//        }
//    }
}
