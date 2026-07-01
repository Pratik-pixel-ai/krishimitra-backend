package com.kisaan.kisaan_backend.service;

import com.kisaan.kisaan_backend.model.Farmer;
import com.kisaan.kisaan_backend.repository.FarmerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class FarmerDetailsService implements UserDetailsService {

    private final FarmerRepository farmerRepository;

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        Farmer farmer = farmerRepository.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("Farmer not found with phone: " + phone));

        return new User(
                farmer.getPhone(),
                farmer.getPassword(),
                Collections.emptyList()
        );
    }
}