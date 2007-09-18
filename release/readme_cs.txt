Tento překlad vypracoval [Josef Molnár], copyright© [2007].

==============================================================================
  OmegaT 1.6.2, soubor Čti mě

  1.  Informace o programu OmegaT
  2.  Co je OmegaT
  3.  Všeobecné poznámky o Java & OmegaT
  4.  Podpora projektu OmegaT
  5.  Máte s OmegaT problémy? Potřebujete pomoc?
  6.  Podrobnosti o vydání

==============================================================================
  1.  Informace o programu OmegaT


Nejaktuálnější informace o aplikaci OmegaT můžete nalézt (anglicky, slovensky, holandsky, portugalsky):
      http://www.omegat.org/omegat/omegat.html

Uživatelská podpora je poskytována v uživatelské skupině Yahoo (vícejazyčná), zde je též možno prohledávat archívy i bez přihlášení se:
     http://groups.yahoo.com/group/OmegaT/

Požadavky na zlepšení (anglicky), na stránce SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Hlášení chyb (anglicky), na stránkách SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Co je OmegaT

OmegaT je nástroj na překlad podporovaný počítačem. Je to program svobodný, v tom smyslu,
že za jeho používání nemusíte nic platit, dokonce ani při
oficiálním používání, a ve významu, že dokud respektujete uživatelskou licenci,
máte svobodu pro jeho úpravy a/nebo šíření.

Hlavní vlastnosti OmegaT jsou
  - schopnost provozu pod jakýmkoliv operačním systémem podporujícím Javu
  - používání jakéhokoliv platného TMX souboru jako překladové reference
  - flexibilní segmentace vět (využíváním metody podobné SRX)
  - vyhledávání v projektu a v referenčních překladových pamětích
  - vyhledávání v jakémkoliv adresáři včetně souborů čitelných pro OmegaT
  - vyhledávání přibližných překladů
  - elegantní zacházení s projekty včetně komplexních adresářových struktur
  - podpora pro slovníky (kontrola terminologie)
  - snadno srozumitelná dokumentace a úvodní tutoriál
  - lokalizace do mnoha jazyků.

OmegaT podporuje dokumenty OpenDocument, Microsoft Office (za užití 
OpenOffice.org jakožto filtru pro konverzi formátu nebo konverzí do HTML),
OpenOffice.org nebo StarOffice, stejně jako (X)HTML, lokalizační soubory Java, soubory v prostém textu a další.

Omega automaticky zpracuje dokonce i ty nejkomplexnější struktury zdrojových adresářů, pro přístup ke všem podporovaným souborům, a vytvoří cílový adresář s přesně stejnou strukturou, včetně kopií jakýchkoliv nepodporovaných souborů.

Pro stručný úvodní tutoriál spusťte program OmegaT a čtěte zobrazený Stručný úvodní průvodce.

Uživatelská příručka je v balíčku, který jste právě stáhli, můžete ji zobrazit z menu [Nápověda] po spuštění aplikace OmegaT.

==============================================================================
 3. Všeobecné poznámky o Java & OmegaT

OmegaT vyžaduje v operačním systému nainstalováno Java Runtime Environment verze 1.4 nebo vyšší. Toto prostředí možno získat zde: 
    http://java.com

Uživatelé Windows a Linuxu možná budou muset Javu nainstalovat, pokud to dosud nebylo provedeno.
Projekt OmegaT také nabízí verzi, ve které je Java zahrnuta. Uživatelé MacOSX mají Javu na svých strojích již instalovánu.

Když máte Javu nainstalovánu, můžete aplikaci OmegaT spustit po dvojitém nakliknutí souboru OmegaT.jar.

Po instalaci Javy bude možná zapotřebí upravit cestu proměnné Vašeho systému tak, aby zde byl uveden adresář, ve kterém je aplikace Java umístěna. 

Uživatelé Linuxu by měli věnovat pozornost skutečnosti, že OmegaT nebude spolupracovat se svobodnými(free)/open-source implementacemi Javy, které se dodávají s mnohými distribucemi Linuxu (například Ubuntu), protože tyto budou buď zastaralé nebo neúplné. Stáhněte si a instalujte
Java Runtime Environment (JRE) od firmy Sun přes výše uvedený odkaz,
nebo si stáhněte a instalujte balík OmegaT, který obsahuje JRE (balík .tar.gz označený jako "Linux").

