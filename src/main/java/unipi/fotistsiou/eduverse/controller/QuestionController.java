package unipi.fotistsiou.eduverse.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import unipi.fotistsiou.eduverse.entity.Chapter;
import unipi.fotistsiou.eduverse.entity.Question;
import unipi.fotistsiou.eduverse.entity.User;
import unipi.fotistsiou.eduverse.service.ChapterService;
import unipi.fotistsiou.eduverse.service.QuestionService;
import unipi.fotistsiou.eduverse.service.UserService;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class QuestionController {
    private final QuestionService questionService;
    private final UserService userService;
    private final ChapterService chapterService;

    @Autowired
    public QuestionController(
        QuestionService questionService,
        UserService userService,
        ChapterService chapterService
    ){
        this.questionService = questionService;
        this.userService = userService;
        this.chapterService = chapterService;
    }

    @GetMapping("/question/new/{chapterId}/{userId}")
    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    public String createNewQuestionForm(
        @PathVariable Long chapterId,
        @PathVariable Long userId,
        Model model,
        Principal principal
    ){
        String authUsername = "anonymousUser";
        if (principal != null) {
            authUsername = principal.getName();
        }
        Optional<User> optionalUser = userService.findUserById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getEmail().equals(authUsername)) {
                Optional<Chapter> optionalChapter = chapterService.findChapterById(chapterId);
                if (optionalChapter.isPresent()) {
                    Chapter chapter = optionalChapter.get();
                    if (chapter.getCourse().getProfessor().getId().equals(userId)) {
                        Question question = new Question();
                        question.setChapter(chapter);
                        model.addAttribute("chapterId", chapterId);
                        model.addAttribute("userId", userId);
                        model.addAttribute("question", question);
                        return "question/question_new";
                    }
                }
            }
            return "redirect:/error_403";
        }
        return "redirect:/error_404";
    }

    @PostMapping("/question/new/{chapterId}/{userId}")
    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    public String createNewQuestion(
        @PathVariable Long chapterId,
        @PathVariable Long userId,
        @Valid @ModelAttribute("question") Question question,
        BindingResult result,
        Model model,
        Principal principal
    ){
        String authUsername = "anonymousUser";
        if (principal != null) {
            authUsername = principal.getName();
        }
        Optional<User> optionalUser = userService.findUserById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getEmail().equals(authUsername)) {
                Optional<Chapter> optionalChapter = chapterService.findChapterById(chapterId);
                if (optionalChapter.isPresent()) {
                    Chapter chapter = optionalChapter.get();
                    if (chapter.getCourse().getProfessor().getId().equals(userId)) {
                        if (result.hasErrors()) {
                            model.addAttribute("question", question);
                            return "question/question_new";
                        }
                        questionService.saveQuestion(question);
                        return String.format("redirect:/question/view/%d/%d?success_create_question", chapterId, userId);
                    }
                }
            }
            return "redirect:/error_403";
        }
        return "redirect:/error_404";
    }

    @GetMapping("/question/view/{chapterId}/{userId}")
    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    public String getChapterQuestions(
        @PathVariable Long chapterId,
        @PathVariable Long userId,
        Model model,
        Principal principal
    ){
        String authUsername = "anonymousUser";
        if (principal != null) {
            authUsername = principal.getName();
        }
        Optional<User> optionalUser = userService.findUserById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getEmail().equals(authUsername)) {
                Optional<Chapter> optionalChapter = chapterService.findChapterById(chapterId);
                if (optionalChapter.isPresent()) {
                    Chapter chapter = optionalChapter.get();
                    if (chapter.getCourse().getProfessor().getId().equals(userId)) {
                        List<Question> questions = questionService.findAllChapterQuestions(chapterId);
                        model.addAttribute("chapter", chapter);
                        model.addAttribute("questions", questions);
                        model.addAttribute("userId", userId);
                        return "question/question_view";
                    }
                }
            }
            return "redirect:/error_403";
        }
        return "redirect:/error_404";
    }
}