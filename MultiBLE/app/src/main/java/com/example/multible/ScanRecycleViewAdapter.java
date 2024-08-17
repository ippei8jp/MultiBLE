package com.example.multible;

import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ScanRecycleViewAdapter extends RecyclerView.Adapter<ScanRecycleViewAdapter.ScanViewHolder> {
    private static final String TAG =
                ScanRecycleViewAdapter.class.getSimpleName();
            
    // =====================================================
    // データ保持リスト
    // =====================================================
    private final List<ScanData> mDataList;
    
    // =====================================================
    // コンテキスト
    // =====================================================
    private final Context mContext;

    // =====================================================
    // ViewHolderクラス
    // =====================================================
    public static class ScanViewHolder extends RecyclerView.ViewHolder {
        public final TextView nameView;
        public final TextView addressView;
        public final TextView uuidView;
        public final LinearLayout background;

        public ScanViewHolder(View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.NameBody);
            addressView = itemView.findViewById(R.id.AddressBody);
            uuidView = itemView.findViewById(R.id.UuidBody);
            background = itemView.findViewById(R.id.RowLayout);
        }
    }

    // =====================================================
    // 初期値なしコンストラクタ
    // =====================================================
    public ScanRecycleViewAdapter(Context ctx) {
        this(ctx, new ArrayList<>());
    }

    // =====================================================
    // 初期値ありコンストラクタ
    // =====================================================
    public ScanRecycleViewAdapter(Context ctx, List<ScanData> mDataList) {
        this.mDataList = mDataList;
        this.mContext = ctx;
    }

    // =====================================================
    // 選択されているScanDataの配列を取得
    // =====================================================
    public ScanData[] getSelectedDeviceData() {
        return mDataList.stream()
                            .filter(ScanData::isSelected)
                            .toArray(ScanData[]::new);
    }

    // =====================================================
    // ViewHolder生成処理
    // =====================================================
    @NonNull
    @Override
    public ScanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_row_item, parent,false);
        ScanViewHolder holder = new ScanViewHolder(inflate);

        // アイテム上でクリックされたときのハンドラを登録
        inflate.setOnClickListener(view -> {
            final int position = holder.getAdapterPosition();
            Log.d(TAG, "onClick  " + position);

            // 処理(実体はViewクラスにある)
            onItemClicked(position, mDataList.get(position));
        });
        return holder;
    }

    // =====================================================
    // 内容が更新されたときの処理
    // データ追加時、notifyItemChanged(position)コール時に呼ばれる
    // =====================================================
    @Override
    public void onBindViewHolder(@NonNull ScanViewHolder holder, int position) {
        Log.d(TAG, "position = " + position);
        
        // 表示内容更新
        holder.nameView.setText(mDataList.get(position).getName());
        holder.addressView.setText(mDataList.get(position).getAddress());
        holder.uuidView.setText(mDataList.get(position).getUuid());

        // 選択/非選択で背景色を変える処理
        if (mDataList.get(position).isSelected()) {
            Log.d(TAG, "selected");
            Drawable bk = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.background_scan_row_item_selected, null);
            holder.background.setBackground(bk);
        } else {
            Log.d(TAG, "unselected");
            Drawable bk = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.background_scan_row_item, null);
            holder.background.setBackground(bk);
        }
    }

    // =====================================================
    // 登録されている項目数
    // =====================================================
    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    // =====================================================
    // アイテム上でクリックされたときの処理
    // 必要ならView側でオーバーライドする
    // =====================================================
    protected void onItemClicked(int position, @NonNull ScanData data) {
        Log.d(TAG, "onItemClicked() getName:" + data.getName());
        Boolean selected = data.toggleSelect();
        if (selected) {
            Log.i(TAG, "onItemClicked() : selected");
        } else {
            Log.i(TAG, "onItemClicked() : unselected");
        }
        notifyItemChanged(position);
    }

    
    // =====================================================
    // データの追加
    // =====================================================
    public int  addData(ScanResult result) {
        String address = result.getDevice().getAddress();
        for (int i = 0; i < mDataList.size(); i++) {
            if (address.equals(mDataList.get(i).getAddress())) {
                // 既に同じアドレスがあったら登録しない
                Log.i(TAG, "addData() : address duplicate   " + address );
                return -1;
            }
        }

        ScanData data = new ScanData(result);
        mDataList.add(data);
        // 追加した位置
        int position = mDataList.lastIndexOf(data);
        // 追加を通知
        notifyItemInserted(position);

        return position;
    }

    // =====================================================
    // データの削除(非選択データ)
    // =====================================================
    public void removeData() {
        // 選択されていないデータのインデックス一覧を取得
        // 降順にソート(降順で行わないと間違ったデータを削除してしまう可能性がある)
        List<Integer> indices = IntStream.range(0, mDataList.size())
                .filter(i -> !mDataList.get(i).isSelected())
                .boxed()
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());

        Log.d(TAG, "removeData() indices :" + indices);
        // データの削除とItemの削除
        for (int i = 0; i < indices.size(); i++) {
            int pos = indices.get(i);
            Log.d(TAG, "remove() position :" + pos);
            mDataList.remove(pos);
            notifyItemRemoved(pos);
        }
    }
    
    // =====================================================
    // データの削除(位置指定)
    // =====================================================
    public void removeData(int pos) {
        Log.d(TAG, "removeData() position :" + pos);
        // posのデータを削除
        if (pos >= 0 && pos < mDataList.size()) {   // posが有効範囲内か確認
            mDataList.remove(pos);
            notifyItemRemoved(pos);
        }
    }

    // =====================================================
    // 全データの削除
    // =====================================================
    public void removeAll() {
        if (!mDataList.isEmpty()) {     // もともと空でないか確認
            Log.d(TAG, "removeAll()");
            // 削除前のアイテム数
            int sz = mDataList.size();

            // 全削除
            mDataList.clear();

            // 全体更新 は最後の手段だとワーニングが出るのでやめておく
            // notifyDataSetChanged();
            // 代わりに範囲削除通知する
            notifyItemRangeRemoved(0, sz);
        }
    }


}
