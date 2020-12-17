package org.dolphinemu.dolphinemu.utils;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.dolphinemu.dolphinemu.BuildConfig;

public class UpdaterUtils
{
  private static LoadCallback sLoadCallback;
  private static DownloadCallback sDownloadCallback;
  private static final String URL = "https://api.npoint.io/c43ee26a63ee41e7c3e5";
  private static DownloadUtils sDownload;

  private static JSONObject jsonData;
  private static int sConfigVersion;
  private static int sLatestVersion;
  private static int sOlderVersion;
  private static String sUrlLatest;
  private static String sUrlOlder;

  public static void init(Context context)
  {
    RequestQueue queue = Volley.newRequestQueue(context);

    JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, URL, null,
      new Response.Listener<JSONObject>()
      {
        @Override
        public void onResponse(JSONObject response)
        {
          jsonData = response;
          load();
        }
      },
      new Response.ErrorListener()
      {
        @Override
        public void onErrorResponse(VolleyError error)
        {
          sLoadCallback.onLoadError();
        }
      });
    queue.add(jsonRequest);
  }

  private static void load()
  {
    try
    {
      sConfigVersion = jsonData.getInt("version");
      sLatestVersion = jsonData.getJSONObject("build").getInt("latest");
      sOlderVersion = jsonData.getJSONObject("build").getInt("older");
      sUrlLatest = jsonData.getJSONObject("url").getString("latest");
      sUrlOlder = jsonData.getJSONObject("url").getString("older");
      sLoadCallback.onLoad();
    }
    catch (JSONException e)
    {
      sLoadCallback.onLoadError();
    }
  }

  public static void download(Context context, String url)
  {
    sDownload = new DownloadUtils(new Handler(Looper.getMainLooper()), sDownloadCallback, url,
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS));
    sDownload.start();
  }

  public static void cancelDownload()
  {
    sDownload.cancel();
  }

  public static void setOnLoadListener(LoadCallback listener)
  {
    sLoadCallback = listener;
  }

  public static void setOnDownloadListener(DownloadCallback listener)
  {
    sDownloadCallback = listener;
  }

  public static int getBuildVersion()
  {
    try
    {
      return BuildConfig.VERSION_CODE;
    }
    catch (Exception e)
    {
      return -1;
    }
  }

  public static int getLatestVersion()
  {
    return sLatestVersion;
  }

  public static int getOlderVersion()
  {
    return sOlderVersion;
  }

  public static String getUrlLatest()
  {
    return sUrlLatest;
  }

  public static String getUrlOlder()
  {
    return sUrlOlder;
  }
}
