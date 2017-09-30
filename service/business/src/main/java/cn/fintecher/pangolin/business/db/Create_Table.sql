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

--2017-09-25
--新增数据库备份表
--胡开甲
DROP TABLE IF EXISTS `system_backup`;
CREATE TABLE `system_backup` (
  `id` varchar(64) NOT NULL,
  `company_code` varchar(64) DEFAULT NULL COMMENT '公司的标识',
  `type` int(4) DEFAULT NULL COMMENT '备份类型 0：自动 1：手动',
  `mysql_name` varchar(255) DEFAULT NULL COMMENT 'mysql数据库文件名称',
  `mongdb_name` varchar(255) DEFAULT NULL COMMENT 'mongdb数据库名称',
  `operator` varchar(200) DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '备份时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--2017-09-26
--新增案件回收表
--祁吉贵
CREATE TABLE `case_info_return` (
  `id` varchar(64) NOT NULL DEFAULT '' COMMENT '主键',
  `case_id` varchar(64) DEFAULT NULL COMMENT '案件ID',
  `outsource_id` varchar(64) DEFAULT NULL COMMENT '委外案件ID',
  `operator_time` datetime DEFAULT NULL COMMENT '操作时间',
  `operator` varchar(64) DEFAULT NULL COMMENT '操作人（username)',
  `reason` varchar(1000) DEFAULT NULL COMMENT '退案原因',
  `source` int(4) DEFAULT NULL COMMENT '退回来源：内催，委外，司法，核销',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='案件回收';

--2017-09-26
--新增司法案件表
--夏群
CREATE TABLE `case_info_judicial` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `case_id` varchar(64) DEFAULT NULL COMMENT '案件ID',
  `operator_user_name` varchar(64) DEFAULT NULL COMMENT '操作人用户名',
  `operator_real_name` varchar(200) DEFAULT NULL COMMENT '操作人姓名',
  `operator_time` datetime DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='司法案件池';

--2017-09-28
--新增委外跟进记录表
--胡艳敏
DROP TABLE IF EXISTS `outsource_follow_record`;
CREATE TABLE `outsource_follow_record` (
  `id` varchar(64) NOT NULL,
  `company_code` varchar(64) DEFAULT NULL COMMENT '公司的标识',
  `case_id` varchar(64) DEFAULT NULL COMMENT '案件id',
  `case_num` varchar(64) DEFAULT NULL COMMENT '案件编号',
  `follow_time` datetime DEFAULT NULL COMMENT '跟进时间',
  `follow_type` int(4) DEFAULT NULL COMMENT '跟进方式 0：电话 1：外访',
  `object_name` int(4) DEFAULT NULL COMMENT '催收对象',
  `user_name` varchar(64) DEFAULT NULL COMMENT '姓名',
  `tel_status` int(4) DEFAULT NULL COMMENT '电话状态 64, 正常 65, 空号 66, 停机 67, 关机 68, 未知',
  `feedback` int(4) DEFAULT NULL COMMENT '催收反馈',
  `follow_record` varchar(1024) DEFAULT NULL COMMENT '跟进记录',
  `follow_person` varchar(64) DEFAULT NULL COMMENT '跟进人',
  `operator_name` varchar(100) DEFAULT NULL COMMENT '操作人姓名',
  `operator_time` datetime DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='委外案件跟进记录信息';