OmegaT の日本語版は、以下の貢献者によって作成されました：
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
  OmegaT 2.0 - お読みください

  1.  OmegaT について
  2.  OmegaT とは？
  3.  OmegaT をインストールする
  4.  OmegaT プロジェクトへの貢献
  5.  バグかな？と思ったら
  6.  リリースに関する詳細

==============================================================================
  1.  OmegaT について


OmegaT に関する最新の情報は以下のウェブサイトで公開されています：
      http://www.omegat.org/

ユーザーサポートは Yahoo ユーザーグループで提供されています（多言語対応）。過去ログは、ユーザー登録しなくても検索できます。
     http://groups.yahoo.com/group/omegat/

機能に関する要望は SourceForge 内の開発サイトへ（英語のみ）：
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

バグ報告も同じく SourceForge 内の開発サイトへ（英語のみ）：
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  OmegaT とは？

OmegaT は翻訳支援ツールです。そして自由なソフトウェアです。これはたとえ商用利用であっても、利用料を支払う必要が無いということを意味しています。ユーザーライセンスを遵守すれば、改変や再配布も自由に行えます。

OmegaT の主な特徴は以下の通りです：
  - Java に対応したオペレーティングシステム上であれば動作可能
  - あらゆる有効な TMX ファイルを、翻訳の参照として利用可能
  - SRX（Segmentation Rules eXchange）に近い手法による、柔軟な分節化
  - プロジェクトや翻訳メモリ内の検索
  - 任意のフォルダーにある、対応した形式を持つファイルの検索  
  - 参考訳文の照合
  - 複雑なフォルダー構造を含むプロジェクトでも、スマートな取り扱い
  - 用語集（専門用語の確認） 
  - オープンソースのオンザフライの綴り確認ツールへの対応
  - StarDict 辞書への対応
  - Google Translate などの機械翻訳サービスへの対応
  - 簡潔でわかりやすい取扱説明書やチュートリアル（お手軽スタートガイド）
  - ソフトウェア自身が多数の言語へ翻訳されている

OmegaT は以下のファイル形式に対応しています：

- プレーンテキストファイル形式

  - ASCII テキスト（.txt など）
  - エンコードされたテキスト（.UTF8）
  - Java リソースバンドル（.properties）
  - PO ファイル（.po）
  - INI ファイル（キー＝値 形式）（.ini）
  - DTD ファイル（.DTD）
  - DokuWiki ファイル（.txt）
  - SubRip 字幕ファイル（.srt）
  - Magento CE Locale CSV ファイル（.csv）

- タグ付きテキストファイル形式

  - OpenOffice.org / OpenDocument（.ods、.ots、.odt、.ott、.odp、.otp）
  - Microsoft Open XML（.docx、.xlsx、.pptx）
  - (X)HTML（.html、.xhtml、.xht）
  - HTML ヘルプ コンパイラ（.hhc、.hhk）
  - DocBook（.xml）
  - monolingual XLIFF（.xlf、.xliff、.sdlxliff）
  - QuarkXPress CopyFlowGold（.tag、.xtg）
  - ResX ファイル（.resx）
  - Android リソース・ファイル（.xml）
  - LaTex（.tex、.latex）
  - Help（.xml）とマニュアル（.hmxp）ファイル
  - Typo3 LocManager（.xml）
  - WiX ローカリゼーションファイル（.wxl）
  - Iceni Infix（.xml）
  - Flash の XML 出力（.xml）
  - Wordfast TXML ファイル（.txml）
  - Camtasia Studio for Windows（.camproj）
  - Microsoft Visio ファイル（.vxd）

他のファイル形式に対しても、 カスタマイズにより対応可能です。

OmegaT は、原文フォルダーの階層が非常に複雑な場合も、対応した形式のファイルをすべて自動的に検索します。そして訳文フォルダーの生成時にはまったく同じ階層を再現し、対応していない形式のファイルはそのままコピーします。

