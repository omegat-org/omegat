本文翻译工作由 Cliff Peng 完成 ，copyright© 2010

==============================================================================
  OmegaT 2.0, 自述文件

  1.  关于 OmegaT
  2.  OmegaT 为何物？  3.  安装 OmegaT
  4.  为 OmegaT 做点贡献
  5.  使用 OmegaT 过程中遇到了 Bug？需要帮助吗？  6.  发布细节

==============================================================================
  1.  关于 OmegaT


可从下列网址获取 OmegaT 的最新信息：
      http://www.omegat.org/

在 Yahoo 用户组可以获得（多语种）用户支持，无需订阅也可以对归档邮件进行搜索，网址是：
     http://groups.yahoo.com/group/OmegaT/

在 SourceForge 网站的下列网址可以（用英语）提出改进建议：
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

在 SourceForge 的下列网址可以（用英语）报告 Bug：
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  OmegaT 为何物？

OmegaT 是一种计算机辅助翻译（CAT）工具。它是免费的，也就是说无需支付任何费用就可以使用，甚至是用于专业用途；而且只要遵守用户授权协议，你就可以对它进行自由修改并且（或）重新发布。

OmegaT的主要特性包括：
  - 在任何支持 Java 的操作系统上运行。
  - 使用任何有效的 TMX 文件作为翻译参考。
  - 灵活的断句（使用类似SRX的方法）
  - 在项目和参考翻译记忆中搜索
  - 在任意目录中搜索所支持格式的文件 
  - 模糊匹配
  - 巧妙地处理包含复杂目录结构的项目
  - 词汇表支持（术语检查） 
  - 支持开源的实时拼写检查
  - 支持 StarDict 字典
  - 支持谷歌翻译的机器翻译服务
  - 清晰全面的文档和教程
  - 被本地化成多种语言

OmegaT 直接支持下列文件格式：
  - 纯文本
  - HTML 和 XHTML
  - HTML 帮助编译文件
  - OpenDocument/OpenOffice.org
  - Java 资源文件 (.properties)
  - INI 文件 （以任何形式编码的键值对文件）
  - PO 文件
  - DocBook 文档文件格式
  - 微软 OpenXML 格式文件
  - Okapi 单语 XLIFF 文件
  - QuarkXPress CopyFlowGold
  - 字母文件 (SRT)
  - ResX
  - Android 资源
  - LaTeX

-还可以定制 OmegaT 以支持其它文件格式。

即便是遇到极复杂的源目录层次结构，OmegaT 也可以自动进行分析以访问所有支持的文件，制作一个结构完全相同且包含所有不支持文件的目标目录。

如果需要一份快速入门指南，可以启动 OmegaT 并阅读所显示的《即时入门指南》。

用户手册就在你刚才下载的文件包中，启动 OmegaT 后可从【帮助】菜单中阅读它。

==============================================================================
 3. 安装 OmegaT

3.1 综述
为了运行 OmegaT，系统必须安装了 1.4 或更高版本的 Java 运行环境 (JRE)。为了省去用户选择、获取和安装的麻烦，现在标准的 OmegaT 与 Java 运行环境一道被提供。 

如果已经安装了 Java，安装 OmegaT 当前版本的最简便方法是使用 Java Web Start。为此，需要下载下面的文件并运行它：

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

它将在第一次运行时为您的计算机安装正确的环境和应用程序本身。之后的调用无需处于在线状态。

在安装期间，根据操作系统的不同，你可能会收到几条安全警告。该证书由 "Didier Briel" 自行签署。您赋予该版本的许可（可能会被认为是“对计算机的无限制访问”）和您给予本地版本的一样的，如同后面提到的按步骤安装：它们被允许访问计算机硬盘。如果处于在线状态，之后再点击 OmegaT.jnlp 将会检查是否有升级，如果有的话将安装升级包并运行 OmegaT。 

