// SPDX-License-Identifier: GPL-2.0-or-later

package org.dolphinemu.dolphinemu.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.dolphinemu.dolphinemu.R;
import org.dolphinemu.dolphinemu.activities.ConvertActivity;
import org.dolphinemu.dolphinemu.activities.CheatEditorActivity;
import org.dolphinemu.dolphinemu.features.settings.model.Settings;
import org.dolphinemu.dolphinemu.features.settings.model.StringSetting;
import org.dolphinemu.dolphinemu.features.settings.ui.MenuTag;
import org.dolphinemu.dolphinemu.features.settings.ui.SettingsActivity;
import org.dolphinemu.dolphinemu.model.GameFile;
import org.dolphinemu.dolphinemu.ui.platform.Platform;
import org.dolphinemu.dolphinemu.utils.DirectoryInitialization;
import org.dolphinemu.dolphinemu.utils.Log;
import org.dolphinemu.dolphinemu.utils.PicassoUtils;

import java.io.File;

public class GamePropertiesDialog extends DialogFragment
{
  public static final String TAG = "GamePropertiesDialog";
  private static final String ARG_PATH = "path";
  private static final String ARG_GAMEID = "game_id";
  public static final String ARG_REVISION = "revision";
  private static final String ARG_PLATFORM = "platform";
  private static final String ARG_SHOULD_ALLOW_CONVERSION = "should_allow_conversion";

  public static GamePropertiesDialog newInstance(GameFile gameFile)
  {
    GamePropertiesDialog fragment = new GamePropertiesDialog();

    Bundle arguments = new Bundle();
    arguments.putString(ARG_PATH, gameFile.getPath());
    arguments.putString(ARG_GAMEID, gameFile.getGameId());
    arguments.putInt(ARG_REVISION, gameFile.getRevision());
    arguments.putInt(ARG_PLATFORM, gameFile.getPlatform());
    arguments.putBoolean(ARG_SHOULD_ALLOW_CONVERSION, gameFile.shouldAllowConversion());
    fragment.setArguments(arguments);

    return fragment;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    final String path = requireArguments().getString(ARG_PATH);
    final String gameId = requireArguments().getString(ARG_GAMEID);
    final int revision = requireArguments().getInt(ARG_REVISION);
    final int platform = requireArguments().getInt(ARG_PLATFORM);
    final boolean shouldAllowConversion =
            requireArguments().getBoolean(ARG_SHOULD_ALLOW_CONVERSION);

    final boolean isDisc = platform == Platform.GAMECUBE.toInt() ||
            platform == Platform.WII.toInt();
    final boolean isWii = platform != Platform.GAMECUBE.toInt();

    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
    ViewGroup contents = (ViewGroup) getActivity().getLayoutInflater()
            .inflate(R.layout.dialog_game_properties, null);


    ImageView banner = contents.findViewById(R.id.banner);


    Button buttonDetails = contents.findViewById(R.id.properties_details);
    buttonDetails.setOnClickListener(view ->
            GameDetailsDialog.newInstance(path).show(requireActivity()
                    .getSupportFragmentManager(), "game_details"));

    Button buttonCustomSettings = contents.findViewById(R.id.custom_settings);
    buttonCustomSettings.setOnClickListener(view ->
            SettingsActivity.launch(getContext(), MenuTag.CONFIG, gameId, revision, isWii));

    Button buttonCheats = contents.findViewById(R.id.button_cheat_code);
    buttonCheats.setOnClickListener(view ->
            CheatEditorActivity.launch(getContext(), path));

    Button buttonConvert = contents.findViewById(R.id.properties_convert);
    buttonConvert.setEnabled(false);
    if (shouldAllowConversion)
    {
      buttonConvert.setEnabled(true);
      buttonConvert.setOnClickListener(view ->
              ConvertActivity.launch(getContext(), path));
    }

/*  if (isDisc)
    {
      Button buttonDefaultISO = contents.findViewById(R.id.properties_default_iso);
      buttonDefaultISO.setOnClickListener(view ->
      {
        try (Settings settings = new Settings())
        {
          settings.loadSettings();
          StringSetting.MAIN_DEFAULT_ISO.setString(settings, path);
          settings.saveSettings(null, getContext());
        }
      });
    }*/

    Button buttonGCControls = contents.findViewById(R.id.button_gcpad_settings);
    buttonGCControls.setOnClickListener(view ->
            SettingsActivity.launch(getContext(), MenuTag.GCPAD_TYPE, gameId, revision, isWii));

    Button buttonWiiControls = contents.findViewById(R.id.button_wiimote_settings);
    if (isWii)
    {
      buttonWiiControls.setOnClickListener(view ->
              SettingsActivity.launch(getActivity(), MenuTag.WIIMOTE, gameId, revision, isWii));
    }
    else
    {
      buttonWiiControls.setEnabled(false);
    }


    Button buttonClearSettings = contents.findViewById(R.id.properties_clear_settings);
    buttonClearSettings.setOnClickListener(view ->
            clearGameSettings(gameId));

    Button buttonClearCache = contents.findViewById(R.id.menu_clear_data);
    buttonClearCache.setOnClickListener(view ->
      clearGameData(gameId));


    PicassoUtils.loadGameBanner(banner, GameFile.parse(path));

    builder.setView(contents);

    return builder.create();
  }

