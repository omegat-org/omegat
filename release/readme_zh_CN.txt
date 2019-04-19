@TRANSLATION_NOTICE@


OmegaT 是什么？
===============

OmegaT 是自由且开源的多平台计算机辅助翻译工具，具有模糊匹配、翻译记忆、
关键字查找、词汇表、以及为更新后的项目自动套用翻译等特性。



许可信息
=====================

OmegaT 根据自由软件基金会发布的 GNU 通用公共许可证许第 3 版或
（根据您的选择）任何更高版本的条款提供。你可以在此找到许可证的文本：
/docs/OmegaT-license.txt

OmegaT使用了许多库。以下文件中提到了每个库的许可证：
/lib/licenses/Licenses.txt



安装 OmegaT 的先决条件
===================================

OmegaT 需要您的系统安装有 Java 运行环境（JRE）1.8 或者更高版本。

我们推荐使用包含 JRE 的 OmegaT 软件包，以避免选择、获取、与安装 JRE 的麻烦。



安装 OmegaT（Windows）
===========================

启动安装程序。



安装 OmegaT（Mac）
=======================

解压 OmegaT 的 .zip 压缩包以获得包含文档文件和 OmegaT 应用程序的文件夹。
将该文件夹移动到合适的地方，比如 Applications 文件夹。



安装 OmegaT（Linux）
=========================

将压缩包放在任何合适的文件夹中并解压之。然后 OmegaT 就已就位，可以运行了。

不过，您可以使用安装脚本（linux-install.sh）获得更整洁、更友好的安装。
要使用此脚本，请打开终端窗口（控制台），进入包含 OmegaT.jar 和 
linux-install.sh 脚本的文件夹，然后用 ./linux-install.sh 执行脚本。



安装 OmegaT（Solaris、FreeBSD、...）
=========================================

将压缩包放在任何合适的文件夹中并解压之。然后 OmegaT 就已就位，可以运行了。



用 Java Web Start 安装 OmegaT（所有平台）
===========================================================

如果已在系统中安装了 Java，安装 OmegaT 的方法之一是使用 Java Web Start。

为此，需要下载以下文件并运行之：

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

它将在第一次运行时为您的计算机与应用程序自身安装正确的环境。
以后再调用就无需处于在线状态。



启动 OmegaT（Windows）
==========================

如果在安装过程中已经在桌面上创建了快捷方式，双击此快捷方式。

安装程序可以在开始菜单、桌面和快速启动栏中创建快捷方式。
您也可以将 OmegaT.exe 文件手工拖动到开始菜单、桌面或快速启动栏
来将其链接到那些地方。

如果您在文件管理器（Windows 资源管理器）中能看到文件 OmegaT 但看不到
OmegaT.exe，请修改设置以显示文件扩展名。



启动 OmegaT（Mac）
======================

双击 OmegaT 应用程序。

你可以将 OmegaT 应用程序拖动到 dock 面板或 Finder 窗口的工具栏
以便从任意位置启动它。您还可以从 Spotlight 搜索区中启动它。



启动 OmegaT（Linux）
========================

如果您用了 linux-install.sh 脚本，你应当可以这样启动 OmegaT：

  Alt+F2

然后：

  omegat

要以对用户更友好的方式启动 OmegaT，您可以使用所提供的 Kaptain
脚本（omegat.kaptn）。要使用此脚本，必须先安装 Kaptain。
然后，可以这样启动 Kaptain 启动脚本：

  Alt+F2

然后：

  omegat.kaptn



从命令行启动 OmegaT（所有平台）
====================================================

启动 OmegaT 的命令是：

cd <OmegaT.jar 文件所在的文件夹>

<Java 可执行文件的路径和名称> -jar OmegaT.jar

（所谓 Java 可执行文件，在 Linux 下是 java 文件，在 Windows 下则是 java.exe 文件。
　如果 Java 是在系统级安装的且处于命令行路径中，则无需输入完整路径。）



贡献者
============

OmegaT 最初的工作由 Keith Godfrey 完成。

Aaron Madlon-Kay 是 OmegaT 的项目经理。

当前团队：
（按字母顺序）

  Vincent Bidaux（文档经理）
  Marco Cevoli（Telegram 社区经理）
  Jean-Christophe Helary（Twitter 社区经理）
  Kos Ivantsof（本地化经理）
  Concepción Martin（Facebook 社区经理）
  Briac Pilpré（网站管理员）
  Lucie Vecerova（Facebook 社区经理）

对代码的贡献记录在 /docs/contributors.txt 中。

以前的贡献者包括：
（按字母顺序）

  Anthony Baldwin（本地化经理）
  Didier Briel（项目经理）
  Alex Buloichik（首席开发人员）
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary（支持小组组长、本地化经理）
  Maxym Mykhalchuk（首席开发人员）
  Samuel Murray
  Henry Pijffers（发布经理）
  Marc Prior（项目调度员、网站管理员）
  Vito Smolej（文档经理）
  还有很多、很多很有帮助的人

（如果您认为您对 OmegaT 项目做出了重大贡献但名字没有出现在列表中，
　请随时与我们联系。）



有用的链接
============

关于 OmegaT 的最新信息，请访问：

   http://www.omegat.org/

在 Yahoo 用户组可以获得（多语种）用户支持，无需订阅也可以对归档进行搜索：

   https://groups.yahoo.com/neo/groups/OmegaT/info

在 SourceForge 网站可以（用英语）提出改进建议与意见：

   https://sourceforge.net/p/omegat/feature-requests/

在 SourceForge 网站可以（用英语）报告 Bug：

   https://sourceforge.net/p/omegat/bugs/
