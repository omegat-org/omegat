; -- OmegaT.iss --

[Setup]
AppName=OmegaT
AppVerName=OmegaT version 1.4.2
DefaultDirName={pf}\OmegaT
DefaultGroupName=OmegaT
UninstallDisplayIcon={app}\MyProg.exe
Compression=lzma
SolidCompression=yes
LicenseFile=license.txt
OutputDir=..\dist
OutputBaseFilename=OmegaT_1.4.2_setup

[Files]
Source: "docs\*"; DestDir: "{app}\docs"; Flags: recursesubdirs
Source: "images\*"; DestDir: "{app}\images"; Flags: recursesubdirs
Source: "source\*"; DestDir: "{app}\source"; Flags: recursesubdirs
Source: "OmegaT.bat"; DestDir: "{app}"
Source: "OmegaT.exe"; DestDir: "{app}"
Source: "license.txt"; DestDir: "{app}"
Source: "readme.txt"; DestDir: "{app}"; Flags: isreadme
Source: "release_notes.txt"; DestDir: "{app}"

[Icons]
Name: "{group}\OmegaT 1.4.2"; Filename: "{app}\OmegaT.exe"
Name: "{group}\OmegaT Readme"; Filename: "{app}\readme.txt"
Name: "{group}\OmegaT User Manual"; Filename: "{app}\docs\index.html"
Name: "{group}\Uninstall OmegaT"; Filename: "{uninstallexe}"

