本翻译工作由 Cliff Peng 完成，版权© [2007].


==============================================================================
  OmegaT 1.7.1, 读我文件

  1.  关于 OmegaT 的信息
  2.  什么是 OmegaT？  3.  Java 和 OmegaT的常见注意事项
  4.  OmegaT的致谢
  5.  使用OmegaT遇到Bug？需要帮助吗？  6.  版本细节

==============================================================================
  1.  关于 OmegaT 的信息


在下列网址可以找到关于OmegaT的最新信息（包括：英语、斯拉夫语、荷兰语、葡萄牙语）：
      http://www.omegat.org/omegat/omegat.html

在Yahoo用户组可以获得（多语种）用户支持，不用登录也可以对归档文件进行搜索，网址是：
     http://groups.yahoo.com/group/OmegaT/

在SourceForge网站的下列网址可以（用英语）提出改进建议：
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

在SourceForge的下列网址可以（用英语）报告Bug：，
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  什么是 OmegaT？

OmegaT 是一种计算机辅助翻译工具。它是免费的，也就是说不用支付任何费用你就可以使用，甚至是用于职业用途；而且，只要遵守用户授权协议，你就可以对它进行自由修改并且（或）重新发布。

OmegaT的主要特性包括：
  - 在任何支持Java的操作系统上运行。
  - 使用任何有效的TMX文件作为翻译参考。
  - 灵活地进行断句（使用类似SRX方法）
  - 在项目和参考翻译记忆中搜索
  - 在包含OmegaT可读文件的任何目录中进行搜索。
  - 模糊匹配
  - 巧妙处理包含复杂目录结构的项目
  - 词汇表支持（术语检查）
  - 易懂的文档和指南
  - 被翻译成多种语言版本

OmegaT 支持开放文档（OpenDocument）、微软Office文件（使用OpenOffice.org作为转换工具，或者转换为HTML）、OpenOffice.org和StarOffice文件，以及(X)HTML、Java本地化文件（localization files）、文本文件等等。

即便是遇到极复杂的源目录层次结构，OmegaT也可以自动进行分析，访问所支持的文件，并制作一个结构完全相同的目标目录，并复制所有不支持的文件。

如果需要一份快速入门指南，可以启动OmegaT并阅读所显示的《即时入门指南》（Instant Start 
Tutorial）。

用户手册就在你刚才下载的文件包中，启动OmegaT后，可以从【帮助】菜单中找到它。

==============================================================================
 3. 安装OmegaT

3.1 总论
为了运行OmegaT，你的系统中必须安装了1.4或者更高版本的Java运行环境（JRE）。为了免除用户在选择、获取和安装Java运行环境上的烦恼，现在OmegaT 已经将JRE作为标准提供。对于Windows 和 Linux 用户来说，如果确信已经安装了合适的JRE版本，你可以安装不带JRE的OmegaT（版本的名称中有"Without_JRE"字样的）。如果有任何疑问，我们建议你还是使用“标准”版本，也就是带有JRE的版本。这样做是安全的，因为即使系统中已经安装了JRE，这个版本也不会对它有所影响。Linux 用户要注意的是：OmegaT 无法和许多Linux发布中的免费开源Java实现一起工作，因为这些Java多数是过时的或不完整的。可以通过上面的网址下载安装Sun公司的Java运行环境（JRE）或者直接下载安装捆绑了JRE的OmegaT（标记了“Linux”的.tar.gz文件）Mac 用户：JRE已经安装在Mac OS X中。对于使用PowerPC系统的Linux用户：用户必须下载IBM公司的JRE，因为Sun公司没有为PPC系统提供JRE。 在该情况下，可从下列网址下载：
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 

3.2 安装
要安装OmegaT,只需为OmegaT创建一个合适的文件夹（例如：在Windows平台上的C:\Program 
Files\OmegaT 或Linux 平台的 /usr/local/lib ）将 OmegaT zip 拷贝到该文件夹并在该文件夹中解压。

3.3 启动OmegaT
OmegaT可以用多种方法启动。

* Windows 用户：双击 OmegaT-JRE.exe 文件，如果你使用的是包含JRE的版本，或者是直接点击OmegaT.exe。

* 双击 OmegaT.bat 文件如果你在文件管理器（Windows 浏览器）中只看到OmegaT文件却看不到OmegaT.bat文件，可以改变设置让文件扩展名显示出来。

* 双击 OmegaT.jar 文件如果你的系统中文件类型.jar和Java关联在一起的话这样做也可以。

* 从命令行用来启动OmegaT的命令是：

cd <文件 OmegaT.jar 所在文件夹>

<Java 可执行文件的路径和名称> -jar OmegaT.jar

（Linux平台上Java可执行文件为java，Windows平台上为java.exe）如果Java被安装为系统级别，则不需要输入完整的路径。）

* Windows 用户：你可以将 OmegaT-JRE.exe、 OmegaT.exe 或
OmegaT.bat 拖放到桌面或开始菜单中，将其链接到该处。

* Linux KDE 用户：你可以按照下列步骤将OmegaT增加到菜单中:

控制中心 - 桌面 - 面板 - 菜单 - 编辑 K 菜单 - 文件 - 新菜单项/新子菜单.

然后，在选择合适的菜单后，使用 文件 - 新建文件和子菜单 - 新菜单项增加一个子菜单或菜单项。将OmegaT 输入作为新菜单项的名称。

在“命令”字段中，使用导航按钮找到OmegaT的启动脚本并选中它。 

点击图标按钮（在 名称/描述/注释 字段的右边）- 其他图标 - 浏览，然后找到OmegaT 应用程序文件夹的 /images 子文件夹。选择OmegaT.png 图标。

