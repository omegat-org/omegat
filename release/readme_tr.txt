Bu çeviri, Erhan Yükselci'ye aittir; telif hakkı © 2007.

==============================================================================
  OmegaT 1.6.2 Beni Oku dosyası

  1.  OmegaT hakkında bilgi
  2.  OmegaT nedir?
  3.  Java ve OmegaT hakkında genel notlar
  4.  OmegaT'ye katkıda bulunma 
  5.  OmegaT'de hata mı buldunuz? Yardıma mı ihtiyacınız var?
  6.  Sürümle ilgili bilgiler

==============================================================================
  1.  OmegaT hakkında bilgi


OmegaT hakkındaki en güncel bilgilere şu adresten ulaşabilirsiniz (İngilizce,
Slovakça, Hollandaca, Portekizce):
      http://www.omegat.org/omegat/omegat.html

Arşivlerde arama yapma imkanı olan Yahoo kullanıcı grubundan (çok dilli)
kullanıcı desteği alabilirsiniz:
     http://groups.yahoo.com/group/OmegaT/

Özellik İstekleri (İngilizce) SourceForge sitesinden yapılabilir:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Hata raporları (İngilizce) SourceForge sitesinden gönderilebilir:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  OmegaT nedir?

OmegaT, Bilgisayar Destekli Çeviri aracıdır. Ücretsizdir, yani profesyonel
kullanım için bile herhangi bir ücret ödemeniz gerekmez; özgürlüğünüzü
kısıtlamaz, yani kullanıcı lisansına riayet ettiğiniz sürece bu programı
değiştirebilir ve/veya yeniden dağıtabilirsiniz.

OmegaT'nin temel özellikleri şunlardır:
  - Java desteği olan tüm işletim sistemlerinde çalışır,
  - geçerli her türlü TMX dosyasını çeviri belleği olarak kullanabilir,
  - esnek cümle temelli bölümlendirmeye (SRX benzeri bir yöntemle) sahiptir,
  - projede ve referans olarak kullanılan çeviri belleklerinde arama
  yapabilir,
  - OmegaT tarafından okunabilen dosyaların bulunduğu bir dizinde arama yapabilir,
  - bulanık eşleşme yapabilir,
  - karmaşık dizin hiyerarşilerine sahip projelerde sorunsuz çalışır,
  - sözlük desteğine (terminoloji kontrolü) sahiptir,
  - anlaşılması kolay yardım ve destek belgelerine sahiptir,
  - çok sayıda dilde yerelleştirilmiştir.

