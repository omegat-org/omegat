Autorzy tego tłumaczenia: [Anna Augustynowicz], copyright© [2010].

==============================================================================
  Plik Read Me, OmegaT 2.1

  1.  Informacja o programie OmegaT
  2.  Czym jest OmegaT?
  3.  Instalacja programu OmegaT
  4.  Udział w OmegaT
  5.  Czy OmegaT działa nieprawidłowo? Potrzebujesz pomocy?
  6.  Informacje dotyczące wersji

==============================================================================
  1.  Informacja o programie OmegaT


Najnowsze informacje na temat OmegaT można znaleźć na stronie
      http://www.omegat.org/

Pomoc dla użytkowników - grupa użytkowników na Yahoo (wielojęzyczna), gdzie archiwa można przeglądać bez zapisywania się:
     http://groups.yahoo.com/group/OmegaT/

Prośby o ulepszenia (po angielsku), na stronie SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Zgłaszanie błędów (po angielsku), na stronie SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Czym jest OmegaT?

OmegaT jest narzędziem do tłumaczenia wspomaganego komputerowo (CAT - computer-assisted translation). Jest oprogramowaniem wolnym, co oznacza, że nie musisz płacić, aby móc go używać, nawet w celach zarobkowych, 
i możesz je modyfikować lub/i rozprowadzać, pod warunkiem że przestrzegasz licencji.

Główne cechy OmegaT:
  - praca w dowolnym systemie operacyjnym, który obsługuje Javę
  - użycie dowolnych plików zgodnych z TMX jako pomocniczego tłumaczenia
  - elastyczna segmentacja zdań (wykorzystująca metodę podobną do SRX)
  - wyszukiwanie w projekcie i pamięciach tłumaczeniowych
  - szukanie plików w obsługiwanych formatach, w dowolnym katalogu 
  - dopasowania rozmyte
  - sprawna obsługa projektów zawierających złożoną strukturę katalogów
  - obsługa glosariuszy (sprawdzanie terminologii) 
  - obsługa open source-owych modułów sprawdzania pisowni na bieżąco
  - obsługa słowników StarDict
  - obsługa tłumaczenia maszynowego Google Translate
  - zrozumiała i obszerna dokumentacja i podręcznik użytkownika
  - lokalizacja w wielu językach.

OmegaT obsługuje bezpośrednio następujące formaty plików:
  - tekst niesformatowany (znakowy)
  - HTML i XHTML
  - HTML Help Compiler
  - OpenDocument/OpenOffice.org
  - Java resource bundles (.properties)
  - pliki INI (pliki z parami klucz=wartość w dowolnym kodowaniu)
  - pliki PO
  - format dokumentacji DocBook
  - pliki Microsoft OpenXML
  - pliki Okapi monolingual XLIFF
  - QuarkXPress CopyFlowGold
  - pliki napisów (SRT)
  - ResX
  - Android resource
  - LaTeX
  - Pliki pomocy i podręczników
  - Windows RC resources

Program OmegaT można dostosować do obsługi również innych formatów plików.

OmegaT automatycznie przeanalizuje nawet najbardziej złożoną strukturę katalogu źródłowego, aby uzyskać dostęp do wszystkich plików w obsługiwanych formatach, i stworzy katalog docelowy o dokładnie takiej samej strukturze, zawierający kopie wszystkich plików nieobsługiwanych.

Aby szybko zapoznać się z podstawami programu, uruchom OmegaT i przeczytaj wyświetlony podręcznik "OmegaT w pigułce".

Podręcznik użytkownika znajduje się w pakiecie, który pobrałeś; znajdziesz go w menu Pomoc po uruchomieniu OmegaT.

==============================================================================
 3. Instalacja programu OmegaT

3.1 Informacje ogólne
OmegaT wymaga środowiska Java Runtime Environment (JRE), w wersji
1.5 lub wyższej, zainstalowanego w twoim systemie. OmegaT jest obecnie standardowo dostarczana z Java Runtime Environment, aby zaoszczędzić użytkownikom kłopotu związanego z 
wybieraniem, uzyskiwaniem i instalowaniem go. 

Jeżeli już masz środowisko Java, najprostszy sposób zainstalowania aktualnej wersji 
OmegaT to użycie Java Web Start. 
W tym celu pobierz, a następnie wykonaj następujący plik:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Przy pierwszym wywołaniu zainstaluje on środowisko właściwe dla twojego komputera oraz samą aplikację. Następne wywołania nie wymagają podłączenia do internetu.

