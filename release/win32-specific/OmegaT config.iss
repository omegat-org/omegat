; -- OmegaT config.iss --
; Takes local configuration files, and copy them
; to {userappdata}\OmegaT. E.g. (XP)
; C:\Documents and Settings\<user_name>\Application Data\OmegaT
; Requires the preprocessor

#define ExeDateAndTime GetDateTimeString('dd/mm/yyyy hh:nn:ss', '-', '-');
#define DateAndTime GetDateTimeString('dd/mm/yyyy hh:nn:ss', '/', ':');

[Setup]
AppName=OmegaT configuration
AppVerName=OmegaT configuration version {#DateAndTime}
AppPublisher=OmegaT
AppPublisherURL=http://www.omegat.org/
DefaultDirName={userappdata}\OmegaT
AllowNoIcons=yes
Compression=lzma
SolidCompression=yes
OutputDir=.
OutputBaseFilename=OmegaT configuration_{#ExeDateAndTime}
Uninstallable=no

[Files]
Source: "segmentation.conf"; DestDir: "{app}";
Source: "filters.xml"; DestDir: "{app}";

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

