package org.dolphinemu.dolphinemu.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import java.lang.ref.WeakReference;

import org.dolphinemu.dolphinemu.R;
import org.dolphinemu.dolphinemu.utils.DownloadCallback;
import org.dolphinemu.dolphinemu.utils.LoadCallback;
import org.dolphinemu.dolphinemu.utils.UpdaterUtils;

public final class UpdaterDialog extends DialogFragment implements View.OnClickListener, LoadCallback, DownloadCallback
{
  private ViewGroup mViewGroup;
  private WeakReference<Button> mActiveButton = new WeakReference<>(null);
  private WeakReference<Button> mInactiveButton = new WeakReference<>(null);
  private WeakReference<ProgressBar> mActivePb = new WeakReference<>(null);
  private WeakReference<View> mActiveCheck = new WeakReference<>(null);
  private WeakReference<ProgressBar> mLoading = new WeakReference<>(null);
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
    textInstalled.setText(getString(R.string.installed_version, mBuildVersion));

    mLoading = new WeakReference<>(mViewGroup.findViewById(R.id.updater_loading));

    UpdaterUtils.init(getContext());
    UpdaterUtils.setOnLoadListener(this);
    UpdaterUtils.setOnDownloadListener(this);

    builder.setView(mViewGroup);
    return builder.create();
  }

  private void setUpdaterMessage()
  {
    TextView updaterMessage = mViewGroup.findViewById(R.id.text_updater_message);
    if (mBuildVersion == UpdaterUtils.getLatestVersion())
    {
      mActiveButton = new WeakReference<>(mViewGroup.findViewById(R.id.button_latest_version));
      updaterMessage.setText(R.string.updater_uptodate);
      updaterMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
    }
    else
    {
      updaterMessage.setText(R.string.updater_newavailable);
      updaterMessage.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
      if (mBuildVersion == UpdaterUtils.getOlderVersion())
      {
        mActiveButton = new WeakReference<>(mViewGroup.findViewById(R.id.button_older_version));
      }
      else return;
    }

    disableActiveButton();
  }

  private void disableActiveButton()
  {
    mActiveButton.get().setText(null);
    mActiveButton.get().setEnabled(false);
    if (mActiveButton.get().getId() == R.id.button_latest_version)
    {
      mActiveCheck = new WeakReference<>(mViewGroup.findViewById(R.id.check_latest_version));
    }
    else if (mActiveButton.get().getId() == R.id.button_older_version)
    {
      mActiveCheck = new WeakReference<>(mViewGroup.findViewById(R.id.check_older_version));
    }
    mActiveCheck.get().setVisibility(View.VISIBLE);
  }

  @Override
  public void onLoad()
  {
    TextView textLatest = mViewGroup.findViewById(R.id.text_latest_version);
    TextView textOlder = mViewGroup.findViewById(R.id.text_older_version);
    textLatest.setText(getString(R.string.version_description, UpdaterUtils.getLatestVersion()));
    textOlder.setText(getString(R.string.version_description, UpdaterUtils.getOlderVersion()));

    Button buttonLatest = mViewGroup.findViewById(R.id.button_latest_version);
    Button buttonOlder = mViewGroup.findViewById(R.id.button_older_version);
    buttonLatest.setOnClickListener(this);
    buttonOlder.setOnClickListener(this);

    setUpdaterMessage();

    View updaterUi = mViewGroup.findViewById(R.id.updater_ui);
    mLoading.get().setVisibility(View.INVISIBLE);
    updaterUi.setVisibility(View.VISIBLE);
  }

  @Override
  public void onLoadError()
  {
    TextView textError = mViewGroup.findViewById(R.id.updater_error);
    mLoading.get().setVisibility(View.INVISIBLE);
    textError.setVisibility(View.VISIBLE);
  }

  @Override
  public void onClick(View view) {
    int viewId = view.getId();
    mActiveButton = new WeakReference<>((Button) view);
    String url = null;

    if (viewId == R.id.button_latest_version)
    {
      mActivePb = new WeakReference<>(mViewGroup.findViewById(R.id.progressbar_latest_version));
      mInactiveButton = new WeakReference<>(mViewGroup.findViewById(R.id.button_older_version));
      url = UpdaterUtils.getUrlLatest();
    }
    else if (viewId == R.id.button_older_version)
    {
      mActivePb = new WeakReference<>(mViewGroup.findViewById(R.id.progressbar_older_version));
      mInactiveButton = new WeakReference<>(mViewGroup.findViewById(R.id.button_latest_version));
      url = UpdaterUtils.getUrlOlder();
    }
    mInactiveButton.get().setClickable(false);

    if (url != null)
      UpdaterUtils.download(url);
    else
      onDownloadError();
  }

  @Override
  public void onDownloadStart()
  {
    mActivePb.get().setVisibility(View.VISIBLE);
    mActiveButton.get().setVisibility(View.INVISIBLE);
  }

  @Override
  public void onDownloadProgress(int progress)
  {
    mActivePb.get().setProgress(progress);
  }

  @Override
  public void onDownloadComplete()
  {
    disableActiveButton();
    onDownloadStop();
  }

  @Override
  public void onDownloadError()
  {
    mActiveButton.get().setText(R.string.button_error);
    onDownloadStop();
  }

  public void onDownloadStop()
  {
    mActivePb.get().setVisibility(View.INVISIBLE);
    mActiveButton.get().setVisibility(View.VISIBLE);
    mInactiveButton.get().setClickable(true);
  }
}
