package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.ItemsModel;
import cn.fintecher.pangolin.business.repository.ExportItemRepository;
import cn.fintecher.pangolin.entity.ExportItem;
import cn.fintecher.pangolin.entity.QExportItem;
import cn.fintecher.pangolin.entity.User;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Administrator on 2017/9/26.
 */
@Service("ExportItemService")
public class ExportItemService {

    @Inject
    ExportItemRepository exportItemRepository;

    public void saveExportItems(ItemsModel items,User user,Integer category) {
            List<String> personalItems = items.getPersonalItems();
            List<ExportItem> personalList = IterableUtils.toList(exportItemRepository.findAll(QExportItem.exportItem.companyCode.eq(user.getCompanyCode())
                    .and(QExportItem.exportItem.type.eq(ExportItem.Type.PERSONAL.getValue()))
                    .and(QExportItem.exportItem.category.eq(category))));
            for (ExportItem item : personalList) {
                if (personalItems.contains(item.getName())) {
                    item.setStatu(0);
                } else {
                    item.setStatu(1);
                }
            }
            exportItemRepository.save(personalList);

            List<String> jobItems = items.getJobItems();
            List<ExportItem> jobList = IterableUtils.toList(exportItemRepository.findAll(QExportItem.exportItem.companyCode.eq(user.getCompanyCode())
                    .and(QExportItem.exportItem.type.eq(ExportItem.Type.JOB.getValue()))
                    .and(QExportItem.exportItem.category.eq(category))));
            for (ExportItem item : jobList) {
                if (jobItems.contains(item.getName())) {
                    item.setStatu(0);
                } else {
                    item.setStatu(1);
                }
            }
            exportItemRepository.save(jobList);

            List<String> connectItems = items.getConnectItems();
            List<ExportItem> connectList = IterableUtils.toList(exportItemRepository.findAll(QExportItem.exportItem.companyCode.eq(user.getCompanyCode())
                    .and(QExportItem.exportItem.type.eq(ExportItem.Type.CONCAT.getValue()))
                    .and(QExportItem.exportItem.category.eq(category))));
            for (ExportItem item : connectList) {
                if (connectItems.contains(item.getName())) {
                    item.setStatu(0);
                } else {
                    item.setStatu(1);
                }
            }
            exportItemRepository.save(connectList);

            List<String> caseItems = items.getCaseItems();
            List<ExportItem> caseList = IterableUtils.toList(exportItemRepository.findAll(QExportItem.exportItem.companyCode.eq(user.getCompanyCode())
                    .and(QExportItem.exportItem.type.eq(ExportItem.Type.CASE.getValue()))
                    .and(QExportItem.exportItem.category.eq(category))));
            for (ExportItem item : caseList) {
                if (caseItems.contains(item.getName())) {
                    item.setStatu(0);
                } else {
                    item.setStatu(1);
                }
            }
            exportItemRepository.save(caseList);

            List<String> bankItems = items.getBankItems();
            List<ExportItem> bankList = IterableUtils.toList(exportItemRepository.findAll(QExportItem.exportItem.companyCode.eq(user.getCompanyCode())
                    .and(QExportItem.exportItem.type.eq(ExportItem.Type.BANK.getValue()))
                    .and(QExportItem.exportItem.category.eq(category))));
            for (ExportItem item : bankList) {
                if (bankItems.contains(item.getName())) {
                    item.setStatu(0);
                } else {
                    item.setStatu(1);
                }
            }
            exportItemRepository.save(bankList);

            List<String> followItems = items.getFollowItems();
            List<ExportItem> followList = IterableUtils.toList(exportItemRepository.findAll(QExportItem.exportItem.companyCode.eq(user.getCompanyCode())
                    .and(QExportItem.exportItem.type.eq(ExportItem.Type.FOLLOW.getValue()))
                    .and(QExportItem.exportItem.category.eq(category))));
            for (ExportItem item : followList) {
                if (followItems.contains(item.getName())) {
                    item.setStatu(0);
                } else {
                    item.setStatu(1);
                }
            }
            exportItemRepository.save(followList);
    }

    public void createExportItems(User user,Integer category){
        createPersonalItems(user,category);
        createJobItems(user,category);
        createConnectItems(user,category);
        if(category==1 || category == 3){
            createCaseItems(user,category);
        } else if(category==2){
            createOutsourceCaseItems(user,category);
        }
        createBankItems(user,category);
        if(category == 3){
            return ;
        }
        createFollowItems(user,category);
    }

