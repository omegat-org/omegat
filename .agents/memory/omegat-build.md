---
name: OmegaT build setup
description: Build and run configuration for OmegaT in Replit — Java Swing desktop GUI app.
---

OmegaT is a Java Swing desktop GUI application (Computer Assisted Translation tool). It has no web interface.

**Build command:** `./gradlew installDist --no-daemon -x checkstyleMain -x spotbugsMain -x pmdMain -x javadoc`

**Run command:** `./build/install/OmegaT/OmegaT`

**Workflow output type:** vnc (desktop GUI)

**Why:** OmegaT uses Swing/AWT for its UI, not a web server, so it runs in VNC mode.

**Fixed bug:** `src/org/omegat/core/matching/GlossaryConsistencyValidator.java` had wrong import `org.omegat.core.data.GlossaryEntry` — correct package is `org.omegat.gui.glossary.GlossaryEntry`.

**How to apply:** If rebuilding from scratch, skip static analysis tasks (-x checkstyleMain etc.) to speed up the build significantly.
