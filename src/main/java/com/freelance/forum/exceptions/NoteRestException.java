package com.freelance.forum.exceptions;

public class NoteRestException {
    
    private String status;
    private String msg;
    public NoteRestException(String status, String msg) {
        this.msg = msg;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
