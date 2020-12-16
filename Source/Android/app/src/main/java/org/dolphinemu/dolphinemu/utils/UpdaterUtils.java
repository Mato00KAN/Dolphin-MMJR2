package org.dolphinemu.dolphinemu.utils;

import android.content.Context;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class UpdaterUtils
{
  private static CustomCallback mCallback;
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
          mCallback.onLoadError();
        }
      });
    queue.add(jsonRequest);
  }

  public static void load()
  {
    try
    {
      mConfigVersion = jsonData.getInt("version");
      mLatestVersion = jsonData.getJSONObject("build").getInt("latest");
      mOlderVersion = jsonData.getJSONObject("build").getInt("older");
      mUrlLatest = jsonData.getJSONObject("url").getString("latest");
      mUrlOlder = jsonData.getJSONObject("url").getString("older");
      mCallback.onLoad();
    }
    catch (Exception e)
    {
      mCallback.onLoadError();
    }
  }

  public static void download(String url)
  {
    mCallback.onDownloadStart();
    mCallback.onDownloadProgress(50);
  }

  public static void setCallbackListener(CustomCallback listener)
  {
    mCallback = listener;
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
