package com.example.multible;

import android.annotation.SuppressLint;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


public class ScanFragment extends Fragment {
    private static final String TAG =
                ScanFragment.class.getSimpleName();

    // =====================================================
    // CONNECTボタンのtext変更のために保存しておく
    // =====================================================
    Button mScanButton;

    // =====================================================
    // BLEスキャンコントロール
    // =====================================================
    private BleScanControl mScanControl;

    // =====================================================
    // RecyclerViewのインスタンス
    // =====================================================
    RecyclerView mRecyclerView;

    // =====================================================
    // RecycleView.Adapterのインスタンス
    // =====================================================
    ScanRecycleViewAdapter mAdapter;

    // =====================================================
    // コンストラクタ
    // =====================================================
    public ScanFragment() {
        // Required empty public constructor
    }

    // =====================================================
    // Fragmentが生成される時にコールされる
    // =====================================================
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
    }

    // =====================================================
    // FragmentのUIを描画する時にコールされる
    // 描画するViewのセットアップを行う
    // =====================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        // RecyclerViewの作成
        mRecyclerView = view.findViewById(R.id.TestRecyclerView);
        // 固定サイズ最適化有効(項目数以外のサイズが変更されない場合)
        mRecyclerView.setHasFixedSize(true);

        ItemDecoration itemDecoration =
                new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);

        // LinearLayoutManagerの作成と登録
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(llm);

        // adapterの作成と登録
        mAdapter = new ScanRecycleViewAdapter(getActivity()){
            // クリック時の処理をオーバーライド
            @Override
            protected void onItemClicked(int position, @NonNull ScanData data) {
                super.onItemClicked(position, data);
            }

            // データ追加処理をオーバーライドして最新位置までスクロールを追加
            @Override
            public int addData(ScanResult result) {
                int position = super.addData(result);

                // 追加した位置までスクロール
                mRecyclerView.scrollToPosition(position);
                return position;
            }
        };
        mRecyclerView.setAdapter(mAdapter);

        // センサ制御インスタンス
        mScanControl = new BleScanControl(requireContext()) {
            // scan結果取得後処理をオーバライド
            @Override
            protected void postScanResult(ScanResult result) {
                super.postScanResult(result);
                
                // データ追加
                mAdapter.addData(result);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onScanStateChanged(Boolean scanning) {
                // 基底クラスの処理
                super.onScanStateChanged(scanning);

                // SCANボタンの表示変更
                if (scanning) {
                    mScanButton.setText("STOP");
                } else {
                    mScanButton.setText("SCAN");
                }
            }
        };

        // SCANボタンのハンドラ登録
        mScanButton = view.findViewById(R.id.ScanButton);
        // end of onClick()
        mScanButton.setOnClickListener(view1 -> {
            Log.d(TAG, "onClick() : SCAN");
            if (!mScanControl.isScanning()) {
                // スキャンしていない
                mScanControl.ScanStart();
            } else {
                // スキャン中
                mScanControl.ScanStop();
            }
        }); // end of mScanButton.setOnClickListener()

        // connectボタンのハンドラ登録
        view.findViewById(R.id.ConnectButton).setOnClickListener(view13 -> {
            Log.d(TAG, "onClick() : CONNECT");

            // スキャン停止
            mScanControl.ScanStop();

            // 選択されたデバイスの配列を取得
            ScanData[] dataArray = mAdapter.getSelectedDeviceData();
            if (dataArray.length == 0) {
                // 一つも選択されていない
                Toast.makeText(requireContext(), "選択されていません", Toast.LENGTH_SHORT).show();
            } else {
                // ConnectFragmentへ切り替え(Argumentは選択されたデバイスの配列)
                NavDirections action = ScanFragmentDirections.actionScanFragmentToConnectFragment(dataArray);
                Navigation.findNavController(view13).navigate(action);
            }
        });

        // CLEARボタンのハンドラ登録
        view.findViewById(R.id.ClearButton).setOnClickListener(view12 -> {
            Log.d(TAG, "onClick() : clear");
            // 全データ削除
            // mAdapter.removeAll();
            mAdapter.removeData();
        });

        return view;
    }

    // =====================================================
    // Fragmentがフォアグラウンドでなくなる時にコールされる
    // 続行しない処理の停止などを行う
    // =====================================================
    @Override
    public void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();

        // スキャン停止
        mScanControl.ScanStop();
    }
}