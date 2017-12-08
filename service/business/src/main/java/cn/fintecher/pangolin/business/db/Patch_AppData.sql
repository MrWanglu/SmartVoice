-- 格式如下:
--修改日期：
--修改内容：
--修改人：

--2017-09-14 qijigui 增加联系人关系 同学
INSERT INTO `pangolin_business`.`data_dict` (`id`, `type_code`, `code`, `name`, `sort`) VALUES ('219', '0015', NULL, '同学', '9');

--2017-09-14 huyanmin 增加公司序列号 sequence
ALTER TABLE company ADD sequence VARCHAR(4) DEFAULT NULL COMMENT '公司序列号';

--2017-09-15 huyanmin 增加新的权限码
INSERT INTO `resource` VALUES ('769', '176', '催大人', '公司名称', '080108', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 769);
INSERT INTO `resource` VALUES ('770', '239', '催大人', '批量管理', '090506', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 770);
INSERT INTO `resource` VALUES ('771', '243', '催大人', '公司名称', '080206', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 771);
INSERT INTO `resource` VALUES ('772', '239', '催大人', '数据导入批量', '090507', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 772);
INSERT INTO `resource` VALUES ('773', '184', '催大人', '系统公告', '0909FF', NULL, NULL, NULL, NULL, NULL, 18, NULL, NULL, NULL, NULL, 773);
INSERT INTO `resource` VALUES ('774', '773', '催大人', '用户名', '090901', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 774);
INSERT INTO `resource` VALUES ('775', '773', '催大人', '姓名', '090902', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 775);
INSERT INTO `resource` VALUES ('776', '773', '催大人', '状态', '090903', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 776);
INSERT INTO `resource` VALUES ('777', '773', '催大人', '全员发布公告', '090904', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 777);
INSERT INTO `resource` VALUES ('778', '773', '催大人', '发布公告', '090905', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 778);

--2017-09-19 huyanmin 删除权限码
DELETE from resource where id=708
--2017-09-19 huyanmin 增加新的权限码
INSERT INTO `resource` VALUES ('779', '74', '催大人', '结案案件', '040AFF', NULL, NULL, NULL, NULL, NULL, 18, NULL, NULL, NULL, NULL, 779);
INSERT INTO `resource` VALUES ('780', '779', '催大人', '客户姓名', '040A01', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 780);
INSERT INTO `resource` VALUES ('781', '779', '催大人', '手机号', '040A02', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 781);
INSERT INTO `resource` VALUES ('782', '779', '催大人', '申请省份', '040A03', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 782);
INSERT INTO `resource` VALUES ('783', '779', '催大人', '申请城市', '040A04', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 783);
INSERT INTO `resource` VALUES ('784', '779', '催大人', '批次号', '040A05', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 784);
INSERT INTO `resource` VALUES ('785', '779', '催大人', '还款状态', '040A06', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 785);
INSERT INTO `resource` VALUES ('786', '779', '催大人', '逾期天数', '040A07', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 786);
INSERT INTO `resource` VALUES ('787', '779', '催大人', '案件金额', '040A08', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 787);
INSERT INTO `resource` VALUES ('788', '779', '催大人', '案件手数', '040A09', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 788);
INSERT INTO `resource` VALUES ('789', '779', '催大人', '佣金比例', '040A10', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 789);
INSERT INTO `resource` VALUES ('790', '779', '催大人', '委托方', '040A11', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 790);
INSERT INTO `resource` VALUES ('791', '779', '催大人', '是否协催', '040A12', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 791);
INSERT INTO `resource` VALUES ('792', '779', '催大人', '催收反馈', '040A13', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 792);
INSERT INTO `resource` VALUES ('793', '779', '催大人', '协催方式', '040A14', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 793);
INSERT INTO `resource` VALUES ('794', '779', '催大人', '催收类型', '040A15', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 794);
INSERT INTO `resource` VALUES ('795', '779', '催大人', '结案删除', '040A16', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 795);
INSERT INTO `resource` VALUES ('796', '779', '催大人', '跟进记录', '040A17', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 796);
INSERT INTO `resource` VALUES ('797', '779', '催大人', '案件流转记录', '040A18', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 797);

