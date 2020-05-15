/*
 * Copyright (C) 2020 meeblue
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.meeblue.checkblue.fragment.more;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.meeblue.checkblue.R;
import com.meeblue.checkblue.core.BaseFragment;
import com.meeblue.checkblue.utils.SettingSPUtils;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.utils.DensityUtils;
import com.xuexiang.xui.utils.ResUtils;
import com.xuexiang.xui.widget.button.switchbutton.SwitchButton;
import com.xuexiang.xui.widget.dialog.materialdialog.DialogAction;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.grouplist.XUICommonListItemView;
import com.xuexiang.xui.widget.grouplist.XUIGroupListView;
import com.xuexiang.xui.widget.tabbar.TabControlView;

import butterknife.BindView;

/**
 * author alvin
 * since 2020-03-17
 */
@Page(name = "Settings")
public class SettingsFragment extends BaseFragment {

    @BindView(R.id.groupListView)
    XUIGroupListView mGroupListView;

    private  XUICommonListItemView itemWithDetail;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_settings;
    }

    @Override
    protected void initViews() {
        initGroupListView();
    }

    private void initGroupListView() {
        XUICommonListItemView itemWithSwitch = mGroupListView.createItemView("Auto send the code:");
        itemWithSwitch.setAccessoryType(XUICommonListItemView.ACCESSORY_TYPE_CUSTOM);

        View view1 = LayoutInflater.from(getActivity()).inflate(R.layout.switch_button_view, null);
        itemWithSwitch.addAccessoryCustomView(view1);

        SwitchButton Switch = view1.findViewById(R.id.sb_default);
        Switch.setChecked(SettingSPUtils.getInstance().auto_send_code_key());
        Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //XToastUtils.toast("checked = " + isChecked);
                SettingSPUtils.getInstance().setauto_send_code_key(isChecked);

            }
        });

        itemWithDetail = mGroupListView.createItemView("Authentication code:");
        itemWithDetail.setDetailText(SettingSPUtils.getInstance().default_password_key());
        itemWithDetail.getDetailTextView().setTextColor(getResources().getColor(R.color.colorPrimary));
        itemWithDetail.getDetailTextView().setTextSize(16);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v instanceof XUICommonListItemView) {
                    //CharSequence text = ((XUICommonListItemView) v).getText();
                    //XToastUtils.toast(text + " is Clicked");
                    if (v == itemWithSwitch)
                    {
                        Switch.setChecked(!Switch.isChecked());
                        SettingSPUtils.getInstance().setauto_send_code_key(Switch.isChecked());
                    }
                    else if (itemWithDetail == v)
                    {
                        showInputDialog();
                    }
                }
            }
        };

        int size = DensityUtils.dp2px(getContext(), 20);
        XUIGroupListView.newSection(getContext())
                .setTitle("\n\nTIP: USE A DEFAULT AUTHENTICATION CODE")
                .setDescription("")
                .setLeftIconSize(size, ViewGroup.LayoutParams.WRAP_CONTENT)
                .addItemView(itemWithSwitch, onClickListener)
                .addItemView(itemWithDetail, onClickListener)
                .addTo(mGroupListView);
    }

    /**
     * 带输入框的对话框
     */
    private void showInputDialog() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.warn_tip)
                .content(R.string.code_tip)
                .inputType(
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_VARIATION_NORMAL
                                | InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .input(
                        getString(R.string.authentication_code),
                        "",
                        false,
                        (new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                //XToastUtils.toast(input.toString());
                            }
                        }))
                .inputRange(6, 6)
                .positiveText(R.string.done)
                .neutralText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //XToastUtils.toast("你输入了:" + dialog.getInputEditText().getText().toString());
                        String INPUT = dialog.getInputEditText().getText().toString();
                        SettingSPUtils.getInstance().setdefault_password_key(INPUT);
                        itemWithDetail.setDetailText(INPUT);
                    }
                })
                .cancelable(false)
                .show();
    }
}
