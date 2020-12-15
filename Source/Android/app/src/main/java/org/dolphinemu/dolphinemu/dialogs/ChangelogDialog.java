package org.dolphinemu.dolphinemu.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.dolphinemu.dolphinemu.R;
import org.dolphinemu.dolphinemu.features.settings.model.ChangelogPost;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ChangelogDialog extends DialogFragment {

  private TextView textViewResult;
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
    ViewGroup contents = (ViewGroup) getActivity().getLayoutInflater()
      .inflate(R.layout.dialog_changelog, null);

    textViewResult = contents.findViewById(R.id.changelog);
    Retrofit retrofit = new Retrofit.Builder()
      .baseUrl("https://raw.githubusercontent.com/")
      .addConverterFactory(GsonConverterFactory.create())
      .build();

    JsonGithubChangelog jsonGithubChangelog = retrofit.create(JsonGithubChangelog.class);

    Call<List<ChangelogPost>> call = jsonGithubChangelog.getPost();

    call.enqueue(new Callback<List<ChangelogPost>>() {
      @Override
      public void onResponse(Call<List<ChangelogPost>> call, Response<List<ChangelogPost>> response) {

        if (!response.isSuccessful()){
          textViewResult.setText("Code: " + response.code());
          return;
        }

          List<ChangelogPost> posts = response.body();

          for (ChangelogPost post : posts) {
            String content = "";
            content += "Revamp Version# " + post.getVersion() + "\n";
            content += "Date = " + post.getDate() + "\n";
            content += post.getStatus() + "\n";
            content += "--------------------------------------------" + "\n";
            content += "Changes: " + "\n";
            content += "---------------------------------------------" + "\n";
            content += post.getChanges() + "\n";
            content += "__________________________________________" + "\n\n";

            textViewResult.append(content);
          }
      }

      @Override
      public void onFailure(Call<List<ChangelogPost>> call, Throwable t) {
        textViewResult.setText("[Failed to load Changelog]");
      }
    });

    builder.setView(contents);
    return builder.create();
  }

}
