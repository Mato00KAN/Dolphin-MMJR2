package org.dolphinemu.dolphinemu.utils;

import android.content.Context;
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
  private static LoadCallback mLoadCallback;
  private static DownloadCallback mDownloadCallback;
  private static final String mURL = "https://api.npoint.io/c43ee26a63ee41e7c3e5";

  private static JSONObject jsonData;
  private static int mConfigVersion;
  private static int mLatestVersion;
  private static int mOlderVersion;
  private static String mUrlLatest;
  private static String mUrlOlder;

  public static void init(Context context)
  {
    RequestQueue queue = Volley.newRequestQueue(context);

    JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, mURL, null,
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
          mLoadCallback.onLoadError();
        }
      });
    queue.add(jsonRequest);
  }

  private static void load()
  {
    try
    {
      mConfigVersion = jsonData.getInt("version");
      mLatestVersion = jsonData.getJSONObject("build").getInt("latest");
      mOlderVersion = jsonData.getJSONObject("build").getInt("older");
      mUrlLatest = jsonData.getJSONObject("url").getString("latest");
      mUrlOlder = jsonData.getJSONObject("url").getString("older");
      mLoadCallback.onLoad();
    }
    catch (JSONException e)
    {
      mLoadCallback.onLoadError();
    }
  }

  public static void download(String url)
  {
    DownloadUtils download = new DownloadUtils(new Handler(Looper.getMainLooper()), url);
    download.setCallbackListener(mDownloadCallback);
    download.start();
  }

  public static void setOnLoadListener(LoadCallback listener)
  {
    mLoadCallback = listener;
  }

  public static void setOnDownloadListener(DownloadCallback listener)
  {
    mDownloadCallback = listener;
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
    return mLatestVersion;
  }

  public static int getOlderVersion()
  {
    return mOlderVersion;
  }

  public static String getUrlLatest()
  {
    return mUrlLatest;
  }

  public static String getUrlOlder()
  {
    return mUrlOlder;
  }
}
