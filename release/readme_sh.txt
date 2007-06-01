Ovaj prevod sačinio je Dragomir Kovačević, copyright© 2007.

==============================================================================
  OmegaT 1.6.2, datoteka Read Me

  1.  Obavještenja o OmegaT
  2.  Šta je OmegaT?
  3.  Opšte napomene o Java & OmegaT
  4.  Doprinos razvoju OmegaT
  5.  Da li te OmegaT zamara zbog bug-ova? Treba li ti pomoć?
  6.  Detalji o ovoj verziji

==============================================================================
  1.  Obavještenja o OmegaT


Najaktuelnija obavještenja o OmegaT na albanskom, engleskom, holandskom, portugalskom, slovačkom, srpskohrvatskom, mogu se naći na:
      http://www.omegat.org/omegat/omegat.html

User support, na višejezičnoj Yahoo mailing listi korisnika, na kojoj se njena arhiva može pretraživati i bez obaveze upisa na listu:
     http://groups.yahoo.com/group/OmegaT/

Zahtjevi za poboljšanja (na engleskom), na web sajtu SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Izvještaji o bug-ovima, (na engleskom), na web sajtu SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Šta je OmegaT?

OmegaT predstavlja alatku za kompjuterski potpomagano prevođenje. Ona je besplatna, u smislu da se ne mora platiti da bi se koristila, pa i onda kad se koristi u profesionalne svrhe, te u smislu da je svako može slobodno mijenjati i/ili redistribuirati ali pod uslovom da poštuje korisničku licencu.

Glavne odlike OmegaT su da
  - funkcioniše na bilo kom operativnom sistemu koji podnosi Java-u
  - radi podrške pri prevođenju koristi bilo kakve valjane TMX datoteke 
  - posjeduje mogućnost fleksibilne rečenične segmentacije (putem metoda SRX)
  - pretražuje po projektu i u memorijama prevoda za podršku
  - pretražuje u bilo kom direktorijumu u kom postoje datoteke koje OmegaT može očitavati
  - koristi tehniku podudarnosti
  - pametno upravlja projektima uključivo i s onim koji imaju složenu strukturu direktorijuma
  - podržava rječnike (terminološka provjera)
  - postoji lakoća u razumjevanju dokumentacije i uputstva
  - postoji lokalizacija na veći broj jezika.

OmegaT može koristiti datoteke OpenDocument, datoteke Microsoft Office (preko OpenOffice.org kao konverzionog filtra, ili konvertovanjem u HTML),
datoteke OpenOffice.org ili StarOffice, kao i  (X)HTML, datoteke Java localization ili datoteke u prostom tekstu, kao i druge.

OmegaT će automatski pročešljati i najsloženiju hijerarhiju direktorijuma, pristupiti datotekama za koje ima podršku, sačiniti target direktorijum sa identičnom strukturom, pa i sa kopijama tipova datoteka za koje nema podršku.

Ako želiš da u kratkom roku sagledaš OmegaT, pokreni je i pročitaj Vodič za brzi početak.

Priručnik za upotrebu nalazi se u paketu koji si upravo preuzeo/la s Interneta. Možeš mu pristupiti preko menija [Pomoć] pošto pokreneš OmegaT.

==============================================================================
 3. Opšte napomene o Java & OmegaT

Za funkcionisanje OmegaT traži da tvom sistemu postoji instalisano okruženje Java Runtime Environment, verzija 1.4 ili viša. Ono se može preuzeti sa:
    http://java.com

Ukoliko to već nisu učinili, korisnici operativnih sistema Windows i Linux moraće instalisati Java-u.
Projekt OmegaT takođe nudi i verzije sa uključenom Java-om. Korisnici MacOSX na svojim mašinama već imaju instalisanu Java-u.

Na pravilno instalisanoj mašini OmegaT se može pokrenuti duplim klikom na datoteku OmegaT.jar.

Po instalaciji java-e, može se ukazati potreba za izmjenom varijable sistemske putanje kako bi ona uključila direktorijum u koji je smještena aplikacija 'java'.

Korisnici Linux-a moraju obratiti pažnju na činjenicu da OmegaT neće funkcionisati sa Java implementacijama free/open-source koja postoji u mnogim distribucijama Linux-a (na primjer u Ubuntu), pošto su one ili zastarjele ili su nekompletne. Preko gornjeg linka preuzmi s Interneta i instališi Sun's Java Runtime Environment (JRE), ili preuzmi OmegaT sa ugrađenim paketom JRE (tar.gz bundle s oznakom "Linux").

Na sistemima Linuxa pod sistemima PowerPC, korisnici će morati preuzeti IBM JRE, pošto kompanija Sun ne proizvodi JRE za PPC sisteme. Preuzimanje je moguće sa:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html

==============================================================================
 4. Doprinos razvoju OmegaT

Radi doprinosa razvoju OmegaT, stupi u vezu sa kolegama koji rade na njenom razvoju, preko:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Ukoliko imaš namjeru da prevedeš korisničku radnu površinu OmegaT, priručnik za upotrebu ili druge dokumente, pročitaj uputstva na:
      http://www.omegat.org/omegat/omegat_en/translation-info.html

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
  Didier Briel
  Kim Bruning
  Sacha Chua
  Thomas Huriaux
  Maxym Mykhalchuk (vođa razvoja)
  Henry Pijffers (urednik verzija)
  Benjamin Siband
  Martin Wunderlich

