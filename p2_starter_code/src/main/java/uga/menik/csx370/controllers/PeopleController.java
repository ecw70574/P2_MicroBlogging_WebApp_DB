/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.csx370.models.FollowableUser;
import uga.menik.csx370.services.PeopleService;
import uga.menik.csx370.services.UserService;

/**
 * Handles /people URL and its sub URL paths.
 */
@Controller
@RequestMapping("/people")
public class PeopleController {

    private final PeopleService peopleService;
    private final UserService userService; 
    @Autowired
    public PeopleController(PeopleService peopleService, UserService userService) {
        this.peopleService = peopleService;
        this.userService = userService; 
    }
    
    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error) {
        ModelAndView mv = new ModelAndView("people_page");

        List<FollowableUser> followableUsers = peopleService.getFollowableUsers(
            userService.getLoggedInUser().getUserId()
        );
        mv.addObject("users", followableUsers);

        String errorMessage = error;
        mv.addObject("errorMessage", errorMessage);
        
        return mv;
    } //webpage


    /**
    * Handles follow/unfollow functionality.
    */
    @GetMapping("{userId}/follow/{isFollow}")
    public String followUnfollowUser(@PathVariable("userId") String userId,
            @PathVariable("isFollow") Boolean isFollow) {
        String followerId = userService.getLoggedInUser().getUserId();

        boolean success = isFollow
            ? peopleService.followUser(followerId, userId)
            : peopleService.unfollowUser(followerId, userId);

        if (success) {
            return "redirect:/people";
        } else {
            String message = URLEncoder.encode(
                "Cannot unfollow user",
                StandardCharsets.UTF_8);
            return "redirect:/people?error=" + message;
        }
    } //followUnfollowUser

}
