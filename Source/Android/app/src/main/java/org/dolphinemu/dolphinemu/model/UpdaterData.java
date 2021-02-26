package org.dolphinemu.dolphinemu.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class UpdaterData implements Parcelable {
  private final int version;
  private final String downloadUrl;

  public UpdaterData(JSONObject data) throws JSONException
  {
    version = data.getInt("tag_name");
    downloadUrl = data.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");
  }

  public int getVersion()
  {
    return version;
  }

  public String getDownloadUrl()
  {
    return downloadUrl;
  }

  protected UpdaterData(Parcel in) {
    version = in.readInt();
    downloadUrl = in.readString();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(version);
    dest.writeString(downloadUrl);
  }

  @SuppressWarnings("unused")
  public static final Parcelable.Creator<UpdaterData> CREATOR = new Parcelable.Creator<UpdaterData>() {
    @Override
    public UpdaterData createFromParcel(Parcel in) {
      return new UpdaterData(in);
    }

    @Override
    public UpdaterData[] newArray(int size) {
      return new UpdaterData[size];
    }
  };
}
