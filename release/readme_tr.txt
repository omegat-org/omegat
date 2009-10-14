Bu çeviri, Erhan Yükselci'ye aittir; copyright© 2009.

==============================================================================
  OmegaT 2.0 Beni Oku dosyası

  1.  OmegaT hakkında bilgi
  2.  OmegaT nedir?
  3.  OmegaT'nin kurulumu
  4.  OmegaT'ye katkıda bulunma
  5.  OmegaT'de hata mı buldunuz? Yardıma mı ihtiyacınız var?
  6.  Sürümle ilgili bilgiler

==============================================================================
  1.  OmegaT hakkında bilgi


OmegaT hakkındaki en güncel bilgilere şu adresten ulaşabilirsiniz
      http://www.omegat.org/

Arşivlerde arama yapma imkanı olan Yahoo kullanıcı grubundan (çok dilli)
kullanıcı desteği alabilirsiniz:
     http://groups.yahoo.com/group/OmegaT/

Özellik İstekleri (İngilizce) SourceForge sitesinden yapılabilir:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Hata raporları (İngilizce) SourceForge sitesinden gönderilebilir:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  OmegaT nedir?

OmegaT, Bilgisayar Destekli Çeviri (BDÇ) aracıdır. Ücretsizdir, yani profesyonel
kullanım için bile herhangi bir ücret ödemeniz gerekmez; özgürlüğünüzü
kısıtlamaz, yani kullanıcı lisansına riayet ettiğiniz sürece bu programı
değiştirebilir ve/veya yeniden dağıtabilirsiniz.

OmegaT'nin temel özellikleri şunlardır:
  - Java desteği olan tüm işletim sistemlerinde çalışır
  - geçerli her türlü TMX dosyasını çeviri belleği olarak kullanabilir
  - esnek cümle temelli bölümlendirmeye (SRX benzeri bir yöntemle) sahiptir
  - projede ve referans olarak kullanılan çeviri belleklerinde arama yapabilir
  - herhangi bir dizinde bulunan desteklenen formatlardaki dosyalarda arama
yapabilir 
  - bulanık eşleşme yapabilir
  - karmaşık dizin hiyerarşilerine sahip projelerde sorunsuz çalışır
  - sözlük desteğine (terminoloji kontrolü) sahiptir 
  - Açık Kaynak kodlu yazım kontrolü programlarını kolaylıkla kullanır
  - StarDict sözlüklerini destekler
  - Google Translate makine çevirisi hizmetlerini destekler
  - anlaşılır ve kapsamlı yardım ve destek belgelerine sahiptir
  - çok sayıda dilde yerelleştirilmiştir.

OmegaT aşağıdaki dosya türlerinin çevirisini direkt olarak destekler:
  - Düz metin
  - HTML ve XHTML
  - HTML Yardım Derleyicisi
  - OpenDocument/OpenOffice.org
  - Java özkaynak desteleri (.properties)
  - INI dosyaları (herhangi bir kodlamaya sahip olan anahtar=değer şeklinde
  çiftleri içeren dosyalar)
  - PO dosyaları
  - DocBook belgelendirme dosya formatı
  - Microsoft OpenXML dosyaları
  - Okapi tekdilli XLIFF dosyaları
  - QuarkXPress CopyFlowGold
  - Altyazı dosyaları (SRT)
  - ResX
  - Android kaynak dosyaları
  - LaTeX

Ayrıca OmegaT, başka formattaki dosyaları da destekleyecek şekilde
özelleştirilebilir.

OmegaT, en karmaşık kaynak dizini hiyerarşilerini bile otomatik olarak
ayrıştırarak desteklenen tüm dosyalara ulaşır ve desteklenmeyen dosyaları da
kopyalamak suretiyle tam olarak aynı yapıya sahip bir hedef dizini oluşturur.

Hızlı başlangıç öğreticisi için OmegaT'yi çalıştırın ve görüntülenen "Hızlı
Başlangıç Öğreticisini" okuyun.

