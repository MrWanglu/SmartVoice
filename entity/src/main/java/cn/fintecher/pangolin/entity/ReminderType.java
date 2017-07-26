package cn.fintecher.pangolin.entity;

/**
 * Created by ChenChang on 2017/4/6.
 */
public enum ReminderType {
    REPAYMENT("还款提醒"), FLLOWUP("跟进提醒"), REPAIRED("修复提醒"), DERATE("减免审批提醒"), APPLY("还款审核提醒"), ASSIST_APPROVE("协催审批提醒");
    private String cName;

    ReminderType(String cName) {
        this.cName = cName;
    }

    public String getcName() {
        return cName;
    }

    public void setcName(String cName) {
        this.cName = cName;
    }
}
