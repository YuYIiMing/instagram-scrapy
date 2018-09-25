package com.service;

import java.util.ArrayList;

public class User {
    private String uid;
    private String username;
    private String business_email;
    private String fullName;
    //个人简介
    private String biography;
    //头像缩略图
    private String headPortrait;
    private String headPortraitHD;
    //个人网站
    private String externalUrl;
    //分类标签
    private String categoryLabels;
    //关注的人数
    private Integer followNum;
    //粉丝数
    private Integer fansNum;
    //个人动态
    private ArrayList<UserDynamic> dynamicList;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBusiness_email() {
        return business_email;
    }

    public void setBusiness_email(String business_email) {
        this.business_email = business_email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getHeadPortrait() {
        return headPortrait;
    }

    public void setHeadPortrait(String headPortrait) {
        this.headPortrait = headPortrait;
    }

    public String getHeadPortraitHD() {
        return headPortraitHD;
    }

    public void setHeadPortraitHD(String headPortraitHD) {
        this.headPortraitHD = headPortraitHD;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String getCategoryLabels() {
        return categoryLabels;
    }

    public void setCategoryLabels(String categoryLabels) {
        this.categoryLabels = categoryLabels;
    }

    public Integer getFollowNum() {
        return followNum;
    }

    public void setFollowNum(Integer followNum) {
        this.followNum = followNum;
    }

    public Integer getFansNum() {
        return fansNum;
    }

    public void setFansNum(Integer fansNum) {
        this.fansNum = fansNum;
    }

    public ArrayList<UserDynamic> getDynamicList() {
        return dynamicList;
    }

    public void setDynamicList(ArrayList<UserDynamic> dynamicList) {
        this.dynamicList = dynamicList;
    }
}
