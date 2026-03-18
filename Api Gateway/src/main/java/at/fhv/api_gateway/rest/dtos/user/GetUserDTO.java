package at.fhv.api_gateway.rest.dtos.user;

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
