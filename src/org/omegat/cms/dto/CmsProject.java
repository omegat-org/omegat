package org.omegat.cms.dto;

import java.io.Serializable;
import java.util.Objects;

public class CmsProject implements CmsIdentified, Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String name;

    public CmsProject(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CmsProject that = (CmsProject) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name != null ? name : id;
    }
}
