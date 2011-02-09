This translation is the work of [insert your name], copyright© [insert year].

==============================================================================
  OmegaT 2.0 ー お読みください。

  1.  OmegaTについて
  2.  OmegaTとは？  3.  OmegaTをインストールする
  4.  OmegaTへの貢献
  5.  OmegaTにお困りですか？ヘルプが必要な方はこちら  6.  リリースに関する詳細

==============================================================================
  1.  OmegaTについて


Omega Tに関する最新の情報は以下のページでご覧になれます：
      http://www.omegat.org/

ユーザーサポートはYahoo userグループへ（多言語対応）。その過去のやり取りは登録せずに検索できるようになっています。
     http://groups.yahoo.com/group/OmegaT/

拡張機能要望については（英語のみの）SourceForgeへ：
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

バグレポートは（英語のみの）SourceForgeへ：
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  OmegaTとは？

Omega Tとは、翻訳支援ツールです。無料で使用することができます。それは、利用する際に利用料を支払う必要が無いと言うことを意味しています。商用利用であっても費用はかかりません。また、自由に使うこともできます。それは、ユーザーライセンスを遵守していただければ、修正を加えたり再配布したりすることもできるという意味です。

OmegaTの主な特徴は以下の通りです。
  -Javaを対応しているオペレーティングシステムであれば動作可能
  - 翻訳参考ファイルとしてどんな種類のTMXファイルをも利用可能
  -SRXのような方法を用いた柔軟な文章の分割
  - 翻訳参考ファイルやプロジェクト全体を検索
  - その他のフォルダにある対応のできる形式のファイル内容を検索 
  - 参考訳文照合
  - 複雑なフォルダ構造になったプロジェクトの取り扱い
  - 用語集を対応（使用語確認） 
  - オープンソースのオンザフライのスペルチェッカーへの対応
  - StarDict 辞書への対応
  - Google Translate 機械翻訳サービスへの対応
  - 入門ガイド（お手軽スタートガイド）や取扱説明ガイドが分かりやすい
  - 多数の言語へのローカライズ

OmegaT は以下のファイル形式に対応しています：
  - プレーンテキスト
  - HTMLまたはXHTML
  - HTMLヘルプコンパイラ
  - OpenDocument や OpenOffice.org
  - Java リソースバンドル（.properties）
  - INI（キー値 形式、多数のエンコード対応）
  - PO ファイル
  - DocBook 文書ファイル形式
  - Microsoft OpenXML
  - Okapiツール作の単一言語XLIFF
  - CopyFlow Gold（for QuarkXPress）形式
  - 字幕ファイル（SRT）
  - ResX 形式
  - Android リソースファイル
  - LaTeX 形式
  - Typo3 LocManagerファイル
  - ヘルプ＆マニュアル
  - Windows RC リソース
  - Mozilla DTDファイル
  - DokuWiki ファイル

その他の形式をコード変更によって対応可能です。

OmegaTはとても複雑な階層構造になった原文ディレクトリであっても、その中の扱えるファイルを全て自動的に解析します。 また、全く同じ構造の訳文ディレクトリを作成し、その中には扱っていないファイルをそのままのコピーが含まれています。

すぐにOmegaTを使えるようになりたければ、OmegaTを立ち上げ、「お手軽スタートガイド」をお読みください。

取扱説明ガイドはダウンロードしたパッケージに含まれており、OmegaTを立ち上げ、［ヘルプ］メニューから読むことができます。

==============================================================================
 3. OmegaTをインストールする

3.1 全般の確認事項
OmegaT の実行には、Java Runtime Environment（JRE）バージョン1.5以降があらかじめインストールされている必要があります。JREの選択、入手やインストールのトラブルを回避するため、現在の OmegaT は JRE 付きパッケージが標準で提供されています。 

すでに Java 実行環境がある場合、現バージョンの OmegaT をインストールする最も簡単な方法は、Java Web Start を使用することです。この場合、以下のファイルをダウンロードし、実行してください：

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

最初の実行時に、お使いのコンピュータに適した実行環境とアプリケーションを自動でインストールします。それ以降の実行時は、オンラインである必要はありません。

