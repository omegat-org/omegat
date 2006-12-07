==============================================================================
  File Leggimi di OmegaT 1.6.1

  1.  Informazioni su OmegaT
  2.  Che cosa è OmegaT?
  3.  Note generali su Java e OmegaT
  4.  Contributi a OmegaT
  5.  OmegaT genera problemi? Si ha necessità di assistenza?
  6.  Informazioni sulla release

==============================================================================
  1.  Informazioni su OmegaT

Le informazioni più aggiornate su OmegaT possono essere reperite accedendo a:
      http://www.omegat.org/omegat/omegat.html

Ulteriori informazioni sono reperibili anche nelle seguenti pagine:

Assistenza all'utente: gruppo di utenti Yahoo:
     http://groups.yahoo.com/group/OmegaT/
     Archivi consultabili senza necessità di iscrizione.

Per le richieste di miglioramenti, accedere al sito SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Per la segnalazione di errori, accedere al sito SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Che cosa è OmegaT?

OmegaT è uno strumento per la traduzione assistita (CAT). È gratuito, nel senso
che per utilizzarlo non viene richiesto alcun pagamento, anche per un uso
professionale, e nel senso che si ha la libertà di modificarlo e/o
ridistribuirlo fintanto che viene rispettata la licenza per l'utente.

Le funzioni principali di OmegaT sono
  - possibilità di "girare" su qualsiasi sistema operativo in grado di gestire
    Java;
  - utilizzo di qualsiasi file TMX valido per avere a disposizione una memoria
    di riferimento;
  - segmentazione flessibile della frase (grazie a un metodo di tipo SRX);
  - ricerche nel progetto e nelle memorie di traduzione di riferimento;
  - ricerche in una qualsiasi directory contenente file leggibili da OmegaT;
  - corrispondenze parziali (fuzzy);
  - semplice gestione dei progetti che prevedono strutture di directory
    particolarmente complesse;
  - gestione dei glossari (ricerca terminologica);
  - documentazione e materiale di addestramento facile da comprendere;
  - localizzazione in più lingue.

OmegaT è in grado di leggere file OpenDocument, file Microsoft Office
(utilizzando OpenOffice.org come filtro di conversione, o tramite conversione
in HTML), file OpenOffice.org o StarOffice e file (X)HTML, Java localization
oppure file di solo testo.

OmegaT analizza automaticamente anche la struttura di directory più complessa
per poter successivamente accedere a tutti i file riconosciuti e generazione
di una struttura di directory di destinazione esattamente uguale a quella di
partenza, comprendente anche le copie di tutti i file non riconosciuti.

Per iniziare a operare subito con OmegaT, avviare il programma e leggere
la "Guida di avvio rapido" che viene proposta.

Il manuale per l'utente si trova nel pacchetto appena scaricato e vi si
potrà accedere dal menu [?] (Guida), dopo aver avviato OmegaT.

==============================================================================
 3. Note generali su Java e OmegaT

OmegaT richiede che sul sistema sia stato installato Java Runtime Environment,
versione 1.4 o superiore. È reperibile accedendo a:
    http://java.com

Se non è ancora presente nel sistema, agli utenti Windows e Linux potrebbe
venire richiesto di installare Java.
Il progetto OmegaT prevede anche una versione che contiene Java. Gli utenti
Mac OSX operano su un sistema sul quale Java è già stato installato.

Dopo aver effettuato una corretta installazione, OmegaT potrà essere avviato
facendo semplicemente doppio clic sul file OmegaT.jar.

Dopo aver installato Java, potrebbe essere necessario modificare la
variabile di percorso del sistema, in modo che questa includa la directory
nella quale si trova l'applicazione "Java".

Gli utenti Linux dovrebbero prestare attenzione al fatto che OmegaT non
sarà in grado di operare con gli sviluppi gratuiti/open-source reperibili
in molti siti per Linux (per esempio, Ubuntu), poiché si tratta di componenti
obsolete, oppure incomplete. Scaricare e installare Java Runtime Envirionment
(JRE) di Sun servendosi del link sopra riportato, oppure scaricare e
installare la versione di OmegaT completa di JRE (il pacchetto .tar.gz
contrassegnato "Linux").

Quando si utilizza Linux su un sistema PowerPC, gli utenti dovrebbero
scaricare JRE di IBM, poiché Sun non mette a disposizione un JRE per
sistemi PPC. Scaricarne uno da:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html

==============================================================================
 4. Contributi a OmegaT

Per contribuire allo sviluppo di OmegaT, mettersi in contatto con gli
sviluppatori a:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Per tradurre l'interfaccia utente di OmegaT, il manuale per l'utente o
altri documenti correlati, leggere:
      http://www.omegat.org/omegat/translation-info.html

e iscriversi all'elenco dei traduttori:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Per altri tipi di contributi, prima iscriversi al gruppo di utenti a:
      http://groups.yahoo.com/group/OmegaT/

Avrete così un'idea di che cosa accade nel mondo di OmegaT...

  OmegaT è un lavoro originale di Keith Godfrey.
  Marc Prior è il coordinatore del progetto OmegaT.

