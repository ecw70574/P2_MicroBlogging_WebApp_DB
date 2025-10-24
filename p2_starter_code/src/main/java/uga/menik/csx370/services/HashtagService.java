package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;


@Service
/*
 * This Service contains post related functions. 
 */
public class HashtagService {
    private final DataSource dataSource;
    private final UserService userService;
    private final PostService postService;

    @Autowired
    public HashtagService(DataSource dataSource, UserService userService, PostService postService) {
        this.dataSource = dataSource;
        this.userService = userService;
        this.postService = postService;
    }

    /*
     * This function should search and return any posts with the hastags. 
     */
    public List<Post> searchPostHashtags(String hashtags) throws SQLException {
        // List of posts with the hastags
        List<Post> posts = new ArrayList<>();

        User this_user = userService.getLoggedInUser();
        String logged_in_userId = this_user.getUserId();

        // we have the list of hashtag words here but need to split them up individaully 
        List<String> searchedHashtags = new ArrayList<>(); //array lsit with hashtags
        String [] search = hashtags.split(" "); //list of all splits
        System.out.println();
        for (int i = 0; i < search.length; i++) {
            if (search[i].startsWith("#")) { //get items that start w/ hashtags
                System.out.print(search[i] + ", ");
                searchedHashtags.add(search[i]); //add to list
            } 
        }

        String getPostSql = "WITH userBookmarked AS ( SELECT postId	" +	//logged in user bookmarks
                                                     "FROM bookmark " +  
                                                     "WHERE userId = ? " + //logged in user
                                                    "), " +
                                "userHearted AS ( SELECT postId	" +	//logged in user hearts
                                                  "FROM post_like " +
                                                  "WHERE userId = ?" + //logged in user
                                                  ") " + 
                            "select p.postId, p.content, p.postDate, " + 
                                    "u.userID, u.firstName, u.lastName, " + 
                                    "(SELECT ub.postID " + //get bookmarked post ids by logged in user
                                        "FROM userBookmarked AS ub " +
                                        "WHERE ub.postId = p.postId) " + //filter by the postID
                                        "AS userBookmarkedPost, " + //use this alias for boolean isBookmarked in helper method
                                    "(SELECT uh.postId " + //get hearted post ids by logged in user
                                        "FROM userHearted AS uh " +
                                        "WHERE uh.postId = p.postId) " + //filter by the postID
                                        "AS userHeartedPost, " + //use this alias for boolean isHearted in helper method
                                    "(SELECT COUNT(*) " + //get count of hearts for posts
                                        "FROM post_like AS pl " + 
                                        "WHERE pl.postId = p.postId) " + //post like for posts
                                            "AS heartsCount, " + //use this alias for int heartsCount in helper method
                                    "(SELECT COUNT(*) " +
                                        "FROM comment as c " +
                                        "WHERE c.postId = p.postId) " +
                                            "AS commentCount " +
                            "from post AS p, user AS u " + 
                            "where p.userId = u.userId and (" ;
        
        // looping through the hastags so that we can search individually
        if(!searchedHashtags.isEmpty()) { //if there are hastags to search
            for (int i = 0; i < searchedHashtags.size(); i++) {
                if (i > 0) {
                    getPostSql += " and "; // add between
                }
                getPostSql += "p.content REGEXP ?"; // content contains the hashtag
            }
        }
        

        // still need to implement that the most recent posts are displayed first 
        getPostSql += ") ORDER BY p.postDate DESC";
        
        try(Connection conn = dataSource.getConnection();
            PreparedStatement hashtStmt = conn.prepareStatement(getPostSql)){ //passes sql query
                hashtStmt.setString(1, logged_in_userId);
                hashtStmt.setString(2, logged_in_userId);
        // binding the like statements to the hashtags
        // (^|\s): means start (^) can be a space or (|) the searchedHashtag
        // ($|\s): means end ($) can be a space or (|) just the end
            for (int i = 0; i < searchedHashtags.size(); i++) {
                hashtStmt.setString(i + 3, "(^|\s)" + searchedHashtags.get(i) + "($|\s)");
            }

            try (ResultSet rs = hashtStmt.executeQuery()) {
                while (rs.next()) {
                    //set helper method parameters
                    boolean isBookmarked = false; //determine if new Post object is bookmarked
                    if (rs.getString("userBookmarkedPost") != null) { //if exists, than true
                        isBookmarked = true; 
                    } //if
                    boolean isHearted = false; //determine if new Post object is hearted
                    if (rs.getString("userHeartedPost") != null) { //if exists, than true
                        isHearted = true;
                    } //if
                    int heartsCount = rs.getInt("heartsCount");
		            int commentCount = rs.getInt("commentCount");
                    posts.add(postService.helpPost(rs, heartsCount, commentCount, isHearted, isBookmarked));
                }
            } catch(SQLException e) {
                System.out.println(e);
            }
        } catch(SQLException e) {
            System.err.println("Error in HashtagService, searchPostHashtags: " + e);
        }
        return posts;

    } // searchPostHashtags

}
