Esta tradução foi feita por Thelma L Sabim, copyright© 2011.

==============================================================================
  OmegaT 2.0, arquivo Leia-me

  1.  Informações sobre o OmegaT
  2.  O que é OmegaT?
  3.  Instalação do OmegaT
  4.  Contribuições ao OmegaT
  5.  Você tem problemas com o OmegaT? Precisa de ajuda?
  6.  Detalhes da versão

==============================================================================
  1.  Informações sobre o OmegaT


As informações mais atualizadas sobre o OmegaT estão disponíveis no 
      http://www.omegat.org/

Suporte ao usuário, no grupo (poliglota) de usuários do Yahoo, onde os arquivos podem ser pesquisados sem ser preciso fazer a inscrição no grupo:
     http://groups.yahoo.com/group/OmegaT/

Os pedidos para melhorias (em inglês), no site da SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Os relatórios de 'bugs' (em inglês), no site da SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

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
  - texto simples
  - HTML e XHTML
  - compilador de ajuda HTML
  - OpenDocument/OpenOffice.org
  - pacotes de recursos Java (.properties)
  - arquivos INI (arquivos com pares key=value de qualquer codificação)
  - arquivos PO
  - formato de arquivo de documentação DocBook
  - arquivos Microsoft OpenXML 
  - arquivos XLIFF monolingue do Okapi
  - QuarkXPress CopyFlowGold
  - Arquivos legendas (SRT)
  - ResX
  - recursos Android
  - LaTeX
  - Typo3 LocManager
  - Help & Manual
  - recursos Windows RC
  - Mozilla DTD
  - DokuWiki

O OmegaT pode ser adaptado para ser compatível com outros formatos de arquivo.

OmegaT analisará automaticamente até mesmo as hierarquias mais complexas de pastas fonte para acessar todos os formatos de arquivos suportados e produzirá uma pasta destino exatamente com a mesma estrutura, inclusive cópias de arquivos em formatos para os quais não oferece suporte.

Para acessar as instruções básicas, inicie o OmegaT e leia o tutorial do Início Rápido.

O manual do usuário está no pacote de arquivos que você acabou de baixar; ele está disponível no menu [Ajuda], após iniciar o OmegaT.

==============================================================================
 3. Instalação do OmegaT

3.1 Geral
Para rodar, o OmegaT requer que o Java Runtime Environment (JRE) versão 1.5 ou mais recente esteja instalado no seu computador. O OmegaT agora já vem com o Java Runtime Environment para evitar problemas aos usuários na seleção, baixa e instalação do Java. 

Se o Java já estiver instalado no seu computador, a maneira mais fácil de instalar a versão atual do OmegaT é usar o Java Web Start. 
Para isso, baixe e rode o seguinte arquivo:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Ele instalará o ambiente correto para seu computador e o aplicativo na primeira rodada. Para outras chamadas do programa não é preciso estar on-line.

Durante a instalação, dependendo do seu sistema operacional, você pode receber diversos avisos de segurança. O certificado de autenticidade e segurança tem a assinatura do "Didier Briel". 
As permissões que você dá a esta versão (que podem ser mencionada como um "acesso irrestrito ao computador") são idênticas às permissões que você dá à versão local, instaladas por um procedimento descrito posteriormente: elas permitem acesso ao disco rígido do computador. Se você estiver on-line e clicar em OmegaT.jnlp o programa verificará se há atualizações. Se houver, instale-as e depois inicie o OmegaT. 

Veja a seguir as formas e meios alternativos para baixar e instalar o OmegaT. 

Usuário do Windows e Linux: se você tiver certeza de que seu computador já tem a versão apropriada do JRE, instale o OmegaT sem o JRE (o nome da versão sem Java indica "Without_JRE"). 
Se tiver dúvidas, recomendamos que use a versão "standard", ou seja, com JRE. Este procedimento é seguro, mesmo se o JRE já estiver instalado no seu computador; esta versão não causará nenhuma interferência no sistema.

Usuário do Linux: note que o OmegaT não funciona com as implementações Java gratuitas/código-aberto que estão incluídas em muitas distribuições do Linux (por exemplo, Ubuntu), pois elas podem estar desatualizadas ou incompletas. Use o link acima para baixar e instalar o Java Runtime Environment (JRE) da Sun, ou baixe e instale o pacote OmegaT com o JRE (o pacote .tar.gz marcado "Linux").

Usuário do Mac: o JRE já está instalado no Mac OS X.

O Linux em sistemas PowerPC: os usuários precisarão baixar o JRE da IBM, pois a Sun não oferece um JRE para sistemas PPC. Neste caso, baixe o JRE do:

    [fuzzy]http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Instalação
* Usuário do Windows: é só rodar o programa de instalação. Se quiser, o programa de instalação cria atalhos para iniciar o OmegaT.
* Outros: para instalar o OmegaT, basta criar uma pasta para o OmegaT(p.ex., /usr/local/lib no Linux). Copie o arquivo compactado OmegaT ou tar.gz para esta pasta e faça a descompactação.

3.3 Como rodar o OmegaT
O OmegaT pode ser acessado de várias maneiras.

* Usuário do Windows: abra o arquivo OmegaT.exe com um clique duplo. Se o Gerenciados de Arquivos (Windows Explorer) exibir o arquivo OmegaT mas não o arquivo OmegaT.exe, modifique as configurações para que as extensões dos arquivos sejam exibidas.

* Dê um clique duplo no arquivo OmegaT.jar. Isto funcionará somente se o tipo de arquivo .jar estiver associado com o Java no seu sistema.

* Na linha de comando. O comando para acessar o OmegaT é:

cd <pasta que contém o arquivo OmegaT.jar>

