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

package com.meeblue.checkblue.utils;


import android.content.Context;
import android.os.Parcelable;

import com.xuexiang.xutil.XUtil;
import com.xuexiang.xutil.data.BaseSPUtil;

import java.util.Set;

/**
 * author alvin
 * since 2020-03-17
 */
public final class SettingSPUtils extends BaseSPUtil{

    public static final String auto_send_code_key = "auto_send_code_key";
    public static final String default_password_key = "default_password_key";
    public static final String temperature_unit_key = "temperature_unit_key";
    public static final String limited_rssi_key = "limited_rssi_key";

    private static volatile SettingSPUtils sInstance = null;

    private SettingSPUtils(Context context) {
        super(context);
    }

    /**
     * 获取单例
     * @return
     */
    public static SettingSPUtils getInstance() {
        if (sInstance == null) {
            synchronized (SettingSPUtils.class) {
                if (sInstance == null) {
                    sInstance = new SettingSPUtils(XUtil.getContext());
                }
            }
        }
        return sInstance;
    }

    public int limited_rssi_key() {
        return getInt(limited_rssi_key, 100);
    }

    public void setlimited_rssi_key(int value) {
        putInt(limited_rssi_key, value);
    }

    public String temperature_unit_key() {
        return getString(temperature_unit_key, "0");
    }

    public void settemperature_unit_key(String value) {
        putString(temperature_unit_key, value);
    }

    public boolean auto_send_code_key() {
        return getBoolean(auto_send_code_key, false);
    }

    public void setauto_send_code_key(boolean auto_send) {
        putBoolean(auto_send_code_key, auto_send);
    }

    public String default_password_key() {
        return getString(default_password_key, "meeble");
    }

    public void setdefault_password_key(String value) {
        putString(default_password_key, value);
    }




}