Kullanma kılavuzu indirdiğiniz paketin içinde yer almaktadır; OmegaT'yi
başlattıktan sonra [Yardım] menüsünden kullanma kılavuzuna ulaşabilirsiniz.

==============================================================================
 3. OmegaT'nin kurulumu

3.1 General
OmegaT'nin çalışması için Java Runtime Ortamı (JRE) 1.5 ya da üzeri sürümünün
sisteminizde yüklü olması gereklidir. Kullanıcıları, JRE temini derdinden kurtarmak
için OmegaT artık standart olarak JRE ile birlikte temin edilmektedir. 

Eğer sisteminizde Java kurulu ise, OmegaT'nin mevcut sürümünü kurmanız en
basit yolu, Java Web Start'ı kullanmaktır. 
Bunun için aşağıdaki dosyayı indirip çalıştırın:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

İlk çalıştırmada bilgisayarınız için uygun ortamı ve uygulanın kendisini kuracaktır. 
Daha sonraki kullanımların çevrimiçi olması şart değildir.

Kurulum sırasında, işletim sisteminize bağlı olarak birtakım güvenlik uyarıları
alabilirsiniz. Güvenlik sertifikası "Didier Briel" tarafından imzalanmıştır. 
Bu sürüme verdiğiniz izin ve yetkilendirmeler ("bilgisayara kısıtsız erişim" şeklinde
ifade edilebilir) aşağıdaki anlatılan prosedürle yaptığınız yerel kurulum için verdiğiniz
izin ve yetkilendirmelerle aynıdır ve bu izin ve yetkilendirmeler bilgisayarın sabit
diskine erişimi mümkün kılmaktadır. Daha sonra OmegaT.jnlp dosyasının her
tıkladığınızda güncelleme olup olmadığı kontrol edilir ve güncelleme varsa bunlar
yüklenir ve OmegaT başlatılır. 

OmegaT'yi indirme ve kurma konusunda alternatif yollar aşağıda gösterilmiştir. 

Windows ve Linux kullanıcıları: sisteminizde uygun bir JRE yüklü olduğundan
eminseniz OmegaT'nin JRE'siz sürümünü (bu durum sürümün adında
"Without_JRE" olarak belirtilir) kurabilirsiniz. 
Eğer hangi sürümü seçmeniz gerektiği konusunda emin değilseniz "standart"
sürümü yani JRE ile geleni kullanmanızı tavsiye ederiz. Bu güvenli bir yöntemdir,
çünkü JRE sisteminizde yüklü olsa bile bu sürüm ayrı bir yere kurulacak ve
sisteminizde geçerli olan sürüme müdahale etmeyecektir.

Linux kullanıcıları: OmegaT'nin birçok Linux dağıtımında (örneğin, Ubuntu)
bulunan özgür/açık kaynak kodlu Java uyarlamalarıyla çalışmaz, çünkü bu
uyarlamalar ya güncel ya da yeterince gelişmiş değildir. Yukarıdaki bağlantıyı
kullanarak Sun'ın Java Runtime Ortamını (JRE) indirin ve kurun ya da JRE ile
paketlenmiş OmegaT'yi ("Linux" ile işaretlenmiş .tar.gz paketi) indirin ve kurun.

Mac kullanıcıları: Java halihazırda Mac OS X'de yüklüdür.

PowerPC sistemlerinde Linux kullananlar: Sun, PPC sistemler için JRE
üretmediği için kullanıcıların IBM'nin JRE'sini indirmesi gerekecektir. IBM'nin
JRE'sini aşağıdaki adresten indirebilirsiniz:

    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Kurulum
* Windows kullanıcıları: Sadece kurulum programını başlatmanız yeterlidir. 
isterseniz kurulum programı OmegaT'yi çalıştırmanız için kısayollar oluşturabilir.
* Diğerleri: OmegaT'yi kurmak için OmegaT için uygun bir dizin oluşturun (sözgelimi
Linux için /usr/local/lib altında bir dizin oluşturabilirsiniz). OmegatT.zip ya da tar.gz
arşivini bu dizine açın.

