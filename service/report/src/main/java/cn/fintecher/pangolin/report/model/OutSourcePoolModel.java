package cn.fintecher.pangolin.report.model;

import lombok.Data;

import java.util.List;

/**
 * Created by huyanmin on 2017/9/26.
 */

@Data
public class OutSourcePoolModel {

    List<QueryOutsourcePool> content;

    int totalPages;

    long totalElements;

}
