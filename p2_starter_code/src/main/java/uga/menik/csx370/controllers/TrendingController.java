package uga.menik.csx370.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.csx370.services.PostService;
import uga.menik.csx370.services.UserService;

/**
 * Handles /trending URL.
 */
@Controller
@RequestMapping("/trending")
public class TrendingController {

    // UserService has user login and registration related functions.
    private final PostService postService;
    private final UserService userService;

    /**
     * See notes in AuthInterceptor.java regarding how this works 
     * through dependency injection and inversion of control.
     */
    @Autowired
    public TrendingController(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    /**
     * This function handles /trending URL itself.
     * This serves the webpage that shows posts of the logged in user.
     */
    @GetMapping
    public ModelAndView showTrendingPage() {
        System.out.println("User is attempting to view the trending page");
        ModelAndView mv = new ModelAndView("posts_page");

        /* 
        try {
            
            List<Post> trendingPosts = postService.getTrendingPosts();
            mv.addObject("posts", trendingPosts);

            if(trendingPosts.isEmpty()) {
                mv.addObject("isNoContent", true);
            }

        } catch (SQLException e) {

            String errorMessage = "Some error occured!";
            mv.addObject("errorMessage", errorMessage);
        }
*/
        return mv;
    }
}