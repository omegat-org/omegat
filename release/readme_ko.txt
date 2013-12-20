v3.0.7 최소 번역 - nerhis, copyright© 2013.

==============================================================================
  OmegaT 3.0, 읽어주세요 파일

  1.  OmegaT에 대한 정보
  2.  OmegaT는 무엇인가?  3.  OmegaT 설치
  4.  OmegaT에 기여
  5.  OmegaT에 버그가 있으면?도움이 필요하세요?  6.  릴리스 세부 정보

==============================================================================
  1.  OmegaT에 대한 정보


OmegaT에 대한 최신 정보는 아래에서 찾을 수 있습니다.
      http://www.omegat.org/

야후 사용자 그룹(다국어)의 사용자 지원, 기록 보관소는 가입하지 않고 검색할 수 있습니다. :
     http://tech.groups.yahoo.com/group/OmegaT/

SourceForge 사이트에서 개선점 요청(영어로)  :
     https://sourceforge.net/p/omegat/feature-requests/

SourceForge 사이트에서 버그 리포트(영어로) :
     https://sourceforge.net/p/omegat/bugs/

==============================================================================
  2.  OmegaT는 무엇인가?

OmegaT는 컴퓨터 지원 번역(CAT) 도구입니다. 무료입니다, 전문적인 용도로 사용하는데 아무것도 지불하지 않아도 되고, 사용자 라이센스를 준수하면 재배포 및 수정도 가능합니다.

OmegaT의 주요 특징은 :
  - 자바를 지원하는 어떠한 운영 체제에서 실행
  - 모든 유효한 TMX 파일을 번역 참고 자료로 사용
  - 유연한 문장 구분 (SRX와 같은 방법을 사용)
  - 프로젝트와 참조 번역 메모리 검색
  - 아무 폴더안에 지원 되는 형식의 파일 검색 
  - 유사 항목 일치
  - 복잡한 폴더 계층 구조를 포함한 프로젝트의 처리
  -용어집(용어 확인)을 지원 
  - OpenSource on-the-fly 맞춤법 검사기 지원
  - StarDict 사전 지원
  - Google 기계 번역 서비스 지원
  - 명확하고 포괄적인 설명서 및 자습서
  - 다양한 언어로 지역화

OmegaT는 다음 파일 형식을 지원합니다. :

- 일반 텍스트 파일 형식

  - ASCII 텍스트 (.txt, 등.)
  - 인코딩된 텍스트 (*.UTF8)
  - Java 리소스 번들 (*.properties)
  - PO 파일 (*.po)
  - INI (key=value) 파일 (*.ini)
  - DTD 파일 (*.DTD)
  - DocuWiki 파일 (*.txt)
  - SubRip 자막 파일 (*.srt)
  - Magento CE Locale CSV (*.csv)

- 태그 있는 텍스트 파일 형식

  - OpenOffice.org / OpenDocument (*.odt, *.ott, *.ods, *.ots, *.odp, *.otp)
  - Microsoft Open XML (*.docx, *.xlsx, *.pptx)
  - (X)HTML (*.html, *.xhtml,*.xht)
  - HTML 도움말 컴파일러 (*.hhc, *.hhk)
  - DocBook (*.xml)
  - monolingual XLIFF (*.xlf, *.xliff, *.sdlxliff)
  - QuarkXPress CopyFlowGold (*.tag, *.xtg)
  - ResX 파일 (*.resx)
  - 안드로이드 리소스 (*.xml)
  - LaTex (*.tex, *.latex)
  - 도움말 (*.xml)과 매뉴얼 (*.hmxp) 파일
  - Typo3 LocManager (*.xml)
  - WiX Localization (*.wxl)
  - Iceni Infix (*.xml)
  - Flash XML export (*.xml)
  - Wordfast TXML (*.txml)
  - Camtasia for Windows (*.camproj)
  - Visio (*.vxd)

OmegaT는 뿐만 아니라 다른 파일 포맷을 지원하기 위해 사용자 설정을 할 수 있습니다.

OmegaT는 자동으로, 심지어는 가장 복잡한 소스 폴더 계층 구조라도, 모든 지원 파일을 구문 분석하고 지원되지 않는 파일의 복사본을 포함하는 동일한 구조의 대상 폴더를 생성합니다.

