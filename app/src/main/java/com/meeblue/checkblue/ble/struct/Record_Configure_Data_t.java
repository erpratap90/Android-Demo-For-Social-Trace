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

public class Record_Configure_Data_t extends Object {


    public long current_time = 0;
    public long current_max_count = 0;
    public long sync_max_count = 0;
    public long option_command = 0;
    public long option_size = 0;

    public byte[] getCombination() {
        byte[] temp = new byte[20];
        System.arraycopy(BLEUtils.UnsignedInt32ToBytes(current_time, false), 0, temp, 0, 4);
        System.arraycopy(BLEUtils.UnsignedInt32ToBytes(current_max_count, false), 0, temp, 4, 4);
        System.arraycopy(BLEUtils.UnsignedInt32ToBytes(sync_max_count, false), 0, temp, 8, 4);
        System.arraycopy(BLEUtils.UnsignedInt32ToBytes(option_command, false), 0, temp, 12, 4);
        System.arraycopy(BLEUtils.UnsignedInt32ToBytes(option_size, false), 0, temp, 16, 4);

        return temp;
    }

    public void setCombination(byte[] value) {
        current_time = BLEUtils.getUnsignedInt32(BLEUtils.subBytes(value, 0, 4), false);
        current_max_count = BLEUtils.getUnsignedInt32(BLEUtils.subBytes(value, 4, 4), false);
        sync_max_count = BLEUtils.getUnsignedInt32(BLEUtils.subBytes(value, 8, 4), false);
        option_command = BLEUtils.getUnsignedInt32(BLEUtils.subBytes(value, 12, 4), false);
        option_size = BLEUtils.getUnsignedInt32(BLEUtils.subBytes(value, 16, 4), false);

    }
}
