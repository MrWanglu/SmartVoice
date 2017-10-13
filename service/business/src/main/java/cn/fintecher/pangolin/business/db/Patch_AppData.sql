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
INSERT INTO `sys_param` VALUES ('ff8080815dfe341a797e0043da6f0011', '0001', 'sys.outcase.followup', '委外案件跟进记录导入模版', 0, '9005', 'http://192.168.3.10/group1/M00/00/4D/wKgDClngp1qAKmQbAAArWaEzWOc85.xlsx', 0, 'administrator', '2017-9-25 19:10:20', '委外案件跟进记录导入模版', NULL);
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