最后，使用 文件 - 保存 将更改存盘。

* Linux GNOME 用户可以按照下列步骤将OmegaT增加到菜单中:

在面板中点击右键 - 增加新启动器在“名字”字段输入“OmegaT”，在“命令”字段，使用导航按钮找到OmegaT启动脚本。选中并点击OK确认。

==============================================================================
 4. OmegaT的致谢

如果想为OmegaT的开发做贡献，可通通过下列网址和开发者取得联系：
    http://lists.sourceforge.net/lists/listinfo/omegat-development

如果想翻译OmegaT的用户界面、用户手册或者其他相关文档，请访问以下网址：
      http://www.omegat.org/omegat/omegat_en/translation-info.html

然后在下列网址的翻译者列表中进行登记：
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

如果想提供其他形式帮助，请先到下列地址的用户组进行登记：
      http://tech.groups.yahoo.com/group/omegat/

并对OmegaT世界正在发生的事情做一些了解……

  OmegaT 最初的工作是由 Keith Godfrey 完成的。  Marc Prior是 OmegaT 项目的协调组织者

之前做过贡献的人还包括：（姓名字母排序）

代码编写人员：
  Didier Briel
  Kim Bruning
  Sacha Chua
  Thomas Huriaux
  Maxym Mykhalchuk （首席开发）
  Henry Pijffers （发布经理）
  Benjamin Siband
  Martin Wunderlich

本地化工作：
 Roberto Argus （葡萄牙语——巴西）
  Alessandro Cattelan （意大利语）
  Sabine Cretella （德语）
  Suzanne Bolduc （世界语）
  Didier Briel （法语）
  Frederik De Vos （荷兰语）
  Cesar Escribano Esteban （西班牙语）
  Dmitri Gabinski （白俄罗斯语、世界语、俄语）
  Takayuki Hayashi （日语）
  Jean-Christophe Helary （法语和日语）
  Yutaka Kachi （日语）
  Dragomir Kovacevic （塞尔维亚——克罗地亚语）
  Elina Lagoudaki （希腊语）
  Martin Lukáč (Slovak)
  Ahmet Murati （阿拉伯语）
  Samuel Murray （斐语）
  Yoshi Nakayama （日语）
  Claudio Nasso （意大利语）
  David Olveira （葡萄牙语）
  Ronaldo Radunz （葡萄牙语——巴西）
  Thelma L. Sabim （葡萄牙语——巴西）
  Juan Salcines （西班牙语）
  Pablo Roca Santiagio （西班牙语）
  Sonja Tomaskovic （德语）
  Karsten Voss （波兰语）
  Gerard van der Weyde （荷兰语）
  Martin Wunderlich （德语）
  Hisashi Yanagida（日语）
  Kunihiko Yokota （日语）
  Erhan Yükselci （土耳其语）
  Mikel Forcada Zubizarreta （加泰隆尼亚语） 

其他贡献者：
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary （文档管理员）
  Samuel Murray
  Marc Prior （本地化翻译管理）
  以及非常、非常多的提供了帮助的人们。

（如果你认为你为Omega项目做出过突出的贡献，但没有出现在该列表中，请联系我们）

OmegaT 使用下列程序库：
  Somik Raha, Derrick Oswald 等人的HTMLParser （LGPL 协议）  http://sourceforge.net/projects/htmlparser

  Steve Roy 的MRJ Adapter （LGPL 协议）  http://homepage.mac.com/sroy/mrjadapter/

  VLSolutions 的VLDocking Framework （CeCILL 协议）  http://www.vlsolutions.com/en/products/docking/

==============================================================================
 5.  使用OmegaT遇到Bug？需要帮助吗？

在报告Bug之前，请确认已经彻底地查阅了文档。也许你所看到只是OmegaT的一个特性，只不过你刚刚发现它而已。如果你查看OmegaT的日志文件，并发现了“Error”（错误）、“Warning”（警告）、“Exception（例外）”、或者“Died Unexpected”（意外崩溃）的字样，那么你可能真的发现了Bug。（log.txt文件用户设定目录下，可以查阅手册了解它的位置）。

接下来要做的事情是让其他用户确认你所发现的问题，以确保它之前没有被报告过。你也可以查阅SourceForge的Bug报告页面。只有在确认自己是第一个发现某些可再现的事件序列会触发不该发生的情况时，你才应该撰写一份Bug报告。

一份好的Bug报告书应该完整地包括三个内容：  - 再现Bug的步骤
  -你所期望看到的情况
  -你预期之外的情况

你可以将文件的拷贝、部分日志、屏幕截图，以及任何其它你认为可以帮助开发人员找到并处理你所遇到的Bug的东西添加到报告中。

以下网址可以查阅用户组的归档文件：
     http://groups.yahoo.com/group/OmegaT/

以下网址可以浏览Bug报告，在必要的情况下也可以书写一份新的Bug报告：
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

如果你想跟近所报告的BUG后续情况，你也许想注册成为Source Forge的用户。

==============================================================================
6.   版本细节

如果想了解本次或以前发行版本的详细变化情况，请查阅“changes.txt”文件。

支持的文件格式：
  - 纯文本
  - HTML 和 XHTML
  - HTML 帮助编译器 (HCC)
  - OpenDocument / OpenOffice.org
  - Java 资源文件 (.properties)
  - INI 文件 （以任何编码的 key=value 对组成的文件）
  - PO 文件
  - DocBook 文档文件格式
  - Microsoft OpenXML 文件
  - Okapi 单语言 XLIFF files

核心变化：
  -

新的用户界面特性（相对于OmegaT1.6系列而言）
  -

==============================================================================

