package com.hung.chatbot.repository;

import com.hung.chatbot.entity.MaterialsInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Admin
 * @since 11/16/2025
 */
@Repository
public interface MaterialsInformationRepository
        extends JpaRepository<MaterialsInformation, Long> {
}