    public void createPersonalItems(User user,Integer category){
        List<ExportItem> personalItems = new ArrayList<>();
        ExportItem temp = new ExportItem();
        temp.setStatu(1);
        temp.setCategory(category);
        temp.setType(ExportItem.Type.PERSONAL.getValue());
        temp.setCompanyCode(user.getCompanyCode());
        ExportItem name = new ExportItem();
        BeanUtils.copyProperties(temp,name);
        name.setName("客户姓名");
        personalItems.add(name);
        ExportItem idCard = new ExportItem();
        BeanUtils.copyProperties(temp,idCard);
        idCard.setName("身份证号");
        personalItems.add(idCard);
        ExportItem mobile = new ExportItem();
        BeanUtils.copyProperties(temp,mobile);
        mobile.setName("手机号码");
        personalItems.add(mobile);
        ExportItem idCardAddress = new ExportItem();
        BeanUtils.copyProperties(temp,idCardAddress);
        idCardAddress.setName("身份证户籍地址");
        personalItems.add(idCardAddress);
        ExportItem address = new ExportItem();
        BeanUtils.copyProperties(temp,address);
        address.setName("家庭住址");
        personalItems.add(address);
        ExportItem phone = new ExportItem();
        BeanUtils.copyProperties(temp,phone);
        phone.setName("固定电话");
        personalItems.add(phone);
        ExportItem province = new ExportItem();
        BeanUtils.copyProperties(temp,province);
        province.setName("省份");
        personalItems.add(province);
        ExportItem city = new ExportItem();
        BeanUtils.copyProperties(temp,city);
        city.setName("城市");
        personalItems.add(city);
        exportItemRepository.save(personalItems);
    }

    public void createJobItems(User user,Integer category) {
        List<ExportItem> jobItems = new ArrayList<>();
        ExportItem temp = new ExportItem();
        temp.setStatu(1);
        temp.setCategory(category);
        temp.setType(ExportItem.Type.JOB.getValue());
        temp.setCompanyCode(user.getCompanyCode());
        ExportItem name = new ExportItem();
        BeanUtils.copyProperties(temp, name);
        name.setName("工作单位名称");
        jobItems.add(name);
        ExportItem address = new ExportItem();
        BeanUtils.copyProperties(temp, address);
        address.setName("工作单位地址");
        jobItems.add(address);
        ExportItem phone = new ExportItem();
        BeanUtils.copyProperties(temp, phone);
        phone.setName("工作单位电话");
        jobItems.add(phone);
        exportItemRepository.save(jobItems);
    }

    public void createConnectItems(User user,Integer category) {
        List<ExportItem> connectItems = new ArrayList<>();
        ExportItem temp = new ExportItem();
        temp.setStatu(1);
        temp.setCategory(category);
        temp.setType(ExportItem.Type.CONCAT.getValue());
        temp.setCompanyCode(user.getCompanyCode());
        ExportItem name = new ExportItem();
        BeanUtils.copyProperties(temp, name);
        name.setName("姓名");
        connectItems.add(name);
        ExportItem mobile = new ExportItem();
        BeanUtils.copyProperties(temp, mobile);
        mobile.setName("手机号码");
        connectItems.add(mobile);
        ExportItem phone = new ExportItem();
        BeanUtils.copyProperties(temp, phone);
        phone.setName("住宅电话");
        connectItems.add(phone);
        ExportItem address = new ExportItem();
        BeanUtils.copyProperties(temp, address);
        address.setName("现居住地址");
        connectItems.add(address);
        ExportItem target = new ExportItem();
        BeanUtils.copyProperties(temp, target);
        target.setName("与客户关系");
        connectItems.add(target);
        ExportItem job = new ExportItem();
        BeanUtils.copyProperties(temp, job);
        job.setName("工作单位");
        connectItems.add(job);
        ExportItem jobPhone = new ExportItem();
        BeanUtils.copyProperties(temp, jobPhone);
        jobPhone.setName("单位电话");
        connectItems.add(jobPhone);
        exportItemRepository.save(connectItems);
    }

