OmegaT の日本語版は以下の貢献者によりできたものです：
・1.4版全体：柳田 ひさし氏 copyright© 2004
・1.6版 お手軽スタートガイド：可知 豊氏 copyright© 2006
・1.6版 UI：林 たかゆき氏 copyright© 2006
・1.6版 readme：中山 嘉孝氏 copyright© 2006
・1.6版 ユーザーマニュアル：白方 健太郎氏 copyright© 2006
・1.6版 全体チェック 横田 邦彦氏
・1.6版 全体チェック エラリー ジャンクリストフ氏
・1.6版 全体チェック＋差分翻訳 松谷 善久氏
・1.8/2.0版 お手軽スタートガイド、UI、readme：エラリー ジャンクリストフ氏 copyright© 2009
・1.8/2.0版 全体チェック 篠原 範子氏

==============================================================================
  OmegaT 2.1 - お読みください

  1.  OmegaT について
  2.  OmegaT とは？
  3.  OmegaT をインストールする
  4.  OmegaTへの貢献
  5.  OmegaT にお困りですか？ヘルプが必要な方はこちら
  6.  リリースに関する詳細

==============================================================================
  1.  OmegaT について


OmegaT に関する最新の情報は以下のページでご覧になれます：
      http://www.omegat.org/

ユーザーサポートは Yahoo ユーザーグループで提供されています（多言語対応）。過去のアーカイブの検索は、グループに登録しなくても行えます。
     http://groups.yahoo.com/group/OmegaT/

機能に関する要望は SourceForge 内の開発サイトへ（英語のみ）：
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

バグ報告も同じく SourceForge 内の開発サイトへ（英語のみ）：
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  OmegaT とは？

OmegaT は翻訳支援ツールです。自由なソフトウェアです。たとえ商用利用であっても、利用料を支払う必要が無いということを意味しています。また、自由に使うこともできます。これは、ユーザーライセンスを遵守すれば、修正の適用や再配布も自由に行えるという意味です。

OmegaT の主な特徴は以下の通りです：
  - Java に対応したオペレーティングシステム上であれば動作可能
  - あらゆる有効な TMX ファイルを、翻訳の参照として利用可能
  - SRX（Segmentation Rules eXchange）に近い手法による、柔軟な分節化
  - プロジェクトや翻訳メモリ内の検索
  - 任意のフォルダーにある、対応した形式を持つファイルの検索 
  - 参考訳文照合
  - 複雑なフォルダー構造を含むプロジェクトでも、スマートな取り扱い
  - 用語集への対応（専門用語の確認） 
  - オープンソースのオンザフライの綴り確認ツールへの対応
  - StarDict 辞書への対応
  - Google Translate 機械翻訳サービスへの対応
  - 簡潔でわかりやすい取扱説明書やチュートリアル（お手軽スタートガイド）
  - ソフトウェア自身が多数の言語へ翻訳されている

OmegaT は以下のファイル形式に対応しています：
  - プレーンテキスト
  - HTML と XHTML
  - HTML ヘルプコンパイラ
  - OpenDocument や OpenOffice.org
  - Java リソースバンドル（.properties）
  - INI ファイル（キー=値 形式、あらゆるエンコードに対応）
  - PO ファイル
  - DocBook 文書ファイル形式
  - Microsoft Open XMLファイル
  - Okapi フレームワークで用いる単一言語の XLIFF ファイル
  - CopyFlow Gold for QuarkXPress
  - 字幕ファイル（SRT）
  - ResX
  - Android リソース
  - LaTeX
  - Typo3 LocManager
  - ヘルプ＆マニュアル
  - Windows RC リソース
  - Mozilla DTD
  - DokuWiki

他のファイル形式に対しても、 カスタマイズにより対応可能です。

OmegaT は、原文フォルダーの階層が非常に複雑な場合も、対応した形式のファイルをすべて自動的に検索します。全く同じ階層を持った訳文フォルダーを作成し、対応していない形式のファイルはそこへコピーを残しておくことができます。

OmegaT を手早く使い慣れたい場合は、まず OmegaT を起動して「お手軽スタートガイド」をお読みください。

取扱説明書は、ダウンロードしたパッケージに含まれており、OmegaT 起動後の［ヘルプ］メニューから読むことができます。

==============================================================================
 3. OmegaT をインストールする

