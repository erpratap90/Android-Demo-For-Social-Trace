/*
 * Copyright (C) 2020 meeblue Inc.
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
package com.meeblue.checkblue.ble.profile;

import java.util.UUID;

public class MEEBLUE_Defines {
    @SuppressWarnings("unused")
    private static final String TAG = "BleProfileService";

    public static final String BROADCAST_CONNECTION_STATE = "com.meeblue.checkblue.BROADCAST_CONNECTION_STATE";
    public static final String BROADCAST_SERVICES_DISCOVERED = "com.meeblue.checkblue.BROADCAST_SERVICES_DISCOVERED";

    public static final String BROADCAST_SERVICES_DISCONNECTED = "com.meeblue.checkblue.BROADCAST_SERVICES_DISCONNECTED";
    public static final String BROADCAST_DEVICE_READY = "com.meeblue.checkblue.DEVICE_READY";
    public static final String BROADCAST_BOND_STATE = "com.meeblue.checkblue.BROADCAST_BOND_STATE";
    @Deprecated
    public static final String BROADCAST_BATTERY_LEVEL = "com.meeblue.checkblue.BROADCAST_BATTERY_LEVEL";
    public static final String BROADCAST_ERROR = "com.meeblue.checkblue.BROADCAST_ERROR";

    /**
     * The parameter passed when creating the service. Must contain the address of the sensor that we want to connect to
     */
    public static final String EXTRA_DEVICE_ADDRESS = "com.meeblue.checkblue.EXTRA_DEVICE_ADDRESS";
    /**
     * The key for the device name that is returned in {@link #BROADCAST_CONNECTION_STATE} with state {@link #STATE_CONNECTED}.
     */
    public static final String EXTRA_DEVICE_NAME = "com.meeblue.checkblue.EXTRA_DEVICE_NAME";
    public static final String EXTRA_DEVICE = "com.meeblue.checkblue.EXTRA_DEVICE";
    public static final String EXTRA_LOG_URI = "com.meeblue.checkblue.EXTRA_LOG_URI";
    public static final String EXTRA_CONNECTION_STATE = "com.meeblue.checkblue.EXTRA_CONNECTION_STATE";
    public static final String EXTRA_BOND_STATE = "com.meeblue.checkblue.EXTRA_BOND_STATE";
    public static final String EXTRA_SERVICE_PRIMARY = "com.meeblue.checkblue.EXTRA_SERVICE_PRIMARY";
    public static final String EXTRA_SERVICE_SECONDARY = "com.meeblue.checkblue.EXTRA_SERVICE_SECONDARY";
    @Deprecated
    public static final String EXTRA_BATTERY_LEVEL = "com.meeblue.checkblue.EXTRA_BATTERY_LEVEL";
    public static final String EXTRA_ERROR_MESSAGE = "com.meeblue.checkblue.EXTRA_ERROR_MESSAGE";
    public static final String EXTRA_ERROR_CODE = "com.meeblue.checkblue.EXTRA_ERROR_CODE";

    public static final int STATE_LINK_LOSS = -1;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_DISCONNECTING = 3;


    public final static UUID MEEBLUE_MAIN_SERVICE = UUID.fromString("D35B1000-E01C-9FAC-BA8D-7CE20BDBA0C6");
    public final static UUID MEEBLUE_MAIN_AUTHENTICATION = UUID.fromString("D35B1001-E01C-9FAC-BA8D-7CE20BDBA0C6");
    public final static UUID MEEBLUE_MAIN_BEACON_STATE = UUID.fromString("D35B1002-E01C-9FAC-BA8D-7CE20BDBA0C6");
    public final static UUID MEEBLUE_MAIN_DEVICE_NAME = UUID.fromString("D35B1003-E01C-9FAC-BA8D-7CE20BDBA0C6");

    public final static UUID MEEBLUE_CLOSE_SERVICE =     UUID.fromString("D35B6000-E01C-9FAC-BA8D-7CE20BDBA0C6");
    public final static UUID MEEBLUE_CLOSE_STATE =     UUID.fromString("D35B6001-E01C-9FAC-BA8D-7CE20BDBA0C6");
    public final static UUID MEEBLUE_CLOSE_DATA =     UUID.fromString("D35B6002-E01C-9FAC-BA8D-7CE20BDBA0C6");
    public final static UUID MEEBLUE_CLOSE_WHITE_LIST =     UUID.fromString("D35B6003-E01C-9FAC-BA8D-7CE20BDBA0C6");

    public final static UUID MEEBLUE_ACC_SERVICE = UUID.fromString("D35B4000-E01C-9FAC-BA8D-7CE20BDBA0C6");
    public final static UUID MEEBLUE_MOTION_1ST = UUID.fromString("D35B4001-E01C-9FAC-BA8D-7CE20BDBA0C6");
    public final static UUID MEEBLUE_MOTION_2ND = UUID.fromString("D35B4002-E01C-9FAC-BA8D-7CE20BDBA0C6");


    public final static UUID MEEBLUE_PERIPHERAL_SERVICE = UUID.fromString("D35B5000-E01C-9FAC-BA8D-7CE20BDBA0C6");
    public final static UUID MEEBLUE_PERIPHERAL_BUZZER = UUID.fromString("D35B5001-E01C-9FAC-BA8D-7CE20BDBA0C6");
    public final static UUID MEEBLUE_PERIPHERAL_BUTTON = UUID.fromString("D35B5002-E01C-9FAC-BA8D-7CE20BDBA0C6");
    public final static UUID MEEBLUE_PERIPHERAL_BATTERY = UUID.fromString("D35B5003-E01C-9FAC-BA8D-7CE20BDBA0C6");


    public final static UUID Device_INFO_SERVER = UUID.fromString("0000180A-0000-1000-8000-00805f9b34fb");
    public final static UUID Device_INFO_SYSTEM_ID = UUID.fromString("00002A23-0000-1000-8000-00805f9b34fb");
    public final static UUID Device_INFO_FIRMWARE_ID = UUID.fromString("00002A26-0000-1000-8000-00805f9b34fb");


    public final static UUID NotifyCation_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    public final static UUID Device_Service_Name = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    public final static UUID Device_Characteristic_Name = UUID.fromString("00002A00-0000-1000-8000-00805f9b34fb");

    public final static String START_AUTH_OPTION_CODE = "i";
    public final static String CHANGE_AUTH_OPTION_CODE = "c";

}