Když provozujete Linux na systémech PowerPC, budete muset stáhnout JRE od IBM,
protože Sun neposkytuje JRE pro systémy PPC. Stahovat pak můžete z:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html

==============================================================================
 4. Podpora projektu OmegaT

Pokud chcete podporovat vývoj OmegaT, spojte se s vývojáři na adrese: 
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Jestli chcete překládat uživatelské rozhraní pro OmegaT, uživatelskou příručku nebo jiné příbuzné dokumenty, čtěte:
      http://www.omegat.org/omegat/omegat_en/translation-info.html

a přihlaste se do seznamu překladatelů:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Pro jiný způsob podpory, přihlaste se nejprve do uživatelské skupiny na adrese:
      http://tech.groups.yahoo.com/group/omegat/

A buďte v kontaktu s děním okolo aplikace OmegaT ...

  OmegaT je původním projektem autora Keith Godfrey.
  Marc Prior je koordinátorem projektu OmegaT.

Mezi předcházející přispěvatele patří:
(podle abecedy)

Do kódu přispěli
  Didier Briel
  Kim Bruning
  Sacha Chua
  Thomas Huriaux
  Maxym Mykhalchuk (vedoucí vývojář)
  Henry Pijffers (release manager, zodpovědný za vydávání aktualizací)
  Benjamin Siband
  Martin Wunderlich

K lokalizaci přispěli
  Roberto Argus (brazilská portugalština)
  Alessandro Cattelan (italština)
  Sabine Cretella (němčina)
  Suzanne Bolduc (esperanto)
  Didier Briel (francouzština)
  Frederik De Vos (holandština)
  Cesar Escribano Esteban (španělština)
  Dmitri Gabinski (běloruština, esperanto a ruština)
  Takayuki Hayashi (japonština)
  Jean-Christophe Helary (francouzština a japonština)
  Yutaka Kachi (japonština)
  Elina Lagoudaki (řečtina)
  Martin Lukáč (slovenština)
  Samuel Murray (afrikaans)
  Yoshi Nakayama (japonština)
  David Olveira (portugalština)
  Ronaldo Radunz (brazilská portugalština)
  Thelma L. Sabim (brazilská portugalština)
  Juan Salcines (španělština)
  Pablo Roca Santiagio (španělština)
  Karsten Voss (polština)
  Gerard van der Weyde (holandština)
  Martin Wunderlich (němčina)
  Hisashi Yanagida (japonština)
  Kunihiko Yokota (japonština)
  Erhan Yükselci (turečtina)
  Dragomir Kovacevic (srbochorvatština)
  Claudio Nasso (italština)
  Ahmet Murati (albánština)
  Sonja Tomaskovic (němčina)

Dále přispěli
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (současný manažer dokumentace)
  Samuel Murray
  Marc Prior (současný manažer lokalizací)
  a mnoho, mnoho dalších nápomocných lidí

(Pokud si myslíte, že jste významně přispěli k projektu OmegaT,
ale své jméno nevidíte v těchto záznamech, klidně nás kontaktujte.)

OmegaT používá následující knihovny:
  HTMLParser od Somik Raha, Derrick Oswald a další (LGPL Licence).
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter od Steve Roy (LGPL Licence).
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework od VLSolutions (CeCILL Licence).
  http://www.vlsolutions.com/en/products/docking/

==============================================================================
 5.   Máte s aplikací OmegaT problémy? Potřebujete pomoc?

Před ohlášením jakékoliv chyby se ujistěte, že jste si důkladně prošli dokumentaci. To co vidíte může být vlastností OmegaT, kterou jste právě objevili. Když se navštívíte log OmegaT a vidíte slova jako 
"Error" (Chyba), "Warning" (Upozornění), "Exception" (Výjimka) nebo "died unexpectedly" (neočekávané ukončení), tak asi něco hledáte (soubor log.txt se nachází v adresáři předvoleb uživatele, jeho umístění naleznete v příručce).

