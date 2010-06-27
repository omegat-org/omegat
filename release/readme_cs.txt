Tento překlad vypracoval [Josef Molnár], copyright© [2010].

==============================================================================
  OmegaT 2.0, soubor Read Me / Čti mě

  1.  Informace o aplikaci OmegaT 
  2.  Co je OmegaT?
  3.  Instalace programu OmegaT
  4.  Podpora projektu OmegaT
  5.  Máte s aplikací OmegaT problémy? Potřebujete pomoc?
  6.  Podrobnosti k vydání

==============================================================================
  1.  Informace o aplikaci OmegaT 


Nejaktuálnější informace o aplikaci OmegaT naleznete na adrese:
      http://www.omegat.org/

Uživatelská podpora v rámci uživatelské skupiny na Yahoo (vícejazyčně), zde je možno prohledávat archívy i bez registrace:
        

Požadavky a návrhy na zlepšení (anglicky), na stránce SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Požadavky a návrhy na zlepšení (anglicky), na stránce SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Co je OmegaT?

OmegaT je nástroj pro překlad podporovaný počítačem (CAT - computer assisted translation). Jedná se o svobodný program, to znamená,
že za jeho používání nemusíte nic platit, dokonce ani při používání ve firmě, a můžete jej, při respektování uživatelské licence, upravovat anebo dále šířit.

Hlavní znaky programu:
  - schopnost běhu na libovolném operačním systému, který podporuje Javu
  - lze použít jakýkoliv platný soubor TMX jakožto překladovou referenci
  - flexibilní segmentace vět (za použití metody typu SRX)
  - vyhledávání v projektu a paměťových souborech překladu
  - vyhledávání souborů podporovaných formátů v libovolném adresáři 
  - nabízení přibližných překladů
  - program přehledně zvládne zpracovat projekty s komplexní adresářovou strukturou
  - podpora glosářů, tj. vlastních slovníků (kontrola terminologie) 
  - podpora průběžné kontroly pravopisu na základě OpenSource
  - podpora slovníků StarDict
  - podpora služby strojového překladu Google Translate
  - jasná a ucelená dokumentace a návod k programu
  - lokalizace do spousty jazyků.

OmegaT bez problémů zvládá zpracovat následující formáty:
  - prostý text
  - HTML a XHTML
  - HTML Help Compiler
  - OpenDocument/OpenOffice.org
  - lokalizační balíčky Java resource bundles (.properties)
  - soubory INI (soubory obsahující páry key=value (klíč=hodnota) v libovolném kódování)
  - soubory PO
  - formát dokumentace DocBook
  - soubory Microsoft OpenXML
  - jednojazyčné soubory Okapi XLIFF
  - QuarkXPress CopyFlowGold
  - soubory titulků (SRT)
  - ResX
  - Android resource
  - LaTeX
  - Typo3 LocManager
  - Help & Manual
  - zdroje Windows RC
  - Mozilla DTD

Aplikaci OmegaT lze stejně dobře přizpůsobit i jiným formátům.

OmegaT automaticky zpracuje dokonce i ty nejkomplexnější struktury zdrojových adresářů, dostane se tak ke všem podporovaným souborům, a vytvoří cílový adresář s přesně stejnou strukturou, včetně kopií jakýchkoliv nepodporovaných souborů.

Návod pro rychlý start (Stručný úvodní průvodce) se zobrazí po spuštění programu OmegaT.

Uživatelská příručka je v balíčku, který jste právě stáhli, po spuštění aplikace OmegaT si ji můžete zobrazit z menu [Nápověda].

==============================================================================
 3. Instalace programu OmegaT

3.1 3.1 Obecné informace
Ke spuštění vyžaduje OmegaT ve vašem systému nainstalované prostředí Java Runtime Environment (JRE) verzi 1.5 nebo vyšší. OmegaT je v současnosti nabízena ve standardním provedení již s JRE, aby se uživatelům ušetřily potíže s výběrem, získáním a instalací vhodného prostředí. 

Jestliže Javu již máte, nejjednodušší způsob instalace aktuální verze programu OmegaT je použití Java Web Start. 
V takovém případě stačí stáhnout následující soubor a spustit jej:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Při prvním spuštění bude nainstalováno nejen prostředí přesně pro váš počítač ale také samotná aplikace. Při dalším spuštění již nemusíte být online.

