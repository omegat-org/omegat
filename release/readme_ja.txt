==============================================================================
  OmegaT 1.6.1 アプデート 01番 「お読みください」

  1.  OmegaTについて
  2.  OmegaTとは？
  3.  Java及びOmegaTに関する一般情報
  4.  OmegaTへの貢献
  5.  OmegaTでお困りのことはありませんか？ヘルプが必要な方はこちら
  6.  リリースに関する詳細

==============================================================================
  1.  OmegaTについて

Omega Tに関する最新の情報は以下のページで（現在、オランダ語、英語、ポルトガル語、スロバキア語のみで）ご覧になれます：
      http://www.omegat.org/omegat/omegat.html

ユーザーサポートはYahoo userグループへ（多言語対応）。そのアーカイブは登録せずに検索できるようになっています。
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
  - SRXのような方法を用いた柔軟性のある文章の分節化
  - 翻訳参考ファイルやプロジェクト全体を検索
  - OmegaTが読み込み可能なファイルを含んだ全てのフォルダの検索
  - 参考訳文照合
  - 複雑なフォルダ構造になったプロジェクトの取り扱い
  - 用語集を対応（使用語確認）
  - 入門ガイド（お手軽スタートガイド）や取扱説明ガイドが分かりやすい
  - 多数の言語へのローカライズ

OmegaTは、OpenDocument形式、マイクロソフトオフィス形式（OpenOffice.orgを変換フィルタとして利用）、OpenOffice.orgまたはStarOffice形式、(X)HTMLやJava他言語化ファイル、それにプレーンテキスト形式のファイルを対応しています。

OmegaTはとても複雑な階層構造になった原文フォルダであっても、対応しているファイルを全て見つけ、自動的に解析します。また、全く同じ構造の訳文フォルダを作成し、その中には非対応のファイルもコピーが含まれています。

すぐにOmegaTを使えるようになりたければ、OmegaTを立ち上げ、「お手軽スタートガイド」をお読みください。

取扱説明ガイドはダウンロードしたパッケージに含まれており、OmegaTを立ち上げ、［ヘルプ］メニューから読むことができます。

==============================================================================
 3. Java及びOmegaTに関する一般情報

OmegaTをインストールするのに、Java Runtime Environment バージョン1.4またはそれ以上を必要とします。ダウンロードの場所は：
    http://java.com

WindowsやLinuxのユーザーは、Javaがインストールされていない場合、インストールする必要があります。OmegaTのダウンロードサイトでは、 Java が予め入ったパッケージも提供しています。Mac OSXユーザーは既にJavaがインストールされています。

Javaがインストールされているパソコンであれば、OmegaT.jarファイルをダブルクリックしてOmegaTを起動させることができます。

場合によって、Javaインストール後、「java」アプリケーションがあるフォルダを含むシステムパスの変数を変更する必要性があります。

Linux をお使いの方は、（Ubuntuなど一部のパッケージに含まれている）オープンソースJavaでは（古すぎたり、機能不足といった理由で）OmegaT が動作しない可能性がありますので注意してください。この場合は、上記リンク先より Sun提供の Java Runtime Environment（JRE）をインストールするか、JREつきの OmegaT を利用してください（"Linux"と記されている、tar.gzファイルです）。

PowerPCで Linux をお使いの方は、（SunがPPC用JREを提供していないので）IBM社が提供する JREを予めインストールしてくださいダウンロードサイトはこちらです:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html

==============================================================================
 4. OmegaTへの貢献

OmegaTの開発に協力したい場合、開発者たちと連絡を取ってください：
    http://lists.sourceforge.net/lists/listinfo/omegat-development

OmegaT ユーザーインターフェイスや取扱説明ガイド、または他の関連文書の翻訳に協力したい場合、下記のファイルを読んでください：
      http://www.omegat.org/omegat/omegat_en/translation-info.html

また、翻訳者リストに参加してください：
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

その他の貢献の方法に関して、ユーザーグループに参加してください：
      http://tech.groups.yahoo.com/group/omegat/

そして、OmegaTの世界でどんなことが進行しているか、感じ取ってください…

  OmegaTは元々Keith Godfreyの作品です。
  Marc PriorはOmegaTプロジェクトのコーディネーターです。

これまでに貢献してくれた方々： （ABC 順）

コードに関する貢献者
  Kim Bruning
  Sacha Chua
  Maxym Mykhalchuk（現在の開発担当）
  Henry Pijffers（1.6版　リリース担当）
  Benjamin Siband

