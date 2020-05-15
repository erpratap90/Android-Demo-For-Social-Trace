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

package com.meeblue.checkblue.ble.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.callback.DataSentCallback;
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

@SuppressWarnings("ConstantConditions")
public abstract class BLEMainDataCallback implements ProfileDataCallback, DataSentCallback {


    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        onOptionState(device, data,true);
    }

    @Override
    public void onDataSent(@NonNull BluetoothDevice device, @NonNull Data data) {
        onOptionState(device, data,true);
    }

    public void onOptionState(@NonNull final BluetoothDevice device, Data data, boolean state)
    {

    }

    public void onNotificatioinState(@NonNull final BluetoothDevice device, boolean state)
    {

    }

    public void onDataReadProcess(@NonNull final BluetoothDevice device, int percent)
    {

    }

    public void onReadAllFinished(@NonNull final BluetoothDevice device)
    {

    }
}
