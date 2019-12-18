@KÄÄNNÖSHUOMAUTUS@


Mikä OmegaT on?
===============

OmegaT on vapaa ja avoin CAT-ohjelma eli tietokoneavusteisen kääntämisen työkalu,
joka toimii eri käyttöjärjestelmissä. OmegaT tarjoaa käännöstyön avuksi
käännösmuistin, erilaisia sanastoja sekä sumeita käännösosumia, ja kokoaa käännökset
resursseineen käännösprojekteiksi, joita on helppo hallita ja päivittää.



Tietoa lisensseistä
=====================

OmegaT on lisensoitu GNU General Public Licencen (GNU GPL) eli GNU-hankkeen yleisellä lisenssillä, jonka julkaisija on Free Software Foundation. Lisenssin versionumero on 3 tai (niin halutessasi) sitä uudempi. Löydät koko lisenssitekstin kohdasta
/docs/OmegaT-license.txt.

OmegaT käyttää useita kirjastoja. Jokaisen kirjaston lisenssi on kerrottu kohdassa
/lib/licenses/Licenses.txt.



Mitä OmegaT:n asennukseen tarvitaan
===================================

OmegaT vaatii, että järjestelmääsi on asennettu Java Runtime Environment (JRE) 1.8 tai uudempi.

Suosittelemme, että käytät OmegaT-paketteja, joissa on JRE. Tällöin vältyt sopivan
JRE:n valitsemiselta, hankinnalta ja asennukselta.



OmegaT:n asentaminen (Windows)
===========================

Käynnistä asennusohjelma.



OmegaT:n asentaminen (Mac)
=======================

Pura arkisto OmegaT.zip, niin löydät kansion, jossa on dokumentaatiotiedosto
ja OmegaT-sovellus. Siirrä kansio sopivaan sijaintiin, esimerkiksi
Apit-kansioon.



OmegaT:n asentaminen (Linux)
=========================

Siirrä arkisto johonkin sopivaan kansioon ja pura se. OmegaT on valmis
käynnistettäväksi.

Asennus on kuitenkin selkeämpi ja käyttäjäystävällisempi, jos käytät asennuskomento-
sarjaa linux-install.sh. Käytä tätä komentosarjaa pääteikkunassa:
siirry kansioon, jossa tiedosto onOmegaT.jar ja komentosarja linux-install.sh
ovat ja suorita tiedosto komennolla ./linux-install.sh.



OmegaT:n asentaminen (Solaris, FreeBSD jne.)
=========================================

Siirrä arkisto johonkin sopivaan kansioon ja pura se. OmegaT on valmis
käynnistettäväksi.



OmegatT:n asentaminen Java Web Startin avulla (kaikki käyttöjärjestelmät)
===========================================================

Jos järjestelmääsi on jo asennettu Java, voit asentaa OmegaT:n Java Web Startin
avulla.

Lataa tällöin seuraava tiedosto ja suorita se:

  https://omegat.sourceforge.net/webstart/OmegaT.jnlp

Tämä asentaa ohjelman ensimmäisen käytön yhteydessä sopivan ympäristön
sekä koneellesi että ohjelmaa varten. Myöhemmin ohjelmaa ei tarvitse käyttää verkkotilassa.



OmegaT:n käynnistäminen (Windows)
==========================

Jos olet asennuksen yhteydessä luonut työpöydälle ohjelmaikonin,
kaksoisnapsauta sitä.

Asennusohjelma voi luoda pikavalintoja käynnistysvalikkoon, työpöydälle
ja pikakäynnistysalueelle. Voit myös itse luoda linkkejä vetämällä OmegaT.exe-tiedoston
käynnistysvalikkoon, työpöydälle tai pikakäynnistysalueelle.

Jos Tiedostonhallinnassa (Windowin Resurssienhallinnassa) näkyy tiedosto OmegaT
muttei OmegaT.exe, muuta asetuksia niin, että tiedostopäätteet
näkyvät.



OmegaT:n käynnistäminen (Mac)
======================

Kaksoisnapsauta OmegaT-appia.

Voit vetää OmegaT-apin Dockiin tai Finder-ikkunan työkaluriville, niin voit
käynnistää ohjelman mistä tahansa. Voit käynnistää ohjelman
myös Spotlightin hakukentästä.



OmegaT:n käynnistäminen (Linux)
========================

Jos asensit OmegaT:n linux-install.sh-komentosarjalla, voit luultavimmin
käynnistää ohjelman komennolla

    Alt+F2

ja kirjoittamalla sitten

    omegat

Käyttäjäystävällisempi tapa käynnistää OmegaT on käyttää
asennuksen
mukana tullutta Kaptain-komentosarjaa (omegat.kaptn). Jotta voit käyttää tätä komentoa, sinun on ensin asennettava Kaptain. Voit
sitten suorittaa Kaptain-käynnistyskomentosarjan painamalla

    Alt+F2

ja kirjoittamalla sitten

    omegat.kaptn



OmegaT:n käynnistäminen komentoriviltä (kaikki käyttöjärjestelmät)
====================================================

Komento, joka käynnistää OmegaT:n, on

    cd <kansio, jossa tiedosto OmegaT.jar on>

    <suoritettavan Java-tiedoston nimi ja polku> -jar OmegaT.jar

(Suoritettava Java-tiedosto on Linuxissa java ja Windowsissa java.exe.)  Jos
Java on asennettu järjestelmätasoisesti ja se on komento-
polulla, koko polkua ei tarvitse kirjoittaa.)



Käyttöön antajat
============

OmegaT:n alkuperäinen kehittäjä on Keith Godfrey.

OmegaT-projektinvetäjä on Aaron Madlon-Kay.

Nykyinen tiimi
(aakkosjärjestyksessä):

- Vincent Bidaux (dokumentaatiokoordinaattori)
- Marco Cevoli (Telegram-yhteisön koordinaattori)
- Jean-Christophe Helary (Twitter-yhteisön koordinaattori)
- Kos Ivantsof (lokalisaatiokoordinaattori)
- Concepción Martin (Facebook-yhteisön koordinaattori)
- Briac Pilpré (sivustovastaava)
- Lucie Vecerova (Facebook-yhteisön koordinaattori)

Kaikki, jotka ovat antaneet koodia käyttöön, on kerrottu tiedostossa /docs/contributors.txt.

Aikaisempia koodintuottajia ovat:
(aakkosjärjestyksessä)

- Anthony Baldwin (lokalisointikoordinaattori)
- Didier Briel (projektinvetäjä)
- Alex Buloichik (pääkehittäjä)
- Sabine Cretella
- Dmitri Gabinski
- Jean-Christophe Helary (tukiryhmän omistaja, lokalisointikoordinaattori)
- Maxym Mykhalchuk (pääkehittäjä)
- Samuel Murray
- Henry Pijffers (jakelukoordinaattori)
- Marc Prior (projektikoordinaattori, sivustovastaava)
- Vito Smolej (dokumentaatiovastaava)

ja monet, monet muut auttavaiset ihmiset

(Jos olet mielestäsi avustanut OmegaT-projektia huomattavasti, mutta nimesi
ei näy listalla, ota meihin yhteyttä.)



Hyödyllisiä linkkejä
============

Löydät ajankohtaisinta OmegaT-tietoa osoitteesta

  https://omegat.org/

Resurssit käyttäjän tueksi:

  https://omegat.org/support

Muutos- tai parannusehdotukset (englanniksi) Sourceforgen sivulla

  https://sourceforge.net/p/omegat/feature-requests/

Bugiraportit (englanniksi) Sourceforgen sivulla

  https://sourceforge.net/p/omegat/bugs/
