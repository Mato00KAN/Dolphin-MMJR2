package org.dolphinemu.dolphinemu.utils;

import androidx.annotation.Keep;

public interface CustomCallback
{
  @Keep
  void onLoad();

  @Keep
  void onLoadError();

  @Keep
  default void onDownloadStart() {}

  @Keep
  default void onDownloadProgress(int progress) {}

  @Keep
  default void onDownloadComplete() {}

  @Keep
  default void onDownloadError() {}
}
