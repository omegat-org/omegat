OmegaT の日本語版は、以下の貢献者によって作成されました(敬称略)：
copyright  2004 柳田 ひさし
copyright 2006 可知 豊,林 たかゆき,中山 嘉孝,白方 健太郎,横田 邦彦,松谷 善久
copyright 2006-2009,2020-2021 エラリー ジャンクリストフ
copyright 2009 篠原 範子
copyright 2013-2015 Yu-Tang
copyright 2020-2023 Hiroshi Miura


OmegaT とは？
===============

OmegaT は、自由に使用できるオープンソースのコンピューター翻訳支援ツールです。参考訳文、翻訳メモリ、キーワード検索、用語集などの機能があり、既存のプロジェクトを更新するための翻訳にも活用できます。



ライセンス情報
=====================

OmegaTは、Free Software Foundationが公開しているGNU General Public License Version 3および(任意の)以降のバージョンの
ライセンス条項の下で利用可能です。
ライセンスのテキストは、
/docs/OmegaT-license.txt
を参照してください。.

OmegaT は多くのライブラリを使用しています。各ライブラリのライセンスについては、
/lib/licenses/Licenses.txt
に記載されています。



OmegaTをインストールするための前提条件
===================================

OmegaT の実行には、Java Runtime Environment（JRE）バージョン 1.8 以降があらかじめインストールされている必要があります。

JRE の選択や入手、またはインストールにおけるトラブルを回避するため、現在の OmegaT は JRE 付きパッケージが標準で提供されています。 



OmegaT のインストール (Windows)
=============================

インストールプログラムを実行してください。



OmegaTのインストール (Mac)
=======================

OmegaT.zip アーカイブを任意のフォルダーにコピーし、展開してください。内容は、取扱説明書の一覧用 HTML ファイルと OmegaT.app、そしてプログラムファイルなどです。フォルダーを、「Applications」フォルダーなどの適切な場所に移動します。



OmegaTのインストール (Linux)
=========================

アーカイブを任意のフォルダーに配置して、展開してください。これで、OmegaT を起動できるようになります。

あるいは、linux-install.sh のようなインストール スクリプトを使うと、もっとユーザーに分かりやすい形でインストールできます。この場合は、ターミナルウィンドウ（コンソール）を開き、OmegaT.jar と linux-install.sh スクリプトを含むフォルダーに移動して、./linux-install.sh を実行してください。



OmegaTのインストール (Solaris, FreeBSD, etc.)
=========================================

アーカイブを任意のフォルダーに配置して、展開してください。これで、OmegaT を起動できるようになります。



OmegaT (Windows)の起動
==========================

インストール中に、デスクトップにショートカットを作成した場合は、それをダブルクリックしてください。

インストーラーは、スタートメニュー、デスクトップ、およびクイック起動領域にショートカットを作成できます。あるいは OmegaT.exe ファイルをマウスの右ボタンでドラッグすれば、ショートカットを自分で作成し、上記のような場所へ配置することもできます。

もしファイルマネージャー（Windows エクスプローラー）上で見たときに、拡張子なしのファイル「OmegaT」しか見つからない場合は、設定を変更（Windows 7 の場合、[コントロールパネル]-[デスクトップのカスタマイズ]-[フォルダー オプション]-[すべてのファイルとフォルダーを表示]-[表示] タブ-[詳細設定]-[登録されている拡張子は表示しない] をオフに）してください。



OmegaT (Mac)の起動
======================

OmegaT アプリケーションをダブルクリックします。

任意の場所から起動できるように、 OmegaT アプリケーションをドックにドラッグするか、「 Finder 」ウィンドウのツールバーにドラッグすることができます。また、 Spotlight 検索フィールドから起動することもできます。



OmegaT (Linux)の起動
========================

linux-install.sh スクリプトを使用した場合は、以下を使用して OmegaT を起動する必要があります。

    Alt+F2

次に:

    omegat

Kaptain スクリプト（omegat.kaptn）を使うと、ユーザーに分かりやすい方法で OmegaT を起動できます。このスクリプトを使用するには、最初に Kaptainをインストールしてください。次に、 Kaptain 起動スクリプトを以下のように起動して起動できます。

    Alt+F2

次に:

    omegat.kaptn



OmegaTをコマンドラインから起動 (全システム)
====================================================

OmegaT を起動するためのコマンドは次のとおりです。

    cd <OmegaT.jar が存在するフォルダー>

    <Java 実行ファイルへのパス> -jar OmegaT.jar

（Java 実行ファイルは、Linux の場合は java、Windows の場合は java.exe です。システムレベルで Java がインストールされている場合、フルパスを指定する必要はありません。）



寄稿者
============

OmegaT のオリジナルは Keith Godfrey によるものです。

Jean-Christophe Helary (プロジェクトマネージャー)

現在のチーム:
(アルファベット順)

- Marco Cevoli (Telegram community manager)
- Jean-Christophe Helary (Twitter community manager)
- Kos Ivantsov (localisation manager)
- Concepción Martin (Facebook community manager)
- Hiroshi Miura (lead developer)
- Briac Pilpré (webmaster)
- Lucie Vecerova (Facebook community manager)

コードへの貢献は、/docs/contributors.txtに記載されています。

これまでに貢献してくれた方々：
（アルファベット順）

- Anthony Baldwin (ローカリゼーション担当)
- Didier Briel (プロジェクトマネージャー)
- Alex Buloichik (リード開発者)
- Sabine Cretella
- Dmitri Gabinski
- Aaron Madlon-Kay (プロジェクトマネージャー)
- Maxym Mykhalchuk (リード開発者)
- Samuel Murray
- Henry Pijffers (リリース担当)
- Marc Prior (プロジェクトコーディネーター、ウェブサイト管理者)
- Vito Smolej (文書作成担当)

そして、大いに貢献してくださった多くの方々

（万が一、OmegaT プロジェクトへ大きく協力したのに、ここに名前がないという場合は、遠慮なくご連絡ください）



便利なリンク
==============

OmegaT に関する最新の情報は以下のウェブサイトで公開されています:

  https://omegat.org/

ユーザーサポート情報：

  https://omegat.org/support

機能に関する要望は SourceForge 内の開発サイトへ（英語のみ）：

  https://sourceforge.net/p/omegat/feature-requests/

バグ報告も同じく SourceForge 内の開発サイトへ（英語のみ）：

  https://sourceforge.net/p/omegat/bugs/
