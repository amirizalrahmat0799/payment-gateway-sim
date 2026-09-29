package com.mizal.pgs.tokenization.vault;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CardTokenRepository extends JpaRepository<CardToken, String> {
}
