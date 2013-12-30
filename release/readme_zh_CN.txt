本文翻译工作由胡杨完成，版权所有© 2013

==============================================================================
  OmegaT 3.0，自述文件

  1.  关于 OmegaT
  2.  OmegaT 为何物？  3.  安装OmegaT
  4.  为 OmegaT 做点贡献
  5.  使用 OmegaT 过程中遇到了 Bug？需要帮助吗？  6.  发布细节

==============================================================================
  1.  关于 OmegaT


可从下列网址获取 OmegaT 的最新信息：
      http://www.omegat.org/

在 Yahoo 用户组可以获得（多语种）用户支持，无需订阅也可以对归档邮件进行搜索，网址是：
     http://tech.groups.yahoo.com/group/OmegaT/

在 SourceForge 网站的下列网址可以（用英语）提出改进建议：
     https://sourceforge.net/p/omegat/feature-requests/

在 SourceForge 的下列网址可以（用英语）报告 Bug：
     https://sourceforge.net/p/omegat/bugs/

==============================================================================
  2.  OmegaT 为何物？

OmegaT 是一种计算机辅助翻译（CAT）工具。它是免费的，也就是说无需支付任何费用就可以使用，甚至是用于专业用途；而且只要遵守用户授权协议，您就可以对它进行自由修改并且（或）重新发布。

OmegaT的主要特性包括：
  - 在任何支持 Java 的操作系统上运行
  - 使用任何有效的 TMX 文件作为翻译参考
  - 灵活的断句（使用类似SRX的方法）
  - 在项目和参考翻译记忆中搜索
  - 在任意目录中搜索所支持格式的文件 
  - 模糊匹配
  - 智能处理包含复杂目录结构的项目
  - 词汇表支持（术语检查） 
  - 支持开源的实时拼写检查工具
  - 支持 StarDict 词典
  - 支持谷歌翻译的机器翻译服务
  - 清晰全面的文档和教程
  - 被本地化成多种语言

OmegaT 直接支持下列文件格式：

- 纯文本文件格式

  - ASCII 文本（.txt等）
  - 已编码文本（*.UTF8）
  - Java资源文件（*.properties）
  - PO文件（*.po）
  - INI（key=value）文件（*.ini）
  - DTD文件（*.DTD）
  - DocuWiki文件（*.txt）
  - SubRip标题文件（*.srt）
  - Magento CE Locale CSV（*.csv）

- 标记文本文件格式

  - OpenOffice.org / OpenDocument（*.odt, *.ott, *.ods, *.ots, *.odp, *.otp）
  - Microsoft Open XML（*.docx, *.xlsx, *.pptx）
  - (X)HTML（*.html, *.xhtml,*.xht）
  - HTML Help Compiler（*.hhc, *.hhk）
  - DocBook（*.xml）
  - 单语XLIFF（*.xlf, *.xliff, *.sdlxliff）
  - QuarkXPress CopyFlowGold（*.tag, *.xtg）
  - ResX文件（*.resx）
  - Android资源（*.xml）
  - LaTex（*.tex, *.latex）
  - Help（*.xml）和Manual（*.hmxp）文件
  - Typo3 LocManager（*.xml）
  - WiX Localization（*.wxl）
  - Iceni Infix（*.xml）
  - Flash XML export（*.xml）
  - Wordfast TXML（*.txml）
  - Camtasia for Windows（*.camproj）
  - Visio（*.vxd）

还可以定制 OmegaT 以支持其它文件格式。

即便是遇到极复杂的源目录层次结构，OmegaT 也可以自动进行分析以访问所有支持的文件，并产生一个结构完全相同且包含所有不支持文件的目标目录。

如果需要一份快速入门指南，可以启动 OmegaT 并阅读所显示的《即时入门指南》。

用户手册就在您刚才下载的文件包中，启动 OmegaT 后可从【帮助】菜单中阅读它。

==============================================================================
 3. 安装OmegaT

3.1综述
为了运行 OmegaT，系统必须安装了 1.5 或更高版本的 Java 运行环境 (JRE)。现在提供了包含Java运行环境的OmegaT文件包以省去用户选择、获取和安装的麻烦。 

如果已经安装了 Java，安装 OmegaT 当前版本的一种方法是使用 Java Web Start。为此，需要下载下面的文件并运行它：

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

它将在第一次运行时为您的计算机安装正确的环境和应用程序本身。之后的调用无需处于在线状态。

在安装期间，根据操作系统的不同，您可能会收到几条安全警告。该证书由 "Didier Briel" 自行签署。您赋予该版本的权限（可能会被认为是“对计算机的无限制访问”）和您给予本地版本的相同，如同后面提到的按步骤安装：它们被允许访问计算机硬盘。如果处于在线状态，之后再点击 OmegaT.jnlp 将会检查是否有升级，如果有的话将安装升级包并运行 OmegaT。 