V závislosti na operačním systému se může objevit několik bezpečnostních výstrah. Certifikát podepsal  „Didier Briel“ (self-signed). 
Práva, která udělíte této verzi (která lze nazývat jako „neomezený přístup k počítači“) jsou identická s právy, která jste přidělili lokální verzi při její instalaci, což znamená: tato práva povolí přístup na hard disk počítače. Při pozdějším spuštění souboru OmegaT.jnlp dojde ke kontrole verze, pokud tedy jste online, a když bude nalezena novější verze, tak ji nainstaluje a aplikaci OmegaT spustí. 

Jiné způsoby stahování a instalace programu OmegaT si ukážeme později. 

Pro uživatele Windows a Linuxu: jestli jste si jisti, že váš systém má již vhodnou verzi JRE nainstalovánu, můžete instalovat verzi programu OmegaT bez JRE (to je naznačeno v samotném názvu verze, „Without_JRE“, tedy bez Javy). 
Pokud máte jakékoliv pochybnosti, doporučujeme použít „standardní“ verzi, tj. s JRE. Tato volba je bezpečná, dokonce i když už máte JRE ve vašem systému instalováno, tato verze s tímto již nainstalovaným prostředím nebude kolidovat.

Pro uživatele Linuxu: věnujte pozornost skutečnosti, že OmegaT nebude spolupracovat se svobodnými/open-source implementacemi Javy, které se dodávají s mnohými distribucemi Linuxu (například Ubuntu), protože tyto mohou být buď zastaralé, nebo neúplné. Stažení a instalace
Stahujte Java Runtime Environment (JRE) od firmy Sun přes výše uvedený odkaz,
nebo si stáhněte a instalujte balík OmegaT, který obsahuje JRE (balík .tar.gz označený výrazem „Linux“).

Uživatelé Maců: JRE je na Mac OS X už instalováno.

Pro uživatele provozující Linux na systémech PowerPC: bude zapotřebí stáhnout JRE od IBM,
protože Sun neposkytuje JRE pro systémy PPC. V tomto případě stahujte z adresy:

    http://www-128. ibm.com/developerworks/java/jdk/linux/download.html 


3.2 3.2 Instalace
* uživatelé Windows: Jednoduše spusťte instalátor. Pokud budete chtít, instalátor může vytvořit zástupce pro spouštění programu OmegaT.
* Ostatní uživatelé: Pro instalaci programu OmegaT vytvořte odpovídající adresář pro program OmegaT (např. v Linuxu /usr/local/lib). Zkopírujte archív OmegaT zip nebo tar.gz do tohoto adresáře a zde jej také rozbalte.

3.3 3.3 Spuštění programu OmegaT
Program OmegaT lze spouštět několika způsoby.

* uživatelé Windows: dvojitým kliknutím na soubor OmegaT.exe. Pokud ve vašem Správci souborů vidíte soubor OmegaT a ne OmegaT.exe (Průzkumník Windows), změňte nastavení tak, aby byly zobrazovány přípony souborů.

* dvojitým kliknutím na soubor OmegaT.jar. Toto bude fungovat, jen když je ve vašem systému typ souboru .jar asociován s Javou.

* Přes příkazovou řádku. Příkaz ke spuštění OmegaT je:

cd <adresář, kde je uložený soubor OmegaT.jar>

<jméno a cesta k souboru spustitelného Javou> -jar OmegaT.jar

(Soubor spustitelný Javou je soubor „java“ v Linuxu a „java.exe“ ve Windows.
Pokud je Java instalována na úrovni systému, nemusíte vkládat úplnou cestu.)

* uživatelé Windows: Instalátor může vytvořit zástupce v menu Start, na Ploše nebo v panelu Snadného spuštění. Stejně tak můžete ručně přetáhnout soubor OmegaT.exe do menu Start, na Plochu nebo na panel Snadné spuštění.

* uživatelé Linuxu KDE: program OmegaT můžete přidat do svých menu následovně:

Control Center - Desktop - Panels - Menus - Edit K Menu - File - New Item/New 
Submenu.

Jakmile označíte vhodnou nabídku, přidejte položku podnabídky prostřednictvím File - New 
Submenu a File - New Item. Jako název nové položky vložte OmegaT.

V „příkazovém“ poli, použijte navigační tlačítko k tomu, abyste nalezli svůj spouštěcí OmegaT skript, a označte jej. 

Klikněte na ikonu (vpravo od Name/Description/Comment fields)
- Other Icons - Browse, a přejděte k podadresáři /images v adresáři aplikace OmegaT. Označte ikonu OmegaT.png.

Nakonec uložte změny -> Soubor - Uložit.

