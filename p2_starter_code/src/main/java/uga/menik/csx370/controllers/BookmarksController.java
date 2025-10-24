/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.controllers;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;
import uga.menik.csx370.services.BookmarksService;
import uga.menik.csx370.services.PeopleService;
import uga.menik.csx370.services.UserService;


/**
 * Handles /bookmarks and its sub URLs.
 * No other URLs at this point.
 * 
 * Learn more about @Controller here: 
 * https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html
 */
@Controller
@RequestMapping("/bookmarks")
public class BookmarksController {

    
    private final BookmarksService bookmarksService;
    private final PeopleService peopleService;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(BookmarksController.class);

    @Autowired
    public BookmarksController(PeopleService peopleService, UserService userService, BookmarksService bookmarksService) {
        this.peopleService = peopleService;
        this.userService = userService; 
        this.bookmarksService = bookmarksService;
    }

    /**
     * /bookmarks URL itself is handled by this.
     */
    @GetMapping
    public ModelAndView webpage() {
        // posts_page is a mustache template from src/main/resources/templates.
        // ModelAndView class enables initializing one and populating placeholders
        // in the template using Java objects assigned to named properties.
        ModelAndView mv = new ModelAndView("posts_page");

        // getting the logged in user so that their book marked posted appear on the page
        User loggedInUser = userService.getLoggedInUser();

        // Adding the bookmarked Posts to the page 
        try {
            List<Post> posts = bookmarksService.getBookMarked(loggedInUser);
            if (posts.isEmpty()) { //show no content message
                mv.addObject("isNoContent", true);
            } else {
                mv.addObject("posts", posts); // object not empy 
            }
        } catch (SQLException e) {
            // If an error occured, you can set the following property with the
            // error message to show the error message to the user.
	        logger.error("Database error occurred", e);
	        mv.addObject("errorMessage", "A database error occurred. Please try again later.");
        } //try-catch
        return mv;
    }
    
}
