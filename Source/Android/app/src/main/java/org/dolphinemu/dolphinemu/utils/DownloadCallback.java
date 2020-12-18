package org.dolphinemu.dolphinemu.utils;

import androidx.annotation.Keep;

public interface DownloadCallback
{
  @Keep
  void onDownloadStart();

  @Keep
  default void onDownloadProgress(int progress) {}

  @Keep
  void onDownloadComplete();

  @Keep
  default void onDownloadCancelled() {}

  @Keep
  void onDownloadError();
}
