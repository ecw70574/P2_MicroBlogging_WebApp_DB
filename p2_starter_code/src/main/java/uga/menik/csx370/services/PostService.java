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
     * @return posts 
     * @throws SQLException
     */
    public List<Post> getPosts() throws SQLException {
        List<Post> posts = new ArrayList<>();

        // in bookmarked table

        User this_user = userService.getLoggedInUser();
        String logged_in_userId = this_user.getUserId();
        final bookmarked_posts = "SELECT p.postId, p.content, p.userId, u.firstName, u.lastName " + 
            "FROM post p " +
            "JOIN user u ON p.userId = u.userId " +
            "WHERE p.postId IN ( " +
                "SELECT b.postId FROM bookmark b WHERE b.userId = ? )";

        try(Connection conn = dataSource.getConnection();
        PreparedStatement isBooked = conn.prepareStatement(bookmarked_posts)) { //passes sql query

            try(ResultSet rs = isBooked.executeQuery()) {
                while (rs.next()) {
                    User user = new User(
                        rs.getString("userId"),
                        rs.getString("firstName"),
                        rs.getString("lastName")
                        );

                    Timestamp currentUTC = rs.getTimestamp("postDate"); //get timestamp in utc
                    //convert to Eastern time: -4 hours
                    LocalDateTime correctedEasterndateTime = currentUTC.toLocalDateTime().minusHours(4);
                    
                    Post post = new Post(
                        rs.getString("postId"),
                        rs.getString("content"),
                        correctedEasterndateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a")), // format String
                        user,
                        0,
                        0,
                        false,
                        true
                    );
                    posts.add(post);
                }
            }
        }


        final not_bookmarked_posts = "SELECT p.postId, p.content, p.userId, u.firstName, u.lastName " + 
            "FROM post p " +
            "JOIN user u ON p.userId = u.userId " +
            "WHERE p.postId IN ( " +
                "SELECT b.postId FROM bookmark b WHERE b.userId = ? )";

        try(Connection conn = dataSource.getConnection();
        PreparedStatement isnotBooked = conn.prepareStatement(not_bookmarked_posts)) { //passes sql query

            try(ResultSet rs = isnotBooked.executeQuery()) {
                while (rs.next()) {
                    User user = new User(
                        rs.getString("userId"),
                        rs.getString("firstName"),
                        rs.getString("lastName")
                        );

                    Timestamp currentUTC = rs.getTimestamp("postDate"); //get timestamp in utc
                    //convert to Eastern time: -4 hours
                    LocalDateTime correctedEasterndateTime = currentUTC.toLocalDateTime().minusHours(4);
                    
                    Post post = new Post(
                        rs.getString("postId"),
                        rs.getString("content"),
                        correctedEasterndateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a")), // format String
                        user,
                        0,
                        0,
                        false,
                        false
                    );
                    posts.add(post);
                }
            }
        }

'''


        final String getPostSql = "select p.postId, p.content, p.postDate, u.userId, u.firstName, u.lastName " +
        "from post p join user u on p.userId = u.userId order by p.postDate desc" ;

        try(Connection conn = dataSource.getConnection();
        PreparedStatement postStmt = conn.prepareStatement(getPostSql)) { //passes sql query

            try(ResultSet rs = postStmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(
                        rs.getString("userId"),
                        rs.getString("firstName"),
                        rs.getString("lastName")
                        );

                    Timestamp currentUTC = rs.getTimestamp("postDate"); //get timestamp in utc
                    //convert to Eastern time: -4 hours
                    LocalDateTime correctedEasterndateTime = currentUTC.toLocalDateTime().minusHours(4);
                    
                    Post post = new Post(
                        rs.getString("postId"),
                        rs.getString("content"),
                        correctedEasterndateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a")), // format String
                        user,
                        0,
                        0,
                        false,
                        false
                    );
                    posts.add(post);
                }
            }
        }
'''
        return posts;
    }

    public List<Post> getPostById(String postId) throws SQLException {
        List<Post> posts = new ArrayList<>();

        final String getPostSql = "select p.postId, p.content, p.postDate, u.userId, u.firstName, u.lastName " +
        "from post p join user u on p.userId = u.userId where p.postId = ?" ;

        try(Connection conn = dataSource.getConnection();
        PreparedStatement postStmt = conn.prepareStatement(getPostSql)) { //passes sql query
            postStmt.setString(1, postId);

            try(ResultSet rs = postStmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(
                        rs.getString("userId"),
                        rs.getString("firstName"),
                        rs.getString("lastName")
                        );

                    Timestamp currentUTC = rs.getTimestamp("postDate"); //get timestamp in utc
                    //convert to Eastern time: -4 hours
                    LocalDateTime correctedEasterndateTime = currentUTC.toLocalDateTime().minusHours(4);
                    
                    Post post = new Post(
                        rs.getString("postId"),
                        rs.getString("content"),
                        correctedEasterndateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a")), // format String
                        user,
                        0,
                        0,
                        false,
                        false
                    );
                    posts.add(post);
                }
            }
        }
        return posts;
    }


/**
     * This function should query and return all from the specified userposts.
     * @return the posts
     * @throws SQLException
     */
    public List<Post> getUserPosts(String userId) throws SQLException {
        List<Post> posts = new ArrayList<>();
        final String getPostSql = "select p.postId, p.content, p.postDate, u.userId, u.firstName, u.lastName " +
        "from post p join user u on p.userId = u.userId where p.userId = ? order by p.postDate desc" ;

        try(Connection conn = dataSource.getConnection();
        PreparedStatement postStmt = conn.prepareStatement(getPostSql)) {//passes sql query
            postStmt.setString(1, userId);
        
            try(ResultSet rs = postStmt.executeQuery()) {
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
        }
        return posts;
    }


    //adds a like to a post
    public boolean addLike(String userId, String postId) {
        // inserts a like in sql
        final String addLikeSql = "INSERT IGNORE INTO post_like (userId, postId) VALUES (?, ?)";

        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(addLikeSql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, postId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }
    }

    //removes a like from a post
    public boolean removeLike(String userId, String postId) {
        // deletes the like - sql
        final String removeLikeSql = "DELETE FROM post_like WHERE userId = ? AND postId = ?";

        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(removeLikeSql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, postId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }
    }

}
