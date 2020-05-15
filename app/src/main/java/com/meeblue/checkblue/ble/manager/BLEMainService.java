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

package com.meeblue.checkblue.ble.manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.meeblue.checkblue.ble.callback.BLEMainDataCallback;
import com.meeblue.checkblue.ble.struct.Beacon_All_Data_t;
import com.meeblue.checkblue.ble.profile.BleProfileService;
import com.meeblue.checkblue.ble.profile.MEEBLUE_Defines;
import com.meeblue.checkblue.ble.profile.MainBleManager;

import no.nordicsemi.android.ble.data.Data;

import com.meeblue.checkblue.fragment.utils.BLEUtils;

import java.util.UUID;

public class BLEMainService extends BleProfileService implements BLEMainManagerCallbacks {
    @SuppressWarnings("unused")
    private static final String TAG = "BLEMainService";

    private BLEMainManager manager;

    public boolean readState = false;

    /**
     * The last value of a cadence
     */
    private float cadence;
    /**
     * Trip distance in cm
     */
    private long distance;
    /**
     * Stride length in cm
     */
    private Integer strideLength;
    /**
     * Number of steps in the trip
     */
    private int stepsNumber;
    private boolean taskInProgress;
    private final Handler handler = new Handler();

    private final static int NOTIFICATION_ID = 200;
    private final static int OPEN_ACTIVITY_REQ = 0;
    private final static int DISCONNECT_REQ = 1;

    private final LocalBinder binder = new BLEServiceBinder();

    /**
     * This local binder is an interface for the bound activity to operate with the RSC sensor.
     */
    public class BLEServiceBinder extends LocalBinder {
        public BLEMainService getService() {
            return BLEMainService.this;
        }
    }

    public void shutdown() {
        BLEUtils.DEBUG_PRINTF("shutdown");
    }

    @Override
    protected LocalBinder getBinder() {
        return binder;
    }

    @Override
    protected MainBleManager<BLEMainManagerCallbacks> initializeManager() {
        BLEUtils.DEBUG_PRINTF("initializeManager");
        return manager = new BLEMainManager(this);
    }

    protected BluetoothGatt getDefaultGatt() {
        return manager.getBasicGatt();
    }


    @Override
    public void onRSCMeasurementReceived(@NonNull final BluetoothDevice device, final boolean running,
                                         final float instantaneousSpeed, final int instantaneousCadence,
                                         @Nullable final Integer strideLength,
                                         @Nullable final Long totalDistance) {

    }

    @Override
    public void onBatteryLevelChanged(@NonNull final BluetoothDevice device, final int value) {
    }


    public Beacon_All_Data_t getBeacon_All_Data() {
        return this.call_back_temp_data.m_beacon_all_data;
    }

    public void start_connect(BluetoothDevice device) {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothDevice = adapter.getRemoteDevice(device.getAddress());
        manager.connect(bluetoothDevice)
                .useAutoConnect(shouldAutoConnect())
                .retry(0, 100)
                .enqueue();
    }

    public BluetoothGatt get_ble_gatt() {
        return manager.getBasicGatt();

    }

    public String getDeviceName() {
        if (manager.DeviceName.length() >= 0) {
            return manager.DeviceName;
        }
        return manager.getBasicGatt().getDevice().getName();
    }

    public void cancel_connect() {
        binder.disconnect();
    }

    public BluetoothGattCharacteristic getCharacteristicByUUID(UUID ServiceUUID, UUID CharacteristicUUID) {
        return this.manager.getCharacteristicByUUID(ServiceUUID, CharacteristicUUID);
    }

    public void readCharacteristic(UUID ServiceUUID, UUID CharacteristicUUID, BLEMainDataCallback callback) {
        this.manager.readCharacteristic(ServiceUUID, CharacteristicUUID, callback);
    }

    public void writeCharacteristic(UUID ServiceUUID, UUID CharacteristicUUID, Data data, BLEMainDataCallback callback) {
        this.manager.writeCharacteristic(ServiceUUID, CharacteristicUUID, data, callback);
    }

    public void ChangeNotifications(UUID ServiceUUID, UUID CharacteristicUUID, BLEMainDataCallback callback) {

        BluetoothGattDescriptor Descriptor = getCharacteristicByUUID(ServiceUUID, CharacteristicUUID).getDescriptor(MEEBLUE_Defines.NotifyCation_UUID);


        if (Descriptor.getValue() == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) {
            this.manager.disableCharacteristicNotifications(ServiceUUID, CharacteristicUUID, callback);
        } else {
            this.manager.enableCharacteristicNotifications(ServiceUUID, CharacteristicUUID, callback);
        }


    }

