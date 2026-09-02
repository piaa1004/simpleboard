package com.example.simpleboard.security.service;

import com.example.simpleboard.account.user.repository.LoginRepository;
import com.example.simpleboard.common.response.message.AccountMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final LoginRepository loginRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return loginRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException(AccountMessage.NOT_FOUND_ACCOUNT.getMessage()));
    }
}