OmegaT を手早く使い慣れたい場合は、まず OmegaT を起動して「お手軽スタートガイド」をお読みください。

取扱説明書は、ダウンロードしたパッケージに含まれており、OmegaT 起動後の［ヘルプ］メニューから読むことができます。

==============================================================================
 3. OmegaT をインストールする

3.1 全般の確認事項
OmegaT の実行には、Java Runtime Environment（JRE）バージョン 1.5 以降があらかじめインストールされている必要があります。JRE の選択や入手、またはインストールにおけるトラブルを回避するため、現在の OmegaT は JRE 付きパッケージが標準で提供されています。  

すでに Java 実行環境があるなら、現バージョンの OmegaT をインストールするひとつの方法として、Java Web Start を使うことができます。この場合、以下のファイルをダウンロードし、実行してください：

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

最初の実行時に、お使いのコンピューターに適した実行環境とアプリケーションを自動でインストールします。それ以降の実行時は、オンラインである必要はありません。

インストール中、OS によってはいくつかのセキュリティ警告が表示される場合があります。証明書は「Didier Briel」による自己署名がなされています。この（Java Web Start 版）実行ファイルに与える権限（「コンピューターへの無制限アクセス」と表示される可能性があります）は、通常のローカル版に対して適用するものと同一で、コンピューターのハードディスクにアクセスすることを許可するものです。インストール後は、OmegaT.jnlp をクリックすると、もしオンラインであればプログラムの更新がないか検索し、もしあればインストールした後、OmegaT を起動します。 

ダウンロードとインストールを行う他の方法は、下記に示す通りです。 

Windows、Linux ユーザー：
適した JRE がすでにインストールされているとわかっている場合、JRE 無しのパッケージ（パッケージ名に「Without_JRE」がついています）をインストールしてください。JRE がインストールされているか不明な場合は、JRE が付属したパッケージをお勧めします。すでに JRE がその OS にインストールされていたとしても、付属の JRE がそれと競合することは無いため、安全です。

Linux ユーザー：
多くのディストリビューション（Ubuntu など）に含まれているオープンソースの Java 実装上で OmegaT を使用した場合、何らかのトラブルや表示上の問題、機能の欠落などに遭遇する可能性があります。そのため、可能な限り Oracle 社が提供する Java Runtime Environment（JRE）をダウンロードしてインストールするか、JRE が付属した OmegaT パッケージを利用してください（「Linux」と記されている .tar.bz2 ファイルです）。Java をシステムレベルにインストールした場合は、起動パスが通っているか、あるいは OmegaT を起動する際にパスを明示して Java を呼んでいることを確認してください。Linux に精通しているのでない限り、JRE が付属した OmegaT パッケージをインストールするのがよいでしょう。すでに JRE がその OS にインストールされていたとしても、付属の JRE がそれと競合することは無いため、安全です。

Mac ユーザー：
Mac OS X 10.7（Lion）より前であれば、JRE はすでに Mac OS X 上にインストールされています。Lion ユーザーは、Java を必要とするプログラムを最初に起動した時点で、Java ランタイムが必要であることを示すメッセージが表示されます。そしてシステムが自動でダウンロードとインストールを行います。

PowerPC 系システムの Linux ユーザー：
Oracle 社は PPC 用 JRE を提供していないため、IBM 社が提供する JRE をあらかじめインストールしてください。ダウンロード先は：

    http://www.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 OmegaT のインストール
* Windows ユーザー：
インストールプログラムを実行してください。OmegaT を起動するためのショートカットも作成できます。

* Linux ユーザー：
アーカイブを任意のフォルダーに配置して、展開してください。これで、OmegaT を起動できるようになります。あるいは、linux-install.sh のようなインストール スクリプトを使うと、もっとユーザーに分かりやすい形でインストールできます。この場合は、ターミナルウィンドウ（コンソール）を開き、OmegaT.jar と linux-install.sh スクリプトを含むフォルダーに移動して、./linux-install.sh を実行してください。

