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

package rkr.simplekeyboard.inputmethod.learner;

import android.text.Editable;
import android.text.TextWatcher;

// Watches an EditText for cases in which user cuts or pastes text. As the keyboard does not notice
// this itself, this class is used to keep the given Logger updated.
// TODO: Implement this class. Apparently, TextWatchers can only be given to EditTexts, which we
// do not have access to. As cut and paste events are seldom, we will for now only save and restart
// our logger once a cut or paste is detected.
public class CutPasteListener implements TextWatcher {
    Logger mLogger;

    public CutPasteListener(Logger textLogger){
        super();
        mLogger = textLogger;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        int end = start + count;
        String loggerText = mLogger.getText();
        String actualText = charSequence.toString();
        if(loggerText.length() != actualText.length() ||
                loggerText.substring(start, end) != actualText.substring(start, end)){
            mLogger.insert(start, end + 1, count);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
