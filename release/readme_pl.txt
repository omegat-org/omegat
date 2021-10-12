Tłumaczenie programu autorstwa Oskara Nowaka, prawa autorskie zastrzeżone © 2016

==============================================================================
  OmegaT 4.0, plik Readme

  1.  Informacje o programie OmegaT
  2.  Czym jest OmegaT?
  3.  Instalacja programu OmegaT
  4.  Udział przy tworzeniu programu OmegaT
  5.  Znalazłeś błędy w programie OmegaT? Potrzebujesz pomocy?
  6.  Informacje o wydaniu

==============================================================================
  1.  Informacje o programie OmegaT


Najświeższe informacje o programie OmegaT znajdziesz na:
      http://www.omegat.org/

Wsparcie użytkowników znajdziesz na (wielojęzycznej) grupie Yahoo, gdzie znajdują się archiwa, które możesz przeglądać bez subskrybowania do grupy:
     https://omegat.org/support

Prośby o usprawnienia programu (w języku angielskim) znajdują się w domenie SourceForge:
     https://sourceforge.net/p/omegat/feature-requests/

Raportowanie błędów (w języku angielskim) znajduje się w domenie SourceForge:
     https://sourceforge.net/p/omegat/bugs/

==============================================================================
  2.  Czym jest OmegaT?

OmegaT to narzędzie wspomagające komputerowo tłumaczenie (CAT). Jest oprogramowaniem wolnym, co oznacza, że nie musisz płacić, aby móc go używać, nawet w celach zarobkowych, 
i możesz je modyfikować lub/i rozprowadzać, pod warunkiem że przestrzegasz licencji użytkowania.

Główne cechy programu OmegaT:
  - możliwość pracy na każdym systemie wspierającym Javę
  - użycie każdego poprawnego pliku TMX jako pomocy tłumaczeniowej
  - elastyczne segmentowanie zdań (używające metody podobnej do SRX)
  - wyszukiwanie tekstu w projekcie i plikach pamięci tłumaczeniowej
  - wyszukiwanie plików we wspieranych formatach w dowolnych folderach 
  - dopasowania rozmyte
  - sprawne zarządzanie projektami (w tym obsługa kompleksowych hierarchii folderów) 
  - obsługa glosariuszy (sprawdzanie terminologii) 
  - wsparcie dla open source'owego sprawdzania pisowni „w locie”
  - obsługa słowników StarDict
  - obsługa tłumaczenia maszynowego Google Translate
  - czytelna i obszerna dokumentacja oraz samouczki
  - interfejs dostępny w wielu językach.

OmegaT standardowo wspiera następujące formaty plików:

- pliki tekstowe niesformatowane (znakowe)

  - pliki tekstowe ASCII (.txt, itp.)
  - pliki tekstowe zakodowane (*.UTF8)
  - paczki zasobów Java (*.properties)
  - pliki PO (*.po)
  - pliki INI (klucz=wartość) (*.ini)
  - pliki DTD (*.DTD)
  - pliki DocuWiki (*.txt)
  - pliki SubRip (*.srt)
  - pliki Magento CE Locale CSV (*.csv)

- pliki tekstowe ze znacznikami

  - pliki OpenOffice.org / OpenDocument (*.odt, *.ott, *.ods, *.ots, *.odp, *.otp)
  - pliki Microsoft Open XML (*.docx, *.xlsx, *.pptx)
  - pliki (X)HTML (*.html, *.xhtml,*.xht)
  - pliki HTML Help Compiler (*.hhc, *.hhk)
  - pliki DocBook (*.xml)
  - jednojęzyczne pliki XLIFF (*.xlf, *.xliff, *.sdlxliff)
  - plikiQuarkXPress CopyFlowGold (*.tag, *.xtg)
  - pliki ResX (*.resx)
  - pliki zasobów Android (*.xml)
  - pliki LaTex (*.tex, *.latex)
  - pliki Pomocy (*.xml) i Podręcznika (*.hmxp)
  - pliki Typo3 LocManager (*.xml)
  - pliki WiX Localization (*.wxl)
  - pliki Iceni Infix (*.xml)
  - pliki eksportowe Flash XML (*.xml)
  - pliki Wordfast TXML (*.txml)
  - pliki Camtasia [Windows] (*.camproj)
  - pliki Visio (*.vxd)

Funkcjonalność programu OmegaT może zostać rozszerzona do wspierania innych rodzajów plików.

OmegaT automatycznie przeanalizuje nawet najbardziej złożoną strukturę katalogu źródłowego, aby uzyskać dostęp do wszystkich plików w obsługiwanych formatach, i stworzy katalog docelowy o dokładnie takiej samej strukturze, zawierający kopie wszystkich plików nieobsługiwanych.

Aby przejść przez szybki start, uruchom program OmegaT i przeczytaj wyświetlony tekst.

