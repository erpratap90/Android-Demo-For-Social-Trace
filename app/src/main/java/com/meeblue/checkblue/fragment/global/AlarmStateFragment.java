/*
 * Copyright (C) 2020 xuexiangjys(xuexiangjys@163.com)
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

package com.meeblue.checkblue.fragment.global;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.meeblue.checkblue.MyApp;
import com.meeblue.checkblue.R;

import com.meeblue.checkblue.ble.callback.BLEMainDataCallback;
import com.meeblue.checkblue.ble.profile.MEEBLUE_Defines;
import com.meeblue.checkblue.ble.struct.ALERT_State_Data_t;
import com.meeblue.checkblue.ble.struct.Beacon_All_Data_t;
import com.meeblue.checkblue.core.BaseFragment;
import com.meeblue.checkblue.utils.LocalBroadcastManager;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.utils.DensityUtils;
import com.xuexiang.xui.widget.grouplist.XUICommonListItemView;
import com.xuexiang.xui.widget.grouplist.XUIGroupListView;

import java.util.HashMap;

import butterknife.BindView;
import no.nordicsemi.android.ble.data.Data;

/**
 * author alvin
 * since 2020-03-17
 */
@Page(name = "Alarm State")
public class AlarmStateFragment extends BaseFragment {

    @BindView(R.id.groupListView)
    XUIGroupListView mGroupListView;

    private boolean OnlyLoad_Once = false;

