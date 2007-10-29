Tłumaczenie to jest dziełem Bartłomieja Łasochy, copyright© 2007.

==============================================================================
  Plik Read Me, OmegaT 1.6.2

  1.  Informacje na temat OmegaT
  2.  Czym jest OmegaT?
  3.  Uwagi ogólne na temat Javy i OmegaT
  4.  Wkład w OmegaT
  5.  Coś działa nieprawidłowo? Potrzebujesz pomocy?
  6.  Uwagi do wydania

==============================================================================
  1.  Informacje na temat OmegaT


Najświeższe informacje na temat OmegaT (po angielsku, słowacku, niderlandzku i portugalsku) znajdziesz pod adresem:
      http://www.omegat.org/omegat/omegat.html

Wsparcie użytkownika na naszej (wielojęzycznej) grupie Yahoo, której zawartość przeglądać możesz bez subskrypcji: 
     http://groups.yahoo.com/group/OmegaT/

Zapotrzebowanie na ulepszenia (po angielsku), na stronie SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Zgłaszanie błędów (po angielsku), na stronie Sourceforge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Czym jest OmegaT?

OmegaT jest narzędziem do tłumaczenia wspomaganego komputerowo (CAT - Computer Assisted Translation). Jest oprogramowaniem wolnym, co oznacza, że nie musisz za nie płacić aby go używać, nawet w celach zarobkowych, oraz w tym sensie, że wolno ci je modyfikować i rozprowadzać, o ile nie łamiesz licencji.

Główne cechy OmegaT:
  - praca w dowolnym systemie operacyjnym który obsługuję Javę
  - użycie dowolnych zgodnych z TMX plików jako pamięci tłumaczenia
  - elastyczna segmentacja (wykorzystująca metodę podobną do SRX)
  - przeszukiwanie projektu i odnośnej pamięci tłumaczenia
  - przeszukiwanie dowolnego katalogu zawierającego pliki odczytywalne przez OmegaT
  - dopasowania rozmyte
  - elegancka obsługa projektów zawierających złożoną strukturę katalogów.
  - obsługa słowników (korekta słownictwa)
  - łatwa do zrozumienia dokumentacja i przewodnik użytkownika
  - lokalizacja w kilku językach.

OmegaT obsługuje pliki OpenDocument, dokumenty Microsoft Office (używając jako filtra OpenOffice.org, lub konwertując do HTML), pliki OpenOffice.org lub StarOffice, oraz (X)HTML, pliki lokalizacyjne Javy, zwykłe pliki tekstowe, i inne.

OmegaT automatycznie przeanalizuje nawet najbardziej złożony katalog źródłowy, aby pobrać wszystkie pliki obsługiwanych formatów, oraz utworzyć katalog docelowy o dokładnie tej samej strukturze, włączając w to kopie plików, których formatu program nie wspiera.

Aby zapoznać się szybko z podstawami programu, uruchom OmegaT i przeczytaj wyświetlony Instant Start 
Tutorial.

Podręcznik użytkownika znajduje się w pakiecie który pobrałeś. Uruchamiając OmegaT, znajdziesz go w menu [Help].

==============================================================================
 3. Uwagi ogólne na temat Javy i OmegaT

OmegaT wymaga Java Runtime Environment w wersji 1.4 lub wyższej. Można je pobrać ze strony:
    http://java.com

Użytkownicy Windows i Linux powinni zainstalować Javę o ile nie zrobili tego wcześniej.
Można również skorzystać z wersji OmegaT w której już jest Java. Użytkownicy MacOSX mają Javę zainstalowaną domyślnie.

W prawidłowo skonfigurowanym systemie, powinieneś móc uruchomić OmegaT klikając dwukrotnie ikonkę pliku OmegaT.jar.

Po instalacji Javy należy upewnić się, czy katalog w którym aplikacja ta znajduje się, został dodany do zmiennej środowiskowej PATH.

Użytkownicy Linuxa powinni zwrócić uwagę na fakt, że OmegaT może nie pracować z darmowymi/open source'owymi implementacjami Javy, które znajdują się w wielu dystrybucjach linuxa (na przykład Ubuntu), gdyż mogą być one nieaktualne, albo niekompletne. Pobierz i zainstaluj Java Runtime Environment (JRE) firmy Sun korzystając z powyższego linku, lub zainstaluj wersję OmegaT która zawiera JRE (paczka tar.gz oznaczona "Linux")