Podręcznik użytkownika znajduje się w pobranej paczce. Może do niego wejść poprzez menu [Pomoc] z paska programu OmegaT.

==============================================================================
 3. Instalacja programu OmegaT

3.1 Informacje ogólne
Do uruchomienia program OmegaT wymaga środowiska Java Runtime Environment (JRE) w wersji 1.6 lub wyższej, zainstalowanego w systemie. Dostępne są także wersje programu OmegaT, które
zawierają środowisko Java Runtime Environment, aby ułatwić
użytkownikom instalację i uruchomienie programu. 

Jeżeli posiadasz już środowisko Java, możesz zainstalować najnowszą wersję programu OmegaT używając Java Web Start. 
Aby tego dokonać, pobierz plik z poniższego linku i otwórz go:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Ten krok zainstaluje na komputerze poprawne środowisko oraz wersję aplikacji przy jej pierwszym uruchomieniu. Kolejne uruchomienia nie wymagają połączenia z internetem.

W trakcie instalacji, zależnie od systemu operacyjnego, możesz otrzymać kilka alertów o zabezpieczeniach. Certyfikat wydany jest przez "PnS Concept". 
Zezwolenia, których udzielasz tej wersji (mogą być oznaczone jako "zabroniony dostęp do komputera"), są identyczne jak zezwolenia, których udzielasz lokalnej wersji, zainstalowanej zgodnie z procedurą opisaną dalej: pozwalają na dostęp do twardego dysku komputera. Kolejne kliknięcia na OmegaT.jnlp spowodują - jeżeli jesteś podłączony do internetu - sprawdzenie, czy są nowsze wersje, zainstalowanie ich, jeśli istnieją, a następnie uruchomienie OmegaT. 

Alternatywne sposoby pobierania i instalacji programu OmegaT opisane są
poniżej. 

Użytkownicy Windows i Linux: jeżeli jesteś pewien, że w twoim systemie już jest zainstalowana odpowiednia wersja JRE, możesz zainstalować wersję OmegaT bez JRE (jest to zaznaczone w nazwie wersji, "Without_JRE"). 
Jeżeli masz wątpliwości, zalecane jest użycie "standardowej" wersji, tzn. z JRE. Jest to bezpieczne, bo nawet jeśli JRE jest już zainstalowane w twoim systemie, ta wersja nie będzie z nim kolidować.

Użytkownicy Linux: 
OmegaT będzie działać na open-source'owych implementacjach środowiska Java, które są dołączane do wielu dystrybucji Linuxa (np. Ubuntu), ale
mogą pojawić się problemy z wyświetlaniem oraz różne inne błędy. Zalecamy pobranie oraz instalację albo Oracle Java Runtime Environment (JRE) 
albo wersji OmegaT rozpowszechnianej razem z JRE (paczka z rozszerzeniem tar.gz podpisana jako "Linux"). Jeżeli instalujesz oprogramowanie Java z poziomu systemu, upewnij się, że ścieżka potrzebna do uruchomienia jest poprawna lub program został poprawnie nazwany. Jeżeli nie jesteś jesteś zaznajomiony z Linuxem, zalecamy instalację wersji programu OmegaT, która zawiera JRE. Jest to bezpieczne,
ponieważ "lokalne" JRE nie zaburza pracy żadnej innej wersji JRE, która zainstalowana jest w systemie.

Użytkownicy Mac: 
JRE jest domyślnie zainstalowane na Mac OS X 10.7 
[Lion] i wcześniejszych. Użytkownicy wersji Lion, przy pierwszym uruchomieniu, zostaną powiadomieni o konieczności posiadania środowiska Java do uruchomienia programu, po czym zostanie ono automatycznie pobrane i zainstalowane.

Systemy Linux on PowerPC: użytkownicy będą musieli pobrać IBM's JRE, ponieważ Sun nie dostarcza JRE dla systemów PPC. W przypadku należy je pobrać z:

    http://www.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Instalacja
* użytkownicy Windows: 
Wystarczy jedynie uruchomić instalator. Instalator może, na życzenie,
utworzyć skrót do uruchamiania programu OmegaT.

* użytkownicy Linuxa:
Umieść archiwum w żądanym folderze i rozpakuj; program będzie
wtedy
gotowy do uruchomienia. Możesz też użyć przyjemniejszego w obsłudze skryptu instalacyjnego (linux-install.sh). Aby użyć tego skryptu, otwórz okno terminala (konsoli), zmień folder na ten zawierający OmegaT.jar oraz skrypt linux-install.sh, a następnie wykonaj skrypt przy użyciu "./linux-install.sh.".

* użytkownicy Mac:
Skopiuj archiwum OmegaT.zip do miejsca docelowego, aby uzyskać folder zawierający plik dokumentacji w HTML oraz aplikację OmegaT.app.

