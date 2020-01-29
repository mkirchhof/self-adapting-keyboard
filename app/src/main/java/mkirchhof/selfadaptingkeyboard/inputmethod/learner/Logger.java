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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

public class Logger extends Object implements Serializable{
    static final String TAG = "Logger";
    private ArrayList<LogItem> mLog;

    public Logger(){
        mLog = new ArrayList<LogItem>(256);
    }

    public Logger(int startLength){
        mLog = new ArrayList<LogItem>(startLength);
        for(int i = 0; i < startLength; i++){
            mLog.add(new LogItem(i));
        }
    }

    // cannot directly access mLog.get(cursorPos), because mLog also contains deleted elements
    // check manually to find the actual object with fitting cursorPos
    private ListIterator<LogItem> listIterator (int cursorPos, boolean skipDeleted){
        ListIterator<LogItem> it = mLog.listIterator(cursorPos);

        boolean foundPos = false;
        while(it.hasNext() & !foundPos){
            LogItem cur = it.next();
            if(cur.getTextPos() == cursorPos & (!skipDeleted | !cur.isDeleted())){
                foundPos = true;
                it.previous();
            }
        }
        return it;
    }

    public Iterator<LogItem> iterator(){
        return mLog.iterator();
    }

    // decreases the textPos of all upcoming LogItems in the iterator by 1
    private void decreaseFollowingTextPos (ListIterator<LogItem> it){
        while(it.hasNext()){
            LogItem cur = it.next();
            cur.setTextPos(cur.getTextPos() - 1);
        }
    }

    // increases the textPos of all upcoming LogItems in the iterator by 1
    private void increaseFollowingTextPos (ListIterator<LogItem> it){
        while(it.hasNext()){
            LogItem cur = it.next();
            cur.setTextPos(cur.getTextPos() + 1);
        }
    }

    // sets a LogItem at the current cursorPos. If there has already been an LogItem at that pos
    // that was deleted, it overrides that one
    private void set(int cursorPos, int code, char text, int posX, int posY, int keyboardHash){
        if(cursorPos < 0){
            return;
        }
        ListIterator<LogItem> it = listIterator(cursorPos, false);

        // insert the new information
        if(!it.hasNext()){
            it.add(new LogItem(cursorPos, code, text, posX, posY, keyboardHash));
        } else {
            LogItem cur = it.next();
            if(cur.isDeleted()){
                cur.set(cursorPos, code, text, posX, posY, keyboardHash);
            } else {
                it.previous();
                it.add(new LogItem(cursorPos, code, text, posX, posY, keyboardHash));
            }
        }

        increaseFollowingTextPos(it);
    }

    // sets a LogItem at the current cursorStart. In case cursorStart < cursorEnd (such as when
    // something is selected and then a key is depressed), deletes the selection first
    public void set(int cursorStart, int cursorEnd, int code, char text, int posX, int posY, int keyboardHash){
        if(cursorStart < cursorEnd){
            delete(cursorStart, cursorEnd);
        }
        set(cursorStart, code, text, posX, posY, keyboardHash);
    }

    // is called when the user deletes a character
    private void delete(int cursorPos){
        if(cursorPos < 0){
            return;
        }

        ListIterator<LogItem> it = listIterator(cursorPos, true);
        if(it.hasNext()){
            it.next().delete();
        }

        decreaseFollowingTextPos(it);
    }

    private void add(LogItem li){
        mLog.add(li);
    }

    // Appends another Logger item to this.
    // WARNING: No checks regarding textPos etc. are made.
    public void append(Logger logger){
        mLog.addAll(logger.mLog);
    }

    // delete (possibly) several characters
    // Example: Text is abcde, cursorStart is 1 (that is: before b) and cursorEnd is 3 (that is
    // before d), then b and c are deleted
    public void delete(int cursorStart, int cursorEnd){
        int nDeletions = cursorEnd - cursorStart;
        do{
            delete(cursorStart);
        } while(--nDeletions > 0);
    }

    // TODO: What happens when user is swiping key?
    public void cut(int cursorStart, int cursorEnd){
        delete(cursorStart, cursorEnd);
    }

    // insert count characters
    public void insert(int cursorStart, int cursorEnd, int count){
        if(cursorStart < cursorEnd) {
            delete(cursorStart, cursorEnd);
        }
        for(int i = 0; i < count; i++){
            set(cursorStart + i, 0, ' ', 0, 0, 0);
        }
    }

