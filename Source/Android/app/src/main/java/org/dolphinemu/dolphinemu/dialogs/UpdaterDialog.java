package org.dolphinemu.dolphinemu.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.dolphinemu.dolphinemu.R;
import org.dolphinemu.dolphinemu.utils.Log;

public final class UpdaterDialog extends DialogFragment implements View.OnClickListener
{
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
    ViewGroup contents = (ViewGroup) getActivity().getLayoutInflater()
            .inflate(R.layout.dialog_updater, null);

    Button buttonLatest = contents.findViewById(R.id.button_latest_update);
    Button buttonOlder = contents.findViewById(R.id.button_older_update);
    buttonLatest.setOnClickListener(this);
    buttonOlder.setOnClickListener(this);

    builder.setView(contents);
    return builder.create();
  }

  @Override
  public void onClick(View view) {
    int viewId = view.getId();

    if (viewId == R.id.button_latest_update)
    {

    } else if (viewId == R.id.button_older_update)
    {

    }


  }
}
