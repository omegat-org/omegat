Esta tradução foi feita por Thelma L , copyright© 2011.
Versões mais recentes traduzidas por Thiago Hilger, copyright© 2015.

==============================================================================
  OmegaT 3.0, arquivo Leia-me

  1.  Informações sobre o OmegaT
  2.  O que é OmegaT?
  3.  Instalando o OmegaT
  4.  Contribuições ao OmegaT
  5.  Você tem problemas com o OmegaT? Precisa de ajuda?
  6.  Detalhes da versão

==============================================================================
  1.  Informações sobre o OmegaT


As informações mais atualizadas sobre o OmegaT estão disponíveis no 
      http://www.omegat.org/

Suporte ao usuário, no grupo (poliglota) de usuários do Yahoo, onde os arquivos podem ser pesquisados sem ser preciso fazer a inscrição no grupo:
     http://tech.groups.yahoo.com/group/OmegaT/

Os pedidos para melhorias (em inglês), no site da SourceForge:
     https://sourceforge.net/p/omegat/feature-requests/

Os relatórios de 'bugs' (em inglês), no site da SourceForge:
     https://sourceforge.net/p/omegat/bugs/

==============================================================================
  2.  O que é OmegaT?

OmegaT é uma ferramenta de tradução com auxílio do computador (sigla em inglês CAT). É gratuito, isto é, você não precisa pagar nada para usar o programa, mesmo se usá-lo profissionalmente; também poderá modificar e redistribuir o programa, desde que respeite os termos da licença de usuário.

Os principais recursos do OmegaT são:
  - pode ser executado em qualquer sistema operacional que suporta Java
  - usa qualquer arquivo TMX válido como referência de tradução
  - flexibilidade de segmentação de frases (usando um método como SRX)
  - busca no projeto e nas memórias de tradução de referência
  - pesquisa de arquivos nos formatos suportados em qualquer pasta 
  - apresenta correspondência parcial de frases
  - processamento inteligente de projetos, inclusive hierarquias complexas de diretórios
  - suporte para glossários (verificação de terminologia) 
  - suporte para corretores ortográficos do OpenSource à medida que surgirem
  - suporte para dicionários StarDict
  - suporte para os serviços de tradução automatizada do Google Tradutor
  - documentação e tutorial abrangentes e fáceis de entender
  - traduzido e localizado em diversas línguas.

OmegaT trabalha diretamente com os seguintes tipos de arquivos:

- Formatos de arquivo de texto simples

  - Texto ASCII (.txt, etc.)
  - Texto codificado (*.UTF8)
  - Pacotes de recursos Java (*.properties)
  - Arquivos PO (*.po)
  - INI (key=value) files (*.ini)
  - Arquivos DTD (*.DTD)
  - Arquivos DocuWiki (*.txt)
  - Arquivos de legenda SubRip (*.srt)
  - Magento CE Locale CSV (*.csv)

- Formatos de arquivo de texto com tags

  - OpenOffice.org / OpenDocument (*.odt, *.ott, *.ods, *.ots, *.odp, *.otp)
  - Microsoft Open XML (*.docx, *.xlsx, *.pptx)
  - (X)HTML (*.html, *.xhtml,*.xht)
  - HTML Help Compiler (*.hhc, *.hhk)
  - DocBook (*.xml)
  - monolingual XLIFF (*.xlf, *.xliff, *.sdlxliff)
  - QuarkXPress CopyFlowGold (*.tag, *.xtg)
  - Arquivos ResX (*.resx)
  - Recurso Android (*.xml)
  - LaTex (*.tex, *.latex)
  - Arquivos Help (*.xml) & Manual (*.hmxp)
  - Typo3 LocManager (*.xml)
  - Localização WiX (*.wxl)
  - Iceni Infix (*.xml)
  - Flash XML export (*.xml)
  - Wordfast TXML (*.txml)
  - Camtasia for Windows (*.camproj)
  - Visio (*.vxd)

O OmegaT pode ser adaptado para ser compatível com outros formatos de arquivo.

O OmegaT analisará automaticamente até mesmo as hierarquias mais complexas de pastas fonte para acessar todos os formatos de arquivos suportados e produzirá uma pasta destino exatamente com a mesma estrutura, inclusive cópias de arquivos em formatos para os quais não oferece suporte.

