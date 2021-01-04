package org.dolphinemu.dolphinemu.dialogs;

import java.io.File;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import org.dolphinemu.dolphinemu.R;
import org.dolphinemu.dolphinemu.utils.DownloadCallback;
import org.dolphinemu.dolphinemu.utils.LoadCallback;
import org.dolphinemu.dolphinemu.utils.UpdaterUtils;

public final class UpdaterDialog extends DialogFragment implements View.OnClickListener, LoadCallback, DownloadCallback
{
  private ViewGroup mViewGroup;
  private Button mActiveButton;
  private Button mInactiveButton;
  private ProgressBar mActivePb;
  private ProgressBar mLoading;
  private final int mBuildVersion = UpdaterUtils.getBuildVersion();

  public static UpdaterDialog newInstance()
  {
    return new UpdaterDialog();
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(),
            R.style.DolphinDialogBase);
    mViewGroup = (ViewGroup) getActivity().getLayoutInflater()
            .inflate(R.layout.dialog_updater, null);

    TextView textInstalled = mViewGroup.findViewById(R.id.text_installed_version);
    textInstalled.setText(getString(R.string.version_description, mBuildVersion, "Installed"));

    mLoading = mViewGroup.findViewById(R.id.updater_loading);

    UpdaterUtils.init(getContext(), this);

    builder.setView(mViewGroup);
    return builder.create();
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
    UpdaterUtils.cancelDownload();
    UpdaterUtils.cleanDownloadFolder(getContext());
  }

  @Override
  public void onLoad()
  {
    TextView textLatest = mViewGroup.findViewById(R.id.text_latest_version);
    TextView textOlder = mViewGroup.findViewById(R.id.text_older_version);
    textLatest.setText(getString(R.string.version_description, UpdaterUtils.getLatestVersion(), "Latest"));
    textOlder.setText(getString(R.string.version_description, UpdaterUtils.getOlderVersion(), "Previous"));

    Button buttonLatest = mViewGroup.findViewById(R.id.button_latest_version);
    Button buttonOlder = mViewGroup.findViewById(R.id.button_older_version);
    buttonLatest.setOnClickListener(this);
    buttonOlder.setOnClickListener(this);

    setUpdaterMessage();
    if (mActiveButton != null)
      disableActiveButton();

    View updaterBody = mViewGroup.findViewById(R.id.updater_body);
    mLoading.setVisibility(View.INVISIBLE);
    updaterBody.setVisibility(View.VISIBLE);
  }

  @Override
  public void onLoadError()
  {
    TextView textError = mViewGroup.findViewById(R.id.updater_error);
    mLoading.setVisibility(View.INVISIBLE);
    textError.setVisibility(View.VISIBLE);
  }

  private void setUpdaterMessage()
  {
    TextView updaterMessage = mViewGroup.findViewById(R.id.text_updater_message);
    if (mBuildVersion >= UpdaterUtils.getLatestVersion())
    {
      updaterMessage.setText(R.string.updater_uptodate);
      updaterMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
      if (mBuildVersion == UpdaterUtils.getLatestVersion())
        mActiveButton = mViewGroup.findViewById(R.id.button_latest_version);
    }
    else
    {
      updaterMessage.setText(R.string.updater_newavailable);
      updaterMessage.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
      if (mBuildVersion == UpdaterUtils.getOlderVersion())
        mActiveButton = mViewGroup.findViewById(R.id.button_older_version);
    }
  }

  /**
   * This function does not check for null mActiveButton reference by design.
   * If null, it will crash the app so check before calling it.
   */
  private void disableActiveButton()
  {
    mActiveButton.setText(null);
    mActiveButton.setEnabled(false);
  }

  @Override
  public void onClick(View view) {
    if (UpdaterUtils.isDownloadRunning())
    {
      UpdaterUtils.cancelDownload();
    }
    else
    {
      int viewId = view.getId();
      mActiveButton = (Button) view;
      String url = null;

      if (viewId == R.id.button_latest_version)
      {
        mActivePb = mViewGroup.findViewById(R.id.progressbar_latest_version);
        mInactiveButton = mViewGroup.findViewById(R.id.button_older_version);
        url = UpdaterUtils.getUrlLatest();
      }
      else if (viewId == R.id.button_older_version)
      {
        mActivePb = mViewGroup.findViewById(R.id.progressbar_older_version);
        mInactiveButton = mViewGroup.findViewById(R.id.button_latest_version);
        url = UpdaterUtils.getUrlOlder();
      }
      mInactiveButton.setClickable(false);

      if (url != null)
      {
        UpdaterUtils.download(getContext(), url, this);
      }
      else
        onDownloadError();
    }
  }

  @Override
  public void onDownloadStart()
  {
    mActivePb.setProgress(0);
    mActiveButton.setActivated(true);
    mActiveButton.setText(R.string.cancel);
  }

  @Override
  public void onDownloadProgress(int progress)
  {
    mActivePb.setProgress(progress);
  }

  @Override
  public void onDownloadComplete(File downloadFile)
  {
    mActiveButton.setText(R.string.button_install);
    onDownloadStop();

    Uri fileUri = FileProvider.getUriForFile(getContext(),
      getContext().getApplicationContext().getPackageName() + ".filesprovider",
      downloadFile);

    Intent promptInstall = new Intent(Intent.ACTION_VIEW);
    promptInstall.setData(fileUri);
    promptInstall.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    startActivity(promptInstall);
  }

  @Override
  public void onDownloadCancelled()
  {
    mActiveButton.setText(R.string.button_download);
    onDownloadStop();
  }

  @Override
  public void onDownloadError()
  {
    mActiveButton.setText(R.string.error);
    onDownloadStop();
  }

  private void onDownloadStop()
  {
    mActiveButton.setActivated(false);
    mInactiveButton.setClickable(true);
  }
}
