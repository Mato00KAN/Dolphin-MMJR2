package org.dolphinemu.dolphinemu.dialogs;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.dolphinemu.dolphinemu.R;
import org.dolphinemu.dolphinemu.utils.Log;
import org.dolphinemu.dolphinemu.utils.UpdaterUtils;

public final class ChangelogDialog extends DialogFragment
{
  private ViewGroup mViewGroup;
  private ProgressBar mLoading;
  private TextView mText;

  public static ChangelogDialog newInstance()
  {
    return new ChangelogDialog();
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(),
      R.style.DolphinDialogBase);
    mViewGroup = (ViewGroup) getActivity().getLayoutInflater()
      .inflate(R.layout.dialog_changelog, null);

    mLoading = mViewGroup.findViewById(R.id.changelog_loading);
    mText = mViewGroup.findViewById(R.id.changelog_text);

    RequestQueue queue = Volley.newRequestQueue(getContext());

    JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, UpdaterUtils.URL, null,
      response ->
      {
        try
        {
          populateText(response);
          onLoad();
        }
        catch (Exception e)
        {
          Log.error(e.getMessage());
          onLoadError();
        }
      },
      error -> onLoadError());
    queue.add(jsonRequest);

    builder.setView(mViewGroup);
    return builder.create();
  }

  private void populateText(JSONArray response) throws JSONException
  {
    for (int i = 0; i < response.length(); i++)
    {
      mText.append(getString(R.string.changelog_section,
        response.getJSONObject(i).getInt("tag_name"),
        response.getJSONObject(i).getString("published_at").substring(0, 10),
        (i == 0) ? "[Latest]\n" : (i == 1) ? "[Previous]\n" : "",
        response.getJSONObject(i).getString("body")
        ));
    }
  }

  public void onLoad()
  {
    mLoading.setVisibility(View.INVISIBLE);
    mText.setVisibility(View.VISIBLE);
  }

  public void onLoadError()
  {
    TextView textError = mViewGroup.findViewById(R.id.changelog_error);
    mLoading.setVisibility(View.INVISIBLE);
    textError.setVisibility(View.VISIBLE);
  }
}