Mac ユーザー：
OmegaT.zip アーカイブを任意のフォルダーにコピーし、展開してください。内容は、取扱説明書の一覧用 HTML ファイルと OmegaT.app、そしてプログラムファイルなどです。

* その他のユーザー（Solaris、FreeBSD など）：
OmegaT 用のフォルダーを適切な場所に作成します。そこに OmegaT の zip か tar.bz2 アーカイブをダウンロードし、展開してください。

3.3 OmegaT の起動
いくつかの方法で起動できます。

* Windows ユーザー：
インストール中に、デスクトップにショートカットを作成した場合は、それをダブルクリックしてください。あるいは、インストール先にある OmegaT.exe をダブルクリックしてください。もしファイルマネージャー（Windows エクスプローラー）上で見たときに、拡張子なしのファイル「OmegaT」しか見つからない場合は、設定を変更（Windows 7 の場合、[コントロールパネル]-[デスクトップのカスタマイズ]-[フォルダー オプション]-[すべてのファイルとフォルダーを表示]-[表示] タブ-[詳細設定]-[登録されている拡張子は表示しない] をオフに）してください。

* Linux ユーザー：
インストール スクリプトを使ってインストールした場合は、次のキー入力で起動できます：
Alt+F2
そして：
omegat

* Mac ユーザー：
ファイル OmegaT.app をダブルクリックしてください。

* ファイルマネージャー経由（全 OS 共通）：
ファイル OmegaT.jar をダブルクリックしてください。この方法は、拡張子 .jar を持つファイルが Java と関連づけられている場合のみ有効です。

* コマンドライン経由（全 OS 共通）：
OmegaT 起動のためのコマンドは：

cd <OmegaT.jar が存在するフォルダー>

<Java 実行ファイルへのパス> -jar OmegaT.jar

（Java 実行ファイルは、Linux の場合は java、Windows の場合は java.exe です。システムレベルで Java がインストールされている場合、フルパスを指定する必要はありません。）

OmegaT 起動方法のカスタマイズ：

* Windows ユーザー：
インストールプログラムを実行すると、スタートメニューやデスクトップ、クイック起動バーへショートカットを作成できます。あるいは OmegaT.exe ファイルをマウスの右ボタンでドラッグすれば、ショートカットを自分で作成し、上記のような場所へ配置することもできます。

* Linux ユーザー：
Kaptain スクリプト（omegat.kaptn）を使うと、ユーザーに分かりやすい方法で OmegaT を起動できます。そのためには、まず最初に Kaptain をインストールしてください。それが済めば、次のコマンドで Kaptain 起動スクリプトを実行できます：
Alt+F2
omegat.kaptn

Kaptain スクリプトと、Linux におけるメニュー項目と起動アイコンの追加に関する詳細は、OmegaT ウェブサイトの Linux HowTo を参照してください。

* Mac ユーザー：
OmegaT.app をドックか、ファインダーのツールバーへドラッグしてください。そこから起動できるようになります。Spotlight の検索欄から呼び出すこともできます。

==============================================================================
 4. OmegaT プロジェクトへの貢献

OmegaT の開発に協力したい場合、以下のサイトから開発者たちと連絡を取ってください：
      http://lists.sourceforge.net/lists/listinfo/omegat-development

OmegaT のユーザーインターフェースや取扱説明書、他の関連文書の翻訳に協力したい場合、まず下記のファイルを読んでください：
      http://www.omegat.org/en/translation-info.html

そして翻訳者のメーリングリストに参加してください：
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

他の何らかの方法で協力したい場合、まずユーザーグループに参加してください：
      http://tech.groups.yahoo.com/group/omegat/

そして、OmegaT の世界がどんな雰囲気であるのかをつかんでください。

  OmegaT のオリジナルは Keith Godfrey によるものです。
  Marc Prior が OmegaT プロジェクトのコーディネーターです。