Para acessar as instruções básicas, inicie o OmegaT e leia o tutorial de Início Rápido.

O manual do usuário está no pacote de arquivos que você acabou de baixar; ele está disponível no menu [Ajuda], após iniciar o OmegaT.

==============================================================================
 3. Instalando o OmegaT

3.1 Geral
Para rodar, o OmegaT requer que o Java Runtime Environment (JRE) versão 1.5 ou mais recente esteja instalado no seu computador. O pacote do OmegaT que inclui o Java Runtime Environment está disponível para evitar problemas aos usuários na selação, baixa e instalação. 

Se você já possui o Java, uma forma de instalar a versão atual do OmegaT é usar o Java Web Start. 
Para isso, baixe e rode o seguinte arquivo:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Ele instalará o ambiente correto para seu computador e o aplicativo na primeira vez que rodar. Para outras chamadas do programa não é preciso estar on-line.

Durante a instalação, dependendo do seu sistema operacional, você pode receber diversos avisos de segurança. O certificado é da "PnS Concept". 
As permissões que você dá à esta versão (que podem ser mencionadas como um "acesso irrestrito ao computador") são idênticas às permissões que você dá à versão local, instaladas por um procedimento descrito posteriormente: elas permitem acesso ao disco rígido do computador. Se você estiver online e clicar em OmegaT.jnlp 
o programa verificará se há atualizações. Se houver, instale-as e
depois inicie o OmegaT. 

Veja a seguir as formas e meios alternativos para baixar e instalar o OmegaT. 

Usuários do Windows e Linux: se você tiver certeza de que seu computador já tem a versão apropriada do JRE, instale o OmegaT sem o JRE (o nome da versão sem Java indica "Without_JRE"). 
Se tiver dúvidas, recomendamos que use a versão com JRE. Este procedimento é seguro, mesmo se o JRE já estiver instalado no seu computador; esta versão não causará nenhuma interferência no sistema.

Usuários do Linux: 
O OmegaT rodará no pacote de implementações Java de código aberto
em muitas distribuições do Linux (por exemplo, Ubuntu), mas podem ocorrer
bugs, exibir problemas ou recursos ausentes. Portanto, recomendamos
que baixe e instale o Oracle Java Runtime Environment (JRE)
ou o pacote OmegaT com JRE (o .tar.bz2) pacote marcado
"Linux"). Se instalar uma versão do Java a nível de sistema, você deve
assegurar-se de que esteja em seu caminho para inicialização, ou acessá-la explicitamente quando iniciar o 
OmegaT. Se não estiver familizariado com o Linux, recomendamos que
instale uma versão do OmegaT com o JRE incluído. Este procedimento é seguro,
já que o JRE "local" não interferirá com qualquer outro JRE instalado
em seu sistema.

Usuários Mac: 
o JRE já está instalado no Mac OS X antes Mac OS X 10.7 
(Lion). Usuários Lion serão avisados pelo sistema na primeira vez que iniciarem
uma aplicação que requeira o Java, e assim, o sistema o
baixará e instalará automaticamente.

Linux nos sistemas PowerPC: 
os usuários terão que baixar o JRE da IBM, pois a Sun
não oferece um JRE para sistemas PPC. Neste caso, baixe o JRE do:

    http://www.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Instalação
* Usuários do Windows: 
é só rodar o programa de instalação. Se quiser, o 
programa de instalação cria atalhos para iniciar o OmegaT.

* Usuários do Linux:
coloque o arquivo na pasta apropriada e descompacte-o; assim o OmegaT
estará pronto para iniciar. No entanto, é possível obter uma instalação mais organizada e de fácil utilização 
ao usar o script de instalação (linux-install.sh). Para usar
esse script, abra uma janela do terminal (console), mude a pasta para a
que contém o OmegaT.jar e o script linux-install.sh e execute o 
script com ./linux-install.sh.

* Usuários do Mac:
Copie o arquivo OmegaT.zip para um local apropriado e descompacte-o 
para obter uma pasta com um arquivo de índice de documentação em HTML e
o arquivo do programa OmegaT.app.

