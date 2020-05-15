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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.meeblue.checkblue.ble.battery.BatteryManager;
import com.meeblue.checkblue.ble.callback.BLEMainDataCallback;
import com.meeblue.checkblue.ble.profile.MEEBLUE_Defines;

import java.util.UUID;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.common.callback.rsc.RunningSpeedAndCadenceMeasurementDataCallback;
import no.nordicsemi.android.ble.data.Data;

public class BLEMainManager extends BatteryManager<BLEMainManagerCallbacks> {

	private BluetoothGattCharacteristic basicCharacteristic;

	private BluetoothGatt basic_gatt;
	public String DeviceName = "";

	BLEMainManager(final Context context) {
		super(context);
	}

	@NonNull
	@Override
	protected BatteryManager.BatteryManagerGattCallback getGattCallback() {
		return new MeeblueManagerGattCallback();
	}

	protected BluetoothGatt getBasicGatt()
	{
		return basic_gatt;
	}

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery,
	 * receiving indication, etc.
	 */
	private class MeeblueManagerGattCallback extends BatteryManagerGattCallback {

		@Override
		protected void initialize() {
			super.initialize();

			BluetoothGattCharacteristic deviceNameCharacteristic = getCharacteristicByUUID(MEEBLUE_Defines.Device_Service_Name, MEEBLUE_Defines.Device_Characteristic_Name);
			if (deviceNameCharacteristic != null) {
				readCharacteristic(MEEBLUE_Defines.Device_Service_Name, MEEBLUE_Defines.Device_Characteristic_Name, new BLEMainDataCallback() {
					@Override
					public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
						super.onOptionState(device, data, state);
						if (state && data != null && data.getStringValue(0) != null && data.getStringValue(0).length() > 0){
							DeviceName = data.getStringValue(0);
						}
					}
				});
			}
		}

		@Override
		public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(MEEBLUE_Defines.MEEBLUE_MAIN_SERVICE);
			if (service != null) {
				basicCharacteristic = service.getCharacteristic(MEEBLUE_Defines.MEEBLUE_MAIN_AUTHENTICATION);
			}
			basic_gatt = gatt;
			return basicCharacteristic != null;
		}

		@Override
		protected void onDeviceDisconnected() {
			super.onDeviceDisconnected();
			basicCharacteristic = null;
			basic_gatt = null;
		}


	}
}
