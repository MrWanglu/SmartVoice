package cn.fintecher.pangolin.entity.message;

import cn.fintecher.pangolin.entity.file.UploadFile;
import lombok.Data;

import java.io.Serializable;

/**
 * 文件解压缩并上传成功消息
 * Created by ChenChang on 2017/3/31.
 */
@Data
public class UnReduceFileMessage implements Serializable {
    private UploadFile uploadFile;
    private String path;
    private String batchNum;
    private String userId;
    private Integer total;
    private Integer current;

}
