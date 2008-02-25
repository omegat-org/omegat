JAXB 2.0.5 jars:
    activation.jar
    jaxb-api.jar
    jaxb-impl.jar
    jsr173_1.0_api.jar

  JAXB required only for jdk 1.5. For jdk 1.6 we don't need it, since jdk 1.6 have built-in JAXB.
We are using JAXB 2.0 instead 2.1, because jdk 1.6.0-1.6.0_03 contains JAXB 2.0.
For use JAXB 2.1 with these jdk we should use endorsed jars. It could make problems.
Jdk 1.60_04 and later uses JAXB 2.1.


jna.jar

  Used for Hunspell native libraries access.

