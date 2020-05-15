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

package com.meeblue.checkblue.fragment.utils;

import android.os.ParcelUuid;

import androidx.annotation.Nullable;

import com.meeblue.checkblue.ble.struct.Advertise_Data_t;

import java.io.Serializable;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class BLELocalInfo implements Serializable {

    //BASIC
    public static final String NULL_STRING_SHOW = "--";
    private ScanResult Result;
    public String       Scan_Record_String;


    public Advertise_Data_t m_adv_data = new Advertise_Data_t();
    public BLELocalInfo(ScanResult result) {
        this.Result = result;
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj.getClass().equals(BLELocalInfo.class))
        {
            BLELocalInfo temp = (BLELocalInfo)obj;
            if (temp.getResult().getDevice().getAddress().equalsIgnoreCase(this.getResult().getDevice().getAddress())) return true;
        }
        else if (obj.getClass().equals(ScanResult.class))
        {
            ScanResult temp = (ScanResult)obj;
            if (temp.getDevice().getAddress().equalsIgnoreCase(this.getResult().getDevice().getAddress())) return true;
        }
        return false;
    }

    public ScanResult getResult() {
        return Result;
    }

    public BLELocalInfo setResult(ScanResult value) {
        Result = value;
        return this;
    }

    public boolean IsAlarmBeacon() {
        byte[] temp = this.Result.getScanRecord().getServiceData(ParcelUuid.fromString(BLEUtils.ALARM_NON_CONNECTABLE_UUID));
        byte[] temp1 = this.Result.getScanRecord().getServiceData(ParcelUuid.fromString(BLEUtils.ALARM_CONNECTABLE_UUID));
        if (temp != null || temp1 != null)
        {
            if (temp != null) m_adv_data.setCombination(temp);
            else if (temp1 != null) m_adv_data.setCombination(temp1);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "NewInfo{" +
                "ScanResult='" + Result.toString()+ '}';
    }
}
