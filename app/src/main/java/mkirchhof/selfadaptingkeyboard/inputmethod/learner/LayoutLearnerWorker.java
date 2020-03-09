/*
 * Copyright (C) 2020 Michael Kirchhof
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mkirchhof.selfadaptingkeyboard.inputmethod.learner;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

// this is a background task collecting the cached loggers and calculating an optimal keyboard layout
public class LayoutLearnerWorker extends Worker {
    private static final String TAG = LayoutLearnerWorker.class.getSimpleName();
    Context mContext;

    public LayoutLearnerWorker(@NonNull Context context, @NonNull WorkerParameters params){
        super(context, params);
        mContext = context;
    }

    // this is the code chunk that gets called each night
    @Override
    public Result doWork(){
        Log.i(TAG, "Running scheduled LayoutLearner");

        LayoutLearner ll = new LayoutLearner(mContext);

        ll.learn();

        return Result.success();
    }
}
