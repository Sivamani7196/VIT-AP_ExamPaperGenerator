package com.university.qpg.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.university.qpg.model.GeneratedPaper;
import com.university.qpg.model.Question;
import com.university.qpg.model.Subject;
import com.university.qpg.repository.SubjectRepository;
import com.university.qpg.service.ExportService;
import com.university.qpg.service.PaperService;

@RestController
@RequestMapping("/api/papers")
public class PaperController {

    private final SubjectRepository subjectRepository;
    private final PaperService paperService;
    private final ExportService exportService;

    public PaperController(SubjectRepository subjectRepository,
                           PaperService paperService,
                           ExportService exportService) {
        this.subjectRepository = subjectRepository;
        this.paperService = paperService;
        this.exportService = exportService;
    }

    @GetMapping("/{subjectId}/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable int subjectId) throws IOException {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));

        List<Question> questions = paperService.getQuestionsForSubject(subject);
        if (questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No questions for this subject");
        }

        byte[] pdf = exportService.exportPdf(subject, questions);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "question-paper.pdf");
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @GetMapping("/{subjectId}/docx")
    public ResponseEntity<byte[]> exportDocx(@PathVariable int subjectId) throws IOException {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));

        List<Question> questions = paperService.getQuestionsForSubject(subject);
        if (questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No questions for this subject");
        }

        byte[] docx = exportService.exportDocx(subject, questions);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        headers.setContentDispositionFormData("attachment", "question-paper.docx");
        return new ResponseEntity<>(docx, headers, HttpStatus.OK);
    }

    @PostMapping("/generate")
    public ResponseEntity<GeneratePaperResponse> generateAndStorePaper(
            @RequestBody GeneratePaperRequest request,
            Principal principal) {
        int totalPercentage = request.getEasyPercentage() + request.getMediumPercentage() + request.getHardPercentage();
        if (totalPercentage != 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Percentages must add up to 100");
        }

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));

        GeneratedPaper savedPaper;
        try {
            savedPaper = paperService.generateAndStorePaper(subject, request, principal.getName());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        GeneratePaperResponse response = new GeneratePaperResponse();
        response.setPaperId(savedPaper.getId());
        response.setPaperContent(savedPaper.getPaperContent());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    public static class GeneratePaperRequest {
        private int subjectId;
        private int totalMarks;
        private int easyPercentage;
        private int mediumPercentage;
        private int hardPercentage;

        public int getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(int subjectId) {
            this.subjectId = subjectId;
        }

        public int getTotalMarks() {
            return totalMarks;
        }

        public void setTotalMarks(int totalMarks) {
            this.totalMarks = totalMarks;
        }

        public int getEasyPercentage() {
            return easyPercentage;
        }

        public void setEasyPercentage(int easyPercentage) {
            this.easyPercentage = easyPercentage;
        }

        public int getMediumPercentage() {
            return mediumPercentage;
        }

        public void setMediumPercentage(int mediumPercentage) {
            this.mediumPercentage = mediumPercentage;
        }

        public int getHardPercentage() {
            return hardPercentage;
        }

        public void setHardPercentage(int hardPercentage) {
            this.hardPercentage = hardPercentage;
        }
    }

    public static class GeneratePaperResponse {
        private Long paperId;
        private String paperContent;

        public Long getPaperId() {
            return paperId;
        }

        public void setPaperId(Long paperId) {
            this.paperId = paperId;
        }

        public String getPaperContent() {
            return paperContent;
        }

        public void setPaperContent(String paperContent) {
            this.paperContent = paperContent;
        }
    }
}
