 Ta prevod je opravil Vito Smolej, Copyright © 2009.


==============================================================================
  OmegaT 2.0, Read Me file

  1.  Informacija o OmegaT
  2.  Kaj je OmegaT?
  3.  Kako namestiti OmegaT
  4.  Kako lahko prispevate k OmegaT
  5.  Vas OmegaT spravlja ob živce?  Potrebujete pomoč?
  6.  Podatki o izdajah


==============================================================================
  1.  Informacija o OmegaT


Najnovejše informacije o OmegaT lahko dobite na:
      http://www.omegat.org/

Podpora uporabnikov, na Yahoo, kjer lahko brskate po arhivu, tudi če niste prijavljeni kot član:
     http://groups.yahoo.com/group/OmegaT/

Dodatne funkcije lahko zahtevate (v angleščini) na strani SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Napake sporočajte (v angleščini) na stran SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350


==============================================================================
  2.  Kaj je OmegaT?

OmegaT je orodje za računalniško podprto prevajanje. 
Z OmegaT lahko svobodno razpolagate, v smislu, da ga lahko uporabljate brezplačno, tudi če gre za poslovno uporabo, in v smislu, da ga lahko po svoji volji spreminjate in/ali delite naprej, pod pogojem, da pri tem spoštujete pogoje uporabniške licence

Glavne poteze OmegaT so:
  - uporablja se ga lahko na kateremkoli operacijskem sistemu, ki podpira Javo
  - za referenco pri prevajanju uporablja kakršnokoli veljavno datoteko TMX
  - za segmentiranje stavkov uporablja prilagodljivo metodo (ki je podobna metodi SRX)
  - išče po projektu in po referenčnih prevodnih spominih
  - išče po datotekah s podprtim formatom v poljubnem podimeniku 
  - uporablja mehko ujemanje
  - obravnava na pameten način projekte, ki lahko vključujejo tudi hierarhije z zapletenimi podimeniki 
  - podpira geslovnike (za preverjanje terminologije)
  - vsebuje lahko razumljivo dokumentacijo in navodila 
   - OmegaT je lokaliziran v vrsto jezikov.

 OmegaT sama od sebe podpira prevajanje datotek v naslednjih formatih:
  - enostavno besedilo
  - HTML in XHTML
  - datoteke za HTML Help Compiler
  - OpenDocument/OpenOffice.org
  - programski svežnji za Javo(.properties)
  - datoteke - INI (datoteke s pari ključ=vrednost za poljubna kodiranja)
  - datoteke PO
  - datotečni format za dokumentacijo - DocBook
  - Microsoft OpenXML files
  - Okapi enojezični XLIFF

OmegaT je mogoče prirediti tudi za druge vrste datotek.

OmegaT bo avtomatično razčlenil še tako zapletene hierarhije imenikov z izvornimi datotekami in bo za ciljne datoteke ustvaril natančno isto strukturo podimenikov, vključno s kopijami datotek, ki niso podprte. 

Za hiter začetek poženite OmegaT in preberite Začetna navodila, ki se vam prikažejo.

Priročnik za uporabnika se nahaja v paketu, ki ste ga ravnokar prenesli, na razpolago vam je ob zagonu OmegaT prek menija / pomoč.


==============================================================================
 3. Kako namestiti OmegaT

3.1 Splošno
Da OmegaT lahko deluje, mora na vašem sistemu biti nameščeno okolje za Javo, JRE verzija 1.4 ali več. OmegaT v standardni inačici že vsebuje Java Runtime Environment, tako da ga uporabniku ni več treba iskati, nalagati in nameščati. 

Uporabniki Windows in Linux: če ste prepričani, da se na vašem sistemu ustrezni JRE že nahaja, lahko naložite OmegaT brez JRE (instalacijo prepoznate po imenu, ki vsebuje oznako "Without_JRE"). 
V kolikor niste čisto prepričani, vam priporočamo uporabo verzije "standard" . t.j. vključno z JRE. Tako ste na varnem tudi v primeru. če se na vašem računalniku JRE že nahaja, saj se verziji med seboj ne bosta motili.

