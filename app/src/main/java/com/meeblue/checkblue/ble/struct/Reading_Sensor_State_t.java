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

public class Reading_Sensor_State_t extends Object  {
    public   long start_time;
    public   long end_time;
    public  long current_count;

    public byte[] getCombination() {
        byte[] temp = new byte[12];
        System.arraycopy(BLEUtils.UnsignedInt32ToBytes(start_time, false), 0, temp, 0, 4);
        System.arraycopy(BLEUtils.UnsignedInt32ToBytes(end_time, false), 0, temp, 4, 4);
        System.arraycopy(BLEUtils.UnsignedInt32ToBytes(current_count, false), 0, temp, 8, 4);

        return temp;
    }

    public void setCombination(byte[] value) {
        start_time = BLEUtils.getUnsignedInt32(BLEUtils.subBytes(value, 0, 4), false);
        end_time = BLEUtils.getUnsignedInt32(BLEUtils.subBytes(value, 4, 4), false);
        current_count = BLEUtils.getUnsignedInt32(BLEUtils.subBytes(value, 8, 4), false);
    }
}
