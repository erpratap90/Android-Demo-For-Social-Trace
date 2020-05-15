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

package com.meeblue.checkblue;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;

import androidx.multidex.MultiDex;

import com.meeblue.checkblue.ble.manager.BLEMainService;
import com.meeblue.checkblue.fragment.utils.BLEUtils;
import com.meeblue.checkblue.utils.sdkinit.XBasicLibInit;
import com.xuexiang.xui.widget.toast.XToast;

/**
 * author alvin
 * since 2020-03-17
 */
public class MyApp extends Application {

    public static BLEMainService Main_Service = null;
    public static BLEMainService.BLEServiceBinder Main_ServiceBinder = null;
    public static Handler m_Main_Handle = null;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //解决4.x运行崩溃的问题
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initLibs();
        startMainService();
    }

    public static Context getApplication()
    {
        return getApplication().getApplicationContext();
    }

    public void startMainService()
    {
        XToast.Config.get().setGravity(Gravity.CENTER);//位置设置为居中
        BLEUtils.DEBUG_PRINTF("startMainService");
        Intent gattServiceIntent = new Intent(MyApp.this, BLEMainService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Main_ServiceBinder = ((BLEMainService.BLEServiceBinder) service);
            Main_Service = Main_ServiceBinder.getService();
            if (m_Main_Handle != null) Main_ServiceBinder.set_callback(m_Main_Handle);
            BLEUtils.DEBUG_PRINTF("onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            BLEUtils.DEBUG_PRINTF("onServiceDisconnected");
            Main_Service.shutdown();
        }
    };

    public static void set_service_call_back_handler(Handler handler)
    {
        m_Main_Handle = handler;
        if (Main_ServiceBinder != null) {
            Main_ServiceBinder.set_callback(handler);
        }
    }

    /**
     * 初始化基础库
     */
    private void initLibs() {
        XBasicLibInit.init(this);
    }


    /**
     * @return 当前app是否是调试开发模式
     */
    public static boolean isDebug() {
        return BuildConfig.DEBUG;
    }


}
