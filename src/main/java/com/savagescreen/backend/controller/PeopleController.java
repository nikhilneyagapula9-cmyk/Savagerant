package com.savagescreen.backend.controller;

import com.savagescreen.backend.dto.PublicProfile;
import com.savagescreen.backend.model.UserAccount;
import com.savagescreen.backend.repository.UserAccountRepository;
import com.savagescreen.backend.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/people")
public class PeopleController {
    private final AuthService authService;
    private final UserAccountRepository userRepository;

    public PeopleController(AuthService authService, UserAccountRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public java.util.List<PublicProfile> findPeople(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        UserAccount user = authService.requireUser(authorization);
        return userRepository.findTop50ByIdNotOrderByDisplayNameAsc(user.getId()).stream()
                .map(person -> new PublicProfile(person.getId(), person.getDisplayName()))
                .toList();
    }
}
