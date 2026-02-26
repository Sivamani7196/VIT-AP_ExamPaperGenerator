
package com.university.qpg.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.university.qpg.model.Question;
import com.university.qpg.model.Subject;

public interface QuestionRepository extends JpaRepository<Question, Integer> {

    @Query("SELECT q FROM Question q WHERE q.subject=:subject AND q.marks=:marks ORDER BY RAND()")
    List<Question> getRandomQuestions(@Param("subject") Subject subject,
                                      @Param("marks") int marks,
                                      Pageable pageable);

    List<Question> findBySubject(Subject subject);
}