另一种方法下载并安装 OmegaT 的方法如下所述： 

Windows 和 Linux 用户：如果确信系统已经安装了合适的 JRE 版本，您可以安装不带 JRE 的 OmegaT 版本（版本名称中标明为 without_JRE）。如果存有疑问，我们建议您使用带有 JRE 的版本。这样做是安全的，因为即便系统中已经安装了JRE，该版本也不会与之冲突。

Linux用户：
OmegaT将运行于随许多Linux发行版（如Ubuntu）一起打包的开源实现的Java，但您可能遇到bug、显示问题或遗失的功能。因此我们建议您下载安装 Oracle Java 运行环境（JRE）或者直接下载安装捆绑了JRE的OmegaT文件包（标记了“Linux”的.tar.gz 包文件）。如果您系统上已经安装了Java，您必须确保它在运行路径中，否则需要在启动OmegaT时明确调用。如果您不熟悉Linux，我们建议您安装带有JRE的OmegaT版本。这样做是安全的，因为该JRE不会与系统中已安装的其它Java相冲突。

Mac用户：
在Mac OS X 10.7 (Lion)之前的Mac OS X已经安装了JRE。当Lion用户首次运行需要Java的程序时，系统将提示用户且最后会自动下载并安装。

PowerPC系统上的Linux：
用户需要下载IBM的JRE，因为Sun没有为PPC系统提供JRE。在此情况下可从下列网址下载：

    http://www.ibm.com/developerworks/java/jdk/linux/download.html 


3.2安装
* Windows用户：
仅需启动安装程序。如果需要，安装程序可以创建启动OmegaT的快捷方式。

* Linux用户：
把压缩包放在适当的文件夹并解压后就可以启动OmegaT了。不过您可以使用安装脚本（linux-install.sh）进行更整洁和友好的安装过程。要使用此脚本，请打开终端窗口（控制台），进入包含OmegaT.jar和linux-install.sh的目录并执行./linux-install.sh。

* Mac 用户：
复制OmegaT.zip压缩包到适当的位置并解压，这样可以可到一个包含HTML文档索引文件和OmegaT.app程序文件的文件夹了。

* 其他（如Solaris、FreeBSD）：
安装OmegaT时只需为其创建适当的文件夹。复制OmegaT zip或tar.bz2压缩包到这个文件夹并解压。

3.3启动OmegaT
按如下方式启动OmegaT。

* Windows用户：
若在安装时您选择创建了桌面快捷方式，那么双击它。或者双击OmegaT.exe文件。如果您能在文件管理器（Windows资源管理器）中看到OmegaT但没有OmegaT.exe文件，请修改此设置以显示文件扩展名。

* Linux用户：
如果您用所提供的脚本实施安装，那么可以这样启动OmegaT：
Alt+F2
然后：
omegat

* Mac用户：
双击OmegaT.app文件。

* 从文件管理器（所有系统）：
双击OmegaT.jar文件。该方法仅当.jar文件类型与系统中的Java相关联时有效。

* 从命令行（所有系统）：
启动OmegaT的命令为：

cd <OmegaT.jar 文件所在文件夹>

<Java 可执行文件的路径和名称> -jar OmegaT.jar

（Java 可执行文件即 Linux 系统中的 java 文件和 Windows 系统中的 java.exe 文件。如果系统上安装了Java且处于命令行路径中，则无需输入它的完整路径。）

定制OmegaT启动方式：

* Windows用户：
安装程序可以在开始菜单、桌面和快速启动栏创建快捷方式。您也可以手动将 OmegaT.exe 文件拖动到开始菜单、桌面或快速启动栏，以将其链接到该处。

* Linux用户：
要更友好的启动OmegaT，您可以使用一起提供的Kaptain脚本（omegat.kaptn）。要使用此脚本必须首先安装Kaptain。然后您可以用Alt+F2运行Kaptain启动脚本omegat.kaptn。

关于Kaptain脚本及在Linux上添加菜单项和启动图标的详细信息，请参阅OmegaT on Linux HowTo。

Mac用户：
拖动OmegaT.app到dock面板或Finder窗口的工具栏以便从任意位置启动此程序。您还可以在Spotlight搜索区域中进行调用。

==============================================================================
 4. 参与OmegaT项目

如果想参与 OmegaT 的开发，可通过下列网址和开发者取得联系：
    http://lists.sourceforge.net/lists/listinfo/omegat-development

