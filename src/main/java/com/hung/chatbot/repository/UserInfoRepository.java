package com.hung.chatbot.repository;

import com.hung.chatbot.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    // Additional custom queries can be added here if needed
}
