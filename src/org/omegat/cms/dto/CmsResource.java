package org.omegat.cms.dto;

import java.io.Serializable;
import java.util.Objects;

public class CmsResource implements CmsIdentified, Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String name;
    private final String path;

    public CmsResource(String id, String name, String path) {
        this.id = id;
        this.name = name;
        this.path = path;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CmsResource that = (CmsResource) o;
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
