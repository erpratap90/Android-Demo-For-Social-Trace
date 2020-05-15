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

public class Beacon_All_Data_t extends Object {

    public static final int WIHITE_LIST_COUNT = 45;
    public static final int WIHITE_LIST_TOTAL_LENGTH = 180;

    public ALERT_State_Data_t Beacon_State = new ALERT_State_Data_t();
    public Record_Configure_Data_t Record_Config = new Record_Configure_Data_t();

    public byte[] mDevice_Name = new byte[20];

    public long[]    m_white_list = new long[WIHITE_LIST_COUNT];

    public int m_batt_voltage;
    public byte[] Systemp_ID = new byte[6];
    public byte[] Firmware_ID = new byte[20];

    public int get_white_list_count()
    {
        int i = 0;
        for (i = 0; i < WIHITE_LIST_COUNT; i++)
        {
            if (m_white_list[i] == 0)
            {
                break;
            }
        }
        return i;
    }
}
