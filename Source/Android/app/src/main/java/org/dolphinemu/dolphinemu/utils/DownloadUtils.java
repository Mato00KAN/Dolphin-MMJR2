package org.dolphinemu.dolphinemu.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.net.URL;
import java.net.HttpURLConnection;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

public class DownloadUtils implements Runnable
{
  private final Handler mHandler = new Handler(Looper.getMainLooper());
  private File mDownloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
  private DownloadCallback mCallback;
  private final String mUrl;
  private File mFile = null;

  public DownloadUtils(String url)
  {
    mUrl = url;
  }

  public void start()
  {
    Thread downloadThread = new Thread(this);
    downloadThread.start();
  }

  @Override
  public void run() {
    downloadFile(mUrl);
  }

  private void downloadFile(String sUrl)
  {
    try {
      URL url = new URL(sUrl);

      HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
      urlConnection.setRequestMethod("GET");
      urlConnection.connect();

      String filename = "download.apk";
      String fieldContentDisp = urlConnection.getHeaderField("Content-Disposition");
      if (fieldContentDisp != null && fieldContentDisp.contains("filename=")) {
        filename = fieldContentDisp.substring(fieldContentDisp.indexOf("filename=") + 9);
      }
      mFile = new File(mDownloadPath, filename);
      mHandler.post(() -> mCallback.onDownloadStart());

      FileOutputStream fileOutput = new FileOutputStream(mFile);
      InputStream inputStream = urlConnection.getInputStream();

      float totalSize = urlConnection.getContentLength();
      int downloadedSize = 0;

      byte[] buffer = new byte[1024];
      int bufferLength = 0;

      while ((bufferLength = inputStream.read(buffer)) > 0) {
        fileOutput.write(buffer, 0, bufferLength);
        downloadedSize += bufferLength;

        int progress = (int) (downloadedSize / totalSize * 100);
        mHandler.post(() -> mCallback.onDownloadProgress(progress));
      }
      fileOutput.close();
      urlConnection.disconnect();

      mHandler.post(() -> mCallback.onDownloadComplete());
    }
    catch (Exception e)
    {
      e.printStackTrace();
      mHandler.post(() -> mCallback.onDownloadError());
      if (mFile != null)
      {
        mFile.delete();
      }
    }
  }

  public void setCallbackListener(DownloadCallback listener)
  {
    mCallback = listener;
  }

  public void setDownloadPath(String path)
  {
    mDownloadPath = new File(path);
  }
}
