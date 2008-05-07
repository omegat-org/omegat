; -- OmegaT.iss --

[Setup]
AppName=OmegaT @VERSION_NUMBER_SUBST@
AppVerName=OmegaT version @VERSION_NUMBER_SUBST@
AppPublisher=OmegaT
AppPublisherURL=http://www.omegat.org/
DefaultDirName={pf}\OmegaT
DefaultGroupName=OmegaT
UninstallDisplayIcon={app}\OmegaT.exe
AllowNoIcons=yes
Compression=lzma
SolidCompression=yes
LicenseFile=license.txt
OutputDir=..\dist2
OutputBaseFilename=OmegaT_@VERSION_NUMBER_SUBST@_Windows

[Files]
Source: "docs\*"; DestDir: "{app}\docs"; Flags: recursesubdirs
Source: "images\*"; DestDir: "{app}\images"; Flags: recursesubdirs
Source: "lib\*"; DestDir: "{app}\lib"; Flags: recursesubdirs
Source: "native\*"; DestDir: "{app}\native"; Flags: recursesubdirs
Source: "..\..\jre\*"; DestDir: "{app}\jre"; Flags: recursesubdirs
Source: "OmegaT.exe"; DestDir: "{app}"
Source: "OmegaT.l4J.ini"; DestDir: "{app}"
Source: "OmegaT.jar"; DestDir: "{app}"
Source: "license.txt"; DestDir: "{app}"
Source: "doc-license.txt"; DestDir: "{app}"
Source: "readme.txt"; DestDir: "{app}"; Flags: isreadme;
Source: "readme_ca.txt"; DestDir: "{app}"; Flags: isreadme; Languages: ca ca_VAL
Source: "readme_cs.txt"; DestDir: "{app}"; Flags: isreadme; Languages: cs
Source: "readme_eu.txt"; DestDir: "{app}"; Flags: isreadme; Languages: eu
Source: "readme_fr.txt"; DestDir: "{app}"; Flags: isreadme; Languages: fr
Source: "readme_hu.txt"; DestDir: "{app}"; Flags: isreadme; Languages: hu
Source: "readme_it.txt"; DestDir: "{app}"; Flags: isreadme; Languages: it
Source: "readme_ja.txt"; DestDir: "{app}"; Flags: isreadme; Languages: ja
Source: "readme_nl.txt"; DestDir: "{app}"; Flags: isreadme; Languages: nl
Source: "readme_pl.txt"; DestDir: "{app}"; Flags: isreadme; Languages: pl
Source: "readme_pt_BR.txt"; DestDir: "{app}"; Flags: isreadme; Languages: pt_BR
Source: "readme_ru.txt"; DestDir: "{app}"; Flags: isreadme; Languages: ru
;Source: "readme_sh.txt"; DestDir: "{app}"; Flags: isreadme; Languages: sh
Source: "readme_sk.txt"; DestDir: "{app}"; Flags: isreadme; Languages: sk
Source: "readme_sq.txt"; DestDir: "{app}"; Flags: isreadme; Languages: sq
Source: "readme_tr.txt"; DestDir: "{app}"; Flags: isreadme; Languages: tr
Source: "readme_zh_CN.txt"; DestDir: "{app}"; Flags: isreadme; Languages: zh_CN
Source: "readme_zh_TW.txt"; DestDir: "{app}"; Flags: isreadme; Languages: zh_TW
Source: "readme*.txt"; DestDir: "{app}";
Source: "join.html"; DestDir: "{app}"
Source: "index.html"; DestDir: "{app}"
Source: "changes.txt"; DestDir: "{app}"

[UninstallDelete]
Type: filesandordirs; Name: "{app}\jre\launch4j-tmp"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: "quicklaunchicon"; Description: "{cm:CreateQuickLaunchIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Icons]
Name: "{group}\OmegaT @VERSION_NUMBER_SUBST@"; Filename: "{app}\OmegaT.exe"; WorkingDir: "{app}"
Name: "{commondesktop}\OmegaT"; Filename: "{app}\OmegaT.exe"; Tasks: desktopicon
Name: "{userappdata}\Microsoft\Internet Explorer\Quick Launch\OmegaT"; Filename: "{app}\OmegaT.exe"; Tasks: quicklaunchicon
Name: "{group}\OmegaT Readme"; Filename: "{app}\readme.txt"
Name: "{group}\OmegaT User Manual"; Filename: "{app}\docs\index.html"
Name: "{group}\Join OmegaT Mailing List"; Filename: "{app}\join.html"
Name: "{group}\Uninstall OmegaT"; Filename: "{uninstallexe}"

