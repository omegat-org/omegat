==============================================================================
  OmegaT 1.6.1 datoteka Read Me

  1.  Obavještenja o OmegaT
  2.  Šta je OmegaT?
  3.  Opšte napomene o Java & OmegaT
  4.  Doprinos razvoju OmegaT
  5.  Da li te OmegaT zamara zbog bug-ova? Treba li ti pomo??
  6.  Detalji o ovoj verziji

==============================================================================
  1.  Obavještenja o OmegaT

Najaktuelnija obavještenja o OmegaT mogu se na?i na:
      http://www.omegat.org/omegat/omegat.html

Dodatna obavještenja mogu se na?i na sljede?im stranicama:

User support, at the Yahoo user group:
     http://groups.yahoo.com/group/OmegaT/
     Arhiva se može pretraživati i bez obaveze upisa na listu.

Zahtjevi za poboljšanja, na web sajtu SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Izvještaji o bug-ovima, na web sajtu SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Šta je OmegaT?

OmegaT predstavlja alatku za kompjuterski potpomagano prevo?enje. Ona je besplatna, u smislu da se ne mora platiti da bi se koristila, pa i onda kad se koristi u profesionalne svrhe, te u smislu da je svako može slobodno mijenjati i/ili redistribuirati ali pod uslovom da poštuje korisni?ku licencu.

Glavne odlike OmegaT su
  - funkcioniše na bilo kom operativnom sistemu koji podnosi Java-u
  - koristi bilo koje valjane TMX datoteke radi podrške pri prevo?enju
  - fleksibilna re?eni?na segmentacija (putem metoda SRX)
  - traganje po projektu i u memorijama prevoda za podršku
  - traganje u bilo kom direktorijumu u kom postoje datoteke koje OmegaT može o?itavati
  - koriš?enje tehnike podudarnosti
  - pametno upravljanje projektima uklju?ivo i onih sa složenom strukturom direktorijuma
  - podrška za rje?nike (terminološka provjera)
  - lako?a u razumjevanju dokumentacije i uputstva
  - localizacija na ve?i broj jezika.

OmegaT može koristiti datoteke OpenDocument, datoteke Microsoft Office (preko OpenOffice.org kao konverzionog filtra, ili konvertovanjem u HTML),
datoteke OpenOffice.org ili StarOffice, kao i  (X)HTML, datoteke Java localization ili datoteke u prostom tekstu.

OmegaT ?e automatski pro?ešljati i najsloženiju hijerarhiju direktorijuma, pristupiti datotekama za koje ima podršku, sa?initi target direktorijum sa identi?nom strukturom, pa i sa kopijama tipova datoteka za koje nema podršku.

Ako želiš da u kratkom roku sagledaš OmegaT, pokreni je i pro?itaj Vodi? za brzi po?etak.

Priru?nik za upotrebu nalazi se u paketu koji si upravo istovario/la sa Interneta. Možeš mu pristupiti preko menija [Help] pošto pokreneš OmegaT.

==============================================================================
 3. Opšte napomene o Java & OmegaT

Za funkcionisanje OmegaT traži da tvom sistemu bude postoji instalisano okruženje Java Runtime Environment verzija 1.4 ili viša. Može se preuzeti sa:
    http://java.com

Ukoliko to ve? nisu u?inili, korisnici operativnih sistema Windows i Linux mora?e instalisati Java-u.
Projekt OmegaT tako?e nudi i verzije sa uklju?enom Java-om. Korisnici MacOSX na svojim mašinama ve? imaju instalisanu Java-u.

Na pravilno instalisanoj mašini OmegaT se može pokrenuti duplim klikom na datoteku OmegaT.jar.

Po instalaciji java-e može se ukazati potreba za izmjenom varijable sistemske putanje kako bi ona uklju?ila direktorijum u koji je smještena aplikacija 'java'.

Korisnici Linux-a moraju obratiti pažnju na ?injenicu da OmegaT ne?e funkcionisati sa Java implementacijama free/open-source koja postoji u mnogim distribucijama Linux-a (na primjer u Ubuntu), pošto su one ili zastarjele ili su nekompletne. Preko gornjeg linka istovari sa Interneta i instališi Sun's Java Runtime Environment (JRE), ili preuzmi OmegaT sa ugra?enim paketom JRE (the .tar.gz bundle marked "Linux").

Na sistemima Linuxa pod sistemima PowerPC, korisnici ?e morati preuzeti IBM JRE, pošto kompanija Sun ne proizvodi JRE za PPC sisteme. Preuzimanje je mogu?e sa:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html

==============================================================================
 4. Doprinos razvoju OmegaT

Radi doprinosa razvoju OmegaT, stupi u vezu sa kolegama koji rade na njenom razvoju, preko:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Ukoliko imaš namjeru da prevedeš korisni?ki interfejs OmegaT, priru?nik za upotrebu ili druge dokumente, pro?itaj uputstva na:
      http://www.omegat.org/omegat/translation-info.html

I upiši se na listu njenih prevodilaca:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Ukoliko imaš u vidu neke druge vrste doprinosa, upiši se na listu korisnika ovog programa, na:
      http://groups.yahoo.com/group/OmegaT/

Na taj na?in upozna?eš se sa zbivanjima u društvenom okruženju OmegaT...

  OmegaT u svom za?etku predstavlja originalan rad Keith-a Godfrey-a.

