/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.controllers;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.csx370.models.FollowableUser;
import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;
import uga.menik.csx370.services.BookmarksService;
import uga.menik.csx370.services.HashtagService;
import uga.menik.csx370.services.PeopleService;
import uga.menik.csx370.services.UserService;
import uga.menik.csx370.utility.Utility;

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

    @Autowired
    private BookmarksService bookmarksService;
    private PeopleService peopleService;
    private UserService userService;

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
            if (posts.isEmpty()) {
                mv.addObject("isNoContent", true);
            } else {
                mv.addObject("posts", posts); // object not empy 
            }
        } catch (SQLException e) {
            String errorMessage = "Some error occurred!";
            mv.addObject("errorMessage", errorMessage);
        }
        

        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        // String errorMessage = "Some error occured!";
        // mv.addObject("errorMessage", errorMessage);

        // Enable the following line if you want to show no content message.
        // Do that if your content list is empty.
        // mv.addObject("isNoContent", true);

        return mv;
    }
    
}