多言語化の貢献者
  Roberto Argus（ポルトガル語・ブラジル）
  Alessandro Cattelan（イタリア語）
  Sabine Cretella（ドイツ語）
  Suzanne Bolduc（エスペラント語）
  Didier Briel（フランス語）
  Frederik De Vos（オランダ語）
  Cesar Escribano Esteban（スペイン語）
  Dmitri Gabinski（ベラルーシ語、エスペラント語、ロシア語）
  Takayuki Hayashi（日本語）
  Jean-Christophe Helary（フランス語、日本語）
  Yutaka Kachi（日本語）
  Elina Lagoudaki（ギリシャ語）
  Martin Lukáč（スロバキア語）
  Samuel Murray（アフリカーンス語）
  Yoshi Nakayama（日本語）
  David Olveira（ポルトガル語）
  Ronaldo Radunz（ポルトガル語・ブラジル）
  Thelma L. Sabim（ポルトガル語・ブラジル）
  Juan Salcines（スペイン語）
  Pablo Roca Santiagio（スペイン語）
  Karsten Voss（ポランド後）
  Gerard van der Weyde（オランダ語）
  Martin Wunderlich（ドイツ語）
  Hisashi Yanagida（日本語）
  Kunihiko Yokota（日本語）
  Erhan Yukselci（トルコ語）

上記以外の貢献者
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary（現在の文書作成担当）
  Samuel Murray
  Marc Prior（現在の多言語化担当）
  そしてとても大変多くの非常に貢献してくださった方々。

OmegaTは、下記のライブラリを使用します。

  Somik Raha氏、Derrick Oswald氏などによるHTMLParser（LGPLライセンス）。
  http://sourceforge.net/projects/htmlparser
  
  Steve Roy氏によるMRJ Adapter（LGPLライセンス）。
  http://homepage.mac.com/sroy/mrjadapter/
  
  VLSolutions社によるVLDocking Framework（CeCILLライセンス）。
  http://www.vlsolutions.com/en/products/docking/

==============================================================================
 5.  OmegaTでお困りのことはありませんか？ヘルプが必要ですか？

バグを報告するのは取扱説明ガイドをじっくり確認してからのことです。たった今発見した状態は、バグではなくOmegaTの特徴である可能性があります。OmegaTのログを確認し「エラー（Error）」、「警告（Warning）」、「例外（Exception）」または「強制終了（died unexpectedly）」といった言葉があった場合、何か起こっています。（log.txtはユーザー設定フォルダに置かれています。その場所については取扱説明ガイドお読みください。）

次に行うことは、他のユーザーの方々とあなたが発見した状態を確認しあい、その状態がそれまでに報告されていないバグであることを確認してください。またSourceForgeでもバグレポートのページで確認することができます。あなた自身が最初の発見者であり、且つ予想外の事象によって引き起こされる再現可能な問題である場合の時のみ、バグレポートを申請してください。

良いバグレポートには次の3点が必ず含まれています。
  - 再現するまでの手順
  - その動作によって予想していた結果
  - 実際に表示された結果

ファイルのコピーやログ、それに画面のスクリーンショットなどの開発者側がバグを見つけることができ、修正をするのに役立つ資料を一緒につけて提出することができます。

ユーザーグループのアーカイブを閲覧するには、下記のリンクへお進みください。
     http://tech.groups.yahoo.com/group/omegat/

バグレポートを閲覧したり、新しいバグレポートを提出する場合には、下記のリンクへお進みください。
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

自分が報告したバグレポートがどうなるか見守りたい場合には、Source Forgeユーザーとして登録してください。

==============================================================================
6.   リリースに関する詳細

このバージョンやこれまでにリリースされたバージョンの変更に関する詳細情報は、「changes.txt」ファイルをご覧ください。

対応しているファイル形式
  - プレーンテキスト
  - HTMLまたはXHTML
  - HTMLヘルプコンパイラ（HCC）
  - OpenDocument / OpenOffice.org
  - Java リソースバンドル（.properties）
  - INI（キー値 形式、多数のエンコード対応）
  - PO ファイル
  - DocBookファイル書式

主な変更箇所
  - 柔軟性のある（文章）分節化
  - プラグインとしてファイル形式フィルタの可能な作成
  - より多くのコメントでコードを再分解
  - Windowsインストーラー
  - HTMLタグの属性の翻訳
  - TMX 1.1-1.4b Level 1との完全な互換性
  - TMX 1.4b Level 2との基本的な互換性

新しいUIの特徴（OmegaT 1.4 シリーズとの比較）：
  - より一層機能性が拡張されて書き換えたインターフェイス
  - ウインドウのレイアウトは変更できるようになり、見やすくなったメインインターフェイス

==============================================================================