Lokalizaciju su potpomogli
  Roberto Argus (brazilski portugalski)
  Alessandro Cattelan (talijanski)
  Sabine Cretella (njemački)
  Suzanne Bolduc (esperanto)
  Didier Briel (francuski)
  Frederik De Vos (holandski)
  Cesar Escribano Esteban (španski)
  Dmitri Gabinski (bjeloruski, esperanto i ruski)
  Takayuki Hayashi (japanski)
  Jean-Christophe Helary (francuski i japanski)
  Yutaka Kachi (japanski)
  Elina Lagoudaki (grčki)
  Martin Lukáč (slovački)
  Samuel Murray (afrikaans)
  Yoshi Nakayama (japanski)
  David Olveira (portugalski)
  Ronaldo Radunz (brazilski portugalski)
  Thelma L. Sabim (brazilski portugalski)
  Juan Salcines (španski)
  Pablo Roca Santiagio (španski)
  Karsten Voss (poljski)
  Gerard van der Weyde (holandski)
  Martin Wunderlich (njemački)
  Hisashi Yanagida (japanski)
  Kunihiko Yokota (japanski)
  Erhan Yükselci (turski)
  Dragomir Kovačević (srpskohrvatski)
  Claudio Nasso (italijanski)
  Ahmet Murati (albanski)
  Sonja Tomašković (njemački)

Ostali doprinosi
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (sadašnji vođa za dokumentaciju)
  Samuel Murray
  Marc Prior (sadašnji vođa za lokalizaciju)
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
  - Stav o onome što se trebalo dobiti određenom komandom, i
  - Šta se s njom doista dobilo.

Takođe možeš pridodati i kopije datoteka, dijelove log-a, snimke sa monitora, i bilo šta što smatraš da će informatičarima za razvoj, pomoći u pronalaženju i popravci nađenog bug-a.

Radi uvida u arhivu mailing liste korisnika, idi na:
     http://groups.yahoo.com/group/OmegaT/

Radi uvida u izvještaje o bug-ovima i prilaganja svog ev. potrebnog bug izvještaja, idi na:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Radi praćenja zbivanja u vezi izvještaja o bug-u, možeš se kao korisnik registrovati na Source Forge.

==============================================================================
6.   Detalji o ovoj verziji

5.  Da li te OmegaT zamara zbog bug-ova? Treba li ti pomoć?

Prije nego prijaviš bug, provjeri da li si detaljno pregledao/la dokumentaciju. Ono što zapažaš, u stvari može biti neka od odlika OmegaT koju si upravo otkrio/la. Ako provjeriš log datoteku OmegaT i zapaziš riječi "Greška", "Upozorenje", "Izuzetak", ili "iznenadno se prekinula", onda si na tragu nečemu (log.txt nalazi se u 'user preferences' direktorijumu, radi određivanja njegove pozicije pogledaj u priručnik).

 Sljedeće što bi trebalo da uradiš je da svoje nalaze potvrdiš kroz kontakte sa ostalim korisnicima. Ovo stoga da ne bi prijavljivao/la nešto o čemu od ranije postoje podaci. Radi provjere, možeš posjetiti i stranicu za prijavu bug-ova, na SourceForge. Tek kada si siguran/na da si prvi korisnik koji otkriva neku sekvencu događaja koja se dade reprodukovati a koja je prouzrokovala nepredviđeno ponašanje, trebalo bi da priložiš svoj izvještaj.

Svaki valjan izvještaj o bug-u mora ispunjavati tri uslova.
  - Proceduru za njegovo reprodukovanje,
  - Stav o onome što se trebalo dobiti određenom komandom, i
  - Šta se s njom doista dobilo.

Takođe možeš pridodati i kopije datoteka, dijelove log-a, snimke sa monitora, i bilo šta što smatraš da će informatičarima za razvoj, pomoći u pronalaženju i popravci nađenog bug-a.

Radi uvida u arhivu mailing liste korisnika, idi na:
     http://tech.groups.yahoo.com/group/omegat/

Radi uvida u izvještaje o bug-ovima i prilaganja svog ev. potrebnog bug izvještaja, idi na:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Radi praćenja zbivanja u vezi izvještaja o bug-u, možeš se kao korisnik registrovati na Source Forge.

==============================================================================
6.   Detalji o ovoj verziji

Radi detaljnijih obavještenja o promjenama kod ove i svih prethodnih verzija, pogledaj datoteku 'changes.txt'.

Podržani formati datoteka obuhvaćaju:
  - Plain text
  - HTML i XHTML
  - HTML Help Compiler (HCC)
  - OpenDocument / OpenOffice.org
  - Java resource bundles (.properties)
  - INI datoteke (datoteke sa key=parovi vrijednosti s bilo kojim kodnim rasporedom)
  - PO datoteke
  - DocBook documentation datotečki format
  - datoteke Microsoft OpenXML

Izmjene u core djelu softvera:
  - Fleksibilna (rečenična) segmentacija
  - Formati filtera za datoteke mogu se napraviti u vidu plugins
  - Prerađeni kod sa mnogo više komentara
  - Windows instaler
  - Atributi HTML tagova mogu se prevoditi
  - Puna kompatibilnost sa TMX 1.1-1.4b Level 1
  - Djelimična podrška TMX 1.4b Level 2

Nove odlike korisničke radne površine (u odnosu na seriju OmegaT 1.4):
  - Iznova napisana radna površina pojačane funkcionalnosti
  - Glavna radna površina je poboljšana zahvaljujući oknima koji se mogu pripojiti

==============================================================================