3.1　全般の確認事項
OmegaT の実行には、Java Runtime Environment（JRE）バージョン 1.5 以降があらかじめインストールされている必要があります。JREの選択や入手、またはインストールにおけるトラブルを回避するため、現在の OmegaT は JRE 付きパッケージが標準で提供されています。 

すでに Java 実行環境がある場合、現バージョンの OmegaT をインストールする最も簡単な方法は、Java Web Start を使用することです。この場合、以下のファイルをダウンロードし、実行してください：

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

最初の実行時に、お使いのコンピュータに適した実行環境とアプリケーションを自動でインストールします。それ以降の実行時は、オンラインである必要はありません。

インストール中、OS によってはいくつかのセキュリティ警告が表示される場合があります。証明書は Didier Briel 氏による自己署名がなされています。この実行ファイルに与える権限（「コンピュータへの無制限アクセス」と表示される可能性があります）は、ローカル版（後述の手順でインストールする、従来版）に対して適用するものと同じもので、コンピュータのハードディスクにアクセスすることを許可するものです。インストール後は、OmegaT.jnlp をクリックすると、もしオンラインであればプログラムの更新がないか検索し、もしあればインストールした後、OmegaT を起動します。 

ダウンロードとインストールを行う代替の方法は、下記に示す通りです。 

Windows、Linux ユーザー：適した JRE がすでにインストールされているとわかっている場合、JRE 無しのバージョン（バージョン番号に「Without_JRE」がついています）をインストールしてください。JRE がインストールされているかが不明な場合は、「standard」つまり JRE が付属したバージョンをお勧めします。すでに JRE がその OS にインストールされていたとしても、バージョンの競合は起こらないため、安全です。

Linux ユーザー：多くのディストリビューション（Ubuntu など）に実装されているフリーまたはオープンソースの Java 環境では、（古かったり、機能が不足しているなどの理由で）OmegaT が起動しない可能性があります。この場合は、Sun 社が提供する Java Runtime Environment（JRE）を上記のリンク先より入手してインストールするか、JREが付属した OmegaT パッケージを利用してください（「Linux」と記されている tar.gz ファイルです。）

Mac ユーザー：JRE は Mac OS X 上にすでにインストールされています。

PowerPC 系システムでの Linux ユーザー：Sun 社は PPC 用 JRE を提供していないため、IBM 社が提供する JRE をあらかじめインストールしてください。ダウンロード先は：

    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 


3.2　OmegaT のインストール
* Windows ユーザー：インストールプログラムを実行してください。OmegaT を起動するためのショートカットも作成できます。* その他のユーザー：まず適したフォルダーを作成します。（Linux の場合、例えば /usr/local/lib）OmegaT の zip または tar.gz ファイルをそこへコピーし、展開します。

3.3　OmegaT の起動
いくつかの方法で起動できます。

* Windows ユーザー：OmegaT.exe のアイコンをダブルクリックします。エクスプローラー上で「OmegaT.exe」でなく「OmegaT」と表示されている場合、拡張子が表示されるよう、エクスプローラーの設定を変更してください。

* OmegaT.jar のアイコンをダブルクリックします。この方法は、拡張子 .jar を持つファイルがご使用の Java と関連づけられている場合のみ有効です。

* コマンドラインからの実行も可能です。OmegaT 起動のためのコマンドは：

cd <OmegaT.jar が存在するフォルダー>

<Java 実行ファイルへのパス> -jar OmegaT.jar

（Java 実行ファイルは、Linux の場合は java、Windows の場合は java.exe です。システムレベルで Java がインストールされている場合、フルパスを指定する必要はありません。

* Windows ユーザー：インストールプログラムを実行すると、スタートメニューやデスクトップ、クイック起動バーへのショートカットを作成できます。OmegaT.exe へのショートカットを自分で作成し、上記のような場所へ配置することもできます。

* Linux KDE ユーザー：OmegaT をメニューに追加するには、まず以下を開きます：

［コントロールセンター］→［デスクトップ］→［パネル］→［メニュー］→［K メニューの編集］→［ファイル］→［新規アイテム/サブメニュー］

追加したいメニューを選択し、［ファイル］→［新規サブメニューとファイル］→［新規アイテム］を選択します。［新規アイテム名］として OmegaT と入力します。

［コマンド］欄には、ナビゲーションボタンを使用して OmegaT 起動スクリプトを指定します。 

