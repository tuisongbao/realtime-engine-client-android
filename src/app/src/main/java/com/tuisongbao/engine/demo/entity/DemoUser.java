package com.tuisongbao.engine.demo.entity;

import java.io.Serializable;

/**
 * Created by user on 15-8-27.
 */
public class DemoUser implements Serializable{
    String id;
    String username;
    Boolean activated;
    String createdAt;
    Object avatar;

    public DemoUser() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "DemoUser{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", activated=" + activated +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }

    public Boolean getActivated() {
        return activated;
    }

    public Object getAvatar() {
        return avatar;
    }

    public void setAvatar(Object avatar) {
        this.avatar = avatar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DemoUser demoUser = (DemoUser) o;

        return !(username != null ? !username.equals(demoUser.username) : demoUser.username != null);

    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }
}