如果想翻译 OmegaT 的用户界面、用户手册或者其它相关文档，请访问以下网址：
      
      http://www.omegat.org/en/howtos/localizing_omegat.php

并订阅下列地址的译者邮件列表：
      https://lists.sourceforge.net/lists/listinfo/omegat-l10n

如欲提供其它形式的帮助，请先订阅下列用户组：
      http://tech.groups.yahoo.com/group/omegat/

并对 OmegaT 世界正在发生的事情做一些了解……

  OmegaT 最初的工作由 Keith Godfrey 完成。  Marc Prior 是 OmegaT 项目的协调组织者。

之前做过贡献的人还包括（姓名字母排序）：

贡献过代码的人：
  Zoltan Bartko
  Volker Berlin
  Didier Briel (开发经理)
  Kim Bruning
  Alex Buloichik (开发人员主管)
  Sandra Jean Chua
  Thomas Cordonnier
  Martin Fleurke  
  Wildrich Fourie
  Phillip Hall
  Jean-Christophe Helary
  Thomas Huriaux
  Hans-Peter Jacobs
  Kyle Katarn
  Piotr Kulik
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

其他贡献者：
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary（本地化管理员）
  Vito Smolej（文档管理员）
  Samuel Murray
  Marc Prior 
  以及非常、非常多的提供了帮助的人们。

（如果您认为自己曾为 OmegaT 项目做出过突出贡献，但未出现在该列表中，请随时联系我们）

OmegaT 使用了下列类库：
  Somik Raha, Derrick Oswald 等人的 HTMLParser （LGPL 许可）
  Steve Roy 的 MRJ Adapter （LGPL 许可）
  VLSolutions 的 VLDocking Framework（CeCILL 许可）
  László Németh 等人的 Hunspell（LGPL 许可）
  Todd Fast、Timothy Wall 等人的 JNA（LGPL 许可）
  Swing-Layout 1.0.2（LGPL 许可）
  Jmyspell 2.1.4（LGPL 许可）
  SVNKit 1.7.5（TMate许可）
  Sequence Library（Sequence Library许可）
  ANTLR 3.4（ANTLR 3许可）
  SQLJet 1.1.3（GPL v2）
  JGit（Eclipse分发许可）
  JSch（JSch许可）
  Base64（公共领域）
  Diff（GPL）
  orion-ssh2-214（Java的Orion SSH许可）
  lucene-*.jar（Apache License 2.0）
  英语解析器（org.omegat.tokenizer.SnowballEnglishTokenizer和
  org.omegat.tokenizer.LuceneEnglishTokenizer）使用Okapi的分割词汇
（http://okapi.sourceforge.net）（LGPL许可）
  tinysegmenter.jar（修改过的BSD许可）
  commons-*.jar（Apache License 2.0）
  jWordSplitter（Apache License 2.0）
  LanguageTool.jar（LGPL许可）
  morfologik-*.jar（Morfologik许可）
  segment-1.4.1.jar（Segment许可）
  pdfbox-app-1.8.1.jar（Apache License 2.0）

==============================================================================
 5.  使用 OmegaT 过程中遇到了 Bug？需要帮助吗？

在报告 Bug 之前，请确认已经彻底地查阅了本文档。也许您所看到只是 OmegaT 的某种特性，只不过您才发现它而已。如果您查看 OmegaT 的日志文件，并发现了“Error”（错误）、“Warning”（警告）、“Exception（异常）”、或者“Died Unexpected”（意外崩溃）的字样，那么您可能真的发现了 Bug。（log.txt文件在用户设定的目录下，请查阅手册了解它的位置）。

接下来要做的事情是让其他用户确认您所发现的问题，以确保它之前没有被报告过。您也可以查阅 SourceForge 的 Bug 报告页面。只有在确认自己是第一个发现某些可再现的事件序列会触发不该发生的情况时，您才应该撰写一份 Bug 报告。

一份好的 Bug 报告书应该完整地包括三个内容：  - 再现 Bug 的步骤
  - 您期望看到的情况
  - 您实际看到的情况

您可以将文件的副本、部分日志、屏幕截图，以及其他任何可以帮助开发人员找到并处理您所遇到的 Bug 的内容添加到报告中。

以下网址可以查阅用户组的归档文件：
     http://tech.groups.yahoo.com/group/OmegaT/

以下网址可以浏览 Bug 报告，在必要的情况下也可以书写一份新的 Bug 报告：
     https://sourceforge.net/p/omegat/bugs/

如果您想跟进所报告的 BUG 之后续情况，您可能需要注册成为 Source Forge 的用户。

==============================================================================
6.   发布细节

如果想了解本次或以前发行版本的详细变化情况，请查阅“changes.txt”文件。


==============================================================================
