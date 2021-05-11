package org.dolphinemu.dolphinemu.dialogs;

import java.io.File;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import org.dolphinemu.dolphinemu.R;
import org.dolphinemu.dolphinemu.model.UpdaterData;
import org.dolphinemu.dolphinemu.utils.DownloadCallback;
import org.dolphinemu.dolphinemu.utils.DownloadUtils;
import org.dolphinemu.dolphinemu.utils.LoadCallback;
import org.dolphinemu.dolphinemu.utils.UpdaterUtils;

public final class UpdaterDialog extends DialogFragment implements View.OnClickListener,
                                                                   LoadCallback<UpdaterData>,
                                                                   DownloadCallback
{
  private static final String DATA = "updaterData";

  private ViewGroup mViewGroup;
  private View updaterBody;
  private Button downloadButton, changelogButton;
  private ProgressBar loadingBar, downloadProgressBar, changelogProgressBar;
  private TextView errorText, downloadText, changelogBody, changelogErrorText;
  private ImageView changelogArrow;

  private UpdaterData mData;
  private DownloadUtils mDownload;

  private Animation rotateDown;
  private Animation rotateUp;

  private final int mBuildVersion = UpdaterUtils.getBuildVersion();
  private boolean isChangelogOpen = false;

  public static UpdaterDialog newInstance(UpdaterData data)
  {
    UpdaterDialog fragment = new UpdaterDialog();

    if (data != null)
    {
      Bundle arguments = new Bundle();
      arguments.putParcelable(DATA, data);
      fragment.setArguments(arguments);
    }

    return fragment;
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
    textInstalled.setText(getString(R.string.installed_version, mBuildVersion));

    loadingBar = mViewGroup.findViewById(R.id.updater_loading);
    updaterBody = mViewGroup.findViewById(R.id.updater_body);
    errorText = mViewGroup.findViewById(R.id.updater_error);
    downloadButton = mViewGroup.findViewById(R.id.button_download);
    downloadText = mViewGroup.findViewById(R.id.text_version);
    downloadProgressBar = mViewGroup.findViewById(R.id.progressbar_download);
    changelogButton = mViewGroup.findViewById(R.id.button_view_changelog);
    changelogProgressBar = mViewGroup.findViewById(R.id.changelog_loading);
    changelogBody = mViewGroup.findViewById(R.id.changelog_text);
    changelogErrorText = mViewGroup.findViewById(R.id.changelog_error);
    changelogArrow = mViewGroup.findViewById(R.id.changelog_arrow);

    if (getArguments() != null) // Assuming valid data is passed!
    {
      onLoad(getArguments().getParcelable(DATA));
    }
    else
    {
      UpdaterUtils.makeDataRequest(this);
    }

    mDownload = new DownloadUtils(new Handler(Looper.getMainLooper()),
            this, UpdaterUtils.getDownloadFolder(getContext()));
    initAnimations();

    builder.setView(mViewGroup);
    return builder.create();
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
    mDownload.cancel();
    UpdaterUtils.cleanDownloadFolder(getContext());
  }

  @Override
  public void onLoad(UpdaterData data)
  {
    mData = data;

    downloadText.setText(getString(R.string.version_description, mData.version));
    downloadButton.setOnClickListener(this);
    changelogButton.setOnClickListener(this);

    setUpdaterMessage();

    loadingBar.setVisibility(View.GONE);
    updaterBody.setVisibility(View.VISIBLE);
  }

  @Override
  public void onLoadError()
  {
    loadingBar.setVisibility(View.GONE);
    errorText.setVisibility(View.VISIBLE);
  }

  @Override
  public void onClick(View view)
  {
    if (view == downloadButton)
    {
      if (mDownload.isRunning())
      {
        mDownload.cancel();
      }
      else
      {
        mDownload.setUrl(mData.downloadUrl);
        mDownload.start();
      }
    }
    else if (view == changelogButton)
    {
      handleChangelog();
    }
  }

  @Override
  public void onDownloadStart()
  {
    downloadProgressBar.setProgress(0);
    downloadButton.setActivated(true);
    downloadButton.setText(R.string.cancel);
  }

  @Override
  public void onDownloadProgress(int progress)
  {
    downloadProgressBar.setProgress(progress);
  }

  @Override
  public void onDownloadComplete(File downloadFile)
  {
    downloadButton.setText(R.string.button_install);
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
    downloadButton.setText(R.string.button_download);
    onDownloadStop();
  }

  @Override
  public void onDownloadError()
  {
    downloadButton.setText(R.string.error);
    onDownloadStop();
  }

  private void onDownloadStop()
  {
    downloadButton.setActivated(false);
  }

  private void setUpdaterMessage()
  {
    TextView updaterMessage = mViewGroup.findViewById(R.id.text_updater_message);
    if (mBuildVersion >= mData.version)
    {
      updaterMessage.setText(R.string.updater_uptodate);
      updaterMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
    }
    else
    {
      updaterMessage.setText(R.string.updater_newavailable);
      updaterMessage.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
    }
  }

  private void handleChangelog()
  {
    if (!isChangelogOpen)
    {
      changelogProgressBar.setVisibility(View.VISIBLE);

      UpdaterUtils.makeChangelogRequest(getString(R.string.changelog_section),
        new LoadCallback<String>()
        {
          @Override
          public void onLoad(String data)
          {
            changelogProgressBar.setVisibility(View.GONE);
            changelogBody.setText(data);
            changelogArrow.startAnimation(rotateDown);
            changelogBody.setVisibility(View.VISIBLE);
            isChangelogOpen = true;
          }

          @Override
          public void onLoadError()
          {
            changelogProgressBar.setVisibility(View.GONE);
            changelogErrorText.setVisibility(View.VISIBLE);
            new Handler().postDelayed(() -> opacityOut(changelogErrorText, View.INVISIBLE), 1750);
          }
        });
    }
    else
    {
      isChangelogOpen = false;
      changelogArrow.startAnimation(rotateUp);
      changelogBody.setVisibility(View.GONE);
    }
  }

  // UI animations stuff
  private void initAnimations()
  {
    rotateDown = new RotateAnimation(0.0f, -180.0f,
      Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
      0.5f);
    rotateDown.setRepeatCount(0);
    rotateDown.setDuration(200);
    rotateDown.setFillAfter(true);

    rotateUp = new RotateAnimation(-180.0f, 0.0f,
      Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
      0.5f);
    rotateUp.setRepeatCount(0);
    rotateUp.setDuration(200);
    rotateUp.setFillAfter(true);
  }

  private void opacityOut(View view, int endVisibility)
  {
    view.animate()
      .alpha(0.0f)
      .setListener(new AnimatorListenerAdapter()
      {
        @Override
        public void onAnimationEnd(Animator animation)
        {
          view.setVisibility(endVisibility);
          view.setAlpha(1.0f);
          view.animate().setListener(null);
        }
      });
  }
}
