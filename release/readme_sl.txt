  Ta prevod je opravil Vito Smolej, copyright © 2007.

==============================================================================
   BeriMe datoteka OmegaT 1.6.2

  1.  Informacija o OmegaT
  2.  Kaj je OmegaT?
  3.  Splošne opombe o Javi in OmegaT
  4.  Kako lahko pripevate k OmegaT
  5.  Vas OmegaT spravlja ob živce?  Potrebujete pomoč?
  6.  Podatki o izdajah

==============================================================================
  1.  Informacija o OmegaT


Najnovejšo informacijo o OmegaT (v angleščini, slovaščini, 
nizozemščini, portugalščini)  lahko dobite na:
      http://www.omegat.org/omegat/omegat.html

Podpora uporabnikov, v skupini uporabnikov (večjezični) na Yahoo, kjer lahko brskate po arhivu, tudi če niste prijavljeni kot član:
     http://groups.yahoo.com/group/OmegaT/

Zahteve za izboljšave lahko predlagate (v angleščini) na strani SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Napake sporočajte (v angleščini) na stran SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

==============================================================================
  2.  Kaj je OmegaT?

OmegaT je orodje za računalniško podprto prevajanje. Z OmegaT lahko svobodno razpolagate, v smislu, da ga lahko uporabljatebrezplačno, tudi če gre za poslovno uporabo, in v smislu, da ga lahko po +svoji volj spreminjate in/ali delite naprej, pod pogojem, da pri tem spoštujete pogoje uporabniške licence

Glavne poteze OmegaT so
  - uporablja se ga lahzko na kateremkoli operacijskem sistemu, ki podpira  Javo
  - za referenco pri prevajanju uporablja kakršnokoli veljavno datoteko TMX
  - za segmentiranje stavkov uporablja prilagodljivo metodo (ki je podobna metodi SRX)
  - išče po projektu iin po referenčnih prevodnioh spominih
  - išče po kateremkoli imeniku, ki vsebuje za Omega čitljive datoteke
  - uporablja mehko ujemanje
  - obravnava na pameten način projekte, ki lahko vključujejo tudi hierarhije z zapletenimi podimeniki 
  - podpira slovarje (preverjanje terminologije)
  - vsebuje lahko razumljivo dokumentacijo in navodila 
   - OmegaT je lokaliziran v vrsto jezikov.

OmegaT podpira datoteke OpenDocument, datoteke Microsoft Office files (pri čemer uporablja OpenOffice.org kot pretvorni filter, ali pa prek pretvorbe v HTML),datoteke OpenOffice.org ali StarOffice, in tudi datoteke (X)HTML, datoteke za lokalizacijo Jave in navsezadnje datoteke z enostavnimi besedili.

OmegaT bo avtomatično razčlenil še tako zapletene hierarhije 
imenikov, v katerih se nahajajo izvorne datoteke, in ustvaril ciljne imenike z 
natančno isto strukturo, vključno s kopijami datotek, ki niso podprte. 

Za hiter začetek, poženite OmegaT in preberite Začetna navodila, ki se vam prikažejo.

The user manual is in the package you just downloaded, you can access it from
the [Help] menu after starting OmegaT.

==============================================================================
 3. Splošne opombe o Javi in OmegaT

Da OmegaT lahko deluje, mora na vašem sistemu biti nameščeno okolje za Javo, JRE verzija 1.4 ali več. Okolje je na razpolago na spletu:
    http://java.com

Uporabniki Windows in Linux go bodo morali namestiti, v kolikor ga na njihovih sistemih še ni.
Projekt OmegaT nudi tudi verzije, v katerih je Java vključena. Uporabnikom MacOSX to ni treba, ker je na njihovih sistemih Java že nameščena.

Če je namestitev potekla v redu, zadošča za zagon OmegaT, da dvokliknete na datoteko OmegaT.jar

Po namestitvi boste mogoče morali prirediti spremenljivko, ki navaja
sistemko pot na vašem računalniku, tako da bo vsebovala tudi imenik, kjer se nahaja
aplikacija 'java'.

Uporabniki Linuxa se morajo zavedati, da OmegaT ne bo delala s 
prostimi ali open-source implementacijami Jave, ki jih je najti v številnih distribucijah
Linuxa (kot je na primer Ubuntu), ker so ali zastarele ali nepopolne.  Naložite in instalirajte z naslova zgoraj Java Runtime Environment (JRE) podjetja Sun, ali pa naloćite in instalirajte verzijo OmegaT, ki vsebuje JRE  (.tar.gz datoteka z oznako "Linux").

Uporabniki, ki delajo z  Linuxom na sistemih PowerPC, si morajo preskrbetiJRE firme IBM, ker SUN Jave za PPC sisteme ne podpira. Prenesete ga lahko z naslova:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html

==============================================================================
 4. Kako lahko pripevate k OmegaT

Že želite sodelovati v razvoju OmegaT, se obrnite na razvijalce pri
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Če ste pripravljeni prevesti uporabniški vmesnik za OmegaT, priročnik za uporabo ali druge dokumente o OmegaT, preberite:
      http://www.omegat.org/omegat/omegat_en/translation-info.html

Na  seznam prevajalcev se prijavite takole:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Za druge vrste sodelovanja se najprej prijavite v uporabniško skupino::
      http://tech.groups.yahoo.com/group/omegat/

 kjer boste dobili občutek, kako se pri OmegaT svet vrti...

  OmegaT je provtno delo Keitha Godfreya.
  Marc Prior je koordinator projekta  OmegaT.

Doslej so k projektu prispevali (v abecednem vrstnem redu):)

