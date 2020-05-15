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

import androidx.annotation.NonNull;

/**
 * author alvin
 * since 2020-03-17
 */

public abstract class RxIteratorTask<T, R> implements IRxIOTask<T, R>, IRxUITask<R> {

    /**
     * 遍历的是否是数组
     */
    private boolean mIsArray;
    /**
     * IO执行任务的入参, 遍历的集合
     */
    private Iterable<T> Iterable;

    /**
     * IO执行任务的入参, 遍历的数组
     */
    private T[] Array;

    public RxIteratorTask(@NonNull Iterable<T> iterable) {
        Iterable = iterable;
        mIsArray = false;
    }

    public RxIteratorTask(@NonNull T[] array) {
        Array = array;
        mIsArray = true;
    }


    public Iterable<T> getIterable() {
        return Iterable;
    }

    public RxIteratorTask setIterable(Iterable<T> iterable) {
        Iterable = iterable;
        return this;
    }

    public T[] getArray() {
        return Array;
    }

    public RxIteratorTask setArray(T[] array) {
        Array = array;
        return this;
    }

    public boolean isArray() {
        return mIsArray;
    }
}