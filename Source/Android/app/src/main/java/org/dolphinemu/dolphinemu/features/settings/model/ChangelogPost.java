package org.dolphinemu.dolphinemu.features.settings.model;


import com.google.gson.annotations.SerializedName;

public class ChangelogPost {


  @SerializedName("Version")
  private int version;

  @SerializedName("Status")
  private String status;

  @SerializedName("Date")
  private String date;

  @SerializedName("Changes")
  private String changes;


  public int getVersion() {
    return version;
  }

  public String getChanges() {
    return changes;
  }

  public String getDate() {
    return date;

  }

  public String getStatus() {
    return status;

  }

}
