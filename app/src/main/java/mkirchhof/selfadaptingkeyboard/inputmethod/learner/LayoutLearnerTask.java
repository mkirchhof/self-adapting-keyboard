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
import android.os.AsyncTask;
import android.widget.Toast;

public class LayoutLearnerTask extends AsyncTask<Context, Void, Context> {
    protected Context doInBackground(Context... cn){
        LayoutLearner ll = new LayoutLearner(cn[0]);
        ll.learn();
        return cn[0];
    }

    protected void onPostExecute(Context cn){
        Toast.makeText(cn, "Learning complete!", Toast.LENGTH_SHORT).show();
    }
}
