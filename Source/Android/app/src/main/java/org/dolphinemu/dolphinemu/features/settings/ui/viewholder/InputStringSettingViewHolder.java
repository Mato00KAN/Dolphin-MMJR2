// SPDX-License-Identifier: GPL-2.0-or-later

package org.dolphinemu.dolphinemu.features.settings.ui.viewholder;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.dolphinemu.dolphinemu.R;
import org.dolphinemu.dolphinemu.features.settings.model.view.InputStringSetting;
import org.dolphinemu.dolphinemu.features.settings.model.view.SettingsItem;
import org.dolphinemu.dolphinemu.features.settings.ui.SettingsAdapter;

public final class InputStringSettingViewHolder extends SettingViewHolder
{
  private InputStringSetting mInputString;
  private SettingsItem mItem;

  private TextView mTextSettingName;
  private TextView mTextSettingDescription;

  private Drawable mDefaultBackground;

  public InputStringSettingViewHolder(View itemView, SettingsAdapter adapter)
  {
    super(itemView, adapter);
  }

  @Override
  protected void findViews(View root)
  {
    mTextSettingName = root.findViewById(R.id.text_setting_name);
    mTextSettingDescription = root.findViewById(R.id.text_setting_description);

    mDefaultBackground = root.getBackground();
  }

  @Override
  public void bind(SettingsItem item)
  {
    mInputString = (InputStringSetting) item;
    mItem = item;

    String inputString = mInputString.getSelectedValue(getAdapter().getSettings());

    itemView.setBackground(mDefaultBackground);
    mTextSettingName.setText(item.getName());

    if (!TextUtils.isEmpty(inputString))
    {
      mTextSettingDescription.setText(inputString);
    }
    else
    {
      mTextSettingDescription.setText(item.getDescription());
    }

    setStyle(mTextSettingName, mItem);
  }

  @Override
  public void onClick(View clicked)
  {
    if (!mItem.isEditable())
    {
      showNotRuntimeEditableError();
      return;
    }

    int position = getAdapterPosition();

    getAdapter().onInputStringClick(mInputString, position);

    setStyle(mTextSettingName, mItem);
  }

  @Nullable @Override
  protected SettingsItem getItem()
  {
    return mItem;
  }
}
