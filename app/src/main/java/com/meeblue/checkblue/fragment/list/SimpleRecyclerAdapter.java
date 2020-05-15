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

package com.meeblue.checkblue.fragment.list;

import android.view.View;
import android.view.ViewGroup;

import com.meeblue.checkblue.fragment.utils.BLELocalInfo;
import com.scwang.smartrefresh.layout.adapter.SmartRecyclerAdapter;
import com.scwang.smartrefresh.layout.adapter.SmartViewHolder;
import com.meeblue.checkblue.R;

import java.util.Collection;

/**
 * @author Alvin
 * @since 2020/3/1 11:04
 */
public class SimpleRecyclerAdapter extends SmartRecyclerAdapter<BLELocalInfo> {

    private int view_type = 0;

    public void setViewType(int type)
    {
        view_type = type;
    }
    public SimpleRecyclerAdapter() {
        super(R.layout.global_basic_list_item);
    }


    public SimpleRecyclerAdapter(Collection<BLELocalInfo> data) {
        super(data, R.layout.global_basic_list_item);
    }

    @Override
    protected View generateItemView(ViewGroup parent, int viewType) {
        return getInflate(parent, R.layout.global_basic_list_item);
    }

    @Override
    public int getItemViewType(int position) {
        return view_type;
    }

    /**
     * @param holder
     * @param model
     * @param position
     */
    @Override
    protected void onBindViewHolder(SmartViewHolder holder, BLELocalInfo model, int position) {

        show_global_basic_view(holder, model, position);
    }


    private void show_global_basic_view(SmartViewHolder holder, BLELocalInfo model, int position)
    {
        holder.text(R.id.device_connectable, "Connectable: "+(model.getResult().isConnectable()? "true" : "false"));
        holder.text(R.id.device_mac, "Mac Adress: "+model.getResult().getDevice().getAddress());

        String TX = (model.getResult().getScanRecord().getTxPowerLevel() < -1000) ? BLELocalInfo.NULL_STRING_SHOW : (""+model.getResult().getScanRecord().getTxPowerLevel());
        holder.text(R.id.device_id, "Device ID: "+ model.m_adv_data.device_id);
        holder.text(R.id.device_battery, "Device Battery: "+ model.m_adv_data.battery_votage +"mv");
        holder.text(R.id.alarm_record, "Alarm Record: "+ model.m_adv_data.record_count);

        holder.text(R.id.device_rssi, "Rssi: "+model.getResult().getRssi());
    }

}