    public void enableCharacteristicNotifications(UUID ServiceUUID, UUID CharacteristicUUID, BLEMainDataCallback callback) {
        this.manager.enableCharacteristicNotifications(ServiceUUID, CharacteristicUUID, callback);
    }

    public void disableCharacteristicNotifications(UUID ServiceUUID, UUID CharacteristicUUID, BLEMainDataCallback callback) {
        this.manager.disableCharacteristicNotifications(ServiceUUID, CharacteristicUUID, callback);
    }

    public void read_all_meeblue_data_from_device(BLEMainDataCallback read_call_back) {
        readState = true;
        readCharacteristic(MEEBLUE_Defines.MEEBLUE_MAIN_SERVICE, MEEBLUE_Defines.MEEBLUE_MAIN_BEACON_STATE, new BLEMainDataCallback() {
            @Override
            public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                super.onOptionState(device, data, state);
                if (readState) readState = state;
                if (state) getBeacon_All_Data().Beacon_State.setCombination(data.getValue());
                read_call_back.onDataReadProcess(get_ble_gatt().getDevice(), 10);

                readCharacteristic(MEEBLUE_Defines.MEEBLUE_MAIN_SERVICE, MEEBLUE_Defines.MEEBLUE_MAIN_DEVICE_NAME, new BLEMainDataCallback() {
                    @Override
                    public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                        super.onOptionState(device, data, state);
                        if (readState) readState = state;
                        if (state)
                            BLEUtils.ByteMemcpy(data.getValue(), 0, getBeacon_All_Data().mDevice_Name, 0, data.getValue().length);
                        read_call_back.onDataReadProcess(get_ble_gatt().getDevice(), 20);

                        readCharacteristic(MEEBLUE_Defines.MEEBLUE_PERIPHERAL_SERVICE, MEEBLUE_Defines.MEEBLUE_PERIPHERAL_BATTERY, new BLEMainDataCallback() {
                            @Override
                            public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                                super.onOptionState(device, data, state);
                                if (readState) readState = state;
                                if (state)
                                    getBeacon_All_Data().m_batt_voltage = data.getIntValue(Data.FORMAT_UINT16, 0);
                                read_call_back.onDataReadProcess(get_ble_gatt().getDevice(), 90);

                                readCharacteristic(MEEBLUE_Defines.Device_INFO_SERVER, MEEBLUE_Defines.Device_INFO_SYSTEM_ID, new BLEMainDataCallback() {
                                    @Override
                                    public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                                        super.onOptionState(device, data, state);
                                        if (readState) readState = state;
                                        if (state)
                                            BLEUtils.ByteMemcpy(data.getValue(), 0, getBeacon_All_Data().Systemp_ID, 0, data.getValue().length);
                                        read_call_back.onDataReadProcess(get_ble_gatt().getDevice(), 92);

                                        readCharacteristic(MEEBLUE_Defines.Device_INFO_SERVER, MEEBLUE_Defines.Device_INFO_FIRMWARE_ID, new BLEMainDataCallback() {
                                            @Override
                                            public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                                                super.onOptionState(device, data, state);
                                                if (readState) readState = state;
                                                if (state)
                                                    BLEUtils.ByteMemcpy(data.getValue(), 0, getBeacon_All_Data().Firmware_ID, 0, data.getValue().length);
                                                read_call_back.onDataReadProcess(get_ble_gatt().getDevice(), 96);

                                                readCharacteristic(MEEBLUE_Defines.MEEBLUE_CLOSE_SERVICE, MEEBLUE_Defines.MEEBLUE_CLOSE_STATE, new BLEMainDataCallback() {
                                                    @Override
                                                    public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                                                        super.onOptionState(device, data, state);
                                                        if (readState) readState = state;
                                                        read_call_back.onDataReadProcess(get_ble_gatt().getDevice(), 98);
                                                        if (state) {
                                                            getBeacon_All_Data().Record_Config.setCombination(data.getValue());
                                                        }


                                                        //Sync Time
                                                        getBeacon_All_Data().Beacon_State.m_current_time_stamp = System.currentTimeMillis() / 1000;

                                                        writeCharacteristic(MEEBLUE_Defines.MEEBLUE_MAIN_SERVICE, MEEBLUE_Defines.MEEBLUE_MAIN_BEACON_STATE, new Data(getBeacon_All_Data().Beacon_State.getCombination()), new BLEMainDataCallback() {
                                                            @Override
                                                            public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                                                                super.onOptionState(device, data, state);
                                                                read_call_back.onReadAllFinished(get_ble_gatt().getDevice());
                                                            }

                                                            @Override
                                                            public void onDataSent(@NonNull BluetoothDevice device, @NonNull Data data) {
                                                                super.onDataSent(device, data);
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }
}
