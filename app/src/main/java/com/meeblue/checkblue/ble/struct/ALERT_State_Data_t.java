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

public class ALERT_State_Data_t extends Object {
    public int m_Beacon_Broadcast;
    public int m_Trigger_Mode_Adv_Time;//2-65535
    public int m_Beacon_State;
    public int m_txPower;
    public int m_Connect_State;//uint:minutes, 0:no limit
    public int m_Motion_Strength_One;
    public int m_Motion_Strength_Two;
    public int m_alarm_type;
    public int Limit_Rssi;
    public int Scan_Interval;//Unit Second，range from 1-30, default 1;
    public long m_device_id;
    public long m_current_time_stamp;


    public byte[] getCombination() {
        byte[] temp = new byte[20];

        int length = 0;
        System.arraycopy(BLEUtils.UnsignedInt16ToBytes(m_Beacon_Broadcast, false), 0, temp, length, 2);
        length += 2;
        System.arraycopy(BLEUtils.UnsignedInt16ToBytes(m_Trigger_Mode_Adv_Time, false), 0, temp, length, 2);
        length += 2;
        System.arraycopy(BLEUtils.UnsignedInt8ToBytes(m_Beacon_State), 0, temp, length, 1);
        length += 1;
        System.arraycopy(BLEUtils.UnsignedInt8ToBytes(m_txPower), 0, temp, length, 1);
        length += 1;
        System.arraycopy(BLEUtils.UnsignedInt8ToBytes(m_Connect_State), 0, temp, length, 1);
        length += 1;
        System.arraycopy(BLEUtils.UnsignedInt8ToBytes(m_Motion_Strength_One), 0, temp, length, 1);
        length += 1;
        System.arraycopy(BLEUtils.UnsignedInt8ToBytes(m_Motion_Strength_Two), 0, temp, length, 1);
        length += 1;
        System.arraycopy(BLEUtils.UnsignedInt8ToBytes(m_alarm_type), 0, temp, length, 1);
        length += 1;
        System.arraycopy(BLEUtils.UnsignedInt8ToBytes(Limit_Rssi), 0, temp, length, 1);
        length += 1;
        System.arraycopy(BLEUtils.UnsignedInt8ToBytes(Scan_Interval), 0, temp, length, 1);
        length += 1;
        System.arraycopy(BLEUtils.UnsignedInt32ToBytes(m_device_id, false), 0, temp, length, 4);
        length += 4;
        System.arraycopy(BLEUtils.UnsignedInt32ToBytes(m_current_time_stamp, false), 0, temp, length, 4);
        return temp;
    }

    public boolean setCombination(byte[] value) {
        if (value.length != 20) {
            BLEUtils.DEBUG_PRINTF("Value length unaviliable");
            return false;
        }
        m_Beacon_Broadcast = BLEUtils.getUnsignedInt16(BLEUtils.subBytes(value, 0, 2), false);
        m_Trigger_Mode_Adv_Time = BLEUtils.getUnsignedInt16(BLEUtils.subBytes(value, 2, 2), false);
        m_Beacon_State = BLEUtils.getUnsignedInt8(BLEUtils.subBytes(value, 4, 1), false);
        m_txPower = new Data(BLEUtils.subBytes(value, 5, 1)).getIntValue(Data.FORMAT_SINT8, 0);
        m_Connect_State = BLEUtils.getUnsignedInt8(BLEUtils.subBytes(value, 6, 1), false);
        m_Motion_Strength_One = BLEUtils.getUnsignedInt8(BLEUtils.subBytes(value, 7, 1), false);
        m_Motion_Strength_Two = BLEUtils.getUnsignedInt8(BLEUtils.subBytes(value, 8, 1), false);
        m_alarm_type = BLEUtils.getUnsignedInt8(BLEUtils.subBytes(value, 9, 1), false);
        Limit_Rssi = new Data(BLEUtils.subBytes(value, 10, 1)).getIntValue(Data.FORMAT_SINT8, 0);
        Scan_Interval = BLEUtils.getUnsignedInt8(BLEUtils.subBytes(value, 11, 1), false);
        m_device_id = BLEUtils.getUnsignedInt32(BLEUtils.subBytes(value, 12, 4), false);
        m_current_time_stamp = BLEUtils.getUnsignedInt32(BLEUtils.subBytes(value, 16, 4), false);

        return true;
    }
}