* Outros (p.ex., Solaris, FreeBSD: 
Para instalar o OmegaT, basta criar uma pasta apropriada para OmegaT. Copie e descompacte o 
OmegaT zip ou o arquivo tar.bz2 nessa pasta.

3.3 Iniciar o OmegaT
Inicie o OmegaT conforme instruções.

* Usuários do Windows: 
Se durante a instalação você criou um atalho na área de trabalho,
dê um clique duplo no atalho. Como alternativa, dê um clique duplo no arquivo
OmegaT.exe. Se o Gerenciador de Arquivos (Windows Explorer) exibir o arquivo OmegaT mas não o arquivo OmegaT.exe, modifique as configurações para que as extensões dos arquivos sejam exibidas.

* Usuários do Linux:
se utilizou o script de instalação fornecido, é possível iniciar o OmegaT com:
Alt+F2
e em seguida:
omegat

* Usuários do Mac:
dê um clique duplo no arquivo OmegaT.app.

* A partir do seu gerenciador de arquivos (todos os sistemas):
dê um clique duplo no arquivo OmegaT.jar. Isto funcionará somente se o tipo de arquivo .jar
estiver associado com o Java no seu sistema

* A partir da linha de comando (todos os sistemas): 
o comando para acessar o OmegaT é:

cd <pasta que contém o arquivo OmegaT.jar>

<nome e caminho do arquivo executável java> -jar OmegaT.jar

(O arquivo Java executável é o arquivo java no Linux e o java.exe no Windows.
Se o Java estiver instalado no sistema e estiver na linha de comando, não é 
necessário digitar o caminho completo.)

Customizar a inicialização do OmegaT:

* Usuários do Windows: 
O prgrama de instalação pode criar atalhos no menu inciar,
na área de trabalho e na área de acesso rápido. Você também pode arrastar
o arquivo OmegaT.exe para o menu iniciar, para a área de trabalho ou para a área de acesso rápido
para fazer um link dali.

* Usuários do Linux:
Um caminho mais fácil para iniciar o OmegaT, é usar o script Kaptain (omegat.kaptn). Antes de usar esse script é preciso instalar o
Kaptain. Você pode comandar o script de incialização Kaptain usando
Alt+F2
omegat.kaptn

Para mais informações sobre o script Kaptain, adicionar itens ao menu e ícones de inicialização no Linux, consulte em OmegaT: Linux, Como Fazer  

Usuários Mac:
Para iniciar o OmegaT de qualquer local, arraste o OmegaT.app para a dock ou para a barra de ferramentas da 
janela do localizador. Também é possível iniciar o programa pelo
campo de busca do Spotlight.

==============================================================================
 4. Como participar no projeto OmegaT

Para participar do desenvolvimento do OmegaT, entre em contato com os desenvolvedores no:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Para traduzir a interface do usuário do OmegaT, manual do usuário ou outros documentos relacionados,
leia:
      
      http://www.omegat.org/en/howtos/localizing_omegat.php

E inscreva-se na lista de tradutores:
      https://lists.sourceforge.net/lists/listinfo/omegat-l10n

Para contribuir de outras formas, inscreva-se primeiro no grupo de usuários no:
      http://tech.groups.yahoo.com/group/omegat/

E fique por dentro do que acontece no mundo do OmegaT...

  OmegaT originou-se do trabalho de Keith Godfrey.
  Didier Briel é o gerente de projeto do OmegaT.

Contribuidores anteriores:
(ordem alfabética)

O código é uma contribuição de
  Zoltan Bartko
  Volker Berlin
  Didier Briel
  Kim Bruning
  Alex Buloichik (desenvolvedor líder)
  Sandra Jean Chua
  Thomas Cordonnier
  Enrique Estévez Fernández
  Martin Fleurke  
  Wildrich Fourie
  Phillip Hall
  Jean-Christophe Helary
  Thomas Huriaux
  Hans-Peter Jacobs
  Kyle Katarn
  Piotr Kulik
  Ibai Lakunza Velasco
  Guido Leenders
  Aaron Madlon-Kay
  Fabián Mandelbaum
  Manfred Martin
  Adiel Mittmann
  John Moran
  Maxym Mykhalchuk 
  Arno Peters
  Henry Pijffers 
  Briac Pilpré
  Tiago Saboga
  Andrzej Sawuła
  Benjamin Siband
  Yu Tang
  Rashid Umarov  
  Antonio Vilei
  Ilia Vinogradov
  Martin Wunderlich
  Michael Zakharov

Outras contribuições de
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (gerente de localização)
  Vincent Bidaux (gerente de documentação)
  Samuel Murray
  Marc Prior (webmaster)
  e muitos outros colaboradores

(Se você contribuiu de forma significativa no Projeto OmegaT
mas seu nome não consta das listas, fale conosco.)

OmegaT usa as seguintes bibliotecas:
  HTMLParser 1.6 por Somik Raha, Derrick Oswald e outros (Licença LGPL)
  VLDocking Framework 3.0.5-SNAPSHOT (Licença LGPL)
  Hunspell por László Németh e outros (Licença LGPL)
  JNA por Todd Fast, Timothy Wall e outros (Licença LGPL)
  Swing-Layout 1.0.4 (Licença LGPL)
  Jmyspell 2.1.4 (Licença LGPL)
  SVNKit 1.8.5 (Licença TMate)
  Sequence Library (Licença Sequence Library)
  ANTLR 3.4 (Licença ANTLR 3)
  SQLJet 1.1.10 (GPL v2)
  JGit (Licença Pública Eclipse)
  JSch (Licença JSch)
  Base64 (domínio público)
  Diff (GPL) 
  trilead-ssh2-1.0.0-build217 (licença Trilead SSH)
  lucene-*.jar (Licença Apache 2.0)
  Os tokenizers em inglês (org.omegat.tokenizer.SnowballEnglishTokenizer e
  org.omegat.tokenizer.LuceneEnglishTokenizer) usam palavras irrelevantes do Okapi
(http://okapi.sourceforge.net) (Licença LGPL)
  tinysegmenter.jar (Licença BSD modificada)
  commons-*.jar (Licença Apache 2.0)
  jWordSplitter (Licença Apache 2.0)
  LanguageTool.jar (Licença LGPL)
  morfologik-*.jar (Licença Morfologik)
  segment-1.4.1.jar (licença Segment)
  pdfbox-app-1.8.1.jar (Licença Apache 2.0)
  KoreanAnalyzer-3x-120223.jar (Licença Apache 2.0)
  SuperTMXMerge-for_OmegaT.jar (licença LGPL)
  groovy-all-2.2.2.jar (Licença Apache 2.0)
  slf4j (Licença MIT)
  juniversalchardet-1.0.3.jar (GPL v2)

==============================================================================
 5.  Você tem problemas com o OmegaT? Precisa de ajuda?

Antes de relatar um erro, verifique com atenção a
documentação. O que você vê pode ser que seja uma característica do OmegaT
que acabou de descobrir. Se verificar o arquivo de registro do OmegaT e encontrar palavras como
"Erro", "Aviso", "Exceção", ou "desativado inesperadamente", então realmente
você descobriu um problema (o log.txt está localizado na pasta preferências do usuário;
consulte o manual para sua localização).

O próximo passo é confirmar o que encontrou com os outros usuários, para ter certeza
de que o problema ainda não foi relatado. Você também pode verificar a página de relatório de 'bugs' no site da
SourceForge. Envie um relatório de 'bugs' somente quando tiver certeza de que é o primeiro
a encontrar um sequência de evento reproduzível que desencadeou algo
que não deveria acontecer.

O relatório de 'bugs' deve conter três informações.
  - Passos para reproduzir,
  - O que você esperava ver, e
  - O que viu em vez disso.

Você pode acrescentar cópias dos arquivos, partes do registro, capturas de tela, qualquer coisa
que achar que possa ajudar os desenvolvedores a encontrar e reparar o problema.

Para fazer uma busca nos arquivos do grupo de usuários, acesse:
     http://tech.groups.yahoo.com/group/OmegaT/

Para ver a página de relatório de erros e enviar um relatório sobre um problema novo, se necessário, acesse:
     https://sourceforge.net/p/omegat/bugs/

Para fazer o acompanhamento do relatório enviado, faça seu registro
como usuário na Source Forge.

==============================================================================
6.   Detalhes da versão

Veja o arquivo 'changes.txt' para informações detalhadas sobre as modificações
nesta e em todas as versões anteriores.


==============================================================================