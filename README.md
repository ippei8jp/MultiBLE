# MultiBLE
Androidで複数のBLEデバイスと通信  

## 概要
AndroidでBLE通信プログラムを作ってみた。  
BLEのスキャン～bind～Notify受信まで一通り行っている。  
(writeはやってないけど)  
MTUサイズの変更がちゃんと動いているか見るために、意味もなく大きなデータも受信している。  
(画面に入らないので表示はしてないけど)  

あと、Navigationによる画面遷移とその間のデータ受け渡しとか、
RecyclerViewによるリスト表示、その並べ替え、削除なんかも入れてみた。  

ちゃんとしたお作法に則っているかはわからん。  


ソースはMultiBLEディレクトリ  
通信相手も必要なので、通信相手が必要だが、  
RaspberryPi Picoで作ったプログラムがmicropythonディレクトリにある  
RaspberryPi3/4/5 で作ったプログラムがpython_pyblenoディレクトリにある  


## 開発環境
```
Android Studio Koala | 2024.1.1 Patch 1
Build #AI-241.18034.62.2411.12071903, built on July 11, 2024
Runtime version: 17.0.11+0--11852314 amd64
VM: OpenJDK 64-Bit Server VM by JetBrains s.r.o.
Non-Bundled Plugins:
  com.intellij.ja (241.271)
```

素人なので、どの情報が必要なのかよくわかってない...  


## 解説

できるほどの知識はない。  
とりあえず、せっかく作ったソースをなくさないようにgithubに上げておく。  


