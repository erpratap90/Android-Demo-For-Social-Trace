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

package com.meeblue.checkblue.core;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Parcelable;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.meeblue.checkblue.R;
import com.meeblue.checkblue.core.http.loader.ProgressLoader;
import com.meeblue.checkblue.fragment.utils.BLELocalInfo;
import com.meeblue.checkblue.fragment.global.GlobalFragment;
import com.meeblue.checkblue.fragment.list.SimpleRecyclerAdapter;
import com.meeblue.checkblue.fragment.utils.StringInputCallback;
import com.meeblue.checkblue.utils.RxJavaUtils;
import com.meeblue.checkblue.utils.SettingSPUtils;
import com.meeblue.checkblue.utils.XToastUtils;
import com.scwang.smartrefresh.layout.adapter.SmartViewHolder;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.xuexiang.xhttp2.subsciber.impl.IProgressLoader;
import com.xuexiang.xpage.base.XPageActivity;
import com.xuexiang.xpage.base.XPageFragment;
import com.xuexiang.xpage.core.PageOption;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xrouter.facade.service.SerializationService;
import com.xuexiang.xrouter.launcher.XRouter;
import com.xuexiang.xui.utils.WidgetUtils;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.actionbar.TitleUtils;
import com.xuexiang.xui.widget.dialog.DialogLoader;
import com.xuexiang.xui.widget.dialog.LoadingDialog;
import com.xuexiang.xui.widget.dialog.materialdialog.DialogAction;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

/**
 * author alvin
 * since 2020-03-17
 */
public abstract class BaseFragment extends XPageFragment {

    private IProgressLoader mIProgressLoader;
    protected TitleBar m_title;


    protected SimpleRecyclerAdapter mAdapter;

    protected RefreshLayout refreshLayout;

    protected String select_show_type = "0";
    protected List<ScanFilter> ble_filters = new ArrayList<>();

    private LoadingDialog mLoadingDialog;

    //LOCAATION
    protected final static int REQUEST_PERMISSION_REQ_CODE = 34; // any 8-bit number

    //BLE
    protected final static int REQUEST_PERMISSION_BLE_CODE = 35; // any 8-bit number

    protected ArrayList<BLELocalInfo> m_ScanList = new ArrayList<BLELocalInfo>();

    private Disposable m_Disposable = null;

    @Override
    protected void initPage() {
        initTitle();
        initViews();
        initLoadingView();
        initListeners();
    }

    private void initLoadingView()
    {
        mLoadingDialog = WidgetUtils.getLoadingDialog(getContext())
                .setIconScale(0.4F)
                .setLoadingSpeed(8);
        mLoadingDialog.setCancelable(false);
    }

    protected void showDialogLoader(String Message)
    {
        if (m_Disposable != null && !m_Disposable.isDisposed()) m_Disposable.dispose();
        if (mLoadingDialog.isShowing()) mLoadingDialog.dismiss();

        DialogLoader.getInstance().showTipDialog(
                getContext(),
                getString(R.string.warn_tip),
                Message,
                "OK");
    }