* uživatelé Linuxu GNOME: můžete přidat OmegaT na svůj panel (lišta v horní části obrazovky) následovně:

Klikněte pravým tlačítkem myši na panel: Přidat na panel...  pak volba Vlastní spouštěč aplikace Vložte „OmegaT“ do pole „Název“; v poli „Příkaz“, použijte navigační tlačítko k nalezení svého spouštěcího skriptu aplikace OmegaT. Označte jej a operaci potvrďte OK.

==============================================================================
 4. Jak se zapojit do projektu OmegaT

Jestli chcete přispívat při vývoji aplikace OmegaT, spojte se s vývojáři na adrese:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Jestli máte zájem o překlad uživatelského rozhraní OmegaT, uživatelské příručky nebo jiných příbuzných dokumentů, tak si nejprve přečtěte tyto informace:
      
      http://www.omegat.org/en/translation-info.html

a přihlaste se do konference pro překladatele:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Když byste chtěli přispívat jinak, přihlaste se nejdříve do uživatelské skupiny na adrese:
      http://tech.groups.yahoo.com/group/omegat/

A seznamte se s děním okolo aplikace OmegaT ...

  Autorem původního projektu OmegaT je Keith Godfrey.
  Koordinátorem projektu OmegaT je Marc Prior.

Mezi dřívější přispěvatele patří:
(podle abecedy)

Do kódu přispěli
  Zoltan Bartko
  Didier Briel (správa vydávání nových verzí)
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

Other contributions by
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (správa lokalizace)
  Vito Smolej (správa dokumentace)
  Samuel Murray
  Marc Prior 
  a mnoho, mnoho dalších velmi užitečných lidí

(Pokud si myslíte, že jste významně přispěli k projektu OmegaT,
ale své jméno nevidíte v těchto záznamech, klidně nás kontaktujte.)

OmegaT používá následující knihovny:

  HTMLParser od přispěvatelů Somik Raha, Derrick Oswald a dalších (Licence LGPL)
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter 1.0.8 od Steve Roy (LGPL Licence)
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework 2.1.4 od VLSolutions (CeCILL Licence)
  http://www.vlsolutions.com/en/products/docking/

  Hunspell autora László Németh a dalších (LGPL Licence).

  JNA dodali Todd Fast, Timothy Wall a dalších (LGPL Licence)

  Swing-Layout 1.0.2 (Licence LGPL)

  Jmyspell 2.1.4 (Licence LGPL)

  JAXB 2.1.7 (GPLv2 + výjimka classpath)

==============================================================================
 5.  Máte s aplikací OmegaT problémy? Potřebujete pomoc?

Než ohlásíte jakoukoliv chybu tak se ujistěte, že jste si důkladně prošli dokumentaci. To co vidíte může být vlastností OmegaT, kterou jste právě objevili. Když se podíváte na log (protokol) OmegaT a vidíte slova jako „Error“ (Chyba), „Warning“ (Upozornění), „Exception“ (Výjimka) nebo „died unexpectedly“ (neočekávané ukončení), tak jste pravděpodobně narazili na opravdový problém (soubor log.txt se nachází v adresáři předvoleb uživatele, jeho umístění naleznete v příručce).

Další věc, kterou učiníte, je ověřit si to co jste nalezli i u ostatních uživatelů, aby jste se ujistili, zda to samé už někdy nebylo hlášeno. Můžete si to také ověřit na stránce pro hlášení chyb na SourceForge. Jedině pokud jste si jisti, že jste první, kdo našel nějakou
zopakovatelnou sekvenci událostí, která spustila něco, co se nemělo stát,
teprve pak byste měli podat hlášení o chybě.

Každé dobré hlášení o chybě potřebuje přesně tři věci.
  - Kroky, které je zapotřebí zopakovat
  - co jste čekali, že uvidíte, a
  - co jste uviděli místo toho

Můžete přidat kopie souborů, části logu, snímky obrazovky, prostě cokoliv, o čem si myslíte, že pomůže vývojářům nalézt a opravit vámi hlášenou chybu.

Archívy uživatelské skupiny můžete prohlížet na adrese:
        

Prohlížet stránku hlášení o chybách a v případě potřeby přidat nové hlášení můžete zde:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Abyste byli informování o tom, co se děje s vaším hlášením o chybě, můžete se zaregistrovat jako uživatel Source Forge.

==============================================================================
6.   Podrobnosti k vydání

Podrobné informace o změnách v tomto a všech předcházejících vydáních naleznete v souborech 'changes.txt'.


==============================================================================
