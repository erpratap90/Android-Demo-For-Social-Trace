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
package com.meeblue.checkblue.ble.struct;

import com.meeblue.checkblue.fragment.utils.BLEUtils;

import no.nordicsemi.android.ble.data.Data;

public class Advertise_Data_t extends Object {

    public long device_id;
    public long record_latest_snape;
    public int record_count;
    public int advertise_interval;
    public int battery_votage;
    public int device_type;
    public int advertise_type;
    public int alert_power;
    public long	Reserved = 0;

    public byte[] getCombination() {
        byte[] temp = new byte[20];
        System.arraycopy(BLEUtils.UnsignedInt32ToBytes(device_id, false), 0, temp, 0, 4);
        System.arraycopy(BLEUtils.UnsignedInt32ToBytes(record_latest_snape, false), 0, temp, 4, 4);
        System.arraycopy(BLEUtils.UnsignedInt16ToBytes(record_count, false), 0, temp, 8, 2);
        System.arraycopy(BLEUtils.UnsignedInt16ToBytes(advertise_interval, false), 0, temp, 10, 2);
        System.arraycopy(BLEUtils.UnsignedInt16ToBytes(battery_votage, false), 0, temp, 12, 2);
        System.arraycopy(BLEUtils.UnsignedInt8ToBytes(device_type), 0, temp, 14, 1);
        System.arraycopy(BLEUtils.UnsignedInt8ToBytes(advertise_type), 0, temp, 15, 1);
        System.arraycopy(BLEUtils.UnsignedInt8ToBytes(alert_power), 0, temp, 16, 1);

        temp[17] = 0;
        temp[18] = 0;
        temp[19] = 0;


        return temp;
    }

    public void setCombination(byte[] value) {
        device_id = BLEUtils.getUnsignedInt32(BLEUtils.subBytes(value, 0, 4), false);
        record_latest_snape = BLEUtils.getUnsignedInt16(BLEUtils.subBytes(value, 4, 4), false);
        record_count = BLEUtils.getUnsignedInt16(BLEUtils.subBytes(value, 8, 2), false);
        advertise_interval = BLEUtils.getUnsignedInt16(BLEUtils.subBytes(value, 10, 2), false);
        battery_votage = BLEUtils.getUnsignedInt16(BLEUtils.subBytes(value, 12, 2), false);
        device_type = BLEUtils.getUnsignedInt8(BLEUtils.subBytes(value, 14, 1), false);
        advertise_type = BLEUtils.getUnsignedInt8(BLEUtils.subBytes(value, 15, 1), false);
        alert_power = new Data(BLEUtils.subBytes(value, 16, 1)).getIntValue(Data.FORMAT_SINT8, 0);
    }
}