--2017-09-20 huyanmin 增加新的权限码
INSERT INTO `resource` VALUES ('798', '107', '催大人', '案件退案', '040429', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 798);
INSERT INTO `resource` VALUES ('799', '74', '催大人', '退回案件', '040BFF', NULL, NULL, NULL, NULL, NULL, 18, NULL, NULL, NULL, NULL, 799);
INSERT INTO `resource` VALUES ('800', '799', '催大人', '客户姓名', '040B01', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 800);
INSERT INTO `resource` VALUES ('801', '799', '催大人', '手机号', '040B02', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 801);
INSERT INTO `resource` VALUES ('802', '799', '催大人', '申请省份', '040B03', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 802);
INSERT INTO `resource` VALUES ('803', '799', '催大人', '申请城市', '040B04', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 803);
INSERT INTO `resource` VALUES ('804', '799', '催大人', '批次号', '040B05', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 804);
INSERT INTO `resource` VALUES ('805', '799', '催大人', '还款状态', '040B06', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 805);
INSERT INTO `resource` VALUES ('806', '799', '催大人', '逾期天数', '	040B07', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 806);
INSERT INTO `resource` VALUES ('807', '799', '催大人', '案件金额', '040B08', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 807);
INSERT INTO `resource` VALUES ('808', '799', '催大人', '案件手数', '040B09', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 808);
INSERT INTO `resource` VALUES ('809', '799', '催大人', '佣金比例', '040B10', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 809);
INSERT INTO `resource` VALUES ('810', '799', '催大人', '委托方', '040B11', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 810);
INSERT INTO `resource` VALUES ('811', '799', '催大人', '是否协催', '040B12', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 811);
INSERT INTO `resource` VALUES ('812', '799', '催大人', '催收反馈', '040B13', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 812);
INSERT INTO `resource` VALUES ('813', '799', '催大人', '协催方式', '040B14', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 813);
INSERT INTO `resource` VALUES ('814', '799', '催大人', '催收类型', '040B15', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 814);
INSERT INTO `resource` VALUES ('815', '799', '催大人', '跟进记录', '040B16', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 815);

--2017-09-25 胡开甲 增加系统参数
INSERT INTO `sys_param` (`id`, `company_code`, `code`, `name`, `status`, `type`, `value`, `sign`, `operator`, `operate_time`, `remark`, `field`) VALUES ('ff8080815dfe341a797e0043da6f0007', '0001', 'Sysparam.mysqlbackup.address', 'mysql备份数据库脚本位置', '0', '9001', '/data/mysqlscript/mysqlbackup.sh', '0', 'administrator', '2017-09-21 14:36:06', 'mysql备份数据库脚本位置', NULL);
INSERT INTO `sys_param` (`id`, `company_code`, `code`, `name`, `status`, `type`, `value`, `sign`, `operator`, `operate_time`, `remark`, `field`) VALUES ('ff8080815dfe341a797e0043da6f0008', '0001', 'Sysparam.mysqlrecover.address', 'mysql数据库恢复脚本位置', '0', '9001', '/data/mysqlscript/mysqlrecover.sh', '0', 'administrator', '2017-09-21 14:38:24', 'mysql数据库恢复脚本位置', NULL);
INSERT INTO `sys_param` (`id`, `company_code`, `code`, `name`, `status`, `type`, `value`, `sign`, `operator`, `operate_time`, `remark`, `field`) VALUES ('ff8080815dfe341a797e0043da6f0009', '0001', 'Sysparam.mongodbbackup.address', 'mongodb备份数据库脚本位置', '0', '9001', '/data/mongoscript/mongdbbackup.sh', '0', 'administrator', '2017-09-22 15:35:52', 'mongodb备份数据库脚本位置', NULL);
INSERT INTO `sys_param` (`id`, `company_code`, `code`, `name`, `status`, `type`, `value`, `sign`, `operator`, `operate_time`, `remark`, `field`) VALUES ('ff8080815dfe341a797e0043da6f0010', '0001', 'Sysparam.mongodbrecover.address', 'mongodb数据库恢复脚本位置', '0', '9001', '/data/mongoscript/mongdbrecover.sh', '0', 'administrator', '2017-09-22 15:35:55', 'mongodb数据库恢复脚本位置', NULL);

