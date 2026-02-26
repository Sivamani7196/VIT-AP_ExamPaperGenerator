// Navigation functionality
document.addEventListener('DOMContentLoaded', function() {
    const navLinks = document.querySelectorAll('.nav-link');
    const sections = document.querySelectorAll('.section');

    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Remove active class from all links and sections
            navLinks.forEach(l => l.classList.remove('active'));
            sections.forEach(s => s.classList.remove('active'));
            
            // Add active class to clicked link
            this.classList.add('active');
            
            // Show corresponding section
            const targetId = this.getAttribute('href');
            const targetSection = document.querySelector(targetId);
            if (targetSection) {
                targetSection.classList.add('active');
            }
        });
    });

    // Set first nav link as active by default
    if (navLinks.length > 0) {
        navLinks[0].classList.add('active');
    }
    if (sections.length > 0) {
        sections[0].classList.add('active');
    }

    // Form handlers
    setupSubjectForm();
    setupQuestionForm();
    setupGenerateForm();
    
    // Load existing data
    loadSubjects();
    loadQuestions();
    
    // Load subjects into dropdowns after a brief delay
    setTimeout(() => {
        updateSubjectSelects();
    }, 500);
});

function getCsrfHeaders() {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    if (csrfToken && csrfHeader) {
        return { [csrfHeader]: csrfToken };
    }
    return {};
}

// Subject Form Handler
function setupSubjectForm() {
    const form = document.getElementById('subjectForm');
    if (!form) return;

    form.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const formData = new FormData(form);
        const subjectData = {
            subjectName: formData.get('subjectName'),
            subjectCode: formData.get('subjectCode'),
            credits: parseInt(formData.get('credits'))
        };

        // Send to backend
        fetch('/api/subjects', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...getCsrfHeaders()
            },
            body: JSON.stringify(subjectData)
        })
        .then(response => {
            if (!response.ok) throw new Error('Failed to add subject');
            return response.json();
        })
        .then(subject => {
            loadSubjects();
            updateSubjectSelects();
            form.reset();
            showNotification('Subject added successfully!', 'success');
        })
        .catch(error => {
            console.error('Error:', error);
            showNotification('Failed to add subject', 'error');
        });
    });
}

