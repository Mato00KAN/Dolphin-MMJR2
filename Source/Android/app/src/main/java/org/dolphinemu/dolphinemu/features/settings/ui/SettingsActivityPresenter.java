package org.dolphinemu.dolphinemu.features.settings.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import org.dolphinemu.dolphinemu.R;
import org.dolphinemu.dolphinemu.NativeLibrary;
import org.dolphinemu.dolphinemu.features.settings.model.NativeConfig;
import org.dolphinemu.dolphinemu.features.settings.model.Settings;
import org.dolphinemu.dolphinemu.features.settings.model.BooleanSetting;
import org.dolphinemu.dolphinemu.features.settings.model.FloatSetting;
import org.dolphinemu.dolphinemu.utils.AfterDirectoryInitializationRunner;
import org.dolphinemu.dolphinemu.utils.DirectoryInitialization;
import org.dolphinemu.dolphinemu.utils.Log;

public final class SettingsActivityPresenter
{
  private static final String KEY_SHOULD_SAVE = "should_save";

  private SettingsActivityView mView;

  private Settings mSettings;

  private boolean mShouldSave;

  private AfterDirectoryInitializationRunner mAfterDirectoryInitializationRunner;

  private MenuTag menuTag;
  private String gameId;
  private int revision;
  private Context context;

  SettingsActivityPresenter(SettingsActivityView view, Settings settings)
  {
    mView = view;
    mSettings = settings;
  }

  public void onCreate(Bundle savedInstanceState, MenuTag menuTag, String gameId, int revision,
          Context context)
  {
    this.menuTag = menuTag;
    this.gameId = gameId;
    this.revision = revision;
    this.context = context;

    mShouldSave = savedInstanceState != null && savedInstanceState.getBoolean(KEY_SHOULD_SAVE);
  }

  public void onDestroy()
  {
    if (mSettings != null)
    {
      mSettings.close();
      mSettings = null;
    }
  }

  public void onStart()
  {
    prepareDolphinDirectoriesIfNeeded();
  }

  private void loadSettingsUI()
  {
    if (mSettings.isEmpty())
    {
      if (!TextUtils.isEmpty(gameId))
      {
        mSettings.loadSettings(gameId, revision, mView);

        if (mSettings.gameIniContainsJunk())
        {
          mView.showGameIniJunkDeletionQuestion();
        }
      }
      else
      {
        mSettings.loadSettings(mView);
      }
    }

    mView.showSettingsFragment(menuTag, null, false, gameId);
    mView.onSettingsFileLoaded(mSettings);
  }

  private void prepareDolphinDirectoriesIfNeeded()
  {
    if (DirectoryInitialization.areDolphinDirectoriesReady())
    {
      loadSettingsUI();
    }
    else
    {
      mView.showLoading();

      mAfterDirectoryInitializationRunner = new AfterDirectoryInitializationRunner();
      mAfterDirectoryInitializationRunner.setFinishedCallback(mView::hideLoading);
      mAfterDirectoryInitializationRunner.run(context, true, this::loadSettingsUI);
    }
  }

  public Settings getSettings()
  {
    return mSettings;
  }

  public void clearSettings()
  {
    mSettings.clearSettings();
    onSettingChanged();
  }

  public void onStop(boolean finishing)
  {
    if (mAfterDirectoryInitializationRunner != null)
    {
      mAfterDirectoryInitializationRunner.cancel();
      mAfterDirectoryInitializationRunner = null;
    }

    if (mSettings != null && finishing && mShouldSave)
    {
      Log.debug("[SettingsActivity] Settings activity stopping. Saving settings to INI...");
      mSettings.saveSettings(mView, context);

      // MMJR: load settings to Core immediately only if emulation is running
      if (NativeLibrary.IsRunning())
      {
        updateRunningSettings();
      }
    }
  }

  public void updateRunningSettings()
  {
    try (Settings settings = new Settings())
    {
      // initialize current saved settings
      int[] mRunningSettings = NativeConfig.getEditableSettings();
      int mRunningSettingsLength = mRunningSettings.length;  //little optimization

      // get settings from the running activity
      settings.loadSettings(null);
      int[] newSettings = new int[mRunningSettingsLength];
      int i = 0;
      newSettings[i++] = BooleanSetting.MAIN_SYNC_ON_SKIP_IDLE.getBoolean(settings) ? 1 : 0;
      newSettings[i++] = BooleanSetting.MAIN_JIT_FOLLOW_BRANCH.getBoolean(settings) ? 1 : 0;
      newSettings[i++] = BooleanSetting.MAIN_OVERCLOCK_ENABLE.getBoolean(settings) ? 1 : 0;
      newSettings[i] = (int)(FloatSetting.MAIN_OVERCLOCK.getFloat(settings)*100);

      // set settings to core only if they have been changed
      boolean isChanged = false;
      for (i = 0; !isChanged && i < mRunningSettingsLength; i++)
      {
        if (newSettings[i] != mRunningSettings[i])
        {
          isChanged = true;
        }
      }
      if (isChanged)
      {
        NativeConfig.setEditableSettings(newSettings);
        mView.showToastMessage("applied settings");
      }
    } catch (Exception e) {
      mView.showToastMessage("Something went wrong applying settings to Core");
      Log.error("Something went wrong applying settings to Core");
    }
  }

  public boolean handleOptionsItem(int itemId)
  {
    if (itemId == R.id.menu_save_exit)
    {
      mView.finish();
      return true;
    }

    return false;
  }

  public void onSettingChanged()
  {
    mShouldSave = true;
  }

  public void saveState(Bundle outState)
  {
    outState.putBoolean(KEY_SHOULD_SAVE, mShouldSave);
  }

  public boolean shouldSave()
  {
    return mShouldSave;
  }

  public void onGcPadSettingChanged(MenuTag key, int value)
  {
    if (value != 0) // Not disabled
    {
      Bundle bundle = new Bundle();
      bundle.putInt(SettingsFragmentPresenter.ARG_CONTROLLER_TYPE, value / 6);
      mView.showSettingsFragment(key, bundle, true, gameId);
    }
  }

  public void onWiimoteSettingChanged(MenuTag menuTag, int value)
  {
    switch (value)
    {
      case 1:
        mView.showSettingsFragment(menuTag, null, true, gameId);
        break;

      case 2:
        mView.showToastMessage("Please make sure Continuous Scanning is enabled in Core Settings.");
        break;
    }
  }

  public void onExtensionSettingChanged(MenuTag menuTag, int value)
  {
    if (value != 0) // None
    {
      Bundle bundle = new Bundle();
      bundle.putInt(SettingsFragmentPresenter.ARG_CONTROLLER_TYPE, value);
      mView.showSettingsFragment(menuTag, bundle, true, gameId);
    }
  }
}