<nome e caminho do arquivo executável java> -jar OmegaT.jar

(O arquivo Java executável é o arquivo java no Linux e o java.exe no Windows.
Se o Java estiver instalado no sistema, não é necessário digitar o caminho completo.)

* Usuário do Windows: o programa de instalação pode criar atalhos no menu iniciar, na área de trabalho e na área de acesso rápido. Você também pode arrastar o arquivo OmegaT.exe para o menu Iniciar, para a área de trabalho ou para a de acesso rápido para fazer um link dali.

* Usuário do Linux KDE: o OmegaT pode ser adicionado nos menus da seguinte maneira:

Centro de Controle - Desktop - Painéis - Menus - Editar Menu K  - Arquivo - Novo item/Novo submenu.

Depois de selecionar um menu, acrescente um submenu/item com Arquivo - Submenu e Arquivo - Novo item. Digite OmegaT como o nome do novo item.

No campo 'Comando', use o botão de navegação para localizar e selecionar o script que inicia o OmegaT. 

Clique no ícone (à direita dos campos Nome/Descrição/Comentário) Outros ícones - Navegar, e acesse a subpasta /imagens na pasta do OmegaT. Selecione o ícone OmegaT.png.

Finalmente, grave as modificações com Arquivo - Salvar.

* Usuário do Linux GNOME: o OmegaT pode ser adicionado ao seu painel (a barra no topo da tela) conforme segue:

Clique com o botão direito do mouse no painel - Adicionar Novo Chamador. Digite 'OmegaT' no campo 'Nome'; no campo 'Comando', use o botão de navegação para localizar o script para iniciar o OmegaT. Selecione e confirme com OK.

==============================================================================
 4. Como participar no projeto OmegaT

Para participar no desenvolvimento do OmegaT, entre em contato com os desenvolvedores no:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Para traduzir a interface de usuário do OmegaT, manual do usuário e outros documentos relacionados, leia:
      
      http://www.omegat.org/en/translation-info.html

E inscreva-se na lista de tradutores:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Para outros tipos de contribuições, inscreva-se primeiro no grupo de usuários no:
      http://tech.groups.yahoo.com/group/omegat/

E fique por dentro do que acontece no mundo do OmegaT...

  OmegaT originou-se do trabalho de Keith Godfrey.
  Marc Prior é o coordenador do projeto OmegaT.

Contribuidores anteriores:(ordem alfabética)

O código é uma contribuição de
  Zoltan Bartko
  Volker Berlin
  Didier Briel (gerente de desenvolvimento)
  Kim Bruning
  Alex Buloichik (desenvolvedor líder)
  Sandra Jean Chua
  Martin Fleurke  
  Wildrich Fourie
  Thomas Huriaux
  Ibai Lakunza Velasco
  Fabián Mandelbaum
  Maxym Mykhalchuk 
  Arno Peters
  Henry Pijffers 
  Tiago Saboga
  Andrzej Sawuła
  Benjamin Siband
  Rashid Umarov  
  Antonio Vilei
  Martin Wunderlich

Outras contribuições de
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (gerente de localização)
  Vito Smolej (gerente de documentação)
  Samuel Murray
  Marc Prior 
  e muitos outros colaboradores

(Se você contribuiu de forma significativa no Projeto OmegaT mas seu nome não consta das listas, fale conosco)

OmegaT usa as seguintes bibliotecas:

  HTMLParser por Somik Raha, Derrick Oswald e outros (Licença LGPL).
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter 1.0.8 por Steve Roy (Licença LGPL).
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework 2.1.4 por VLSolutions (Licença CeCILL).
  http://www.vlsolutions.com/en/products/docking/

  Hunspell por László Németh e outros (Licença LGPL)

  JNA por Todd Fast, Timothy Wall e outros (Licença LGPL)

  Swing-Layout 1.0.2 (Licença LGPL)

  Jmyspell 2.1.4 (Licença LGPL)

  JAXB 2.1.7 (GPLv2 + exceção classpath)

==============================================================================
 5.  Você tem problemas com o OmegaT? Precisa de ajuda?

Antes de relatar um erro, verifique com atenção a documentação. O que você vê pode ser que seja uma característica do OmegaT que acabou de descobrir. Se verificar o arquivo de registro do OmegaT e lá encontrar palavras como "Erro", "Aviso", "Exceção" ou "morreu inesperadamente", então realmente você descobriu um problema (o log.txt está localizado na pasta de preferências do usuário; consulte o manual para sua localização).

A próxima coisa que você deve fazer é confirmar o que encontrou com outros usuários, para ter certeza de que o problema ainda não foi relatado. Você também pode verificar a página de relatório de 'bugs' no site da SourceForge. Envie um relatório de erro somente quando tiver certeza de que é o primeiro a encontrar uma seqüência de evento reproduzível que causou algum problema.

O relatório de 'bugs' deve conter três informações.
  - Passos para reproduzir,
  - O que você esperava ver, e
  - O que viu em vez disso.

Você pode acrescentar cópias de arquivos, partes do registro, capturas de tela, qualquer coisa que achar que possa ajudar os desenvolvedores a localizar e consertar o problema.

Para fazer uma busca nos arquivos do grupo de usuários, acesse:
     http://groups.yahoo.com/group/OmegaT/

Para ver a página de relatório de erros e enviar um relatório sobre um problema novo, se necessário, acesse:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Para fazer o acompanhamento do relatório enviado, faça seu registro como usuário na SourceForge.

==============================================================================
6.   Detalhes da versão

Veja o arquivo 'changes.txt' para obter informações detalhadas sobre as modificações nesta e em versões anteriores.


==============================================================================
