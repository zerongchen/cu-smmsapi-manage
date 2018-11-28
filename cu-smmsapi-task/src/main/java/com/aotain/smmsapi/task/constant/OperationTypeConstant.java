package com.aotain.smmsapi.task.constant;


public enum OperationTypeConstant {

    //（1-新增，2=变更，3=删除）
    ADD(1,"新增"),
    MODIFY(2,"变更"),
    DELETE(3,"删除")
   ;

    private int action;
    private String actionDesc;


    OperationTypeConstant( int action, String actionDesc ) {
        this.action=action;
        this.actionDesc=actionDesc;
    }

    public int getAction() {
        return action;
    }

    public void setAction( int action ) {
        this.action = action;
    }

    public String getActionDesc() {
        return actionDesc;
    }

    public void setActionDesc( String actionDesc ) {
        this.actionDesc = actionDesc;
    }
}