    protected void showPasswordInput(StringInputCallback callback) {
        new MaterialDialog.Builder(getContext())
                .title(R.string.warn_tip)
                .content(R.string.code_tip)
                .inputType(InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_NORMAL
                        | InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .input(
                        getString(R.string.authentication_code),
                        SettingSPUtils.getInstance().default_password_key(),
                        false,
                        (new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                //XToastUtils.toast(input.toString());
                            }
                        }))
                .inputRange(6, 6)
                .positiveText("Save & Continue")
                .neutralText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        callback.onStringReceived(dialog.getInputEditText().getText().toString(), true);
                        SettingSPUtils.getInstance().setdefault_password_key(dialog.getInputEditText().getText().toString());
                        SettingSPUtils.getInstance().setauto_send_code_key(true);
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        callback.onStringReceived("", false);
                    }
                })
                .cancelable(false)
                .show();
    }

    protected void showInputDialog(String DefaultShow, int inputType, StringInputCallback callback) {
        new MaterialDialog.Builder(getContext())
                .title("Input Data")
                .inputType(inputType)
                .input(
                        DefaultShow,
                        "",
                        false,
                        (new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                //XToastUtils.toast(input.toString());
                            }
                        }))
                .positiveText(R.string.done)
                .neutralText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (dialog.getInputEditText().getText().toString() != null && dialog.getInputEditText().getText().toString().length() > 0) callback.onStringReceived(dialog.getInputEditText().getText().toString(), true);
                        else callback.onStringReceived("", false);
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        callback.onStringReceived("", false);
                    }
                })
                .cancelable(false)
                .show();
    }

    public void showLoadingDialog(String Message) {


        if (m_Disposable != null && !m_Disposable.isDisposed()) m_Disposable.dispose();

        mLoadingDialog.updateMessage(Message);
        //mLoadingDialog.setCancelable(cannelable);
        //mLoadingDialog.setLoadingCancelListener(listener);
        if (!mLoadingDialog.isShowing()) mLoadingDialog.show();

        m_Disposable = RxJavaUtils.delay(15, new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                showErrorDialog("Option timeout, please try again later");
            }
        });
    }

    public void showSuccessDialog(String Message) {

        if (m_Disposable != null && !m_Disposable.isDisposed()) m_Disposable.dispose();
        if (mLoadingDialog.isShowing()) mLoadingDialog.dismiss();
        XToastUtils.success(Message);
//        mLoadingDialog.updateMessage(Message);
//        mLoadingDialog.setCancelable(true);
//        mLoadingDialog.show();
//        RxJavaUtils.delay(time, new Consumer<Long>() {
//            @Override
//            public void accept(Long aLong) throws Exception {
//                mLoadingDialog.dismiss();
//            }
//        });
    }

    public void showErrorDialog(String Message) {
        if (m_Disposable != null && !m_Disposable.isDisposed()) m_Disposable.dispose();

        if (mLoadingDialog.isShowing()) mLoadingDialog.dismiss();
        XToastUtils.error(Message);
//        if (!mLoadingDialog.isShowing()) mLoadingDialog.dismiss();
//        mLoadingDialog.updateMessage(Message);
//        mLoadingDialog.setCancelable(true);
//        mLoadingDialog.show();
//        RxJavaUtils.delay(time, new Consumer<Long>() {
//            @Override
//            public void accept(Long aLong) throws Exception {
//                mLoadingDialog.dismiss();
//            }
//        });
    }

    public void dismissDialog()
    {
        if (m_Disposable != null && !m_Disposable.isDisposed()) m_Disposable.dispose();
        mLoadingDialog.dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLoadingDialog.recycle();
    }

    protected TitleBar initTitle() {
        m_title = TitleUtils.addTitleBarDynamic((ViewGroup) getRootView(), getPageTitle(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popToBack();
            }
        });
        //m_title.setLeftImageResource(R.drawable.ic_action_menu);
        //m_title.setLeftText("");
        m_title.setTitle(getPageTitle());
        return m_title;
    }

    protected void SetTitle(String value){
        m_title.setTitle(value);
    }

    protected void onRightActionClick(){

    }

    protected void addActionRightAction(String message)
    {
        m_title.addAction(new TitleBar.TextAction(message) {
            @Override
            public void performAction(View view) {
                onRightActionClick();
            }
        });
    }

    protected TitleBar GetATitle() {
        return m_title;
    }

    protected void SetATitle(int resId, String leftTitle) {
        m_title.setLeftImageResource(resId);
        m_title.setLeftText(leftTitle);
    }


    @Override
    protected void initListeners() {

    }

    /**
     * 获取进度条加载者
     *
     * @return 进度条加载者
     */
    public IProgressLoader getProgressLoader() {
        if (mIProgressLoader == null) {
            mIProgressLoader = ProgressLoader.create(getContext());
        }
        return mIProgressLoader;
    }

    /**
     * 获取进度条加载者
     *
     * @param message
     * @return 进度条加载者
     */
    public IProgressLoader getProgressLoader(String message) {
        if (mIProgressLoader == null) {
            mIProgressLoader = ProgressLoader.create(getContext(), message);
        } else {
            mIProgressLoader.updateMessage(message);
        }
        return mIProgressLoader;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //屏幕旋转时刷新一下title
        super.onConfigurationChanged(newConfig);
        ViewGroup root = (ViewGroup) getRootView();
        if (root.getChildAt(0) instanceof TitleBar) {
            root.removeViewAt(0);
            initTitle();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    //==============================页面跳转api===================================//

    /**
     * 打开一个新的页面【建议只在主tab页使用】
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T extends XPageFragment> Fragment openNewPage(Class<T> clazz) {
        return new PageOption(clazz)
                .setNewActivity(true)
                .open(this);
    }

    /**
     * 打开一个新的页面【建议只在主tab页使用】
     *
     * @param clazzName
     * @param <T>
     * @return
     */
    public <T extends XPageFragment> Fragment openNewPage(String clazzName) {
        return new PageOption(clazzName)
                .setAnim(CoreAnim.slide)
                .setNewActivity(true)
                .open(this);
    }


    /**
     * 打开一个新的页面【建议只在主tab页使用】
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T extends XPageFragment> Fragment openNewPage(Class<T> clazz, @NonNull Class<? extends XPageActivity> containActivityClazz) {
        return new PageOption(clazz)
                .setNewActivity(true)
                .setContainActivityClazz(containActivityClazz)
                .open(this);
    }

    /**
     * 打开一个新的页面【建议只在主tab页使用】
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T extends XPageFragment> Fragment openNewPage(Class<T> clazz, String key, Object value) {
        PageOption option = new PageOption(clazz).setNewActivity(true);
        return openPage(option, key, value);
    }

    public Fragment openPage(PageOption option, String key, Object value) {
        if (value instanceof Integer) {
            option.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            option.putFloat(key, (Float) value);
        } else if (value instanceof String) {
            option.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            option.putBoolean(key, (Boolean) value);
        } else if (value instanceof Long) {
            option.putLong(key, (Long) value);
        } else if (value instanceof Double) {
            option.putDouble(key, (Double) value);
        } else if (value instanceof Parcelable) {
            option.putParcelable(key, (Parcelable) value);
        } else if (value instanceof Serializable) {
            option.putSerializable(key, (Serializable) value);
        } else {
            option.putString(key, serializeObject(value));
        }
        return option.open(this);
    }

    /**
     * 打开页面
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T extends XPageFragment> Fragment openPage(Class<T> clazz, boolean addToBackStack, String key, String value) {
        return new PageOption(clazz)
                .setAddToBackStack(addToBackStack)
                .putString(key, value)
                .open(this);
    }

    /**
     * 打开页面
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T extends XPageFragment> Fragment openPage(Class<T> clazz, String key, Object value) {
        return openPage(clazz, true, key, value);
    }

    /**
     * 打开页面
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T extends XPageFragment> Fragment openPage(Class<T> clazz, boolean addToBackStack, String key, Object value) {
        PageOption option = new PageOption(clazz).setAddToBackStack(addToBackStack);
        return openPage(option, key, value);
    }

    /**
     * 打开页面
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T extends XPageFragment> Fragment openPage(Class<T> clazz, String key, String value) {
        return new PageOption(clazz)
                .putString(key, value)
                .open(this);
    }

    /**
     * 打开页面,需要结果返回
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T extends XPageFragment> Fragment openPageForResult(Class<T> clazz, String key, Object value, int requestCode) {
        PageOption option = new PageOption(clazz).setRequestCode(requestCode);
        return openPage(option, key, value);
    }

    /**
     * 打开页面,需要结果返回
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T extends XPageFragment> Fragment openPageForResult(Class<T> clazz, String key, String value, int requestCode) {
        return new PageOption(clazz)
                .setRequestCode(requestCode)
                .putString(key, value)
                .open(this);
    }

    /**
     * 打开一个新的页面,需要结果返回
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T extends XPageFragment> Fragment openNewPageForResult(Class<T> clazz, String key, String value, int requestCode) {
        return new PageOption(clazz)
                .setNewActivity(true)
                .setRequestCode(requestCode)
                .putString(key, value)
                .open(this);
    }

    /**
     * 打开页面,需要结果返回
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T extends XPageFragment> Fragment openPageForResult(Class<T> clazz, int requestCode) {
        return new PageOption(clazz)
                .setRequestCode(requestCode)
                .open(this);
    }

    /**
     * 序列化对象
     *
     * @param object
     * @return
     */
    public String serializeObject(Object object) {
        return XRouter.getInstance().navigation(SerializationService.class).object2Json(object);
    }

    /**
     * 反序列化对象
     *
     * @param input
     * @param clazz
     * @return
     */
    public <T> T deserializeObject(String input, Type clazz) {
        return XRouter.getInstance().navigation(SerializationService.class).parseObject(input, clazz);
    }

    protected Timer MainTimer = null;

    protected TimerTask MainTask = null;


    protected void TimerEverySecond() {
        mAdapter.refresh(m_ScanList);
    }

    protected Handler MainHandler = new Handler();

    protected Runnable mUpdateResults = new Runnable() {
        public void run() {
            TimerEverySecond();
        }
    };

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    protected void stop_second_timer()
    {
        if (MainTimer != null)
        {
            MainTimer.cancel();
            MainTimer	= null;
        }
    }

    protected void start_second_timer()
    {
        if (MainTimer != null)
        {
            MainTimer.cancel();
            MainTimer	= null;
        }

        MainTask = new TimerTask() {
            @Override
            public void run() {
                MainHandler.post(mUpdateResults);
            }
        };

        MainTimer = new Timer();
        MainTimer.schedule(MainTask,0,1000);
    }

    protected RefreshHeader getRefreshHeader(String item) {
        try {
            Class<?> headerClass = Class.forName("com.scwang.smartrefresh.header." + item);
            Constructor<?> constructor = headerClass.getConstructor(Context.class);
            return (RefreshHeader) constructor.newInstance(getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //BLE
    @Override
    public void onRequestPermissionsResult(final int requestCode, final @NonNull String[] permissions, final @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_REQ_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // We have been granted the Manifest.permission.ACCESS_FINE_LOCATION permission. Now we may proceed with scanning.
                    startScan(ble_filters);
                } else {
                    XToastUtils.toast("Location service is unavailable, we need to get location service to start scanning BLE devices");
                }
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_BLE_CODE) {
            startScan(ble_filters);
        }
    }

    protected void reflash_tables(boolean anmi)
    {
        if (anmi) {
            refreshLayout.autoRefresh();
            return;
        }
        m_ScanList.clear();
        mAdapter.refresh(m_ScanList);
    }

    public static boolean isBleEnabled() {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter != null && adapter.isEnabled();
    }
    /**
     * Scan for 5 seconds and then stop scanning when a BluetoothLE device is found then lEScanCallback
     * is activated This will perform regular scan for custom BLE Service UUID and then filter out.
     *      * using class ScannerServiceParser
     */
    public void startScan(List<ScanFilter> filters) {
        // Since Android 6.0 we need to obtain Manifest.permission.ACCESS_FINE_LOCATION to be able to scan for
        // Bluetooth LE devices. This is related to beacons as proximity devices.
        // On API older than Marshmallow the following code does nothing.
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // When user pressed Deny and still wants to use this functionality, show the rationale
            //ActivityCompat.requestPermissions(getActivity().this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

            XToastUtils.toast("Location service is unavailable, we need to get location service to start scanning BLE devices");
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_REQ_CODE);
            return;
        }

        if (!this.isBleEnabled())
        {
            final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_PERMISSION_BLE_CODE);
        }


        this.ble_filters = filters;

        //adapter.clearDevices();
        stopScan();//先停止扫描

        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        final ScanSettings settings = new ScanSettings.Builder()
                .setLegacy(false)
                //.setUseHardwareBatchingIfSupported(true).build();
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(20).setUseHardwareBatchingIfSupported(false).build();
        scanner.startScan(filters, settings, scanCallback);
    }


    public void set_fragment_is_aviliable(boolean show) {

    }

    /**
     * Stop scan if user tap Cancel button
     */
    public void stopScan() {
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.stopScan(scanCallback);
    }

    protected ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(final int callbackType, @NonNull final ScanResult result) {
        }

        @Override
        public void onBatchScanResults(@NonNull final List<ScanResult> results) {
            onfindbledevice(results);
        }

        @Override
        public void onScanFailed(final int errorCode) {
            // should never be called
        }
    };

    protected void onfindbledevice(List<ScanResult> results)
    {
    }

    protected void initTableAdapter(){
        mAdapter.setViewType(Integer.valueOf(select_show_type).intValue());
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setEnableAutoLoadMore(true);//开启自动加载功能（非必须）


        RefreshHeader header = getRefreshHeader("WaterDropHeader");

        refreshLayout.setRefreshHeader(header);
        refreshLayout.setPrimaryColorsId(R.color.colorPrimary, android.R.color.white);


        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull final RefreshLayout refreshLayout) {
                refreshLayout.getLayout().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        reflash_tables(false);
                        BaseFragment.this.refreshLayout.finishRefresh();
                        BaseFragment.this.refreshLayout.resetNoMoreData();
                        onRefreshFinished();
                    }
                }, 500);
            }
        });

        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull final RefreshLayout refreshLayout) {
                refreshLayout.getLayout().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BaseFragment.this.refreshLayout.finishLoadMore();
                        onLoadMoreFinished();
                    }
                }, 2000);
            }
        });

        mAdapter.setOpenAnimationEnable(false);
        //item 点击测试
        mAdapter.setOnItemClickListener(new SmartViewHolder.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                onTableItemClick(position);
            }
        });

        mAdapter.setOnItemLongClickListener(new SmartViewHolder.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int position) {
                //XToastUtils.toast("长按:" + position);
                onTableItemLongClick(position);
            }
        });
    }

    protected  void onTableItemClick(int position)
    {

    }

    protected  void onTableItemLongClick(int position)
    {

    }

    protected  void onRefreshFinished()
    {

    }

    protected  void onLoadMoreFinished()
    {

    }

}