Korzystając z Linuxa na platformie PowerPC, należy pobrać i zainstalować JRE firmy IBM, gdyż Sun nie produkuje Javy dla systemów PPC. Znajdziemy je pod adresem:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html

==============================================================================
 4. Nasz wkład w OmegaT

Aby włączyć się w rozwój OmegaT, skontaktuj się z członkami projektu na:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Aby wziąć udział w tłumaczeniu interfejsu użytkownika OmegaT, instrukcji, lub innych związanych z nimi dokumentów, przeczytaj:
      http://www.omegat.org/omegat/omegat_en/translation-info.html

I zapisz się na listę tłumaczy pod adresem:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Jeśli chcesz nam pomóc w inny sposób, zacznij od wpisania się na listę użytkowników: 
      http://tech.groups.yahoo.com/group/omegat/

I spróbuj poczuć co w trawie piszczy...

  Pierwotnym twórcą OmegaT jest Keith Godfrey.
  Marc Prior jest koordynatorem projektu OmegaT.

Współtwórcami byli:
(w porządku alfabetycznym)

Swój wkład w kod wnieśli
  Didier Briel
  Kim Bruning
  Sacha Chua
  Thomas Huriaux
  Maxym Mykhalchuk (lider projektu)
  Henry Pijffers (release manager)
  Benjamin Siband
  Martin Wunderlich

W lokalizacji uczestniczyli
  Roberto Argus (portugalski-Brazylia)
  Alessandro Cattelan (włoski)
  Sabine Cretella (niemiecki)
  Suzanne Bolduc (esperanto)
  Didier Briel (francuski)
  Frederik De Vos (niderlandzki)
  Cesar Escribano Esteban (hiszpański)
  Dmitri Gabinski (białoruski, esperanto, i rosyjski)
  Takayuki Hayashi (japoński)
  Jean-Christophe Helary (francuski i japoński)
  Yutaka Kachi (japoński)
  Elina Lagoudaki (grecki)
  Martin Lukáč (słowacki)
  Samuel Murray (afrykanerski)
  Yoshi Nakayama (japoński)
  David Olveira (portugalski)
  Ronaldo Radunz (portugalski-Brazylia)
  Thelma L. Sabim (portugalski-Brazylia)
  Juan Salcines (hiszpański)
  Pablo Roca Santiagio (hiszpański)
  Karsten Voss (polski)
  Gerard van der Weyde (niderlandzki)
  Martin Wunderlich (niemiecki)
  Hisashi Yanagida (japoński)
  Kunihiko Yokota (japoński)
  Erhan Yükselci (turecki)
  Dragomir Kovacevic (serbski, chorwacki)
  Claudio Nasso (włoski)
  Ahmet Murati (albański)
  Sonja Tomaskovic (niemiecki)

Inne rodzaje czynnego uczestnictwa
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (obecny menadżer dokumentacji)
  Samuel Murray
  Marc Prior (obecny menadżer lokalizacji)
I wielu wielu innych, bardzo pomocnych ludzi

(Jeśli uważasz że i ty wyraźnie przyczyniłeś się do projektu OmegaT, a nie widzisz tutaj swojego nazwiska, skontaktuj się z nami.)

OmegaT korzysta z następujących bibliotek:
  HTMLParser którego autorem jest Somik Raha, Derrick Oswald i inni (na licencji LGPL).
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter którego autorem jest Steve Roy (na licencji LGPL).
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework od VLSolutions (licencja CeCILL).
  http://www.vlsolutions.com/en/products/docking/

==============================================================================
 5. Coś działa nieprawidłowo? Potrzebujesz pomocy?

Zanim zgłosisz usterkę, upewnij się, że dokładnie sprawdziłeś w dokumentacji. To z czym się zetknąłeś, może być czymś typowym dla OmegaT, o czym nie wiedziałeś wcześniej. Jeżeli sprawdzasz logi OmegaT i widzisz w nich komunikaty typu
"Error", "Warning", "Exception", lub "died unexpectedly", to coś jest na rzeczy (plik log.txt znajduje się w katalogu ustawień użytkownika, miejsce to wskazane jest w instrukcji).

