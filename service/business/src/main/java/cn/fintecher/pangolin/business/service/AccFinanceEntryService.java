package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.OutsourceFollowUpRecordModel;
import cn.fintecher.pangolin.business.repository.AccFinanceEntryRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.OutsourceFollowupRecordRepository;
import cn.fintecher.pangolin.business.repository.OutsourcePoolRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.*;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.util.ZWStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;


/**
 * Created by LQ on 2017/6/5.
 */
@Service("accFinanceEntryService")
public class AccFinanceEntryService {
    private final Logger logger = LoggerFactory.getLogger(AccFinanceEntryService.class);
    @Autowired
    AccFinanceEntryRepository accFinanceEntryRepository;
    @Autowired
    OutsourcePoolRepository outsourcePoolRepository;
    @Autowired
    OutsourceFollowupRecordRepository outsourceFollowupRecordRepository;
    @Autowired
    CaseInfoRepository caseInfoRepository;

    public List<CellError> importAccFinanceData(String fileUrl, int[] startRow, int[] startCol, Class<?>[] dataClass, AccFinanceEntry accFinanceEntry, OutsourceFollowRecord outsourceFollowRecord, Integer type) throws Exception {
        List<CellError> errorList = null;
        try {
            //从文件服务器上获取Excel文件并解析：
            ExcelSheetObj excelSheetObj = ExcelUtil.parseExcelSingle(fileUrl, dataClass, startRow, startCol);
            List dataList = excelSheetObj.getDatasList();
            //导入错误信息
            errorList = excelSheetObj.getCellErrorList();
            if (errorList.isEmpty()) {
                if (type == 0) {
                    processFinanceData(dataList, accFinanceEntry, errorList);
                } else {
                    processFinanceDataFollowup(dataList, outsourceFollowRecord, errorList);
                }

            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            return errorList;
        }
        return errorList;
    }

    private void processFinanceData(List datalist, AccFinanceEntry accFinanceEntry, List<CellError> errorList) {
        for (Object obj : datalist) {
            AccFinanceEntry afe = new AccFinanceEntry();
            AccFinanceDataExcel accFinanceDataExcel = (AccFinanceDataExcel) obj;

            afe.setFileId(accFinanceEntry.getFileId());
            afe.setFienCasenum(accFinanceDataExcel.getCaseNum());
            afe.setFienCustname(accFinanceDataExcel.getCustName());
            afe.setFienIdcard(accFinanceDataExcel.getIdCardNumber());
            afe.setFienCount(BigDecimal.valueOf(accFinanceDataExcel.getCaseAmount()));
            afe.setFienPayback(BigDecimal.valueOf(accFinanceDataExcel.getPayAmount()));
            afe.setFienStatus(Status.Enable.getValue());
            afe.setFienRemark(accFinanceEntry.getFienRemark());
            afe.setCreator(accFinanceEntry.getCreator());
            afe.setCreateTime(accFinanceEntry.getCreateTime());
            afe.setCompanyCode(accFinanceEntry.getCompanyCode());

            QOutsourcePool qOutsourcePool = QOutsourcePool.outsourcePool;

            Iterable<OutsourcePool> outsourcePools = outsourcePoolRepository.findAll(qOutsourcePool.caseInfo.caseNumber.eq(accFinanceDataExcel.getCaseNum()));
            if (Objects.nonNull(outsourcePools) && outsourcePools.iterator().hasNext()) {
                afe.setFienBatchnum(outsourcePools.iterator().next().getOutBatch());
                afe.setFienFgname(outsourcePools.iterator().next().getOutsource().getOutsName());
            }
            //验证必要数据的合法性
            if (!validityFinance(errorList, afe)) {
                return;
            }
            accFinanceEntryRepository.save(afe);
        }
    }

    private Boolean validityFinance(List<CellError> errorList, AccFinanceEntry afe) {
        if (ZWStringUtils.isEmpty(afe.getFienCustname())) {
            CellError cellError = new CellError();
            cellError.setErrorMsg("客户姓名为空");
            errorList.add(cellError);
            return false;
        }
        if (ZWStringUtils.isEmpty(afe.getFienCasenum())) {
            CellError cellError = new CellError();
            cellError.setErrorMsg("客户[".concat(afe.getFienCustname()).concat("]的案件编号为空"));
            errorList.add(cellError);
            return false;
        }
        if (StringUtils.isBlank(afe.getFienIdcard())) {
            CellError cellError = new CellError();
            cellError.setErrorMsg("客户[".concat(afe.getFienCustname()).concat("]的身份证号为空"));
            errorList.add(cellError);
            return false;
        } else {
            if (!IdcardUtils.validateCard(afe.getFienIdcard())) {
                CellError cellError = new CellError();
                cellError.setErrorMsg("客户[".concat(afe.getFienCustname()).concat("]的身份证号[").concat(afe.getFienIdcard()).concat("]不合法"));
                errorList.add(cellError);
                return false;
            }
        }
        return true;
    }

    /**
     * Created by huyanmin 2017/9/26
     * 将Excel中的数据存入数据库中
     */
    public void processFinanceDataFollowup(List datalist, OutsourceFollowRecord outsourceFollowRecord, List<CellError> errorList) {

        for (Object obj : datalist) {
            OutsourceFollowRecord out = new OutsourceFollowRecord();
            OutsourceFollowUpRecordModel followUpRecordModel = (OutsourceFollowUpRecordModel) obj;

            CaseInfo caseInfo = null;
            if (Objects.nonNull(followUpRecordModel.getCaseNum())) {
                out.setCaseNum(followUpRecordModel.getCaseNum());
                caseInfo = caseInfoRepository.findOne(QCaseInfo.caseInfo.caseNumber.eq(followUpRecordModel.getCaseNum()));
                if (Objects.nonNull(caseInfo)) {
                    out.setCaseInfo(caseInfo);
                }
            }
            OutsourceFollowRecord.FollowType[] followTypes = OutsourceFollowRecord.FollowType.values();//跟进形势
            Integer followtype = 0;
            for(int i=0; i<followTypes.length;i++){
                if(Objects.nonNull(followUpRecordModel.getFollowType())){
                    if(followTypes[i].getRemark().equals(followUpRecordModel.getFollowType())){
                        followtype = followTypes[i].getValue();
                    }
                }

            }
            out.setFollowType(followtype);
            if(Objects.nonNull(outsourceFollowRecord.getCompanyCode())){
                out.setCompanyCode(outsourceFollowRecord.getCompanyCode());
            }
            out.setFollowTime(followUpRecordModel.getFollowTime());
            out.setFollowPerson(followUpRecordModel.getFollowPerson());
            out.setUserName(followUpRecordModel.getUserName());
            OutsourceFollowRecord.ObjectName[] objectNames = OutsourceFollowRecord.ObjectName.values();//跟进对象
            Integer objectName = 0;
            for(int i=0; i<objectNames.length;i++){
                if(Objects.nonNull(followUpRecordModel.getObjectName())){
                    if(objectNames[i].getRemark().equals(followUpRecordModel.getObjectName())){
                        objectName = objectNames[i].getValue();
                    }
                }

            }
            out.setObjectName(objectName);
            OutsourceFollowRecord.FeedBack[] feedBacks = OutsourceFollowRecord.FeedBack.values();//跟进对象
            Integer feedBack = 0;
            for(int i=0; i<feedBacks.length;i++){
                if(Objects.nonNull(followUpRecordModel.getFeedback())){
                    if(feedBacks[i].getRemark().equals(followUpRecordModel.getFeedback())){
                        feedBack = feedBacks[i].getValue();
                    }
                }

            }
            out.setFeedback(feedBack);
            out.setFollowRecord(followUpRecordModel.getFollowRecord());
            OutsourceFollowRecord.TelStatus[] telStatuslist = OutsourceFollowRecord.TelStatus.values();//电话状态
            Integer telStatus = 0;
            for(int i=0; i<telStatuslist.length;i++){
                if(Objects.nonNull(followUpRecordModel.getTelStatus())){
                    if(telStatuslist[i].getRemark().equals(followUpRecordModel.getTelStatus())){
                        telStatus = telStatuslist[i].getValue();
                    }
                }
            }
            out.setTelStatus(telStatus);
            out.setOperatorName(outsourceFollowRecord.getOperatorName());
            out.setOperatorTime(ZWDateUtil.getNowDateTime());

            //验证必要数据的合法性
            if (!validityFinanceFollowup(errorList, out)) {
                return;
            }
            outsourceFollowupRecordRepository.save(out);

        }
    }

    /**
     * Created by huyanmin 2017/9/26
     * 验证案件号是否为空
     */
    private Boolean validityFinanceFollowup(List<CellError> errorList, OutsourceFollowRecord out) {

        if (ZWStringUtils.isEmpty(out.getCaseNum())) {
            CellError cellError = new CellError();
            cellError.setErrorMsg("客户[".concat(out.getCaseNum()).concat("]的案件编号为空"));
            errorList.add(cellError);
            return false;
        }
        return true;
    }


}
