package com.instagram;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dao.UserDao;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.service.User;
import com.service.UserDynamic;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhoudanfeng on 18/9/17.
 */
public class InstagramSpider {

    private static BlockingQueue<String> userQueue = new ArrayBlockingQueue<String>(200000);
    private static Set<String> addedUsers = Sets.newConcurrentHashSet();
    private static String queryHashImg;
    private static String queryHashFans;
    private static String queryHashFocus;


    public static void init(String baseUser, String cookie) throws Exception {
        String url = "https://www.instagram.com/" + baseUser;
        Map header = Maps.newHashMap();
        header.put("cookie", cookie);
        header.put("Content-Type", HttpContentType.APPLICATION_FORM_URLENCODED);
        try (CloseableHttpResponse resp = HttpClientUtils.execute(url, null, "GET", header)) {
            String html = EntityUtils.toString(resp.getEntity());
            Document doc = Jsoup.parse(html);
            Elements eles = doc.getElementsByTag("script");
            for (int i = 0; i < eles.size(); i ++) {
                Element e = eles.get(i);
                String jsUrl = "https://www.instagram.com" + e.attr("src");
                if (jsUrl != null && jsUrl.contains("ProfilePageContainer.js")) {
                    try (CloseableHttpResponse ppcResp = HttpClientUtils.execute(jsUrl, null, "GET", header)) {
                        String js = EntityUtils.toString(ppcResp.getEntity());
                        int index = js.indexOf("0===r?void 0:r.pagination},queryId:\"") + 36;
                        queryHashImg = js.substring(index, index + 32);
                    }
                } else if (jsUrl != null && jsUrl.contains("Consumer.js")) {
                    try (CloseableHttpResponse ppcResp = HttpClientUtils.execute(jsUrl, null, "GET", header)) {
                        String js = EntityUtils.toString(ppcResp.getEntity());
                        int index = js.indexOf(",l=\"") + 4;
//                        queryHashFans = js.substring(index, index + 32);
//                        queryHashFocus = js.substring(index + 37, index + 69);
                        queryHashFans = js.substring(index - 37, index - 5);
                        queryHashFocus = js.substring(index, index + 32);
                    }
                }

            }

        }

    }


    public static void main(String[] args) throws Exception {
        addedUsers.add("subramanimsubra927");
        userQueue.add("subramanimsubra927");
        //mirchelley  印度普通人： nadithalahendra  印度明星： therealkareenakapoor   无头像用户： surbhi6882
        final String cookie = "mcd=3; mid=W58dIwAEAAE6h3kPLw71tYpo0Sht; csrftoken=5ah3KVnHeDBy9lv9F7BNwfpPxR5CfEK8; ds_user_id=8594346411; sessionid=IGSCc9288dd18fa81de066f1007547552275ef2601478744c18e920be9dc8056bd23%3ARquUS58mWed88dtSu6rJcbDh8ENk9JeP%3A%7B%22_auth_user_id%22%3A8594346411%2C%22_auth_user_backend%22%3A%22accounts.backends.CaseInsensitiveModelBackend%22%2C%22_auth_user_hash%22%3A%22%22%2C%22_platform%22%3A4%2C%22_token_ver%22%3A2%2C%22_token%22%3A%228594346411%3AaqoMgWYzt8EbFiz8Ox0rmQAdj5hlLIyH%3A2009c017659585e8434c3effea0f6e1f9cce61a2490d4ceeaffc40eff222bc45%22%2C%22last_refreshed%22%3A1537154414.7271106243%7D; rur=PRN; csrftoken=5ah3KVnHeDBy9lv9F7BNwfpPxR5CfEK8; urlgen=\"{\\\"47.88.236.175\\\": 45102}:1g25kk:q1e6eUN1Hv4oNOxXvn63V_a5QmY\"";
        //从js中找出各个queryhash
        init("subramanimsubra927", cookie);

        //不确定instagram是否有多线程、ip限流等，暂时使用单线程
        ExecutorService threadpool = Executors.newFixedThreadPool(1);
        while (true) {
            //移除返回队列顶部元素
            final String user = userQueue.take();
            threadpool.submit(new SpiderTask(user, cookie));
        }

    }

