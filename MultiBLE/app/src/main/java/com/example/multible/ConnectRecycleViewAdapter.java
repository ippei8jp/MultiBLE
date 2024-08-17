package com.example.multible;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConnectRecycleViewAdapter extends RecyclerView.Adapter<ConnectRecycleViewAdapter.ConnectViewHolder> {
    private static final String TAG =
                ConnectRecycleViewAdapter.class.getSimpleName();
            
    // =====================================================
    // データ保持リスト
    // =====================================================
    private final List<ConnectData> mDataList;
    
    // =====================================================
    // コンテキスト
    // =====================================================
    private final Context mContext;

    // =====================================================
    // RecyclerView のインスタンス
    // RecyclerView のUIスレッドから実行する必要のあるメソッドがあるため
    // =====================================================
    private final RecyclerView mRecyclerView;

    // =====================================================
    // ViewHolderクラス
    // =====================================================
    public static class ConnectViewHolder extends RecyclerView.ViewHolder {
        /** @noinspection FieldCanBeLocal*/
        private final String TAG =
                    ConnectViewHolder.class.getSimpleName();
        // 何度も読みに行かないようにコンストラクタでキャッシュしておく
        public final TextView nameView;
        public final TextView addressView;
        public final TextView data1View;
        public final TextView data2View;
        public final LinearLayout background;
        public final ImageButton button;

        // background resource
        public final Drawable background_active;
        public final Drawable background_inactive;

        // コンストラクタ
        public ConnectViewHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "constructor");

            // それぞれのViewをキャッシュしておく
            nameView =      itemView.findViewById(R.id.NameBody);
            addressView =   itemView.findViewById(R.id.AddressBody);
            data1View =     itemView.findViewById(R.id.Data1Body);
            data2View =     itemView.findViewById(R.id.Data2Body);
            background =    itemView.findViewById(R.id.ConnectRowLayout);
            button =        itemView.findViewById(R.id.ConnectButton);

            // background resource
            background_active = ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.background_connect_row_item_active, null);
            background_inactive = ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.background_connect_row_item_inactive, null);
       }
    }

    // =====================================================
    // 初期値なしコンストラクタ
    // =====================================================
    public ConnectRecycleViewAdapter(Context context, RecyclerView recyclerView) {
        this(context, recyclerView, new ArrayList<>());
    }

    // =====================================================
    // 初期値ありコンストラクタ
    // =====================================================
    public ConnectRecycleViewAdapter(Context context, RecyclerView recyclerView, List<ConnectData> dataList) {
        this.mDataList = dataList;
        this.mContext = context;
        this.mRecyclerView = recyclerView;
    }

    // =====================================================
    // ViewHolder生成処理
    // =====================================================
    @NonNull
    @Override
    public ConnectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder()  " + viewType);
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.connect_row_item, parent,false);
        
        ConnectViewHolder viewHolder = new ConnectViewHolder(inflate);
        
        // Connectボタンがクリックされたときのハンドラを登録
        viewHolder.button.setOnClickListener(view -> {
            // 上位側のメソッドをcall
            onItemButtonClicked(viewHolder);
        });
        
        return viewHolder;
    }

    // =====================================================
    // 内容が更新されたときの処理
    // データ追加時、notifyItemChanged(position)コール時に呼ばれる
    // =====================================================
    @Override
    public void onBindViewHolder(@NonNull ConnectViewHolder viewHolder, int position) {
        Log.d(TAG, "onBindViewHolder() : position = " + position);
        // 対象データ
        ConnectData connectData = mDataList.get(position);
        
        // 表示内容更新
        viewHolder.nameView.setText(    connectData.getName());
        viewHolder.addressView.setText( connectData.getAddress());
        viewHolder.data1View.setText(   String.format("%04x", connectData.getData1()));
        viewHolder.data2View.setText(   String.format("%04x", connectData.getData2()));

        // background更新
        updateBackgroundView( viewHolder, connectData.gattCallback != null);
    }

    // =====================================================
    // 登録されている項目数
    // =====================================================
    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    // =====================================================
    // notifyItemChanged()のラッパ
    // notifyItemChanged()はRecyclerViewのUIスレッドから実行する必要があるため、
    // ラッパを用意しておく
    // 参考：https://qiita.com/aluceps/items/609590c8bacf2b1b708e
    // =====================================================
    protected void execNotifyItemChanged(int position) {
        mRecyclerView.post(() -> notifyItemChanged(position));
    } 

    // ViewHolderのデータ更新
    private void updateData1View(ConnectViewHolder viewHolder, int data) {
        viewHolder.data1View.setText(String.format("%04x", data));
    }
    private void updateData2View(ConnectViewHolder viewHolder, int data) {
        viewHolder.data2View.setText(String.format("%04x", data));
    }
    private void updateBackgroundView(ConnectViewHolder viewHolder, Boolean active) {
        if (active) {
            // 接続
            viewHolder.background.setBackground(viewHolder.background_active);
        } else {
            // 未接続
            viewHolder.background.setBackground(viewHolder.background_inactive);
        }
    }


    // =====================================================
    // アイテムのボタンがクリックされたときの処理
    // 必要なら上位側でオーバーライドする
    // =====================================================
    @SuppressLint("MissingPermission")
    protected void onItemButtonClicked(ConnectViewHolder viewHolder) {
        // viewHolderからpositionとconnectDataを取得
        int position = viewHolder.getAdapterPosition();
        ConnectData connectData = mDataList.get(position);

        Log.d(TAG, "onItemButtonClicked() : Position = " + position + "Name = " + connectData.getName());

        if (connectData.gattCallback == null) {
            // 未接続
            // Gatt callbackの生成
            connectData.gattCallback = new GattCallbackRandomSensor3() {
                // 接続時処理
                @Override
                protected void onConnected(){
                    Log.d(TAG, "connected");
                    // ちらつき防止のため、updateBackgroundView()で変更
                    // // データ(background)変更を通知
                    // execNotifyItemChanged(position);
                    updateBackgroundView(viewHolder, true);
                }
                
                // 切断時処理
                @Override
                protected void onDisconnected(){
                    Log.d(TAG, "disconnected");
                    // ちらつき防止のため、updateBackgroundView()で変更
                    // // データ(background)変更を通知
                    // execNotifyItemChanged(position);
                    updateBackgroundView(viewHolder, false);
                }
                
                // データ受信時処理
                @Override
                protected void onDataReceived(Integer data1, Integer data2, byte[] data3){
                    Log.d(TAG, "onDataReceived()");
                    // data1が有効→データ更新
                    if (null != data1) {
                        // データ更新
                        connectData.setData1(data1);
                        // ViewHolderの更新
                        updateData1View(viewHolder, data1);
                    }
                    // data2が有効→データ更新
                    if (null != data2) {
                        // データ更新
                        connectData.setData2(data2);
                        // ViewHolderの更新
                        updateData2View(viewHolder, data2);
                    }
                    // 今回Data3は使わない
                    
                    // ちらつき防止のため、直接書き換え(上のupdateDataXView())
                    // // データ変更を通知
                    // execNotifyItemChanged(position);
                }
            };
            // クリック時に背景色を変更しておく(接続待ち中未接続状態に見えるので)
            updateBackgroundView(viewHolder, true);
            // 接続
            connectData.scanData.getScanResult().getDevice().connectGatt(mContext, false, connectData.gattCallback);
        } else {
            // 接続中
            // 切断する
            connectData.gattCallback.Disconnect();
            
            // Gatt callbackの破棄
            connectData.gattCallback = null;

            // クリック時に背景色を変更しておく
            updateBackgroundView(viewHolder, false);
        }
    }
    
    // =====================================================
    // 接続中のデバイスをすべて切断する
    // =====================================================
    public void disconnectAll() {
        for(ConnectData connectData : mDataList){
            if (null != connectData.gattCallback) {
                // 接続中 → 切断
                connectData.gattCallback.Disconnect();
            }
        }
    }

    // ==== データの追加 =======================
    public int  addData(ScanData scandata) {
        ConnectData data = new ConnectData(scandata);
        mDataList.add(data);
        // 追加した位置
        int position = mDataList.lastIndexOf(data);
        // 追加を通知
        notifyItemInserted(position);

        return position;
    }

    // ==== データの交換 =======================
    public void  swapData(int fromPos, int toPos) {
        if (fromPos >= 0 && fromPos < mDataList.size() 
         && toPos   >= 0 && toPos   < mDataList.size()  ) {   // fromPos/toPosが有効範囲内か確認
            Collections.swap(mDataList, fromPos, toPos);
            notifyItemMoved(fromPos, toPos);
        }
    }

    // ==== データの削除(位置指定) =======================
    public void removeData(int pos) {
        // posのデータを削除
        if (pos >= 0 && pos < mDataList.size()) {   // posが有効範囲内か確認
            // 接続中なら切断する
            ConnectData connectData = mDataList.get(pos);
            if (null != connectData.gattCallback) {
                // 接続中 → 切断
                connectData.gattCallback.Disconnect();
            }
            // 削除
            mDataList.remove(pos);
            notifyItemRemoved(pos);
        }
    }

    // ==== 全データの削除 =======================
    public void removeAll() {
        if (!mDataList.isEmpty()) {     // もともと空でないか確認
            // 削除前のアイテム数
            int sz = mDataList.size();

            // 接続中のものは切断する
            disconnectAll();

            // 全削除
            mDataList.clear();

            // 全体更新 は最後の手段だとワーニングが出るのでやめておく
            // notifyDataSetChanged();
            // 代わりに範囲削除通知する
            notifyItemRangeRemoved(0, sz);
            
        }
    }
}