  private void clearGameSettings(String gameId)
  {
    String gameSettingsPath =
            DirectoryInitialization.getUserDirectory() + "/GameSettings/" + gameId + ".ini";
    String gameProfilesPath = DirectoryInitialization.getUserDirectory() + "/Config/Profiles/";
    File gameSettingsFile = new File(gameSettingsPath);
    File gameProfilesDirectory = new File(gameProfilesPath);
    boolean hadGameProfiles = recursivelyDeleteGameProfiles(gameProfilesDirectory, gameId);

    if (gameSettingsFile.exists() || hadGameProfiles)
    {
      if (gameSettingsFile.delete() || hadGameProfiles)
      {
        Toast.makeText(getContext(),
                getResources().getString(R.string.properties_clear_success, gameId),
                Toast.LENGTH_SHORT).show();
      }
      else
      {
        Toast.makeText(getContext(),
                getResources().getString(R.string.properties_clear_failure, gameId),
                Toast.LENGTH_SHORT).show();
      }
    }
    else
    {
      Toast.makeText(getContext(), R.string.properties_clear_missing, Toast.LENGTH_SHORT).show();
    }
  }

  private boolean recursivelyDeleteGameProfiles(@NonNull final File file, String gameId)
  {
    boolean hadGameProfiles = false;

    if (file.isDirectory())
    {
      File[] files = file.listFiles();

      if (files == null)
      {
        return false;
      }

      for (File child : files)
      {
        if (child.getName().startsWith(gameId) && child.isFile())
        {
          if (!child.delete())
          {
            Log.error("[GamePropertiesDialog] Failed to delete " + child.getAbsolutePath());
          }
          hadGameProfiles = true;
        }
        hadGameProfiles |= recursivelyDeleteGameProfiles(child, gameId);
      }
    }
    return hadGameProfiles;
  }
  private void clearGameData(String gameId)
  {
    int count = 0;
    String cachePath = String.valueOf(getContext().getExternalCacheDir());
    File dir = new File(cachePath);
    if (dir.exists())
    {
      for (File f : dir.listFiles())
      {
        if (f.getName().contains(gameId) & f.getName().endsWith(".uidcache"))
        {
          if (f.delete())
          {
            count += 1;
          }
        }
      }
    }

    String shadersPath = cachePath + File.separator + "Shaders";
    dir = new File(shadersPath);
    if (dir.exists())
    {
      for (File f : dir.listFiles())
      {
        if (f.getName().contains(gameId) & f.getName().endsWith(".cache"))
        {
          if (f.delete())
          {
            count += 1;
          }
        }
      }
    }

    if (count > 0){
      Toast.makeText(getContext(), "Cleared Cache for " + gameId, Toast.LENGTH_SHORT)
        .show();
    } else{
      Toast.makeText(getContext(), "No Cache Found for " + gameId, Toast.LENGTH_SHORT)
        .show();
    }
  }
}
