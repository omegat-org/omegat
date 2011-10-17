Questa traduzione è frutto del lavoro di Valter Mura, copyright© 2011.

==============================================================================
  File Leggimi di OmegaT 2.0

  1.  Informazioni su OmegaT
  2.  Che cosa è OmegaT?
  3.  Installazione di OmegaT
  4.  Contributi a OmegaT
  5.  OmegaT genera problemi? Si ha bisogno di assistenza?
  6.  Informazioni sul rilascio

==============================================================================
  1.  Informazioni su OmegaT


Le informazioni più aggiornate su OmegaT possono essere reperite accedendo a:
      http://www.omegat.org/

Assistenza all'utente, nel gruppo utenti di Yahoo (multilingue), in cui è possibile ricercare negli archivi senza necessità di iscrizione:
     http://groups.yahoo.com/group/OmegaT/

Richieste di miglioramenti (in Inglese), nel sito SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Segnalazione errori (in Inglese), nel sito SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Che cosa è OmegaT?

OmegaT è uno strumento per la traduzione assistita da computer (CAT). È gratuito, ossia non si deve pagare nulla per usarlo, anche per un uso professionale 
e si ha la libertà di modificarlo e, o in alternativa, redistribuirlo fintanto che viene rispettata la licenza per l'utente.