Elenco dei contributi precedenti (in ordine alfabetico)

Contributi sul codice:
  Kim Bruning
  Sacha Chua
  Maxym Mykhalchuk (attuale sviluppatore primario)
  Henry Pijffers
  Benjamin Siband

Contributi per la localizzazione:
  Roberto Argus (Portoghese Brasile)
  Alessandro Cattelan (Italiano)
  Sabine Cretella (Tedesco)
  Suzanne Bolduc (Esperanto)
  Didier Briel (Francese)
  Frederik De Vos (Olandese)
  Cesar Escribano Esteban (Spagnolo)
  Dmitri Gabinski (Bielorusso, Esperanto e Russo)
  Takayuki Hayashi (Giapponese)
  Jean-Christophe Helary (Francese e Giapponese)
  Yutaka Kachi (Giapponese)
  Elina Lagoudaki (Greco)
  Martin Lukac (Slovacco)
  Samuel Murray (Afrikaans)
  Yoshi Nakayama (Giapponese)
  Claudio Nasso (Italiano)
  David Olveira (Portoghese)
  Ronaldo Radunz (Portoghese Brasile)
  Thelma L. Sabim (Portoghese Brasile)
  Juan Salcines (Spagnolo)
  Pablo Roca Santiagio (Spagnolo)
  Karsten Voss (Polacco)
  Gerard van der Weyde (Olandese)
  Martin Wunderlich (Tedesco)
  Hisashi Yanagida (Giapponese)
  Kunihiko Yokota (Giapponese)
  Erhan Yukselci (Turco)

Altri contributi:
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (attuale gestore documentazione)
  Samuel Murray
  Marc Prior (attuale gestore localizzazione)
  e molte, molte altre persone che hanno contribuito.

OmegaT utilizza le seguenti librerie:
  HTMLParser di Somik Raha, Derrick Oswald e altri (Common Public License).
  MRJ Adapter di Steve Roy (LGPL License).
  VLDocking Framework di VLSolutions (CeCILL License).

==============================================================================
 5.  OmegaT genera problemi? Si ha necessità di assistenza?

Prima di segnalare un difetto, accertarsi di aver consultato attentamente
la documentazione. Ciò che viene proposto potrebbe essere una caratteristica
particolare di OmegaT che si è appena scoperta. Se si apre il file di log
di OmegaT, e nello stesso vengono riportate parole come "Error", "Warning",
"Exception" oppure "died unexpectedly", allora si sarà in presenza di un
errore (il file log.txt viene memorizzato nella directory delle preferenze
dell'utente; per il percorso di memorizzazione si rimanda al manuale).

La successiva cosa da fare è di chiedere conferma di quanto è accaduto
agli altri utenti, in modo da verificare se si tratta di un problema già
segnalato. Si può anche accedere alla pagina di riferimento degli
errori di SourceForge. Inviare una segnalazione di errore solo quando si
è sicuri di essere stati i primi ad aver rilevato una sequenza
riproducibile di un evento che ha generato un qualche cosa che non sarebbe
dovuto accadere.

Una qualsiasi segnalazione di errore dovrebbe prevedere tre elementi:
  - sequenza operativa da riprodurre;
  - che cosa ci si aspettava di ottenere, e
  - che cosa, invece, si è ottenuto.

Si possono allegare copie di file, parti del file di log, schermate e
qualsiasi cosa si ritenga possa essere d'aiuto agli sviluppatori
per il reperimento e la correzione dell'errore.

Per accedere agli archivi del gruppo di utenti:
     http://groups.yahoo.com/group/OmegaT/

Per accedere alla pagina delle segnalazioni degli errori e registrare
una nuova segnalazione di errore:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Per seguire il corso degli eventi relativi a una segnalazione
di errore ci si dovrebbe iscrivere come utente Source Forge.

==============================================================================
6.   Informazioni sulla release

Per informazioni particolareggiate relative a questa e a tutte le release
precedenti, si veda il file "changes.txt".

Formati di file riconosciuti:
  - File di solo testo
  - File HTML e XHTML
  - File Compilatore Help HTML (HCC)
  - File OpenDocument / OpenOffice.org
  - File Java resource bundles (.properties)
  - File INI (file con schema "codice" = "valore" in una qualsiasi codifica)
  - File PO
  - File in formato documentazione DocBook

Modifiche sostanziali:
  - Segmentazione flessibile (per frase)
  - Possibilità di creare filtri di formato file sotto forma di plug-in
  - Codici rielaborati con ulteriori commenti
  - Installatore Windows
  - Traducibilità degli attributi dei tag HTML
  - Piena compatibilità con TMX 1.1-1.4b Livello 1
  - Gestione parziale di TMX 1.4b Livello 2

Nuove caratteristiche della UI (rispetto alla serie OmegaT 1.4):
  - Interfaccia della funzione di ricerca riscritta, con funzionalità avanzata
  - Interfaccia principale migliorata, con adozione di finestre posizionabili

==============================================================================