Uporabniki Linux: vedite, da OmegaT ne bo delala s prostimi ali odprtokodnimi implementacijami Jave, ki jih je najti v številnih distribucijah Linuxa (kot je na primer Ubuntu), ker so ali zastarele ali nepopolne.  Naložite z naslova zgoraj Java Runtime Environment (JRE) podjetja Sun in jo namestite, ali pa naložite in namestite verzijo OmegaT, ki vsebuje JRE (.tar.gz datoteka z oznako "Linux").

Uporabniki Mac: na Mac OS X je Java že nameščena.

Linux na sistemih PowerPC: priskrbeti si morate JRE podjetja IBM, ker SUN Jave za PPC sisteme ne podpira. Prenesete ga lahko z naslova:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 

32. Namestitev
* uporabniki Windows: poženite enostavno namestitveni program. Če želite, vam namestitveni program tudi pripravi bližnjice za OmegaT.
* drugi: za namestitev OmegaT si morate samo ustvariti ustrezno mapo za OmegaT (npr. /usr/local/lib v Linuxu). Prekopirajte arhiv OmegaT zip ali tar.gz 
v to mapo in ga tu razpakirajte. 

3.3 Zagon OmegaT
OmegaT lahko zaženete na celo vrsto načinov.

* uporabniki Windows: by double-clicking on the file OmegaT.exe. Če v eksplorerju vidite OmegaT, datoteke OmegaT.exe pa ne, popravite nastavitve tako, da bo videti pripone datotek.  

* dvokliknite na datoteko OmegaT.jar. 
To bo šlo samo v primeru, če je na vašem sistemu pripona .jar povezana z Javo.

* Z ukazno vrstico. Ukaz, s katerim se požene OmegaT, je:

cd <mapa, v kateri se nahaja datoteka OmegaT.jar>

<pot in ime programa za izvajanje Jave> -jar OmegaT.jar

(Datoteka, s katero se Java izvaja, je pod Linux datoteka Java in pod Windows java.exe.

Če je Java nameščena in konfigurirana na nivoju sistema, polna pot ni potrebna.)

* uporabniki Windows: program za namestitev lahko pripravi bližnjice na namizju in na območju za 'Hitri zagon'. Lahko tudi povlečete datoteo OmegaT.exe na omizje ali območje za hitri zagon ter v začetni meni, da vam bo OmegaT tam na razpolago.

* uporabniki Linux KDE: OmegaT lahko svojim menijem dodate kot sledi:

Kontrolni center - namizje - paneli - meniji - uredi K meni - datoteka - nov element/nov podmeni

Ko ste izbrali ustrezen podmeni, mu dodajte podmeni / element z datoteka - nov podmeni ali datoteka - nov element. Za ime nove postavke vnesite OmegaT.

V polju "Ukaz" poiščite z gumbom za navigacijo vaš skript za zagon OmegaT in ga izberite. 

Kliknite na ikono (desno od polj za ime/opis/komentar) - druge ikone - brskaj, in zakrmarite do podimenika /images v mapi za aplikacijo OmegaT. Izberite ikono OmegaT.png.

Shranite spremembe z datoteka - shrani.

* uporabniki Linux GNOME: OmegaT lahko svojemu panoju (vrstici vrh zaslona) dodate kot sledi:

Desnokliknite na pano - dodaj nov zagon. Vnesite "OmegaT" v polje za ime; V polju "Ukaz" poiščite z gumbom za navigacijo svoj skript za zagon OmegaT. Izberite ga. in kliknite V redu.


==============================================================================
 4. Kako lahko prispevate k projektu OmegaT