    public static class SpiderTask implements Runnable {
        private String user;
        private String cookie;

        public SpiderTask(String user, String cookie) {
            this.user = user;
            this.cookie = cookie;
        }

        @Override
        public void run() {
            System.out.println("--------------- start user " + user + " ----------------");
            //首屏json，包含用户信息和首屏图片
            String url = "https://www.instagram.com/" + user + "/?__a=1";
            Map header = Maps.newHashMap();
            header.put("cookie", cookie);
            header.put("Content-Type", HttpContentType.APPLICATION_FORM_URLENCODED);
            //instagram被墙，httpclient写死使用了本机shadowsocks代理
            try (CloseableHttpResponse resp = HttpClientUtils.execute(url, null, "GET", header)) {
                JSONObject entryData = JSON.parseObject(EntityUtils.toString(resp.getEntity()));
                JSONObject userObject = entryData.getJSONObject("graphql").getJSONObject("user");

                User user = new User();
                //uid
                String userId = userObject.getString("id");
                user.setUid(userId);
                //基本信息
                String username = userObject.getString("username");
                user.setUsername(username);
                System.out.println("用户名："+username);
                String business_email = userObject.getString("business_email");
                user.setBusiness_email(business_email);
                System.out.println("邮箱："+business_email);

                String fullName = userObject.getString("full_name");
                user.setFullName(fullName);
                System.out.println("全名："+fullName);

                //简介
                String biography = userObject.getString("biography");
                user.setBiography(biography);
                System.out.println("简介："+biography);

                //头像
                String headPortrait = userObject.getString("profile_pic_url");
                user.setHeadPortrait(headPortrait);
                System.out.println("头像缩略图："+headPortrait);

                String headPortraitHD = userObject.getString("profile_pic_url_hd");
                user.setHeadPortraitHD(headPortraitHD);
                System.out.println("头像："+headPortraitHD);

                //个人网站
                String externalUrl = userObject.getString("external_url");
                user.setExternalUrl(externalUrl);
                System.out.println("个人网站："+externalUrl);

                //分类标签
                String categoryLabels = userObject.getString("business_category_name");
                user.setCategoryLabels(categoryLabels);
                System.out.println("分类标签："+categoryLabels);

                //关注的人数
                Integer followNum = userObject.getJSONObject("edge_follow").getInteger("count");
                user.setFollowNum(followNum);
                System.out.println("关注的人数："+followNum);

                //粉丝数
                Integer fansNum = userObject.getJSONObject("edge_followed_by").getInteger("count");
                user.setFansNum(fansNum);
                System.out.println("粉丝人数："+fansNum);

                //精彩场面计数（不知道是啥）
                Integer highlight_reel_count = userObject.getInteger("highlight_reel_count");

                //插入user表
                UserDao.insertUser(user);

                //帖子
                JSONObject timeLines = userObject.getJSONObject("edge_owner_to_timeline_media");
                boolean next = timeLines.getJSONObject("page_info").getBooleanValue("has_next_page");
                String after = timeLines.getJSONObject("page_info").getString("end_cursor");

                //帖子计数器，防止帖子太多，为了效率只爬取部分。
                long num = 0;

                for (int i = 0; i < timeLines.getJSONArray("edges").size(); i ++) {
                    JSONObject edge = timeLines.getJSONArray("edges").getJSONObject(i).getJSONObject("node");
                     UserDynamic userDynamic = new UserDynamic();
                     userDynamic.setUid(user.getUid());
                    //帖子图片
                    System.out.println("img:" + edge.getString("thumbnail_src"));
                    userDynamic.setThumbnail_src(edge.getString("thumbnail_src"));
                    //display_url
                    String display_url = edge.getString("display_url");
                    userDynamic.setDisplay_url(display_url);
                    //评论数量
                    if(edge.getJSONObject("edge_media_to_comment") != null){
                        Integer comment_count = edge.getJSONObject("edge_media_to_comment").getInteger("count");
                        userDynamic.setComment_count(comment_count);
                    }

                    //发布时间戳
                    String creat_timestamp = edge.getString("taken_at_timestamp");
                    userDynamic.setCreat_timestamp(creat_timestamp);
                    System.out.println("发布时间戳："+creat_timestamp);

                    //点赞数
                    if(edge.getJSONObject("edge_liked_by") != null){
                        Integer like_count = edge.getJSONObject("edge_liked_by").getInteger("count");
                        userDynamic.setLike_count(like_count);
                        System.out.println("点赞数："+like_count);
                    }

                    //是否为视频，如果数视频就不收录吧
                    Boolean isVideo = edge.getBoolean("is_video");
                    userDynamic.setVideo(isVideo);
                    //帖子标题
                    if(edge.getJSONObject("edge_media_to_caption").getJSONArray("edges").size() != 0){
                        String title = edge.getJSONObject("edge_media_to_caption").getJSONArray("edges").getJSONObject(0).getJSONObject("node").getString("text");
                        System.out.println("本人描述："+title);
                        userDynamic.setTitle(title);
                    }
                    //media_preview 媒体预览
                    String media_preview = edge.getString("media_preview");
                    num ++;
                    if(userDynamic.getVideo() == false){
                        UserDao.insertUserDynamic(userDynamic);
                    }else{
                        //爬取视频
                        String shortcode = edge.getString("shortcode");
                       //视频和评论都在这个网址上
                        String vurl = "https://www.instagram.com/p/" + shortcode + "/?__a=1";
//                        Map header = Maps.newHashMap();
//                        header.put("cookie", cookie);
//                        header.put("Content-Type", HttpContentType.APPLICATION_FORM_URLENCODED);
                        try (CloseableHttpResponse resp2 = HttpClientUtils.execute(vurl, null, "GET", header)) {
                            JSONObject entryData2 = JSON.parseObject(EntityUtils.toString(resp2.getEntity()));
                            JSONObject shortMedia = entryData2.getJSONObject("graphql").getJSONObject("shortcode_media");
                            //视频地址
                            String video_url =shortMedia.getString("video_url");
                            userDynamic.setVideo_url(video_url);
                            //视频长度
                            String video_duration =shortMedia.getString("video_duration");
                            userDynamic.setVideo_duration(video_duration);
                            //视频播放量
                            String video_view_count =shortMedia.getString("video_view_count");
                            userDynamic.setVideo_view_count(video_view_count);
                            UserDao.insertUserDynamic(userDynamic);
                        } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                        //
                    }
                }
                while (next) {
                    Map variables = Maps.newHashMap();
                    variables.put("id", userId);
                    variables.put("first", 12);
                    variables.put("after", after);

                    try (CloseableHttpResponse r = HttpClientUtils.execute("https://www.instagram.com/graphql/query/?query_hash=" + queryHashImg + "&variables=" + URLEncoder.encode(JSON.toJSONString(variables), "utf-8"), null, "GET", header)) {
                        JSONObject data = JSON.parseObject(EntityUtils.toString(r.getEntity())).getJSONObject("data");
                        userObject = data.getJSONObject("user");
                        timeLines = userObject.getJSONObject("edge_owner_to_timeline_media");
                        next = timeLines.getJSONObject("page_info").getBooleanValue("has_next_page");
                        after = timeLines.getJSONObject("page_info").getString("end_cursor");
                        for (int i = 0; i < timeLines.getJSONArray("edges").size(); i ++) {
                            JSONObject edge = timeLines.getJSONArray("edges").getJSONObject(i).getJSONObject("node");
                            UserDynamic userDynamic = new UserDynamic();
                            userDynamic.setUid(user.getUid());
                            //帖子图片
                            System.out.println("img:" + edge.getString("thumbnail_src"));
                            userDynamic.setThumbnail_src(edge.getString("thumbnail_src"));
                            //display_url
                            String display_url = edge.getString("display_url");
                            userDynamic.setDisplay_url(display_url);
                            //评论数量
                            if(edge.getJSONObject("edge_media_to_comment") != null){
                                Integer comment_count = edge.getJSONObject("edge_media_to_comment").getInteger("count");
                                userDynamic.setComment_count(comment_count);
                            }
                            //发布时间戳
                            String creat_timestamp = edge.getString("taken_at_timestamp");
                            userDynamic.setCreat_timestamp(creat_timestamp);
                            System.out.println("发布时间戳："+creat_timestamp);

                            //点赞数
                            if(edge.getJSONObject("edge_liked_by") != null){
                                Integer like_count = edge.getJSONObject("edge_liked_by").getInteger("count");
                                userDynamic.setLike_count(like_count);
                                System.out.println("点赞数："+like_count);
                            }
                            //是否为视频
                            Boolean isVideo = edge.getBoolean("is_video");
                            userDynamic.setVideo(isVideo);
                            //帖子标题
                            if(edge.getJSONObject("edge_media_to_caption").getJSONArray("edges").size() != 0){
                                String title = edge.getJSONObject("edge_media_to_caption").getJSONArray("edges").getJSONObject(0).getJSONObject("node").getString("text");
                                System.out.println("本人描述："+title);
                                userDynamic.setTitle(title);
                            }
                            //media_preview 媒体预览
                            String media_preview = edge.getString("media_preview");
                            num ++;
                            if(userDynamic.getVideo() == false){
                                //暂时不爬视频
                                UserDao.insertUserDynamic(userDynamic);
                            }
                            else{
                                //爬取视频
                                String shortcode = edge.getString("shortcode");
                                //视频和评论都在这个网址上
                                String vurl = "https://www.instagram.com/p/" + shortcode + "/?__a=1";
                                try (CloseableHttpResponse resp2 = HttpClientUtils.execute(vurl, null, "GET", header)) {
                                    JSONObject entryData2 = JSON.parseObject(EntityUtils.toString(resp2.getEntity()));
                                    JSONObject shortMedia = entryData2.getJSONObject("graphql").getJSONObject("shortcode_media");
                                    //视频地址
                                    String video_url =shortMedia.getString("video_url");
                                    userDynamic.setVideo_url(video_url);
                                    //视频长度
                                    String video_duration =shortMedia.getString("video_duration");
                                    userDynamic.setVideo_duration(video_duration);
                                    //视频播放量
                                    String video_view_count =shortMedia.getString("video_view_count");
                                    userDynamic.setVideo_view_count(video_view_count);
                                    UserDao.insertUserDynamic(userDynamic);
                            }
                        }
                            num ++;

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                    //限制一下爬取帖子数
                    if(num > 20){
                        break;
                    }
                }

                Map fansParam = Maps.newHashMap();
                fansParam.put("id", userId);
                fansParam.put("include_reel", false);
                fansParam.put("fetch_mutual", true);
                fansParam.put("first", 24);

                //粉丝计数器
                long fansnum = 0;
                try (CloseableHttpResponse r = HttpClientUtils.execute("https://www.instagram.com/graphql/query/?query_hash=" + queryHashFans + "&variables=" + URLEncoder.encode(JSON.toJSONString(fansParam), "utf-8"), null, "GET", header)) {
                    JSONObject data = JSON.parseObject(EntityUtils.toString(r.getEntity())).getJSONObject("data");
                    userObject = data.getJSONObject("user");
                    JSONObject follows = userObject.getJSONObject("edge_followed_by");
                    if(follows != null){

                    next = follows.getJSONObject("page_info").getBooleanValue("has_next_page");
                    after = follows.getJSONObject("page_info").getString("end_cursor");
                    for (int i = 0; i < follows.getJSONArray("edges").size(); i ++) {
                        JSONObject edge = follows.getJSONArray("edges").getJSONObject(i).getJSONObject("node");
                        String userName = edge.getString("username");
                        if (!addedUsers.contains(userName)) {
                            System.out.println("add user " + userName);
                            addedUsers.add(userName);
                            userQueue.add(userName);
                            fansnum ++;
                        }
                    }
                    while (next) {
                        fansParam.put("fetch_mutual", false);
                        fansParam.put("first", 12);
                        fansParam.put("after", after);
                        try (CloseableHttpResponse f = HttpClientUtils.execute("https://www.instagram.com/graphql/query/?query_hash=" + queryHashFans + "&variables=" + URLEncoder.encode(JSON.toJSONString(fansParam), "utf-8"), null, "GET", header)) {
                            data = JSON.parseObject(EntityUtils.toString(f.getEntity())).getJSONObject("data");
                            userObject = data.getJSONObject("user");
                            follows = userObject.getJSONObject("edge_followed_by");
                            next = follows.getJSONObject("page_info").getBooleanValue("has_next_page");
                            after = follows.getJSONObject("page_info").getString("end_cursor");
                            for (int i = 0; i < follows.getJSONArray("edges").size(); i ++) {
                                JSONObject edge = follows.getJSONArray("edges").getJSONObject(i).getJSONObject("node");
                                String userName = edge.getString("username");
                                if (!addedUsers.contains(userName)) {
                                    System.out.println("add user " + userName);
                                    addedUsers.add(userName);
                                    userQueue.add(userName);
                                    fansnum ++;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }

                        //限制一下爬粉丝数
                        if(fansnum > 1000){
                            break;
                        }
                        //如果队列里数据太多，就先不加了
                        if(userQueue.size()>1000){
                            break;
                        }

                    }

                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }


                Map focusParam = Maps.newHashMap();
                focusParam.put("id", userId);
                focusParam.put("include_reel", false);
                focusParam.put("fetch_mutual", true);
                focusParam.put("first", 24);
                long focusnum = 0;
                try (CloseableHttpResponse r = HttpClientUtils.execute("https://www.instagram.com/graphql/query/?query_hash=" + queryHashFocus + "&variables=" + URLEncoder.encode(JSON.toJSONString(focusParam), "utf-8"), null, "GET", header)) {
                    JSONObject data = JSON.parseObject(EntityUtils.toString(r.getEntity())).getJSONObject("data");
                    userObject = data.getJSONObject("user");
                    JSONObject follows = userObject.getJSONObject("edge_follow");
                    next = follows.getJSONObject("page_info").getBooleanValue("has_next_page");
                    after = follows.getJSONObject("page_info").getString("end_cursor");
                    for (int i = 0; i < follows.getJSONArray("edges").size(); i ++) {
                        JSONObject edge = follows.getJSONArray("edges").getJSONObject(i).getJSONObject("node");
                        String userName = edge.getString("username");
                        if (!addedUsers.contains(userName)) {
                            System.out.println("add user " + userName);
                            addedUsers.add(userName);
                            userQueue.add(userName);
                            focusnum ++;
                        }

                    }
                    while (next) {
                        focusParam.put("fetch_mutual", false);
                        focusParam.put("first", 12);
                        focusParam.put("after", after);
                        try (CloseableHttpResponse f = HttpClientUtils.execute("https://www.instagram.com/graphql/query/?query_hash=" + queryHashFocus + "&variables=" + URLEncoder.encode(JSON.toJSONString(focusParam), "utf-8"), null, "GET", header)) {
                            data = JSON.parseObject(EntityUtils.toString(f.getEntity())).getJSONObject("data");
                            userObject = data.getJSONObject("user");
                            follows = userObject.getJSONObject("edge_follow");
                            next = follows.getJSONObject("page_info").getBooleanValue("has_next_page");
                            after = follows.getJSONObject("page_info").getString("end_cursor");
                            for (int i = 0; i < follows.getJSONArray("edges").size(); i ++) {
                                JSONObject edge = follows.getJSONArray("edges").getJSONObject(i).getJSONObject("node");
                                String userName = edge.getString("username");
                                if (!addedUsers.contains(userName)) {
                                    System.out.println("add user " + userName);
                                    addedUsers.add(userName);
                                    userQueue.add(userName);
                                    focusnum ++;
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }
                        //限制一下爬取的关注用户数量
                        if(focusnum > 30){
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