빠른 시작 자습서는 OmegaT를 시작하면 바로 나옵니다.

사용자 설명서는 여러분이 방금 다운로드 한 패키지에 있고, OmegaT를 시작한 후 [도움말] 메뉴에서 볼 수 있습니다.

==============================================================================
 3. OmegaT 설치

3.1일반적으로  OmegaT를 실행하기 위해서는 ,자바 런타임 환경(JRE) 버전 1.5 이상이 시스템에 설치되어 있어야 합니다. 자바 런타임 환경을 포함한 OmegaT 패키지를 선택하고 설치하면, 여러분의 선택의 수고를 덜 수 있습니다. 

자바가 이미 설치되어 있는 경우, Java Web Start를 사용하여 OmegaT를 설치하는 것도 한 가지 방법이 될 수 있습니다. 
그럴려면 다음 파일을 다운로드 하고 실행하세요 :

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

첫 번째로 실행하면 컴퓨터와 응용 프로그램에 대해 올바른 환경을 설치합니다. 한 번 실행하면 나중에 온라인 상태가 될 필요 없습니다.

운영 체제에 따라 설치하는 동안 여러 보안 경고가 나타날 수 있습니다. 인증서는 "Didier Briel"에 의해 자체 서명되었습니다. 
뒷에서 설명 하는 절차에 의해 설치된 것과 이 버전은 동일한 사용 권한("컴퓨터에 무제한 액세스"로 언급 될 수도 있습니다)을 부여받습니다. 이것은 컴퓨터 하드 드라이브에 대한 액세스를 허용합니다OmegaT.jnlp에 계속을 클릭하면 모든 업그레이드에 대한 확인을 하고, 온라인인 경우, OmegaT를 설치하고 시작합니다. 

OmegaT 다운로드 대체 방법과 설치는 아래와 같습니다. 

윈도우즈와 리눅스 사용자 : 시스템에 이미 설치 된 JRE의 적합한 버전이 있는 경우 (버전, "Without_JRE"의 이름으로 표시 됩니다) JRE 설치 없이 OmegaT를 설치할 수 있습니다. 
모르겠다면, JRE와 함께 제공된 버전을 사용하는 것이 좋습니다. 이것은 당신의 시스템에 JRE가 설치 되어 있는 경우 버전 간섭을 하지 않기 때문에 안전합니다.

리눅스 사용자: OmegaT는 오픈 소스 자바 리눅스 배포판 (예를 들어, 우분투)와 함께 패키지 실행 됩니다. 하지만 버그가 발생할 수 있습니다. 따라서 오라클 자바 런타임 환경 (JRE) 또는 "리눅스"로 표시된 JRE 포함 OmegaT 패키지(the .tar.bz2)를 다운받고 설치하는 것이 좋습니다. 시스템 수준에서 자바의 버전을 설치할 경우 실행 경로를, OmegaT를 시작할 때 명시적으로 호출하거나 확인해야 합니다. 당신이 리눅스를 잘 모르면, 우리의 JRE 포함 OmegaT 버전을 설치 하는 것이 좋습니다. 이것은 안전하고, 이후 "로컬" JRE가 시스템에 설치되어도 다른 JRE에 간섭하지 않을 것 입니다.

Mac 사용자: 맥 OS X 10.7 (라이언) 부터 Mac OS X에는 JRE가 설치 되어 있습니다. 라이언 사용자는 처음 자바를 필요로 하는 응용 프로그램을 시작할 때, 시스템이 자동으로 다운로드 하고 설치 합니다.

PowerPC 시스템의 리눅스 : Sun은 PPC 시스템에 JRE를 제공하지 않습니다. IBM의 JRE를 다운로드 하세요. 이 경우 다운로드 :

    http://www.ibm.com/developerworks/java/jdk/linux/download.html 


3.2설치 * 윈도우 사용자: 간단하게 설치 프로그램을 실행합니다. 원하는 경우, 설치 프로그램에서 OmegaT를 시작하는 바로 가기를 만들 수 있습니다.

* 리눅스 사용자: 적당한 폴더에 보관 하고 압축을 풀면. OmegaT 실행 준비가 됩니다. 그러나 설치 스크립트 (linux-install.sh)를 사용하여 깔끔하고 더 사용자 친화적인 설치를 할 수 있습니다. 이 스크립트를 사용 하려면 터미널 창(콘솔)을 열고, OmegaT.jar 및 리눅스 install.sh 스크립트를 포함 하는 폴더에./linux-install.sh 스크립트를 실행합니다.