Kodo so prispevali
  Didier Briel
   Kim Bruning
  Sacha Chua
  Thomas Huriaux
  Maxym Mykhalchuk (glavni razvijalec)
  Henry Pijffers (odgovorni za verzije)
  Benjamin Siband
  Martin Wunderlich

Lokalizacijo so prispevali
  Roberto Argus (brazilska portugalščina)
  Alessandro Cattelan (italijanščina)
  Sabine Cretella (nemščina)
  Suzanne Bolduc (esperanto)
  Didier Briel (francoščina)
  Frederik De Vos (nizozemščina)
  Cesar Escribano Esteban (španščina)
  Dmitri Gabinski (beloruščina, esperanto, in ruščina)
  Takayuki Hayashi (japonščina)
  Jean-Christophe Helary (francoščina in japonščina)
  Yutaka Kachi (japonščina)Vito Smolej (slovenščina)
  Elina Lagoudaki (grščina)
  Martin Lukáč (Slovak)
  Samuel Murray (afrikanščina)
  Yoshi Nakayama (japonščina)
  David Olveira (portugalščina)
  Ronaldo Radunz (brazilska portugalščina)
  Thelma L. Sabim  (brazilska portugalščina)
  Juan Salcines (španščina)
  Pablo Roca Santiagio (španščina) 
  Karsten Voss (polščina) 
  Gerard van der Weyde (nizozemščina) )
  Martin Wunderlich  (nemščina)
  Hisashi Yanagida (japonščina)
  Kunihiko Yokota (japonščina)
  Erhan Yükselci (Turkish)
  Dragomir Kovacevic (srbohrvaščina)
  Claudio Nasso (italijanščina)
  Ahmet Murati (albanščina)
  Sonja Tomaskovic (German)

Na druge načine so prispevali tudi
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (trenutno odgovoren za dokumentacijo)
  Samuel Murray
  Marc Prior (trenutno odgovoren za lokalizacijo)
  in še mnogo, mnogo drugih, zelo koristnih ljudi

(Če ste mnenja, da ste k projektu OmegaT Project pombmno prispevali, pa na seznamu svojega imena ne najdete, se prosimo obrnite na nas.)

OmegaT uporablja naslednje knjižnice:
  HTMLParser - avtorji Somik Raha, Derrick Oswald in drugi (LGPL licenca).
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter - avtor Steve Roy (LGPL licenca).
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework - VLSolutions (CeCILL Licenca).
  http://www.vlsolutions.com/en/products/docking/

==============================================================================
 5.  Vas OmegaT spravlja ob živce?  Potrebujete pomoč?

Preden pošljete sporočilo o napaki, se prepričajte, da ste
temeljito preverili dokumentacijo.  Lahko da boste tako odkrili kako lastnost OmegaT,
ki vam je bila doslej skrita. Če pri pregledu log datoteke OmegaT log naletite na besede,
kot so "Error", "Warning", "Exception", ali pa "died unexpectedly", potem ste znabiti 
naleteli na kaj resnega (datoteka log.txt se nahaja v imeniku z uporabnikovimi
nastavitvami, poglejte v priročnik za natančnejšo lokacijo..

Kot naslednje lahko preverite odkrito nepravilnost  z drugimi uporabniki
in se prepričate, da napake še ni bila javljena. Ogledate si lahko tudi stran s poročili o napakah na SourceForge. Sporočilo o napaki pošljite samo, če ste prepričani, da ste
kot prvi odkrili ponovljivo zaporedje korakov, ki vodijo do nezaželjenh oziroma
nepričakovanih posledic.

Dobro poročilo o napaki vedno vsebuje tri stvari.
  - korake, ki do napake vodijo,
  - kaj je bilo pričakovati, da se pripeti, in
  - kaj se je v resnici dogodilo.

Priložite lahko kopije datotek, izreze iz log datoteke, posnetke zaslona, vse, kar mislite, da bo razvijalcem pomagalo napako najti in popraviti.

Po arhivih uporabniške skupine lahko brskate na naslednjem naslovu:
     http://groups.yahoo.com/group/OmegaT/

Naslov za  brskanje po poročilih o napakah in za prijavo novih napak: :
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Včlanite se v  Source Forge, da boste lahko zasledovali, kaj se
z vašim poročilom o napaki dogaja

==============================================================================
6.   Podatki o izdajah

Podrobne podatke o spremembah v tej in v prejšnjih izdajah najdete
v datoteki  'changes.txt'.

Podprti formati datotek:
  - enostavno besedilo
  - HTML in XHTML
  HTML Help Compiler (HCC)
  - OpenDocument / OpenOffice.org
  - programski svežnji za Javo(.properties)
  datoteke - INI (datoteke s pari ključ=vrednost za poljubna kodiranja)
  - datoteke PO
  datotečni format za dokumentacijo - DocBook
  - Microsoft OpenXML files

Spremembe jedra:
  - fleksibilna  (stavčna) segmentacija
  - filtre za datotečne formate je mogoče ustvarjati v obliki vtičnikov 
  - kodarefaktorirana, z dodatnimi komentarji 
  - program za nameščanje pod Windows
  - prilastki oznak HTML so prevedljivi
  - polna kompatibilnost z TMX 1.1-1.4b Level 1
  - delna podpora za TMX 1.4b Level 2

novosti v uporabniškem vmesniku (v primerjavi s serijo 1.4 OmegaT):
  - vmesnik za iskanje napisan nanovo, z dodatno funkcionalnostjo 
  - glavni vmesnik izboljšan, s pomočjo plavajočih oken

==============================================================================

