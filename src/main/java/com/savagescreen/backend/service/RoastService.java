package com.savagescreen.backend.service;

import com.savagescreen.backend.dto.RoastRequestDTO;
import com.savagescreen.backend.dto.RoastResponseDTO;
import com.savagescreen.backend.model.RoastHistory;
import com.savagescreen.backend.model.UserAccount;
import java.util.List;

public interface RoastService {
    RoastResponseDTO generateRoast(RoastRequestDTO request, UserAccount user);
    List<RoastResponseDTO> getAllRoasts(UserAccount user);
    RoastResponseDTO getRoast(Long id, UserAccount user);
}