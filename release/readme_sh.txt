Ovaj prevod sačinio je Dragomir Kovačević, copyright© 2008.

==============================================================================
  OmegaT 1.8.0, datoteka Read Me

  1.  Obavještenja o OmegaT
  2.  Šta je OmegaT?
  3.  Način instalacije OmegaT
  4.  Doprinos razvoju OmegaT
  5.  Da li vas OmegaT zamara zbog bug-ova? Treba li vam pomoć?
  6.  Detalji o ovoj verziji

==============================================================================
  1.  Obavještenja o OmegaT


Najaktuelnija obavještenja o OmegaT mogu se naći na:
      http://www.omegat.org/

User support, na višejezičnoj Yahoo mailing listi korisnika, na kojoj se njena arhiva može pretraživati i bez obaveze upisa na listu:
     http://groups.yahoo.com/group/OmegaT/

Zahtjevi za poboljšanja (na engleskom), na web sajtu SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Izvještaji o bug-ovima, (na engleskom), na web sajtu SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Šta je OmegaT?

OmegaT predstavlja alatku za kompjuterski potpomagano prevođenje (CAT). Ona je slobodna, u smislu da se ne mora platiti da bi se koristila, pa i onda kad se koristi u profesionalne svrhe, te u smislu da je svako može slobodno mijenjati i/ili redistribuirati ali pod uslovom da poštuje korisničku licencu.

Glavne odlike OmegaT su da:
  - funkcioniše na bilo kom operativnom sistemu koji podnosi Java-u
  - koristi bilo kakve valjane TMX datoteke radi pomoći u prevođenju
  - posjeduje mogućnost fleksibilne rečenične segmentacije (putem metoda SRX)
  - pretražuje po projektu i u memorijama prevoda za podršku
  - pretraga datoteka u svim podržanim formatima u bilo kom diretorijumu 
  - koristi tehniku podudarnosti
  - pametno upravlja projektima uključivo i onima sa složenom strukturom direktorijuma
  - podržava rječnike (terminološka provjera)
  - postoji lakoća u razumjevanju dokumentacije i uputstva
  - postoji lokalizacija na veći broj jezika.

OmegaT direktno podržava prevođenje sljedećih tipova datoteka:
  - plain text
  - HTML i XHTML
  - HTML Help Compiler
  - OpenDocument/OpenOffice.org
  - Java resource bundles (.properties)
  - INI datoteke (datoteke sa key=parovi vrijednosti s bilo kojim kodnim rasporedom)
  - PO datoteke
  - DocBook documentation datotečki format
  - datoteke Microsoft OpenXML
  - Okapi monolingual XLIFF datoteke

OmegaT se može modifikovati tako da podržava i dodatne formate datoteka.

OmegaT će automatski pročešljati i najsloženiju hijerarhiju direktorijuma, pristupiti datotekama za koje ima podršku, sačiniti target direktorijum sa identičnom strukturom, pa i sa kopijama tipova datoteka za koje nema podršku.

Ako želite da u kratkom roku sagledate OmegaT, pokrenite je i pročitajte Vodič za brzi početak.

Priručnik za upotrebu nalazi se u paketu koji ste upravo preuzeli s Interneta. Možete mu pristupiti preko menija [Pomoć], pošto pokrenete OmegaT.

==============================================================================
 3. Način instalacije OmegaT

3.1 Opšte napomene
Za funkcionisanje OmegaT, potrebno je da u vašem sistemu postoji instalisano okruženje Java Runtime Environment, verzija 1.4 ili viša. Sada se OmegaT standardno isporučuje s ugrađenim Java Runtime Environment. Na taj način korisnici su pošteđeni svake brige oko izabiranja, pronalaženja i instalacije. 

Za korisnike na Windows-ima i Linux-u: Ako pouzdano znate da u vašem sistemu već postoji neka od pogodnih verzija JRE, možete u tom slučaju, staviti OmegaT bez JRE (ova naznaka postoji i u nazivu same verzije, tj. "Without_JRE"). 
Ako ste u dilemi, savjetujemo vam da koristite "standardnu" verziju, tj. sa JRE. Ovakvo rješenje je sasvim sigurno, jer, čak i da je JRE već instalirana na vašem sistemu, ova verzija njoj neće smetati.