3.3 OmegaT'nin çalıştırılması
OmegaT çeşitli şekillerde çalıştırılabilir.

* Windows kullanıcıları: OmegaT.exe dosyasına çift tıklayarak. Dosya Yöneticisinde
(Windows Explorer) OmegaT dosyasını görebiliyor, ama OmegaT.exe dosyasını
göremiyorsanız, ayarlarınızı değiştirerek dosya uzantılarının görünür hale gelmesini
sağlayın.

* OmegaT.jar adlı dosyaya çift tıklayarak. Bu yöntem, eğer sisteminizde .jar uzantısı
Java ile ilişkilendirilmişse kullanılabilir.

* Komut satırından. OmegaT'yi başlatmak için gerekli komut şudur:

cd <OmegaT.jar adlı dosyanın bulunduğu dizin>

<Java çalıştırılabilir dosyasının adı ve dosya yolu> -jar OmegaT.jar

(Java çalıştırılabilir dosyası, Linux sistemlerinde java, Windows sistemlerinde ise
java.exe adlı dosyadır.
Eğer Java tüm sistem seviyesinde kurulmuş ise, tam dosya yolunun belirtilmesine
gerek yoktur).

* Windows kullanıcıları: Kurulum programı sizin için başlangıç menüsünde, masaüstünde
ve hızlı başlat alanında kısayollar oluşturabilir. Alternatif olarak OmegaT.exe dosyasını manüel
olarak masaüstüne ya da hızlı başlat alanına taşımak suretiyle bağlantı
oluşturabilirsiniz.

* Linux KDE kullanıcıları: OmegaT'yi menüye şu şekilde ekleyebilirsiniz:

Kontrol Merkezi - Masaüstü - Paneller - Menüler - K Menüyü Düzenle - Dosya - Yeni
Girdi/Yeni Altmenü.

Daha sonra, uygun menüyü seçip Dosya - Yeni Altmenü ve Dosya - Yeni Girdi adımlarıyla
altmenü/yeni girdi ekleyebilirsiniz. Yeni girdi adı olarak OmegaT yazın.

"Komut" alanında gözat düğmesini kullanarak OmegaT başlatma betiğini bulup seçin. 

Simge düğmesine Diğer Simgeler - Gözat tıklayın (Ad/Açıklama/Yorum alanlarının
sağında) ve Omega uygulamasının dizinindeki /images altdizinine gidin. OmegaT.png
simgesini seçin.

Son olarak, Dosya - Kaydet komutlarıyla değişiklikleri kaydedin.

* Linux GNOME kullanıcıları: OmegaT'yi panelinize (ekranın üst kısmındaki çubuk) şu
şekilde ekleyebilirsiniz:

Panel üzerindeyken farenin sağ tuşuna tıklayın ve Yeni Başlatıcı Ekle menüsünü seçin. 
"Ad" kısmına OmegaT yazın ve "Komut" alanında gözatma düğmesini kullanarak OmegaT
başlatma betiğini bulun. Betiği seçin ve Tamam düğmesini tıklayın.

==============================================================================
 4. OmegaT projesini katılmak

OmegaT'nin geliştirilmesini katılmak için şu adresten geliştiricilerle
irtibat kurabilirsiniz:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

OmegaT'nin kullanıcı ara yüzü, kullanma kılavuzu ya da diğer belgelerini
tercüme etmek için şu belgeyi okuyun:
      
      http://www.omegat.org/en/translation-info.html

ve şu adresten çevirmenler listesine abone olun:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Diğer türdeki katkılar için lütfen ilk önce aşağıdaki adresten kullanıcı
grubuna abone olun:
      http://tech.groups.yahoo.com/group/omegat/

ve OmegaT dünyasında neler olduğunu tecrübe edin...

  OmegaT ilk olarak Keith Godfrey tarafından geliştirilmiştir.
  Marc Prior, OmegaT projesinin koordinatörüdür.

Katkıda bulunanlar aşağıda belirtilmektedir:
(alfabetik sıra ile)

