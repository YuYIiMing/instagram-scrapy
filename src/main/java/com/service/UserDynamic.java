package com.service;
//用户动态
public class UserDynamic {
    //缩略图
    private String thumbnail_src;
    //图
    private String display_url;
    //评论数量
    private Integer comment_count;
    //发布时间戳
    private String creat_timestamp;
    //点赞数
    private Integer like_count;
    //是否为视频，如果为视频就不收录吧
    private Boolean isVideo;
    //本人描述
    private String title;

    private String uid;
    //视频地址
    private String video_url;
    //视频长度
    private String video_duration;
    //视频播放量
    private String video_view_count;


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getThumbnail_src() {
        return thumbnail_src;
    }

    public void setThumbnail_src(String thumbnail_src) {
        this.thumbnail_src = thumbnail_src;
    }

    public String getDisplay_url() {
        return display_url;
    }

    public void setDisplay_url(String display_url) {
        this.display_url = display_url;
    }

    public Integer getComment_count() {
        return comment_count;
    }

    public void setComment_count(Integer comment_count) {
        this.comment_count = comment_count;
    }

    public String getCreat_timestamp() {
        return creat_timestamp;
    }

    public void setCreat_timestamp(String creat_timestamp) {
        this.creat_timestamp = creat_timestamp;
    }

    public Integer getLike_count() {
        return like_count;
    }

    public void setLike_count(Integer like_count) {
        this.like_count = like_count;
    }

    public Boolean getVideo() {
        return isVideo;
    }

    public void setVideo(Boolean video) {
        isVideo = video;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getVideo_duration() {
        return video_duration;
    }

    public void setVideo_duration(String video_duration) {
        this.video_duration = video_duration;
    }

    public String getVideo_view_count() {
        return video_view_count;
    }

    public void setVideo_view_count(String video_view_count) {
        this.video_view_count = video_view_count;
    }
}
