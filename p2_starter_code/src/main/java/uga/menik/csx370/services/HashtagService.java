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
        // still need to implement that the most recent posts are displayed first 
        String [] searchedHashtags = hashtags.split(" ");

        String getPostSql = "select p.postId, p.content, p.postDate, u.userID, u.firstName, u.lastName " + 
                                "from post AS p, user AS u " + 
                                "where p.userId = u.userId AND " ;

        //String getPostSql = "select p.content from post AS p where " ;

        // looping through the hastags so that we can search individually 
        for (int i = 0; i < searchedHashtags.length; i++) {
            if (i> 0) {
                getPostSql += " and "; // add between
            }
            getPostSql += "p.content like ?"; // content contains the hashtag
        }

        getPostSql += " ORDER BY p.postDate DESC";
        
        try(Connection conn = dataSource.getConnection();
            PreparedStatement hashtStmt = conn.prepareStatement(getPostSql)){ //passes sql query

        // binding the like statements to the hashtags
            for (int i = 0; i < searchedHashtags.length; i++) {
                hashtStmt.setString(i + 1, "%" + searchedHashtags[i] + "%");
            }

            try (ResultSet rs = hashtStmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(
                        rs.getString("userId"), 
                        rs.getString("firstName"), 
                        rs.getString("lastName")
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
