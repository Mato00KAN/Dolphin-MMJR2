package org.dolphinemu.dolphinemu.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.dolphinemu.dolphinemu.R;
import org.dolphinemu.dolphinemu.BuildConfig;
import org.dolphinemu.dolphinemu.utils.UpdaterUtils;

public final class UpdaterDialog extends DialogFragment implements View.OnClickListener
{
  private static ViewGroup mViewGroup;
  private static Button mButtonLatest;
  private static Button mButtonOlder;
  private static ProgressBar mProgressbarLatest;
  private static ProgressBar mProgressbarOlder;
  private boolean mLatestDownloaded = false;
  private boolean mOlderDownloaded = false;

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

    UpdaterUtils.init();
    TextView textInstalled = mViewGroup.findViewById(R.id.text_installed_version);
    TextView textLatest = mViewGroup.findViewById(R.id.text_latest_version);
    TextView textOlder = mViewGroup.findViewById(R.id.text_older_version);
    textInstalled.setText(getString(R.string.installed_version, BuildConfig.VERSION_CODE));
    textLatest.setText(getString(R.string.version_description, UpdaterUtils.getLatestVersion()));
    textOlder.setText(getString(R.string.version_description, UpdaterUtils.getOlderVersion()));

    mProgressbarLatest = mViewGroup.findViewById(R.id.progressbar_latest_version);
    mProgressbarOlder = mViewGroup.findViewById(R.id.progressbar_older_version);

    mButtonLatest = mViewGroup.findViewById(R.id.button_latest_version);
    mButtonOlder = mViewGroup.findViewById(R.id.button_older_version);
    if (UpdaterUtils.getErrorLevel())
    {
      mButtonLatest.setOnClickListener(this);
      mButtonOlder.setOnClickListener(this);
    }
    else
    {
      mButtonLatest.setText(R.string.button_error);
      mButtonOlder.setText(R.string.button_error);
    }

    builder.setView(mViewGroup);
    return builder.create();
  }

  @Override
  public void onClick(View view) {
    int viewId = view.getId();
    ProgressBar activeProgressbar = null;
    Button activeButton = null;

    if (viewId == R.id.button_latest_version)
    {
      if (mLatestDownloaded)
        return;
      activeProgressbar = mProgressbarLatest;
      activeButton = mButtonLatest;
      setLatestDownloaded();
    }
    else if (viewId == R.id.button_older_version)
    {
      if (mOlderDownloaded)
        return;
      activeProgressbar = mProgressbarOlder;
      activeButton = mButtonOlder;
      setOlderDownloaded();
    }
    assert activeProgressbar != null;
    testFillProgressBar(activeProgressbar, activeButton);
  }

  private static void testFillProgressBar(ProgressBar progressBar, Button button) {
    progressBar.setVisibility(View.VISIBLE);
    button.setVisibility(View.INVISIBLE);
    ValueAnimator animator = ValueAnimator.ofInt(0, progressBar.getMax());
    animator.setDuration(3000);
    animator.addUpdateListener(animation -> progressBar.setProgress((Integer)animation.getAnimatedValue()));
    animator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        progressBar.setVisibility(View.INVISIBLE);
        button.setVisibility(View.VISIBLE);
        button.setText(R.string.button_download_done);
      }
    });
    animator.start();
  }

  public void setLatestDownloaded()
  {
    mLatestDownloaded = true;
  }

  public void setOlderDownloaded()
  {
    mOlderDownloaded = true;
  }
}
