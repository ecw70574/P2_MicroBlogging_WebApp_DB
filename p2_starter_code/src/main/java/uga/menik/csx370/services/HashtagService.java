package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Autowired
    public HashtagService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /*
     * This function should search and return any posts with the hastags. 
     */
    public List<Post> searchPostHashtags(String hashtags) throws SQLException {
        // List of posts with the hastags
        List<Post> posts = new ArrayList<>();

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

        String getPostSql = "select p.postId, p.content, p.postDate, u.userID, u.firstName, u.lastName, u.lastActiveDate " + 
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

        // binding the like statements to the hashtags
        // (^|\s): means start (^) can be a space or (|) the searchedHashtag
        // ($|\s): means end ($) can be a space or (|) just the end
            for (int i = 0; i < searchedHashtags.size(); i++) {
                hashtStmt.setString(i + 1, "(^|\s)" + searchedHashtags.get(i) + "($|\s)");
            }

            try (ResultSet rs = hashtStmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp currentUTCActiveDate = rs.getTimestamp("lastActiveDate"); //get timestamp in utc
                    //convert to Eastern time: -4 hours
                    LocalDateTime correctedEasterndateTimeActiveDate = currentUTCActiveDate.toLocalDateTime().minusHours(4);

                    User user = new User(
                        rs.getString("userId"), 
                        rs.getString("firstName"), 
                        rs.getString("lastName"),
                        correctedEasterndateTimeActiveDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a"))
                        );
                    Post post = new Post(
                        rs.getString("postId"),
                        rs.getString("content"),
                        rs.getTimestamp("postDate").toString(),
                        user,
                        0,
                        0,
                        false,
                        false
                    );
                    posts.add(post);
                }
            } catch(SQLException e) {
                System.out.println(e);
            }
        } catch(SQLException e) {
        }
        return posts;

    } // searchPostHashtags

}
