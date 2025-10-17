package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;


/**
 * This service contains people related functions.
 */
@Service
public class PostService {

    private final DataSource dataSource;
    private final UserService userService;

    public PostService(DataSource dataSource, UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

    
    
    /**
     * This function should create new posts.
     * Returns true if post creation was successful. 
     */
    public boolean createPost(User user, String content) throws SQLException {
        //create SQL query to insert the post from the user into posts table
        //User user = userService.getLoggedInUser();
        final String postSql = "insert into post (userId, content, postDate) values (?, ?, NOW())";

        try (Connection conn = dataSource.getConnection(); //establish connection with database
            PreparedStatement postStmt = conn.prepareStatement(postSql)) { //passes sql queary
            postStmt.setString(1, user.getUserId());
            postStmt.setString(2, content);

            int rowsAffected = postStmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * This function should query and return all posts.
     * @return
     * @throws SQLException
     */


    public List<Post> getPosts() throws SQLException {
        List<Post> posts = new ArrayList<>();

        final String getPostSql = "select p.postId, p.content, p.postDate, u.userId, u.firstName, u.lastName from post p join user u on p.userId = u.userId" ;

        try(Connection conn = dataSource.getConnection();
        PreparedStatement postStmt = conn.prepareStatement(getPostSql); //passes sql query

        
        ResultSet rs = postStmt.executeQuery()) {
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
        }
        return posts;
    }

}