--2017-09-26 祁吉贵 增加案件池类型
INSERT INTO `pangolin_business`.`data_dict_type` (`id`, `code`, `name`) VALUES ('50', '0050', '案件池类型');

INSERT INTO `pangolin_business`.`data_dict` (`id`, `type_code`, `code`, `name`, `sort`) VALUES ('225', '0050', NULL, '内催', '0');
INSERT INTO `pangolin_business`.`data_dict` (`id`, `type_code`, `code`, `name`, `sort`) VALUES ('226', '0050', NULL, '委外', '1');
INSERT INTO `pangolin_business`.`data_dict` (`id`, `type_code`, `code`, `name`, `sort`) VALUES ('227', '0050', NULL, '司法', '2');
INSERT INTO `pangolin_business`.`data_dict` (`id`, `type_code`, `code`, `name`, `sort`) VALUES ('228', '0050', NULL, '核销', '3');


--2017-09-25 huyanmin 新增导入跟进记录参数
INSERT INTO `sys_param` VALUES ('ff8080815dfe341a797e0043da6f0066', '0001', 'sys.outcase.followup', '委外案件跟进记录导入模版', 0, '9005', 'http://192.168.3.10:9000/file-service/api/fileUploadController/view/59fad7cc0f25c0362c83cd63.xlsx', 0, 'administrator', '2017-9-25 19:10:20', '委外案件跟进记录导入模版', NULL);
--2017-09-25 huyanmin 在委外池中新增3个字段
ALTER TABLE `outsource_pool` ADD COLUMN `company_code` varchar(64) DEFAULT NULL COMMENT '公司特定标识';
ALTER TABLE `outsource_pool` ADD COLUMN `over_outsource_time` date DEFAULT NULL COMMENT '委外到期时间';
ALTER TABLE `outsource_pool` ADD COLUMN `end_outsource_time` date DEFAULT NULL COMMENT '已结案日期';
ALTER TABLE `outsource_pool`
MODIFY COLUMN `contract_amt`  decimal(18,4) NULL DEFAULT NULL COMMENT '案件总金额' AFTER `overdue_periods`;
ALTER TABLE `outsource_pool`
MODIFY COLUMN `commission_rate`  varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '佣金比例' AFTER `out_batch`;
ALTER TABLE `outsource_pool`
MODIFY COLUMN `commission`  bigint(100) NULL DEFAULT NULL COMMENT '佣金' AFTER `commission_rate`;

--2017-09-27 huyanmin 增加新的权限码
INSERT INTO `resource` VALUES ('816', '156', '催大人', '导出还款明细', '06020C', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 816);
INSERT INTO `resource` VALUES ('817', '156', '催大人', '按钮', '06020D', NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, 817);

--2017-09-28 孙艳平 增加数据字典项
INSERT INTO `data_dict_type` (`id`, `code`, `name`) VALUES ('51', '0051', '策略类型');
INSERT INTO `pangolin_business`.`data_dict` (`id`, `type_code`, `code`, `name`, `sort`) VALUES ('230', '0051', NULL, '导入案件分配策略', '0');
INSERT INTO `pangolin_business`.`data_dict` (`id`, `type_code`, `code`, `name`, `sort`) VALUES ('231', '0051', NULL, '内催池案件分配策略', '1');
INSERT INTO `pangolin_business`.`data_dict` (`id`, `type_code`, `code`, `name`, `sort`) VALUES ('232', '0051', NULL, '委外池案件分配策略', '2');

