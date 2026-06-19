package com.example.worldcuppredictor.domain.repository;

import com.example.worldcuppredictor.domain.entity.Prediction;
import com.example.worldcuppredictor.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link Prediction} persistence.
 *
 * <p>Provides standard CRUD operations inherited from {@link JpaRepository} plus
 * domain-specific finders scoped to a single user to enforce data isolation.
 */
public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    /**
     * Returns a page of predictions belonging to {@code user}.
     * The caller supplies the sort order via the {@link Pageable} argument.
     */
    Page<Prediction> findByUser(User user, Pageable pageable);

    /**
     * Returns a page of predictions belonging to {@code user}, ordered by
     * {@code requestedAt} descending. Retained for backwards compatibility.
     */
    Page<Prediction> findByUserOrderByRequestedAtDesc(User user, Pageable pageable);

    /**
     * Returns the prediction with the given {@code id} if and only if it belongs to {@code user}.
     * Returns empty when the prediction does not exist or belongs to a different user.
     */
    java.util.Optional<Prediction> findByIdAndUser(Long id, User user);
}