另一种方法下载并安装 OmegaT 的方法如下所述： 

Windows 和 Linux 用户: 如果确信系统已经安装了合适的 JRE 版本，你可以安装不带 JRE 的 OmegaT 版本（版本名称中标明为 without_JRE）。如果还是有疑问，我们建议您使用“标准”版本，即带有 JRE 的版本。这是件安全的事情，因为即便系统中已经安装了 JRE ,该版本也不会与之冲突。

Linux 用户: 要注意 OmegaT 无法与一些 Linux 发行版（如 Ubuntu）中的免费开源 Java 实现一起运行，因为它们或是过时，或是不完整。可以通过上述网址下载安装 Sun 公司的 Java 运行环境（JRE）或者直接下载安装捆绑了 JRE 的 OmegaT（标记了“Linux”的.tar.gz 包文件）。

Mac 用户: Mac OS X 上已经安装了 JRE 。

PowerPC 上的 Linux 用户：由于 Sun 公司未提供 PPC 系统的 JRE，用户必须下载 IBM 公司的 JRE。在此情况下可从下列网站下载：

    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 安装
* Windows 用户: 仅需运行安装程序。如果需要，安装程序可以创建启动 OmegaT 的快捷方式。* 其他用户: 为安装 OmegaT，仅需为 OmegaT 创建一个适当的文件夹（例如: Linux 系统上的 /usr/local/lib)。将 OmegaT 的 zip 或 tar.gz 存档文件拷贝到该文件夹并将其在该处解压缩。

3.3 启动 OmegaT 可以通过多种途径启动 OmegaT 。

* Windows 用户: 通过双击 OmegaT.exe 文件。如果在文件管理器（Windows Explorer）中仅看到 OmegaT 而不是 OmegaT.exe 文件，请变更设置以显示文件扩展名。

* 通过双击 OmegaT.jar 文件。该方法仅当 .jar 文件类型与系统中的 Java 相关联时有效。

* 从命令行运行。启动 OmegaT 的命令为：

cd < OmegaT.jar 文件所在文件夹>

<Java 可执行文件的路径和名称> -jar OmegaT.jar

(Java 可执行文件即 Linux 系统中的 java 文件和 Windows 系统中的 java.exe 文件。如果 Java 安装为系统级，则无须输入完整路径。)

* Windows 用户: 安装程序可为您在开始菜单、桌面或快速启动区中创建快捷方式。您也可以手动将 OmegaT.exe 文件拖动到启动菜单、桌面或快速启动区，以将其链接到该处。

* Linux KDE 用户: 可按下列步骤将 OmegaT 添加到菜单中：

-［控制中心］－［桌面］－［面板］－［菜单］－［编辑 K 菜单］－［文件］－［新菜单项／新子菜单］。

然后选择适当的菜单，通过［文件］－［新子菜单］ 和［文件］－［新项目］ 添加一个子菜单／项目。输入 OmegaT 作为新菜单项的名称。

在 "命令［Command］" 字段中，通过导航［navigation］按钮找到 OmegaT  启动脚本并加以选中。 

点击图标按钮（在 Name/Description/Comment 字段的右边），其它按钮［Other Icons］ - 浏览［Browse］，导航至 OmegaT 应用程序文件夹的 /images 子文件夹。选中 OmegaT.png 图标。

最后，通过“文件-保存”［File－Save］将所作变更保存。

* Linux GNOME 用户: 可通过下列步骤将 OmegaT 添加到面板（屏幕顶部的菜单条）：

在面板单击鼠标右键 － “添加到面板”［Add New Launcher］。在［名称］（Name）字段输入 "OmegaT" ，在[命令] （Command）字段使用［导航］按钮找到 OmegaT 启动脚本。选中并通过［确定］（OK）按钮进行确认。

==============================================================================
 4. 参与 OmegaT 项目

如果想参与 OmegaT 的开发，可通过下列网址和开发者取得联系：
    http://lists.sourceforge.net/lists/listinfo/omegat-development

