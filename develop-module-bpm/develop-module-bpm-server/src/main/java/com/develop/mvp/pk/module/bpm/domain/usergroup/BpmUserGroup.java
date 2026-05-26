package com.develop.mvp.pk.module.bpm.domain.usergroup;
// DDD 角色：BPM用户组聚合根 - AggregateRoot_Bpm_Skill

import com.develop.mvp.pk.module.bpm.domain.usergroup.event.*;
import com.develop.mvp.pk.module.bpm.domain.usergroup.valueobject.*;
import java.util.*;

public final class BpmUserGroup {
    private final UserGroupId id;
    private final UserGroupName name;
    private final String description;
    private UserGroupStatus status;
    private final Set<Long> userIds;
    private final List<UserGroupDomainEvent> events = new ArrayList<>();

    BpmUserGroup(UserGroupId id, UserGroupName name, String description, UserGroupStatus status, Set<Long> userIds) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.description = description;
        this.status = status != null ? status : UserGroupStatus.ENABLED;
        this.userIds = userIds != null ? userIds : Collections.emptySet();
    }

    public void markDeleted() { events.add(new UserGroupDeletedEvent(this.id().value())); }

    // ── accessors ──
    public UserGroupId id() { return id; }
    public UserGroupName name() { return name; }
    public String description() { return description; }
    public UserGroupStatus status() { return status; }
    public Set<Long> userIds() { return userIds; }
    public boolean isEnabled() { return status.isEnabled(); }
    public List<UserGroupDomainEvent> pullEvents() { List<UserGroupDomainEvent> r = new ArrayList<>(events); events.clear(); return r; }

    @Override public boolean equals(Object o) { return o instanceof BpmUserGroup u && id.equals(u.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