これまでに貢献してくれた方々：
（アルファベット順）

コードに関する貢献者
  Zoltan Bartko
  Volker Berlin
  Didier Briel（開発担当）
  Kim Bruning
  Alex Buloichik（開発リーダー）
  Sandra Jean Chua
  Thomas Cordonnier
  Martin Fleurke  
  Wildrich Fourie
  Phillip Hall
  Jean-Christophe Helary
  Thomas Huriaux
  Hans-Peter Jacobs
  Ibai Lakunza Velasco
  Guido Leenders
  Aaron Madlon-Kay
  Fabián Mandelbaum
  John Moran
  Maxym Mykhalchuk 
  Arno Peters
  Henry Pijffers 
  Briac Pilpré
  Tiago Saboga
  Andrzej Sawuła
  Benjamin Siband
  Yu Tang
  Rashid Umarov  
  Antonio Vilei
  Martin Wunderlich
  Michael Zakharov

その他の貢献者
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary（地域化担当）
  Vito Smolej（文書作成担当）
  Samuel Murray
  Marc Prior 
  そして、大いに貢献してくださった多くの方々

（万が一、OmegaT プロジェクトへ大きく協力したのに、ここに名前がないという場合は、遠慮なくご連絡ください）

OmegaT は以下に示すライブラリを使用しています。
  Somik Raha, Derrick Oswald などによる HTMLParser 1.6（LGPL ライセンス）
  Steve Roy による MRJ Adapter 1.0.8（LGPL ライセンス）
  VLSolutions 社による VLDocking Framework 2.1.4（CeCILL ライセンス）
  László Németh などによる Hunspell（LGPL ライセンス）
  Todd Fast, Timothy Wall などによる JNA（LGPL ライセンス）
  Swing-Layout 1.0.2（LGPL ライセンス）
  Jmyspell 2.1.4（LGPL ライセンス）
  JAXB（GPL v2 ライセンスと Classpath 例外）
  SJXP 1.0.2（GPL v2 ライセンス）
  SVNKit 1.7.5（TMate ライセンス）
  Sequence Library（Sequence Library ライセンス）
  ANTLR 3.4（ANTLR 3 ライセンス）
  SQLJet 1.1.3（GPL v2 ライセンス）
  JGit（Eclipse Distribution ライセンス）
  JSch（JSch ライセンス）
  Base64（パブリックドメイン）
  Diff（GPL ライセンス）

==============================================================================
 5.  バグかな？と思ったら

バグ報告の前に、まず取扱説明書などをじゅうぶん確認してください。発見したその現象は、バグではなく OmegaT の特徴である可能性もあります。OmegaT のログに「Error」「Warning」「Exception」「died unexpectedly」といった単語が残っていれば、おそらく何らかの問題が生じています（log.txt はユーザー設定フォルダーに生成されます。ログファイルの場所については、取扱説明書を参照してください）。

次に、その現象がすでに他のユーザーから報告されていないかどうか、確認してください。SourceForge の OmegaT 開発サイトの、バグ報告のページで確認できます。他にまだ報告されておらず、再現性がある現象であると確認できた場合、バグ報告を行ってください。

よいバグ報告には、次の 3 点が含まれています：
  - 再現するまでの手順
  - その操作によって期待される動作
  - 実際の動作

開発者がバグを発見し、修正するのに助けとなる情報を添付してください。たとえば、該当ファイルやログの一部、スクリーンショットなどです。

ユーザーグループの過去ログは、以下のサイトで閲覧できます：
     http://groups.yahoo.com/group/omegat/

バグ報告を閲覧したり、新規にバグ報告を行う場合は、以下を参照してください：
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

バグ報告に関する進捗を確認したい場合は、SourceForge にユーザー登録してください。

==============================================================================
6.   リリースに関する詳細

現在のバージョンや、これまでにリリースされたバージョンの変更に関する詳細は、 changes.txt ファイルをご覧ください。


==============================================================================
