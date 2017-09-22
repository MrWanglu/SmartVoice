//package cn.fintecher.pangolin.dataimp.service;
//
//import cn.fintecher.pangolin.dataimp.entity.CellError;
//import cn.fintecher.pangolin.dataimp.entity.DataInfoExcel;
//import cn.fintecher.pangolin.dataimp.entity.ExcelSheetObj;
//import cn.fintecher.pangolin.dataimp.entity.TemplateExcelInfo;
//import cn.fintecher.pangolin.dataimp.model.AreaResult;
//import cn.fintecher.pangolin.dataimp.repository.AreaReviseDataRepository;
//import cn.fintecher.pangolin.dataimp.util.ExcelUtil;
//import cn.fintecher.pangolin.entity.User;
//import cn.fintecher.pangolin.entity.file.UploadFile;
//import cn.fintecher.pangolin.entity.message.ProgressMessage;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import org.apache.commons.lang.StringUtils;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//import java.util.regex.Pattern;
//
//
///**
// * @Author:liuxiang
// * @Description:区域解析service 层
// * @Date:2017-8-9
// */
//
//@Service("areaReviseDataService")
//public class AreaReviseDataService {
//
//
//    @Value("${cuibei.placeInquire.webUrl}")
//    private  String webUrl;
//    @Value("${cuibei.placeInquire.ak}")
//    private String ak;
//
//    @Autowired
//    AreaReviseDataRepository areaReviseDataRepository;
//    @Autowired
//    RabbitTemplate rabbitTemplate;
//
//    //截取地址,进行二次解析的关键字
//    private static final  String[] STR_ARR = {"街道","镇","民族乡","乡","自治县","县","旗", "区", "市", "自治州", "州"};
//
//    // 解析维度
//    private enum Dim {
//        // 家庭地址
//        HOME_ADDR(0),
//        // 工作地址
//        WORK_ADDR(1),
//        // 家庭和工作地址
//        HOME_WORK_ADDR(2);
//
//        private int val;
//
//        Dim(int val) {
//            this.val = val;
//        }
//    }
//
//
//    //导入的方法
//     public List<CellError> importOriginalData(UploadFile file, int[] startRow, int[] startCol, Class<?>[] dataClass,List<TemplateExcelInfo> templateExcelInfos, User user) throws Exception {
//        List<CellError> cellErrorList = null;
//         try {
//             ExcelSheetObj excelSheetObj = null;
//             try {
//                // 解析Excel
//                excelSheetObj  = ExcelUtil.parseExcelSingle(file, dataClass, startRow, startCol,templateExcelInfos);
//            } catch (final Exception e) {
//                e.printStackTrace();
//                throw e;
//            }
//            if (Objects.nonNull(excelSheetObj)) {
//                // 获取到解析的数据
//                List dataList = excelSheetObj.getDataList();
//                // 解析时产生的错误信息
//                cellErrorList = excelSheetObj.getCellErrorList();
//                // 解析产生错误
//                if (!cellErrorList.isEmpty()) {
//                    return cellErrorList;
//                }
//                // 解析完全正确，将解析得到的数据存放在MongoDB
//                List<DataInfoExcel> alist = new ArrayList<>();
//                for (Object dataObj : dataList) {
//                    DataInfoExcel dataInfoExcel = (DataInfoExcel) dataObj;
//                    //areaData.setUserId(userId);
//                    dataInfoExcel.setOperator(user.getId());
//                    alist.add(dataInfoExcel);
//                }
//                areaReviseDataRepository.save(alist);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw e;
//        }
//        return cellErrorList;
//    }
//
//
//    /**
//     * 一键解析功能Service层
//     * @param all
//     * @param style
//     * @param userId
//     * @return
//     * @throws Exception
//     */
//    public List<DataInfoExcel> reviseStrategic(List<DataInfoExcel> all,Integer style,String userId) throws Exception {
//         //用来存储解析正确的
//        List<DataInfoExcel> list = new ArrayList<DataInfoExcel>();
//         //用来存储不能解析的
//        List<DataInfoExcel> clist = new ArrayList<DataInfoExcel>();
//        //用来存储能解析，但是不需要修改的
//        List<DataInfoExcel> slist = new ArrayList<DataInfoExcel>();
//        DataInfoExcel dataInfoExcel = new DataInfoExcel();
//
//
//         //需要解析的总数
//        int total = all.size();
//        //策略一：以家庭地址解析
//        if(Objects.equals(style,Dim.HOME_ADDR.val)) {
//           for (int i= 0;i < total;i++) {
//                dataInfoExcel= all.get(i);
//               ProgressMessage progressMessage = new ProgressMessage();
//               // 登录人ID
//               progressMessage.setUserId(userId);
//               //要解析的总数据
//               progressMessage.setTotal(total);
//               //当前解析的数据
//               progressMessage.setCurrent(i);
//               //正在处理数据
//               progressMessage.setText("正在处理数据");
//               rabbitTemplate.convertAndSend("mr.cui.data.area.progress", progressMessage);
//               //获取家庭住址
//               String homeAddress = dataInfoExcel.getHomeAddress();
//               //获取省份
//               String province = dataInfoExcel.getProvince();
//               //获取城市
//               String city = dataInfoExcel.getCity();
//               if(Objects.nonNull(homeAddress) && StringUtils.isNotBlank(StringUtils.trim(homeAddress))) {
//                   reviseDataOther(homeAddress, list, clist, slist, dataInfoExcel, province, city);
//
//               }else {
//                   //家庭地址为空不能解析
//                   clist.add(dataInfoExcel);
//               }
//
//           }
//        }
//       //策略二:以工作地址解析
//        if(Objects.equals(style,Dim.WORK_ADDR.val)) {
//            for(int i=0;i < total;i++) {
//                dataInfoExcel = all.get(i);
//
//                ProgressMessage progressMessage = new ProgressMessage();
//                //登录人ID
//                progressMessage.setUserId(userId);
//                //要解析的总数据
//                progressMessage.setTotal(total);
//                //解析当前的数据
//                progressMessage.setCurrent(i);
//                //正在解析数据
//                progressMessage.setText("正在解析数据");
//                rabbitTemplate.convertAndSend("mr.cui.data.area.progress", progressMessage);
//
//                //获取单位地址
//                String workAddress = dataInfoExcel.getCompanyAddr();
//                //获取省份
//                String province = dataInfoExcel.getProvince();
//                //获取城市
//                String city = dataInfoExcel.getCity();
//                if(Objects.nonNull(workAddress) && StringUtils.isNotBlank(StringUtils.trim(workAddress))) {
//                    reviseDataOther(workAddress, list, clist, slist,dataInfoExcel, province, city);
//                }else {
//                    //工作地址为空，不能提供解析
//                    clist.add(dataInfoExcel);
//                }
//
//            }
//        }
//       //策略三:以家庭地址和公司地址联合来解析
//        if(Objects.equals(style,Dim.HOME_WORK_ADDR.val)) {
//            for(int i=0;i <total;i++) {
//                dataInfoExcel = all.get(i);
//
//                ProgressMessage progressMessage = new ProgressMessage();
//                //获取登录人ID
//                progressMessage.setUserId(userId);
//                //获取要解析的总数据
//                progressMessage.setTotal(total);
//                //获取当前解析的数据
//                progressMessage.setCurrent(i);
//                //获取正在解析的数据
//                progressMessage.setText("获取正在解析的数据");
//                rabbitTemplate.convertAndSend("mr.cui.data.area.progress",progressMessage);
//
//                //获取家庭住址
//                String homeAddress = dataInfoExcel.getHomeAddress();
//                //获取单温地址
//                String workAddress  = dataInfoExcel.getCompanyAddr();
//                //获取省份
//                String province = dataInfoExcel.getProvince();
//                //获取城市
//                String city = dataInfoExcel.getCity();
//
//                //根据家庭住址或者单位地址获取到正确的省份
//                //1.当家庭地址存在
//                if(Objects.nonNull(homeAddress) && StringUtils.isNotBlank(StringUtils.trim(homeAddress))) {
//                    AreaResult result = null;
//                    try{
//                        result = getAreaByAddress(homeAddress);
//                    } catch ( final Exception e) {
//                        e.printStackTrace();
//                        throw  e;
//                    }
//                    if(Objects.nonNull(result)){
//                        reviseData(list, slist, dataInfoExcel, result, province, city);
//                    }else if(Objects.nonNull(workAddress) && org.apache.commons.lang3.StringUtils.isNotBlank(org.apache.commons.lang3.StringUtils.trim(workAddress))){
//                        // 根据详细家庭地址解析不出结果，使用工作地址解析
//                        try {
//                            result = getAreaByAddress(workAddress);
//                        }catch (Exception e) {
//                            e.printStackTrace();
//                            throw e;
//                        }
//                        if(Objects.nonNull(result)) {
//                            reviseData(list, slist, dataInfoExcel, result, province, city);
//                        }else {
//                            // 根据工作地址解析不出来，进行二次解析
//                            // 根据截取操作后家庭地址解析
//                            try{
//                                result = getAreaByAddress(getSubAddress(homeAddress));
//                            }catch (final Exception e) {
//                                e.printStackTrace();
//                                throw e;
//                            }
//                            reviseDataOtherSame(workAddress, list, clist, slist, dataInfoExcel, province, city, result);
//                        }
//                    }else {
//                        // 家庭地址存在，查询结果为空，单位地址不存在，截取家庭地址查询
//                        try{
//                            result = getAreaByAddress(getSubAddress(homeAddress));
//                        }catch (final  Exception e) {
//                            e.printStackTrace();
//                            throw e;
//                        }
//                           if(Objects.nonNull(result)) {
//                               reviseData(list, slist, dataInfoExcel, result, province, city);
//                           } else {
//                               clist.add(dataInfoExcel);
//                           }
//                    }
//                    // 家庭地址不存在，单位地址存在
//                }else if (Objects.nonNull(workAddress) && StringUtils.isNotBlank(StringUtils.trim(workAddress))) {
//                    reviseDataOther(workAddress, list, clist, slist, dataInfoExcel, province, city);
//
//
//                }else {
//                    clist.add(dataInfoExcel);
//                }
//           }
//
//        }
//
//        //将解析成功的数据更新
//        if(!list.isEmpty()) {
//            try{
//                areaReviseDataRepository.save(list);
//            }catch (Exception e) {
//                e.printStackTrace();
//                throw e;
//            }
//        }
//        System.out.println("解析成功：" + (list.size() + slist.size() + "条数据"));
//        System.out.println("对比后正确的" + slist.size() + "条数据");
//        System.out.println("不能解析的" + clist.size() + "条数据");
//        return  clist;
//    }
//
//
//
//    /**
//     * 判断使用高德api查询是否得到结果，如果查不到，认为解析失败，将其加入到clist
//     *
//     * @param address
//     * @param list
//     * @param clist
//     * @param slist
//     * @param dataInfoExcel
//     * @param province
//     * @param city
//     * @throws Exception
//     */
//
//    private void reviseDataOther(String address,List<DataInfoExcel> list,List<DataInfoExcel> clist,List<DataInfoExcel> slist,DataInfoExcel dataInfoExcel,String province, String city)throws  Exception {
//
//        AreaResult result = null;  //这个有问题
//        try{
//            result = this.getAreaByAddress(address);
//           // result = areaReviseDataService.getAreaByAddress(address);
//
//        }catch (Exception e) {
//            e.printStackTrace();
//            throw  e;
//
//        }
//        reviseDataOtherSame(address, list, clist, slist, dataInfoExcel, province, city, result);
//
//    }
//
//
//    /**
//     * 截取地址获取新的地址，进行二次解析
//     * @param address
//     * @param list
//     * @param clist
//     * @param slist
//     * @param dataInfoExcel
//     * @param province
//     * @param city
//     * @param result
//     * @throws Exception
//     */
//    private  void  reviseDataOtherSame(String address,List<DataInfoExcel> list,List<DataInfoExcel> clist,List<DataInfoExcel> slist,DataInfoExcel dataInfoExcel,String province,String city,AreaResult result ) throws  Exception{
//
//        if(Objects.nonNull(result)) {
//            reviseData(list, slist, dataInfoExcel, result, province, city);
//
//        }else {
//            try{
//                result = getAreaByAddress(getSubAddress(address));
//            }catch (Exception e) {
//                e.printStackTrace();
//                throw e;
//
//            }
//            if(Objects.nonNull(result)) {
//                reviseData(list, slist, dataInfoExcel, result, province, city);
//            }else{
//                clist.add(dataInfoExcel);
//            }
//        }
//    }
//
//    /**
//     * 对比解析成功的数据和源数据,如果相同将其加入slist,不同将其加入list
//     *
//     * @param list
//     * @param slist
//     * @param dataInfoExcel
//     * @param result
//     * @param province
//     * @param city
//     */
//
//    private void reviseData(List<DataInfoExcel> list,List<DataInfoExcel> slist,DataInfoExcel dataInfoExcel,AreaResult result, String province, String city) {
//
//        if(!Objects.equals(province,result.getProvince()) || !Objects.equals(city, result.getCity())) {
//
//            dataInfoExcel.setProvince(result.getProvince());
//            dataInfoExcel.setCity(result.getCity());
//            list.add(dataInfoExcel);
//        }else {
//            slist.add(dataInfoExcel);
//        }
//
//    }
//
//    /**
//     * 地址截取方法
//     * @param address
//     * @return
//     */
//    private  String getSubAddress(String address) {
//
//        int index = 0;
//        int i;
//        Boolean flag = false;
//        for(i=0;i < STR_ARR.length;i++) {
//            if(address.contains(STR_ARR[i])) {
//               index =address.lastIndexOf(STR_ARR[i]);
//               flag = true;
//               break;
//            }
//        }
//        String subAddress = null;
//        if(flag) {
//            subAddress = address.substring(0,index + STR_ARR[i].length());
//        }
//        return subAddress;
//    }
//
//
//    public  AreaResult getAreaByAddress(String address) throws Exception {
//       // String ak = "5ee22f76f9a1ec809a820b3dbd764c7e";
//        // 调用高德web服务API
//        String url = webUrl.concat("&key=" + ak).concat("&keywords=" + address);
//        HttpHeaders  headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Accept-Charset", "UTF-8");
//        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<String> entity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
//
//        //将返回的数据进行转换，获取到对应的省份和城市
//        AreaResult areaResult = new AreaResult();
//        if(Objects.nonNull(entity)) {
//            String body = entity.getBody();
//            if(body.contains("infocode")) {
//                JSONObject jsonObject = JSON.parseObject(body);
//                String info = jsonObject.getString("info");
//                String infocode = jsonObject.getString("infocode");
//                // 查询的状态 infocode:10000 info:OK 说明正常
//
//                if(StringUtils.isNotBlank(info) && Objects.equals(StringUtils.trim(info),"ok")
//                        && StringUtils.isNotBlank(infocode) && Objects.equals(StringUtils.trim(infocode),"10000")
//                        ) {
//                    //pois中包含的是返回的有用数据
//                    String pois = jsonObject.getString("pois");
//                    //返回的数据是否存在
//                    if(StringUtils.isNotBlank(pois)) {
//                        //返回的数据存放在集合中
//                        if(Pattern.matches("^\\[.*\\]$",pois.trim())) {
//                            JSONArray jsonArray = JSON.parseArray(pois);
//                            if(!jsonArray.isEmpty()) {
//                                //以集合中第一个数据为准
//                                String result0 = jsonArray.get(0).toString();
//                                if(StringUtils.isNotBlank(result0)) {
//                                    JSONObject result0JS = JSONObject.parseObject(result0);
//                                    String pname = result0JS.getString("pname");
//                                    String cityname = result0JS.getString("cityname");
//                                    // 获取省份和城市
//                                    areaResult.setProvince(pname);
//                                    areaResult.setCity(cityname);
//                                }else {
//                                    return null;
//                                }
//
//                            }else {
//                                return null;
//                            }
//
//                        }else {
//                            return  null;
//                        }
//                    }else {
//                        return  null;
//                    }
//                }else {
//                    throw  new RuntimeException("网络异常,请稍后再试");
//                }
//
//            }else {
//                throw  new RuntimeException("网络异常，请稍后再试");
//            }
//        }else {
//            throw new RuntimeException("网络异常，请稍后再试");
//        }
//        return  areaResult;
//
//    }
//
//    public Iterable<DataInfoExcel> batchSave(List<DataInfoExcel> list) throws Exception {
//        try {
//            return areaReviseDataRepository.save(list);
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw e;
//        }
//    }
//
//}
//
//