Další věc, kterou učiníte je ověřit si to, co jste nalezli i u ostatních uživatelů, aby jste se ujistili, zda to samé už někdy nebylo hlášeno. Můžete si to také ověřit na stránce
SourceForge. Jedině, že jste si jisti, že jste první, kdo našel nějakou
zopakovatelnou sekvenci událostí, která spustila něco, co se nemělo stát,
tak byste měli podat hlášení o chybě.

Každé dobré hlášení o chybě potřebuje přesně tři věci.
  - Kroky, které je zapotřebí zopakovat,
  - co jste čekali, že uvidíte, a
  - co jste uviděli místo toho.

Můžete přidat kopie souborů, části logu, snímky obrazovky, prostě cokoliv o čem si myslíte,
že pomůže vývojářům nalézt a opravit Vámi hlášenou chybu.

Archívy uživatelské skupiny můžete prohlížet na adrese:
     http://groups.yahoo.com/group/OmegaT/

Prohlížet stránku hlášení o chybách a v případě potřeby přidat nové hlášení můžete zde:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Aby jste byli informování o tom, co se děje s Vaším hlášením o chybě, můžete se zaregistrovat jako uživatel Source Forge.

==============================================================================
6.   Podrobnosti o vydání

5.   Máte s aplikací OmegaT problémy? Potřebujete pomoc?

Před ohlášením jakékoliv chyby se ujistěte, že jste si důkladně prošli dokumentaci. To co vidíte může být vlastností OmegaT, kterou jste právě objevili. Když se navštívíte log OmegaT a vidíte slova jako 
"Error" (Chyba), "Warning" (Upozornění), "Exception" (Výjimka) nebo "died unexpectedly" (neočekávané ukončení), tak asi něco hledáte (soubor log.txt se nachází v adresáři předvoleb uživatele, jeho umístění naleznete v příručce).

Další věc, kterou učiníte je ověřit si to, co jste nalezli i u ostatních uživatelů, aby jste se ujistili, zda to samé už někdy nebylo hlášeno. Můžete si to také ověřit na stránce
SourceForge. Jedině, že jste si jisti, že jste první, kdo našel nějakou
zopakovatelnou sekvenci událostí, která spustila něco, co se nemělo stát,
tak byste měli podat hlášení o chybě.

Každé dobré hlášení o chybě potřebuje přesně tři věci.
  - Kroky, které je zapotřebí zopakovat,
  - co jste čekali, že uvidíte, a
  - co jste uviděli místo toho.

Můžete přidat kopie souborů, části logu, snímky obrazovky, prostě cokoliv o čem si myslíte,
že pomůže vývojářům nalézt a opravit Vámi hlášenou chybu.

Archívy uživatelské skupiny můžete prohlížet na adrese:
     http://tech.groups.yahoo.com/group/omegat/

Prohlížet stránku hlášení o chybách a v případě potřeby přidat nové hlášení můžete zde:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Aby jste byli informování o tom, co se děje s Vaším hlášením o chybě, můžete se zaregistrovat jako uživatel Source Forge.

==============================================================================
6.   Podrobnosti o vydání

Podrobné informace o změnách v tomto a všech předcházejících vydáních naleznete v souborech 'changes.txt'.

Podporované formáty souborů:
  - prostý text
  - HTML a XHTML
  - HTML Help Compiler (HCC)
  - OpenDocument / OpenOffice.org
  - zdrojové balíčky Java (.properties)
  - soubory INI (soubory s páry klíč=hodnota v jakémkoliv kódování 
  - soubory PO
  - formát dokumentačních souborů DocBook
  - soubory Microsoft Open XML

Změny v jádru:
  - flexibilní (větná) segmentace
  - filtry formátů souborů mohou být vytvářeny jako pluginy
  - aktualizovaný kód s více komentáři
  - instalátor pro Windows
  - atributy tagů v HTML je možno překládat
  - plná kompatibilita pamětí TMX 1.1-1.4 Level 1
  - částečná podpora pamětí TMX 1.4b Level 2

Nové vlastnosti uživatelského rozhraní (ve srovnání se sérií OmegaT 1.4):
  - rozhraní hledání doplněno rozšířenou funkčností
  - hlavní rozhraní zlepšeno použitím připojitelných oken

==============================================================================

