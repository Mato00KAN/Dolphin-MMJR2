package org.dolphinemu.dolphinemu.utils;

import java.io.File;

import android.content.Context;
import android.os.Environment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.dolphinemu.dolphinemu.BuildConfig;
import org.dolphinemu.dolphinemu.R;
import org.dolphinemu.dolphinemu.model.UpdaterData;
import org.dolphinemu.dolphinemu.dialogs.UpdaterDialog;
import org.dolphinemu.dolphinemu.features.settings.model.BooleanSetting;
import org.dolphinemu.dolphinemu.features.settings.model.IntSetting;
import org.dolphinemu.dolphinemu.features.settings.model.Settings;

public class UpdaterUtils
{
  public static final String URL = "https://api.github.com/repos/Darwin-Rist/Releases/releases";
  public static final String URL_LATEST = "https://api.github.com/repos/Darwin-Rist/Releases/releases/latest";

  public static void openUpdaterWindow(Context context, UpdaterData data)
  {
    FragmentManager fm = ((AppCompatActivity) context).getSupportFragmentManager();
    UpdaterDialog updaterDialog = UpdaterDialog.newInstance(data);
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
    makeDataRequest(context, new LoadCallback<UpdaterData>()
    {
      @Override
      public void onLoad(UpdaterData data)
      {
        if (IntSetting.CHECK_UPDATES_SKIPPED.getIntGlobal() != data.getVersion() &&
            getBuildVersion() < data.getVersion())
        {
          showUpdateMessage(context, data);
        }
      }

      @Override
      public void onLoadError() {}
    });
  }

  private static void showUpdateMessage(Context context, UpdaterData data)
  {
    new AlertDialog.Builder(context, R.style.DolphinDialogBase)
      .setTitle(context.getString(R.string.updates_alert))
      .setMessage(context.getString(R.string.updater_alert_body))
      .setPositiveButton(R.string.yes, (dialogInterface, i) ->
        openUpdaterWindow(context, data))
      .setNegativeButton(R.string.skip_version, (dialogInterface, i) ->
        setSkipVersion(data.getVersion()))
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
        setPrefs(true))
      .setNegativeButton(R.string.no, (dialogInterface, i) ->
        setPrefs(false))
      .show();
  }

  private static void setPrefs(boolean enabled)
  {
    try (Settings settings = new Settings())
    {
      settings.loadSettings();

      BooleanSetting.CHECK_UPDATES.setBoolean(settings, enabled);
      BooleanSetting.CHECK_UPDATES_PERMISSION_ASKED.setBoolean(settings, true);

      // Context is set to null to avoid toasts
      settings.saveSettings(null, null);
    }
  }

  private static void setSkipVersion(int version)
  {
    try (Settings settings = new Settings())
    {
      settings.loadSettings();

      IntSetting.CHECK_UPDATES_SKIPPED.setInt(settings, version);

      // Context is set to null to avoid toasts
      settings.saveSettings(null, null);
    }
  }

  public static void makeDataRequest(Context context, LoadCallback<UpdaterData> listener)
  {
    JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, URL_LATEST, null,
      response ->
      {
        try
        {
          UpdaterData data = new UpdaterData(response);
          listener.onLoad(data);
        }
        catch (Exception e)
        {
          Log.error(e.getMessage());
          listener.onLoadError();
        }
      },
      error -> listener.onLoadError());
    VolleyUtil.getQueue().add(jsonRequest);

    cleanDownloadFolder(context);
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
}
