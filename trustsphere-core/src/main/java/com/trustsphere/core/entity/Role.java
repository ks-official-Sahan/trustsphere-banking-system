package com.trustsphere.core.entity;

import com.trustsphere.core.entity.base.BaseAuditEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

@NamedQueries({
        @NamedQuery(
                name = "Role.findByName",
                query = "SELECT r FROM Role r WHERE r.name = :name"
        ),
        @NamedQuery(
                name = "Role.findAll",
                query = "SELECT r FROM Role r ORDER BY r.hierarchyLevel ASC"
        )
})
@Entity
@Table(name = "roles")
public class Role extends BaseAuditEntity {

    @NotNull
    @Size(min = 2, max = 50)
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @NotNull
    @Column(name = "hierarchy_level", nullable = false)
    private Integer hierarchyLevel;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users;

    public Role() {}

    public Role(String name, Integer hierarchyLevel) {
        this.name = name;
        this.hierarchyLevel = hierarchyLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getHierarchyLevel() {
        return hierarchyLevel;
    }

    public void setHierarchyLevel(Integer hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}