[Languages]
; Official translations
Name: "en"; MessagesFile: "compiler:Default.isl"
Name: "eu"; MessagesFile: "compiler:Languages\Basque.isl"
Name: "pt_BR"; MessagesFile: "compiler:Languages\BrazilianPortuguese.isl"
Name: "ca"; MessagesFile: "compiler:Languages\Catalan.isl"
Name: "cs"; MessagesFile: "compiler:Languages\Czech.isl"
Name: "da"; MessagesFile: "compiler:Languages\Danish.isl"
Name: "nl"; MessagesFile: "compiler:Languages\Dutch.isl"
Name: "fi"; MessagesFile: "compiler:Languages\Finnish.isl"
Name: "fr"; MessagesFile: "compiler:Languages\French.isl"
Name: "de"; MessagesFile: "compiler:Languages\German.isl"
Name: "he"; MessagesFile: "compiler:Languages\Hebrew.isl"
Name: "hu"; MessagesFile: "compiler:Languages\Hungarian.isl"
Name: "it"; MessagesFile: "compiler:Languages\Italian.isl"
Name: "nb"; MessagesFile: "compiler:Languages\Norwegian.isl"
Name: "pl"; MessagesFile: "compiler:Languages\Polish.isl"
Name: "pt"; MessagesFile: "compiler:Languages\Portuguese.isl"
Name: "ru"; MessagesFile: "compiler:Languages\Russian.isl"
Name: "sk"; MessagesFile: "compiler:Languages\Slovak.isl"
Name: "sl"; MessagesFile: "compiler:Languages\Slovenian.isl"
Name: "es"; MessagesFile: "compiler:Languages\Spanish.isl"
; Non-official translations
Name: "af"; MessagesFile: "compiler:Languages\Afrikaans.isl"
Name: "sq"; MessagesFile: "compiler:Languages\Albanian.isl"
Name: "ar"; MessagesFile: "compiler:Languages\Arabic.isl"
Name: "ast"; MessagesFile: "compiler:Languages\Asturian.isl"
Name: "be"; MessagesFile: "compiler:Languages\Belarus.isl"
Name: "bs"; MessagesFile: "compiler:Languages\Bosnian.isl"
Name: "bg"; MessagesFile: "compiler:Languages\Bulgarian.isl"
Name: "zh_CN"; MessagesFile: "compiler:Languages\ChineseSimp.isl"
Name: "zh_TW"; MessagesFile: "compiler:Languages\ChineseTrad.isl"
Name: "hr"; MessagesFile: "compiler:Languages\Croatian.isl"
Name: "eo"; MessagesFile: "compiler:Languages\Esperanto.isl"
Name: "et"; MessagesFile: "compiler:Languages\Estonian.isl"
Name: "fa"; MessagesFile: "compiler:Languages\Farsi.isl"
Name: "gl"; MessagesFile: "compiler:Languages\Galician.isl"
Name: "el"; MessagesFile: "compiler:Languages\Greek.isl"
Name: "is"; MessagesFile: "compiler:Languages\Icelandic.isl"
Name: "id"; MessagesFile: "compiler:Languages\Indonesian.isl"
Name: "ja"; MessagesFile: "compiler:Languages\Japanese.isl"
Name: "kk"; MessagesFile: "compiler:Languages\Kazakh.isl"
Name: "kk"; MessagesFile: "compiler:Languages\Kazakh.isl"
Name: "ko"; MessagesFile: "compiler:Languages\Korean.isl"
Name: "lv"; MessagesFile: "compiler:Languages\Latvian.isl"
Name: "lt"; MessagesFile: "compiler:Languages\Lithuanian.isl"
Name: "lb"; MessagesFile: "compiler:Languages\Luxemburgish.isl"
Name: "mk"; MessagesFile: "compiler:Languages\Macedonian.isl"
Name: "ms"; MessagesFile: "compiler:Languages\Malaysian.isl"
Name: "nn"; MessagesFile: "compiler:Languages\NorwegianNynorsk.isl"
Name: "oc"; MessagesFile: "compiler:Languages\Occitan.isl"
Name: "ro"; MessagesFile: "compiler:Languages\Romanian.isl"
Name: "sr"; MessagesFile: "compiler:Languages\Serbian.isl"
Name: "sv"; MessagesFile: "compiler:Languages\Swedish.isl"
Name: "tt"; MessagesFile: "compiler:Languages\Tatarish.isl"
Name: "th"; MessagesFile: "compiler:Languages\Thai.isl"
Name: "tr"; MessagesFile: "compiler:Languages\Turkish.isl"
Name: "uk"; MessagesFile: "compiler:Languages\Ukrainian.isl"
Name: "ca_VAL"; MessagesFile: "compiler:Languages\Valencian.isl"





