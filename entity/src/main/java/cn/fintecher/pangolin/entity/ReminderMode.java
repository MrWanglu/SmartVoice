package cn.fintecher.pangolin.entity;


public enum ReminderMode {

    POPUP("弹屏提醒"), COMMON("普通提醒");

    private String cName;

    ReminderMode(String cName) {
        this.cName = cName;
    }

    public String getcName() {
        return cName;
    }

    public void setcName(String cName) {
        this.cName = cName;
    }

}
