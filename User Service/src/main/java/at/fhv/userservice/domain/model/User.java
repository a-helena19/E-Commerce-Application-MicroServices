package at.fhv.userservice.domain.model;

import at.fhv.userservice.domain.exception.InvalidUserDataException;

import java.util.UUID;
import java.util.regex.Pattern;

public class User {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private UserStatus status;

    // Private constructor - use factory methods instead
    private User(UUID id, String firstName, String lastName, String email, UserStatus status) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.status = status;
    }

    public static User create(String firstName, String lastName, String email) {
        validateFirstName(firstName);
        validateLastName(lastName);
        validateEmail(email);

        return new User(
                null,
                firstName.trim(),
                lastName.trim(),
                email.trim().toLowerCase(),
                UserStatus.ACTIVE
        );
    }

    // Factory method for reconstituting from persistence
    public static User reconstitute(UUID id, String firstName, String lastName, String email, UserStatus status) {
        return new User(id, firstName, lastName, email, status);
    }

    public void update(String firstName, String lastName, String email) {
        validateFirstName(firstName);
        validateLastName(lastName);
        validateEmail(email);

        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.email = email.trim().toLowerCase();
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void deactivate() {
        if (this.status == UserStatus.INACTIVE) {
            throw new IllegalStateException("User is already inactive");
        }
        this.status = UserStatus.INACTIVE;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean isInactive() {
        return status == UserStatus.INACTIVE;
    }

    private static void validateFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new InvalidUserDataException("First name cannot be null or empty");
        }
        if (firstName.trim().length() < 2) {
            throw new InvalidUserDataException("First name must be at least 2 characters long");
        }
        if (firstName.trim().length() > 50) {
            throw new InvalidUserDataException("First name cannot exceed 50 characters");
        }
    }

    private static void validateLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new InvalidUserDataException("Last name cannot be null or empty");
        }
        if (lastName.trim().length() < 2) {
            throw new InvalidUserDataException("Last name must be at least 2 characters long");
        }
        if (lastName.trim().length() > 50) {
            throw new InvalidUserDataException("Last name cannot exceed 50 characters");
        }
    }

    private static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidUserDataException("Email cannot be null or empty");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new InvalidUserDataException("Invalid email format");
        }
        if (email.trim().length() > 100) {
            throw new InvalidUserDataException("Email cannot exceed 100 characters");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public UserStatus getStatus() {
        return status;
    }

    // Package-private setter for ID - only used by repository after persistence
    void setId(UUID id) {
        this.id = id;
    }

}
