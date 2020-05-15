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
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.meeblue.checkblue.MyApp;
import com.meeblue.checkblue.ble.callback.BLEMainDataCallback;
import com.meeblue.checkblue.ble.manager.BLE_MESSAGE_CALL_BACK_TYPE;
import com.meeblue.checkblue.ble.profile.BleProfileService;
import com.meeblue.checkblue.ble.profile.MEEBLUE_Defines;
import com.meeblue.checkblue.core.BaseFragment;
import com.meeblue.checkblue.R;
import com.meeblue.checkblue.fragment.utils.BLELocalInfo;
import com.meeblue.checkblue.fragment.utils.BLEUtils;
import com.meeblue.checkblue.fragment.list.SimpleRecyclerAdapter;
import com.meeblue.checkblue.fragment.more.SettingsFragment;
import com.meeblue.checkblue.fragment.utils.StringInputCallback;
import com.meeblue.checkblue.utils.SettingSPUtils;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.widget.actionbar.TitleBar;

import java.util.List;

import butterknife.BindView;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

/**
 * author alvin
 * since 2020-03-17
 */
@Page(name = "GlobalFragment")
public class GlobalFragment extends BaseFragment {

    @BindView(R.id.main_settings)
    Button mMain_settings;

    @BindView(R.id.main_total_count)
    Button mMain_total_count;

    @BindView(R.id.scanning_title)
    TextView scanning_title;



    private int limit_rssi = 100;


    /**
     * @return 返回为 null意为不需要导航栏
     */
    @Override
    protected TitleBar initTitle() {
        return null;
    }


