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
package com.meeblue.checkblue.ble.profile;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.meeblue.checkblue.utils.LocalBroadcastManager;
import com.meeblue.checkblue.ble.manager.BLE_MESSAGE_CALL_BACK_TYPE;
import com.meeblue.checkblue.ble.struct.Beacon_All_Data_t;
import com.meeblue.checkblue.fragment.utils.BLEUtils;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.BleManagerCallbacks;

@SuppressWarnings("unused")
public abstract class BleProfileService extends Service implements BleManagerCallbacks {

    private MainBleManager<BleManagerCallbacks> bleManager;
    protected Handler handler;

    protected Handler call_back_handler = null;
    protected HandlerCallBackObject call_back_temp_data = new HandlerCallBackObject();

    protected boolean bound;
    protected BluetoothDevice bluetoothDevice;
    protected String deviceName;


    public class HandlerCallBackObject extends Object {

        public BluetoothDevice device;
        public BluetoothGatt gatt;
        public Beacon_All_Data_t m_beacon_all_data = new Beacon_All_Data_t();

    }

    private final BroadcastReceiver bluetoothStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);

            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    onBluetoothEnabled();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                case BluetoothAdapter.STATE_OFF:
                    onBluetoothDisabled();
                    break;
            }
        }

        private String state2String(final int state) {
            switch (state) {
                case BluetoothAdapter.STATE_TURNING_ON:
                    return "TURNING ON";
                case BluetoothAdapter.STATE_ON:
                    return "ON";
                case BluetoothAdapter.STATE_TURNING_OFF:
                    return "TURNING OFF";
                case BluetoothAdapter.STATE_OFF:
                    return "OFF";
                default:
                    return "UNKNOWN (" + state + ")";
            }
        }
    };

    public class LocalBinder extends Binder {
        /**
         * Disconnects from the sensor.
         */
        public final void disconnect() {
            final int state = bleManager.getConnectionState();
            if (state == BluetoothGatt.STATE_DISCONNECTED || state == BluetoothGatt.STATE_DISCONNECTING) {
                bleManager.close();
                onDeviceDisconnected(bluetoothDevice);
                return;
            }

            bleManager.disconnect().enqueue();
        }


        public final void set_callback(Handler hand) {
            call_back_handler = hand;
        }

        /**
         * Returns the device address
         *
         * @return device address
         */
        public String getDeviceAddress() {
            return bluetoothDevice.getAddress();
        }

        /**
         * Returns the device name
         *
         * @return the device name
         */
        public String getDeviceName() {
            return deviceName;
        }

        /**
         * Returns the Bluetooth device
         *
         * @return the Bluetooth device
         */
        public BluetoothDevice getBluetoothDevice() {
            return bluetoothDevice;
        }

        /**
         * Returns <code>true</code> if the device is connected to the sensor.
         *
         * @return <code>true</code> if device is connected to the sensor, <code>false</code> otherwise
         */
        public boolean isConnected() {
            return bleManager.isConnected();
        }


        /**
         * Returns the connection state of given device.
         *
         * @return the connection state, as in {@link BleManager#getConnectionState()}.
         */
        public int getConnectionState() {
            return bleManager.getConnectionState();
        }
    }

    /**
     * Returns a handler that is created in onCreate().
     * The handler may be used to postpone execution of some operations or to run them in UI thread.
     */
    protected Handler getHandler() {
        return handler;
    }

    /**
     * Returns the binder implementation. This must return class implementing the additional manager interface that may be used in the bound activity.
     *
     * @return the service binder
     */
    protected LocalBinder getBinder() {
        // default implementation returns the basic binder. You can overwrite the LocalBinder with your own, wider implementation
        return new LocalBinder();
    }

    @Override
    public IBinder onBind(final Intent intent) {
        bound = true;
        return getBinder();
    }

    @Override
    public final void onRebind(final Intent intent) {
        bound = true;
        onRebind();
    }

    /**
     * Called when the activity has rebound to the service after being recreated.
     * This method is not called when the activity was killed to be recreated when the phone orientation changed
     */
    protected void onRebind() {
        // empty default implementation
    }

    @Override
    public final boolean onUnbind(final Intent intent) {
        bound = false;

        onUnbind();

        // We want the onRebind method be called if anything else binds to it again
        return true;
    }

    /**
     * Called when the activity has unbound from the service before being finished.
     * This method is not called when the activity is killed to be recreated when the phone orientation changed.
     */
    protected void onUnbind() {
        // empty default implementation
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler();

        // Initialize the manager
        bleManager = initializeManager();
        bleManager.setManagerCallbacks(this);

        // Register broadcast receivers
        registerReceiver(bluetoothStateBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        // Service has now been created
        onServiceCreated();

        // Call onBluetoothEnabled if Bluetooth enabled
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {
            onBluetoothEnabled();
        }
    }

    /**
     * Called when the service has been created, before the {@link #onBluetoothEnabled()} is called.
     */
    protected void onServiceCreated() {
        // empty default implementation
    }

    /**
     * Initializes the Ble Manager responsible for connecting to a single device.
     *
     * @return a new BleManager object
     */
    @SuppressWarnings("rawtypes")
    protected abstract MainBleManager initializeManager();


    protected BluetoothGatt getDefaultGatt(){
        return null;
    }

    /**
     * This method returns whether autoConnect option should be used.
     *
     * @return true to use autoConnect feature, false (default) otherwise.
     */
    protected boolean shouldAutoConnect() {
        return false;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        BLEUtils.DEBUG_PRINTF("onStartCommand");
        return START_REDELIVER_INTENT;
    }

    /**
     * Called when the service has been started. The device name and address are set.
     * The BLE Manager will try to connect to the device after this method finishes.
     */
    protected void onServiceStarted() {
        // empty default implementation
    }

    @Override
    public void onTaskRemoved(final Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        // This method is called when user removed the app from Recents.
        // By default, the service will be killed and recreated immediately after that.
        // However, all managed devices will be lost and devices will be disconnected.
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister broadcast receivers
        unregisterReceiver(bluetoothStateBroadcastReceiver);

        // shutdown the manager
        bleManager.close();
        bleManager = null;
        bluetoothDevice = null;
        deviceName = null;
        handler = null;
    }

    /**
     * Method called when Bluetooth Adapter has been disabled.
     */
    protected void onBluetoothDisabled() {
        // empty default implementation
    }

    /**
     * This method is called when Bluetooth Adapter has been enabled and
     * after the service was created if Bluetooth Adapter was enabled at that moment.
     * This method could initialize all Bluetooth related features, for example open the GATT server.
     */
    protected void onBluetoothEnabled() {
        // empty default implementation
    }

    private void send_message(BLE_MESSAGE_CALL_BACK_TYPE what, BluetoothDevice device)
    {
        call_back_temp_data.device = device;
        call_back_temp_data.gatt = getDefaultGatt();

        Message message = new Message();
        message.what = what.ordinal();
        message.obj = call_back_temp_data;
        if (call_back_handler != null) call_back_handler.sendMessage(message);
    }

    @Override
    public void onDeviceConnecting(@NonNull final BluetoothDevice device) {

        send_message(BLE_MESSAGE_CALL_BACK_TYPE.MESSAGE_ID_CONNECTING, device);
    }

    @Override
    public void onDeviceConnected(@NonNull final BluetoothDevice device) {
        BLEUtils.DEBUG_PRINTF("onDeviceConnected");
        send_message(BLE_MESSAGE_CALL_BACK_TYPE.MESSAGE_ID_CONNECTED, device);
    }

    @Override
    public void onDeviceDisconnecting(@NonNull final BluetoothDevice device) {
        // Notify user about changing the state to DISCONNECTING
        BLEUtils.DEBUG_PRINTF("onDeviceDisconnecting");
        send_message(BLE_MESSAGE_CALL_BACK_TYPE.MESSAGE_ID_DISCONNECTING, device);
    }

    /**
     * This method should return false if the service needs to do some asynchronous work after if has disconnected from the device.
     * In that case the {@link #stopService()} method must be called when done.
     *
     * @return true (default) to automatically stop the service when device is disconnected. False otherwise.
     */
    protected boolean stopWhenDisconnected() {
        return false;
    }

    @Override
    public void onDeviceDisconnected(@NonNull final BluetoothDevice device) {


        final Intent broadcast = new Intent(MEEBLUE_Defines.BROADCAST_SERVICES_DISCONNECTED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);

        BLEUtils.DEBUG_PRINTF("onDeviceDisconnected");
        send_message(BLE_MESSAGE_CALL_BACK_TYPE.MESSAGE_ID_DISCONNECTED, device);
        if (stopWhenDisconnected())
            stopService();
    }

    protected void stopService() {
        // user requested disconnection. We must stop the service
        BLEUtils.DEBUG_PRINTF("Stopping service...");
        stopSelf();
    }

    @Override
    public void onLinkLossOccurred(@NonNull final BluetoothDevice device) {
        BLEUtils.DEBUG_PRINTF("onLinkLossOccurred");
        send_message(BLE_MESSAGE_CALL_BACK_TYPE.MESSAGE_ID_LINK_LOSS, device);
    }

    @Override
    public void onServicesDiscovered(@NonNull final BluetoothDevice device, final boolean optionalServicesFound) {
        BLEUtils.DEBUG_PRINTF("onServicesDiscovered");
        send_message(BLE_MESSAGE_CALL_BACK_TYPE.MESSAGE_ID_SERVICE_DISCOVERED, device);
    }

    @Override
    public void onDeviceReady(@NonNull final BluetoothDevice device) {
        BLEUtils.DEBUG_PRINTF("onDeviceReady");
        send_message(BLE_MESSAGE_CALL_BACK_TYPE.MESSAGE_ID_DEVICE_READY, device);
    }

    @Override
    public void onDeviceNotSupported(@NonNull final BluetoothDevice device) {
        BLEUtils.DEBUG_PRINTF("onDeviceNotSupported");
        send_message(BLE_MESSAGE_CALL_BACK_TYPE.MESSAGE_ID_NOT_SUPPORT, device);
    }

    @Override
    public void onBondingRequired(@NonNull final BluetoothDevice device) {
        BLEUtils.DEBUG_PRINTF("onBondingRequired");
        send_message(BLE_MESSAGE_CALL_BACK_TYPE.MESSAGE_ID_Bonding_Required, device);
    }

    @Override
    public void onBonded(@NonNull final BluetoothDevice device) {
        BLEUtils.DEBUG_PRINTF("onBonded");
        send_message(BLE_MESSAGE_CALL_BACK_TYPE.MESSAGE_ID_Bonded, device);
    }

    @Override
    public void onBondingFailed(@NonNull final BluetoothDevice device) {
        BLEUtils.DEBUG_PRINTF("onBondingFailed");
        send_message(BLE_MESSAGE_CALL_BACK_TYPE.MESSAGE_ID_BondingFailed, device);
    }

    @Override
    public void onError(@NonNull final BluetoothDevice device, @NonNull final String message, final int errorCode) {
        BLEUtils.DEBUG_PRINTF("onError:"+message);
        send_message(BLE_MESSAGE_CALL_BACK_TYPE.MESSAGE_ID_CONNECT_ERROR, device);
    }

    /**
     * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
     *
     * @param messageResId an resource id of the message to be shown
     */
    protected void showToast(final int messageResId) {
        handler.post(() -> Toast.makeText(BleProfileService.this, messageResId, Toast.LENGTH_SHORT).show());
    }

    /**
     * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
     *
     * @param message a message to be shown
     */
    protected void showToast(final String message) {
        handler.post(() -> Toast.makeText(BleProfileService.this, message, Toast.LENGTH_SHORT).show());
    }
    /**
     * Returns the device address
     *
     * @return device address
     */
    protected String getDeviceAddress() {
        return bluetoothDevice.getAddress();
    }

    /**
     * Returns the Bluetooth device object
     *
     * @return bluetooth device
     */
    protected BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    /**
     * Returns the device name
     *
     * @return the device name
     */
    protected String getDeviceName() {
        return deviceName;
    }

    /**
     * Returns <code>true</code> if the device is connected to the sensor.
     *
     * @return <code>true</code> if device is connected to the sensor, <code>false</code> otherwise
     */
    protected boolean isConnected() {
        return bleManager != null && bleManager.isConnected();
    }
}
