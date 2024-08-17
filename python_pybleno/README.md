# テスト用通信相手

## 実行環境
- RaspberryPi3/4/5/ZeroW 
- python3.9.x 
- pybleno

## 準備
pyblenoのインストールは以下
```
pip install pybleno
```

## 注意
pythonは3.11では動作しないとの情報あり。  
参考：[pyblenoライブラリを使ってBLE Peripheralの作成](https://tomosoft.jp/design/?p=41722)  
最近のRaspberyPi OS に標準インストールされているバージョンは3.11.xなので
pyenvを使うなどして3.9.xをインストールして使用する。  

実行はsudoで行う必要がある(rawデータアクセスのため)。



    
