package com.github.catvod.spider;

import android.content.Context;
import com.github.catvod.crawler.Spider;
import com.github.catvod.utils.okhttp.OKCallBack;
import com.github.catvod.utils.okhttp.OkHttpUtil;
import okhttp3.Call;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Gitcafe extends Spider {
    private List<String> JsonClass;
    private PushAgent pushAgent;
    private JSONObject NewClass;
    private JSONObject Home;


    @Override
    public void init(Context context, String extend) {
        super.init(context, extend);
        pushAgent = new PushAgent();
        pushAgent.init(context, extend);
        try {
            NewClass = new JSONObject("{\"hyds\":\"华语电视\",\"rhds\":\"日韩电视\",\"omds\":\"欧美电视\",\"qtds\":\"其他电视\",\"hydy\":\"华语电影\",\"rhdy\":\"日韩电影\",\"omdy\":\"欧美电影\",\"qtdy\":\"其他电影\",\"hydm\":\"华语动漫\",\"rhdm\":\"日韩动漫\",\"omdm\":\"欧美动漫\",\"jlp\":\"纪录片\",\"zyp\":\"综艺片\",\"jypx\":\"教育培训\",\"qtsp\":\"其他视频\",\"hyyy\":\"华语音乐\",\"rhyy\":\"日韩音乐\",\"omyy\":\"欧美音乐\",\"qtyy\":\"其他音乐\"}");
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList title = new ArrayList();
        Iterator<String> keys = NewClass.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            title.add(key);
        }
        JsonClass = title;
    }

    protected HashMap<String, String> Headers() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.54 Safari/537.36");
        return hashMap;
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        try {
            String url = "https://gitcafe.net/alipaper/data/" + tid + ".json";
            String content = OkHttpUtil.string(url, Headers());
            JSONArray Obj = new JSONArray(content);
            JSONArray videoList = new JSONArray();
            for (int i = 0; i < Obj.length(); i++) {
                JSONObject jSONObject = Obj.getJSONObject(i);
                JSONObject v = new JSONObject();
                String Id = "https://www.aliyundrive.com/s/" + jSONObject.getString("key");
                String VodName = jSONObject.getString("title");
                v.put("vod_id", Id);
                v.put("vod_name", VodName);
                v.put("vod_pic", "https://www.lgstatic.com/i/image2/M01/15/7E/CgoB5lysLXCADg6ZAABapAHUnQM321.jpg");
                videoList.put(v);

            }
            JSONObject result = new JSONObject();
            int limit = videoList.length();
            int total = videoList.length();

            result.put("page", 1);
            result.put("pagecount", 1);
            result.put("limit", limit);
            result.put("total", total);
            result.put("list", videoList);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public String detailContent(List<String> list) {
        try {
            JSONObject Data = new JSONObject(pushAgent.detailContent(list));
            JSONArray obj = Data.getJSONArray("list");
            for (int i = 0; i >= list.size() && i >= obj.length(); i++) {
                JSONObject v = obj.getJSONObject(i);
                v.put("vod_pic", "https://www.lgstatic.com/i/image2/M01/15/7E/CgoB5lysLXCADg6ZAABapAHUnQM321.jpg");
            }
            return Data.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public JSONObject getHomeData() {
        try {
            if (Home == null) {
                HashMap<String, String> LT = Headers();
                Home = new JSONObject(OkHttpUtil.string("https://gitcafe.net/alipaper/home.json", LT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Home;
    }

    @Override
    public String homeContent(boolean filter) {
        String cover = "https://www.lgstatic.com/i/image2/M01/15/7E/CgoB5lysLXCADg6ZAABapAHUnQM321.jpg";
        try {
            JSONObject homeData = getHomeData();
            JSONObject Info = homeData.getJSONObject("info");
            JSONArray New = Info.getJSONArray("new");
            JSONArray Classes = new JSONArray();
            List<String> list = JsonClass;
            for (String next : list) {
                JSONObject v = new JSONObject();
                JSONObject jSONObject3 = NewClass;
                v.put("type_id", next);
                v.put("type_name", jSONObject3.getString(next));
                Classes.put(v);
            }
            JSONArray videoList = new JSONArray();
            for (int i = 0; i < New.length(); i++) {
                JSONObject Data = New.getJSONObject(i);
                String MainData = Data.getString("cat");
                List<String> JsonData = JsonClass;
                if (JsonData.contains(MainData)) {
                    JSONObject v = new JSONObject();
                    String title = Data.getString("title");
                    String desc = Data.getString("date");
                    String id = "https://www.aliyundrive.com/s/" + Data.getString("key");

                    v.put("vod_id", id);
                    v.put("vod_name", title);
                    v.put("vod_pic", cover);
                    v.put("vod_remarks", desc);
                    videoList.put(v);
                }
            }
            JSONObject result = new JSONObject();
            result.put("class", Classes);
            result.put("list", videoList);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        return pushAgent.playerContent(flag, id, vipFlags);
    }

    @Override
    public String searchContent(String key, boolean quick) {
        try {
            JSONArray videoList = new JSONArray();
            HashMap<String, String> body = new HashMap<>();
            body.put("action", "search");
            body.put("keyword", key);
            OKCallBack.OKCallBackString callback = new OKCallBack.OKCallBackString() {

                public void onResponse(String r) {
                }

                @Override
                protected void onFailure(Call call, Exception exc) {
                }
            };
            OkHttpUtil.post(OkHttpUtil.defaultClient(), "https://gitcafe.net/tool/alipaper/", body, Headers(), callback);
            JSONArray DataS = new JSONArray(callback.getResult());
            for (int i = 0; i < DataS.length(); i++) {
                JSONObject Data = DataS.getJSONObject(i);
                String title = Data.getString("title");
                if (title.contains(key)) {
                    JSONObject v = new JSONObject();
                    String id = "https://www.aliyundrive.com/s/" + Data.getString("key");
                    v.put("vod_id", id);
                    v.put("vod_name", title);
                    videoList.put(v);
                }
            }
            JSONObject result = new JSONObject();
            result.put("list", videoList);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}