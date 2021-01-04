package org.dolphinemu.dolphinemu.utils;

import androidx.annotation.Keep;

public interface LoadCallback
{
  @Keep
  void onLoad();

  @Keep
  void onLoadError();
}
