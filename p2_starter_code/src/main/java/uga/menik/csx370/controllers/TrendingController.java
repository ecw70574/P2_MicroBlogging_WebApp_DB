package uga.menik.csx370.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;
import uga.menik.csx370.services.BookmarksService;
import uga.menik.csx370.services.PostService;
import uga.menik.csx370.services.UserService;
import uga.menik.csx370.services.TrendingService;


/**
 * Handles /trending URL.
 */
@Controller
@RequestMapping("/trending")
public class TrendingController {

    // UserService has user login and registration related functions.
    private final TrendingService trendingService;
    //private final UserService userService;

    /**
     * See notes in AuthInterceptor.java regarding how this works 
     * through dependency injection and inversion of control.
     */
    @Autowired
    public TrendingController(TrendingService trendingService) {
        this.trendingService = trendingService;
    }

    /**
     * This function handles /trending URL itself.
     * This serves the webpage that shows posts of the logged in user.
     */
    @GetMapping
    public ModelAndView showTrendingPage() {
        System.out.println("User is attempting to view the trending page");
        ModelAndView mv = new ModelAndView("posts_page");

        try {
            
            List<Post> trendingPosts = trendingService.getTrendingPosts();
            mv.addObject("posts", trendingPosts);

            if(trendingPosts.isEmpty()) {
                mv.addObject("isNoContent", true);
            }

        } catch (SQLException e) {

            String errorMessage = "Some error occured!";
            mv.addObject("errorMessage", errorMessage);
        }

        return mv;
    }
}