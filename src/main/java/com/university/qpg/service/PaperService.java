
package com.university.qpg.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.university.qpg.controller.PaperController.GeneratePaperRequest;
import com.university.qpg.model.GeneratedPaper;
import com.university.qpg.model.Question;
import com.university.qpg.model.Subject;
import com.university.qpg.repository.GeneratedPaperRepository;
import com.university.qpg.repository.QuestionRepository;

@Service
public class PaperService {

    @Autowired
    private QuestionRepository questionRepo;

    @Autowired
    private GeneratedPaperRepository generatedPaperRepository;

    @Autowired
    private AppUserService appUserService;

    public List<Question> generatePaper(Subject subject) {
        List<Question> paper = new ArrayList<>();
        paper.addAll(questionRepo.getRandomQuestions(subject, 2, PageRequest.of(0, 10)));
        paper.addAll(questionRepo.getRandomQuestions(subject, 10, PageRequest.of(0, 5)));
        return paper;
    }

    public List<Question> getQuestionsForSubject(Subject subject) {
        return questionRepo.findBySubject(subject);
    }

    public GeneratedPaper generateAndStorePaper(Subject subject, GeneratePaperRequest request, String username) {
        List<Question> allQuestions = questionRepo.findBySubject(subject);
        if (allQuestions.isEmpty()) {
            throw new IllegalArgumentException("No questions found for this subject");
        }

        int totalMarks = request.getTotalMarks();
        int easyMarks = Math.round(totalMarks * (request.getEasyPercentage() / 100f));
        int mediumMarks = Math.round(totalMarks * (request.getMediumPercentage() / 100f));
        int hardMarks = totalMarks - easyMarks - mediumMarks;

        List<Question> easyQuestions = allQuestions.stream().filter(q -> "easy".equalsIgnoreCase(q.getDifficulty())).toList();
        List<Question> mediumQuestions = allQuestions.stream().filter(q -> "medium".equalsIgnoreCase(q.getDifficulty())).toList();
        List<Question> hardQuestions = allQuestions.stream().filter(q -> "hard".equalsIgnoreCase(q.getDifficulty())).toList();

        String paperHtml = buildPaperHtml(totalMarks, easyMarks, mediumMarks, hardMarks, easyQuestions, mediumQuestions, hardQuestions);

        GeneratedPaper generatedPaper = new GeneratedPaper();
        generatedPaper.setSubject(subject);
        generatedPaper.setGeneratedBy(appUserService.findByUsername(username));
        generatedPaper.setTotalMarks(totalMarks);
        generatedPaper.setEasyPercentage(request.getEasyPercentage());
        generatedPaper.setMediumPercentage(request.getMediumPercentage());
        generatedPaper.setHardPercentage(request.getHardPercentage());
        generatedPaper.setPaperContent(paperHtml);
        generatedPaper.setCreatedAt(LocalDateTime.now());

        return generatedPaperRepository.save(generatedPaper);
    }

    private String buildPaperHtml(int totalMarks, int easyMarks, int mediumMarks, int hardMarks,
                                  List<Question> easyQuestions,
                                  List<Question> mediumQuestions,
                                  List<Question> hardQuestions) {
        StringBuilder paper = new StringBuilder();
        paper.append("<div style=\"text-align: center; margin-bottom: 20px;\">")
                .append("<h3>Question Paper</h3>")
                .append("<p><strong>Total Marks:</strong> ").append(totalMarks).append("</p>")
                .append("<p><strong>Time: 3 Hours</strong></p><hr></div>");

        if (!easyQuestions.isEmpty()) {
            paper.append("<div class=\"question-item\"><div class=\"question-number\">SECTION A - Easy Questions</div>")
                    .append("<div class=\"question-marks\">").append(easyMarks).append(" marks</div>")
                    .append("<p style=\"clear: both; margin-top: 10px;\">Attempt all questions.</p>");
            int limit = Math.max(1, (int) Math.ceil(Math.max(easyMarks, 1) / 2.0));
            for (int i = 0; i < Math.min(limit, easyQuestions.size()); i++) {
                Question q = easyQuestions.get(i);
                paper.append("<p style=\"margin-top: 5px;\">")
                        .append(i + 1)
                        .append(". ")
                        .append(escapeHtml(q.getQuestionText()))
                        .append(" <span class=\"question-marks\">")
                        .append(q.getMarks())
                        .append(" marks</span></p>");
            }
            paper.append("</div>");
        }

        if (!mediumQuestions.isEmpty()) {
            paper.append("<div class=\"question-item\"><div class=\"question-number\">SECTION B - Medium Questions</div>")
                    .append("<div class=\"question-marks\">").append(mediumMarks).append(" marks</div>")
                    .append("<p style=\"clear: both; margin-top: 10px;\">Attempt any ")
                    .append(Math.max(1, Math.floor(mediumQuestions.size() / 2.0)))
                    .append(" out of ").append(mediumQuestions.size()).append(" questions.</p>");
            for (int i = 0; i < mediumQuestions.size(); i++) {
                Question q = mediumQuestions.get(i);
                paper.append("<p style=\"margin-top: 5px;\">")
                        .append(easyQuestions.size() + i + 1)
                        .append(". ")
                        .append(escapeHtml(q.getQuestionText()))
                        .append(" <span class=\"question-marks\">")
                        .append(q.getMarks())
                        .append(" marks</span></p>");
            }
            paper.append("</div>");
        }

        if (!hardQuestions.isEmpty()) {
            paper.append("<div class=\"question-item\"><div class=\"question-number\">SECTION C - Difficult Questions</div>")
                    .append("<div class=\"question-marks\">").append(hardMarks).append(" marks</div>")
                    .append("<p style=\"clear: both; margin-top: 10px;\">Attempt any 1 out of ")
                    .append(hardQuestions.size()).append(" questions.</p>");
            for (int i = 0; i < hardQuestions.size(); i++) {
                Question q = hardQuestions.get(i);
                paper.append("<p style=\"margin-top: 5px;\">")
                        .append(easyQuestions.size() + mediumQuestions.size() + i + 1)
                        .append(". ")
                        .append(escapeHtml(q.getQuestionText()))
                        .append(" <span class=\"question-marks\">")
                        .append(q.getMarks())
                        .append(" marks</span></p>");
            }
            paper.append("</div>");
        }

        return paper.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#039;");
    }
}
