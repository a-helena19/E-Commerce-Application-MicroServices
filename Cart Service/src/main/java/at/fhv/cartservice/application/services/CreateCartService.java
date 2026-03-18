package at.fhv.cartservice.application.services;

import at.fhv.cartservice.rest.dtos.GetCartDTO;

import java.util.UUID;

public interface CreateCartService {
    GetCartDTO createCartForUser(UUID userId);
}

