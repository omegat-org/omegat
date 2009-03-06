Tento překlad vypracoval [Josef Molnár], copyright© [2009].

==============================================================================
  OmegaT 1.8, soubor Read Me / Čti mě

  1.   Informace o programu OmegaT 
  2.  Co je OmegaT?
  3.  Instalace programu OmegaT
  4.  Podpora projektu OmegaT
  5.  Máte s OmegaT problémy? Potřebujete pomoc?
  6.  Podrobnosti k vydání

==============================================================================
  1.   Informace o programu OmegaT 


Nejaktuálnější informace o aplikaci OmegaT naleznete na adrese
      http://www.omegat.org/

Uživatelská podpora, v rámci uživatelské skupiny na Yahoo (vícejazyčně), je zde možno prohledávat archívy i bez registrace:
     http://groups.yahoo.com/group/OmegaT/

Požadavky na zlepšení (anglicky), na stránce SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Hlášení chyb (anglicky), na stránkách SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Co je OmegaT?

OmegaT je nástroj na překlad podporovaný počítačem, tzv. CAT tool (computer-assisted translation tool). Je to program svobodný, v tom smyslu,
že za jeho používání nemusíte nic platit, dokonce ani při používání ve firmě, a můžete jej, při respektování uživatelské licence, upravovat a/nebo dále šířit.

Hlavní vlastnosti OmegaT jsou:
  - schopnost provozu pod jakýmkoliv operačním systémem podporujícím Javu
  - používání jakéhokoliv platného TMX souboru jako překladové reference
  - flexibilní segmentace vět (využíváním metody podobné SRX)
  - vyhledávání v projektu a v referenčních překladových pamětích
  - vyhledávání souborů v podporovaných formátech v libovolném adresáři 
  - vyhledávání přibližných překladů
  - inteligentní zacházení s projekty včetně komplexních adresářových struktur
  - podpora pro slovníky (kontrola terminologie)
  - jasná a vyčerpávající dokumentace a úvodní tutoriál
  - lokalizace do mnoha jazyků.

OmegaT podporuje následující formáty souborů:
  - prostý text
  - HTML a XHTML
  - soubory HTML Help Compiler
  - soubory OpenDocument/OpenOffice.org
  - zdrojové balíčky Java (.properties)
  - soubory INI (soubory s páry klíč=hodnota v jakémkoliv kódování)
  - soubory PO
  - formát dokumentačních souborů DocBook
  - soubory Microsoft OpenXML
  - jednojazyčné soubory XLIFF Okapi

Aplikaci OmegaT lze stejně dobře přizpůsobit i jiným formátům.

Omega automaticky zpracuje dokonce i ty nejkomplexnější struktury zdrojových adresářů, pro přístup ke všem podporovaným souborům, a vytvoří cílový adresář s přesně stejnou strukturou, včetně kopií jakýchkoliv nepodporovaných souborů.

Pro zobrazení stručného úvodního tutoriálu spusťte program OmegaT a přečtěte si zobrazeného Stručného úvodního průvodce.

Uživatelská příručka je v balíčku, který jste právě stáhli, můžete ji zobrazit z menu [Nápověda] po spuštění aplikace OmegaT.

==============================================================================
 3. Instalace programu OmegaT

3.1 Obecné informace
Aby bylo možno program spustit, vyžaduje OmegaT aby ve vašem systému bylo instalováno prostředí Java Runtime Environment (JRE) verzi 1.4 nebo vyšší. OmegaT je v současnosti nabízena ve standardním provedení již s JRE aby se uživatelům ušetřily potíže s výběrem, získáním a instalací vhodného prostředí. 

Pro uživatele Windows a Linuxu: jestli jste si jisti, že váš systém má již vhodnou verzi JRE instalovánu, můžete instalovat verzi programu OmegaT bez JRE (to je naznačeno v samotném názvu verze, „Without_JRE“, tedy bez JRE). 
Pokud máte jakékoliv pochybnosti, doporučujeme použít „standardní“ verzi, tj. s JRE. Tato volba je bezpečná, dokonce i když už máte JRE ve vašem systému instalováno, tato verze nebude s tímto již nainstalovaným prostředím kolidovat.

