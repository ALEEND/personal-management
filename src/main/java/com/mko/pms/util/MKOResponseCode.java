package com.mko.pms.util;

public enum MKOResponseCode {

    Success(0, "成功"),
    BusinessError(1, "业务逻辑错误"),
    UnknownError(2, "未知错误"),
    ParamsLack(3, "缺少参数"),
    MethodNotFound(4, "方法未找到"),
    AuthSessionFail(5, "会话验证失败"),
    AuthSessionExpired(6, "会话失效，超时"),
    NoPermission(7, "没有权限"),
    DataNotFound(8, "找不到数据"),
    DataExist(9, "数据已经存在"),
    DataInsertFailed(10, "数据写入失败"),
    DataUpdateFailed(11, "数据更新失败"),
    DataDeleteFailed(12, "数据删除失败"),
    DataFormatError(13, "数据格式错误");

    private Integer code;
    private String desc;

    private MKOResponseCode(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "[Code]: " + this.code + ", [Desc]: " + this.desc;
    }
}
