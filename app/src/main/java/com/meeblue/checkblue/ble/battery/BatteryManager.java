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

package com.meeblue.checkblue.ble.battery;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.meeblue.checkblue.ble.callback.BLEMainDataCallback;
import com.meeblue.checkblue.ble.profile.MEEBLUE_Defines;
import com.meeblue.checkblue.ble.profile.MainBleManager;

import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.common.callback.battery.BatteryLevelDataCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.log.LogContract;

/**
 * The Ble Manager with Battery Service support.
 *
 * @param <T> The profile callbacks type.
 * @see BleManager
 */
public abstract class BatteryManager<T extends BatteryManagerCallbacks> extends MainBleManager<T> {

    /**
     * Last received Battery Level value.
     */
    private Integer batteryLevel;
    private BluetoothGatt basic_gatt;

    public BluetoothGattCharacteristic getCharacteristicByUUID(UUID ServiceUUID, UUID CharacteristicUUID) {
        final BluetoothGattService service = basic_gatt.getService(ServiceUUID);
        BluetoothGattCharacteristic Characteristic = null;
        if (service != null) {
            Characteristic = service.getCharacteristic(CharacteristicUUID);
        }
        return Characteristic;
    }

    /**
     * The manager constructor.
     *
     * @param context context.
     */
    public BatteryManager(final Context context) {
        super(context);
    }

    public void readCharacteristic(UUID ServiceUUID, UUID CharacteristicUUID, BLEMainDataCallback callback) {
        BluetoothGattCharacteristic Characteristic = getCharacteristicByUUID(ServiceUUID, CharacteristicUUID);

        if (Characteristic == null || (Characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) == 0)
            callback.onOptionState(basic_gatt.getDevice(), null, false);
        if (isConnected()) {
            readCharacteristic(Characteristic)
                    .with(callback)
                    .fail(new FailCallback() {
                        @Override
                        public void onRequestFailed(@NonNull BluetoothDevice device, int status) {
                            callback.onOptionState(basic_gatt.getDevice(), null, false);
                        }
                    })
                    .enqueue();
            return;
        }
        callback.onOptionState(basic_gatt.getDevice(), null, false);
    }

    public void writeCharacteristic(UUID ServiceUUID, UUID CharacteristicUUID, Data data, BLEMainDataCallback callback) {
        BluetoothGattCharacteristic Characteristic = getCharacteristicByUUID(ServiceUUID, CharacteristicUUID);
        if (Characteristic == null || (Characteristic.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) == 0)
            callback.onOptionState(basic_gatt.getDevice(), null, false);
        if (isConnected()) {
            writeCharacteristic(Characteristic, data)
                    .with(callback)
                    .fail(new FailCallback() {
                        @Override
                        public void onRequestFailed(@NonNull BluetoothDevice device, int status) {
                            callback.onOptionState(basic_gatt.getDevice(), null, false);
                        }
                    }).enqueue();
            return;
        }
        callback.onOptionState(basic_gatt.getDevice(), null, false);
    }

    public void enableCharacteristicNotifications(UUID ServiceUUID, UUID CharacteristicUUID, BLEMainDataCallback callback) {
        BluetoothGattCharacteristic Characteristic = getCharacteristicByUUID(ServiceUUID, CharacteristicUUID);
        if (Characteristic == null || (Characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0)
            callback.onNotificatioinState(basic_gatt.getDevice(), false);
        if (isConnected()) {
            // If the Battery Level characteristic is null, the request will be ignored
            setNotificationCallback(Characteristic)
                    .with(callback);
            enableNotifications(Characteristic)
                    .done(new SuccessCallback() {
                        @Override
                        public void onRequestCompleted(@NonNull BluetoothDevice device) {
                            callback.onNotificatioinState(basic_gatt.getDevice(), true);
                        }
                    })
                    .fail(new FailCallback() {
                        @Override
                        public void onRequestFailed(@NonNull BluetoothDevice device, int status) {
                            callback.onNotificatioinState(basic_gatt.getDevice(), false);
                        }
                    })
                    .enqueue();
            return;
        }
        callback.onNotificatioinState(basic_gatt.getDevice(), false);
    }

    /**
     * Disables Battery Level notifications on the Server.
     */
    public void disableCharacteristicNotifications(UUID ServiceUUID, UUID CharacteristicUUID, BLEMainDataCallback callback) {
        BluetoothGattCharacteristic Characteristic = getCharacteristicByUUID(ServiceUUID, CharacteristicUUID);
        if (Characteristic == null || (Characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0)
            callback.onNotificatioinState(basic_gatt.getDevice(), false);
        if (isConnected()) {
            disableNotifications(Characteristic)
                    .done(new SuccessCallback() {
                        @Override
                        public void onRequestCompleted(@NonNull BluetoothDevice device) {
                            callback.onNotificatioinState(basic_gatt.getDevice(), true);
                        }
                    })
                    .fail(new FailCallback() {
                        @Override
                        public void onRequestFailed(@NonNull BluetoothDevice device, int status) {
                            callback.onNotificatioinState(basic_gatt.getDevice(), false);
                        }
                    })
                    .enqueue();

            return;

        }
        callback.onNotificatioinState(basic_gatt.getDevice(), false);
    }


    protected abstract class BatteryManagerGattCallback extends BleManager.BleManagerGattCallback {

        @Override
        protected void initialize() {

        }

        @Override
        protected boolean isOptionalServiceSupported(@NonNull final BluetoothGatt gatt) {
            basic_gatt = gatt;
            return true;
        }

        @Override
        protected void onDeviceDisconnected() {
            batteryLevel = null;
            basic_gatt = null;
        }
    }
}
