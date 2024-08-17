package com.example.multible;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.UUID;

public class GattCallbackRandomSensor3 extends BluetoothGattCallback {
    private static final String TAG =
                GattCallbackRandomSensor3.class.getSimpleName();

    // =====================================================
    //UUID
    // =====================================================
    // Service UUID
    private static final UUID UUID_MY_SERVICE           = UUID.fromString("c9d40fb0-37b0-4c42-94bc-770c3d553a17");
    // DATA1 Characteristic UUID
    private static final UUID UUID_DATA1_CHARACTERISTIC = UUID.fromString("c9d40fb1-37b0-4c42-94bc-770c3d553a17");
    // DATA2 Characteristic UUID
    private static final UUID UUID_DATA2_CHARACTERISTIC = UUID.fromString("c9d40fb2-37b0-4c42-94bc-770c3d553a17");
    // DATA3 Characteristic UUID
    private static final UUID UUID_DATA3_CHARACTERISTIC = UUID.fromString("c9d40fb3-37b0-4c42-94bc-770c3d553a17");

    // =====================================================
    // MTU変更要求サイズ
    // =====================================================
    /** @noinspection FieldCanBeLocal*/
    private final int REQUEST_MTU_SIZE = 256;

    // =====================================================
    // Androidの固定値
    // =====================================================
    // Client Characteristic Configuration Descriptor
    private static final UUID UUID_CCCD                 = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // =====================================================
    // 制御用変数
    // =====================================================
    public        BluetoothGatt     mBleGatt            = null;

    // Characteristic
    private BluetoothGattCharacteristic mBluetoothGattCharacteristic_data1;
    private BluetoothGattCharacteristic mBluetoothGattCharacteristic_data2;
    private BluetoothGattCharacteristic mBluetoothGattCharacteristic_data3;

    // =====================================================
    // コンストラクタ
    // =====================================================
    public GattCallbackRandomSensor3() {
    }

    // =====================================================
    // 接続時処理
    // 上位側でオーバライドする
    // =====================================================
    protected void onConnected(){
        // mUiHandler.post(() -> mDisp.DispToastMessage("接続しました"));
    }

    // =====================================================
    // 切断時処理
    // 上位側でオーバライドする
    // =====================================================
    protected void onDisconnected(){
    }

    // =====================================================
    // データ受信時処理
    // 上位側でオーバライドする
    // =====================================================
    protected void onDataReceived(Integer data1, Integer data2, byte[] data3){
    }

    // =====================================================
    // 接続中かの問い合わせ
    // =====================================================
    public Boolean isConnected() {
        return mBleGatt != null;
    }

    // =====================================================
    // 切断要求
    // 上位側/内部から切断を要求する
    // =====================================================
    @SuppressLint("MissingPermission")
    public void Disconnect(){
        if (mBleGatt != null) {
            // 接続中
            Log.i(TAG, "disconnect");
            mBleGatt.close();
            mBleGatt = null;
    
            // 上位側へ切断を通知
            onDisconnected();
        }
        // 未接続なら何もしない
    }

    // =====================================================
    // 接続ステータス変更時処理
    // =====================================================
    @SuppressLint("MissingPermission")
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        // 基底クラスの処理
        super.onConnectionStateChange(gatt, status, newState);