Podczas instalacji możesz otrzymać, zależnie od twojego systemu operacyjnego, kilka ostrzeżeń. Ten certyfikat jest podpisany przez "Didier Briel". 
Zezwolenia, których udzielasz tej wersji (które mogą być oznaczone jako "zabroniony dostęp do komputera"), są identyczne jak zezwolenia, których udzielasz lokalnej wersji, zainstalowanej zgodnie z procedurą opisaną dalej: pozwalają na dostęp do twardego dysku komputera. Kolejne kliknięcia na OmegaT.jnlp spowodują - jeżeli jesteś podłączony do internetu - sprawdzenie, czy są nowsze wersje, zainstalowanie ich, jeśli istnieją, a następnie uruchomienie OmegaT. 

Inne sposoby pobierania i instalowania OmegaT są opisane poniżej. 

Użytkownicy Windows i Linux: jeżeli jesteś pewien, że w twoim systemie już jest zainstalowana odpowiednia wersja JRE, możesz zainstalować wersję OmegaT bez JRE (jest to zaznaczone w nazwie wersji, "Without_JRE"). 
Jeżeli masz wątpliwości, zalecane jest użycie "standardowej" wersji, tzn. z JRE. Jest to bezpieczne, bo nawet jeśli JRE jest już zainstalowane w twoim systemie, ta wersja nie będzie z nim kolidować.

Użytkownicy Linux: zauważ, że OmegaT nie działa z implementacjami Java free/open-source, które są dołączane do wielu dystrybucji Linuxa (na przykład 
Ubuntu), ponieważ są one albo nieaktualne, albo niekompletne. Pobierz i zainstaluj Sun's Java Runtime Environment (JRE) za pośrednictwem powyższego linku albo pobierz i zainstaluj pakiet OmegaT zawierający JRE ( pakiet .tar.gz oznaczony "Linux").

Użytkownicy Mac: JRE jest zainstalowane w systemie Mac OS X.

Systemy Linux on PowerPC: użytkownicy będą musieli pobrać IBM's JRE, ponieważ Sun nie dostarcza JRE dla systemów PPC. W tym przypadku pobierz z:

    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Instalacja
* Użytkownicy Windows: Po prostu uruchom program instalacyjny. Jeśli chcesz, program instalacyjny może utworzyć skróty do uruchamiania OmegaT.
* Inni: Aby zainstalować OmegaT, po prostu stwórz odpowiedni katalog dla OmegaT 
(np., /usr/local/lib w Linux). Skopiuj archiwum OmegaT zip lub tar.gz
do tego katalogu i rozpakuj je tam.

3.3 Uruchamianie OmegaT
OmegaT może zostać uruchomiona na wiele sposobów.

 * Użytkownicy Windows: przez dwukrotne kliknięcie na pliku OmegaT.exe. Jeśli widzisz plik OmegaT, ale nie widzisz OmegaT.exe w twoim Menedżerze plików (Eksploratorze Windows), 
zmień ustawienia tak, aby rozszerzenia plików były wyświetlane.

* Przez dwukrotne kliknięcie na pliku OmegaT.jar. Będzie to działać, pod warunkiem że w twoim systemie typ pliku .jar jest skojarzony z Java.

* Z linii poleceń. Polecenie uruchamiające OmegaT:

cd <katalog, w którym znajduje się plik OmegaT.jar>

<nazwa i ścieżka dostępu do pliku wykonywalnego Java> -jar OmegaT.jar

(Plik wykonywalny Java to plik java w Linuxie i java.exe w Windows.
Jeżeli Java jest zainstalowana w systemie, nie trzeba podawać pełnej ścieżki.)

* Użytkownicy Windows: Program instalacyjny może utworzyć skróty w menu Start, na pulpicie i w menu szybkiego uruchamiania. Możesz także ręcznie przeciągąć plik OmegaT.exe do menu Start, na pulpit lub do menu szybkiego uruchamiania, aby tam utworzyć skrót.

* Użytkownicy Linux KDE: możesz dodać OmegaT do swoich menu w następujący sposób:

Centrum sterowania - Pulpit - Panele - Menu - Edycja Menu KDE - Plik - Nowa pozycja/Nowe 
podmenu.

Następnie, po wybraniu odpowiedniego menu, dodaj podmenu/pozycję za pomocą Plik - Nowe podmenu i Plik - Nowa pozycja. Wprowadź OmegaT jako nazwę nowej pozycji.

W polu "Polecenie", użyj klawisza nawigacji, aby znaleźć skrypt uruchomieniowy OmegaT, i wybierz go. 

Naciśnij klawisz z ikoną (po prawej stronie pól Nazwa/Opis/Komentarz) 
- Inne ikony - Przeglądaj i przejdź do podkatalogu /images w katalogu aplikacji OmegaT. Wybierz ikonę OmegaT.png.

Na koniec, zachowaj zmiany za pomocą Plik - Zapisz.

* Użytkownicy Linux GNOME: możesz dodać OmegaT do swojego panelu (pasek w górze ekranu) w następujący sposób:

