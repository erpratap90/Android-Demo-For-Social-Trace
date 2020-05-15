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

package com.meeblue.checkblue.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.meeblue.checkblue.core.BaseActivity;
import com.meeblue.checkblue.core.BaseFragment;
import com.meeblue.checkblue.core.MyViewPager;
import com.meeblue.checkblue.utils.XToastUtils;
import com.meeblue.checkblue.R;
import com.meeblue.checkblue.fragment.global.GlobalFragment;
import com.xuexiang.xaop.annotation.SingleClick;
import com.xuexiang.xui.adapter.FragmentAdapter;
import com.xuexiang.xui.utils.ResUtils;
import com.xuexiang.xutil.XUtil;
import com.xuexiang.xutil.common.ClickUtils;
import com.xuexiang.xutil.common.CollectionUtils;

import butterknife.BindView;

/**
 * author alvin
 * since 2020-03-17
 */
//ViewPager.OnPageChangeListener
public class MainActivity extends BaseActivity implements View.OnClickListener, BottomNavigationView.OnNavigationItemSelectedListener, ClickUtils.OnClick2ExitListener {

    @BindView(R.id.view_pager)
    MyViewPager viewPager;
    /**
     * 底部导航栏
     */
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigation;
    /**
     * 侧边栏
     */

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    private String[] mTitles;
    private BaseFragment[] fragments;



    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initViews();
        initListeners();
    }

    @Override
    protected boolean isSupportSlideBack() {
        return false;
    }

    private void initViews() {

        viewPager.setScrollble(false);
        mTitles = ResUtils.getStringArray(R.array.home_titles);
        this.setTitle(mTitles[0]);


        fragments = new BaseFragment[]{
                new GlobalFragment()
        };

        FragmentAdapter<BaseFragment> adapter = new FragmentAdapter<>(getSupportFragmentManager(), fragments);
        viewPager.setOffscreenPageLimit(mTitles.length - 1);
        viewPager.setAdapter(adapter);
    }

    protected void initListeners() {
        bottomNavigation.setOnNavigationItemSelectedListener(this);
    }


    @SingleClick
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nav_header:
                XToastUtils.toast("...");
                break;
            default:
                break;
        }
    }

    //================Navigation================//

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int index = CollectionUtils.arrayIndexOf(mTitles, menuItem.getTitle());
        if (index != -1) {
            this.setTitle(menuItem.getTitle());
            viewPager.setCurrentItem(index, false);
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ClickUtils.exitBy2Click(2000, this);
        }
        return true;
    }

    @Override
    public void onRetry() {
        XToastUtils.toast("Click Again to Exit");
    }

    @Override
    public void onExit() {
        XUtil.get().exitApp();
    }
}
