package org.omegat.gui.team.history;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Data classes (same as before)
public class TmxCommitChange {
    private final String commitId;
    private final String message;
    private final String author;
    private final Date date;
    private List<TuChange> tuChanges;

    public TmxCommitChange(String commitId, String message, String author, Date date) {
        this.commitId = commitId;
        this.message = message;
        this.author = author;
        this.date = date;
        this.tuChanges = new ArrayList<>();
    }

    public void setTuChanges(List<TuChange> tuChanges) {
        this.tuChanges = tuChanges;
    }

    // Getters
    public String getCommitId() {
        return commitId;
    }

    public String getMessage() {
        return message;
    }

    public String getAuthor() {
        return author;
    }

    public Date getDate() {
        return date;
    }

    public List<TuChange> getTuChanges() {
        return tuChanges;
    }

    @Override
    public String toString() {
        return String.format("Commit: %s by %s (%s)\nMessage: %s\nTU Changes: %d",
                commitId.substring(0, 8), author, date, message, tuChanges.size());
    }
}
