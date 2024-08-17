package com.example.multible;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.Objects;

public class BleScanControl {
    private static final String TAG =
            BleScanControl.class.getSimpleName();
    
    // =====================================================
    // 定数
    // =====================================================
    // スキャン時間(msec)
    /** @noinspection FieldCanBeLocal*/
    private final int SCAN_TIME = 10000;
    
    // =====================================================
    // 制御用変数
    // =====================================================
    private final Context mContext;
    private final BluetoothAdapter mBluetoothAdapter;
    private final Handler mUiHandler;
    private final Handler mScanTimeoutHandler;
    private boolean mScanning = false;
    
    
    // =====================================================
    // コンストラクタ
    // =====================================================
    public BleScanControl(Context context) {
        this.mContext = context;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mUiHandler = new Handler();
        this.mScanTimeoutHandler = new Handler();
    }

/*  これらはこのクラス作成前に行う必要があるので
    ここでは削除しておく
    // =====================================================
    // BLEをサポートしているか
    // =====================================================
    public boolean isBleSupported() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    // =====================================================
    // Bluetoothをサポートしているか
    // =====================================================
    public boolean isBluetoothSupported() {
        return mBluetoothAdapter != null;
    }

    // =====================================================
    // BluetoothがEnableか
    // =====================================================
    public boolean isBluetoothEnabled() {
        if (mBluetoothAdapter == null) {
            return false;
        }
        return mBluetoothAdapter.isEnabled();
    }
*/

    // =====================================================
    // スキャン中か?
    // =====================================================
    public Boolean isScanning() {
       return  mScanning;
    }


    // =====================================================
    // scanステータス変更時処理
    // =====================================================
    public void onScanStateChanged(Boolean scanning) {
        if (scanning) {
            mUiHandler.post(() ->  Toast.makeText(mContext,"スキャン開始", Toast.LENGTH_SHORT).show());
        } else {
            mUiHandler.post(() ->  Toast.makeText(mContext,"スキャン停止", Toast.LENGTH_SHORT).show());
        }
    }
    
    
    // =====================================================
    // Scan開始処理
    // =====================================================
    @SuppressLint("MissingPermission")
    public void ScanStart() {
        if (mScanning) {
            Log.i(TAG, "Scanning in progress");
        } else {
            mScanning = true;
            mScanTimeoutHandler.postDelayed(() -> {
                        // スキャンタイムアウト処理
                        Log.i(TAG, "Scan timeout");
                        mScanning = false;
                        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback); //探索を停止
                        onScanStateChanged(mScanning);
                    }, SCAN_TIME);
            // スキャン開始
            Log.i(TAG, "scan start");
            mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
            onScanStateChanged(mScanning);
        }
    }

    // =====================================================
    // Scan停止処理
    // =====================================================
    @SuppressLint("MissingPermission")
    public void ScanStop() {
        if (mScanning) {
            // スキャン中
            Log.i(TAG, "scan cancel");
            mScanning = false;
            mScanTimeoutHandler.removeCallbacksAndMessages(null);       // タイムアウト処理をキャンセル
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback); //探索を停止
            onScanStateChanged(mScanning);
        } else {
            Log.i(TAG, "not Scanning");
        }
    }

    // =====================================================
    // スキャン結果取得後処理
    // 上位側でオーバーライドする
    // =====================================================
    protected void postScanResult(ScanResult result) {
    }


    // =====================================================
    // scan callback
    // =====================================================
    private final ScanCallback mScanCallback = new ScanCallback() {
        // =====================================================
        // scan 結果取得時処理
        // =====================================================
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            // 基底クラスの処理
            super.onScanResult(callbackType, result);

            Log.d(TAG, "DeviceName : " + result.getDevice().getName());
            Log.d(TAG, "DeviceAddr : " + result.getDevice().getAddress());
            Log.d(TAG, "RSSI       : " + result.getRssi());
            Log.d(TAG, "UUID       : " + Objects.requireNonNull(result.getScanRecord()).getServiceUuids());

            // 上位側クラスでの処理
            postScanResult(result);
        }

        // =====================================================
        // scan 失敗時処理
        // =====================================================
        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, String.format("onScanFailed() : scan error: %d", errorCode));

            // 基底クラスの処理
            super.onScanFailed(errorCode);

            // スキャン停止
            ScanStop();
        }
    };
}
