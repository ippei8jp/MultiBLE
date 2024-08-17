package com.example.multible;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.Manifest;

public class MainActivity extends AppCompatActivity {
    private static final String TAG =
                MainActivity.class.getSimpleName();

    // =====================================================
    // Permission関連
    // =====================================================
    private static final int REQUEST_CODE_LOCATE_PERMISSION = 5;    // onRequestPermissionsResult() で結果を受け取るときに使用

    // =====================================================
    // Activityが生成された時に1回コールされる
    // =====================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // BLE初期設定
        // Permissionの設定はFragmentよりActivityでやった方が簡単
        InitializeBleSetting();
    }

    // =====================================================
    // BLEの初期設定(permission関連)
    // =====================================================
    private void InitializeBleSetting() {
        // Bluetooth Adapter
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Bluetoothをサポートしているかの確認
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetoothをサポートしていません", Toast.LENGTH_LONG).show();
            // 終了
            finish();
        } else {
            // Bluetoothをサポートしている
            // BLEをサポートしているかの確認
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, "BLEをサポートしていません", Toast.LENGTH_LONG).show();
                // 終了
                finish();
            }

            // bluetoothがONになっているか確認しOFFならONを要求する
            if (!bluetoothAdapter.isEnabled()) {
                // BluetoothをONする処理をLaunch
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mBtEnableLauncher.launch(enableBtIntent);
            }
        }

        // permission(位置情報)のチェック
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PermissionChecker.PERMISSION_GRANTED){
            // 許可されていない
            // → 位置情報のPermissionを要求
            ActivityCompat.requestPermissions(this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            REQUEST_CODE_LOCATE_PERMISSION
                                        );
            // 結果はonRequestPermissionsResult()で受け取る
        }
    }

    // =====================================================
    // BluetoothをONする処理
    // =====================================================
    final ActivityResultLauncher<Intent> mBtEnableLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
        if (result.getResultCode() != Activity.RESULT_OK) {
            // 許可しなかった
            Toast.makeText(this, "Bluetoothが有効になりませんでした", Toast.LENGTH_LONG).show();
            finish();
        }
    });

    // =====================================================
    // Permission許可の結果を受け取る
    // =====================================================
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_LOCATE_PERMISSION) {
            // 位置情報のpermission
             if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 許可された
                Log.i(TAG, "許可された");
            } else {
                // 許可されなかった
                Log.i(TAG, "許可されなかった");
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // 説明ダイアログを表示(今後表示しないを選択しない限り永遠に出る)
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle("パーミッションの追加説明");
                    dialog.setMessage("このアプリを使うには位置情報の許可が必要です");
                    // OKが押されたら再度permission要求
                    dialog.setPositiveButton("OK",
                            (dialog1, which) -> ActivityCompat.requestPermissions(
                                                                MainActivity.this,
                                                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                                REQUEST_CODE_LOCATE_PERMISSION
                                                            )
                            );
                    dialog.create();
                    dialog.show();
                } else {
                    Log.i(TAG, "今後表示しない");
                }
            }
        }
    }
}