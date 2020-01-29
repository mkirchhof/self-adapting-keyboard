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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.ListIterator;

// stores statistical information of all keys of a keyboard in KeyStat objects
public class KeyStats extends LinkedList<KeyStat>{
    final static String TAG = KeyStats.class.getSimpleName();
    private final int mKeyboardHash;

    public KeyStats(int keyboardHash){
        super();
        mKeyboardHash = keyboardHash;
    }

    public int getKeyboardHash(){ return mKeyboardHash; }

    // merges the information on means and variances of two keyboards (two learning sessions of the same keyboard)
    public void merge(@NonNull KeyStats other){
        ListIterator<KeyStat> it = other.listIterator();
        while(it.hasNext()){
            KeyStat cur = it.next();
            KeyStat existingKeyStat = findCode(cur.getCode());
            if(existingKeyStat == null){
                add(cur);
            } else {
                existingKeyStat.merge(cur);
            }
        }
    }

    // Adds a new Key press to one of the KeyStat objects or to a new one
    public void add(int code, int x, int y){
        KeyStat existingKeyStat = findCode(code);
        if(existingKeyStat == null){
            add(new KeyStat(code, x, y));
        } else {
            existingKeyStat.add(x, y);
        }
    }

    // searches for a KeyStat object in the list that has the given key code.
    // Returns null if nothing is found
    public KeyStat findCode(int code){
        ListIterator<KeyStat> it = listIterator();
        while(it.hasNext()){
            KeyStat cur = it.next();
            if(cur.getCode() == code){
                return cur;
            }
        }
        return null;
    }

    // writes all KeyStat objects managed by this object to an internal file
    public void save(Context context){
        try {
            File folder = new File(context.getFilesDir(),"KeyStats");
            if(!folder.exists()){
                folder.mkdir();
            }
            File file = new File(folder, "KeyStats" + mKeyboardHash + ".ser");
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            // write number of objects to the file so that the load method will know when the file ends
            // (otherwise, this is only possible in ObjectInputStream by using exceptions which is bad style)
            oos.writeInt(size());
            ListIterator<KeyStat> it = listIterator();
            while(it.hasNext()){
                KeyStat cur = it.next();
                oos.writeObject(cur);
            }

            fos.close();
            oos.close();
        } catch(IOException ex){
            Log.e(TAG, "Could not save KeyStats: " + ex.toString());
        }
    }

    // loads KeyStat objects from a saved file
    // TODO: serialize the whole object instead of its ingredients
    public static KeyStats load(Context context, int keyboardHash){
        KeyStats ks = new KeyStats(keyboardHash);
        try {
            File folder = new File(context.getFilesDir(),"KeyStats");
            File file = new File(folder, "KeyStats" + keyboardHash + ".ser");
            if(file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);

                int nObjects = ois.readInt();
                KeyStat cur;
                for (int i = 0; i < nObjects; i++) {
                    cur = (KeyStat) ois.readObject();
                    ks.add(cur);
                }

                fis.close();
                ois.close();
            } else {
                Log.d("KeyStats.load", "Could not find KeyStats for keyboard " + keyboardHash);
            }
        } catch(Exception ex){
            Log.e("KeyStats.load", "Could not read KeyStats for hash " + keyboardHash +
                    ": " + ex.toString());
        }
        return ks;
    }

    public String toString(){
        ListIterator<KeyStat> it = listIterator();
        String str = "";

        while(it.hasNext()){
            KeyStat cur = it.next();
            str += cur.toString();
        }

        return str;
    }

    // returns the number of logged datapoints across all keys
    public int getN(){
        ListIterator<KeyStat> it = listIterator();
        int sum = 0;

        while(it.hasNext()){
            KeyStat cur = it.next();
            sum += cur.getN();
        }

        return sum;
    }

    // reduces the learnt data to a certain percentage evenly across all keys
    public void reduceData(double percentage){
        ListIterator<KeyStat> it = listIterator();

        while(it.hasNext()){
            KeyStat cur = it.next();
            cur.reduceData(percentage);
        }
    }
}