--2017-09-29 胡艳敏 修改委外池中佣金比例名
ALTER TABLE `outsource_pool`
CHANGE COLUMN `commissionRate` `commission_rate`  varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `out_batch`;

--2017-09-29 胡艳敏 修改数据字典168-催收中
UPDATE `pangolin_business`.`data_dict` SET `name`='催收中' WHERE (`id`='168');
UPDATE `pangolin_business`.`data_dict` SET `name`='已结案'  WHERE (`id`='170');

--2017-10-09 胡艳敏 增加案件跟踪记录类型
ALTER TABLE `case_followup_record`
ADD COLUMN `case_followup_type`  int(4) DEFAULT NULL COMMENT '跟踪记录类型（内催、委外、司法、核销)';
ADD COLUMN `follow_time`  date DEFAULT NULL COMMENT '跟进时间';
ADD COLUMN `follow_person`  varchar(64) DEFAULT NULL COMMENT '跟进人员';

--2017-10-11 baizhangyu 近期sql修改记录整理
INSERT INTO `data_dict` VALUES (229, '0038', NULL, 'BeauPhone语音卡', 3);
INSERT INTO `sys_param` VALUES ('ff8080815dfe341a797e0043da6f0013', '0001', 'SysParam.bfyuyin.url', 'BeauPhone语音卡参数地址', 0, '9001', '192.168.3.79:8080', 0, 'administrator', '2017-9-29 10:46:40', NULL, NULL);
ALTER TABLE `user`
ADD COLUMN `channel_no`  varchar(64) NULL COMMENT '通道号码';
ALTER TABLE `user`
ADD COLUMN `zoneno`  varchar(10) NULL COMMENT '主叫电话区号';
ALTER TABLE `case_followup_record`
ADD COLUMN `file_name`  varchar(200) NULL COMMENT '录音文件名称';
ALTER TABLE `case_followup_record`
ADD COLUMN `file_path`  varchar(100) NULL COMMENT '录音文件在服务器上的目录';


--2017-10-13 孙艳平 案件回收表修改
ALTER TABLE `case_info_return`
DROP COLUMN `outsource_id`,
ADD COLUMN `outs_name`  varchar(100) NULL DEFAULT NULL COMMENT '委外方名称' AFTER `source`,
ADD COLUMN `out_time`  datetime NULL DEFAULT NULL COMMENT '委外日期' AFTER `outs_name`,
ADD COLUMN `over_outsource_time`  date NULL DEFAULT NULL COMMENT '委外结案日期' AFTER `out_time`,
ADD COLUMN `out_batch`  varchar(64) NULL DEFAULT NULL COMMENT '委外批次号' AFTER `over_outsource_time`,
ADD COLUMN `company_code`  varchar(64) NULL DEFAULT NULL COMMENT '公司Code' AFTER `out_batch`;

--2017-10-14 祁吉贵 增加了M0 配置了顺序
INSERT INTO `pangolin_business`.`data_dict` (`id`, `type_code`, `code`, `name`, `sort`) VALUES ('190', '0043', NULL, 'M1', '1');
INSERT INTO `pangolin_business`.`data_dict` (`id`, `type_code`, `code`, `name`, `sort`) VALUES ('191', '0043', NULL, 'M2', '2');
INSERT INTO `pangolin_business`.`data_dict` (`id`, `type_code`, `code`, `name`, `sort`) VALUES ('192', '0043', NULL, 'M3', '3');
INSERT INTO `pangolin_business`.`data_dict` (`id`, `type_code`, `code`, `name`, `sort`) VALUES ('193', '0043', NULL, 'M4', '4');
INSERT INTO `pangolin_business`.`data_dict` (`id`, `type_code`, `code`, `name`, `sort`) VALUES ('194', '0043', NULL, 'M5', '5');
INSERT INTO `pangolin_business`.`data_dict` (`id`, `type_code`, `code`, `name`, `sort`) VALUES ('195', '0043', NULL, 'M6+', '6');
INSERT INTO `pangolin_business`.`data_dict` (`id`, `type_code`, `code`, `name`, `sort`) VALUES ('244', '0043', NULL, 'M0', '0');

