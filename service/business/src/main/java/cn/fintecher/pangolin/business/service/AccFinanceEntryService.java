package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.OutsourceFollowUpRecordModel;
import cn.fintecher.pangolin.business.repository.AccFinanceEntryRepository;
import cn.fintecher.pangolin.business.repository.CaseFollowupRecordRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
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
    CaseFollowupRecordRepository caseFollowupRecordRepository;
    @Autowired
    CaseInfoRepository caseInfoRepository;

    public List<CellError> importAccFinanceData(String fileUrl, String fileType, int[] startRow, int[] startCol, Class<?>[] dataClass, AccFinanceEntry accFinanceEntry, CaseFollowupRecord outsourceFollowRecord, Integer type) throws Exception {
        List<CellError> errorList = null;
        try {
            //从文件服务器上获取Excel文件并解析：
            ExcelSheetObj excelSheetObj = ExcelUtil.parseExcelSingle(fileUrl, fileType, dataClass, startRow, startCol);

            List dataList = null;
            if(Objects.nonNull(excelSheetObj)){
                dataList = excelSheetObj.getDatasList();
            }else{
                throw new RuntimeException("数据不能为空!");
            }
            //导入错误信息
            errorList = excelSheetObj.getCellErrorList();
            if (errorList.isEmpty()) {
                if (type == 0) {
                    errorList = processFinanceData(dataList, accFinanceEntry, errorList);
                } else {
                    errorList = processFinanceDataFollowup(dataList, outsourceFollowRecord, errorList);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        return errorList;
    }

    private List<CellError> processFinanceData(List datalist, AccFinanceEntry accFinanceEntry, List<CellError> errorList) {

        for (int i =0; i<datalist.size();i++) {
            AccFinanceEntry afe = new AccFinanceEntry();
            AccFinanceDataExcel accFinanceDataExcel = (AccFinanceDataExcel) datalist.get(i);
            afe.setFileId(accFinanceEntry.getFileId());

            if (Objects.nonNull(accFinanceDataExcel.getCaseNum())) {
                afe.setFienCasenum(accFinanceDataExcel.getCaseNum());
            } else {
                errorList.add(new CellError("",  i+1, 1, "", "", "案件编号不存在", null));
            }
            //验证必要数据的合法性
            errorList = validityFinance(errorList, accFinanceDataExcel);
            if(errorList.size()!=0){
                return errorList;
            }
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
            if(Objects.nonNull(accFinanceDataExcel.getCaseNum())) {
                OutsourcePool outsourcePool = outsourcePoolRepository.findOne(qOutsourcePool.caseInfo.caseNumber.eq(accFinanceDataExcel.getCaseNum()));
                if (Objects.nonNull(outsourcePool)) {
                    afe.setFienBatchnum(outsourcePool.getOutBatch());
                    if (Objects.nonNull(outsourcePool.getOutsource())) {
                        afe.setFienFgname(outsourcePool.getOutsource().getOutsName());
                    }
                }else {
                    errorList.add(new CellError("", 0, 0, "", "", "案件编号(".concat(accFinanceDataExcel.getCaseNum()).concat(")非委外案件编号！"), null));
                    return errorList;
                }
            }
            accFinanceEntryRepository.save(afe);
        }
        return errorList;
    }

    private List<CellError> validityFinance(List<CellError> errorList, AccFinanceDataExcel afe) {
        if (ZWStringUtils.isEmpty(afe.getCustName())) {
            CellError cellError = new CellError();
            cellError.setErrorMsg("客户姓名为空");
            errorList.add(cellError);
            return errorList;
        }
        if (StringUtils.isBlank(afe.getIdCardNumber())) {
            CellError cellError = new CellError();
            cellError.setErrorMsg("客户[".concat(afe.getIdCardNumber()).concat("]的身份证号为空"));
            errorList.add(cellError);
            return errorList;
        } else {
            if (!IdcardUtils.validateCard(afe.getIdCardNumber())) {
                CellError cellError = new CellError();
                cellError.setErrorMsg("客户[".concat(afe.getCustName()).concat("]的身份证号[").concat(afe.getIdCardNumber()).concat("]不合法"));
                errorList.add(cellError);
                return errorList;
            }
        }
        return errorList;
    }

    /**
     * Created by huyanmin 2017/9/26
     * 将Excel中的数据存入数据库中
     */
    public List<CellError> processFinanceDataFollowup(List datalist, CaseFollowupRecord outsourceFollowRecord, List<CellError> errorList) {

        List<CaseFollowupRecord> outList = new ArrayList<>();
        for (int m = 0; m < datalist.size(); m++) {
            CaseFollowupRecord out = new CaseFollowupRecord();
            OutsourceFollowUpRecordModel followUpRecordModel = (OutsourceFollowUpRecordModel) datalist.get(m);
            CaseInfo caseInfo = null;
            OutsourcePool outsourcePool = null;
            if (Objects.nonNull(followUpRecordModel.getCaseNum())) {
                out.setCaseNumber(followUpRecordModel.getCaseNum());
                caseInfo = caseInfoRepository.findOne(QCaseInfo.caseInfo.caseNumber.eq(followUpRecordModel.getCaseNum()));
                outsourcePool = outsourcePoolRepository.findOne(QOutsourcePool.outsourcePool.caseInfo.caseNumber.eq(followUpRecordModel.getCaseNum()));
                //案件是否是委外案件
                if (Objects.nonNull(outsourcePool)) {
                    if (Objects.nonNull(caseInfo)) {
                        out.setCaseId(caseInfo.getId());
                    }
                } else {
                    errorList.add(new CellError("", m + 1, 1, "", "", "案件编号(".concat(followUpRecordModel.getCaseNum()).concat(")非委外案件编号！"), null));
                    return errorList;
                }
            } else {
                errorList.add(new CellError("", m + 1, 1, "", "", "案件编号不能为空!", null));
                return errorList;
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
        return errorList;
    }

}