    // saves the current Logger to a cache file prior to its destruction
    //TODO: Throws objectNotSerializableException
    public String save(Context context){
        try {
            File folder = new File(context.getCacheDir(),"Logger");
            if(!folder.exists()){
                folder.mkdir();
            }
            File tempFile = File.createTempFile("Logger", ".ser", folder);
            FileOutputStream fos = new FileOutputStream(tempFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            // write number of objects to the file so that the load method will know when the file ends
            // (otherwise, this is only possible in ObjectInputStream by using exceptions which is bad style)
            oos.writeInt(mLog.size());
            Iterator<LogItem> it = mLog.listIterator();
            Log.d(TAG, "Starting serialization");
            while(it.hasNext()){
                LogItem cur = it.next();
                if(cur.getCode() != -1 & cur.getPosX() != -1 & cur.getPosY() != -1 & !cur.isDeleted()) {
                    oos.writeObject(cur);
                }
            }

            fos.close();
            oos.close();
            Log.i(TAG, "Logger saved to " + tempFile.getName());
            return tempFile.toString();

        } catch(IOException ex){
            Log.e(TAG, "Error while saving Logger: " + ex.toString());
            return null;
        }
    }

    // loads KeyStat objects from a saved file
    public static Logger load(Context context, String fullPath){
        Logger l = new Logger();
        try {
            //File folder = new File(context.getCacheDir(),"Logger");
            //File file = new File(folder, filename);
            File file = new File(fullPath);
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);

            int nObjects = ois.readInt();
            LogItem cur;
            for(int i = 0; i < nObjects; i++){
                cur = (LogItem) ois.readObject();
                l.add(cur);
            }

            fis.close();
            ois.close();
        } catch(Exception ex){
            Log.e("Logger.load", "Could not read Logger: " + ex.toString());
        }
        return l;
    }

    // validates if the currently logged text matches the actual text given as argument
    // TODO: does not yet work with any non-standard key (like aliasses)
    public boolean matchesText(String actualText){
        String logText = getText();

        if(logText.length() != actualText.length()){
            return false;
        }

        for(int i = 0; i < logText.length(); i++){
            char actualChar = actualText.charAt(i);
            char logChar = logText.charAt(i);
            // ' ' is the "unknown-char" char in the log
            if(logChar != ' ' & actualChar != logChar){
                return false;
            }
        }

        return true;
    }

    // returns all deleted and non deleted logItems in the list
    // first index of array is the item index, second it 0 for textPos, 1 for code, 2 for posX,
    // 3 for posY and 4 for isDeleted (1 is true, 0 else)
    // onlyComplete - whether to return all LogItems or just LogItems that have no missing info
    private int[][] toArray(boolean onlyComplete){
        if(mLog.size() == 0){
            return null;
        }

        int[][] arr = new int[mLog.size()][6];

        ListIterator<LogItem> it = mLog.listIterator();
        int i =  0;
        while(it.hasNext()){
            LogItem cur = it.next();

            if(!onlyComplete || (cur.getCode() != -1 & cur.getPosX() != -1 & cur.getPosY() != -1 & !cur.isDeleted())){
                arr[i][0] = cur.getTextPos();
                arr[i][1] = cur.getCode();
                arr[i][2] = cur.getPosX();
                arr[i][3] = cur.getPosY();
                arr[i][4] = cur.isDeleted() ? 1 : 0;
                arr[i][5] = cur.getKeyboardHash();

                i++;
            }
        }

        return arr;
    }

    public int[][] toArray(){
        return toArray(false);
    }

    // returns all non-deleted text currently logged
    public String getText(){
        String text = "";
        ListIterator<LogItem> it = mLog.listIterator();

        while(it.hasNext()){
            LogItem cur = it.next();
            if(!cur.isDeleted()){
                text += cur.getText();
            }
        }

        return text;
    }

    public int size(){
        return mLog.size();
    }

    public class LogItem extends Object implements Serializable {
        private static final long serialVersionUID = 2L;

        private int mTextPos;
        private int mCode;
        private char mText;
        private int mPosX;
        private int mPosY;
        private int mKeyboardHash;
        private boolean mIsDeleted;

        public LogItem(int textPos){
            mIsDeleted = false;
            set(textPos, -1, ' ', -1, -1, 0);
        }

        public LogItem(int textPos, int code, char text, int posX, int posY, int keyboardHash){
            mIsDeleted = false;
            set(textPos, code, text, posX, posY, keyboardHash);
        }

        public void set(int textPos, int code, char text, int posX, int posY, int keyboardHash){
            // Don't overwrite the original typing position if available
            if(!mIsDeleted | mPosX == -1 | mPosY == -1) {
                mPosX = posX;
                mPosY = posY;
                mKeyboardHash = keyboardHash;
            }
            mTextPos = textPos;
            mCode = code;
            mText = text;
            mIsDeleted = false;
        }

        public void setTextPos(int textPos){
            mTextPos = textPos;
        }

        public void delete(){
            mIsDeleted = true;
        }

        public int getTextPos() {return mTextPos; }

        public int getCode(){
            return mCode;
        }

        public char getText() {
            return mText;
        }

        public int getPosX(){
            return mPosX;
        }

        public int getPosY(){
            return mPosY;
        }

        public int getKeyboardHash(){ return mKeyboardHash; }

        public boolean isDeleted(){
            return mIsDeleted;
        }

        public String toString(){
            String s = "TextPos = " + mTextPos + ", Code = " + mCode + ", PosX = "
                    + mPosX + ", PosY = " + mPosY + ", isDeleted = " + mIsDeleted +
                    ", keyboardHash = " + mKeyboardHash;
            return s;
        }
    }
}
