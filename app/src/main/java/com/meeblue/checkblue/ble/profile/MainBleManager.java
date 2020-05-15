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

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.BleManagerCallbacks;
import no.nordicsemi.android.log.ILogSession;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.log.Logger;

/**
 * The manager that logs to nRF Logger. If nRF Logger is not installed, logs are ignored.
 *
 * @param <T> the callbacks class.
 */
public abstract class MainBleManager<T extends BleManagerCallbacks> extends BleManager<T> {
	private ILogSession logSession;

	/**
	 * The manager constructor.
	 * <p>
	 * After constructing the manager, the callbacks object must be set with
	 * {@link #setManagerCallbacks(BleManagerCallbacks)}.
	 *
	 * @param context the context.
	 */
	public MainBleManager(@NonNull final Context context) {
		super(context);
	}

	/**
	 * Sets the log session to log into.
	 *
	 * @param session nRF Logger log session to log inti, or null, if nRF Logger is not installed.
	 */
	public void setLogger(@Nullable final ILogSession session) {
		logSession = session;
	}

	@Override
	public void log(final int priority, @NonNull final String message) {
		Logger.log(logSession, LogContract.Log.Level.fromPriority(priority), message);
		Log.println(priority, "BleManager", message);
	}
}