如果想翻译 OmegaT 的用户界面、用户手册或者其它相关文档，请访问以下网址：
      
      http://www.omegat.org/en/translation-info.html

并订阅下列地址的译者邮件列表：
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

如欲提供其它形式帮助，请先订阅下列用户组：
      http://tech.groups.yahoo.com/group/omegat/

并对 OmegaT 世界正在发生的事情做一些了解……

  OmegaT 最初的工作由 Keith Godfrey 完成。  Marc Prior 是 OmegaT 项目的协调组织者。

之前做过贡献的人还包括（姓名字母排序）：

贡献过代码的人：
  Zoltan Bartko
  -Didier Briel (发布经理)
  Kim Bruning
  Alex Buloichik
  Sandra Jean Chua
  Martin Fleurke  
  Wildrich Fourie
  Thomas Huriaux
  Fabián Mandelbaum
  Maxym Mykhalchuk 
  Arno Peters
  Henry Pijffers 
  Tiago Saboga
  Andrzej Sawuła
  Benjamin Siband
  Martin Wunderlich

其他贡献者：
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (localization manager)
  Vito Smolej (文档管理)
  Samuel Murray
  Marc Prior 
  以及非常、非常多的提供了帮助的人们。

（如果您认为自己曾为 OmegaT 项目做出过突出贡献，但未出现在该列表中，请联系我们）

OmegaT 使用了下列类库：

  Somik Raha, Derrick Oswald 等人的HTMLParser （LGPL 许可协议）
  http://sourceforge.net/projects/htmlparser

  Steve Roy 的 MRJ Adapter （LGPL 许可协议）
  http://homepage.mac.com/sroy/mrjadapter/

  VLSolutions 的 VLDocking Framework （CeCILL 许可协议）
  http://www.vlsolutions.com/en/products/docking/

  László Németh 等人的 Hunspell (LGPL 许可协议)

  Todd Fast 、 Timothy Wall 等人的 JNA (LGPL 许可协议)

  Swing-Layout 1.0.2 (LGPL 许可协议)

  Jmyspell 2.1.4 (LGPL 许可协议)

  JAXB 2.1.7 (GPLv2 + classpath exception)

==============================================================================
 5.  使用 OmegaT 过程中遇到了 Bug？需要帮助吗？

在报告 Bug 之前，请确认已经彻底地查阅了本文档。也许你所看到只是 OmegaT 的某种特性，只不过你才发现它而已。如果你查看 OmegaT 的日志文件，并发现了“Error”（错误）、“Warning”（警告）、“Exception（例外）”、或者“Died Unexpected”（意外崩溃）的字样，那么你可能真的发现了Bug。（log.txt文件用户设定目录下，查阅手册了解它的位置）。

接下来要做的事情是让其他用户确认你所发现的问题，以确保它之前没有被报告过。你也可以查阅 SourceForge 的 Bug 报告页面。只有在确认自己是第一个发现某些可再现的事件序列会触发不该发生的情况时，你才应该撰写一份 Bug 报告。

一份好的 Bug 报告书应该完整地包括三个内容：  - 再现 Bug 的步骤
  - 您所期望看到的情况
  - 您预期之外的情况

您可以将文件的拷贝、部分日志、屏幕截图，以及任何其它可以帮助开发人员找到并处理你所遇到的 Bug 的内容添加到报告中。

以下网址可以查阅用户组的归档文件：
     http://groups.yahoo.com/group/OmegaT/

以下网址可以浏览 Bug 报告，在必要的情况下也可以书写一份新的 Bug 报告：
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

如果你想跟进所报告的 BUG 之后续情况，你可能需要注册成为 Source Forge 的用户。

==============================================================================
6.   发布细节

如果想了解本次或以前发行版本的详细变化情况，请查阅“changes.txt”文件。


==============================================================================