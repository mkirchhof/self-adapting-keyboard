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

package mkirchhof.selfadaptingkeyboard.inputmethod.latin.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import mkirchhof.selfadaptingkeyboard.inputmethod.R;
import mkirchhof.selfadaptingkeyboard.inputmethod.learner.LayoutLearnerTask;

public class LearnerSettingsFragment extends SubScreenFragment{
    final static String TAG = LearnerSettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(final Bundle icicle){
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.prefs_screen_learner);
        final Context context = getActivity();

        // Methods to be applied when clicking each of the preference buttons
        Preference reset = findPreference(Settings.PREF_RESET_LAYOUTS);
        reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            @Override
            public boolean onPreferenceClick(Preference preference){
                deleteLayouts(context);
                Toast.makeText(context, "All layouts have been reset to default!", Toast.LENGTH_LONG).show();
                return true;
            }
        });



        /*
        Preference exportBackup = findPreference(Settings.PREF_EXPORT_LAYOUTS);
        exportBackup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            @Override
            public boolean onPreferenceClick(Preference preference){
                // TODO: Make intent to get file save location

                https://stackoverflow.com/questions/36675190/which-android-intent-to-use-to-get-the-save-to-device-behaviour-in-dropbox-app?rq=1
                Intent saveAs = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                saveAs.setType("application/*");
                startActivityForResult(saveAs, 0);
                createBackup(context, path);

                return true;
            }
        });

        Preference importBackup = findPreference(Settings.PREF_IMPORT_LAYOUTS);
        importBackup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            @Override
            public boolean onPreferenceClick(Preference preference){
                // deleteLayouts(context);
                // TODO: Implement unzipping
                return true;
            }
        });
        */

        Preference runLearner = findPreference(Settings.PREF_RUN_LEARNER);
        runLearner.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            @Override
            public boolean onPreferenceClick(Preference preference){
                LayoutLearnerTask task = new LayoutLearnerTask();
                task.execute(context);

                return true;
            }
        } );
    }

    // TODO: Move most of this stuff to utils

    // from https://stackoverflow.com/questions/6683600/zip-compress-a-folder-full-of-files-on-android/14868161
    // zips all keyStats and hitBoxes files together and returns the path to the zip
    private boolean createBackup(Context context, String path){
        Log.d(TAG, "Creating Backup");

        final int BUFFER = 2048;

        try{
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(path);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            // get all the files ready:
            File[] hitboxes = null;
            File[] keyStats = null;
            File hbFolder = new File(context.getFilesDir(),"Hitboxes");
            if(hbFolder.exists()) {
                hitboxes = hbFolder.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File file, String filename) {
                        return filename.startsWith("Hitboxes") & filename.endsWith(".ser");
                    }
                });
            }
            File ksFolder = new File(context.getFilesDir(),"KeyStats");
            if(ksFolder.exists()) {
                keyStats = ksFolder.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File file, String filename) {
                        return filename.startsWith("KeyStats") & filename.endsWith(".ser");
                    }
                });
            }

            // zip the files:
            if(hitboxes != null && hitboxes.length > 0) {
                for (File file : hitboxes) {
                    byte data[] = new byte[BUFFER];
                    String unmodifiedFilePath = file.getPath();
                    String relativePath = unmodifiedFilePath.substring(hbFolder.getParent().length());
                    FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(relativePath);
                    entry.setTime(file.lastModified()); // to keep modification time after unzipping
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
            }
            if(keyStats != null && keyStats.length > 0) {
                for (File file : keyStats) {
                    byte data[] = new byte[BUFFER];
                    String unmodifiedFilePath = file.getPath();
                    String relativePath = unmodifiedFilePath.substring(ksFolder.getParent().length());
                    FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(relativePath);
                    entry.setTime(file.lastModified()); // to keep modification time after unzipping
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
            }

            out.close();
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void deleteLayouts(Context context){
        Log.d(TAG, "Deleting all keyboard layouts");

        // delete all Loggers:
        File folder = new File(context.getCacheDir(),"Logger");
        File[] cachedFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String filename) {
                return filename.startsWith("Logger") & filename.endsWith(".ser");
            }
        });
        if(cachedFiles != null) {
            for (File file : cachedFiles) {
                file.delete();
            }
        }

        // delete all hitboxes:
        folder = new File(context.getFilesDir(),"Hitboxes");
        cachedFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String filename) {
                return filename.startsWith("Hitboxes") & filename.endsWith(".ser");
            }
        });
        if(cachedFiles != null) {
            for (File file : cachedFiles) {
                file.delete();
            }
        }

        // delete all KeyStats:
        folder = new File(context.getFilesDir(),"KeyStats");
        cachedFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String filename) {
                return filename.startsWith("KeyStats") & filename.endsWith(".ser");
            }
        });
        if(cachedFiles != null) {
            for (File file : cachedFiles) {
                file.delete();
            }
        }
    }
}