Kolejną rzeczą którą zrobić należy, jest potwierdzenie tego co obserwujesz przez innych użytkowników, aby upewnić się, że nie zostało już wcześniej zgłoszone. Możesz również sprawdzić na stronie raportowania błędów na Sourceforge. Tylko kiedy jesteś pewny, że to ty jako pierwszy zauważyłeś pewną powtarzalną sekwencję zdarzeń, która skutkowała niewłaściwym zachowaniem programu powinieneś zgłosić błąd.

W każdym dobrym zgłoszeniu błędu muszą znaleźć się dokładnie trzy elementy:
  - Krok po kroku co należy zrobić,
  - Czego oczekujesz w wyniku, oraz
  - Co zamiast tego się dzieje.

Możesz dodać kopie plików, części pliku log, zrzuty ekranu, wszystko co myślisz że może pomóc twórcom w znalezieniu i naprawieniu usterki.

Zasoby grupy dyskusyjnej użytkowników przegląć możesz pod adresem:
     http://groups.yahoo.com/group/OmegaT/

Stronę zgłaszania błędów znajdziesz, i ewentualnie zgłosisz usterkę, pod adresem:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Jeśli interesują cię losy twojego zgłoszenia błędu, możesz się zarejestrować jako użytkownik Source Forge.

==============================================================================
6.   Uwagi do wydania

==============================================================================
 5. Coś działa nieprawidłowo? Potrzebujesz pomocy?

Zanim zgłosisz usterkę, upewnij się, że dokładnie sprawdziłeś w dokumentacji. To z czym się zetknąłeś, może być czymś typowym dla OmegaT, o czym nie wiedziałeś wcześniej. Jeżeli sprawdzasz logi OmegaT i widzisz w nich komunikaty typu
"Error", "Warning", "Exception", lub "died unexpectedly", to coś jest na rzeczy (plik log.txt znajduje się w katalogu ustawień użytkownika, miejsce to wskazane jest w instrukcji).

Kolejną rzeczą którą zrobić należy, jest potwierdzenie tego co obserwujesz przez innych użytkowników, aby upewnić się, że nie zostało już wcześniej zgłoszone. Możesz również sprawdzić na stronie raportowania błędów na Sourceforge. Tylko kiedy jesteś pewny, że to ty jako pierwszy zauważyłeś pewną powtarzalną sekwencję zdarzeń, która skutkowała niewłaściwym zachowaniem programu powinieneś zgłosić błąd.

W każdym dobrym zgłoszeniu błędu muszą znaleźć się dokładnie trzy elementy:
  - Krok po kroku co należy zrobić,
  - Czego oczekujesz w wyniku, oraz
  - Co zamiast tego się dzieje.

Możesz dodać kopie plików, części pliku log, zrzuty ekranu, wszystko co myślisz że może pomóc twórcom w znalezieniu i naprawieniu usterki.

Aby przeglądać zasoby grupy wejdź na:
     http://tech.groups.yahoo.com/group/omegat/

Stronę zgłaszania błędów znajdziesz, i ewentualnie zgłosisz usterkę, pod adresem:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Jeśli interesują cię losy twojego zgłoszenia błędu, możesz się zarejestrować jako użytkownik Source Forge.

==============================================================================
6.   Uwagi do wydania

Zajrzyj do pliku 'changes.txt' jeśli szukasz szczegółowych informacji o zmianach w tym i wszystkich poprzednich wydaniach.

Obsługiwane formaty plików:
  - Zwykłe pliki tekstowe
  - HTML oraz XHTML
  - pliki HTML Help Compiler'a (HCC)
  - OpenDocument / OpenOffice.org
  - pliki Java resource bundle (o rozszerzeniu .properties)
  - pliki INI  (pliki zawierające pary klucz=wartość w dowolnym kodowaniu)
  - pliki PO
  - format dokumentacji DocBook 
  - pliki Microsoft OpenXML

Zmiany w rdzeniu programu:
  - Elastyczna segmentacja (zdań)
  - Filtry do różnych formatów plików mogą być tworzone jako wtyczki.
  - Nowy podział kodu, z liczniejszymi komentarzami
  - Instalator dla windows
  - Możliwość tłumaczenia atrybutów znaczników HTML
  - Pełna zgodność z TMX 1.1-1.4b Level 1
  - Częściowa obsługa TMX 1.4b Level 2 

Nowe Funkcje interfejsu użytkownika (w porównaniu do wydania 1.4 OmegaT):
  - Napisany od nowa, bardziej funkcjonalny  interfejs wyszukiwania
  - Ulepszony główny interfejs z użyciem okien dokowanych 

==============================================================================

