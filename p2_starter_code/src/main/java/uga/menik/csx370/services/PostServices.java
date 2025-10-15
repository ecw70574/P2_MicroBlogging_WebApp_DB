package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import uga.menik.csx370.models.FollowableUser;
import uga.menik.csx370.models.Post;

/**
 * This service contains people related functions.
 */
@Service
public class PostServices {

    private final DataSource dataSource;

    public PostServices(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * This function should create new posts.
     * Returns true if post creation was successful. 
     */
    public boolean createPost(String userId, String content) throws SQLException {
        //create SQL query to insert the post from the user into posts table
        final String postSql = "insert into posts (userId, content, postDate) values (?, ?, NOW())";

        try (Connection conn = dataSource.getConnection(); //establish connection with database
            PreparedStatement postStmt = conn.prepareStatement(postSql)) { //passes sql queary
            postStmt.setString(1, userId);
            postStmt.setString(2, content);

            int rowsAffected = postStmt.executeUpdate();
            return rowsAffected > 0;

    }

    /**
     * This function should query and return all posts.
     * @return
     * @throws SQLException
     */
    public List<Post> getPosts() throws SQLException {
        List<Post> posts = new ArrayList<>();

        final String getPostSql = "select * from post";

        try(Connection conn = dataSource.getConnection();
        PreparedStatement postStmt = conn.prepareStatement(getPostSql); //passes sql query
        ResultSet rs = postStmt.executeQuery()) {
            while (rs.next()) {
                Post post = new Post(
                    rs.getInt("postId"),
                    rs.getString("content"),
                    rs.getTimestamp("postDate"),
                    rs.getString("user")
                );
                posts.add(post);
            }
        }
        return posts;
    }

}