* inni (np. Solaris, FreeBSD): 
Aby zainstalować OmegaT, po prostu stwórz odpowiedni folder dla OmegaT. Skopiuj archiwum OmegaT (.zip lub .tar.gz.)
do tego folderu i rozpakuj je tam.

3.3 Uruchamianie OmegaT
OmegaT może zostać uruchomiona na wiele sposobów.

* użytkownicy Windows: 
Jeśli, w trakcie instalacji, utworzono skrót na pulpicie, kliknij dwukrotnie na ten skrót. Alternatywnie, kliknij dwukrotnie na plik
OmegaT.exe. Jeśli widzisz plik OmegaT, ale nie widzisz OmegaT.exe w twoim Menedżerze plików (Eksploratorze Windows), 
zmień ustawienia tak, aby rozszerzenia plików były wyświetlane.

* użytkownicy Linux:
Jeśli użyto skryptu, będącego częścią paczki, można uruchomić program OmegaT za pomocą kombinacji: Alt+F2, a następnie wpisując: omegat

* użytkownicy Mac:
Kliknij dwukrotnie na plik OmegaT.app.

* przy użyciu menedżera plików (wszystkie systemy):
Kliknij dwukrotnie na plik OmegaT.jar. Będzie to działać, pod warunkiem, że w twoim systemie typ pliku .jar jest skojarzony ze środowiskiem Java.

* przy użyciu linii poleceń (wszystkie systemy): 
Polecenie uruchamiające OmegaT:

cd <folder, w którym znajduje się plik OmegaT.jar>

<nazwa i ścieżka dostępu do pliku wykonywalnego Java> -jar OmegaT.jar

(Plik wykonywalny na Linuxie to java, a pod Windowsem to java.exe.
Jeżeli Java jest zainstalowana w systemie, nie trzeba podawać pełnej ścieżki.)

Ustawienia uruchamiania programu OmegaT:

* użytkownicy Windows: 
Program instalacyjny może utworzyć skróty w menu Start, na pulpicie i w menu szybkiego uruchamiania. Możesz także ręcznie przeciągąć plik OmegaT.exe do menu Start, na pulpit lub do menu szybkiego uruchamiania, aby utworzyć tam skrót.

* użtkownicy Linuxa:
Dla łatwiejszego uruchamiania programu OmegaT można użyć załączonego skryptu Kaptain (omegat.kaptn). Aby wykonać ten skrypt należy wcześniej zainstalować oprogramowanie Kaptain. Następnie można uruchomić skrypt Kaptain za pomocą kombinacji
Alt+F2
omegat.kaptn

Po więcej informacji, na temat skryptu Kaptain oraz dodawania pozycji do menu i ikon uruchamiających, wyszukaj na Linux HowTo wyrażenie "OmegaT".

użytkownicy Mac:
Przeciągnij OmegaT.app na pasek dokowania lub narzędzi okna Wyszukiwani, aby móc uruchomić aplikację z każdego miejsca. Możesz także przywołać aplikację w polu wyszukiwania Spotlight.

==============================================================================
 4. Udział w projekcie OmegaT

Aby wziąć udział w rozwoju OmegaT, skontaktuj się z jego twórcami na:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Aby wesprzeć projekt jako programista, należy najpierw przeczytać dokumentację:
    https://sourceforge.net/p/omegat/svn/HEAD/tree/trunk/docs_devel/

Aby wziąć udział w tłumaczeniu interfejsu OmegaT, podręcznika użytkownika lub innych związanych z nimi dokumentów,
przeczytaj:
      
      http://www.omegat.org/en/howtos/localizing_omegat.php

Zapisz się na listę tłumaczy pod adresem:
      https://lists.sourceforge.net/lists/listinfo/omegat-l10n

Jeśli chcesz pomóc w inny sposób, zacznij od zapisania się do grupy użytkowników pod adresem:
      http://tech.groups.yahoo.com/group/omegat/

I poczuj, co się dzieje w świecie OmegaT...

  OmegaT jest oryginalnym dziełem Keitha Godfrey'a.
  Didier Briel jest koordynatorem projektu OmegaT

Współtwórcy:
(w porządku alfabetycznym)

Udział w tworzeniu kodu mieli:
  Zoltan Bartko
  Volker Berlin
  Didier Briel
  Kim Bruning
  Alex Buloichik (główny developer)
  Sandra Jean Chua
  Thomas Cordonnier
  Enrique Estévez Fernández
  Martin Fleurke  
  Wildrich Fourie
  Tony Graham
  Phillip Hall
  Jean-Christophe Helary
  Chihiro Hio
  Thomas Huriaux
  Hans-Peter Jacobs
  Kyle Katarn
  Piotr Kulik
  Ibai Lakunza Velasco
  Guido Leenders
  Aaron Madlon-Kay (menadżer integracji wydań)
  Fabián Mandelbaum
  Manfred Martin
  Adiel Mittmann
  Hiroshi Miura 
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
  Ilia Vinogradov
  Martin Wunderlich
  Michael Zakharov

