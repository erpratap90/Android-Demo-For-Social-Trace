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

/**
 * author alvin
 * since 2020-03-17
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.meeblue.checkblue.ble.callback.BLEMainDataCallback;
import com.meeblue.checkblue.ble.struct.Sensor_data_t;
import com.meeblue.checkblue.fragment.utils.BLEUtils;

/**
 * CSV操作(导出和导入)
 *
 */
public class CSVUtil {

    public static void isExist(String path) {//判断文件夹是否存在,如果不存在则创建文件夹
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public static final String csv_segmentation_symbol = ",";



    public static String get_share_file_path_by_mac(Context context, String mac)
    {
        isExist(context.getCacheDir()+"/app/");
        return context.getCacheDir()+"/app/"+mac+".csv";
    }

    public static String get_configuration_file_path_by_mac(Context context, String mac)
    {
        isExist(context.getCacheDir()+"/app/");
        return context.getCacheDir()+"/app/configuration_"+mac+".csv";
    }


    public static String get_history_file_path_by_mac(Context context,String mac)
    {
        isExist(context.getCacheDir()+"/app/");
        return context.getCacheDir()+"/app/th_history_"+mac+".bin";
    }



    public static void share_file(Context context, String fileUrl)
    {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        Uri FileUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                new File(fileUrl));
        //shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(fileUrl)));
        shareIntent.putExtra(Intent.EXTRA_STREAM, FileUri);
        shareIntent.setType("text/plain");
        context.startActivity(shareIntent);
    }

    /**
     * 导出
     *
     * @param file csv文件(路径+文件名)，csv文件不存在会自动创建
     * @param dataList 数据
     * @return
     */
    public static String exportCsv(String path, List<String> dataList){
        File file =new File(path);
        boolean isSucess=false;
        FileOutputStream out=null;
        OutputStreamWriter osw=null;
        BufferedWriter bw=null;
        try {
            out = new FileOutputStream(file);
            osw = new OutputStreamWriter(out);
            bw =new BufferedWriter(osw);
            if(dataList!=null && !dataList.isEmpty()){
                for(String data : dataList){
                    bw.append(data).append("\r\n");
                }
            }
            isSucess=true;
        } catch (Exception e) {
            isSucess=false;
        }finally{
            if(bw!=null){
                try {
                    bw.close();
                    bw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(osw!=null){
                try {
                    osw.close();
                    osw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(out!=null){
                try {
                    out.close();
                    out=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (isSucess)  return path;
        return null;
    }

    /**
     * 导入
     *
     * @param file csv文件(路径+文件)
     * @return
     */
    public static List<String> importCsv(File file){
        List<String> dataList=new ArrayList<String>();
        BufferedReader br=null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = "";
            while ((line = br.readLine()) != null) {
                dataList.add(line);
            }
        }catch (Exception e) {
        }finally{
            if(br!=null){
                try {
                    br.close();
                    br=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return dataList;
    }


    public static void writeTHBytesToFile(ArrayList<Sensor_data_t> write, Context context, BluetoothDevice device, BLEMainDataCallback callback) {

        ByteBuffer export = ByteBuffer.allocate(write.size()*8);
        for (Sensor_data_t temp : write)
        {
            export.put(temp.getCombination());
        }

        writeFileToSDCard(export.array(), get_history_file_path_by_mac(context, device.getAddress().replace(":", "")), true, false, callback);
    }

    public static ArrayList<Sensor_data_t> readTHBytesToBuffer(Context context, BluetoothDevice device) {
        byte[] temp =  readFileToByteArray(get_history_file_path_by_mac(context, device.getAddress().replace(":", "")));
        ArrayList<Sensor_data_t> read = new ArrayList<Sensor_data_t>();
        if (temp == null || temp.length < 8) return read;
        int count = (int) temp.length/8;
        for (int i = 0; i < count; i++)
        {
            Sensor_data_t read_buffer = new Sensor_data_t();
            byte[] sub = BLEUtils.subBytes(temp, i*8, 8);
            read_buffer.setCombination(sub);
            read.add(read_buffer);
        }
        BLEUtils.DEBUG_PRINTF("Count:"+read.size());
        return read;

    }

    public synchronized static void writeFileToSDCard(@NonNull final byte[] buffer, @Nullable final String fileName, final boolean append, final boolean autoLine, BLEMainDataCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                BLEUtils.DEBUG_PRINTF("1"+fileName);
                File file = new File(fileName);
                RandomAccessFile raf = null;
                FileOutputStream out = null;
                try {
                    if (append) {
                        //如果为追加则在原来的基础上继续写文件
                        raf = new RandomAccessFile(file, "rw");
                        raf.seek(file.length());
                        raf.write(buffer);
                        if (autoLine) {
                            raf.write("\n".getBytes());
                        }
                    } else {
                        //重写文件，覆盖掉原来的数据
                        out = new FileOutputStream(file);
                        out.write(buffer);
                        out.flush();
                    }
                } catch (IOException e) {
                    callback.onOptionState(null, null, false);
                    e.printStackTrace();
                } finally {
                    try {
                        if (raf != null) {
                            raf.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                        callback.onOptionState(null, null, true);
                    } catch (IOException e) {
                        callback.onOptionState(null, null, false);
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    private static byte[] readFileToByteArray(String path) {
        File file = new File(path);
        FileInputStream in = null;
        if(!file.exists()) {
            BLEUtils.DEBUG_PRINTF("1"+path);
            return null;
        }
        try {
            in = new FileInputStream(file);
            long inSize = in.getChannel().size();//判断FileInputStream中是否有内容
            if (inSize == 0) {
                BLEUtils.DEBUG_PRINTF("2");
                return null;
            }

            byte[] buffer = new byte[in.available()];//in.available() 表示要读取的文件中的数据长度
            in.read(buffer);  //将文件中的数据读到buffer中
            return buffer;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            BLEUtils.DEBUG_PRINTF("3");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            BLEUtils.DEBUG_PRINTF("4");
            return null;
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException e) {
                BLEUtils.DEBUG_PRINTF("5");
                return null;
            }
            //或IoUtils.closeQuietly(in);
        }
    }

}