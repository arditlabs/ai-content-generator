package com.aicontentgenerator.content.repository;

import com.aicontentgenerator.content.entity.Content;
import com.aicontentgenerator.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {

    /** All content for a user, newest first. */
    List<Content> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Fetch a single content record scoped to a specific user.
     * Used to prevent users from accessing each other's content.
     */
    Optional<Content> findByIdAndUser(Long id, User user);

    /** Scoped delete — verifies ownership before removal. */
    void deleteByIdAndUser(Long id, User user);

    boolean existsByIdAndUser(Long id, User user);
}