［アイコン］ボタン（［Name/Description/Comment］欄の右にあります）をクリックし、［その他のアイコン］→［閲覧］を押します。OmegaT アプリケーションフォルダーにある images フォルダを選択し、その中にある OmegaT.png を選択します。

最後に［ファイル］→［保存］で変更を保存します。

Linux GNOME ユーザー：以下の手順で OmegaT をパネル（画面上部のバー）へ追加できます：

パネルを右クリックし、［新規ランチャーを追加］を選択します。［名前］欄に OmegaT と入力します。［コマンド］欄には、ナビゲーションボタンを使用して OmegaT 起動スクリプトを指定します。最後に［OK］をクリックします。

==============================================================================
 4. OmegaT プロジェクトへの貢献

OmegaT の開発に協力したい場合、以下のサイトから開発者たちと連絡を取ってください：
    http://lists.sourceforge.net/lists/listinfo/omegat-development

OmegaT のユーザーインターフェイスや取扱説明書、他の関連文書の翻訳に協力したい場合、まず下記のファイルをお読みください：
      
      http://www.omegat.org/en/translation-info.html

そして翻訳者のメーリングリストに参加してください：
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

他の何らかの方法で協力したい場合、まずユーザーグループに参加してください：
      http://tech.groups.yahoo.com/group/omegat/

そして、OmegaT の世界がどんな雰囲気であるのかをつかんでください。

  
OmegaT のオリジナルは Keith Godfrey によるものです。  Marc Prior が OmegaT プロジェクトのコーディネーターです。

これまでに貢献してくれた方々：（アルファベット順）

コードに関する貢献者
  Zoltan Bartko
  Volker Berlin
  Didier Briel（リリース担当）
  Kim Bruning
  Alex Buloichik
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

その他の貢献者
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary （多言語化担当）
  Vito Smolej（文書作成担当）
  Samuel Murray
  Marc Prior 
  そして、大いに貢献してくださった多くの方々

（万が一、OmegaT プロジェクトへ大きく協力したのに、ここに名前がないという場合は、遠慮なくご連絡ください）

OmegaT は以下に示すライブラリを使用しています。

  Somik Raha、Derrick OswaldなどによるHTMLParser（LGPLライセンス）
  http://sourceforge.net/projects/htmlparser

  Steve RoyによるMRJ Adapter 1.0.8（LGPLライセンス）
  http://homepage.mac.com/sroy/mrjadapter/

  VLSolutions社によるVLDocking Framework 2.1.4（CeCILLライセンス）
  http://www.vlsolutions.com/en/products/docking/

  László NémethなどによるHunspell（LGPLライセンス）

  Todd Fast、Timothy WallなどによるJNA （LGPLライセンス）

  Swing-Layout 1.0.2（LGPLライセンス）

  Jmyspell 2.1.4（LGPLライセンス）

  JAXB 2.1.7（GPLv2ライセンスとClasspath例外）

==============================================================================
 5.  OmegaT にお困りですか？ヘルプが必要な方はこちら

バグ報告の前に、まず取扱説明書などを充分確認してください。発見したその現象は、バグではなく OmegaT の特徴である可能性もあります。OmegaT のログに「Error」「Warning」「Exception」「died unexpectedly」といった単語が残っていた場合、おそらく何らかの問題が生じていることを示します。（log.txt はユーザー設定フォルダに生成されます。ログファイルの場所についてはマニュアルをお読みください。）

次に、その現象がすでに他のユーザーから報告されていないかどうか、確認してください。SourceForge の OmegaT 開発サイトの、バグ報告のページで確認できます。他にまだ報告されておらず、再現性がある現象であると確認できた場合、バグ報告を行ってください。

よいバグ報告には、次の 3 点が含まれています：  - 再現するまでの手順
  - その操作によって期待される動作
  - 実際の動作

開発者がバグを発見し、修正するのに助けとなる情報を添付してください。例えば、該当ファイルやログの一部、スクリーンショットなどです。

ユーザーグループでの過去のやりとりのアーカイブは、以下のサイトで閲覧できます：
     http://groups.yahoo.com/group/OmegaT/

バグ報告の閲覧、新規にバグ報告を行う場合は、以下をご参照ください：
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

すでに行った報告に関する進捗を確認したい場合は、SourceForge ユーザー登録してください。

==============================================================================
6.   リリースに関する詳細

現在のバージョンや、これまでにリリースされたバージョンの変更に関する詳細は、 changes.txt ファイルをご覧ください。


==============================================================================