* Mac 사용자: OmegaT.zip 적당한 위치에 압축을 풀어, HTML 문서 인덱스 파일 및 OmegaT.app, 응용 프로그램 파일을 포함 하는 폴더를 생성합니다.

* 기타 설치 (예를 들어, Solaris, FreeBSD) : OmegaT를 설치하려면 OmegaT에 대한 적절한 폴더를 만듭니다. 이 폴더에 OmegaT 압축 또는 tar.bz2  복사하고 압축을 풉니다.

3.3OmegaT 실행
OmegaT를 다음과 같이 시작합니다.

* 윈도우 사용자 : 설치하는 동안 바탕 화면에 바로 가기를 만든 경우, 해당 바로 가기를 두 번 클릭 합니다. 아니면, OmegaT.exe 파일을 두 번 클릭 합니다. 파일이 OmegaT.exe가 아니라 OmegaT로만 보이면, 파일 관리자 (Windows 탐색기)에 파일 확장명이 표시되도록 설정을 변경 합니다.

* 리눅스 사용자: 제공 된 설치 스크립트를 사용하는 경우, OmegaT 시작: Alt+F2 누르고: omegat

* Mac 사용자: OmegaT.app 파일을 두 번 클릭합니다.

* 파일 매니저 (모든 시스템)에서: OmegaT.jar 파일을 두 번 클릭합니다. 이것은 .jar 파일 형식이 자바와 연결되는 경우에 작동합니다.

* 명령줄 에서(모든 시스템) : OmegaT를 실행 하는 명령:

cd <OmegaT.jar 파일이 위치한 폴더>

<자바 실행파일> -jar OmegaT.jar

(Java 실행 파일은 리눅스에서 file java, 윈도우에서 java.exe자바가 시스템 수준에서 설치되어 있는 경우, 명령 경로에 전체 경로 입력이 필요 하지 않을 수 있습니다.)

사용자 정의 OmegaT 실행 :

* 윈도우 사용자 : 설치 프로그램 시작 메뉴, 바탕 화면 및 빠른 실행 영역에서 바로가기를 만들 수 있습니다. 또한 수동으로 시작 메뉴, 바탕 화면 또는빠른 실행 영역에 OmegaT.exe 파일을 드래그해 링크를 만들 수 있습니다.

* 리눅스 사용자: OmegaT의 사용자 더 우호적인 Kaptain 스크립트(omegat.kaptn)를 사용할 수 있습니다. 이 스크립트를 사용하려면 먼저 Kaptain을 설치해야 합니다. 다음 Alt+F2 > omegat.kaptn으로 Kaptain 실행 스크립트를 시작할 수 있습니다.

자세한 내용은 Kaptain 스크립트와 리눅스에 메뉴 항목 및 진입 아이콘 추가, 리눅스 하우투에 OmegaT를 참조 하십시오.

Mac 사용자: OmegaT.app를 독에 드래그하거나, 아무데서나 파인더 윈도우의 도구 막대로 시작할 수 있습니다. 또한 Spotlight 검색 필드에 호출할 수 있습니다.

==============================================================================
 4. OmegaT 프로젝트에 기여

OmegaT의 개발에 참여하려면, 개발자에게 연락하세요:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

OmegaT의 사용자 인터페이스, 사용자 설명서 또는 다른 관련된 문서를 번역하려면 참조하세요 :
      
      http://www.omegat.org/en/howtos/localizing_omegat.php

그리고 번역자의 리스트에 가입하세요 :
      https://lists.sourceforge.net/lists/listinfo/omegat-l10n

다른 방법으로 기여하려면 먼저 사용자 그룹에 가입하세요 :
      http://tech.groups.yahoo.com/group/omegat/

그리고 OmegaT 세계에서 어떤 느낌을 받으세요...

  OmegaT는 원래 Keith Godfrey의 작품입니다.
  Marc Prior는 OmegaT 프로젝트의 코디네이터입니다.

이전 참여자 포함 : (알파벳 순서)

