package org.dolphinemu.dolphinemu.utils;

import java.io.File;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.dolphinemu.dolphinemu.BuildConfig;
import org.dolphinemu.dolphinemu.R;
import org.dolphinemu.dolphinemu.dialogs.UpdaterDialog;
import org.dolphinemu.dolphinemu.features.settings.model.BooleanSetting;
import org.dolphinemu.dolphinemu.features.settings.model.IntSetting;
import org.dolphinemu.dolphinemu.features.settings.model.Settings;

public class UpdaterUtils
{
  private static final String URL = "https://api.github.com/repos/Darwin-Rist/Releases/releases";
  private static DownloadUtils sDownload;

  private static JSONArray jsonData;
  private static int sLatestVersion;
  private static int sOlderVersion;
  private static String sUrlLatest;
  private static String sUrlOlder;

  public static void openUpdaterWindow(Context context)
  {
    FragmentManager fm = ((AppCompatActivity) context).getSupportFragmentManager();
    UpdaterDialog updaterDialog = UpdaterDialog.newInstance();
    updaterDialog.show(fm, "fragment_updater");
  }

  public static void checkUpdatesInit(Context context)
  {
    new AfterDirectoryInitializationRunner().run(context, false, () ->
    {
      if (!BooleanSetting.CHECK_UPDATES_PERMISSION_ASKED.getBooleanGlobal())
      {
        showPermissionDialog(context);
      }

      if (BooleanSetting.CHECK_UPDATES.getBooleanGlobal())
      {
        checkUpdates(context);
      }
    });
  }

  private static void checkUpdates(Context context)
  {
    init(context, new LoadCallback()
    {
      @Override
      public void onLoad()
      {
        if (IntSetting.CHECK_UPDATES_SKIPPED.getIntGlobal() != sLatestVersion && getBuildVersion() < sLatestVersion)
        {
          showUpdateMessage(context);
        }
      }

      @Override
      public void onLoadError() {}
    });
  }

  private static void showUpdateMessage(Context context)
  {
    new AlertDialog.Builder(context, R.style.DolphinDialogBase)
      .setTitle(context.getString(R.string.updates_alert))
      .setMessage(context.getString(R.string.updater_alert_body))
      .setPositiveButton(R.string.yes, (dialogInterface, i) ->
        openUpdaterWindow(context))
      .setNegativeButton(R.string.skip_version, (dialogInterface, i) ->
        skippedVersionSetter())
      .setNeutralButton(R.string.not_now,
        ((dialogInterface, i) -> dialogInterface.dismiss()))
      .show();
  }

  private static void showPermissionDialog(Context context)
  {
    new AlertDialog.Builder(context, R.style.DolphinDialogBase)
      .setTitle(context.getString(R.string.check_updates))
      .setMessage(context.getString(R.string.check_updates_description))
      .setPositiveButton(R.string.yes, (dialogInterface, i) ->
        firstUpdaterSetter(true))
      .setNegativeButton(R.string.no, (dialogInterface, i) ->
        firstUpdaterSetter(false))
      .show();
  }

  private static void firstUpdaterSetter(boolean enabled)
  {
    try (Settings settings = new Settings())
    {
      settings.loadSettings(null);

      BooleanSetting.CHECK_UPDATES.setBoolean(settings, enabled);
      BooleanSetting.CHECK_UPDATES_PERMISSION_ASKED.setBoolean(settings, true);

      // Context is set to null to avoid toasts
      settings.saveSettings(null, null);
    }
  }

  private static void skippedVersionSetter()
  {
    try (Settings settings = new Settings())
    {
      settings.loadSettings(null);

      IntSetting.CHECK_UPDATES_SKIPPED.setInt(settings, sLatestVersion);

      // Context is set to null to avoid toasts
      settings.saveSettings(null, null);
    }
  }

  public static void init(Context context, LoadCallback listener)
  {
    RequestQueue queue = Volley.newRequestQueue(context);
    queue.getCache().clear();

    JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, URL, null,
      response ->
      {
        jsonData = response;
        try
        {
          load();
          listener.onLoad();
        }
        catch (Exception e)
        {
          Log.error(e.getMessage());
          listener.onLoadError();
        }
      },
      error -> listener.onLoadError());
    jsonRequest.setShouldCache(false);
    queue.add(jsonRequest);

    cleanDownloadFolder(context);
  }

  private static void load() throws JSONException
  {
    sLatestVersion = jsonData.getJSONObject(0).getInt("tag_name");
    sOlderVersion = jsonData.getJSONObject(1).getInt("tag_name");
    sUrlLatest = jsonData.getJSONObject(0).getJSONArray("assets")
            .getJSONObject(0).getString("browser_download_url");
    sUrlOlder = jsonData.getJSONObject(1).getJSONArray("assets")
            .getJSONObject(0).getString("browser_download_url");
  }

  public static void download(Context context, String url, DownloadCallback listener)
  {
    sDownload = new DownloadUtils(new Handler(Looper.getMainLooper()), listener,
            getDownloadFolder(context));
    sDownload.setUrl(url);
    sDownload.start();
  }

  public static boolean isDownloadRunning()
  {
    if (sDownload == null)
      return false;

    return sDownload.isRunning();
  }

  public static void cancelDownload()
  {
    if (sDownload != null)
      sDownload.cancel();
  }

  public static void cleanDownloadFolder(Context context)
  {
    File downloadFolder = getDownloadFolder(context);
    for (File file : downloadFolder.listFiles())
      file.delete();
  }

  public static File getDownloadFolder(Context context)
  {
    return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
  }

  public static int getBuildVersion()
  {
    try
    {
      return BuildConfig.VERSION_CODE;
    }
    catch (Exception e)
    {
      Log.error(e.getMessage());
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