    @Override
    protected void initListeners() {
        super.initListeners();
        mMain_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNewPage(SettingsFragment.class);
            }
        });

        mMain_total_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //XToastUtils.toast("Total Count");
                GlobalFragment.this.refreshLayout.autoRefresh();
            }
        });

        MyApp.set_service_call_back_handler(m_ble_connect_handler);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_global_main;
    }

    @Override
    protected void initViews() {
        initTabControlView();
        initTable();
        scanning_title.setText(R.string.global_scan);
    }

    private void initTabControlView() {
    }

    private void initTable(){
        AbsListView listView = findViewById(R.id.listView);
        listView.setAdapter(mAdapter = new SimpleRecyclerAdapter());
        initTableAdapter();
    }

    @Override
    protected void reflash_tables(boolean anmi)
    {
        super.reflash_tables(anmi);
        mMain_total_count.setText("Total:"+m_ScanList.size());
    }

    @Override
    protected  void onRefreshFinished()
    {
        this.ble_filters.clear();
        startScan(this.ble_filters);
    }

    @Override
    protected  void onLoadMoreFinished()
    {
        startScan(this.ble_filters);
    }

    @Override
    protected void onfindbledevice(List<ScanResult> results) {
        super.onfindbledevice(results);
        handleTheList(results);
    }


    private void handleTheList(List<ScanResult> results)
    {
        for (ScanResult result : results)
        {
            BLELocalInfo TEMP = new BLELocalInfo(result);
            if (limit_rssi < 100 && result.getRssi() < -limit_rssi)
            {
                continue;
            }
            if (!TEMP.IsAlarmBeacon())
            {
                continue;
            }

            if (m_ScanList.contains(TEMP))
            {
                BLELocalInfo Here = m_ScanList.get(m_ScanList.indexOf(TEMP));
                Here.setResult(result);
            }
            else {
                m_ScanList.add(TEMP);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //触发自动刷新
        refreshLayout.autoRefresh();
        start_second_timer();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopScan();
        stop_second_timer();
    }


    @Override
    public void set_fragment_is_aviliable(boolean show) {
        super.set_fragment_is_aviliable(show);
        if (!show)
        {
            stop_second_timer();
            stopScan();
        }
        else
        {
            MyApp.set_service_call_back_handler(m_ble_connect_handler);
            m_ScanList.clear();
            mAdapter.refresh(m_ScanList);
            start_second_timer();
            refreshLayout.autoRefresh();
        }
    }

    @Override
    protected void TimerEverySecond() {
        super.TimerEverySecond();
        mMain_total_count.setText("Total:"+m_ScanList.size());
    }

    @Override
    protected void onTableItemLongClick(int position) {
        super.onTableItemLongClick(position);
    }

    @Override
    protected void onTableItemClick(int position) {
        super.onTableItemClick(position);

        BLELocalInfo temp = m_ScanList.get(position);
        if (temp.getResult().isConnectable())
        {
            this.showLoadingDialog("Trying to connect...");
            MyApp.Main_Service.start_connect(temp.getResult().getDevice());
            BLEUtils.DEBUG_PRINTF("Start connected");
        }
        else{
            this.showErrorDialog("Current device in Non-Connectable mode");
        }


    }


    protected Handler m_ble_connect_handler = new BLE_MAIN_Handler();
    protected Handler messagehandler = new Handler();

    private class BLE_MAIN_Handler extends Handler {
        private BLE_MAIN_Handler() {
        }

        public void handleMessage(Message msg) {
            final int what = msg.what;
            final BleProfileService.HandlerCallBackObject call_back = (BleProfileService.HandlerCallBackObject)msg.obj;

            GlobalFragment.this.messagehandler.post(new Runnable() {
                public void run() {
                    BLE_MESSAGE_CALL_BACK_TYPE callState = BLE_MESSAGE_CALL_BACK_TYPE.values()[what];
                    switch (callState) {
                        case MESSAGE_ID_CONNECTING:
                            GlobalFragment.this.showLoadingDialog("Start connecting...");
                            BLEUtils.DEBUG_PRINTF("Service start connect");
                            break;
                        case MESSAGE_ID_CONNECTED:
                            GlobalFragment.this.showLoadingDialog("Scanning characteristics...");
                            break;
                        case MESSAGE_ID_DISCONNECTING:
                            BLEUtils.DEBUG_PRINTF("Service start MESSAGE_ID_DISCONNECTING");
                            break;
                        case MESSAGE_ID_DISCONNECTED:
                            BLEUtils.DEBUG_PRINTF("Service start MESSAGE_ID_DISCONNECTED");
                            GlobalFragment.this.showErrorDialog("Disconnected from device");
                            break;
                        case MESSAGE_ID_LINK_LOSS:
                            BLEUtils.DEBUG_PRINTF("Service start MESSAGE_ID_LINK_LOSS");
                            GlobalFragment.this.showErrorDialog("Link loss from device");
                            break;
                        case MESSAGE_ID_SERVICE_DISCOVERED:
                            BLEUtils.DEBUG_PRINTF("Service start MESSAGE_ID_SERVICE_DISCOVERED");
                            break;
                        case MESSAGE_ID_DEVICE_READY:
                            BLEUtils.DEBUG_PRINTF("Service start MESSAGE_ID_DEVICE_READY");
                            //GlobalFragment.this.showSuccessDialog("Device ready for option");
                            start_auth(call_back);
                            break;
                        case MESSAGE_ID_NOT_SUPPORT:
                            BLEUtils.DEBUG_PRINTF("Service start MESSAGE_ID_NOT_SUPPORT");
                            showDialogLoader("Current device is not supported");
                            break;
                        case MESSAGE_ID_CONNECT_ERROR:
                            BLEUtils.DEBUG_PRINTF("Service start MESSAGE_ID_CONNECT_ERROR");
                            GlobalFragment.this.showErrorDialog("Failed to connect the device");
                            break;
                        default:

                            break;
                    }
                }
            });
        }
    }

    private void start_auth(BleProfileService.HandlerCallBackObject value)//开始验证设备
    {
        if (MyApp.Main_Service.getCharacteristicByUUID(MEEBLUE_Defines.MEEBLUE_MAIN_SERVICE, MEEBLUE_Defines.MEEBLUE_MAIN_AUTHENTICATION) == null)
        {
            connected_success(value);
            return;
        }
        MyApp.Main_Service.enableCharacteristicNotifications(MEEBLUE_Defines.MEEBLUE_MAIN_SERVICE, MEEBLUE_Defines.MEEBLUE_MAIN_AUTHENTICATION, new BLEMainDataCallback(){

            @Override
            public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                super.onOptionState(device, data, state);
                if (data != null)
                {
                    String Message = data.getStringValue(0);
                    BLEUtils.DEBUG_PRINTF(Message);
                    if (Message.contains("success") || Message.contains("ignore"))
                    {
                        connected_success(value);
                    }
                    else if (Message.contains("failed"))
                    {
                        switch (data.getStringValue(6)) {
                            case "1":
                                showDialogLoader("Wrong authentication code");
                                break;
                            case "2":
                                showDialogLoader("Length of Authentication code incorrect");
                                break;
                            case "3":
                                showDialogLoader("Unknown option");
                                break;
                            default:
                                showDialogLoader("Unknown options");
                                break;
                        }
                    }

                }
            }
        });

        if (SettingSPUtils.getInstance().auto_send_code_key())
        {
            showLoadingDialog("Sending authentication code");
            MyApp.Main_Service.writeCharacteristic(MEEBLUE_Defines.MEEBLUE_MAIN_SERVICE, MEEBLUE_Defines.MEEBLUE_MAIN_AUTHENTICATION, Data.from(MEEBLUE_Defines.START_AUTH_OPTION_CODE+SettingSPUtils.getInstance().default_password_key()), new BLEMainDataCallback(){
                @Override
                public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                    super.onOptionState(device, data, state);
                }
            });
        }
        else{
            GlobalFragment.this.dismissDialog();
            showPasswordInput(new StringInputCallback(){
                @Override
                public void onStringReceived(String input, boolean state) {
                    super.onStringReceived(input, state);
                    if (state)
                    {
                        showLoadingDialog("Sending authentication code");
                        MyApp.Main_Service.writeCharacteristic(MEEBLUE_Defines.MEEBLUE_MAIN_SERVICE, MEEBLUE_Defines.MEEBLUE_MAIN_AUTHENTICATION, Data.from(MEEBLUE_Defines.START_AUTH_OPTION_CODE+input), new BLEMainDataCallback(){
                            @Override
                            public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                                super.onOptionState(device, data, state);
                            }
                        });
                    }
                    else
                    {
                        MyApp.Main_Service.cancel_connect();
                    }
                }
            });
        }
    }

    private void connected_success(BleProfileService.HandlerCallBackObject value)
    {
        dismissDialog();
        openNewPage(MultSetFragment.class);
    }
}
