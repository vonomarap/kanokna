package com.kanokna.account.domain.model;

import java.util.Objects;

/**
 * Value object representing a person's name.
 */
public record PersonName(String firstName, String lastName) {
    public PersonName {
        if (firstName != null) {
            firstName = firstName.strip();
        }
        if (lastName != null) {
            lastName = lastName.strip();
        }
    }

    public boolean isEmpty() {
        return (firstName == null || firstName.isBlank())
            && (lastName == null || lastName.isBlank());
    }

    public PersonName withFirstName(String updatedFirstName) {
        return new PersonName(updatedFirstName, lastName);
    }

    public PersonName withLastName(String updatedLastName) {
        return new PersonName(firstName, updatedLastName);
    }

    public boolean sameAs(PersonName other) {
        return Objects.equals(firstName, other.firstName)
            && Objects.equals(lastName, other.lastName);
    }
}