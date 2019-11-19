@@ÇEVİRİ UYARISI@@


OmegaT nedir?
===============

OmegaT özgür ve açık kaynak kodlu her platformda çalışan bir Bilgisayar Destekli
Çeviri aracıdır; bulanık eşleşme, çeviri belleği, anahtar sözcük arama, sözlükçe ve
proje güncelleme gibi özelliklere sahiptir.



Lisans bilgileri
=====================

OmegaT'yi Özgür Yazılım Vakfı tarafından yayınlanan GNU Genel Kamu
Lisansının 3. ya da (tercihinize bağlı olarak) daha sonraki bir sürümü altında
yeniden dağıtabilir ve/veya değiştirebilirsiniz. Lisansın metnini şurada bulabilirsiniz:
/docs/OmegaT-license.txt.

OmegaT bazı kitaplıklar kullanmaktadır. Her kitaplığın lisansı şurada belirtilmektedir:
in /lib/licenses/Licenses.txt.



OmegaT kurulumunun ön koşulları
===================================

OmegaT, Java Çalışma Zamanı Ortamı (JRE) 1.8 ya da üzeri sürümünün
sisteminizde yüklü olmasını gerektirir.

JRE seçmek, bulmak ve kurmakla uğraşmamak için JRE ile birlikte gelen
OmegaT paketlerini kullanmanızı öneririz.



OmegaT kurulumu (Windows)
===========================

Kurulum programını başlatın.



OmegaT kurulumu (Mac)
=======================

OmegaT .zip arşivini açtığınız klasörde bir belgelendirme dosyası
ve OmegaT uygulaması bulunur. Klasörü uygun bir yere
(söz gelimi Uygulamalar klasörüne) taşıyın.



OmegaT kurulumu (Linux)
=========================

Arşivi uygun bir klasöre koyup açın. OmegaT başlatılmak için
hazır olacaktır.

Bununla birlikte kurulum betiğini (linux-install.sh) kullanırsanız daha temiz ve
daha kullanıcı dostu bir kurulum elde edebilirsiniz. Bu betiği kullanmak için
bir terminal penceresi (konsol) açın, klasörü OmegaT.jar dosyasını ve linux-install.sh
betiğini içeren klasöre değiştirin ve ./linux-install.sh komutuyla betiği
çalıştırın.



OmegaT kurulumu (Solaris, FreeBSD, ...)
=========================================

Arşivi uygun bir klasöre koyup açın. OmegaT başlatılmak için
hazır olacaktır.



OmegaT'nin Java Ağ Başlatıcı kullanılarak kurulması (tüm platformlar)
===========================================================

Eğer sisteminizde Java kurulu ise, Java Ağ Başlatıcıyı kullanarak OmegaT'yi kurabilirsiniz.

Bunun için aşağıdaki dosyayı indirip çalıştırın:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

İlk çalıştırmada bilgisayarınız için uygun ortamı ve uygulamanın kendisini
kuracaktır. Daha sonraki kullanımlarda çevrim içi olmak şart değildir.



OmegaT'nin başlatılması (Windows)
==============================

Eğer kurulum sırasında masaüstünde bir kısayol oluşturduysanız bu kısayola
çift tıklayın.

Kurulum programı sizin için başlangıç menüsünde, masaüstünde ve hızlı
başlat alanında kısayollar oluşturabilir. Alternatif olarak OmegaT.exe
dosyasını manüel olarak masaüstüne ya da hızlı başlat alanına taşımak suretiyle
bağlantı oluşturabilirsiniz.

Dosya Yöneticisinde (Windows Explorer) OmegaT dosyasını görebiliyor,
ama OmegaT.exe dosyasını göremiyorsanız, ayarlarınızı değiştirerek dosya
uzantılarının görünür hale gelmesini sağlayın.



OmegaT'nin başlatılması (Mac)
==========================

OmegaT uygulamasına çift tıklayın.

OmegaT uygulamasını dosyasını sabitleme yuvasına ya da bir Finder penceresinin
araç çubuğuna sürükleyerek uygulamayı herhangi bir konumdan açabilirsiniz. Ayrıca Spotlight arama alanından da çağırabilirsiniz.



OmegaT'nin başlatılması (Linux)
===========================

Eğer linux-install.sh betiğini kullandıysanız OmegaT'yi başlatmak için önce:

  Alt+F2

tuşuna basın ve sonra:

  omegat

OmegaT'nin daha kullanıcı dostu şekilde başlatılması için Kaptain betiğini (omegat.kaptn) kullanabilirsiniz. Bu betiği kullanmak için önce Kaptain'i
kurmanız gereklidir. Sonra Kaptain başlatma betiğini aşağıdaki şekilde çalıştırabilirsiniz:


  Alt+F2

tuşuna basın ve sonra:

  omegat.kaptn



OmegaT'nin komut satırından başlatılması (tüm sistemler)
====================================================

OmegaT'yi başlatmak için gerekli komut şudur:

cd <OmegaT.jar adlı dosyanın bulunduğu klasör>

<Java çalıştırılabilir dosyasının adı ve dosya yolu> -jar OmegaT.jar

(Java çalıştırılabilir dosyası, Linux sistemlerinde java, Windows sistemlerinde ise
java.exe adlı dosyadır.
Eğer Java tüm sistem seviyesinde kurulmuşsa ve komut yolunda yer alıyorsa, tam
dosya yolunun girilmesine gerek yoktur).



Katkıda Bulunanlar
================

OmegaT ilk olarak Keith Godfrey tarafından geliştirilmiştir.

Aaron Madlon-Kay, OmegaT proje yöneticisidir.

Şu anki ekip:
(alfabetik sıra)

  Vincent Bidaux (belgelendirme yöneticisi)
  Marco Cevoli (Telegram topluluk yöneticisi)
  Jean-Christophe Helary (Twitter topluluk yöneticisi) 
  Kos Ivantsof (yerelleştirme yöneticisi)
  Concepción Martin (Facebook topluluk yöneticisi)
  Briac Pilpré (web yöneticisi)
  Lucie Vecerova (Facebook topluluk yöneticisi)

Koda yapılan katkılar /docs/contributors.txt dosyasında belgelenmiştir.

Daha önce katkıda bulunanlar:
(alfabetik sıra)

  Anthony Baldwin (yerelleştirme yöneticisi)
  Didier Briel (proje yöneticisi)
  Alex Buloichik (baş geliştirici)
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (destek grubu sahibi, yerelleştirme yöneticisi)
  Maxym Mykhalchuk (baş geliştirici)
  Samuel Murray
  Henry Pijffers (sürüm yöneticisi)
  Marc Prior (proje koordinatörü, web yöneticisi)
  Vito Smolej (dokümantasyon yöneticisi)
  ve çok sayıda son derece yardımcı insan.

(Eğer OmegaT Projesine önemli bir katkıda bulunmuşsanız, ama adınız yukarıdaki
listede yer almıyorsa bizimle irtibata geçebilirsiniz).



Faydalı bağlantılar
================

OmegaT hakkındaki en güncel bilgilere şu adresten ulaşabilirsiniz:

   http://www.omegat.org/

Arşivlerde arama yapma imkanı olan Yahoo kullanıcı grubundan (çok dilli)
kullanıcı desteği alabilirsiniz:

   https://omegat.org/support

Özellik İstekleri (İngilizce) SourceForge sitesinden yapılabilir:

   https://sourceforge.net/p/omegat/feature-requests/

Hata raporları (İngilizce) SourceForge sitesinden gönderilebilir:

   https://sourceforge.net/p/omegat/bugs/