Korisnici na Linux-u: imajte u vidu da OmegaT ne radi pod implementacijama Java free/open-source, kakve se nalaze u mnogima distribucijama Linux-a (na primjer, u Ubuntu), pošto su one ili zastarjele ili nepotpune. Sa gornje poveznice preuzmite i instalirajte Java Runtime Environment (JRE), ili pak, preuzmite i instalirajte OmegaT s ugrađenim JRE (tar.gz bundle pod oznakom "Linux").

Korisnici na Mac-u: JRE je već instalirana u operativnom sistemu Mac OS X.

Linux na sistemima PowerPC: korisnici bi trebalo da preuzmu IBM JRE, pošto kompanija Sun ne proizvodi JRE za sisteme PPC. U ovom slučaju, preuzimajte sa:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 

3.2 Instalacija
* korisnici Windows-a: Jednostavno, samo startujte instalacijski program. Već prema vašoj želji, instalacijski progam može napraviti i poveznice za startovanje OmegaT.
* Ostali korisnici: Radi instalacije OmegaT, samo sačinite pogodan direktorijum za OmegaT 
(na pr., na Linux-u: /usr/local/lib). Ukopirajte OmegaT zip ili tar.gz
arhivu u taj direktorijum i tu je raspakujte.

3.3 Pozivanje OmegaT
OmegaT se može pozvati na više načina.

* Korisnici na Windows-ima: * Duplim klikom na datoteku OmegaT.exe. Ako u vašem File Manager-u (Windows Explorer) vidite datoteku OmegaT, ali ne i OmegaT.exe, omogućite da se u vašem sistemu vide sufiksi datoteka.

* Duplim klikom na datoteku OmegaT.jar. Ovo će imati efekta samo ukoliko je u vašem sistemu, tip datoteke .jar, asociran sa Java-om.

* Sa komandne linije. Komanda za pozivanje OmegaT je:

cd <direktorijum gdje je locirana datoteka OmegaT.jar>

<ime i putanja izvršne datoteke Java> -jar OmegaT.jar

(izvršna datoteka Java u Linux-u je datoteka java, a na Windows-ima: java.exe.
Ako je Java instalirana na sistemskom nivou, cijela putanja se i ne mora unositi.)

* Korisnici na Windows-ima: Instalacijski program može napraviti kratice u start meniju, na desktop-u, te u zoni za quick launch. Za datoteku OmegaT.exe možete načiniti link na desktop, Meni Start, ili u zoni za quick launch

* Korisnici Linux-a: možete dodati OmegaT vašim menijima na sljedeći način:

Control Center - Desktop - Panels - Menus - Edit K Menu - File - New Item/New 
Submenu.

Pošto ste odabrali pogodan meni, tada možete dodati submenu/item pomoću File - New 
Submenu i File - New Item. Za ime novog "item" stavite OmegaT.

U polju "Command", koristite navigaciono dugme za nalaženje vašeg skripta za pozivanje OmegaT, te ga selektujte. 

Kliknite na dugme ikonice (s desne strane od Name/Description/Comment fields) 
- Other Icons - Browse, te pronađite pod-direktorijum /images subfolder u aplikacijskom direktorijumu OmegaT. Odaberite ikonicu OmegaT.png.

I na kraju, spasite promjene pomoću File - Save.

* korisnici Linux GNOME: OmegaT možete dodati vašem panelu (traka pri vrhu ekrana) na sljedeći način:

Desni-klik na panel - Add New Launcher. Unesite "OmegaT" u polje "Name"; U "Command" field-u, koristite navigaciono dugme za  nalaženje vašeg skripta za pozivanje OmegaT. Selektujte ga i potvrdite sa OK.

==============================================================================
 4. Angažovanje u projektu OmegaT

Radi doprinosa razvoju OmegaT, stupite u vezu sa kolegama koji rade na njenom razvoju, preko:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Ukoliko imate namjeru da prevedete korisničku radnu površinu OmegaT, priručnik za upotrebu ili druge dokumente, pročitajte uputstva na:
      
      http://www.omegat.org/en/translation-info.html

