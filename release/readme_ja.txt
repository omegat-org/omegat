OmegaTの日本語版は以下の貢献者によりできたものです：
・1.4版全体：柳田 ひさし氏 copyright© 2004
・1.6版 お手軽スタートガイド：可知 豊氏 copyright© 2006
・1.6版 UI：林 たかゆき氏 copyright© 2006
・1.6版 readme：中山 嘉孝氏 copyright© 2006
・1.6版 ユーザーマニュール：白方 健太郎氏 copyright© 2006
・1.6版 全体チェック 横田 邦彦氏
・1.6版 全体チェック エラリー ジャンクリストフ氏
・1.6版 全体チェック＋差分翻訳 松谷 善久氏


==============================================================================
  OmegaT 1.6.2 ー お読みください。

  1.  OmegaTについて
  2.  OmegaTとは？  
  3.  General notes about Java & OmegaT
  4.  OmegaTへの貢献
  5.  OmegaTにお困りですか？ヘルプが必要な方はこちら  6.  リリースに関する詳細

==============================================================================
  1.  OmegaTについて


Omega Tに関する最新の情報は以下のページで（現在、英語、オランダ語、スロバキア語、ポルトガル語のみで）ご覧になれます：
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
  -Javaをサポートしているオペレーティングシステムであれば動作可能
  - 翻訳リファレンスとしてどんな種類のTMXファイルをも利用
  -SRXのような方法を用いたフレキシブルな文章の分割
  - 翻訳メモリのリファレンスやプロジェクトを検索
  - OmegaTが読み込み可能なファイルを含んだ全てのディレクトリの検索
  - ファジーマッチング
  - 複雑なディレクトリ構造になったプロジェクトの取り扱い
  - 用語集をサポート（用語チェック）
  - ドキュメントやチュートリアルがわかりやすい
  - 多種の言語へのローカライズ

OmegaTは、OpenDocument形式、マイクロソフトオフィス形式（OpenOffice.orgを変換フィルタとして利用）、OpenOffice.orgまたはStarOffice形式、(X)HTMLやJava他言語化ファイル、それにプレーンテキスト形式などのファイルを対応しています。

OmegaTはとても複雑な階層構造になったソースフォルダであっても、対応しているファイルを全て見つけ、自動的に解析します。 また、全く同じ構造のターゲットフォルダを作成し、その中には非対応のファイルもコピーが含まれています。

すぐにOmegaTを使えるようになりたければ、OmegaTを立ち上げ、「お手軽スタートガイド」をお読みください。

取扱説明ガイドはダウンロードしたパッケージに含まれており、OmegaTを立ち上げ、［ヘルプ］メニューから読むことができます。

==============================================================================
 3. General notes about Java & OmegaT

OmegaTをインストールするのに、Java Runtime Environment バージョン1.4またはそれ以上を必要とします。ダウンロードの場所は：
    http://java.com

WindowsやLinuxのユーザーは、Javaがインストールされていない場合、インストールする必要があります。OmegaTのダウンロードサイトでは、 Java が予め入ったパッケージも提供しています。Mac OSXユーザーは既にJavaがインストールされています。

Javaがインストールされているパソコンであれば、OmegaT.jarファイルをダブルクリックしてOmegaTを起動させることができます。

場合によって、Javaインストール後、「java」アプリケーションがあるディレクトリを含むシステムパスの変数を変更する必要性があります。

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

  OmegaTは元々Keith Godfreyの作品です。  Marc PriorはOmegaTプロジェクトのコーディネーターです。

これまでに貢献してくれた方々： （アルファベット順）

コードに関する貢献者
  Didier Briel
  Kim Bruning
  Sacha Chua
  Thomas Huriaux
  Maxym Mykhalchuk （現在の開発担当）
  Henry Pijffers（リリース担当）
  Benjamin Siband
  Martin Wunderlich

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
  Erhan Yükselci（トルコ語）
  Dragomir Kovacevic（セルボクロアチア語）
  Claudio Nasso（イタリア語）
  Ahmet Murati（アルバニア語）
  Sonja Tomaskovic（ドイツ語）

上記以外の貢献者
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary（現在の文書作成担当）
  Samuel Murray
  Marc Prior（現在の多言語化担当）
  そしてとても大変多くの非常に貢献してくださった方々。

（貢献されている方で、名前は書かれていない場合は開発チームの方へご連絡ください。）