--2017-10-16 孙艳平 增加系统参数
INSERT INTO `pangolin_business`.`sys_param` (`id`, `company_code`, `code`, `name`, `status`, `type`, `value`, `sign`, `operator`, `operate_time`, `remark`, `field`) VALUES (UUID(), '0001', 'Sysparam.recover', '案件到期批量回收、回收提醒调度时间', '0', '9001', '000100', '0', 'administrator', '2017-10-16 13:45:51', '案件到期批量回收、提醒调度时间', NULL);
INSERT INTO `pangolin_business`.`sys_param` (`id`, `company_code`, `code`, `name`, `status`, `type`, `value`, `sign`, `operator`, `operate_time`, `remark`, `field`) VALUES (UUID(), '0001', 'Sysparam.recover.status', '案件到期批量回收、回收提醒状态', '0', '9001', '0', '0', 'administrator', '2017-10-16 11:37:13', '案件到期批量处理状态0-启用 1-停用', NULL);

--2017-10-17 白章宇 增加字段
ALTER TABLE `user_device`
ADD COLUMN `mac`  varchar(64) NULL COMMENT 'MAC地址' AFTER `field`;

--2017-10-17 孙艳平 resource增加权限
INSERT INTO `pangolin_business`.`resource` (`id`, `pid`, `sys_name`, `name`, `code`, `level`, `status`, `path`, `icon`, `type`, `file_type`, `remark`, `operator`, `operate_time`, `field`, `flag`) VALUES ('510', '484', '崔大人', '删除案件', '10030E', NULL, NULL, NULL, NULL, NULL, '19', NULL, NULL, NULL, NULL, '510');
INSERT INTO `pangolin_business`.`resource` (`id`, `pid`, `sys_name`, `name`, `code`, `level`, `status`, `path`, `icon`, `type`, `file_type`, `remark`, `operator`, `operate_time`, `field`, `flag`) VALUES ('511', '497', '催大人', '删除案件', '100409', NULL, NULL, NULL, NULL, NULL, '19', NULL, NULL, NULL, NULL, '511');
INSERT INTO `pangolin_business`.`resource` (`id`, `pid`, `sys_name`, `name`, `code`, `level`, `status`, `path`, `icon`, `type`, `file_type`, `remark`, `operator`, `operate_time`, `field`, `flag`) VALUES ('594', '526', '催大人', '删除案件', '0F0510', NULL, NULL, NULL, NULL, NULL, '19', NULL, NULL, NULL, NULL, '594');
INSERT INTO `pangolin_business`.`resource` (`id`, `pid`, `sys_name`, `name`, `code`, `level`, `status`, `path`, `icon`, `type`, `file_type`, `remark`, `operator`, `operate_time`, `field`, `flag`) VALUES ('595', '525', '催大人', '删除案件', '0F0412', NULL, NULL, NULL, NULL, NULL, '19', NULL, NULL, NULL, NULL, '595');

--2017-10-19 祁吉贵 增加催收机构
INSERT INTO `data_dict` (`id`, `type_code`, `code`, `name`, `sort`) VALUES ('245', '0036', 'C', '催收机构', '4');

--2017-10-24 白章宇 增加系统参数
INSERT INTO `sys_param` VALUES ('ff8080815dfe341a797e0043da6f00016', '0001', 'Sysparam.revokedistribute', '案件分案撤销时长', 0, '9001', '30', 0, 'administrator', '2017-10-17 18:41:15', '案件分案撤销时长(分钟)', '1');

