package com.pachara.somjeed.somjeed_chatbot.repository;

import com.pachara.somjeed.somjeed_chatbot.model.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