Razvoju kodova doprinijeli su:
  Marc Prior je koordinator projekta OmegaT.

Me?u kolege koji su ranije dali svoj doprinos razvoju ubrajaju se:
(po abecednom redu)

Za razvoj koda
  Kim Bruning
  Sacha Chua
  Maxym Mykhalchuk (sadašnji vo?a razvoja)
  Henry Pijffers
  Benjamin Siband

Lokalizaciju su potpomogli
  Roberto Argus (brazilski portugalski)
  Alessandro Cattelan (talijanski)
  Sabine Cretella (njema?ki)
  Suzanne Bolduc (esperanto)
  Didier Briel (francuski)
  Frederik De Vos (holandski)
  Cesar Escribano Esteban (španski)
  Dmitri Gabinski (bjeloruski, esperanto i ruski)
  Takayuki Hayashi (japanski)
  Jean-Christophe Helary (francuski i japanski)
  Yutaka Kachi (japanski)
  Elina Lagoudaki (gr?ki)
  Martin Lukac (slova?ki)
  Samuel Murray (afrikaans)
  Yoshi Nakayama (japanski)
  David Olveira (portugalski)
  Ronaldo Radunz (brazilski portugalski)
  Thelma L. Sabim (brazilski portugalski)
  Juan Salcines (španski)
  Pablo Roca Santiagio (španski)
  Karsten Voss (poljski)
  Gerard van der Weyde (holandski)
  Martin Wunderlich (njema?ki)
  Hisashi Yanagida (japanski)
  Kunihiko Yokota (japanski)
  Erhan Yukselci (turski)

Ostali doprinosi
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (sadašnji vo?a za dokumentaciju)
  Samuel Murray
  Marc Prior (sadašnji vo?a za lokalizaciju)
  i mnogi, mnogi drugi ljudi koji su bili od velike pomo?i

OmegaT koristi sljede?e biblioteke:
  HTML Parser autora Somik Raha, Derrick Oswald-a i drugih (Common Public License).
  MRJ Adapter Steve Roy-a (LGPL License).
  VLDocking Framework od strane VLSolutions (CeCILL License).

==============================================================================
 5.  Da li te OmegaT zamara zbog bug-ova? Treba li ti pomo??

Pre nego prijaviš bug, provjeri da li si detaljno pregledao/la dokumentaciju. Ono što zapažaš, u stvari može biti neka od odlika OmegaT koju si upravo otkrio/la. Ako provjeriš log datoteku OmegaT i zapaziš rije?i "Greška", "Upozorenje", "Izuzetak", ili "iznenadno se prekinula", onda si na tragu ne?emu (log.txt nalazi se u 'user preferences' direktorijumu, radi odre?ivanja njegove pozicije pogledaj u priru?nik).

Slede?e što bi trebalo da uradiš je da svoje nalaze potvrdiš kroz kontakte sa ostalim korisnicima. Ovo stoga da ne bi prijavljivao/la nešto o ?emu od ranije postoje podaci. Radi provjere možeš posjetiti i stranicu za prijavu bug-ova, na SourceForge. Tek kada si siguran/na da si prvi korisnik koji otkriva neku sekvencu doga?aja koja se dade reprodukovati a koja je prouzrokovala nepredvi?eno ponašanje, trebalo bi da priložiš svoj izvještaj.

Svaki valjan izvještaj o bug-u mora ispunjavati tri uslova
  - Proceduru za njegovo reprodukovanje,
  - Stav o onome što se trebalo dobiti odre?enom komandom, i
  - Šta se s njom doista dobilo.

Tako?e možeš pridodati i kopije datoteka, dijelove log-a, snimke sa monitora, i bilo šta što smatraš da ?e informati?arima za razvoj pomo?i u pronalaženju i popravci na?enog bug-a.

Radi uvida u arhivu liste korisnika, idi na:
     http://groups.yahoo.com/group/OmegaT/

Radi uvida u izvještaje o bug-ovima i prilaganja svog ev. potrebnog bug izvještaja, idi na:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Radi pra?enja zbivanja u vezi izvještaja o bug-u, možeš se kao korisnik registrovati na Source Forge.

==============================================================================
6.   Detalji o ovoj verziji

Radi detaljnijih obavještenja o promjenama kod ove i svih prethodnih verzija, pogledaj datoteku 'changes.txt'.

Podržani formati datoteka obuhvataju:
  - Plain text
  - HTML i XHTML
  - HTML Help Compiler (HCC)
  - OpenDocument / OpenOffice.org
  - Java resource bundles (.properties)
  - INI datoteke (datoteke sa key=value pairs of any encoding)
  PO datoteke
  - DocBook documentation file format

Izmjene u core djelu softvera:
  - Fleksibilna (re?eni?na) segmentacija
  - Formati filtera za datoteke mogu se napraviti u vidu plugins
  - Prera?eni kod sa mnogo više komentara
  - Windows instaler
  - Atributi HTML tagova mogu se prevoditi
  - Puna kompatibilnost sa TMX 1.1-1.4b Level 1
  - Djelimi?na podrška TMX 1.4b Level 2

Nove odlike korisni?kog interfejsa (u odnosu na seriju OmegaT 1.4):
  - Iznova napisan interfejs poja?ane funkcionalnosti
  - Glavni interfejs je poboljšan zahvaljuju?i oknima koji se mogu pripojiti

==============================================================================

