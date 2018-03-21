package com.example.paulo.attendly;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.nfc.Tag;
import android.os.Environment;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import com.example.paulo.attendly.firebase.connectFirebase;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {
    protected static final String TAG = "MonitoringActivity";
    protected static final String TAG2 = "MonitoringActivity2";
    private BeaconManager beaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        beaconManager = BeaconManager.getInstanceForApplication(this);
//        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
//        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
//         beaconManager.getBeaconParsers().add(new BeaconParser().
//                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
//        beaconManager.bind(this);
        loadFileList();
//        connectFirebase.writeData();
//        connectFirebase.readData();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }
//

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                Log.d(TAG, "BEACONS" + beacons);
                if (beacons.size() > 0) {
                    for (Beacon beacon : beacons) {
                        if (beacon.getDistance() <= 2.0) {
                            Log.i(TAG2, "The first beacon I see is about " + beacons.iterator().next().getDistance() + " meters away.");
                            Log.i(TAG, "The beacon " + beacons.iterator().next().getBluetoothAddress() + " bluetooth");

                        }
                    }


                    Log.i(TAG, "BT Device" + getBluetoothMacAddress());

                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }

    private String getBluetoothMacAddress() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String bluetoothMacAddress = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                Field mServiceField = bluetoothAdapter.getClass().getDeclaredField("mService");
                mServiceField.setAccessible(true);

                Object btManagerService = mServiceField.get(bluetoothAdapter);

                if (btManagerService != null) {
                    bluetoothMacAddress = (String) btManagerService.getClass().getMethod("getAddress").invoke(btManagerService);
                }
            } catch (NoSuchFieldException e) {

            } catch (NoSuchMethodException e) {

            } catch (IllegalAccessException e) {

            } catch (InvocationTargetException e) {

            }
        } else {
            bluetoothMacAddress = bluetoothAdapter.getAddress();
        }
        return bluetoothMacAddress;
    }


    private String[] mFileList;
    private File mPath = new File(Environment.getExternalStorageDirectory() + "//yourdir//");
    private String mChosenFile;
    private static final String FTYPE = ".txt";
    private static final int DIALOG_LOAD_FILE = 1000;

    private void loadFileList() {
        try {
            mPath.mkdirs();
        } catch (SecurityException e) {
            Log.e(TAG, "unable to write on the sd card " + e.toString());
        }
        if (mPath.exists()) {
            FilenameFilter filter = new FilenameFilter() {

                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    return filename.contains(FTYPE) || sel.isDirectory();
                }

            };
            mFileList = mPath.list(filter);
        } else {
            mFileList = new String[0];
        }
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (id) {
            case DIALOG_LOAD_FILE:
                builder.setTitle("Choose your file");
                if (mFileList == null) {
                    Log.e(TAG, "Showing file picker before loading the file list");
                    dialog = builder.create();
                    return dialog;
                }
                builder.setItems(mFileList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mChosenFile = mFileList[which];
                        //you can do stuff with the file here too
                    }
                });
                break;
        }
        dialog = builder.show();
        return dialog;
    }
}