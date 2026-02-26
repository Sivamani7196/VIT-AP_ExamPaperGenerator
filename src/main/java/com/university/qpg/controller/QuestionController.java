package com.university.qpg.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.university.qpg.model.Question;
import com.university.qpg.model.Subject;
import com.university.qpg.repository.QuestionRepository;
import com.university.qpg.repository.SubjectRepository;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;

    public QuestionController(QuestionRepository questionRepository, SubjectRepository subjectRepository) {
        this.questionRepository = questionRepository;
        this.subjectRepository = subjectRepository;
    }

    @PostMapping
    public ResponseEntity<Question> createQuestion(@RequestBody QuestionRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));

        Question question = new Question();
        question.setQuestionText(request.getQuestionText());
        question.setMarks(request.getMarks());
        question.setDifficulty(request.getDifficulty());
        question.setUnit(request.getUnit());
        question.setSubject(subject);

        Question saved = questionRepository.save(question);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Question>> getAllQuestions() {
        List<Question> questions = questionRepository.findAll();
        return new ResponseEntity<>(questions, HttpStatus.OK);
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<List<Question>> getQuestionsBySubject(@PathVariable int subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));

        List<Question> questions = questionRepository.findBySubject(subject);
        return new ResponseEntity<>(questions, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Question> getQuestion(@PathVariable int id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
        return new ResponseEntity<>(question, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable int id) {
        if (!questionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
        }
        questionRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public static class QuestionRequest {
        private int subjectId;
        private String questionText;
        private int marks;
        private String difficulty;
        private int unit;

        public int getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(int subjectId) {
            this.subjectId = subjectId;
        }

        public String getQuestionText() {
            return questionText;
        }

        public void setQuestionText(String questionText) {
            this.questionText = questionText;
        }

        public int getMarks() {
            return marks;
        }

        public void setMarks(int marks) {
            this.marks = marks;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(String difficulty) {
            this.difficulty = difficulty;
        }

        public int getUnit() {
            return unit;
        }

        public void setUnit(int unit) {
            this.unit = unit;
        }
    }
}
