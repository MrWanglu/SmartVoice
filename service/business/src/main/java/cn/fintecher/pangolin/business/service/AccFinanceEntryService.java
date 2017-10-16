package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.OutsourceFollowUpRecordModel;
import cn.fintecher.pangolin.business.repository.*;
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
import java.util.ArrayList;
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
    CaseFollowupRecordRepository caseFollowupRecordRepository;
    @Autowired
    CaseInfoRepository caseInfoRepository;

    public List<CellError> importAccFinanceData(String fileUrl,String fileType, int[] startRow, int[] startCol, Class<?>[] dataClass, AccFinanceEntry accFinanceEntry, CaseFollowupRecord outsourceFollowRecord, Integer type) throws Exception {
        List<CellError> errorList = null;
        try {
            //从文件服务器上获取Excel文件并解析：
            ExcelSheetObj excelSheetObj = ExcelUtil.parseExcelSingle(fileUrl,fileType, dataClass, startRow, startCol);
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
            throw new RuntimeException(e.getMessage());
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
    public void processFinanceDataFollowup(List datalist, CaseFollowupRecord outsourceFollowRecord, List<CellError> errorList) {

        List<CaseFollowupRecord> outList = new ArrayList<>();
        for (int m = 0; m < datalist.size(); m++) {
            CaseFollowupRecord out = new CaseFollowupRecord();
            OutsourceFollowUpRecordModel followUpRecordModel = (OutsourceFollowUpRecordModel) datalist.get(m);
            CaseInfo caseInfo = null;
            if (Objects.nonNull(followUpRecordModel.getCaseNum())) {
                out.setCaseNumber(followUpRecordModel.getCaseNum());
                caseInfo = caseInfoRepository.findOne(QCaseInfo.caseInfo.caseNumber.eq(followUpRecordModel.getCaseNum()));
                //案件编号是否存在
                if (Objects.nonNull(caseInfo)) {
                    out.setCaseId(caseInfo.getId());
                } else {
                    errorList.add(new CellError("", m + 1, 1, "", "", "客户[".concat(out.getCaseNumber()).concat("]的案件编号不存在"), null));
                }
            } else {
                errorList.add(new CellError("", m + 1, 1, "", "", "案件编号不存在", null));
            }
            CaseFollowupRecord.Type[] followTypes = CaseFollowupRecord.Type.values();//跟进方式
            Integer followtype = 0;
            for (int i = 0; i < followTypes.length; i++) {
                if (Objects.nonNull(followUpRecordModel.getFollowType())) {
                    if (followTypes[i].getRemark().equals(followUpRecordModel.getFollowType())) {
                        followtype = followTypes[i].getValue();
                    }
                }
            }
            out.setType(followtype);
            if (Objects.nonNull(outsourceFollowRecord.getCompanyCode())) {
                out.setCompanyCode(outsourceFollowRecord.getCompanyCode());
            }
            out.setFollowTime(followUpRecordModel.getFollowTime());
            out.setFollowPerson(followUpRecordModel.getFollowPerson());
            out.setTargetName(followUpRecordModel.getUserName());
            CaseFollowupRecord.Target[] objectNames = CaseFollowupRecord.Target.values();//跟进对象
            Integer objectName = 0;
            for (int i = 0; i < objectNames.length; i++) {
                if (Objects.nonNull(followUpRecordModel.getObjectName())) {
                    if (objectNames[i].getRemark().equals(followUpRecordModel.getObjectName())) {
                        objectName = objectNames[i].getValue();
                    }
                }
            }
            out.setTarget(objectName);
            CaseFollowupRecord.EffectiveCollection[] feedBacks = CaseFollowupRecord.EffectiveCollection.values();//有效催收反馈
            Integer feedBack = 0;
            for (int i = 0; i < feedBacks.length; i++) {
                if (Objects.nonNull(followUpRecordModel.getFeedback())) {
                    if (feedBacks[i].getRemark().equals(followUpRecordModel.getFeedback())) {
                        feedBack = feedBacks[i].getValue();
                    }
                }
            }
            CaseFollowupRecord.InvalidCollection[] InvalidFeedBacks = CaseFollowupRecord.InvalidCollection.values();//无效催收反馈
            for (int i = 0; i < InvalidFeedBacks.length; i++) {
                if (Objects.nonNull(followUpRecordModel.getFeedback())) {
                    if (InvalidFeedBacks[i].getRemark().equals(followUpRecordModel.getFeedback())) {
                        feedBack = InvalidFeedBacks[i].getValue();
                    }
                }
            }
            out.setCollectionFeedback(feedBack);
            out.setContent(followUpRecordModel.getFollowRecord());
            CaseFollowupRecord.ContactState[] telStatusList = CaseFollowupRecord.ContactState.values();//电话状态
            Integer telStatus = 0;
            for (int i = 0; i < telStatusList.length; i++) {
                if (Objects.nonNull(followUpRecordModel.getTelStatus())) {
                    if (telStatusList[i].getRemark().equals(followUpRecordModel.getTelStatus())) {
                        telStatus = telStatusList[i].getValue();
                    }
                }
            }
            out.setContactState(telStatus);
            out.setOperatorName(outsourceFollowRecord.getOperatorName());
            out.setOperator(outsourceFollowRecord.getOperator());
            out.setOperatorTime(ZWDateUtil.getNowDateTime());
            out.setCaseFollowupType(CaseFollowupRecord.CaseFollowupType.OUTER.getValue());
            outList.add(out);
        }
        caseFollowupRecordRepository.save(outList);
    }

}
