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

