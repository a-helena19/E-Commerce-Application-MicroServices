package at.fhv.userservice.rest.dtos;

import java.util.UUID;

public record GetUserDTO(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String status,
        UUID cartId
) {
}
