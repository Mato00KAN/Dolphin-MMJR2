package org.dolphinemu.dolphinemu.utils;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import org.json.JSONArray;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.dolphinemu.dolphinemu.BuildConfig;

public class UpdaterUtils
{
  private static LoadCallback sLoadCallback;
  private static DownloadCallback sDownloadCallback;
  private static final String URL = "https://api.github.com/repos/Darwin-Rist/Releases/releases";
  private static DownloadUtils sDownload;

  private static JSONArray jsonData;
  private static int sLatestVersion;
  private static int sOlderVersion;
  private static String sUrlLatest;
  private static String sUrlOlder;

  public static void init(Context context)
  {
    RequestQueue queue = Volley.newRequestQueue(context);

    JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, URL, null,
      response ->
      {
        jsonData = response;
        load();
      },
      error -> sLoadCallback.onLoadError());
    queue.add(jsonRequest);

    cleanFolder(getDownloadFolder(context));
    sDownload = new DownloadUtils(new Handler(Looper.getMainLooper()), sDownloadCallback,
      getDownloadFolder(context));
  }

  private static void load()
  {
    try
    {
      sLatestVersion = jsonData.getJSONObject(0).getInt("tag_name");
      sOlderVersion = jsonData.getJSONObject(1).getInt("tag_name");
      sUrlLatest = jsonData.getJSONObject(0).getJSONArray("assets")
        .getJSONObject(0).getString("browser_download_url");
      sUrlOlder = jsonData.getJSONObject(1).getJSONArray("assets")
        .getJSONObject(0).getString("browser_download_url");

      sLoadCallback.onLoad();
    }
    catch (Exception e) { sLoadCallback.onLoadError(); }
  }

  public static void download(String url)
  {
    sDownload.setUrl(url);
    sDownload.start();
  }

  public static void cancelDownload()
  {
    if (sDownload != null)
      sDownload.cancel();
  }

  public static void cleanFolder(@NonNull File folder)
  {
    for (File file : folder.listFiles())
      file.delete();
  }

  public static File getDownloadFolder(Context context)
  {
    return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
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