I upišite se na listu njenih prevodilaca:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Ukoliko imate u vidu neke druge vrste doprinosa, upišite se na listu korisnika ovog programa, na:
      http://tech.groups.yahoo.com/group/omegat/

Na taj način upoznaćete se sa zbivanjima u društvenom okruženju OmegaT...

  OmegaT u svom začetku predstavlja originalan rad Keith-a Godfrey-a.
  Marc Prior je koordinator projekta OmegaT.

U kolege koji su ranije dali svoj doprinos razvoju, ubrajaju se:
(po abecednom redu)

Za razvoj koda
  Zoltan Bartko
  Didier Briel (urednik verzija)
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

Ostali doprinosi
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary
  Vito Smolej (urednik dokumentacije)
  Samuel Murray
  Marc Prior (urednik za lokalizaciju)
  i mnogi, mnogi drugi ljudi koji su bili od velike pomoći

(Ako smatrate da je i vaš doprinos Projektu OmegaT bio od značaja, a ne vidite, pak, svoje ime na listi, molimo vas, stupite s nama u vezu oko toga.)

OmegaT koristi sljedeće biblioteke:

  HTML Parser autora Somik Raha, Derrick Oswald-a i drugih (LGPL License).
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter 1.0.8 Steve Roy-a (LGPL License)
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework 2.0.6d od VLSolutions (CeCILL License)
  http://www.vlsolutions.com/en/products/docking/

  Hunspell 1.1.12 od László Németh i ostalih (LGPL License)

  JNA od Todd Fast, Timothy Wall i drugih (LGPL License)

  Swing-Layout 1.0.2 (LGPL License)

  Backport-util-concurrent (Public Domain)

  Retroweaver 2.0.1 (Retroweaver License)

  Jmyspell 2.1.4 (LGPL License)

==============================================================================
 5.  Da li vas OmegaT zamara zbog bug-ova? Treba li vam pomoć?

Prije nego prijavite bug, provjerite da li ste detaljno pregledali dokumentaciju. Ono što zapažate, u stvari može biti neka od odlika OmegaT koju ste upravo otkrili. Ako provjerite log datoteku OmegaT i zapazite riječi "Greška", "Upozorenje", "Izuzetak", ili "iznenadno se prekinula", onda ste na tragu nečemu (log.txt nalazi se u 'user preferences' direktorijumu, radi određivanja njegove pozicije pogledajte u priručnik).

Sljedeće što bi trebalo da uradite je da nalaze potvrdite kroz kontakte sa ostalim korisnicima. Ovo stoga da ne bi prijavljivali nešto o čemu od ranije postoje podaci. Radi provjere, možete posjetiti i stranicu za prijavu bug-ova na SourceForge. Tek kada ste sigurni da ste prvi korisnik koji otkriva neku sekvencu događaja koja se dade reprodukovati a koja je prouzrokovala nepredviđeno ponašanje, trebalo bi da priložite izvještaj.

Svaki valjan izvještaj o bug-u mora ispunjavati tri uslova.
  - Proceduru za njegovo reprodukovanje,
  - Saznanje o onome što se trebalo dobiti određenom komandom, i
  - Šta se s njom doista dobilo.

Takođe možete pridodati i kopije datoteka, dijelove log-a, snimke sa monitora i bilo šta što smatrate da će informatičarima za razvoj pomoći u pronalaženju i popravci nađenog bug-a.

Radi uvida u arhivu mailing liste korisnika, idite na:
     http://groups.yahoo.com/group/OmegaT/

Radi uvida u izvještaje o bug-ovima i prilaganja svog ev. potrebnog bug izvještaja, idite na:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Radi praćenja zbivanja u vezi izvještaja o bug-u, možete se kao korisnik registrovati na Source Forge.

==============================================================================
6.   Detalji o ovoj verziji

Radi detaljnijih obavještenja o promjenama kod ove i svih prethodnih verzija, pogledajte datoteku 'changes.txt'.


==============================================================================