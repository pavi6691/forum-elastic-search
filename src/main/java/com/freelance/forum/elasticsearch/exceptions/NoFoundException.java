package com.freelance.forum.elasticsearch.exceptions;

public class NoFoundException {
    
    private int status;
    private String msg;
    public NoFoundException(int status, String msg) {
        this.msg = msg;
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
