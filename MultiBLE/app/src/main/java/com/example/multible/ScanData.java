package com.example.multible;

import android.annotation.SuppressLint;
import android.bluetooth.le.ScanResult;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Objects;

// 参考：https://qiita.com/skonb/items/8e9346cb6f2159c1489e


// ==== データ保持用クラス =======================
public class ScanData implements Parcelable {
    // =====================================================
    // データ
    // =====================================================
    private Boolean     selected = false;
    public ScanResult   scanResult;

    // =====================================================
    // コンストラクタ
    // =====================================================
    public ScanData(ScanResult result) {
        super();
        scanResult = result;
    }

    // =====================================================
    // getter
    // =====================================================
    public ScanResult   getScanResult() { return scanResult;    }
    public Boolean      isSelected()    { return selected;      }
    @SuppressLint("MissingPermission")
    public String       getName()       { return scanResult.getDevice().getName();      }
    public String       getAddress()    { return scanResult.getDevice().getAddress();   }
    public String       getUuid()       {
        List<ParcelUuid> parcelUuid = Objects.requireNonNull(scanResult.getScanRecord()).getServiceUuids();
        if (parcelUuid != null) {
            return parcelUuid.get(0).toString();
        } else {
            return "--";
        }
    }

    // =====================================================
    // setter
    // =====================================================
    public void setResult(ScanResult result)  { scanResult = result;          }
    public void setSelect()                 { selected = true;      }
    public void clearSelect()               { selected = false;     }

    // =====================================================
    // 反転
    // =====================================================
    public Boolean toggleSelect() {
        selected = !selected;
        return selected;
    }


    // =====================================================
    // parcelable
    // =====================================================
    public static final Creator<ScanData> CREATOR = new Creator<ScanData>() {
        @Override
        public ScanData createFromParcel(Parcel in) {
            return new ScanData(in);
        }

        @Override
        public ScanData[] newArray(int size) {
            return new ScanData[size];
        }
    };

    private ScanData(Parcel in) {
        byte tmpSelected = in.readByte();
        selected = tmpSelected == 0 ? null : tmpSelected == 1;
        scanResult = in.readParcelable(ScanResult.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeByte((byte) (selected == null ? 0 : selected ? 1 : 2));
        parcel.writeParcelable(scanResult, i);
    }
}