    private HashMap<String, XUICommonListItemView> m_table_hash_map = new HashMap<String, XUICommonListItemView>();

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_settings;
    }

    @Override
    protected void initViews() {

        if (MyApp.Main_Service.get_ble_gatt().getService(MEEBLUE_Defines.MEEBLUE_MAIN_SERVICE) != null)
            addActionRightAction("Reset");
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        UpdateTable_View();
    }

    @Override
    protected void onRightActionClick() {
        super.onRightActionClick();

        showLoadingDialog("Trying to configure...");

        Beacon_All_Data_t ALL = MyApp.Main_Service.getBeacon_All_Data();
        ALERT_State_Data_t Beacon_state = ALL.Beacon_State;
        Beacon_state.m_alarm_type = 0x00;

        MyApp.Main_Service.writeCharacteristic(MEEBLUE_Defines.MEEBLUE_MAIN_SERVICE, MEEBLUE_Defines.MEEBLUE_MAIN_BEACON_STATE, new Data(Beacon_state.getCombination()), new BLEMainDataCallback() {
            @Override
            public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                super.onOptionState(device, data, state);
                if (state) {
                    dismissDialog();
                    UpdateTable_View();
                } else {
                    showErrorDialog("Configure failed");
                }
            }
        });
    }

    public class GroupListItemObject extends Object {
        public int section = 0;
        public int row = 0;
        public boolean right = false;

        GroupListItemObject(int s, int r, boolean rt) {
            super();
            section = s;
            row = r;
            right = rt;
        }
    }

    private XUICommonListItemView CreatItemView(String Title, String Subtitle, int Section, int Row, boolean right) {
        String KEY = "Section:" + Section + "-Row:" + Row;
        XUICommonListItemView itemWithDetailBelow = m_table_hash_map.get(KEY);

        if (itemWithDetailBelow != null) {
            itemWithDetailBelow.setText(Title);
            itemWithDetailBelow.setDetailText(Subtitle);
            if (right) {
                itemWithDetailBelow.setAccessoryType(XUICommonListItemView.ACCESSORY_TYPE_CUSTOM);
                FrameLayout view = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.right_select_view, null);
                itemWithDetailBelow.addAccessoryCustomView(view);
            } else {
                itemWithDetailBelow.setAccessoryType(XUICommonListItemView.ACCESSORY_TYPE_NONE);
            }

            return itemWithDetailBelow;
        }

        itemWithDetailBelow = mGroupListView.createItemView(Title);
        itemWithDetailBelow.setOrientation(XUICommonListItemView.HORIZONTAL);
        itemWithDetailBelow.setDetailText(Subtitle);
        itemWithDetailBelow.setTag(new GroupListItemObject(Section, Row, right));

        itemWithDetailBelow.getTextView().setTextSize(15);
        itemWithDetailBelow.getDetailTextView().setTextColor(getResources().getColor(R.color.colorPrimary));
        itemWithDetailBelow.getDetailTextView().setTextSize(15);

        if (right) {
            itemWithDetailBelow.setAccessoryType(XUICommonListItemView.ACCESSORY_TYPE_CUSTOM);
            FrameLayout view = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.right_select_view, null);
            itemWithDetailBelow.addAccessoryCustomView(view);
        } else {
            itemWithDetailBelow.setAccessoryType(XUICommonListItemView.ACCESSORY_TYPE_NONE);
        }
        m_table_hash_map.put(KEY, itemWithDetailBelow);
        return itemWithDetailBelow;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v instanceof XUICommonListItemView) {
                OnTableItemClick((GroupListItemObject) v.getTag());
            }
        }
    };


    private void OnTableItemClick(GroupListItemObject object) {
        didSelectRowAtIndexPath(object);
    }

    private void initGroupListView() {
        int size = DensityUtils.dp2px(getContext(), 20);

        if (!OnlyLoad_Once) {
            for (int section = 0; section < numberOfSectionsInTableView(); section++) {
                String Title = titleForHeaderInSection(section);
                XUIGroupListView.Section temp = XUIGroupListView.newSection(getContext());
                temp.setTitle(Title);
                temp.setDescription("");
                for (int row = 0; row < numberOfRowsInSection(section); row++) {
                    XUICommonListItemView TempView = cellForRowAtIndexPath(new GroupListItemObject(section, row, false));
                    if (TempView != null) temp.addItemView(TempView, onClickListener);
                }
                temp.addTo(mGroupListView);
            }
        } else
        {
            for (int section = 0; section < numberOfSectionsInTableView(); section++) {
                for (int row = 0; row < numberOfRowsInSection(section); row++) {
                    cellForRowAtIndexPath(new GroupListItemObject(section, row, false));
                }
            }
        }
        OnlyLoad_Once = true;
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(BLEStateBroadcastReceiver);
        super.onDestroyView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(BLEStateBroadcastReceiver, new IntentFilter(MEEBLUE_Defines.BROADCAST_SERVICES_DISCONNECTED));
    }

    private final BroadcastReceiver BLEStateBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            popToBack();
        }
    };


    public int numberOfRowsInSection(int section) {
        return 3;
    }


    public int numberOfSectionsInTableView() {
        return 1;
    }


    public String titleForHeaderInSection(int section) {
        switch (section) {
            case 0:
                return "\nSelect Alarm Type When Closed To Each Other";
            default:
                break;
        }
        return "";
    }

    private XUICommonListItemView cellForAlarmType(GroupListItemObject indexPath) {
        switch (indexPath.row) {
            case 0://LED
                return CreatItemView("LED", "", indexPath.section, indexPath.row, ((MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_alarm_type & 0x01) != 0));

            case 1://Buzzer
                return CreatItemView("Buzzer", "", indexPath.section, indexPath.row, ((MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_alarm_type & 0x02) != 0));

            case 2://
                return CreatItemView("Vibrate", "", indexPath.section, indexPath.row, ((MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_alarm_type & 0x04) != 0));
            default:
                break;
        }

        return null;
    }


    private XUICommonListItemView cellForRowAtIndexPath(GroupListItemObject indexPath) {
        switch (indexPath.section) {
            case 0:
                return cellForAlarmType(indexPath);
        }
        return null;
    }


    private void didSelectRowAtIndexPath(GroupListItemObject indexPath) {

        switch (indexPath.section) {
            case 0:
                switch (indexPath.row) {
                    case 0://LED
                        MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_alarm_type ^= 0x01;
                        Data_ReloadData(false);
                        break;
                    case 1://Buzzer
                        MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_alarm_type ^= 0x02;
                        Data_ReloadData(false);
                        break;
                    case 2://Vibrate
                        MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_alarm_type ^= 0x04;
                        Data_ReloadData(false);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    protected void Data_ReloadData(boolean Name) {
        showLoadingDialog("Trying to configure...");
        ALERT_State_Data_t Beacon_State = MyApp.Main_Service.getBeacon_All_Data().Beacon_State;

        MyApp.Main_Service.writeCharacteristic(MEEBLUE_Defines.MEEBLUE_MAIN_SERVICE, MEEBLUE_Defines.MEEBLUE_MAIN_BEACON_STATE, new Data(Beacon_State.getCombination()), new BLEMainDataCallback() {
            @Override
            public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                super.onOptionState(device, data, state);
                if (state) {
                    dismissDialog();
                    UpdateTable_View();
                } else {
                    showErrorDialog("Configure failed");
                }
            }
        });
    }

    protected void UpdateTable_View() {
        initGroupListView();
    }

    @Override
    public void onResume() {
        super.onResume();
        UpdateTable_View();
    }
}
