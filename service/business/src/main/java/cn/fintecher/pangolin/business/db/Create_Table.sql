-- 格式如下:
--修改日期：
--修改内容：
--修改人：

--2017-09-20
--新增案件备注信息表
--夏群
CREATE TABLE `case_info_remark` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `case_id` varchar(64) DEFAULT NULL COMMENT '案件ID',
  `remark` varchar(1000) DEFAULT NULL COMMENT '备注信息',
  `operator_user_name` varchar(64) DEFAULT NULL COMMENT '操作人用户名',
  `operator_real_name` varchar(200) DEFAULT NULL COMMENT '操作人姓名',
  `operator_time` datetime DEFAULT NULL COMMENT '操作时间',
  `company_code` varchar(64) DEFAULT NULL COMMENT '公司code码',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='案件备注信息';