Pro uživatele Linuxu: věnujte pozornost skutečnosti, že OmegaT nebude spolupracovat se svobodnými(free)/open-source implementacemi Javy, které se dodávají s mnohými distribucemi Linuxu (například Ubuntu), protože tyto budou buď zastaralé, nebo neúplné. Download a instalace
Stahujte Java Runtime Environment (JRE) od firmy Sun přes výše uvedený odkaz,
nebo si stáhněte a instalujte balík OmegaT, který obsahuje JRE (balík .tar.gz označený jako „Linux“).

Uživatelé Mac-ů: JRE je na Mac OS X už instalováno.

Pro uživatele provozující Linux na systémech PowerPC: bude zapotřebí stáhnout JRE od IBM,
protože Sun neposkytuje JRE pro systémy PPC. Stahovat pak můžete z:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 

3.2 Instalace
* uživatelé Windows: Jednoduše spusťte instalátor. Pokud budete chtít, instalátor může vytvořit zástupce pro spouštění programu OmegaT.
* Ostatní uživatelé: Pro instalaci programu OmegaT vytvořte odpovídající adresář pro program OmegaT (např. v Linuxu  /usr/local/lib). Zkopírujte archív OmegaT zip nebo tar.gz do tohoto adresáře a zde jej také rozbalte.

3.3 Spuštění programu OmegaT
Program OmegaT lze spouštět několika způsoby.

* uživatelé Windows: dvojitým kliknutím na soubor OmegaT.exe. Pokud ve vašem Správci souborů vidíte soubor OmegaT a ne OmegaT.exe  (Windows Explorer), změňte nastavení tak, aby byly zobrazovány přípony souborů.

* dvojitým kliknutím na soubor OmegaT.jar. Toto bude fungovat, jen když je ve vašem systému typ souboru .jar asociován s Javou.

* Přes příkazový řádek. Příkaz ke spuštění OmegaT je:

cd <adresář, kde je uložený soubor OmegaT.jar>

<jméno a cesta k souboru spustitelného Javou> -jar OmegaT.jar

(Soubor spustitelný Javou je soubor java v Linuxu a  java.exe ve Windows.
Pokud je Java instalována na úrovni systému, nemusíte vkládat úplnou cestu.)

* uživatelé Windows: Instalátor může vytvořit zástupce v menu Start, na Ploše nebo v panelu Snadného spuštění. Stejně tak můžete ručně přetáhnout soubor OmegaT.exe do menu Start, na Plochu nebo na panel Snadné spuštění.

* uživatelé Linuxu KDE: program OmegaT můžete přidat do svých menu následovně:

Control Center - Desktop - Panels - Menus - Edit K Menu - File - New Item/New 
Submenu.

Jakmile označíte vhodné menu, přidejte submenu/položku s File - New 
Submenu a File - New Item. Vložte OmegaT jakožto název nové položky.

V „příkazovém“ poli, použijte navigační tlačítko k tomu, abyste nalezli svůj spouštěcí OmegaT skript, a označte jej. 

Klikněte na ikonu (vpravo od Name/Description/Comment fields)
- Other Icons - Browse, a přejděte k podadresáři /images v adresáři aplikace OmegaT. Označte ikonu OmegaT.png.

Nakonec uložte změny - File - Save.

* uživatelé Linuxu GNOME: můžete přidat OmegaT na svůj panel (lišta v horní části obrazovky) následovně:

Klikněte pravým tlačítkem myši na panel - Add New Launcher. Vložte „OmegaT“ do pole „Name“; v poli „Command“, použijte navigační tlačítko k nalezení svého spouštěcího skriptu aplikace OmegaT. Označte jej a operaci potvrďte OK.

==============================================================================
 4. Jak se zapojit do projektu OmegaT

Když se chcete podílet na vývoji aplikace OmegaT, kontaktujte vývojáře na adrese:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Jestli chcete překládat uživatelské rozhraní pro OmegaT, uživatelskou příručku nebo jiné příbuzné dokumenty, 
čtěte:
      
      http://www.omegat.org/en/translation-info.html

