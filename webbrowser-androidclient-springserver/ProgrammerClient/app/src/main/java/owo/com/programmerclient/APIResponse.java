package owo.com.programmerclient;

public class APIResponse<T> {
    private int code = 0;
    private String msg;
    private T data;

    public int getCode() {
        return code;
    }

    public APIResponse<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public APIResponse<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public T getData() {
        return data;
    }

    public APIResponse<T> setData(T data) {
        this.data = data;
        return this;
    }
}
