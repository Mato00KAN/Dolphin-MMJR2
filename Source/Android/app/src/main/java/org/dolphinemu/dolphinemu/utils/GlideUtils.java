// SPDX-License-Identifier: GPL-2.0-or-later

package org.dolphinemu.dolphinemu.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.dolphinemu.dolphinemu.R;
import org.dolphinemu.dolphinemu.features.settings.model.BooleanSetting;
import org.dolphinemu.dolphinemu.model.GameFile;

import java.io.File;

public class GlideUtils
{
  public static void loadGameBanner(ImageView imageView, GameFile gameFile)
  {
    Context context = imageView.getContext();
    int[] vector = gameFile.getBanner();
    int width = gameFile.getBannerWidth();
    int height = gameFile.getBannerHeight();
    if (width > 0 && height > 0)
    {
      Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
      bitmap.setPixels(vector, 0, width, 0, 0, width, height);
      Glide.with(context)
              .load(bitmap)
              .centerCrop()
              .into(imageView);
    }
    else
    {
      Glide.with(context)
              .load(R.drawable.no_banner)
              .centerCrop()
              .into(imageView);
    }
  }

  public static void loadGameCover(ImageView imageView, GameFile gameFile)
  {
    Context context = imageView.getContext();
    File cover = new File(gameFile.getCustomCoverPath());
    if (cover.exists())
    {
      Glide.with(context)
              .load(cover)
              .centerCrop()
              .error(R.drawable.no_banner)
              .into(imageView);
    }
    else if ((cover = new File(gameFile.getCoverPath())).exists())
    {
      Glide.with(context)
              .load(cover)
              .centerCrop()
              .error(R.drawable.no_banner)
              .into(imageView);
    }
    // GameTDB has a pretty close to complete collection for US/EN covers. First pass at getting
    // the cover will be by the disk's region, second will be the US cover, and third EN.
    else if (BooleanSetting.MAIN_USE_GAME_COVERS.getBooleanGlobal())
    {
      Glide.with(context)
              .load(CoverHelper.buildGameTDBUrl(gameFile, CoverHelper.getRegion(gameFile)))
              .centerCrop()
              .listener(new RequestListener<Drawable>()
              {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model,
                        Target<Drawable> target, boolean isFirstResource)
                {
                  return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource,
                        Object model, Target<Drawable> target,
                        DataSource dataSource, boolean isFirstResource)
                {
                  CoverHelper.saveCover(
                          ((BitmapDrawable) resource).getBitmap(),
                          gameFile.getCoverPath());
                  return false;
                }
              })
              .error(
                      Glide.with(context)
                              .load(CoverHelper.buildGameTDBUrl(gameFile, "US"))
                              .centerCrop()
                              .listener(new RequestListener<Drawable>()
                              {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e,
                                        Object model, Target<Drawable> target,
                                        boolean isFirstResource)
                                {
                                  return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource,
                                        Object model, Target<Drawable> target,
                                        DataSource dataSource, boolean isFirstResource)
                                {
                                  CoverHelper.saveCover(
                                          ((BitmapDrawable) resource).getBitmap(),
                                          gameFile.getCoverPath());
                                  return false;
                                }
                              })
                              .error(
                                      Glide.with(context)
                                              .load(CoverHelper.buildGameTDBUrl(gameFile, "EN"))
                                              .centerCrop()
                                              .listener(new RequestListener<Drawable>()
                                              {
                                                @Override
                                                public boolean onLoadFailed(
                                                        @Nullable GlideException e,
                                                        Object model, Target<Drawable> target,
                                                        boolean isFirstResource)
                                                {
                                                  return false;
                                                }

                                                @Override
                                                public boolean onResourceReady(Drawable resource,
                                                        Object model, Target<Drawable> target,
                                                        DataSource dataSource,
                                                        boolean isFirstResource)
                                                {
                                                  CoverHelper.saveCover(
                                                          ((BitmapDrawable) resource).getBitmap(),
                                                          gameFile.getCoverPath());
                                                  return false;
                                                }
                                              })
                                              .error(R.drawable.no_banner)
                              )
              )
              .into(imageView);
    }
    else
    {
      Glide.with(context)
              .load(R.drawable.no_banner)
              .centerCrop()
              .into(imageView);
    }
  }
}