OmegaTは、下記のライブラリを使用します。
  Somik Raha氏、Derrick Oswald氏などによるHTMLParser（LGPLライセンス）。
  http://sourceforge.net/projects/htmlparser

  Steve Roy氏によるMRJ Adapter（LGPLライセンス）。
  http://homepage.mac.com/sroy/mrjadapter/

  VLSolutions社によるVLDocking Framework（CeCILLライセンス）。
  http://www.vlsolutions.com/en/products/docking/

==============================================================================
 5.  OmegaTにお困りですか？ヘルプが必要ですか？

取り扱い説明書をじっくり確認してから、バグを報告してください。ちょうど発見した状態がOmegaTの特徴である可能性があります。OmegaTのログをチェックし「エラー（Error）」、「警告（Warning）」、「例外（Exception）」または「強制終了（died unexpectedly）」といった言葉があった場合、何か起こっています。（log.txtはユーザー設定ディレクトリに置かれています。その場所についてはマニュアルお読みください。）

次に行うことは、他のユーザーの方々とあなたが発見した状態を確認しあい、その状態がそれまでに報告されていないバグであることを確認してください。またSourceForgeでもバグリポートのページで確認することができます。あなた自身が最初の発見者であり、且つ予想外の事象によって引き起こされる再現可能な問題である場合の時のみ、バグリポートを申請してください。

良いバグリポートには次の3点が必ず含まれています。  - 再現するまでの手順
  - その動作によって予想していた結果
  - 実際に表示された結果

ファイルのコピーやログ、それに画面のスクリーンショットなどのディベロッパー側がバグを見つけることができ、修正をするのに役立つ資料を一緒につけて提出することができます。

ユーザーグループのアーカイブを閲覧するには、下記のリンクへお進みください。
     http://groups.yahoo.com/group/OmegaT/

バグリポートを閲覧したり、新しいバグリポートを提出する場合には、下記のリンクへお進みください。
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

自分が報告したバグリポートがどうなるか見守りたい場合には、Source Forgeユーザーとして登録してください。

==============================================================================
6.   リリースに関する詳細

5.  OmegaTにお困りですか？ヘルプが必要ですか？

取り扱い説明書をじっくり確認してから、バグを報告してください。ちょうど発見した状態がOmegaTの特徴である可能性があります。OmegaTのログをチェックし「エラー（Error）」、「警告（Warning）」、「例外（Exception）」または「強制終了（died unexpectedly）」といった言葉があった場合、何か起こっています。（log.txtはユーザー設定ディレクトリに置かれています。その場所についてはマニュアルお読みください。）

次に行うことは、他のユーザーの方々とあなたが発見した状態を確認しあい、その状態がそれまでに報告されていないバグであることを確認してください。またSourceForgeでもバグリポートのページで確認することができます。あなた自身が最初の発見者であり、且つ予想外の事象によって引き起こされる再現可能な問題である場合の時のみ、バグリポートを申請してください。

良いバグリポートには次の3点が必ず含まれています。  - 再現するまでの手順
  - その動作によって予想していた結果
  - 実際に表示された結果

ファイルのコピーやログ、それに画面のスクリーンショットなどのディベロッパー側がバグを見つけることができ、修正をするのに役立つ資料を一緒につけて提出することができます。

ユーザーグループのアーカイブを閲覧するには、下記のリンクへお進みください。
     http://tech.groups.yahoo.com/group/omegat/

バグリポートを閲覧したり、新しいバグリポートを提出する場合には、下記のリンクへお進みください。
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

自分が報告したバグリポートがどうなるか見守りたい場合には、Source Forgeユーザーとして登録してください。

==============================================================================
6.   リリースに関する詳細

このバージョンやこれまでにリリースされたバージョンの変更に関する詳細情報は、「changes.txt」ファイルをご覧ください。

サポートしているファイルフォーマット
  - プレーンテキスト
  - HTMLまたはXHTML
  - HTMLヘルプコンパイラ（HCC）
  - OpenDocument / OpenOffice.org
  - Java リソースバンドル（.properties）
  - INI（キー値 形式、多数のエンコード対応）
  - PO ファイル
  - DocBook documentation file format
  - Microsoft OpenXMLファイル形式

コアの変更箇所
  - フレキシブルな（文章）分節化
  - プラグインとしてファイルフォーマットフィルタの作成
  - より多くのコメントでコードを再分解
  - ウィンドウズインストーラー
  - HTMLタグの属性の翻訳
  - TMX 1.1-1.4b Level 1との完全な互換性
  - TMX 1.4b Level 2との基本的な互換性

新しいUIの特徴（OmegaT 1.4 シリーズとの比較）：
  - より一層機能性が拡張されて書き換えたインターフェイス
  - ウインドウのレイアウトは変更できるようになり、見やすくなったメインインターフェイス

==============================================================================

