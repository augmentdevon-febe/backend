package com.example.worldcuppredictor.domain.repository;

import com.example.worldcuppredictor.domain.entity.Prediction;
import com.example.worldcuppredictor.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    Page<Prediction> findByUserOrderByRequestedAtDesc(User user, Pageable pageable);
    java.util.Optional<Prediction> findByIdAndUser(Long id, User user);
}
