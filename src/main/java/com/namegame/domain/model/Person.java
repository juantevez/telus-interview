package com.namegame.domain.model;

import com.namegame.domain.enums.Gender;

import java.time.Instant;
import java.util.UUID;

public class Person {

    private UUID id;
    private String firstName;
    private String lastName;
    private Gender gender;
    private String photoUrl;
    private Boolean active;
    private Instant createdAt;

    public Person() {}

    public Person(UUID id, String firstName, String lastName, Gender gender,
                  String photoUrl, Boolean active, Instant createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.photoUrl = photoUrl;
        this.active = active;
        this.createdAt = createdAt;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
