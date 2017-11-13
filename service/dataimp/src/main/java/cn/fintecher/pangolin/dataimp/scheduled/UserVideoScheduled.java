package cn.fintecher.pangolin.dataimp.scheduled;

import cn.fintecher.pangolin.dataimp.entity.UserVideo;
import cn.fintecher.pangolin.dataimp.repository.UserVideoRepository;
import cn.fintecher.pangolin.util.VideoSwitchUtil;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.util.ZWStringUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
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
    final String spliterStr = System.getProperty("file.separator");

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    UserVideoRepository userVideoRepository;

    @Scheduled(cron = "0 0 15 * * ?")
    public void autoUserVideo() {

        String filePath = String.format("%shome%sdata%s", spliterStr, spliterStr, spliterStr);
        String ffmpeg = String.format("%susr%sbin%sffmpeg", spliterStr, spliterStr, spliterStr);

        try {
            //获取所有的催收用户
            ResponseEntity<Iterable> userIterables = restTemplate.getForEntity("http://business-service/api/userResource/getAllUsers".concat("?companyCode=0001"), Iterable.class);
            if (Objects.isNull(userIterables) || !userIterables.hasBody())
                return;
            //得到今天的录音文件夹
            String dateDir = filePath + ZWDateUtil.getFormatNowDate("yyyyMMdd");
            File file = new File(dateDir);
            if (Objects.isNull(file)) {
                logger.error("访问录音文件夹失败：" + dateDir);
                return;
            }
            for (Object object : userIterables.getBody()) {
                Map user = (Map) object;
                if (Objects.nonNull(file) && file.list().length > 0) {
                    //遍历催收员文件夹
                    for (String fileName : file.list()) {
                        //匹配催收员
                        if (!user.get("userName").toString().equals(fileName.toString()))
                            continue;

                        //根据匹配的催收员匹配下一层文件夹 取 WAV文件
                        //eg: /home/data/20171010/pingping
                        String userDir = dateDir + spliterStr + fileName;
                        String[] fileNewList = (new File(userDir)).list();
                        if (Objects.nonNull(fileNewList) && fileNewList.length > 0) {
                            for (String recFile : fileNewList) {
                                if (recFile.endsWith(".WAV") || recFile.endsWith(".wav")) {
                                    logger.debug("开始生成催收员录音:" + recFile);
                                    //转换文件并保存到数据库
                                    ConvertRecFile(recFile, userDir, ffmpeg, user);
                                }
                            }
                        }
                    }
                }
            }
            //创建新文件 下一天用
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 1);
            Date date = calendar.getTime();
            new File(filePath.concat(ZWDateUtil.fomratterDate(date, "yyyyMMdd"))).mkdirs();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /*
        fileName:要转换的MP3格式的文件
        toolPaht:转换工具所在的目录
        mapUser： 用户列表
     */
    private void ConvertRecFile(String fileName, String userDir, String toolPath, Map mapUser) {

        //转化成mp3
        String convertFilePath = userDir + spliterStr + fileName;
        String newPath = VideoSwitchUtil.switchToMp3(convertFilePath, toolPath);
        String videoLength = "";
        //获取时长
        try {
            videoLength = VideoSwitchUtil.getVideoTime(convertFilePath, toolPath);
        } catch (Exception ex) {
            logger.error("获取录音文件时长出错：", ex);
        }

        //上传文件服务器
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        FileSystemResource fileSystemResource = new FileSystemResource(newPath);
        param.add("file", fileSystemResource);
        String url = restTemplate.postForObject("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class);

        if (ZWStringUtils.isEmpty(url)) {
            logger.error("获取文件上传路径出错");
            return;
        }

        //新增记录
        UserVideo userVideo = new UserVideo();
        userVideo.setCompanyCode("0001");
        userVideo.setVideoName(fileName.replace("WAV", "mp3"));
        userVideo.setDeptCode(((Map) mapUser.get("department")).get("code").toString());
        userVideo.setDeptName(((Map) mapUser.get("department")).get("name").toString());
        userVideo.setOperatorTime(ZWDateUtil.getNowDateTime());
        userVideo.setUserName(mapUser.get("userName").toString());
        userVideo.setUserRealName(mapUser.get("realName").toString());
        userVideo.setVideoUrl(url);
        userVideo.setVideoLength(videoLength);
        userVideoRepository.save(userVideo);
    }

}
