from pybleno import *

bleno = Bleno()

# 接続中フラグ
connected = False

# ================================================
# サービス名
# ================================================
SERVICE_NAME = "random-sensor3-pybleno"

# ================================================
# UUID
# ================================================
# サービスUUID
UUID_MY_SERVICE             = 'c9d40fb0-37b0-4c42-94bc-770c3d553a17'
# DATA1 キャラクタリスティック UUID
UUID_DATA1_CHARACTERISTIC   = 'c9d40fb1-37b0-4c42-94bc-770c3d553a17'
# DATA2 キャラクタリスティック UUID
UUID_DATA2_CHARACTERISTIC   = 'c9d40fb2-37b0-4c42-94bc-770c3d553a17'
# DATA3 キャラクタリスティック UUID
UUID_DATA3_CHARACTERISTIC   = 'c9d40fb3-37b0-4c42-94bc-770c3d553a17'

# ================================================
# Characteristic のクラス定義
# ================================================
class Data1Characteristic(Characteristic):
    def __init__(self):
        Characteristic.__init__(self, {
            'uuid': UUID_DATA1_CHARACTERISTIC,
            'properties': ['read', 'notify'],
            'value': None
        })

        self._value = 0
        self._updateValueCallback = None

    def onSubscribe(self, maxValueSize, updateValueCallback):
        print('Data1Characteristic - onSubscribe')
        self._updateValueCallback = updateValueCallback

    def onUnsubscribe(self):
        print('Data1Characteristic - onUnsubscribe')
        self._updateValueCallback = None

    def onReadRequest(self, offset, callback):
        print('Data1Characteristic - onReadRequest')
        callback(result=Characteristic.RESULT_SUCCESS, data=self._value)


class Data2Characteristic(Characteristic):
    def __init__(self):
        Characteristic.__init__(self, {
            'uuid': UUID_DATA2_CHARACTERISTIC,
            'properties': ['read', 'notify'],
            'value': None
        })

        self._value = 0
        self._updateValueCallback = None

    def onSubscribe(self, maxValueSize, updateValueCallback):
        print('Data2Characteristic - onSubscribe')
        self._updateValueCallback = updateValueCallback

    def onUnsubscribe(self):
        print('Data2Characteristic - onUnsubscribe')
        self._updateValueCallback = None

    def onReadRequest(self, offset, callback):
        print('Data2Characteristic - onReadRequest')
        callback(result=Characteristic.RESULT_SUCCESS, data=self._value)


class Data3Characteristic(Characteristic):
    def __init__(self):
        Characteristic.__init__(self, {
            'uuid': UUID_DATA3_CHARACTERISTIC,
            'properties': ['read', 'notify'],
            'value': None
        })

        self._value = 0
        self._updateValueCallback = None

    def onSubscribe(self, maxValueSize, updateValueCallback):
        print('Data3Characteristic - onSubscribe')
        self._updateValueCallback = updateValueCallback

    def onUnsubscribe(self):
        print('Data3Characteristic - onUnsubscribe')
        self._updateValueCallback = None

    def onReadRequest(self, offset, callback):
        print('Data3Characteristic - onReadRequest')
        callback(result=Characteristic.RESULT_SUCCESS, data=self._value)

# ================================================
# ステータス変更時処理
# ================================================
def onStateChange(state):
    print('on -> stateChange: ' + state)

    if (state == 'poweredOn'):
        bleno.startAdvertising(name=SERVICE_NAME, service_uuids=[UUID_MY_SERVICE])
    else:
        bleno.stopAdvertising()

# ================================================
# Advertising開始処理
# ================================================
def onAdvertisingStart(error):
    print('on -> advertisingStart: ' + ('error ' + error if error else 'success'))

    if not error:
        bleno.setServices([
            BlenoPrimaryService({
                'uuid': UUID_MY_SERVICE,
                'characteristics': [
                    Data1_Characteristic,
                    Data2_Characteristic,
                    Data3_Characteristic,
                ]
            })
        ])

# 接続時
def onAccept(handle, *address):
    global connected
    connected = True
    print(f'accept {address}')

#  切断時
def onDisconnect(handle, *address):
    global connected
    connected = False
    print(f'disconnect {address}')




# ================================================
# Characteristic のインスタンス化
# ================================================
Data1_Characteristic = Data1Characteristic()
Data2_Characteristic = Data2Characteristic()
Data3_Characteristic = Data3Characteristic()

# ================================================
# イベントハンドラ登録
# ================================================
bleno.on('stateChange', onStateChange)
bleno.on('advertisingStart', onAdvertisingStart)
bleno.on('disconnect', onDisconnect)
bleno.on('accept', onAccept)

# ================================================
# BLEスタート
# ================================================
bleno.start()


import time
import random
import struct

# ================================================
# uint16データをバイナリデータ(bytes)に変換
# ================================================
def _encode_data(data):
    return struct.pack("<H", int(data))

def DataGenerator():
    global connected
    while True:
        if connected :
            # 接続中だけ動作する
            data1 = random.randint(0, 0xffff)       # 0x0000～0xffffの乱数
            data2 = random.randint(0, 0xffff)       # 0x0000～0xffffの乱数
            data3 = [random.randint(0, 0xff) for i in range(200)] 
            data3_str = " ".join([f"{d:02x}" for d in data3])
            print(f'DATA : {data1:04x} {data2:04x} {data3_str}')
            
            # 書き込み
            value = _encode_data(data1)
            Data1_Characteristic._value = value
            if Data1_Characteristic._updateValueCallback:
                Data1_Characteristic._updateValueCallback(data=value)

            value = _encode_data(data2)
            Data2_Characteristic._value = value
            if Data2_Characteristic._updateValueCallback:
                Data2_Characteristic._updateValueCallback(data=value)

            value = data3
            Data3_Characteristic._value = value
            if Data3_Characteristic._updateValueCallback:
                Data3_Characteristic._updateValueCallback(data=value)
        
        # 取得間隔待ち
        time.sleep(1)

# データ生成処理起動
DataGenerator()