    public void createCaseItems(User user,Integer category) {
        List<ExportItem> caseItems = new ArrayList<>();
        ExportItem temp = new ExportItem();
        temp.setStatu(1);
        temp.setCategory(category);
        temp.setType(ExportItem.Type.CASE.getValue());
        if(Objects.nonNull(user.getCompanyCode())){
            temp.setCompanyCode(user.getCompanyCode());
        }
        ExportItem product = new ExportItem();
        BeanUtils.copyProperties(temp, product);
        product.setName("产品系列");
        caseItems.add(product);
        ExportItem contractNum = new ExportItem();
        BeanUtils.copyProperties(temp, contractNum);
        contractNum.setName("合同编号");
        caseItems.add(contractNum);
        ExportItem city = new ExportItem();
        BeanUtils.copyProperties(temp, city);
        city.setName("城市");
        caseItems.add(city);
        ExportItem province = new ExportItem();
        BeanUtils.copyProperties(temp, province);
        province.setName("省份");
        caseItems.add(province);
        ExportItem loanDate = new ExportItem();
        BeanUtils.copyProperties(temp, loanDate);
        loanDate.setName("贷款日期");
        caseItems.add(loanDate);
        ExportItem contractAmt = new ExportItem();
        BeanUtils.copyProperties(temp, contractAmt);
        contractAmt.setName("合同金额");
        caseItems.add(contractAmt);
        ExportItem leftCapital = new ExportItem();
        BeanUtils.copyProperties(temp, leftCapital);
        leftCapital.setName("剩余本金(元)");
        caseItems.add(leftCapital);
        ExportItem leftInterest = new ExportItem();
        BeanUtils.copyProperties(temp, leftInterest);
        leftInterest.setName("剩余利息(元)");
        caseItems.add(leftInterest);
        ExportItem overDueAmt = new ExportItem();
        BeanUtils.copyProperties(temp, overDueAmt);
        overDueAmt.setName("逾期总金额(元)");
        caseItems.add(overDueAmt);
        ExportItem overdueCapital = new ExportItem();
        BeanUtils.copyProperties(temp, overdueCapital);
        overdueCapital.setName("逾期本金(元)");
        caseItems.add(overdueCapital);
        ExportItem overdueInterest = new ExportItem();
        BeanUtils.copyProperties(temp, overdueInterest);
        overdueInterest.setName("逾期利息(元)");
        caseItems.add(overdueInterest);
        ExportItem overdueFine = new ExportItem();
        BeanUtils.copyProperties(temp, overdueFine);
        overdueFine.setName("逾期罚息(元)");
        caseItems.add(overdueFine);
        ExportItem periods = new ExportItem();
        BeanUtils.copyProperties(temp, periods);
        periods.setName("还款期数");
        caseItems.add(periods);
        ExportItem perPayAmount = new ExportItem();
        BeanUtils.copyProperties(temp, perPayAmount);
        perPayAmount.setName("每期还款金额(元)");
        caseItems.add(perPayAmount);
        ExportItem otherAmt = new ExportItem();
        BeanUtils.copyProperties(temp, otherAmt);
        otherAmt.setName("其他费用(元)");
        caseItems.add(otherAmt);
        ExportItem overDueDate = new ExportItem();
        BeanUtils.copyProperties(temp, overDueDate);
        overDueDate.setName("逾期日期");
        caseItems.add(overDueDate);
        ExportItem overduePeriods = new ExportItem();
        BeanUtils.copyProperties(temp, overduePeriods);
        overduePeriods.setName("逾期期数");
        caseItems.add(overduePeriods);
        ExportItem overdueDays = new ExportItem();
        BeanUtils.copyProperties(temp, overdueDays);
        overdueDays.setName("逾期天数");
        caseItems.add(overdueDays);
        ExportItem hasPayAmt = new ExportItem();
        BeanUtils.copyProperties(temp, hasPayAmt);
        hasPayAmt.setName("已还款金额(元)");
        caseItems.add(hasPayAmt);
        ExportItem hasPayPeriods = new ExportItem();
        BeanUtils.copyProperties(temp, hasPayPeriods);
        hasPayPeriods.setName("已还款期数");
        caseItems.add(hasPayPeriods);
        ExportItem latelyPayDate = new ExportItem();
        BeanUtils.copyProperties(temp, latelyPayDate);
        latelyPayDate.setName("最近还款日期");
        caseItems.add(latelyPayDate);
        ExportItem latelyPayAmt = new ExportItem();
        BeanUtils.copyProperties(temp, latelyPayAmt);
        latelyPayAmt.setName("最近还款金额(元)");
        caseItems.add(latelyPayAmt);
        ExportItem rate = new ExportItem();
        BeanUtils.copyProperties(temp, rate);
        rate.setName("佣金比例(%)");
        caseItems.add(rate);
        exportItemRepository.save(caseItems);
    }