Že želite sodelovati v razvoju OmegaT, se obrnite na razvijalce pri:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Če ste pripravljeni prevesti uporabniški vmesnik za OmegaT, priročnik za uporabo ali druge dokumente o OmegaT, preberite:
      
      http://www.omegat.org/en/translation-info.html

Na seznam prevajalcev se prijavite takole:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Za druge vrste sodelovanja se najprej prijavite v uporabniško skupino::
      http://tech.groups.yahoo.com/group/omegat/

 kjer boste lahko dobili občutek, kako se pri OmegaT svet vrti...

  OmegaT je prvotno delo Keitha Godfreya.
  Marc Prior je koordinator projekta OmegaT.

Doslej so k projektu prispevali:
(v abecednem vrstnem redu):)

Kodo so prispevali
  Zoltan Bartko
  Didier Briel (odgovoren za verzije)
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
  Andrzej Sawula
  Benjamin Siband
  Martin Wunderlich

Na druge načine so prispevali tudi
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary
  Vito Smolej (odgovoren za dokumentacijo)
  Samuel Murray
  Marc Prior (odgovoren za lokalizacijo)
  in še mnogo, mnogo drugih, zelo koristnih ljudi

(Če ste mnenja, da ste k projektu OmegaT Project pomembno prispevali, pa na seznamu svojega imena ne najdete, se prosimo obrnite na nas.)

OmegaT uporablja naslednje knjižnice:

  HTMLParser - avtorji Somik Raha, Derrick Oswald in drugi (LGPL licenca).
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter 1.0.8 - avtor Steve Roy (LGPL licenca).
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework 2.1.4 - VLSolutions (CeCILL licenca).
  http://www.vlsolutions.com/en/products/docking/

  Hunspell László Németh in drugi (LGPL License)

  JNA - avtor Todd Fast, Timothy Wall in drugi (LGPL licenca)

  Swing-Layout 1.0.2 (LGPL licenca)

  Jmyspell 2.1.4 (LGPL licenca)

  JAXB 2.1.7 (GPLv2 + classpath exception)


==============================================================================
 5.  Vas OmegaT spravlja ob živce?  Potrebujete pomoč?

Preden pošljete sporočilo o napaki, se prepričajte, da ste temeljito preverili dokumentacijo.  Lahko da boste tako odkrili kako lastnost OmegaT, ki vam je bila doslej skrita. Če pri pregledu log datoteke OmegaT log naletite na besede,kot so "Error", "Warning", "Exception", ali pa "died unexpectedly", potem ste lahko da naleteli na kaj resnega (datoteka log.txt se nahaja v mapi z uporabnikovimi nastavitvami, poglejte v priročnik za natančnejšo lokacijo.


Kot naslednje lahko preverite odkrito nepravilnost z drugimi uporabniki in se prepričate, da napake še ni bila javljena. Ogledate si lahko tudi stran s poročili o napakah na SourceForge. Sporočilo o napaki pošljite samo, če ste prepričani, da ste kot prvi odkrili ponovljivo zaporedje korakov, ki vodijo do nezaželenih oziroma nepričakovanih posledic.


Dobro poročilo o napaki vedno vsebuje tri stvari.
  - korake, ki do napake vodijo,
  - kaj je bilo pričakovati, da se pripeti, in
  - kaj se je v resnici dogodilo.


Priložite lahko kopije datotek, izreze iz log datoteke, posnetke zaslona, vse, kar mislite, da bo razvijalcem pomagalo napako najti in popraviti.


Po arhivih uporabniške skupine lahko brskate na naslednjem naslovu:
     http://groups.yahoo.com/group/OmegaT/

Naslov za brskanje po poročilih o napakah in za prijavo novih napak: :
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Včlanite se v Source Forge, da boste lahko zasledovali, kaj se z vašim poročilom o napaki dogaja

==============================================================================
6.   Podatki o izdajah

Podrobne podatke o spremembah v tej in v prejšnjih izdajah najdete v datoteki 'changes.txt'.

==============================================================================