// Add subject to table
function loadSubjects() {
    fetch('/api/subjects')
        .then(response => response.json())
        .then(subjects => {
            const table = document.getElementById('subjectsTable');
            table.innerHTML = '';
            
            if (subjects.length === 0) {
                table.innerHTML = '<tr><td colspan="5" class="empty-message">No subjects added yet</td></tr>';
                return;
            }
            
            subjects.forEach(subject => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${subject.subjectId}</td>
                    <td>${escapeHtml(subject.subjectName)}</td>
                    <td>${escapeHtml(subject.subjectCode || '-')}</td>
                    <td>${subject.credits ?? subject.semester ?? '-'}</td>
                    <td>
                        <button class="btn btn-danger" onclick="deleteSubject(${subject.subjectId})">Delete</button>
                    </td>
                `;
                table.appendChild(row);
            });
        })
        .catch(error => console.error('Error loading subjects:', error));
}

// Question Form Handler
function setupQuestionForm() {
    const form = document.getElementById('questionForm');
    if (!form) return;

    form.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const formData = new FormData(form);
        const subjectId = parseInt(formData.get('questionSubject'));
        
        if (!subjectId) {
            showNotification('Please select a subject', 'error');
            return;
        }

        const questionData = {
            subjectId: subjectId,
            questionText: formData.get('questionText'),
            marks: parseInt(formData.get('questionMarks')),
            difficulty: formData.get('questionDifficulty'),
            unit: 1
        };

        // Send to backend
        fetch('/api/questions', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...getCsrfHeaders()
            },
            body: JSON.stringify(questionData)
        })
        .then(response => {
            if (!response.ok) throw new Error('Failed to add question');
            return response.json();
        })
        .then(question => {
            loadQuestions();
            form.reset();
            showNotification('Question added successfully!', 'success');
        })
        .catch(error => {
            console.error('Error:', error);
            showNotification('Failed to add question', 'error');
        });
    });
}

// Add question to table
function loadQuestions() {
    fetch('/api/questions')
        .then(response => response.json())
        .then(questions => {
            const table = document.getElementById('questionsTable');
            table.innerHTML = '';
            
            if (questions.length === 0) {
                table.innerHTML = '<tr><td colspan="6" class="empty-message">No questions added yet</td></tr>';
                return;
            }
            
            questions.forEach(question => {
                const row = document.createElement('tr');
                const difficultyBadge = `<span class="badge badge-${question.difficulty}">${question.difficulty.toUpperCase()}</span>`;
                row.innerHTML = `
                    <td>${question.questionId}</td>
                    <td>${question.subject.subjectName}</td>
                    <td>${escapeHtml(question.questionText.substring(0, 50))}...</td>
                    <td>${question.marks}</td>
                    <td>${difficultyBadge}</td>
                    <td>
                        <button class="btn btn-danger" onclick="deleteQuestion(${question.questionId})">Delete</button>
                    </td>
                `;
                table.appendChild(row);
            });
        })
        .catch(error => console.error('Error loading questions:', error));
}

// Generate Paper Form Handler
function setupGenerateForm() {
    const form = document.getElementById('generateForm');
    if (!form) return;

    form.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const formData = new FormData(form);
        const subjectId = parseInt(formData.get('paperSubject'));
        
        if (!subjectId) {
            showNotification('Please select a subject', 'error');
            return;
        }

        const paperData = {
            subject: subjectId,
            totalMarks: parseInt(formData.get('totalMarks')),
            easyPercentage: parseInt(formData.get('easyPercentage')),
            mediumPercentage: parseInt(formData.get('mediumPercentage')),
            hardPercentage: parseInt(formData.get('hardPercentage'))
        };

        // Validate percentages
        const totalPercentage = paperData.easyPercentage + paperData.mediumPercentage + paperData.hardPercentage;
        if (totalPercentage !== 100) {
            showNotification('Percentages must add up to 100%', 'error');
            return;
        }

        fetch('/api/papers/generate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...getCsrfHeaders()
            },
            body: JSON.stringify({
                subjectId: subjectId,
                totalMarks: paperData.totalMarks,
                easyPercentage: paperData.easyPercentage,
                mediumPercentage: paperData.mediumPercentage,
                hardPercentage: paperData.hardPercentage
            })
        })
            .then(response => response.json())
            .then(result => {
                if (!result.paperContent) {
                    showNotification('No questions found for this subject', 'error');
                    return;
                }
                const paperContainer = document.getElementById('generatedPaper');
                const paperContent = document.getElementById('paperContent');
                paperContent.innerHTML = result.paperContent;
                paperContainer.style.display = 'block';
                showNotification(`Question paper generated and saved (ID: ${result.paperId})`, 'success');
            })
            .catch(error => {
                console.error('Error:', error);
                showNotification('Failed to generate paper', 'error');
            });
    });
}

// Update subject dropdown selects
function updateSubjectSelects() {
    fetch('/api/subjects')
        .then(response => response.json())
        .then(subjects => {
            const selects = document.querySelectorAll('#questionSubject, #paperSubject');
            selects.forEach(select => {
                const currentValue = select.value;
                select.innerHTML = '<option value="">-- Choose a subject --</option>';
                subjects.forEach(subject => {
                    const option = document.createElement('option');
                    option.value = subject.subjectId;
                    option.textContent = subject.subjectName;
                    select.appendChild(option);
                });
                select.value = currentValue;
            });
        })
        .catch(error => console.error('Error loading subjects for select:', error));
}

// Delete row from table
function deleteSubject(subjectId) {
    if (confirm('Are you sure you want to delete this subject?')) {
        fetch(`/api/subjects/${subjectId}`, {
            method: 'DELETE',
            headers: {
                ...getCsrfHeaders()
            }
        })
        .then(response => {
            if (response.ok) {
                loadSubjects();
                updateSubjectSelects();
                showNotification('Subject deleted successfully!', 'success');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showNotification('Failed to delete subject', 'error');
        });
    }
}

function deleteQuestion(questionId) {
    if (confirm('Are you sure you want to delete this question?')) {
        fetch(`/api/questions/${questionId}`, {
            method: 'DELETE',
            headers: {
                ...getCsrfHeaders()
            }
        })
        .then(response => {
            if (response.ok) {
                loadQuestions();
                showNotification('Question deleted successfully!', 'success');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showNotification('Failed to delete question', 'error');
        });
    }
}

// Download paper as PDF
function downloadPaper() {
    const subjectSelect = document.getElementById('paperSubject');
    const subjectId = parseInt(subjectSelect.value);
    
    if (!subjectId) {
        showNotification('Please select a subject first', 'error');
        return;
    }
    
    window.location.href = `/api/papers/${subjectId}/pdf`;
}

// Utility function to escape HTML
function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, m => map[m]);
}

// Show notification
function showNotification(message, type) {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        background-color: ${type === 'success' ? '#27ae60' : '#e74c3c'};
        color: white;
        border-radius: 4px;
        box-shadow: 0 2px 10px rgba(0,0,0,0.2);
        z-index: 1000;
        animation: slideIn 0.3s ease;
    `;
    document.body.appendChild(notification);
    
    setTimeout(() => {
        notification.remove();
    }, 3000);
}

// Add animation styles
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(400px);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }

    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(400px);
            opacity: 0;
        }
    }

    .badge {
        display: inline-block;
        padding: 4px 8px;
        border-radius: 4px;
        font-size: 0.85em;
        font-weight: 600;
    }

    .badge-easy {
        background-color: #27ae60;
        color: white;
    }

    .badge-medium {
        background-color: #f39c12;
        color: white;
    }

    .badge-hard {
        background-color: #e74c3c;
        color: white;
    }
`;
document.head.appendChild(style);
