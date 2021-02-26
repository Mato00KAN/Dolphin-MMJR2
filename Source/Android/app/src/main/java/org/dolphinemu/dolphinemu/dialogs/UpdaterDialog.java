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
  private static final String DATA = "data";

  private ViewGroup mViewGroup;
  private Button mButton;
  private ProgressBar mLoading;
  private ProgressBar mProgressBar;
  private Button mButtonChangelog;
  private ProgressBar mLoadingChangelog;
  private TextView mTextChangelog;
  private View mChangelog;
  private ImageView mArrow;
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

    mLoading = mViewGroup.findViewById(R.id.updater_loading);
    mButton = mViewGroup.findViewById(R.id.button_download);
    mProgressBar = mViewGroup.findViewById(R.id.progressbar_download);
    mButtonChangelog = mViewGroup.findViewById(R.id.button_view_changelog);
    mTextChangelog = mViewGroup.findViewById(R.id.changelog_text);
    mLoadingChangelog = mViewGroup.findViewById(R.id.changelog_loading);
    mChangelog = mViewGroup.findViewById(R.id.changelog_body);
    mArrow = mViewGroup.findViewById(R.id.changelog_arrow);

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

    TextView textLatest = mViewGroup.findViewById(R.id.text_version);
    textLatest.setText(getString(R.string.version_description, mData.getVersion()));

    mButton.setOnClickListener(this);
    mButtonChangelog.setOnClickListener(this);

    setUpdaterMessage();

    View updaterBody = mViewGroup.findViewById(R.id.updater_body);
    mLoading.setVisibility(View.GONE);
    updaterBody.setVisibility(View.VISIBLE);
  }

  @Override
  public void onLoadError()
  {
    TextView textError = mViewGroup.findViewById(R.id.updater_error);
    mLoading.setVisibility(View.GONE);
    textError.setVisibility(View.VISIBLE);
  }

  @Override
  public void onClick(View view)
  {
    if (view == mButton)
    {
      if (mDownload.isRunning())
      {
        mDownload.cancel();
      }
      else
      {
        String url = mData.getDownloadUrl();
        mDownload.setUrl(url);
        mDownload.start();
      }
    }
    else if (view == mButtonChangelog)
    {
      handleChangelog();
    }
  }

  @Override
  public void onDownloadStart()
  {
    mProgressBar.setProgress(0);
    mButton.setActivated(true);
    mButton.setText(R.string.cancel);
  }

  @Override
  public void onDownloadProgress(int progress)
  {
    mProgressBar.setProgress(progress);
  }

  @Override
  public void onDownloadComplete(File downloadFile)
  {
    mButton.setText(R.string.button_install);
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
    mButton.setText(R.string.button_download);
    onDownloadStop();
  }

  @Override
  public void onDownloadError()
  {
    mButton.setText(R.string.error);
    onDownloadStop();
  }

  private void onDownloadStop()
  {
    mButton.setActivated(false);
  }

  private void setUpdaterMessage()
  {
    TextView updaterMessage = mViewGroup.findViewById(R.id.text_updater_message);
    if (mBuildVersion >= mData.getVersion())
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
      mLoadingChangelog.setVisibility(View.VISIBLE);

      UpdaterUtils.makeChangelogRequest(getString(R.string.changelog_section),
        new LoadCallback<String>()
        {
          @Override
          public void onLoad(String data)
          {
            new Handler().postDelayed(() -> mLoadingChangelog.setVisibility(View.GONE), 200);
            mTextChangelog.setText(data);
            mArrow.startAnimation(rotateDown);
            mChangelog.setVisibility(View.VISIBLE);
            isChangelogOpen = true;
          }

          @Override
          public void onLoadError()
          {
            TextView textError = mViewGroup.findViewById(R.id.changelog_error);
            mLoadingChangelog.setVisibility(View.GONE);
            textError.setVisibility(View.VISIBLE);
            new Handler().postDelayed(() -> opacityOut(textError, View.INVISIBLE), 1500);
          }
        });
    }
    else
    {
      isChangelogOpen = false;
      mArrow.startAnimation(rotateUp);
      mChangelog.setVisibility(View.GONE);
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