Kliknij prawym klawiszem na panelu Dodaj do panelu / Własny aktywator aplikacji. Wpisz "OmegaT" w polu "Nazwa"; w polu "Polecenie" użyj klawisza "Przeglądaj", aby znaleźć skrypt uruchomieniowy OmegaT. Wybierz i potwierdź za pomocą OK.

==============================================================================
 4. Udział w projekcie OmegaT

Aby wziąć udział w rozwoju OmegaT, skontaktuj się z jego twórcami na:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Aby wziąć udział w tłumaczeniu interfejsu użytkownika OmegaT, podręcznika użytkownika lub innych związanych z nimi dokumentów,
przeczytaj:
      
      http://www.omegat.org/en/translation-info.html

I zapisz się na listę tłumaczy pod adresem:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Jeśli chcesz nam pomóc w inny sposób, zacznij od zapisania się do grupy użytkowników pod adresem:
      http://tech.groups.yahoo.com/group/omegat/

I poczuj, co się dzieje w świecie OmegaT...

  OmegaT jest oryginalnem dziełem Keitha Godfrey'a.
  Marc Prior jest koordynatorem projektu OmegaT.

Współtwórcy:
(w porządku alfabetycznym)

Wkład w rozwój kodu programu wnieśli
  Zoltan Bartko
  Didier Briel (kierownik wydań)
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

Inne rodzaje czynnego uczestnictwa
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (kierownik lokalizacji)
  Vito Smolej (kierownik dokumentacji)
  Samuel Murray
  Marc Prior 
  i wielu, wielu innych bardzo pomocnych ludzi

(Jeżeli uważasz, że i ty wziąłeś znaczący udział w projekcie OmegaT, a nie widzisz tutaj swojego nazwiska, skontaktuj się z nami.)

OmegaT korzysta z następujących bibliotek:

  HTMLParser, autorzy: Somik Raha, Derrick Oswald i inni (Licencja LGPL)
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter 1.0.8, autor: Steve Roy (Licencja LGPL)
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework 2.1.4, producent: VLSolutions (Licencja CeCILL)
  http://www.vlsolutions.com/en/products/docking/

  Hunspell, autor: László Németh i inni (Licencja LGPL)

  JNA, autorzy: Todd Fast, Timothy Wall i inni (Licencja LGPL)

  Swing-Layout 1.0.2 (Licencja LGPL)

  Jmyspell 2.1.4 (Licencja LGPL)

  JAXB 2.1.7 (GPLv2 + wyjątek classpath)

==============================================================================
 5.  Czy OmegaT działa nieprawidłowo? Potrzebujesz pomocy?

Zanim zgłosisz błąd, upewnij się, że dokładnie sprawdziłeś w 
dokumentacji. To, z czym się zetknąłeś, może nie być błędem, lecz cechą OmegaT, o której nie wiedziałeś wcześniej. Jeżeli sprawdzasz logi OmegaT i widzisz komunikaty takie jak
"Error", "Warning", "Exception" albo "died unexpectedly", to prawdopodobnie odkryłeś autentyczny problem (plik log.txt znajduje się w katalogu ustawień użytkownika, sprawdź w podręczniku, gdzie jest ten katalog).

Kolejna rzecz, którą należy zrobić, to potwierdzenie tego, co obserwujesz, przez innych użytkowników, żeby upewnić się, że nie zostało to już wcześniej zgłoszone. Możesz też sprawdzić to na stronie zgłaszania błędów na SourceForge. Tylko kiedy jesteś pewien, że to ty jako pierwszy zauważyłeś pewną powtarzalną sekwencję zdarzeń, która wywołała niewłaściwe zachowanie programu, powinienieś wysłać zgłoszenie błędu.

Dobre zgłoszenie błędu musi zawierać dokładnie trzy rzeczy:
  - Kroki prowadzące do wystąpienia błędu,
  - Czego oczekiwałeś i
  - Co zobaczyłeś zamiast tego.

Możesz dołączyć kopie plików, fragmenty pliku log, zrzuty ekranu, wszystko, co według ciebie pomoże twórcom programu znaleźć i naprawić błąd.

Zasoby grupy dyskusyjnej użytkowników można przeglądać pod adresem:
     http://groups.yahoo.com/group/OmegaT/

Przeglądać zgłoszenia błędów i wysłać nowe zgłoszenie błędu w razie potrzeby, można pod adresem:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Aby śledzić, co dzieje się z twoim zgłoszeniem błedu, możesz zarejestrować się jako użytkownik Source Forge.

==============================================================================
6.   Informacje dotyczące wersji

W pliku 'changes.txt' znajdziesz szczegółowe informacje o zmianach w tej i wszystkich poprzednich wersjach.


==============================================================================