a přihlaste se do seznamu překladatelů:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Pro jiný způsob podpory, se přihlaste nejprve do uživatelské skupiny na adrese:
      http://tech.groups.yahoo.com/group/omegat/

A sledujte dění okolo aplikace OmegaT ...

  Keith Godfrey je autorem původního projektu OmegaT.
  Koordinátorem projektu OmegaT je Marc Prior.

Mezi dřívější přispěvatele patří:
(podle abecedy)

Do kódu přispěli
  Zoltan Bartko
  Didier Briel (release manager, zodpovědný za vydávání aktualizací)
  Kim Bruning
  Alex Buloichik
  Sandra Jean Chua
  Martin Fleurke  
  Thomas Huriaux
  Fabián Mandelbaum
  Maxym Mykhalchuk 
  Henry Pijffers 
  Tiago Saboga
  Andrzej Sawuła
  Benjamin Siband
  Martin Wunderlich

Dále přispěli
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary
  Vito Smolej (manažer dokumentace)
  Samuel Murray
  Marc Prior (manažer lokalizací)
  a mnoho, mnoho dalších nápomocných lidí

(Pokud si myslíte, že jste významně přispěli k projektu OmegaT,
ale své jméno nevidíte v těchto záznamech, klidně nás kontaktujte.)

OmegaT používá následující knihovny:

  HTMLParser od autorů Somik Raha, Derrick Oswald a další (LGPL Licence).
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter 1.0.8 od Steve Roy (LGPL Licence).
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework od VLSolutions (CeCILL Licence).
  http://www.vlsolutions.com/en/products/docking/

  Hunspell 1.1.12 autorů László Németh a dalších (LGPL Licence).

  JNA dodali Todd Fast, Timothy Wall a dalších (LGPL Licence)

  Swing-Layout 1.0.2 (LGPL License)

  Backport-util-concurrent (Public Domain)

  Retroweaver 2.0.1 (Retroweaver License)

  Jmyspell 2.1.4 (LGPL License)

==============================================================================
 5.  Máte s OmegaT problémy? Potřebujete pomoc?

Než ohlásíte jakoukoliv chybu se ujistěte, že jste si důkladně prošli dokumentaci. To co vidíte může být vlastností OmegaT, kterou jste právě objevili. Když navštívíte log OmegaT a vidíte slova jako 
„Error“ (Chyba), „Warning“ (Upozornění), „Exception“ (Výjimka) nebo „died unexpectedly“ (neočekávané ukončení), tak jste pravděpodobně narazili na opravdový problém (soubor log.txt se nachází v adresáři předvoleb uživatele, jeho umístění naleznete v příručce).

Další věc, kterou učiníte, je ověřit si to co jste nalezli i u ostatních uživatelů, aby jste se ujistili, zda to samé už někdy nebylo hlášeno. Můžete si to také ověřit na stránce pro hlášení chyb na SourceForge. Jedině pokud jste si jisti, že jste první, kdo našel nějakou
zopakovatelnou sekvenci událostí, která spustila něco, co se nemělo stát,
teprve pak byste měli podat hlášení o chybě.

Každé dobré hlášení o chybě potřebuje přesně tři věci.
  - Kroky, které je nutno zopakovat,
  - co jste čekali, že uvidíte, a
  - co jste uviděli místo toho.

Můžete přidat kopie souborů, části logu, snímky obrazovky, prostě cokoliv, o čem si myslíte,
že pomůže vývojářům nalézt a opravit vámi hlášenou chybu.

Archívy uživatelské skupiny můžete prohlížet na adrese:
     http://groups.yahoo.com/group/OmegaT/

Prohlížet stránku hlášení o chybách a v případě potřeby přidat nové hlášení můžete zde:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Abyste byli informování o tom, co se děje s vaším hlášením o chybě, můžete se zaregistrovat jako uživatel Source Forge.

==============================================================================
6.   Podrobnosti k vydání

Podrobné informace o změnách v tomto a všech předcházejících vydáních naleznete v souborech 'changes.txt'.


==============================================================================