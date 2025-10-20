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
 * This service contains bookmark related functions. 
 */
public class BookmarksService {
    private final DataSource dataSource;
   

    @Autowired
    public BookmarksService(DataSource datasource) {
        this.dataSource = datasource;
    }

    /*
     * Adds a bookmark to a post. 
     * returns true is bookmark was added. 
     */
    public boolean addBookmark(User user, String postId) throws SQLException {
        // inserting the user and the post they bookmarked into the bookmark table 
        final String postSql = "insert into bookmark (userId, postId) values (?, ?)";

        try (Connection conn = dataSource.getConnection(); //establish connection with database
            PreparedStatement postStmt = conn.prepareStatement(postSql)) { //passes sql queary
            postStmt.setString(1, user.getUserId());
            postStmt.setString(2, postId); // unique identifier for post (assuming this will be in the post table)

            int rowsAffected = postStmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /*
     * Removes a bookmark from a post. 
     * returns true if the bookmark was removed.
     */
    public boolean removeBookmark(User user, String postId) throws SQLException {
        // deleting the user and the post they bookmarked into the bookmark table 
        final String removeSql = "delete from bookmark where userId = ? and postId = ?";

        try (Connection conn = dataSource.getConnection(); //establish connection with database
            PreparedStatement removeStmt = conn.prepareStatement(removeSql)) { //passes sql queary
            removeStmt.setString(1, user.getUserId());
            removeStmt.setString(2, postId); // unique identifier for post (assuming this will be in the post table)

            int rowsAffected = removeStmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /*
     * Shows the posts that the User has bookmarked. 
     */
    public List<Post> getBookMarked(User user) throws SQLException {
        // joining post and bookmarks to get the posts that have been bookmarked
        final String getBookMarkedSql = "select p.postId, p.content, p.postDate, u.userID, u.firstName, u.lastName" +
        "from bookmark b" +
        "join post p on p.postId = b.postId" +  
        "join user u on u.userId = p.userId" +
        "where b.userId = ?";

        List<Post> posts = new ArrayList<>();

        try (Connection conn = dataSource.getConnection(); //establish connection with database
            PreparedStatement getBookedStmt = conn.prepareStatement(getBookMarkedSql)) { //passes sql queary
            getBookedStmt.setString(1, user.getUserId());
            
            try (ResultSet rs = getBookedStmt.executeQuery()) {
                while (rs.next()) {
                    User postAuthor = new User(
                        rs.getString("userId"), 
                        rs.getString("firstName"), 
                        rs.getString("lastName")
                        );
                    Post post = new Post(
                        rs.getString("postId"),
                        rs.getString("content"),
                        rs.getTimestamp("postDate").toString(),
                        postAuthor,
                        0,
                        0,
                        false,
                        false
                    );
                    posts.add(post);
                } 
            } // try
        } 
        return posts;
    }

    
}