--2017-10-30 祁吉贵 增加撤销案件权限码
INSERT INTO `pangolin_business_test`.`resource` VALUES ('439', '390', '催大人', '撤销分案', '0406FF', NULL, NULL, NULL, NULL, NULL, '18', NULL, NULL, NULL, NULL, '439');
INSERT INTO `pangolin_business_test`.`resource` VALUES ('440', '439', '催大人', '客户姓名', '040601', NULL, NULL, NULL, NULL, NULL, '19', NULL, NULL, NULL, NULL, '440');
INSERT INTO `pangolin_business_test`.`resource` VALUES ('441', '439', '催大人', '案件编号', '040602', NULL, NULL, NULL, NULL, NULL, '19', NULL, NULL, NULL, NULL, '441');
INSERT INTO `pangolin_business_test`.`resource` VALUES ('442', '439', '催大人', '批次号', '040603', NULL, NULL, NULL, NULL, NULL, '19', NULL, NULL, NULL, NULL, '442');
INSERT INTO `pangolin_business_test`.`resource` VALUES ('443', '439', '催大人', '委托方', '040604', NULL, NULL, NULL, NULL, NULL, '19', NULL, NULL, NULL, NULL, '443');
INSERT INTO `pangolin_business_test`.`resource` VALUES ('444', '439', '催大人', '原催收员', '040605', NULL, NULL, NULL, NULL, NULL, '19', NULL, NULL, NULL, NULL, '444');
INSERT INTO `pangolin_business_test`.`resource` VALUES ('445', '439', '催大人', '当前催收员', '040606', NULL, NULL, NULL, NULL, NULL, '19', NULL, NULL, NULL, NULL, '445');
INSERT INTO `pangolin_business_test`.`resource` VALUES ('446', '439', '催大人', '分案时间', '040607', NULL, NULL, NULL, NULL, NULL, '19', NULL, NULL, NULL, NULL, '446');
INSERT INTO `pangolin_business_test`.`resource` VALUES ('447', '439', '催大人', '数据来源', '040608', NULL, NULL, NULL, NULL, NULL, '19', NULL, NULL, NULL, NULL, '447');
INSERT INTO `pangolin_business_test`.`resource` VALUES ('448', '439', '催大人', '案件金额', '040609', NULL, NULL, NULL, NULL, NULL, '19', NULL, NULL, NULL, NULL, '448');
INSERT INTO `pangolin_business_test`.`resource` VALUES ('449', '439', '催大人', '撤销分案', '040610', NULL, NULL, NULL, NULL, NULL, '19', NULL, NULL, NULL, NULL, '449');

--2017-10-30 白章宇 增加字段
ALTER TABLE `case_distributed_temporary`
ADD COLUMN `case_remark`  varchar(64) NULL COMMENT '案件备注ID';

--2017-10-30 胡艳敏 公司表中增加新字段
ALTER TABLE `company`
ADD COLUMN `register_day`  int(6) NULL DEFAULT NULL COMMENT '注册天数 null没有注册  99999表示无线注册' AFTER `sequence`;
--2017-10-30 胡艳敏 软件注册，增加新的参数
INSERT INTO `sys_param` (`id`, `company_code`, `code`, `name`, `status`, `type`, `value`, `sign`, `operator`, `operate_time`, `remark`, `field`) VALUES ('ff8080815dfe341a797e0043da6f0015', '0001', 'SysParam.registersoftware', '软件注册码', '0', '9001', '21218cca77804d2ba1922c33e0151105', '0', 'administrator', '2017-10-25 17:49:35', NULL, NULL);

--2017-10-30 白章宇 增加字段
ALTER TABLE `case_distributed_temporary`
ADD COLUMN `current_department_code`  varchar(64) NULL COMMENT '案件当前所在部门code码';

--2017-10-31 祁吉贵 增加受托方的机构类型

INSERT INTO `data_dict_type` (`id`, `code`, `name`) VALUES ('52', '0052', '受托方机构类型');