インストール中、OS によってはいくつかのセキュリティ警告が表示される場合があります。証明書は Didier Briel 氏による自己署名がなされています。この（Java Web Start版）実行ファイルに与える権限（"コンピュータへの無制限アクセス"と表示される可能性があります）は、ローカル版（後述の手順でインストールする従来のもの）に対して適用するものと同一で、コンピュータのハードディスクにアクセスすることを許可するものです。インストール後は、OmegaT.jnlp をクリックすると、もしオンラインであればプログラムの更新がないか検索し、もしあればインストールした後、OmegaT を起動します。 

代替のダウンロードとインストール方法は（従来通り）下記に示した通りです。 

Windows、Linux ユーザ：適した JRE がすでにインストールされているとわかっている場合、JRE なしバージョン（バージョン番号に "Without_JRE" がついています）をインストールしてください。JREにかんして不明な場合は standard（JRE付き）バージョンをお勧めします。すでに JRE がそのシステムにインストールされていたとしても、競合はおこらないため、安全です。

Linux ユーザ：多くのディストリビューション（Ubuntu など）に実装されているフリー/オープンソースの Java 環境では、（古すぎたり、機能不足といった理由で）OmegaT が起動しない可能性があります。この場合は、上記リンク先より Sun提供の Java Runtime Environment（JRE）をインストールするか、JREつきの OmegaT を利用してください（"Linux"と記されている、tar.gzファイルです）。

Mac OSXユーザーは既にJavaがインストールされています。

PowerPCで Linux をお使いの方は、SunがPPC用JREを提供していないので、IBM社が提供する JREを予めインストールしてくださいダウンロードサイトはこちらです：

    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 OmegaT のインストール
* Windows ユーザ：インストールプログラムを実行してください。OmegaT 起動のためのショートカットの作成も行えます。* その他のユーザ：まず適したフォルダを作成します。
（Linux の場合例えば /usr/local/lib）OmegaT の zip または tar.gz ファイルをそこへコピーし、展開します。

3.3 OmegaT の起動
いくつかの方法があります。

* Windows users: by double-clicking on the file OmegaT.exe. エクスプローラ上で "OmegaT.exe" でなく "OmegaT" のみが表示されている場合、拡張子が表示されるようにエクスプローラの設定を変更してください。

* OmegaT.jar のアイコンをダブルクリックします。これは、拡張子 .jar ファイルがご使用の Java と関連づけられている場合のみ有効です。

* 命令入力行から。OmegaTを起動するコマンドは：

cd <OmegaT.jarが置かれているディレクトリ。>

<実行可能なJavaファイル名とパス> -jar OmegaT.jar

（実行可能なJavaファイルはLinuxの場合は java で、Windowsの場合は java.exe です。）システムレベルで Java がインストールされている場合（つまり、環境変数に Java 実行ファイルへのパスが含まれている場合など）フルパスを指定する必要はありません。

* Windows ユーザ：スタートメニュー、デスクトップやクイック起動バーへのショートカット作成が可能です。OmegaT.exe へのショートカットを自分で作成し、上記のような場所へ配置してもかまいません。

* Linux KDE ユーザ：OmegaT をメニューに追加するには、まず以下を開きます：

コントロールセンター → デスクトップ → パネル → メニュー → K メニューの編集 → ファイル → 新規アイテム/サブメニュー

次に適したメニューを選択し、ファイル → 新規サブメニューとファイル → 新規アイテム を選択します。 ［新規アイテム名］として OmegaT と入力します。

"コマンド" 領域には、ナビゲーションボタンを使用して OmegaT 起動スクリプトがある場所を指定します。 

アイコンボタン（Name/Description/Comment フィールドの右にあります）をクリックし、その他のアイコン → 閲覧 と進み、OmegaT フォルダにある /images サブフォルダを指定します。その中にある OmegaT.png を選択します。

最後に ファイル → 保存 メニューから保存します。

* Linux GNOME ユーザ：以下の手順で OmegaT をパネルに追加できます：

パネルを右クリックし、新規ランチャーを追加 を選択"名前" フィールドに "OmegaT" と入力します。"コマンド" フィールドには、ナビゲーションボタンを使用して OmegaT 起動スクリプトを指定します。［OK］ボタンをクリックします。

==============================================================================
 4. OmegaT プロジェクトへの貢献

OmegaTの開発に協力したい場合、開発者たちと連絡を取ってください：
    http://lists.sourceforge.net/lists/listinfo/omegat-development

