package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-09-22-14:34
 */
@Data
public class DeleteSystemBackupIds {
    private List<String> ids; //案件ID
}
