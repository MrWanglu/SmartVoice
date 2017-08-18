package cn.fintecher.pangolin.entity.message;

import cn.fintecher.pangolin.entity.file.UploadFile;
import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传成功消息
 * Created by ChenChang on 2017/3/31.
 */
@Data
public class ImportFileUploadSuccessMessage implements Serializable {
    private UploadFile uploadFile;
    private String batchNum;
    private String userId;
    private String userName;
    private String companyCode;

}
