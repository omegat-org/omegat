Ovaj prevod sačinio je Dragomir Kovačević, copyright© 2006-2007-2008.

==============================================================================
  OmegaT 1.7.3, datoteka Read Me

  1.  Obavještenja o OmegaT
  2.  Šta je OmegaT?
  3.  Opšte napomene o Java & OmegaT
  4.  Doprinos razvoju OmegaT
  5.  Da li te OmegaT zamara zbog bug-ova? Treba li ti pomoć?
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

OmegaT predstavlja alatku za kompjuterski potpomagano prevođenje (CAT). Ona je besplatna, u smislu da se ne mora platiti da bi se koristila, pa i onda kad se koristi u profesionalne svrhe, te u smislu da je svako može slobodno mijenjati i/ili redistribuirati ali pod uslovom da poštuje korisničku licencu.

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

OmegaT se može modifikovati ako da podržava i dodatne formate datoteka.

OmegaT će automatski pročešljati i najsloženiju hijerarhiju direktorijuma, pristupiti datotekama za koje ima podršku, sačiniti target direktorijum sa identičnom strukturom, pa i sa kopijama tipova datoteka za koje nema podršku.

Ako želiš da u kratkom roku sagledaš OmegaT, pokreni je i pročitaj Vodič za brzi početak.

Priručnik za upotrebu nalazi se u paketu koji si upravo preuzeo/la s Interneta. Možeš mu pristupiti preko menija [Pomoć] pošto pokreneš OmegaT.

==============================================================================
 3. Način instalacije OmegaT

3.1 Opšte napomene
Za funkcionisanje OmegaT, potrebno je da u tvom sistemu postoji instalisano okruženje Java Runtime Environment, verzija 1.4 ili viša. Sada se OmegaT standardno isporučuje s ugrađenim Java Runtime Environment. Na taj način korisnici su pošteđeni svake brige oko izabiranja, pronalaženja i instalacije. 

Za korisnike na Windows-ima i Linux-u: Ako pouzdano znaš da u tvom sistemu već postoji neka od pogodnih verzija JRE, možeš, u tom slučaju, staviti OmegaT bez JRE (ova naznaka postoji i u nazivu same verzije, tj. "Without_JRE"). 
Ako si u dilemi, savjetujemo ti da koristiš "standardnu" verziju, tj. sa JRE. Ovakvo rješenje je sasvim sigurno, jer, čak i da je JRE već instalirana na vašem sistemu, ova verzija njoj neće smetati.

Korisnici na Linux-u: imaj u vidu da OmegaT ne radi pod implementacijama Java free/open-source, kakve se nalaze u mnogima distribucijama Linux-a (na primjer, u Ubuntu), pošto su one ili zastarjele ili nepotpune. Sa gornje poveznice preuzmi i instaliraj Java Runtime Environment (JRE), ili pak, preuzmi i instaliraj OmegaT s ugrađenim JRE (tar.gz bundle pod oznakom "Linux").

Korisnici na Mac-u: JRE je već instalirana u operativnom sistemu Mac OS X.

Linux na sistemima PowerPC: korisnici bi trebalo da preuzmu IBM JRE, pošto kompanija Sun ne proizvodi JRE za sisteme PPC. U ovom slučaju, preuzimaj sa:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 

3.2 Instalacija
Radi instalacije OmegaT, jednostavno napravi neki pogodni direktorijum za OmegaT (na pr. C:\Program 
Files\OmegaT na Windows-ima, ili /usr/local/lib na Linux). Iskopiraj zipovanu arhivu OmegaT u ovaj direktorijum, i tamo je otpakuj.

3.3 Pokretanje OmegaT

3.3.1 Korisnici na Windows-ima

OmegaT se može pozvati na više načina.

* Ukoliko koristiš verziju sa uključenim JRE, duplim klikom na datoteku OmegaT-JRE.exe, ili u suprotnom slučaju, na OmegaT.exe.

* Duplim klikom na datoteku OmegaT.bat. Ako u tvom File Manager-u (Windows Explorer) vidiš datoteku OmegaT, ali ne i OmegaT.bat, omogući da se vide sufiksi datoteka u sistemu.

* Duplim klikom na datoteku OmegaT.jar. Ovo će imati efekta samo ukoliko je u tvom sistemu, tip datoteke .jar, asociran sa Java-om.

* S komandne linije. Komanda za pozivanje OmegaT je:

  cd <direktorijum gdje je locirana datoteka OmegaT.jar>

  <ime i putanja izvršne datoteke Java> -jar OmegaT.jar

(Izvršna datoteka Java je datoteka java.exe.
Ako je Java instalirana na sistemskom nivou, cijela putanja se i ne mora unositi.)

Za datoteke OmegaT-JRE.exe, OmegaT.exe ili OmegaT.bat možeš načiniti link na desktop ili Meni Start.

3.3.2 Korisnici na Linux-u

* S komandne linije:

  cd <direktorijum gdje je locirana datoteka OmegaT.jar>

  <ime i putanja izvršne datoteke Java> -jar OmegaT.jar

(Izvršna datoteka Java je datoteka java. Ako je Java instalirana na sistemskom nivou, cijela putanja se i ne mora unositi.)


3.3.2.1 Korisnici Linux-a KDE:

Možeš dodati OmegaT tvojim menijima na slijedeći način:

Control Center - Desktop - Panels - Menus - Edit K Menu - File - New Item/New 
Submenu.

Pošto si odabrao/la pogodan meni, tada možeš dodati submenu/item pomoću File - New 
Submenu i File - New Item. Za ime novog "item" stavi OmegaT.

U polju "Command", koristi navigaciono dugme za nalaženje tvog skripta za pozivanje OmegaT, te ga selektuj. 

Klikni na dugme ikonice (s desne strane od Name/Description/Comment fields) 
- Other Icons - Browse, te pronađi pod-direktorijum /images subfolder u aplikacijskom direktorijumu OmegaT. Odaberi ikonicu OmegaT.png.

I na kraju, spasi promjene pomoću File - Save.

3.3.2.2 korisnici Linux GNOME

OmegaT možeš dodati tvom panelu (traka pri vrhu ekrana) na slijedeći način:

Desni-klik na panel - Add New Launcher. Unesi "OmegaT" u polje "Name"; U "Command" field-u, koristi navigaciono dugme za  nalaženje tvog skripta za pozivanje OmegaT. Selektuj ga i potvrdi sa OK.

==============================================================================
 4. Angažovanje u projektu OmegaT

Radi doprinosa razvoju OmegaT, stupi u vezu sa kolegama koji rade na njenom razvoju, preko:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Ukoliko imaš namjeru da prevedeš korisničku radnu površinu OmegaT, priručnik za upotrebu ili druge dokumente, pročitaj uputstva na:
      
      http://www.omegat.org/en/translation-info.html

I upiši se na listu njenih prevodilaca:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Ukoliko imaš u vidu neke druge vrste doprinosa, upiši se na listu korisnika ovog programa, na:
      http://tech.groups.yahoo.com/group/omegat/

Na taj način upoznaćeš se sa zbivanjima u društvenom okruženju OmegaT...

  OmegaT u svom začetku predstavlja originalan rad Keith-a Godfrey-a.
  Marc Prior je koordinator projekta OmegaT.

Među kolege koji su ranije dali svoj doprinos razvoju, ubrajaju se:
(po abecednom redu)

Za razvoj koda
  Zoltan Bartko
  Didier Briel (release manager)
  Kim Bruning
  Sacha Chua
  Thomas Huriaux
  Maxym Mykhalchuk
  Henry Pijffers
  Tiago Saboga
  Benjamin Siband
  Martin Wunderlich

Ostali doprinosi
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary
  Vito Smolej (documentation manager)
  Samuel Murray
  Marc Prior (localization manager)
  i mnogi, mnogi drugi ljudi koji su bili od velike pomoći

(Ako smatraš da je i tvoj doprinos Projektu OmegaT bio od značaja, a ne vidiš, pak, svoje ime na listi, molimo te, stupi s nama u vezu oko toga.)

OmegaT koristi sljedeće biblioteke:
  HTML Parser autora Somik Raha, Derrick Oswald-a i drugih (LGPL License).
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter Steve Roy-a (LGPL License).
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework od strane VLSolutions (CeCILL License).
  http://www.vlsolutions.com/en/products/docking/

==============================================================================
 5.  Da li te OmegaT zamara zbog bug-ova? Treba li ti pomoć?

Prije nego prijaviš bug, provjeri da li si detaljno pregledao/la dokumentaciju. Ono što zapažaš, u stvari može biti neka od odlika OmegaT koju si upravo otkrio/la. Ako provjeriš log datoteku OmegaT i zapaziš riječi "Greška", "Upozorenje", "Izuzetak", ili "iznenadno se prekinula", onda si na tragu nečemu (log.txt nalazi se u 'user preferences' direktorijumu, radi određivanja njegove pozicije pogledaj u priručnik).

Sljedeće što bi trebalo da uradiš je da svoje nalaze potvrdiš kroz kontakte sa ostalim korisnicima. Ovo stoga da ne bi prijavljivao/la nešto o čemu od ranije postoje podaci. Radi provjere, možeš posjetiti i stranicu za prijavu bug-ova, na SourceForge. Tek kada si siguran/na da si prvi korisnik koji otkriva neku sekvencu događaja koja se dade reprodukovati a koja je prouzrokovala nepredviđeno ponašanje, trebalo bi da priložiš svoj izvještaj.

Svaki valjan izvještaj o bug-u mora ispunjavati tri uslova.
  - Proceduru za njegovo reprodukovanje,
  - Svijest o onome što se trebalo dobiti određenom komandom, i
  - Šta se s njom doista dobilo.

Takođe možeš pridodati i kopije datoteka, dijelove log-a, snimke sa monitora i bilo šta što smatraš da će informatičarima za razvoj, pomoći u pronalaženju i popravci nađenog bug-a.

Radi uvida u arhivu mailing liste korisnika, idi na:
     http://groups.yahoo.com/group/OmegaT/

Radi uvida u izvještaje o bug-ovima i prilaganja svog ev. potrebnog bug izvještaja, idi na:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Radi praćenja zbivanja u vezi izvještaja o bug-u, možeš se kao korisnik registrovati na Source Forge.

==============================================================================
6.   Detalji o ovoj verziji

Radi detaljnijih obavještenja o promjenama kod ove i svih prethodnih verzija, pogledaj datoteku 'changes.txt'.


==============================================================================

