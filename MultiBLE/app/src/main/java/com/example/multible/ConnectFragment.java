package com.example.multible;

import static java.util.Objects.requireNonNull;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

// connect画面
public class ConnectFragment extends Fragment {
    private static final String TAG =
            ConnectFragment.class.getSimpleName();

    // RecyclerViewのインスタンス
    RecyclerView mRecyclerView;
    // RecycleView.Adapterのインスタンス
    ConnectRecycleViewAdapter mAdapter;

    // =====================================================
    // コンストラクタ
    // =====================================================
    public ConnectFragment() {
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
        View view = inflater.inflate(R.layout.fragment_connect, container, false);

        // Argumentの取得
        ScanData[] dataArray = ConnectFragmentArgs.fromBundle(requireNonNull(requireArguments())).getScanData();
        List<ConnectData> connectList = new ArrayList<>();

        // ScanDataからConnectDataを作る
        for (ScanData scanData : dataArray) {
            ConnectData data = new ConnectData(scanData);
            connectList.add(data);
        }

        // RecyclerViewの作成
        mRecyclerView = view.findViewById(R.id.ConnectRecyclerView);
        // 固定サイズ最適化有効(項目数以外のサイズが変更されない場合)
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);

        // LinearLayoutManagerの作成と登録
        LinearLayoutManager llm = new LinearLayoutManager(requireContext());
        mRecyclerView.setLayoutManager(llm);

        // adapterの作成と登録
        mAdapter = new ConnectRecycleViewAdapter(requireContext(), mRecyclerView, connectList);
        mRecyclerView.setAdapter(mAdapter);

        // ドラックアンドドロップの操作を実装する
        ItemTouchHelper itemDecor = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                                                        ItemTouchHelper.UP | ItemTouchHelper.DOWN, 
                                                        ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                final int fromPos = viewHolder.getAdapterPosition();
                final int toPos = target.getAdapterPosition();
                Log.d(TAG, "onMove() : " + fromPos + " -> " + toPos);
                mAdapter.swapData(fromPos, toPos);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int pos = viewHolder.getAdapterPosition();
                Log.d(TAG, "onSwiped() : " + pos);
                mAdapter.removeData(pos);
            }
        });
        itemDecor.attachToRecyclerView(mRecyclerView);

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

        // 全デバイス切断
        mAdapter.disconnectAll();
    }












}