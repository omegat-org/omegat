; -- OmegaT.iss --

[Setup]
AppName=OmegaT
AppVerName=OmegaT version @VERSION_NUMBER_SUBST@
DefaultDirName={pf}\OmegaT
DefaultGroupName=OmegaT
UninstallDisplayIcon={app}\OmegaT.exe
Compression=lzma
SolidCompression=yes
LicenseFile=license.txt
OutputDir=..\dist
OutputBaseFilename=OmegaT_@VERSION_NUMBER_SUBST@_setup

[Files]
Source: "docs\*"; DestDir: "{app}\docs"; Flags: recursesubdirs
Source: "images\*"; DestDir: "{app}\images"; Flags: recursesubdirs
Source: "source\*"; DestDir: "{app}\source"; Flags: recursesubdirs
Source: "OmegaT.bat"; DestDir: "{app}"
Source: "OmegaT.exe"; DestDir: "{app}"
Source: "OmegaT.jar"; DestDir: "{app}"
Source: "license.txt"; DestDir: "{app}"
Source: "readme.txt"; DestDir: "{app}"; Flags: isreadme
Source: "release_notes.txt"; DestDir: "{app}"

[Icons]
Name: "{group}\OmegaT @VERSION_NUMBER_SUBST@"; Filename: "{app}\OmegaT.exe"; WorkingDir: "{app}"
Name: "{group}\OmegaT Readme"; Filename: "{app}\readme.txt"
Name: "{group}\OmegaT User Manual"; Filename: "{app}\docs\index.html"
Name: "{group}\Uninstall OmegaT"; Filename: "{uninstallexe}"