    public void createOutsourceCaseItems(User user,Integer category) {
        List<ExportItem> caseItems = new ArrayList<>();
        ExportItem temp = new ExportItem();
        temp.setStatu(1);
        temp.setCategory(category);
        temp.setType(ExportItem.Type.CASE.getValue());
        if(Objects.nonNull(user.getCompanyCode())){
            temp.setCompanyCode(user.getCompanyCode());
        }
        ExportItem outsourceAmt = new ExportItem();
        BeanUtils.copyProperties(temp, outsourceAmt);
        outsourceAmt.setName("委外案件总金额");
        caseItems.add(outsourceAmt);

        ExportItem hasPayAmt = new ExportItem();
        BeanUtils.copyProperties(temp, hasPayAmt);
        hasPayAmt.setName("已还款金额(元)");
        caseItems.add(hasPayAmt);

        ExportItem leftCapital = new ExportItem();
        BeanUtils.copyProperties(temp, leftCapital);
        leftCapital.setName("剩余金额(元)");
        caseItems.add(leftCapital);

        ExportItem outsourceName = new ExportItem();
        BeanUtils.copyProperties(temp, outsourceName);
        outsourceName.setName("委外方");
        caseItems.add(outsourceName);

        ExportItem batchNum = new ExportItem();
        BeanUtils.copyProperties(temp, batchNum);
        batchNum.setName("批次号");
        caseItems.add(batchNum);

        ExportItem outTime = new ExportItem();
        BeanUtils.copyProperties(temp, outTime);
        outTime.setName("委案日期");
        caseItems.add(outTime);

        ExportItem overOutsourceTime = new ExportItem();
        BeanUtils.copyProperties(temp, overOutsourceTime);
        overOutsourceTime.setName("委案到期日期");
        caseItems.add(overOutsourceTime);

        ExportItem overdueDays = new ExportItem();
        BeanUtils.copyProperties(temp, overdueDays);
        overdueDays.setName("剩余委托时间(天)");
        caseItems.add(overdueDays);

        ExportItem caseStatus = new ExportItem();
        BeanUtils.copyProperties(temp, caseStatus);
        caseStatus.setName("案件状态");
        caseItems.add(caseStatus);

        ExportItem rate = new ExportItem();
        BeanUtils.copyProperties(temp, rate);
        rate.setName("佣金比例(%)");
        caseItems.add(rate);

        ExportItem endOutsourceTime = new ExportItem();
        BeanUtils.copyProperties(temp, endOutsourceTime);
        endOutsourceTime.setName("结案日期");
        caseItems.add(endOutsourceTime);

        exportItemRepository.save(caseItems);
    }

    public void createBankItems(User user,Integer category) {
        List<ExportItem> bankItems = new ArrayList<>();
        ExportItem temp = new ExportItem();
        temp.setStatu(1);
        temp.setCategory(category);
        temp.setType(ExportItem.Type.BANK.getValue());
        temp.setCompanyCode(user.getCompanyCode());
        ExportItem name = new ExportItem();
        BeanUtils.copyProperties(temp, name);
        name.setName("客户还款卡银行");
        bankItems.add(name);
        ExportItem card = new ExportItem();
        BeanUtils.copyProperties(temp, card);
        card.setName("客户还款卡号");
        bankItems.add(card);
        exportItemRepository.save(bankItems);
    }