INSERT INTO `data_dict` (`id`, `type_code`, `code`, `name`, `sort`) VALUES ('246', '0052', 'P', '贷款公司', '1');
INSERT INTO `data_dict` (`id`, `type_code`, `code`, `name`, `sort`) VALUES ('247', '0052', 'I', '保险公司', '2');
INSERT INTO `data_dict` (`id`, `type_code`, `code`, `name`, `sort`) VALUES ('248', '0052', 'O', '其他', '4');
INSERT INTO `data_dict` (`id`, `type_code`, `code`, `name`, `sort`) VALUES ('249', '0052', 'C', '催收机构', '3');

--2017-10-31
--新增数据字典项
--夏群
INSERT INTO `data_dict` VALUES ('253', '0008', null, '还款强制拒绝', '6');

--2017-10-31
--新增已结案设置导出项权限码
--胡艳敏
INSERT INTO `pangolin_business`.`resource` VALUES ('596', '525', '催大人', '设置导出项', '0F0413', NULL, NULL, NULL, NULL, NULL, '19', NULL, NULL, NULL, NULL, '596');
INSERT INTO `pangolin_business`.`resource` VALUES ('913', '858', '催大人', '操作时间', '090703', NULL, NULL, NULL, NULL, NULL, '19', NULL, NULL, NULL, NULL, '913');

--2017-11-06
--核销审批表案件来源字段
--袁艳婷
ALTER TABLE `case_info_verification_apply`
ADD COLUMN `source`  int(4) NULL DEFAULT NULL COMMENT '案件池来源' AFTER `commission_rate`;

--2017-11-06
--核销表添加打包状态字段
--袁艳婷
ALTER TABLE `case_info_verification`
DROP COLUMN `packing_status`,
ADD COLUMN `packing_status`  int(4) NULL DEFAULT NULL COMMENT '打包状态' AFTER `state`;

ALTER TABLE `case_info_verification`
ADD COLUMN `packing_status`  int(4) NULL COMMENT '打包状态' AFTER `state`;

--2017-11-14
--修改参数表名称
--胡艳敏
UPDATE `pangolin_business`.`sys_param` SET `name`='短信发送统计报表' WHERE name like '%报标';
UPDATE `pangolin_business`.`sys_param` SET `name`='协催申请失效天数(天)' WHERE name like '协催申请失效%';
UPDATE `pangolin_business`.`sys_param` SET `name`='消息提醒批量(时分秒)' WHERE name = '消息提醒批量';
UPDATE `pangolin_business`.`sys_param` SET `name`='案件到期批量回收、回收提醒调度时间(时分秒)' WHERE name = '案件到期批量回收、回收提醒调度时间';
UPDATE `pangolin_business`.`sys_param` SET `name`='案件分案撤销时长(分钟)' WHERE name = '案件分案撤销时长';

--2017-11-15
--司法表增加说明字段
--袁艳婷
ALTER TABLE `case_info_judicial`
ADD COLUMN `state`  varchar(255) NULL AFTER `company_code`;



--2017-12-07
--还款申请表增加部门Code
--祁吉贵
ALTER TABLE `case_pay_apply`
ADD COLUMN `dept_code`  varchar(128) NULL COMMENT '部门code' AFTER `case_amt`;


--2017-12-07
--电催提前流转表 修改depart_id 为 dept_code
--彭长须
ALTER TABLE `case_advance_turn_applay`
change `depart_id` `dept_code` varchar(64) DEFAULT NULL COMMENT '部门Code';


--2017-12-07
--协催表添加外访协催审批人的部门code
--胡艳敏
ALTER TABLE `case_assist`
ADD COLUMN `dept_code`  varchar(128) NULL COMMENT '外访协催审批人的部门code' AFTER `assist_close_flag`;

--2017-12-07
--case_info_verification_apply 部门Code
--彭长须
ALTER TABLE `case_info_verification_apply`
ADD COLUMN `dept_code`  varchar(128) NULL COMMENT '部门code' AFTER `case_number`;

--2017-12-07
--case_assist_apply 修改depart_id 为 dept_code
--彭长须
ALTER TABLE `case_assist_apply`
change `depart_id` `dept_code` varchar(64) DEFAULT NULL COMMENT '部门Code';