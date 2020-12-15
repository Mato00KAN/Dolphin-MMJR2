package org.dolphinemu.dolphinemu.dialogs;

import org.dolphinemu.dolphinemu.features.settings.model.ChangelogPost;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface JsonGithubChangelog {


  @GET("Darwin-Rist/Releases/master/README.md")
  Call<List<ChangelogPost>> getPost();
}