    public void createFollowItems(User user,Integer category) {
        List<ExportItem> followItems = new ArrayList<>();
        ExportItem temp = new ExportItem();
        temp.setStatu(1);
        temp.setCategory(category);
        temp.setType(ExportItem.Type.FOLLOW.getValue());
        temp.setCompanyCode(user.getCompanyCode());
        ExportItem date = new ExportItem();
        BeanUtils.copyProperties(temp, date);
        date.setName("跟进时间");
        followItems.add(date);
        ExportItem type = new ExportItem();
        BeanUtils.copyProperties(temp, type);
        type.setName("跟进方式");
        followItems.add(type);
        ExportItem target = new ExportItem();
        BeanUtils.copyProperties(temp, target);
        target.setName("催收对象");
        followItems.add(target);
        ExportItem targetName = new ExportItem();
        BeanUtils.copyProperties(temp, targetName);
        targetName.setName("姓名");
        followItems.add(targetName);
        ExportItem statu = new ExportItem();
        BeanUtils.copyProperties(temp, statu);
        statu.setName("电话/地址");
        followItems.add(statu);
        ExportItem back = new ExportItem();
        BeanUtils.copyProperties(temp, back);
        back.setName("催收反馈");
        followItems.add(back);
        ExportItem content = new ExportItem();
        BeanUtils.copyProperties(temp, content);
        content.setName("跟进记录");
        followItems.add(content);
        ExportItem operator = new ExportItem();
        BeanUtils.copyProperties(temp, operator);
        operator.setName("跟进人员");
        followItems.add(operator);
        exportItemRepository.save(followItems);
    }

    public List parseItems(List<ExportItem> items){
        List<String> list = new ArrayList<>();
        for(ExportItem item : items){
            list.add(item.getName());
        }
        return list;
    }

    public ItemsModel getExportItems(User user,Integer category){
        List<ExportItem> personalList = IterableUtils.toList(exportItemRepository.findAll(QExportItem.exportItem.companyCode.eq(user.getCompanyCode())
                .and(QExportItem.exportItem.statu.eq(0))
                .and(QExportItem.exportItem.type.eq(ExportItem.Type.PERSONAL.getValue()))
                .and(QExportItem.exportItem.category.eq(category))));
        List<ExportItem> jobList = IterableUtils.toList(exportItemRepository.findAll(QExportItem.exportItem.companyCode.eq(user.getCompanyCode())
                .and(QExportItem.exportItem.statu.eq(0))
                .and(QExportItem.exportItem.type.eq(ExportItem.Type.JOB.getValue()))
                .and(QExportItem.exportItem.category.eq(category))));
        List<ExportItem> connectList = IterableUtils.toList(exportItemRepository.findAll(QExportItem.exportItem.companyCode.eq(user.getCompanyCode())
                .and(QExportItem.exportItem.statu.eq(0))
                .and(QExportItem.exportItem.type.eq(ExportItem.Type.CONCAT.getValue()))
                .and(QExportItem.exportItem.category.eq(category))));
        List<ExportItem> caseList = IterableUtils.toList(exportItemRepository.findAll(QExportItem.exportItem.companyCode.eq(user.getCompanyCode())
                .and(QExportItem.exportItem.statu.eq(0))
                .and(QExportItem.exportItem.type.eq(ExportItem.Type.CASE.getValue()))
                .and(QExportItem.exportItem.category.eq(category))));
        List<ExportItem> bankList = IterableUtils.toList(exportItemRepository.findAll(QExportItem.exportItem.companyCode.eq(user.getCompanyCode())
                .and(QExportItem.exportItem.statu.eq(0))
                .and(QExportItem.exportItem.type.eq(ExportItem.Type.BANK.getValue()))
                .and(QExportItem.exportItem.category.eq(category))));
        List<ExportItem> followList = IterableUtils.toList(exportItemRepository.findAll(QExportItem.exportItem.companyCode.eq(user.getCompanyCode())
                .and(QExportItem.exportItem.statu.eq(0))
                .and(QExportItem.exportItem.type.eq(ExportItem.Type.FOLLOW.getValue()))
                .and(QExportItem.exportItem.category.eq(category))));
        if (exportItemRepository.count(QExportItem.exportItem.companyCode.eq(user.getCompanyCode())
                .and(QExportItem.exportItem.category.eq(category))) == 0) {
            createExportItems(user,category);
        }
        ItemsModel model = new ItemsModel();
        model.setPersonalItems(parseItems(personalList));
        model.setJobItems(parseItems(jobList));
        model.setConnectItems(parseItems(connectList));
        model.setCaseItems(parseItems(caseList));
        model.setBankItems(parseItems(bankList));
        model.setFollowItems(parseItems(followList));
        return model;
    }

}
