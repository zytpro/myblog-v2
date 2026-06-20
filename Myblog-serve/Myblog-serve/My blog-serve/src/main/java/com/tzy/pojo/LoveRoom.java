package com.tzy.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class LoveRoom {
    private String boyName;
    private String girlName;
    private String boyAvatar;
    private String girlAvatar;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date time;

    public String getBoyName() {
        return boyName;
    }

    public void setBoyName(String boyName) {
        this.boyName = boyName;
    }

    public String getGirlName() {
        return girlName;
    }

    public void setGirlName(String girlName) {
        this.girlName = girlName;
    }

    public String getBoyAvatar() {
        return boyAvatar;
    }

    public void setBoyAvatar(String boyAvatar) {
        this.boyAvatar = boyAvatar;
    }

    public String getGirlAvatar() {
        return girlAvatar;
    }

    public void setGirlAvatar(String girlAvatar) {
        this.girlAvatar = girlAvatar;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
