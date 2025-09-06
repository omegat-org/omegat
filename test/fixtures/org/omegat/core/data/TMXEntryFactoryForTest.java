package org.omegat.core.data;

public final class TMXEntryFactoryForTest {
    private final PrepareTMXEntry prep;
    private boolean defaultTranslation = true;
    public TMXEntry.ExternalLinked linked;

    public TMXEntryFactoryForTest() {
        prep = new PrepareTMXEntry();
    }

    public TMXEntryFactoryForTest setSource(String source) {
        prep.source = source;
        return this;
    }

    public TMXEntryFactoryForTest setTranslation(String translation) {
        prep.translation = translation;
        return this;
    }

    public TMXEntryFactoryForTest setCreator(String creator) {
        prep.creator = creator;
        return this;
    }

    public TMXEntryFactoryForTest setCreationDate(long creationDate) {
        prep.creationDate = creationDate;
        return this;
    }

    public TMXEntryFactoryForTest setNote(String note) {
        prep.note = note;
        return this;
    }

    public TMXEntryFactoryForTest setDefaultTranslation(boolean defaultTranslation) {
        this.defaultTranslation = defaultTranslation;
        return this;
    }

    public TMXEntryFactoryForTest setExternalLinked(TMXEntry.ExternalLinked linked) {
        this.linked = linked;
        return this;
    }

    public TMXEntry build() {
        return new TMXEntry(prep, defaultTranslation, linked);
    }

    public static TMXEntry createTMXEntry(String source, String translation, String creator, long createDate, boolean defaultTranslation) {
        return new TMXEntryFactoryForTest()
                .setSource(source).setTranslation(translation).setCreator(creator).setCreationDate(createDate)
                .setDefaultTranslation(defaultTranslation)
                .build();
    }
}