OmegaT ユーザーインターフェイスや取扱説明ガイド、または他の関連文書の翻訳に協力したい場合、下記のファイルを読んでください：
      
      http://www.omegat.org/en/translation-info.html

また、翻訳者リストに参加してください：
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

その他の貢献の方法に関して、ユーザーグループに参加してください：
      http://tech.groups.yahoo.com/group/omegat/

そして、OmegaTの世界でどんなことが進行しているか、感じ取ってください…

  OmegaTは元々Keith Godfreyの作品です。  Marc PriorはOmegaTプロジェクトのコーディネーターです。

これまでに貢献してくれた方々： （アルファベット順）

コードに関する貢献者
  Zoltan Bartko
  Volker Berlin
  Didier Briel（開発担当）
  Kim Bruning
  Alex Buloichik（開発リーダー）
  Sandra Jean Chua
  Martin Fleurke  
  Wildrich Fourie
  Thomas Huriaux
  Ibai Lakunza Velasco
  Fabián Mandelbaum
  Maxym Mykhalchuk 
  Arno Peters
  Henry Pijffers 
  Tiago Saboga
  Andrzej Sawuła
  Benjamin Siband
  Antonio Vilei
  Martin Wunderlich

上記以外の貢献者
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary （多言語化担当）
  Vito Smolej（文書作成担当）
  Samuel Murray
  Marc Prior 
  そしてとても大変多くの非常に貢献してくださった方々。

（万が一、OmegaT プロジェクトへ大きく協力したのにここに名前がない、という場合は、遠慮なくご連絡ください）

OmegaTは、下記のライブラリを使用します。

  Somik Raha氏、Derrick Oswald氏などによるHTMLParser（LGPLライセンス）。
  http://sourceforge.net/projects/htmlparser

  Steve Roy氏による MRJ Adapter 1.0.8（LGPLライセンス）
  http://homepage.mac.com/sroy/mrjadapter/

  VLSolutions社による VLDocking Framework 2.1.4（CeCILLライセンス）
  http://www.vlsolutions.com/en/products/docking/

  László Németh氏などによる Hunspell（LGPLライセンス）

  Todd Fast氏、Timothy Wall氏などによる JNA （LGPLライセンス）

  Swing-Layout 1.0.2（LGPLライセンス）

  Jmyspell 2.1.4（LGPLライセンス）

  JAXB 2.1.7（GPLv2ライセンスと Classpath 例外）

==============================================================================
 5.  OmegaTにお困りですか？ヘルプが必要な方はこちら

バグを報告するのは取扱説明ガイドをじっくり確認してからのことです。たった今発見した状態は、バグではなくOmegaTの特徴である可能性があります。OmegaTのログをチェックし「エラー（Error）」、「警告（Warning）」、「例外（Exception）」または「強制終了（died unexpectedly）」といった言葉があった場合、何か起こっています。（log.txtはユーザー設定ディレクトリに置かれています。その場所についてはマニュアルお読みください。）

次に行うことは、他のユーザーの方々とあなたが発見した状態を確認しあい、その状態がそれまでに報告されていないバグであることを確認してください。またSourceForgeでもバグリポートのページで確認することができます。あなた自身が最初の発見者であり、且つ予想外の事象によって引き起こされる再現可能な問題である場合の時のみ、バグレポートを申請してください。

良いバグレポートには次の3点が必ず含まれています。  - 再現するまでの手順
  - その動作によって予想していた結果
  - 実際に表示された結果

ファイルのコピーやログ、それに画面のスクリーンショットなどの開発者側がバグを見つけることができ、修正をするのに役立つ資料を一緒につけて提出することができます。

ユーザーグループのアーカイブを閲覧するには、下記のリンクへお進みください。
     http://groups.yahoo.com/group/OmegaT/

バグリポートを閲覧したり、新しいバグリポートを提出する場合には、下記のリンクへお進みください。
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

自分が報告したバグリポートがどうなるか見守りたい場合には、Source Forgeユーザーとして登録してください。

==============================================================================
6.   リリースに関する詳細

このバージョンやこれまでにリリースされたバージョンの変更に関する詳細情報は、「changes.txt」ファイルをご覧ください。


==============================================================================
