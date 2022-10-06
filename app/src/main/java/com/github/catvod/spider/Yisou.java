package com.github.catvod.spider;

import android.content.Context;

import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.okhttp.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;


public class Yisou extends Spider {
    private static final Pattern aliyun = Pattern.compile("(https://www.aliyundrive.com/s/[^\"]+)");
    private PushAgent ali;


    @Override
    public String detailContent(List<String> ids) {
        try {
            return ali.detailContent(ids);
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    private HashMap<String, String> headers() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.54 Safari/537.36");
        return headers;
    }


    @Override
    public void init(Context context, String ext) {
        super.init(context, ext);
        ali = new PushAgent();
        ali.init(context, ext);
    }


    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        return ali.playerContent(flag, id, vipFlags);
    }


    @Override
    public String searchContent(String key, boolean quick) {
        JSONObject result = new JSONObject();
        try {
            if (quick)
                return "";
            String url = "https://yiso.fun/api/search?name=" + key + "&pageNo=1&from=ali";
            String data = OkHttpUtil.string(url, headers());
            JSONObject json = new JSONObject(data);
            JSONArray list = json.optJSONObject("data").optJSONArray("list");
            JSONArray videos = new JSONArray();
            for (int i = 0; i < list.length(); i++) {
                JSONObject item = list.optJSONObject(i);
                JSONObject v = new JSONObject();
                v.put("vod_id", item.optString("url"));
                v.put("vod_name", Jsoup.parse(item.optString("name")).text());
                v.put("vod_pic", "https://pic.rmb.bdstatic.com/bjh/6a2278365c10139b5b03229c2ecfeea4.jpeg");
                v.put("vod_remarks", item.optString("gmtCreate"));
                videos.put(v);
            }
            result.put("list", videos);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }
}