        if (newState == BluetoothProfile.STATE_CONNECTED) {
            // 接続時、MTU拡張を要求
            if (gatt.requestMtu(REQUEST_MTU_SIZE)) {
                Log.d(TAG, "Requested MTU successfully");
            } else {
                // MTU拡張要求失敗
                Log.e(TAG, "Failed to request MTU");
            }
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            // ペリフェラルが応答しなくなった時など
            Disconnect();
        }
    }

    // =====================================================
    // MTU変更時処理
    // =====================================================
    @SuppressLint("MissingPermission")
    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.e(TAG, "Change MTU size failed error : " + status);
        } else {
            Log.i(TAG, "New MTU Size: " + mtu);

            if (gatt != null) {
                // サービスの検索
                gatt.discoverServices();
            } else {
                // たぶんないけど念のため
                Log.e(TAG, "gatt is null");
            }
        }
    }

    // =====================================================
    // サービス検索完了時処理
    // =====================================================
    @SuppressLint("MissingPermission")
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        // 基底クラスの処理
        super.onServicesDiscovered(gatt, status);

        if (status == BluetoothGatt.GATT_SUCCESS) {
            // 上位側へ接続を通知
            onConnected();
            
            BluetoothGattService service = gatt.getService(UUID_MY_SERVICE);
            if (service != null) {
                mBleGatt = gatt;
                mBluetoothGattCharacteristic_data1 = service.getCharacteristic(UUID_DATA1_CHARACTERISTIC);
                mBluetoothGattCharacteristic_data2 = service.getCharacteristic(UUID_DATA2_CHARACTERISTIC);
                mBluetoothGattCharacteristic_data3 = service.getCharacteristic(UUID_DATA3_CHARACTERISTIC);

                // DATA1 Notification 有効化
                if (mBluetoothGattCharacteristic_data1 != null) {
                    // ペリフェラル側のNotificationを有効化
                    Log.d(TAG, "data1 Notification register request");
                    EnablePeripheralNotification(mBluetoothGattCharacteristic_data1);
                }
                // 以降は onDescriptorWrite() で行う
            }
        } else {
            // サービス検索失敗 → 切断する
            Log.e(TAG, "Services Discovered failed error : " + status);
            Disconnect();
        }
    }

    // =====================================================
    // ディスクリプタ書き込み完了時処理
    //     ディスクリプタを連続で書き込むと正常に書き込めないため
    //     書き込み終了を待って次の書き込みを行う
    // =====================================================
    @SuppressLint("MissingPermission")
    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
        String uuid = characteristic.getUuid().toString();

        Log.d(TAG, "onDescriptorWrite() :");
        Log.d(TAG, "gatt           = " + gatt);
        Log.d(TAG, "descriptor     = " + descriptor);
        Log.d(TAG, "status         = " + status);
        Log.d(TAG, "characteristic = " + characteristic);
        Log.d(TAG, "uuid           = " + uuid);

        if (characteristic == mBluetoothGattCharacteristic_data1) {
            // DATA1 Notification 設定完了
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // DATA1 Notification 設定成功
                // セントラル側のNotificationを有効化
                @SuppressLint("MissingPermission") boolean registered = gatt.setCharacteristicNotification(characteristic, true);
                if (registered) {
                    Log.d(TAG, "data1 Notification register OK");
                } else {
                    Log.e(TAG, "data1 Notification register NG");
                }
            } else {
                Log.e(TAG, "data1 Notification register NG");
            }

            // DATA2のNotificationを有効にする
            if (mBluetoothGattCharacteristic_data2 != null) {
                // ペリフェラル側のNotificationを有効化
                Log.d(TAG, "data2 Notification register request");
                EnablePeripheralNotification(mBluetoothGattCharacteristic_data2);
            }
        } else if (characteristic == mBluetoothGattCharacteristic_data2) {
            // DATA2 Notification 設定完了
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // DATA2 Notification 設定成功
                // セントラル側のNotificationを有効化
                boolean registered = gatt.setCharacteristicNotification(characteristic, true);
                if (registered) {
                    Log.d(TAG, "data2 Notification register OK");
                } else {
                    Log.e(TAG, "data2 Notification register NG");
                }
            } else {
                Log.e(TAG, "data2 Notification register NG");
            }
            
            // DATA3のNotificationを有効にする
            if (mBluetoothGattCharacteristic_data3 != null) {
                // ペリフェラル側のNotificationを有効化
                Log.d(TAG, "data3 Notification register request");
                EnablePeripheralNotification(mBluetoothGattCharacteristic_data3);
            }
        } else if (characteristic == mBluetoothGattCharacteristic_data3) {
            // DATA3 Notification 設定完了
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // DATA2 Notification 設定成功
                // セントラル側のNotificationを有効化
                boolean registered = gatt.setCharacteristicNotification(characteristic, true);
                if (registered) {
                    Log.d(TAG, "data3 Notification register OK");
                } else {
                    Log.e(TAG, "data3 Notification register NG");
                }
            } else {
                Log.e(TAG, "data3 Notification register NG");
            }
        }
    }

    // =====================================================
    // ペリフェラル側のNotificationを有効化する処理
    // =====================================================
    @SuppressLint("MissingPermission")
    private void EnablePeripheralNotification(BluetoothGattCharacteristic characteristic) {
        BluetoothGattDescriptor cccd_descriptor = characteristic.getDescriptor(UUID_CCCD);
        cccd_descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBleGatt.writeDescriptor(cccd_descriptor);
    }

    // =====================================================
    // Characteristic 変更検出時処理
    // =====================================================
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
        // 基底クラスの処理
        super.onCharacteristicChanged(gatt, characteristic);

        // characteristic のデータとUUID取得
        byte[] RecvByteValue = characteristic.getValue();
        UUID uuid = characteristic.getUuid();

        if (UUID_DATA1_CHARACTERISTIC.equals(uuid)) {
            // DATA1
            int data = (((RecvByteValue[1] << 8) & 0xff00)
                    | ((RecvByteValue[0]) & 0x00ff)
            );
            Log.d(TAG, String.format("Data1 : Array 0x%02x 0x%02x  data 0x%04x", RecvByteValue[0], RecvByteValue[1], data));

            // 上位側へデータ通知
            onDataReceived(data, null, null);

        } else if (UUID_DATA2_CHARACTERISTIC.equals(uuid)) {
            // DATA2
            int data = (((RecvByteValue[1] << 8) & 0xff00)
                    | ((RecvByteValue[0]) & 0x00ff)
            );
            Log.d(TAG, String.format("Data2 : Array 0x%02x 0x%02x  data 0x%04x", RecvByteValue[0], RecvByteValue[1], data));

            // 上位側へデータ通知
            onDataReceived(null, data, null);
        } else if (UUID_DATA3_CHARACTERISTIC.equals(uuid)) {
            int recvlen = RecvByteValue.length;
            Log.d(TAG, String.format("Data3 : Array 0x%02x 0x%02x  ... 0x%02x 0x%02x", RecvByteValue[0], RecvByteValue[1], RecvByteValue[recvlen-2], RecvByteValue[recvlen-1]));

            // DATA3
            // 上位側へデータ通知
            onDataReceived(null, null, RecvByteValue);
        } else {
            Log.e(TAG, "unknown characteristic" + uuid.toString());
        }
    }   // end of onCharacteristicChanged()

}
