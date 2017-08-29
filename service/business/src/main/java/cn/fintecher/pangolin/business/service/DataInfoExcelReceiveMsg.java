package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.entity.DataInfoExcelModel;
import cn.fintecher.pangolin.entity.message.ConfirmDataInfoMessage;
import cn.fintecher.pangolin.entity.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: PeiShouWen
 * @Description: 接收案件确认的消息处理类
 * @Date 15:22 2017/7/24
 */
@Component("dataInfoExcelReceiveMsg")
@RabbitListener(queues = Constants.DATAINFO_CONFIRM_QE)
public class DataInfoExcelReceiveMsg {

    private final Logger logger= LoggerFactory.getLogger(DataInfoExcelReceiveMsg.class);
    @Autowired
    ProcessDataInfoExcelService processDataInfoExcelService;
    //待分配案件MAP KEY :客户名称_身份证号_委托方CODE_公司码
    ConcurrentHashMap<String,DataInfoExcelModel> dataInfoExcelModelMap=new ConcurrentHashMap<>();
    /**
     * 接收案件确认数据(多线程的方式处理数据)
     * @param confirmDataInfoMessage
     */
    @RabbitHandler
    public void receiveMsg(ConfirmDataInfoMessage confirmDataInfoMessage){
        logger.info("收到案件确认消息数量 {}", confirmDataInfoMessage.getDataCount());
        dataInfoExcelModelMap.clear();
        List<DataInfoExcelModel> dataInfoExcelModelList=confirmDataInfoMessage.getDataInfoExcelModelList();
        for(int i=0;i<dataInfoExcelModelList.size();i++){
            processDataInfoExcelService.doTask(dataInfoExcelModelList.get(i),dataInfoExcelModelMap,confirmDataInfoMessage.getUser(),i);
        }
    }

}
