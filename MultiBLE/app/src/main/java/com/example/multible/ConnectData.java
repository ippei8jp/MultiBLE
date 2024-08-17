package com.example.multible;

// ==== データ保持用クラス =======================
public class ConnectData {
    // =====================================================
    // データ
    // =====================================================
    public final ScanData   scanData;
    private int             Data1 = 0;
    private int             Data2 = 0;

    // =====================================================
    // BLE接続時のGatt callback
    // =====================================================
    public GattCallbackRandomSensor3    gattCallback;

    // =====================================================
    // コンストラクタ
    // =====================================================
    public ConnectData(ScanData data) {
        scanData = data;
        gattCallback = null;
    }

    // =====================================================
    // getter
    // =====================================================
    public String getAddress()  { return scanData.getAddress();   }
    public String getName()     { return scanData.getName();      }
    public int getData1()       { return Data1;                     }
    public int getData2()       { return Data2;                     }

    // =====================================================
    // setter
    // =====================================================
    public void setData1(int data1)                 { Data1 = data1;    }
    public void setData2(int data2)                 { Data2 = data2;    }

}
