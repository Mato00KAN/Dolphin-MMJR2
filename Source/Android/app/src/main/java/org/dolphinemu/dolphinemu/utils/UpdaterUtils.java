package org.dolphinemu.dolphinemu.utils;

import androidx.annotation.Nullable;

import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

public class UpdaterUtils
{
  private static final String mURL = "https://api.npoint.io/c43ee26a63ee41e7c3e5";
  private static JSONObject jsonData;
  private static int errorLevel = 0;

  public static void init()
  {
    errorLevel = 0;
    jsonData = getJsonFromUrl(mURL);
  }

  public static boolean getErrorLevel()
  {
    return errorLevel == 0;
  }

  public static int getLatestVersion()
  {
    if (getErrorLevel())
    {
      try
      {
        return jsonData.getJSONObject("build").getInt("latest");
      }
      catch (Exception e) { errorLevel += 1; }
    }
    return 0;
  }

  public static int getOlderVersion()
  {
    if (getErrorLevel())
    {
      try
      {
        return jsonData.getJSONObject("build").getInt("older");
      }
      catch (Exception ignored) {}
    }
    return 0;
  }

  @Nullable
  public static String getLatestUrl()
  {
    if (getErrorLevel())
    {
      try
      {
        return jsonData.getJSONObject("url").getString("latest");
      }
      catch (Exception ignored) { errorLevel += 1; }
    }
    return null;
  }

  @Nullable
  public static String getOlderUrl()
  {
    if (getErrorLevel())
    {
      try
      {
        return jsonData.getJSONObject("url").getString("older");
      }
      catch (Exception ignored) { errorLevel += 1; }
    }
    return null;
  }

  @Nullable
  public static String getStringFromUrl(String url)
  {
    try (InputStream inputStream = new URL(url).openStream())
    {
      ByteArrayOutputStream result = new ByteArrayOutputStream();
      byte[] buffer = new byte[256];
      int length;
      while ((length = inputStream.read(buffer)) != -1) {
        result.write(buffer, 0, length);
      }

      return result.toString("UTF-8");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      errorLevel += 1;
    }
    return null;
  }

  @Nullable
  public static JSONObject getJsonFromUrl(String url)
  {
    if (getErrorLevel())
    {
      try
      {
        return new JSONObject(getStringFromUrl(url));
      }
      catch (Exception e)
      {
        e.printStackTrace();
        errorLevel += 1;
      }
    }
    return null;
  }
}
