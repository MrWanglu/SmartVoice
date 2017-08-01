package cn.fintecher.pangolin.entity.util;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 13:32 2017/7/17
 */
public final class Constants {
    //系统管理模块
    public static final String ADMINISTRATOR_ID = "0o0oo0o0-0o0o-0000-0000-0ooo000o0o0o";
    public static final String ADMIN_USER_NAME = "administrator";
    public static final String ADMIN_DEPARTMENT_ID = "1c1cc1c1-1c1c-1111-1111-0ccc000c0c0c";
    public static final String ADMIN_DEPARTMENT_CODE = "oooooooo";
    public static final String ADMIN_DEPARTMENT_NAME = "催大人";
    public static final String ADMIN_ROLE_ID = "2r2rr2r2-2r2r-2222-2222-0rrr000r0r0r";
    public static final String ADMIN_ROLE_NAME = "超级管理员";
    public static final String SESSION_USER = "USER";
    public static final String SYS_EXCEPTION_NOSESSION="未登录请重新登录";
    public static final String APPLY_PASSWORD_CODE = "SysParam.applypassword";
    public static final String APPLY_PASSWORD_TYPE = "0001";
    public static final String USER_OVERDAY_CODE = "SysParam.overday";
    public static final String USER_OVERDAY_TYPE = "0002";
    public static final String APPLY_USER_NUMBER_CODE = "SysParam.usernumber";
    public static final String APPLY_USER_NUMBER_TYPE = "0003";
    public static final String USER_RESET_PASSWORD_CODE = "SysParam.resetpassword";
    public static final String USER_RESET_PASSWORD_TYPE = "0004";
    public static final String LOGIN_RET_PASSWORD = "21218cca77804d2ba1922c33e0151105"; //默认密码888888
    public static final String RET_PASSWORD = "21218cca77804d2ba1922c33e0151105"; //默认密码888888
    //呼叫中心模块
    public static final String PHONE_CALL_CODE = "SysParam.phone.call";
    public static final String PHONE_CALL_TYPE = "0005";

    //导入批次号最大999(3位)
    public final static String ORDER_SEQ = "orderSeq";
    //案件编号最大99999（5位）
    public final static String CASE_SEQ = "caseSeq";
    //委托方编号最大999（3位）
    public final static String PRIN_SEQ = "prinSeq";
    public static final String ERROR_MESSAGE="系统错误";
    //日期格式
    public static final String DATE_FORMAT_ONE="yyyyMMdd";
    //Excel数据导入格式
    public static final String EXCEL_TYPE_XLS="xls" ;
    public static final String EXCEL_TYPE_XLSX="xlsx" ;


    //通过文件ID获取文件对象的地址
    public static final String FILEID_SERVICE_URL="http://file-service/api/";
    //通过TOKEN获取用户对象
    public static final String USERTOKEN_SERVICE_URL="http://business-service/api/userResource/getUserByToken?token=";
    //用户服务
    public static final  String USER_SERVICE_URL="http://business-service/api/api/";

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    //案件确认数据发送队列
    public static  final String DATAINFO_CONFIRM_QE="dataInfoExcel.confirm.progress.dev";

    /**
     * 数据来源
     */
    public enum DataSource{
        IMPORT(145,"导入"),PORT(146,"接口"),REPAIR(147,"修复");
        private Integer value;
        private String remark;
        DataSource(Integer value,String remark){
            this.value=value;
            this.remark=remark;
        }

        public Integer getValue() {
            return value;
        }

        public String getRemark() {
            return remark;
        }
    }

}
