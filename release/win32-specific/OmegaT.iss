; -- OmegaT.iss --

[Setup]
AppName=OmegaT @VERSION_NUMBER_SUBST@
AppVerName=OmegaT version @VERSION_NUMBER_SUBST@
DefaultDirName={pf}\OmegaT
DefaultGroupName=OmegaT
UninstallDisplayIcon={app}\OmegaT.exe
Compression=lzma
SolidCompression=yes
LicenseFile=license.txt
OutputDir=..\dist2
OutputBaseFilename=OmegaT_win_@VERSION_NUMBER_SUBST@

[Files]
Source: "docs\*"; DestDir: "{app}\docs"; Flags: recursesubdirs
Source: "images\*"; DestDir: "{app}\images"; Flags: recursesubdirs
Source: "lib\*"; DestDir: "{app}\lib"; Flags: recursesubdirs
Source: "OmegaT.bat"; DestDir: "{app}"
Source: "OmegaT.exe"; DestDir: "{app}"
Source: "OmegaT.jar"; DestDir: "{app}"
Source: "lib-mnemonics.jar"; DestDir: "{app}"
Source: "license.txt"; DestDir: "{app}"
Source: "doc-license.txt"; DestDir: "{app}"
Source: "readme.txt"; DestDir: "{app}"; Flags: isreadme
Source: "join.html"; DestDir: "{app}"
Source: "index.html"; DestDir: "{app}"
Source: "changes.txt"; DestDir: "{app}"

[Icons]
Name: "{group}\OmegaT @VERSION_NUMBER_SUBST@"; Filename: "{app}\OmegaT.exe"; WorkingDir: "{app}"
Name: "{group}\OmegaT Readme"; Filename: "{app}\readme.txt"
Name: "{group}\OmegaT User Manual"; Filename: "{app}\docs\index.html"
Name: "{group}\Join OmegaT Mailing List"; Filename: "{app}\join.html"
Name: "{group}\Uninstall OmegaT"; Filename: "{uninstallexe}"

