import sys

from micropython import const

import asyncio
import aioble
import bluetooth

import random
import struct

# ================================================
# UUID
# ================================================
# サービスUUID
UUID_MY_SERVICE             = bluetooth.UUID('c9d40fb0-37b0-4c42-94bc-770c3d553a17')
# DATA1 キャラクタリスティック UUID
UUID_DATA1_CHARACTERISTIC   = bluetooth.UUID('c9d40fb1-37b0-4c42-94bc-770c3d553a17')
# DATA2 キャラクタリスティック UUID
UUID_DATA2_CHARACTERISTIC   = bluetooth.UUID('c9d40fb2-37b0-4c42-94bc-770c3d553a17')
# DATA3 キャラクタリスティック UUID
UUID_DATA3_CHARACTERISTIC   = bluetooth.UUID('c9d40fb3-37b0-4c42-94bc-770c3d553a17')

# advertising パケットの送信間隔(usec)
_ADV_INTERVAL_US = 250_000

# ================================================
# グローバル変数
# ================================================
# データ取得間隔(msec)
data_interval = const(1000)

# 接続中フラグ
connected = False


# ================================================
# サービス/キャラクタリスティックの登録
# ================================================
my_service           = aioble.Service(UUID_MY_SERVICE)
data1_characteristic = aioble.Characteristic(my_service, UUID_DATA1_CHARACTERISTIC,  read=True,  notify=True)
data2_characteristic = aioble.Characteristic(my_service, UUID_DATA2_CHARACTERISTIC,  read=True,  notify=True)
data3_characteristic = aioble.Characteristic(my_service, UUID_DATA3_CHARACTERISTIC,  read=True,  notify=True)

aioble.register_services(my_service)


# ================================================
# uint16データをバイナリデータ(bytes)に変換
# ================================================
def _encode_data(data):
    return struct.pack("<H", int(data))


# ================================================
# バイナリデータ(bytes)をuint16データに変換
# ================================================
def _decode_data(data):
    if data is not None:
        try:
            return struct.unpack("<H", data)[0]
        except Exception as e:
            print("Error decoding data:", e)
            return None
    return None


# ================================================
# 接続待ち/切断待ちタスク
# ================================================
async def advertising_task():
    global connected
    global data1_interval
    
    while True:
        # 接続待ち(advertisingパケット送出)
        async with await aioble.advertise(
            _ADV_INTERVAL_US,
            name="random-sensor",
            services=[UUID_MY_SERVICE],
            # appearance=__ADV_APPEARANCE_GENERIC_MY_SERVICE,
        ) as connection:
            connected = True
            
            print("Connection from", connection.device)
            
            # 切断待ち
            await connection.disconnected(timeout_ms=None)
            connected = False
            print("** Disconnected **")

# ================================================
# データ設定タスク
# 乱数値を書き込む(Notifyあり)
# ================================================
async def data_set_task():
    while True:
        if connected :
            # 接続中だけ動作する
            data1 = random.randint(0, 0xffff)       # 0x0000～0xffffの乱数
            data2 = random.randint(0, 0xffff)       # 0x0000～0xffffの乱数
            data3 = [random.randint(0, 0xff) for i in range(200)] 
            data3_str = " ".join([f"{d:02x}" for d in data3])
            print(f'DATA : {data1:04x} {data2:04x} {data3_str}')
            
            # 書き込み
            data1_characteristic.write(_encode_data(data1), send_update=True)
            data2_characteristic.write(_encode_data(data2), send_update=True)
            data3_characteristic.write(bytes(data3), send_update=True)
        
        # 取得間隔待ち
        await asyncio.sleep_ms(data_interval)

# ================================================
# メインタスク
# ================================================
async def main():
    print('Program Start!')
    # タスクの生成と起動
    tasks = (
        asyncio.create_task(advertising_task()),
        asyncio.create_task(data_set_task()), 
    )
    await asyncio.gather(*tasks)

asyncio.run(main())