Koda katkıda bulunanlar:
  Zoltan Bartko
  Didier Briel (sürüm yöneticisi)
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
  Andrzej Sawuła
  Benjamin Siband
  Martin Wunderlich

Diğer katkılar:
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (yerelleştirme yöneticisi)
  Vito Smolej (dokümantasyon yöneticisi)
  Samuel Murray
  Marc Prior 
  ve çok sayıda son derece yardımcı insan.

(Eğer OmegaT Projesine önemli bir katkıda bulunmuşsanız, ama adınız yukarıdaki
listede yer almıyorsa bizimle irtibata geçebilirsiniz).

OmegaT aşağıdaki kitaplıkları kullanmaktadır:

  Somik Raha, Derrick Oswald ve diğerleri tarafından geliştirilen HTMLParser
  (LGPL Lisansı).
  http://sourceforge.net/projects/htmlparser

  Steve Roy tarafından geliştirilen MRJ Adapter 1.0.8 (LGPL Lisansı)
  http://homepage.mac.com/sroy/mrjadapter/

  VLSolutions tarafından geliştirilen VLDocking Framework 2.1.4 (CeCILL Lisansı)
  http://www.vlsolutions.com/en/products/docking/

  László Németh ve diğerleri tarafından geliştirilen Hunspell (LGPL Lisansı)

  Todd Fast, Timothy Wall ve diğerleri tarafından geliştirilen JNA (LGPL Lisansı)

  Swing-Layout 1.0.2 (LGPL Lisansı)

  Jmyspell 2.1.4 (LGPL Lisansı)

  JAXB 2.1.7 (GPLv2 + classpath exception)

==============================================================================
 5.  OmegaT'de hata mı buldunuz? Yardıma mı ihtiyacınız var?

Herhangi bir hata bildiriminde bulunmadan önce yardım belgelerini tamamen
kontrol ettiğinizden emin olun. Karşılaştığınız şey, OmegaT'nin bir özelliği
olabilir. Eğer OmegaT kütük dosyasını kontrol eder ve "Error" (hata), "Warning"
(uyarı), "Exeption" (istisna) ya da "died unexpectedly" (beklenmedik şekilde sonlandı)
gibi ifadeler görürseniz o zaman yeni bir sorun keşfetmişsiniz demektir (kütük dosyası
olan log.txt, kullanıcı tercihleri dizininde bulunur; tam konumu için kılavuza bakınız).

Daha sonra ise bulduğunuz sorununun daha önce bildirilip bildirilmediğini
anlamak için diğer kullanıcılar görüş alışverişinde bulunmaktır. SourceForge'da
hata raporu sayfasından da sorunu teyit edebilirsiniz. Bütün bu kontrollerden
sonra eğer olmaması gereken bir şeye yol açan ve tekrar üretilebilir bir
olaylar dizisini ilk bulanın siz olduğundan emin iseniz bir hata raporu
yazmanız iyi olur.

İyi bir hata raporu yazmak için tam olarak üç şeye ihtiyaç vardır.
  - Hatayı tekrar ortaya çıkarmak için gerekli adımlar,
  - Ne bekliyordunuz ve
  - Bunun yerine ne gördünüz.

Dosyalar, kütük dosyalarının ilgili bölümleri, ekran görüntüleri ve hatanın
bulunması ve giderilmesi konusunda geliştiricilere yardımcı olabileceğini
düşündüğünüz diğer şeyleri de ekleyebilirsiniz.

Kullanıcı grubunun arşivlerini görmek için aşağıdaki adrese gidin:
     http://groups.yahoo.com/group/OmegaT/

Hata raporu sayfasını görmek ve gerekirse hata raporu bildirmek için aşağıdaki
adrese gidin:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Bildirdiğiniz hata raporu konusundaki gelişmeleri takip etmek için SourceForge
kullanıcısı olarak kayıt olmanız gerekir.

==============================================================================
6.   Sürümle ilgili bilgiler

Bu ve önceki sürümlerdeki değişiklikler hakkında ayrıntı bilgi almak için
'changes.txt' dosyasına bakın.


==============================================================================