Wkład w inne części mieli:
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (koordynator tłumaczeń)
  Vincent Bidaux (koordynator dokumentacji)
  Samuel Murray
  Marc Prior (webmaster)
  oraz bardzo wielu pomocnych osób

(Jeżeli uważasz, że i ty wziąłeś znaczący udział w projekcie OmegaT, a nie widzisz tutaj swojego nazwiska, skontaktuj się z nami.)

OmegaT korzysta z następujących bibliotek:
  HTMLParser 1.6 by Somik Raha, Derrick Oswald and others (LGPL License)
  VLDocking Framework 3.0.5-SNAPSHOT (LGPL License)
  Hunspell by László Németh and others (LGPL License)
  JNA by Todd Fast, Timothy Wall and others (LGPL License)
  Swing-Layout 1.0.4 (Licencja LGPL)
  Jmyspell 2.1.4 (Licencja LGPL)
  SVNKit 1.8.5 (Licencja TMate)
  Sequence Library (Licencja Sequence Library)
  ANTLR 3.4 (Licencja ANTLR 3)
  SQLJet 1.1.10 (Licencja GPL v2)
  JGit (Licencja Eclipse Distribution)
  JSch (Licencja JSch)
  Base64 (domena publiczna)
  Diff (Licencja GPL)
  trilead-ssh2-1.0.0-build217 (Licencja Trilead SSH)
  lucene-*.jar (Licencja Apache 2.0)
  The English tokenizers (org.omegat.tokenizer.SnowballEnglishTokenizer and
org.omegat.tokenizer.LuceneEnglishTokenizer) use stop words originally from
Okapi (http://okapi.sourceforge.net) (LGPL license)
  tinysegmenter.jar (Modified BSD license)
  commons-*.jar (Apache License 2.0)
  jWordSplitter (Apache License 2.0)
  LanguageTool.jar (LGPL license)
  morfologik-*.jar (Morfologik license)
  segment-1.4.1.jar (Segment license)
  pdfbox-app-1.8.1.jar (Apache License 2.0)
  KoreanAnalyzer-3x-120223.jar (Apache License 2.0)
  SuperTMXMerge-for_OmegaT.jar (LGPL license)
  groovy-all-2.2.2.jar (Apache Licence 2.0)
  slf4j (MIT License)
  juniversalchardet-1.0.3.jar (GPL v2)
  DictZip from JDictd (GPL v2)

==============================================================================
 5.  Znalazłeś błędy w programie OmegaT? Potrzebujesz pomocy?

Przed zgłoszeniem błędu, upewnij się, że dokładnie przeczytałeś dokumentację. To, z czym się zetknąłeś, może nie być błędem, lecz cechą OmegaT, o której nie wiedziałeś wcześniej. Jeżeli sprawdzając logi OmegaT widzisz komunikaty takie jak
"Error", "Warning", "Exception" albo "died unexpectedly", to prawdopodobnie odkryłeś autentyczny problem (plik log.txt znajduje się w folderze ustawień użytkownika, sprawdź w podręczniku, gdzie ten folder się znajduje).

Kolejną rzeczą, którą należy zrobić, to potwierdzenie przez innych użytkowników tego co obserwujesz, żeby upewnić się, że nie zostało to już wcześniej zgłoszone.  Możesz też sprawdzić to na stronie zgłaszania błędów na SourceForge. Powinieneś wysłać zgłoszenie błędu tylko kiedy jesteś pewien, że to ty jako pierwszy zauważyłeś pewną powtarzalną sekwencję zdarzeń, która wywołała niewłaściwe zachowanie programu. 

Prawidłowe zgłoszenie błędu musi zawierać dokładnie trzy rzeczy:
  - kroki prowadzące do wystąpienia błędu,
  - to czego oczekiwałeś,
  - to co otrzymałeś

Możesz także dołączyć kopie plików, fragmenty pliku log, zrzuty ekranu, wszystko, co według ciebie pomoże twórcom programu znaleźć i naprawić błąd..

Zasoby grupy dyskusyjnej użytkowników można przeglądać pod adresem:
     http://tech.groups.yahoo.com/group/OmegaT/

Przeglądać zgłoszone błędy i wysłać nowe zgłoszenia można pod adresem:
     https://sourceforge.net/p/omegat/bugs/

Aby śledzić, co dzieje się z twoim zgłoszeniem błedu, możesz zarejestrować się jako użytkownik SourceForge.

==============================================================================
6.   Informacje o wydaniu

W pliku 'changes.txt' znajdziesz szczegółowe informacje o zmianach w tej i wszystkich poprzednich wersjach.


==============================================================================