코드를 제공
  Zoltan Bartko
  Volker Berlin
  Didier Briel (개발 매니저)
  Kim Bruning
  Alex Buloichik (수석 개발자)
  Sandra Jean Chua
  Thomas Cordonnier
  Martin Fleurke  
  Wildrich Fourie
  Phillip Hall
  Jean-Christophe Helary
  Thomas Huriaux
  Hans-Peter Jacobs
  Kyle Katarn
  Ibai Lakunza Velasco
  Guido Leenders
  Aaron Madlon-Kay
  Fabián Mandelbaum
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
  Martin Wunderlich
  Michael Zakharov

Other contributions by
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (지역화 관리자)
  Vito Smolej (문서 관리자)
  Samuel Murray
  Marc Prior 
  그리고 도움을 준 사람이 많이 더 많이 있습니다

(만약 당신이 OmegaT 프로젝트에 크게 기여 했지만 당신의 이름이 목록에 나타나지 않으면, 저희에게 자유롭게 연락주세요.)

OmegaT는 다음과 같은 라이브러리를 사용합니다.
  HTMLParser 1.6 by Somik Raha, Derrick Oswald and others (LGPL License)
  MRJ Adapter 1.0.8 by Steve Roy (LGPL License)
  VLDocking Framework 2.1.4 by VLSolutions (CeCILL License)
  Hunspell by László Németh and others (LGPL License)
  JNA by Todd Fast, Timothy Wall and others (LGPL License)
  Swing-Layout 1.0.2 (LGPL License)
  Jmyspell 2.1.4 (LGPL License)
  SVNKit 1.7.5 (TMate License)
  Sequence Library (Sequence Library License)
  ANTLR 3.4 (ANTLR 3 license)
  SQLJet 1.1.3 (GPL v2)
  JGit (Eclipse Distribution License)
  JSch (JSch License)
  Base64 (public domain)
  Diff (GPL)
  orion-ssh2-214 (Orion SSH for Java license)
  lucene-*.jar (Apache License 2.0)
  The English tokenizers (org.omegat.tokenizer.SnowballEnglishTokenizer and
  org.omegat.tokenizer.LuceneEnglishTokenizer) use stop words from Okapi
(http://okapi.sourceforge.net) (LGPL license)
  tinysegmenter.jar (Modified BSD license)
  commons-*.jar (Apache License 2.0)
  jWordSplitter (Apache License 2.0)
  LanguageTool.jar (LGPL license)
  morfologik-*.jar (Morfologik license)
  segment-1.4.1.jar (Segment license)
  pdfbox-app-1.8.1.jar (Apache License 2.0)

==============================================================================
 5.  OmegaT에 버그가 있으면?도움이 필요하세요?

버그를 보고 하기 전에 설명서를 철저하게 확인하세요. 발견한게 그냥 OmegaT의 특성일 수 있습니다. 만약 OmegaT 로그를 확인 하 고 "오류", "경고", "예외" 같은 단어를 보거나 또는 "예기치 않게 사망"이면, 아마 진짜 문제를 발견한 겁니다 (log.txt는 사용자 환경 설정 폴더에 위치하고, 그것의 위치에 대한건 설명서를 참조 하세요).

이것을 다른 사용자가 이미 보고했는지 확인 하려면SourceForge에서 확인할 수 있습니다. 일부 재현 시퀀스를 첫번째로 발견했다고 당신이 확신하는 경우에만  버그 리포트를 제출해야 합니다.

모든 좋은 버그 리포트는 정확하게 세 가지가 필요합니다.
  - 재현되는 단계
  - 원래 무엇을 기대했는데
   - 대신 무었을 보았는지.

파일, 로그, 스크린샷의 일부 복사본을 추가하면, 당신이 생각하는 것을 찾고 버그를 수정하는 개발자에게 도움이됩니다.

사용자 그룹의 아카이브를 검색 하려면 다음으로 이동 :
     http://tech.groups.yahoo.com/group/OmegaT/

버그 보고서 페이지를 탐색하고 필요한 경우 새로운 버그 리포트하려면, 다음으로 이동:
     https://sourceforge.net/p/omegat/bugs/

여러분의 버그 리포트를 추적 하려면, Source Forge 사용자로 등록 하셔도 됩니다.

==============================================================================
6.   릴리스 세부 정보

'changes.txt' 파일에서 이전 버전에서의 변경점과 추가사항의 자세한 내용을 확인하세요.


==============================================================================
