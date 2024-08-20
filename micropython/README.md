# テスト用通信相手

## 実行環境
- RaspberryPi Pico
- micropython

## 準備
RaspberryPi Picoにmicropythoをインストールしておく。  
そんなに難しいことはしてないので、最新バージョンで大丈夫と思う  

## 通らばリーチ~~根拠なし~~
~~ESP32でも大丈夫かも。~~  
ESP32でも大丈夫っぽい。  
ESP32のmicropythonにはaiobleが入ってないので、mip等でインストールする必要がある。  
以下のページで説明されている mpremote を使うと割と簡単にインストールできる。  
[MicroPython のリモート制御: mpremote](https://micropython-docs-ja.readthedocs.io/ja/latest/reference/mpremote.html#mpremote-command-mip)  
