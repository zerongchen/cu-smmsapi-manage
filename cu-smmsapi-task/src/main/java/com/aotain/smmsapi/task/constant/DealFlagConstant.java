package com.aotain.smmsapi.task.constant;


public enum DealFlagConstant {

    //（0-未预审、1-预审不通过、2-上报审核中、3-上报审核不通过、4-提交上报、5-上报成功、6-上报失败）
    UN_PRE_VARIFY(0,"未预审"),
    PRE_VARIFY_FAIL(1,"预审不通过"),
    RPT_VARIFY(2,"上报审核中"),
    RPT_VARIFY_FAIL(3,"上报审核不通过"),
    SUB_RPT(4,"提交上报"),
    RPT_SUCCESS(5,"上报成功"),
    RPT_FAIL(6,"上报失败"),;

    private int dealFlag;
    private String dealDesc;


    DealFlagConstant( int dealFlag, String dealDesc ) {
        this.dealDesc=dealDesc;
        this.dealFlag=dealFlag;
    }

    public static String getDealDesc(int dealFlag){
        for (DealFlagConstant dealFlagConstant:DealFlagConstant.values()){
            if (dealFlagConstant.getDealFlag()==dealFlag){
                return dealFlagConstant.getDealDesc();
            }
        }
        return null;
    }

    public int getDealFlag() {
        return dealFlag;
    }

    public void setDealFlag( int dealFlag ) {
        this.dealFlag = dealFlag;
    }

    public String getDealDesc() {
        return dealDesc;
    }

    public void setDealDesc( String dealDesc ) {
        this.dealDesc = dealDesc;
    }

	public enum StatusEnum {

		// 返回处理状态
		WAITING(0, "等待"), 
		SUCCESS(1, "成功"), 
		FAIL(2, "失败");
		StatusEnum(int status, String value) {
			this.status = status;
		}

		private int status;
		private String value;

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

    public enum ChildDealFlagEum{
        UPLOAD(1),
        UN_UPLOAD(0);
        private Integer value;
        ChildDealFlagEum(Integer value){
            this.value=value;
        }

        public Integer getValue() {
            return value;
        }
    }
}