OmegaT; OpenDocument dosyaları, Microsoft Office dosyaları (OpenOffice.org'un
dönüşümde kullanılması suretiyle ya da HTML'ye dönüşüm yapmak suretiyle),
OpenOffice.org ve StarOffice dosyalarının yanı sıra (X)HTML, Java yerelleştirme
dosyaları ve düz metin dosyalarını ve daha birçok dosyayı desteklemektedir.

OmegaT, en karmaşık kaynak dizini hiyerarşilerini bile otomatik olarak
ayrıştırarak desteklenen tüm dosyalara ulaşır ve desteklenmeyen dosyaları da
kopyalamak suretiyle tam olarak aynı yapıya sahip bir hedef dizini oluşturur.

Hızlı başlangıç öğreticisi için OmegaT'yi çalıştırın ve görüntülenen "Hızlı
Başlangıç Öğreticisini" okuyun.

Kullanma kılavuzu indirdiğiniz paketin içinde yer almaktadır; OmegaT'yi
başlattıktan sonra [Yardım] menüsünden kullanma kılavuzuna ulaşabilirsiniz.

==============================================================================
 3. Java ve OmegaT hakkında genel notlar

OmegaT'nin çalışması için Java Runtime Ortamı 1.4 ya da üzeri sürümünün
sisteminizde yüklü olması gereklidir. Java'yı şu adresten edinebilirsiniz:
    http://java.com

Java yüklü değilse Windows ve Linux kullanıcılarının Javayı yüklemeleri
gerekebilir.
OmegaT projesi ayrıca Java'lı sürümler de sunmaktadır. MacOSX kullanıcıları için Java halihazırda sistemlerinde yüklüdür.

Kurulumun düzgün yapıldığı bir sistem OmegaT.jar dosyasına tıklayarak OmegaT'yi
başlatabilirsiniz.

Java'yı kurduktan sonra 'java' uygulamasının bulunduğu dizini içerecek şekilde
sistem yol değişkenini değiştirmeniz gerekebilir.

Linux kullanıcıları, OmegaT'nin birçok Linux dağıtımında (örneğin, Ubuntu)
bulunabilecek özgür/açık kaynak kodlu Java uyarlamalarıyla çalışmadığına dikkat
etmelidir, çünkü bu uyarlamalar ya güncelliklerini yitirmişlerdir ya da
yeterince gelişmiş değildir. Yukarıdaki bağlantıyı kullanarak Sun'ın Java
Runtime Ortamını (JRE) indirin ve kurun ya da JRE ile paketlenmiş OmegaT'yi
("Linux" ile işaretlenmiş .tar.gz paketi) indirin ve kurun.

PowerPC sistemlerinde Linux kullanılıyorsa Sun, PPC sistemler için JRE
üretmediği için kullanıcıların IBM'nin JRE'sini indirmesi gerekecektir. IBM'nin
JRE'sini aşağıdaki adresten indirebilirsiniz:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html

==============================================================================
 4. OmegaT'ye katkıda bulunma 

OmegaT'nin geliştirilmesini katkıda bulunmak için şu adresten geliştiricilerle
irtibat kurabilirsiniz:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

OmegaT'nin kullanıcı ara yüzü, kullanma kılavuzu ya da diğer belgelerini
tercüme etmek için şu belgeyi okuyun:
      http://www.omegat.org/omegat/omegat_en/translation-info.html

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
  Didier Briel
  Kim Bruning
  Sacha Chua
  Thomas Huriaux
  Maxym Mykhalchuk (baş geliştirici)
  Henry Pijffers (sürüm yöneticisi)
  Benjamin Siband
  Martin Wunderlich

Yerelleştirmeye katkıda bulunanlar:
  Roberto Argus (Portekizce)
  Alessandro Cattelan (İtalyanca)
  Sabine Cretella (Almanca)
  Suzanne Bolduc (Esperanto)
  Didier Briel (Fransızca)
  Frederik De Vos (Hollandaca)
  Cesar Escribano Esteban (İspanyolca)
  Dmitri Gabinski (Belarusça, Esperanto ve Rusça)
  Takayuki Hayashi (Japonca)
  Jean-Christophe Helary (Fransızca ve Japonca)
  Yutaka Kachi (Japonca)
  Elina Lagoudaki (Yunanca)
  Martin Lukáč (Slovakça)
  Samuel Murray (Afrikaanca)
  Yoshi Nakayama (Japonca)
  David Olveira (Portekizce)
  Ronaldo Radunz (Portekizce)
  Thelma L. Sabim (Portekizce)
  Juan Salcines (İspanyolca)
  Pablo Roca Santiagio (İspanyolca)
  Karsten Voss (Polonyaca)
  Gerard van der Weyde (Hollandaca)
  Martin Wunderlich (Almanca)
  Hisashi Yanagida (Japonca)
  Kunihiko Yokota (Japonca)
  Erhan Yükselci (Türkçe)
  Dragomir Kovacevic (Sırpça-Hırvatça)
  Claudio Nasso (İtalyanca)
  Ahmet Murati (Arnavutça)
  Sonja Tomaskovic (Almanca)

Diğer katkılar:
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (şu anki dokümantasyon yöneticisi)
  Samuel Murray
  Marc Prior (şu anki yerelleştirme yöneticisi)
  ve çok sayıda son derece yardımcı insan.

(Eğer OmegaT Projesine önemli bir katkıda bulunmuşsanız, ama adınız yukarıdaki
listede yer almıyorsa bizimle irtibata geçebilirsiniz).

OmegaT aşağıdaki kitaplıkları kullanmaktadır:
  Somik Raha, Derrick Oswald ve diğerleri tarafından geliştirilen HTMLParser
  (LGPL Lisansı).
  http://sourceforge.net/projects/htmlparser

  Steve Roy tarafından geliştirilen MRJ Adapter (LGPL Lisansı).
  http://homepage.mac.com/sroy/mrjadapter/

  VLSolutions tarafından geliştirilen VLDocking Framework (CeCILL Lisansı).
  http://www.vlsolutions.com/en/products/docking/

==============================================================================
 5.  OmegaT'de hata mı buldunuz? Yardıma mı ihtiyacınız var?

Herhangi bir hata bildiriminde bulunmadan önce yardım belgelerini tamamen
kontrol ettiğinizden emin olun. Karşılaştığınız şey, OmegaT'nin bir özelliği
olabilir. Eğer OmegaT kütük dosyasını kontrol eder ve "Error" (hata), "Warning"
(uyarı) ya da "died unexpectedly" (beklenmedik şekilde sonlandı) gibi ifadeler
görürseniz o zaman bir şeylerin peşindesiniz demektir (kütük dosyası olan
log.txt, kullanıcı tercihleri dizininde bulunur; tam konumu için kılavuza
bakınız).

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

5.  OmegaT'de hata mı buldunuz? Yardıma mı ihtiyacınız var?

Herhangi bir hata bildiriminde bulunmadan önce yardım belgelerini tamamen
kontrol ettiğinizden emin olun. Karşılaştığınız şey, OmegaT'nin bir özelliği
olabilir. Eğer OmegaT kütük dosyasını kontrol eder ve "Error" (hata), "Warning"
(uyarı) ya da "died unexpectedly" (beklenmedik şekilde sonlandı) gibi ifadeler
görürseniz o zaman bir şeylerin peşindesiniz demektir (kütük dosyası olan
log.txt, kullanıcı tercihleri dizininde bulunur; tam konumu için kılavuza
bakınız).

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
     http://tech.groups.yahoo.com/group/omegat/

Hata raporu sayfasını görmek ve gerekirse hata raporu bildirmek için aşağıdaki
adrese gidin:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Bildirdiğiniz hata raporu konusundaki gelişmeleri takip etmek için SourceForge
kullanıcısı olarak kayıt olmanız gerekir.

==============================================================================
6.   Sürümle ilgili bilgiler

Bu ve önceki sürümlerdeki değişiklikler hakkında ayrıntı bilgi almak için
'changes.txt' dosyasına bakın.

Desteklenen dosya formatları:
  - Düz metin
  - HTML ve XHTML
  - HTML Yardım Derleyicisi (HHC)
  - OpenDocument / OpenOffice.org
  - Java özkaynak desteleri (.properties)
  - INI dosyaları (herhangi bir kodlamaya sahip olan anahtar=değer şeklinde
  çiftleri içeren dosyalar)
  - PO dosyaları
  - DocBook belgelendirme dosya formatı
  - Microsoft OpenXML dosyaları

Temel değişiklikler:
  - Esnek (cümle temelli) dilimlendirme
  - Dosya formatı filtreleri, eklentiler şeklinde oluşturulabilir
  - Daha fazla açıklama eklenerek kod yeniden düzenlendi
  - Windows kurulum dosyası
  - HTML etiketlerinin özelliklerini çevirmek mümkün
  - TMX 1.1-1.4b Seviye 1'e tam uygunluk
  - TMX 1.4b Seviye 2'ye tam uygunluk

Yeni kullanıcı arabirimi özellikleri (OmegaT 1.4 serisi ile
karşılaştırıldığında):
  - Arama arayüzü daha gelişmiş özelliklerle yeniden yazıldı
  - Yapışık pencereler kullanılarak ara arayüz iyileştirildi

==============================================================================