Le caratteristiche principali di OmegaT sono:
  - possibilità di funzionare su qualsiasi sistema operativo che supporti Java
  - utilizzo di qualsiasi file TMX valido come memoria di riferimento
  - segmentazione flessibile della frase (grazie all'uso di un metodo di tipo SRX)
  - ricerche nel progetto e nelle memorie di traduzione di riferimento
  - ricerche dei file nei formati supportati in qualsiasi cartella 
  - concordanze parziali (fuzzy)
  - semplice gestione dei progetti che prevedono strutture di cartelle complesse
  - supporto dei glossari (ricerca terminologica) 
  - supporto dei correttori ortografici open source "al volo"
  - supporto dei dizionari StarDict
  - supporto dei servizi di traduzione automatica Google Translate
  - - documentazione ed esercitazioni chiare ed esaustive
  - localizzazione in più lingue

OmegaT è in grado di riconoscere immediatamente i seguenti formati di file formattati:
  - solo testo
  - HTML e XHTML
  - Compilatore Help HTML (HCC)
  - OpenDocument/OpenOffice.org
  - Java resource bundles (.properties)
  - file INI (file con coppie chiave=valore di qualsiasi codifica)
  - file PO
  - formato di file per documentazione DocBook
  - file Microsoft OpenXML
  - file Okapi monolingual XLIFF
  - QuarkXPress CopyFlowGold
  - file di sottotitoli (SRT)
  - ResX
  - risorse Android
  - LaTeX
  - Typo3 LocManager
  - file di guide e manuali
  - risorse RC di Windows
  - Mozilla DTD
  - DokuWiki
  - Wix  
  - Infix
  - esportazione Flash XML
  - Wordfast TXML

OmegaT può essere personalizzato per gestire anche altri tipi di formati di file.

OmegaT analizza automaticamente anche la struttura di cartelle più complessa per accedere poi a tutti i file riconosciuti e generare una struttura di cartelle di destinazione esattamente uguale a quella di partenza, comprendente anche le copie di tutti i file non riconosciuti.

Per iniziare a operare subito con OmegaT, avviare il programma e leggere la "Guida di avvio rapido" che viene proposta.

Il manuale per l'utente si trova nel pacchetto appena scaricato e vi si potrà accedere dal menu Aiuto dopo aver avviato OmegaT.

==============================================================================
 3. Installazione di OmegaT

3.1 Generale
Per funzionare, OmegaT richiede che sul sistema sia installato Java Runtime Environment (JRE) versione 1.5 o superiore. OmegaT è ora fornito normalmente con tale programma, in modo che gli utenti non debbano cercarlo, scaricarlo e installarlo. 

Se Java è già installato nel sistema, il metodo più semplice per installare la versione corrente di OmegaT è usare Java Web Start. 
A questo proposito, scaricare ed eseguire il file seguente:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Esso installerà l'ambiente corretto per il proprio computer e l'applicazione per il primo avvio. Non è necessario eseguire online le chiamate successive.

Durante l'installazione, in base al sistema operativo, si potrebbero ricevere diversi avvisi di sicurezza. Il certificato è firmato da "Didier Briel". 
I permessi che si concedono a questa versione (che potremmo chiamare "accesso illimitato al computer") sono gli stessi che si attribuiscono alla versione locale, come installati da una procedura descritta in seguito: essi consentono l'accesso al disco rigido del computer. I clic successivi su OmegaT.jnlp verificheranno e installeranno gli aggiornamenti, se presenti, e avvieranno OmegaT. 

Di seguito, sono descritti i metodi alternativi e gli strumenti per scaricare e installare OmegaT. 

Per gli utenti di Windows e Linux: se si è a conoscenza che il proprio sistema possiede già una versione adatta di JRE, è possibile installare la versione OmegaT senza JRE (indicata col nome della versione "Without_JRE"). 
Se si è in dubbio, raccomandiamo di usare la versione "standard", con JRE. Essa è sicura, nonostante JRE sia già installato nel sistema questa versione non interferirà con esso.

Per gli utenti di Linux: OmegaT non lavora con le implementazioni Java free/open-source inserite nelle molteplici distribuzioni Linux (per esempio, Ubuntu), poiché queste non sono aggiornate o sono incomplete. Scaricare e installare Java Runtime Environment (JRE) di Sun servendosi del collegamento sopra riportato, oppure scaricare e installare la versione di OmegaT completa di JRE (il pacchetto .tar.gz contrassegnato "Linux").

Per gli utenti di Mac: JRE è già installato in Mac OS X.

Per Linux sui sistemi PowerPC: gli utenti devono scaricare il JRE di IBM, dato che Sun non fornisce una versione JRE per sistemi PPC. In questo caso, scaricarlo da:

    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Installazione
* Utenti di Windows: avviare il programma di installazione. Il programma di installazione può creare, se si desidera, collegamenti per l'avvio di OmegaT.
* Altri: per installare OmegaT, creare semplicemente una cartella appropriata per il programma (per es., /usr/local/lib in Linux). Copiare ed estrarre l'archivio zip o tar.gz OmegaT in questa cartella.

3.3 Avvio di OmegaT
OmegaT può essere avviato in vari modi.

* Utenti di Windows: facendo doppio clic sul file OmegaT.exe. Se nel proprio gestore di file (Windows Explorer) si visualizza il file OmegaT ma non quello OmegaT.exe, modificare le impostazioni affinché vengano visualizzate le estensioni dei file.

* Facendo doppio clic sul file OmegaT.jar. Questo metodo funzionerà solo se il tipo di file .jar è associato con Java nel proprio sistema.

* Da riga di comando. Il comando per avviare OmegaT è:

cd <cartella dove si trova il file OmegaT.jar>

<nome e percorso del file eseguibile Java> -jar OmegaT.jar

(il file eseguibile Java è il file java in Linux e java.exe in Windows.
Se Java è installato a livello di sistema, il percorso completo non deve essere indicato).

* Utenti di Windows: il programma di installazione può creare collegamenti nel menu start, nel desktop e nell'area di avvio rapido. Per creare il collegamento, è possibile anche trascinare a mano il file OmegaT.exe nel menu start, nel desktop nella barra di avvio rapido.

* Utenti di Linux KDE - È possibile aggiungere OmegaT ai propri menu, nel modo seguente:

Centro di controllo - Desktop - Pannelli - Menu - Modifica il menu K - File - Nuova voce/Nuovo sottomenu.

Dopo aver selezionato un menu adatto, aggiungere un sottomenu/voce tramite File - Nuovo sottomenu e file - Nuova voce. Inserire OmegaT come nome della nuova voce.

Nel campo "Comando", usare il pulsante di navigazione, individuare lo script di avvio di OmegaT e selezionarlo. 

Premere il pulsante per l'icona (a destra dei campi Nome/Descrizione/Commento) - Altre icone - Navigare fino alla sottocartella /images della cartella di programma di OmegaT. Selezionare l'icona OmegaT.png.

Salvare, infine, le modifiche con File - Salva.

* Utenti di Linux GNOME - È possibile aggiungere OmegaT al proprio pannello (barra in cima allo schermo) nel modo seguente::

Fare clic col destro sul pannello - Aggiungi nuovo lanciatore. Scrivere "OmegaT" nel campo "Nome"; nel campo "Comando", usare il pulsante di navigazione, individuare lo script di avvio di OmegaT. Selezionarlo e confermarlo con OK.

==============================================================================
 4. Partecipare al progetto OmegaT

Per partecipare allo sviluppo di OmegaT, mettersi in contatto con gli
sviluppatori a:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Per tradurre l'interfaccia utente di OmegaT, il manuale per l'utente o
altri documenti correlati, leggere:
      
      http://www.omegat.org/en/translation-info.html

e iscriversi all'elenco dei traduttori:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Per altri tipi di contributi, prima iscriversi al gruppo di utenti a:
      http://tech.groups.yahoo.com/group/omegat/

Avrete così un'idea di che cosa accade nel mondo di OmegaT...

  OmegaT è un lavoro originale di Keith Godfrey.
  Marc Prior è il coordinatore del progetto OmegaT.

Elenco dei contributi precedenti (in ordine alfabetico)

Contributi al codice:
  Zoltan Bartko
  Volker Berlin
  Didier Briel (gestore dello sviluppo)
  Kim Bruning
  Alex Buloichik (sviluppatore primario)
  Sandra Jean Chua
  Martin Fleurke  
  Wildrich Fourie
  Thomas Huriaux
  Ibai Lakunza Velasco
  Fabián Mandelbaum
  Maxym Mykhalchuk 
  Arno Peters
  Henry Pijffers 
  Briac Pilpré
  Tiago Saboga
  Andrzej Sawuła
  Benjamin Siband
  Rashid Umarov  
  Antonio Vilei
  Martin Wunderlich

Altri contributi:
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (gestore della localizzazione)
  Vito Smolej (gestore della documentazione)
  Samuel Murray
  Marc Prior 
  e molte, molte altre persone che hanno contribuito

(se ritenete di aver contribuito in modo significativo al Progetto OmegaT ma il vostro nome non è presente nell'elenco, contattateci senza problemi).

OmegaT usa le seguenti librerie:

  HTMLParser di Somik Raha, Derrick Oswald et al (licenza LGPL)
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter 1.0.8 di Steve Roy (licenza LGPL)
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework 2.1.4 di VLSolutions (licenza CeCILL)
  http://www.vlsolutions.com/en/products/docking/

  Hunspell di László Németh et al (licenza LGPL)

  JNA di Todd Fast, Timothy Wall et al (licenza LGPL)

  Swing-Layout 1.0.2 (licenza LGPL)

  Jmyspell 2.1.4 (licenza LGPL)

  JAXB 2.1.7 (GPLv2 + classpath exception)

==============================================================================
 5.  OmegaT genera problemi? Si ha bisogno di assistenza?

Prima di segnalare un difetto, accertarsi di aver consultato attentamente la documentazione. Ciò che viene proposto potrebbe essere una caratteristica particolare di OmegaT che si è appena scoperta. Se si apre il file di registro di OmegaT, e nello stesso vengono riportate parole come "Error", "Warning", "Exception" oppure "died unexpectedly", allora si è in presenza di un errore importante (il file log.txt viene memorizzato nella cartella delle preferenze dell'utente; per il percorso di memorizzazione si rimanda al manuale).

La successiva cosa da fare è di chiedere conferma di quanto è accaduto agli altri utenti, in modo da verificare se si tratta di un problema già segnalato. Si può anche accedere alla pagina di segnalazione degli errori di SourceForge. Inviare una segnalazione di errore solo quando si è sicuri di essere stati i primi ad aver rilevato una sequenza riproducibile di un evento che ha generato qualche cosa che non sarebbe dovuta accadere.

Una qualsiasi segnalazione di errore dovrebbe prevedere tre elementi:
  - sequenza operativa da riprodurre,
  - che cosa ci si aspettava di ottenere, e
  - che cosa, invece, si è ottenuto.

Si possono allegare copie di file, parti del file di registro, schermate e tutto ciò che si ritiene possa essere d'aiuto agli sviluppatori per il reperimento e la correzione dell'errore.

Per accedere agli archivi del gruppo di utenti:
     http://groups.yahoo.com/group/OmegaT/

Per accedere alla pagina delle segnalazioni degli errori e registrare
una nuova segnalazione di errore:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Per seguire il corso degli eventi relativi a una segnalazione di errore ci si dovrebbe iscrivere come utente Source Forge.

==============================================================================
6.   Informazioni sul rilascio

Per informazioni particolareggiate su questo rilascio, e tutti quelli precedenti, si veda il file "changes.txt".


==============================================================================
