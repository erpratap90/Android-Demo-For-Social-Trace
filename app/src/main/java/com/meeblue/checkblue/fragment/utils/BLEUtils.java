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
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.meeblue.checkblue.R;
import com.meeblue.checkblue.utils.SettingSPUtils;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

public class BLEUtils {

    public static final String ALARM_NON_CONNECTABLE_UUID = "0000ea70-0000-1000-8000-00805f9b34fb";
    public static final String ALARM_CONNECTABLE_UUID = "0000ea80-0000-1000-8000-00805f9b34fb";

    public static String int2Hex(int bytes, int digits) {

        StringBuilder sb = new StringBuilder("0x");

        String value = Integer.toHexString(bytes);
        for (int i = value.length(); i < digits; i++) sb.append("0");
        sb.append(value);

        return sb.toString().toLowerCase().trim();
    }


    public static String byte2String(byte[] bytes) {

        String stmp = "";
        StringBuilder sb = new StringBuilder("0x");
        for (int n = 0; n < bytes.length; n++) {
            stmp = Integer.toHexString(bytes[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
        }

        return sb.toString().toLowerCase().trim();
    }

    /**
     * Check whether two {@link SparseArray} equal.
     */
    static boolean equals(@Nullable final SparseArray<byte[]> array,
                          @Nullable final SparseArray<byte[]> otherArray) {
        if (array == otherArray) {
            return true;
        }
        if (array == null || otherArray == null) {
            return false;
        }
        if (array.size() != otherArray.size()) {
            return false;
        }

        // Keys are guaranteed in ascending order when indices are in ascending order.
        for (int i = 0; i < array.size(); ++i) {
            if (array.keyAt(i) != otherArray.keyAt(i) ||
                    !Arrays.equals(array.valueAt(i), otherArray.valueAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check whether two {@link Map} equal.
     */
    static <T> boolean equals(@Nullable final Map<T, byte[]> map, Map<T, byte[]> otherMap) {
        if (map == otherMap) {
            return true;
        }
        if (map == null || otherMap == null) {
            return false;
        }
        if (map.size() != otherMap.size()) {
            return false;
        }
        Set<T> keys = map.keySet();
        if (!keys.equals(otherMap.keySet())) {
            return false;
        }
        for (T key : keys) {
            if (!deepEquals(map.get(key), otherMap.get(key))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if both arguments are null,
     * the result of {@link Arrays#equals} if both arguments are primitive arrays,
     * the result of {@link Arrays#deepEquals} if both arguments are arrays of reference types,
     * and the result of {@link #equals} otherwise.
     */
    static boolean deepEquals(final Object a, final Object b) {
        if (a == null || b == null) {
            return a == b;
        } else if (a instanceof Object[] && b instanceof Object[]) {
            return Arrays.deepEquals((Object[]) a, (Object[]) b);
        } else if (a instanceof boolean[] && b instanceof boolean[]) {
            return Arrays.equals((boolean[]) a, (boolean[]) b);
        } else if (a instanceof byte[] && b instanceof byte[]) {
            return Arrays.equals((byte[]) a, (byte[]) b);
        } else if (a instanceof char[] && b instanceof char[]) {
            return Arrays.equals((char[]) a, (char[]) b);
        } else if (a instanceof double[] && b instanceof double[]) {
            return Arrays.equals((double[]) a, (double[]) b);
        } else if (a instanceof float[] && b instanceof float[]) {
            return Arrays.equals((float[]) a, (float[]) b);
        } else if (a instanceof int[] && b instanceof int[]) {
            return Arrays.equals((int[]) a, (int[]) b);
        } else if (a instanceof long[] && b instanceof long[]) {
            return Arrays.equals((long[]) a, (long[]) b);
        } else if (a instanceof short[] && b instanceof short[]) {
            return Arrays.equals((short[]) a, (short[]) b);
        }
        return a.equals(b);
    }

    /**
     * Null-safe equivalent of {@code a.equals(b)}.
     */
    static boolean equals(final Object a, final Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    /**
     * Convenience wrapper for {@link Arrays#hashCode}, adding varargs.
     * This can be used to compute a hash code for an object's fields as follows:
     * {@code Objects.hash(a, b, c)}.
     */
    static int hash(final Object... values) {
        return Arrays.hashCode(values);
    }

    /**
     * Returns "null" for null or {@code o.toString()}.
     */
    static String toString(final Object o) {
        return (o == null) ? "null" : o.toString();
    }


    public static String byteToStringValue(int value, boolean with_start) {

        String stmp="";
        StringBuilder sb = new StringBuilder("");

        stmp = Integer.toHexString(((byte)value) & 0xFF);

        sb.append((stmp.length()==1)? "0"+stmp : stmp);

        if (with_start) return "0x" + sb.toString().toUpperCase().trim();

        return ""+sb.toString().toUpperCase().trim();
    }


    public static String byteToStringValue(byte[] bytes, boolean with_start) {

        String stmp="";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < bytes.length; n++)
        {
            stmp = Integer.toHexString(bytes[n] & 0xFF);
            sb.append((stmp.length()==1)? "0"+stmp : stmp);
        }
        if (with_start) return "0x" + sb.toString().toUpperCase().trim();

        return ""+sb.toString().toUpperCase().trim();
    }

    public static int getUnsignedByte(byte[] bytes) {
        return unsignedByteToInt(bytes[0]);
    }

    public static String normalizeProximityUUID(byte[] bytes) {

        String stmp="";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < bytes.length; n++)
        {
            stmp = Integer.toHexString(bytes[n] & 0xFF);
            sb.append((stmp.length()==1)? "0"+stmp : stmp);
        }

        String Temp = sb.toString().toUpperCase().trim();

        return String.format(
                "%s-%s-%s-%s-%s",
                new Object[] { Temp.substring(0, 8),
                        Temp.substring(8, 12),
                        Temp.substring(12, 16),
                        Temp.substring(16, 20),
                        Temp.substring(20, 32) });
    }

    public static boolean CheckStringIsVaid(String Str)
    {
        String temp = Str.replace("-", "");

        for (int i = 0; i < temp.length(); i++)
        {
            char temp1 = temp.charAt(i);
            if (temp1 != 'a' && temp1 != 'b' && temp1 != 'c' && temp1 != 'd' && temp1 != 'e' && temp1 != 'f'
                    && temp1 != 'A' && temp1 != 'B' && temp1 != 'C' && temp1 != 'D' && temp1 != 'E' && temp1 != 'F'
                    && temp1 != '0' && temp1 != '1' && temp1 != '2' && temp1 != '3' && temp1 != '4'
                    && temp1 != '5' && temp1 != '6' && temp1 != '7' && temp1 != '8' && temp1 != '9')
            {
                return false;
            }
        }
        return true;
    }

    // Helper method to extract bytes from byte array.
    public static byte[] extractBytes(@NonNull final byte[] scanRecord,
                                       final int start, final int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(scanRecord, start, bytes, 0, length);
        return bytes;
    }


    public static byte[] subBytes(@NonNull final byte[] value,
                                      final int start, final int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(value, start, bytes, 0, length);
        return bytes;
    }

    public static boolean isAllFFValue(@NonNull final byte[] value) {

        for (int i = 0; i < value.length; i++)
        {
            if (unsignedByteToInt(value[i]) != 0xFF) return false;
        }

        return true;
    }

    public static int unsignedByteToInt(byte value)
    {
        return value & 0xFF;
    }

    public static int getUnsignedInt8(byte[] bytes, boolean litterfirst) {
        return unsignedByteToInt(bytes[0]);
    }

    public static int getUnsignedInt16(byte[] bytes, boolean litterfirst) {
        if (litterfirst)
        {
            return unsignedByteToInt(bytes[1]) + (unsignedByteToInt(bytes[0]) << 8);
        }
        return unsignedByteToInt(bytes[0]) + (unsignedByteToInt(bytes[1]) << 8);
    }

    public static long getUnsignedInt32(byte[] bytes, boolean litterfirst) {
        if (litterfirst)
        {
            return (unsignedByteToInt(bytes[0]) << 24) + (unsignedByteToInt(bytes[1]) << 16) + (unsignedByteToInt(bytes[2]) << 8) +  unsignedByteToInt(bytes[3]);
        }
        return (unsignedByteToInt(bytes[3]) << 24) + (unsignedByteToInt(bytes[2]) << 16) + (unsignedByteToInt(bytes[1]) << 8) +  unsignedByteToInt(bytes[0]);
    }


    public static long GetLongData(byte[] time, byte[] data) {
        ByteBuffer temp =ByteBuffer.allocate(time.length+data.length);
        temp.put(time);
        temp.put(data);
        return temp.asLongBuffer().get(0);
    }

    public static long GetLongData(byte[] value) {
        ByteBuffer temp =ByteBuffer.allocate(value.length);
        temp.put(value);
        return temp.asLongBuffer().get(0);
    }


    public static byte[] UnsignedIntToBytes(long value, boolean litterfirst, int length) {
        if (litterfirst)
        {
            byte[] temp = new byte[length];
            for(int i = 0;i < length;i++){
                temp[i] = (byte)(value >> (8*(length-1) - i * 8));
            }
            return temp;
        }
        else
        {
            byte[] temp = new byte[length];
            for(int i = 0;i < length;i++){
                temp[length-1-i] = (byte)(value >> (8*(length-1) - i * 8));
            }
            return temp;
        }
    }

    public static byte[] UnsignedInt8ToBytes(int value)
    {
        byte[] temp = new byte[1];
        temp[0] = (byte) (value & 0xFF);
        return temp;
    }

    public static byte[] UnsignedInt16ToBytes(int value, boolean litterfirst) {
        return UnsignedIntToBytes(value, litterfirst, 2);
    }

    public static byte[] UnsignedInt32ToBytes( long value, boolean litterfirst) {
        return UnsignedIntToBytes(value, litterfirst, 4);
    }


    public static void ByteMemcpy(byte[] src, int src_offect, byte[] desc, int desc_offect, int length) {
        System.arraycopy(src, src_offect, desc, desc_offect, length);
    }

    public static void ByteMemset(byte[] desc, int value, int length) {
        for (int i = 0; i < length; i++) desc[i] = (byte) value;
    }

    public static boolean isByteValue(int value) {
        if (value >= -127 && value <= 255)
        {
            return true;
        }
        return false;
    }


    public static String Convert_Temperature(int value) {
        double temperature = -46.85 + 175.72f * value/65536.0f;
        if (!SettingSPUtils.getInstance().temperature_unit_key().equalsIgnoreCase("0"))
        {
            temperature = 9*temperature/5.0f+32;
            return format("%.2f", temperature)+"℉";
        }

        return format("%.2f", temperature)+"℃";
    }

    public static String Convert_Humidity(long value) {
        double humidity = -6 + 125.0f * value / 65536.0f;
        return format("%.2f", humidity)+"%";
    }


    private final static byte[] hex = "0123456789ABCDEF".getBytes();

    public static byte[] HexString2Bytes(String hexstr) {
        if (hexstr.length() == 0 || hexstr.length() % 2 != 0) return null;
        int length = hexstr.length()/2;
        byte[] b = new byte[hexstr.length() / 2];
        int j = 0;
        for (int i = 0; i < b.length; i++) {
            char c0 = hexstr.charAt(j++);
            char c1 = hexstr.charAt(j++);
            b[i] = (byte) ((parse(c0) << 4) | parse(c1));
        }
        return b;
    }

    public static long BytesToLong(byte[] value)
    {
        if (value == null || value.length == 0 || value.length > 4) return 0;

        long temp = 0;

        for (int i = 0; i < value.length; i++)
        {
            long offect = value[i];
            temp = temp + offect << 8*(value.length-1);
        }

        return temp;
    }

    private static int parse(char c) {
        if (c >= 'a')
            return (c - 'a' + 10) & 0x0f;
        if (c >= 'A')
            return (c - 'A' + 10) & 0x0f;
        return (c - '0') & 0x0f;
    }

    public static String Convert_TimeSeconed(long value) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(value * 1000));
    }


    public static String Convert_Time(long value) {
        return new SimpleDateFormat("HH:mm:ss").format(new Date(value));
    }

    public static void DEBUG_PRINTF(String Message) {
        Log.d("Alvin", "Alvin:"+Message);
    }
}
