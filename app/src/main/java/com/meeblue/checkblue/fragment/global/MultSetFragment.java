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

package com.meeblue.checkblue.fragment.global;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.meeblue.checkblue.MyApp;
import com.meeblue.checkblue.R;
import com.meeblue.checkblue.ble.struct.ALERT_State_Data_t;
import com.meeblue.checkblue.utils.LocalBroadcastManager;
import com.meeblue.checkblue.ble.callback.BLEMainDataCallback;
import com.meeblue.checkblue.ble.struct.Beacon_All_Data_t;
import com.meeblue.checkblue.ble.profile.MEEBLUE_Defines;
import com.meeblue.checkblue.core.BaseFragment;
import com.meeblue.checkblue.fragment.utils.BLEUtils;
import com.meeblue.checkblue.fragment.utils.CSVUtil;
import com.meeblue.checkblue.fragment.utils.StringInputCallback;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.utils.DensityUtils;
import com.xuexiang.xui.widget.grouplist.XUICommonListItemView;
import com.xuexiang.xui.widget.grouplist.XUIGroupListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import butterknife.BindView;
import no.nordicsemi.android.ble.data.Data;

/**
 * author alvin
 * since 2020-03-17
 */
@Page(name = "Mult-Set")
public class MultSetFragment extends BaseFragment {

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
            addActionRightAction("Refresh");
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        UpdateTableView();
    }

    @Override
    protected void onRightActionClick() {
        UpdateTableView();
        super.onRightActionClick();
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


    private void UpdateTableView() {
        showLoadingDialog("Reading configurations...");
        MyApp.Main_Service.read_all_meeblue_data_from_device(new BLEMainDataCallback() {
            @Override
            public void onDataReadProcess(@NonNull BluetoothDevice device, int percent) {
                super.onDataReadProcess(device, percent);
                showLoadingDialog("Reading...finished:" + percent + "%");
            }

            @Override
            public void onReadAllFinished(@NonNull BluetoothDevice device) {
                super.onReadAllFinished(device);
                dismissDialog();
                UpdateTable_View();
            }
        });
    }

    private XUICommonListItemView CreatItemView(String Title, String Subtitle, int Section, int Row, boolean right) {
        String KEY = "Section:" + Section + "-Row:" + Row;
        XUICommonListItemView itemWithDetailBelow = m_table_hash_map.get(KEY);

        if (itemWithDetailBelow != null) {
            itemWithDetailBelow.setText(Title);
            itemWithDetailBelow.setDetailText(Subtitle);
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
            FrameLayout view = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.acc_right_view, null);
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
        //ToastUtils.toast("Section:" + object.section + "  Row：" + object.row);
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
        } else//更新数据即可
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
        MyApp.Main_Service.cancel_connect();
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
        switch (section) {
            case 0://beacon state
                return 7;
            case 1://record
                return 1;
            case 2://functioin
                return 4;
            case 3://Device information
                return 5;
            case 4://export all configutations
                return 1;
        }
        return 0;
    }

    public int numberOfSectionsInTableView() {
        return 5;
    }


    public String titleForHeaderInSection(int section) {
        switch (section) {
            case 0:
                return "Beacon State Control";

            case 1:
                return "Close Alert Data";

            case 2:
                return "Device Funtion";

            case 3:
                return "Device Information";

            case 6:
                return "Configurations For production";
            default:
                break;
        }
        return "";
    }

    private XUICommonListItemView cellForState(GroupListItemObject indexPath) {
        switch (indexPath.row) {
            case 0:
                return CreatItemView("Device ID:", "" + MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_device_id, indexPath.section, indexPath.row, true);
            case 1:
                if (MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_Connect_State == 0) {
                    return CreatItemView("Keep Connect Max Time:", "No limit", indexPath.section, indexPath.row, true);
                } else {
                    return CreatItemView("Keep Connect Max Time:", MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_Connect_State + " min", indexPath.section, indexPath.row, true);
                }
            case 2:
                return CreatItemView("Signal Limit >=", MyApp.Main_Service.getBeacon_All_Data().Beacon_State.Limit_Rssi + "", indexPath.section, indexPath.row, true);
            case 3:
                return CreatItemView("Alarm Delay Time:", MyApp.Main_Service.getBeacon_All_Data().Beacon_State.Scan_Interval + "", indexPath.section, indexPath.row, true);
            case 4:
                return CreatItemView("Tx Power Code:", MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_txPower + " dBm", indexPath.section, indexPath.row, true);
            case 5:
                if (MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_alarm_type == 0 || (MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_alarm_type & 0x07) == 00) {
                    return CreatItemView("Alarm Type:", "None", indexPath.section, indexPath.row, true);
                } else {
                    String ShowString = "";
                    if ((MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_alarm_type & 0x01) != 0) {
                        ShowString = ShowString+"+LED";
                    }
                    if ((MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_alarm_type & 0x02) != 0) {
                        ShowString = ShowString + "+Buzzer";
                    }
                    if ((MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_alarm_type & 0x04) != 0) {
                        ShowString = ShowString + "+Vibrate";
                    }
                    ShowString = ShowString.substring(1);
                    return CreatItemView("Alarm Type:", ShowString, indexPath.section, indexPath.row, true);
                }
            case 6:
                return CreatItemView("White List Count", MyApp.Main_Service.getBeacon_All_Data().get_white_list_count() + "", indexPath.section, indexPath.row, true);
            default:
                break;
        }
        return null;
    }

    private XUICommonListItemView cellForINformation(GroupListItemObject indexPath) {
        switch (indexPath.row) {
            case 0://name
                return CreatItemView("Device Name:", new String(MyApp.Main_Service.getBeacon_All_Data().mDevice_Name), indexPath.section, indexPath.row, true);

            case 1://batt
                return CreatItemView("Battery Voltage:", MyApp.Main_Service.getBeacon_All_Data().m_batt_voltage + " mv", indexPath.section, indexPath.row, false);

            case 2://Device Time
                return CreatItemView("Time From Device:", BLEUtils.Convert_TimeSeconed(MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_current_time_stamp) + "", indexPath.section, indexPath.row, false);

            case 3://firmware ID
                return CreatItemView("Firmware Version:", new String(MyApp.Main_Service.getBeacon_All_Data().Firmware_ID).toUpperCase(), indexPath.section, indexPath.row, false);
            case 4://Device ID
                return CreatItemView("Device Identifier:", MyApp.Main_Service.get_ble_gatt().getDevice().getAddress().replace(":", "").toUpperCase(), indexPath.section, indexPath.row, false);
            default:
                break;
        }

        return null;
    }

    private XUICommonListItemView cellForAlert(GroupListItemObject indexPath) {

        switch (indexPath.row) {

            case 0://temp
                return CreatItemView("Sync All Alert Data:", MyApp.Main_Service.getBeacon_All_Data().Record_Config.current_max_count+"", indexPath.section, indexPath.row, true);

            default:
                break;
        }

        return null;
    }

    private XUICommonListItemView cellForPeripheral(GroupListItemObject indexPath) {
        switch (indexPath.row) {
            case 0:
                return CreatItemView("Find The Beacon:", "", indexPath.section, indexPath.row, true);
            case 2://1st motion
            {
                BluetoothGattDescriptor Descriptor = MyApp.Main_Service.getCharacteristicByUUID(MEEBLUE_Defines.MEEBLUE_ACC_SERVICE, MEEBLUE_Defines.MEEBLUE_MOTION_1ST).getDescriptor(MEEBLUE_Defines.NotifyCation_UUID);
                if (Descriptor.getValue() == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) {
                    return CreatItemView("INT1 Motion Detect:", "Enabled", indexPath.section, indexPath.row, true);
                }
                return CreatItemView("INT1 Motion Detect:", "Disabled", indexPath.section, indexPath.row, true);
            }

            case 3: {
                BluetoothGattDescriptor Descriptor = MyApp.Main_Service.getCharacteristicByUUID(MEEBLUE_Defines.MEEBLUE_ACC_SERVICE, MEEBLUE_Defines.MEEBLUE_MOTION_2ND).getDescriptor(MEEBLUE_Defines.NotifyCation_UUID);
                if (Descriptor.getValue() == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) {
                    return CreatItemView("INT2 Motion Detect:", "Enabled", indexPath.section, indexPath.row, true);
                }
                return CreatItemView("INT2 Motion Detect:", "Disabled", indexPath.section, indexPath.row, true);
            }

            case 1: {
                BluetoothGattDescriptor Descriptor = MyApp.Main_Service.getCharacteristicByUUID(MEEBLUE_Defines.MEEBLUE_PERIPHERAL_SERVICE, MEEBLUE_Defines.MEEBLUE_PERIPHERAL_BUTTON).getDescriptor(MEEBLUE_Defines.NotifyCation_UUID);
                if (Descriptor.getValue() == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) {
                    return CreatItemView("Button Detect:", "Enabled", indexPath.section, indexPath.row, true);
                }
                return CreatItemView("Button Detect:", "Disabled", indexPath.section, indexPath.row, true);
            }

            default:
                break;
        }

        return null;
    }


    private XUICommonListItemView cellForProduct(GroupListItemObject indexPath) {

        switch (indexPath.row) {
            case 0:
                return CreatItemView("Export All Configurations For Production", "", indexPath.section, indexPath.row, true);

            default:
                break;
        }
        return null;
    }

    private XUICommonListItemView cellForRowAtIndexPath(GroupListItemObject indexPath) {
        switch (indexPath.section) {
            case 0:
                return cellForState(indexPath);
            case 1:
                return cellForAlert(indexPath);
            case 2:
                return cellForPeripheral(indexPath);
            case 3:
                return cellForINformation(indexPath);
            case 4:
                return cellForProduct(indexPath);

            default:
                break;
        }
        return null;
    }


    private void didSelectRowAtIndexPath(GroupListItemObject indexPath) {
        switch (indexPath.section) {
            case 1:
                switch (indexPath.row) {
                    case 0://History
                        openNewPage(HistorySyncFragment.class, "channel", 1);
                        break;
                    default:
                        break;
                }
                break;
            case 2:
                switch (indexPath.row) {
                    case 0://Find The Beacon
                    {
                        showLoadingDialog("Sending command...");
                        MyApp.Main_Service.writeCharacteristic(MEEBLUE_Defines.MEEBLUE_PERIPHERAL_SERVICE, MEEBLUE_Defines.MEEBLUE_PERIPHERAL_BUZZER, Data.opCode((byte) 1), new BLEMainDataCallback() {
                            @Override
                            public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                                super.onOptionState(device, data, state);
                                if (state) {
                                    dismissDialog();
                                } else {
                                    showErrorDialog("Command send failed");
                                }
                            }
                        });
                    }
                    break;
                    case 2://1st Channel Motion Detect
                    {
                        showLoadingDialog("Setting...");
                        MyApp.Main_Service.ChangeNotifications(MEEBLUE_Defines.MEEBLUE_ACC_SERVICE, MEEBLUE_Defines.MEEBLUE_MOTION_1ST, new BLEMainDataCallback() {
                            @Override
                            public void onNotificatioinState(@NonNull BluetoothDevice device, boolean state) {
                                super.onNotificatioinState(device, state);
                                if (state) {
                                    dismissDialog();
                                    UpdateTable_View();
                                } else {
                                    showErrorDialog("Failed to notify INT1");
                                }
                            }

                            @Override
                            public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
                                super.onDataReceived(device, data);
                                if (data != null) {
                                    showSuccessDialog("INT1 trigger detected");
                                }
                            }
                        });
                    }
                    break;

                    case 3://2st Channel Motion Detect
                    {
                        showLoadingDialog("Setting...");
                        MyApp.Main_Service.ChangeNotifications(MEEBLUE_Defines.MEEBLUE_ACC_SERVICE, MEEBLUE_Defines.MEEBLUE_MOTION_2ND, new BLEMainDataCallback() {
                            @Override
                            public void onNotificatioinState(@NonNull BluetoothDevice device, boolean state) {
                                super.onNotificatioinState(device, state);
                                if (state) {
                                    dismissDialog();
                                    UpdateTable_View();
                                } else {
                                    showErrorDialog("Failed to notify INT2");
                                }
                            }

                            @Override
                            public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
                                super.onDataReceived(device, data);
                                if (data != null) {
                                    showSuccessDialog("INT2 trigger detected");
                                }
                            }
                        });
                    }
                    break;
                    case 1://Button Detect
                    {
                        showLoadingDialog("Setting...");
                        MyApp.Main_Service.ChangeNotifications(MEEBLUE_Defines.MEEBLUE_PERIPHERAL_SERVICE, MEEBLUE_Defines.MEEBLUE_PERIPHERAL_BUTTON, new BLEMainDataCallback() {
                            @Override
                            public void onNotificatioinState(@NonNull BluetoothDevice device, boolean state) {
                                super.onNotificatioinState(device, state);
                                if (state) {
                                    dismissDialog();
                                    UpdateTable_View();
                                } else {
                                    showErrorDialog("Failed to notify button");
                                }
                            }

                            @Override
                            public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
                                super.onDataReceived(device, data);
                                if (data != null) {
                                    if (data.getIntValue(Data.FORMAT_UINT8, 0) == 0x02) {
                                        showSuccessDialog("Button long press trigger detected");
                                    } else if (data.getIntValue(Data.FORMAT_UINT8, 0) == 0x01) {
                                        showSuccessDialog("Button press trigger detected");
                                    }
                                }
                            }
                        });
                    }
                    break;
                    default:
                        break;
                }
                break;
            case 3:
                switch (indexPath.row) {
                    case 0://Name
                    {
                        showInputDialog("length from 1 to 15", InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_VARIATION_NORMAL
                                | InputType.TYPE_TEXT_FLAG_CAP_WORDS, new StringInputCallback() {
                            @Override
                            public void onStringReceived(String input, boolean state) {
                                super.onStringReceived(input, state);
                                if (state && input.length() >= 1 && input.length() <= 15) {
                                    Beacon_All_Data_t temp = MyApp.Main_Service.getBeacon_All_Data();
                                    BLEUtils.ByteMemset(temp.mDevice_Name, 0x00, 20);
                                    BLEUtils.ByteMemcpy(input.getBytes(), 0, temp.mDevice_Name, 0, input.length());
                                    Data_ReloadData(true);
                                } else {
                                    showErrorDialog("Data entered is not acceptable");
                                }
                            }
                        });
                    }
                    break;
                    case 1://Battery Voltage
                        break;
                    case 2://Device Temperture Data
                        break;
                    case 3://Device Humidity Data
                        break;
                    default:
                        break;
                }
                break;
            case 0:
                switch (indexPath.row) {
                    case 0://Beacon State
                    {
                        if (MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_device_id != 0) {
                            showErrorDialog("Device ID Can Not Be Changed");
                            return;
                        }

                        showInputDialog("Range from 1 to 4294967295", InputType.TYPE_CLASS_NUMBER, new StringInputCallback() {
                            @Override
                            public void onStringReceived(String input, boolean state) {
                                super.onStringReceived(input, state);
                                long InputData = 0;
                                long temp = 1294967295;
                                if (state) InputData = Long.parseLong(input);
                                if (state && InputData >= 1 && InputData <= temp) {
                                    MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_device_id = InputData;
                                    Data_ReloadData(false);
                                } else {
                                    showErrorDialog("Data entered is not acceptable");
                                }
                            }
                        });
                    }
                        break;
                    case 1://Keep Connect Max Time
                    {
                        showInputDialog("Range from 0 to 255", InputType.TYPE_CLASS_NUMBER, new StringInputCallback() {
                            @Override
                            public void onStringReceived(String input, boolean state) {
                                super.onStringReceived(input, state);
                                int InputData = 0;
                                if (state) InputData = Integer.parseInt(input);
                                if (state && InputData >= 0 && InputData <= 255) {
                                    MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_Connect_State = InputData;
                                    Data_ReloadData(false);
                                } else {
                                    showErrorDialog("Data entered is not acceptable");
                                }
                            }
                        });
                    }
                    break;
                    case 2://Signal Limit
                    {
                        showInputDialog("Range from -100 to -40", InputType.TYPE_CLASS_TEXT, new StringInputCallback() {
                            @Override
                            public void onStringReceived(String input, boolean state) {
                                super.onStringReceived(input, state);
                                int InputData = 0;
                                if (state) InputData = Integer.parseInt(input);
                                if (state && InputData >= -100 && InputData <= -40) {
                                    MyApp.Main_Service.getBeacon_All_Data().Beacon_State.Limit_Rssi = InputData;
                                    Data_ReloadData(false);
                                } else {
                                    showErrorDialog("Data entered is not acceptable");
                                }
                            }
                        });
                    }
                    break;
                    case 3://Alarm Delay
                    {
                        showInputDialog("Range from 1 to 30", InputType.TYPE_CLASS_NUMBER, new StringInputCallback() {
                            @Override
                            public void onStringReceived(String input, boolean state) {
                                super.onStringReceived(input, state);
                                int InputData = 0;
                                if (state) InputData = Integer.parseInt(input);
                                if (state && InputData >= 1 && InputData <= 30) {
                                    MyApp.Main_Service.getBeacon_All_Data().Beacon_State.Scan_Interval = InputData;
                                    Data_ReloadData(false);
                                } else {
                                    showErrorDialog("Data entered is not acceptable");
                                }
                            }
                        });
                    }
                    break;
                    case 4://Tx Power Code
                    {
                        showInputDialog("Input aviliable int value", InputType.TYPE_CLASS_TEXT, new StringInputCallback() {
                            @Override
                            public void onStringReceived(String input, boolean state) {
                                super.onStringReceived(input, state);
                                int InputData = 0;
                                if (state) InputData = Integer.parseInt(input);
                                if (state && BLEUtils.isByteValue(InputData)) {
                                    MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_txPower = InputData;
                                    Data_ReloadData(false);
                                } else {
                                    showErrorDialog("Data entered is not acceptable");
                                }
                            }
                        });
                    }
                    break;

                    case 5://Trigger Mode Adv Time
                    {

                    }
                    break;
                    case 6:
                    {
                        ;
                    }
                    break;
                    default:
                        break;
                }
                break;
            case 4:
                if (indexPath.row == 0) {
                    ExportAllConfigure();
                }
                break;
            default:
                break;
        }
    }

    protected void Data_ReloadData(boolean Name) {
        showLoadingDialog("Trying to configure...");

        if (Name) {
            MyApp.Main_Service.writeCharacteristic(MEEBLUE_Defines.MEEBLUE_MAIN_SERVICE, MEEBLUE_Defines.MEEBLUE_MAIN_DEVICE_NAME, Data.from(new String(MyApp.Main_Service.getBeacon_All_Data().mDevice_Name)), new BLEMainDataCallback() {
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
        } else {
            ALERT_State_Data_t Beacon_state = MyApp.Main_Service.getBeacon_All_Data().Beacon_State;

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
    }

    private void ExportAllConfigure() {
        ArrayList<String> export = new ArrayList<String>();
        BLEUtils.DEBUG_PRINTF("Here");
        showLoadingDialog("Trying to generate csv file");

        Beacon_All_Data_t ALL_DATA = MyApp.Main_Service.getBeacon_All_Data();

        String ALL = "ID" + CSVUtil.csv_segmentation_symbol + "Service UUID" + CSVUtil.csv_segmentation_symbol + "Characteristic UUID" + CSVUtil.csv_segmentation_symbol + "Hex/String Value" + CSVUtil.csv_segmentation_symbol + "Length";
        export.add(ALL);


        String TEMPSTR = BLEUtils.byteToStringValue(ALL_DATA.Firmware_ID, false);
        //firmware version
        export.add(1 + CSVUtil.csv_segmentation_symbol + MEEBLUE_Defines.Device_INFO_SERVER.toString().toUpperCase() + CSVUtil.csv_segmentation_symbol + MEEBLUE_Defines.Device_INFO_FIRMWARE_ID.toString().toUpperCase() + CSVUtil.csv_segmentation_symbol + TEMPSTR + CSVUtil.csv_segmentation_symbol + TEMPSTR.length());

        //Name
        TEMPSTR = new String(ALL_DATA.mDevice_Name);

        export.add(2 + CSVUtil.csv_segmentation_symbol + MEEBLUE_Defines.MEEBLUE_MAIN_SERVICE.toString().toUpperCase() + CSVUtil.csv_segmentation_symbol + MEEBLUE_Defines.MEEBLUE_MAIN_DEVICE_NAME.toString().toUpperCase() + CSVUtil.csv_segmentation_symbol + TEMPSTR + CSVUtil.csv_segmentation_symbol + TEMPSTR.length());

        //State
        TEMPSTR = BLEUtils.byteToStringValue(ALL_DATA.Beacon_State.getCombination(), false);
        export.add(3 + CSVUtil.csv_segmentation_symbol + MEEBLUE_Defines.MEEBLUE_MAIN_SERVICE.toString().toUpperCase() + CSVUtil.csv_segmentation_symbol + MEEBLUE_Defines.MEEBLUE_MAIN_BEACON_STATE.toString().toUpperCase() + CSVUtil.csv_segmentation_symbol + TEMPSTR + CSVUtil.csv_segmentation_symbol + TEMPSTR.length());


        String File = CSVUtil.exportCsv(CSVUtil.get_configuration_file_path_by_mac(requireContext(), MyApp.Main_Service.get_ble_gatt().getDevice().getAddress().replace(":", "")), export);
        if (File != null) {
            dismissDialog();
            CSVUtil.share_file(requireContext(), File);
        } else {
            showErrorDialog("Failed to export");
        }

        BLEUtils.DEBUG_PRINTF("Here1");
    }

    protected void UpdateTable_View() {
        //(self.m_table_view reloadData);
        initGroupListView();
    }

    @Override
    public void onResume() {
        super.onResume();
        UpdateTable_View();
    }
}
