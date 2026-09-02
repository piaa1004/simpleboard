package com.example.simpleboard.smtp.repository;

import com.example.simpleboard.smtp.entity.MailCert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MailCertRepository extends JpaRepository<MailCert, Long> {

    Optional<MailCert> findByEmail(String email);

    void deleteByEmail(String email);
}
