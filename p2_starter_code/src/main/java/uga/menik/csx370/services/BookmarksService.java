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
import uga.menik.csx370.services.PostService;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;

@Service
/*
 * This service contains bookmark related functions. 
 */
public class BookmarksService {
    private final DataSource dataSource;
    private final PostService postService;
   

    @Autowired
    public BookmarksService(DataSource datasource, PostService postService) {
        this.dataSource = datasource;
        this.postService = postService;
    }

    /*
     * Adds a bookmark to a post. 
     * returns true is bookmark was added. 
     */
    public boolean addBookmark(User user, String postId) throws SQLException {
        final String getAuthor = "select p.userId from post p where p.postId = ?";
        // run author query first
        String authorId = null;

        try (Connection conn = dataSource.getConnection(); //establish connection with database
            PreparedStatement authorStmt = conn.prepareStatement(getAuthor)) { //passes sql query
            authorStmt.setString(1, postId);
            ResultSet rs = authorStmt.executeQuery();
            // whose post is this

            if (rs.next()) {
                authorId = rs.getString("userId");  //  get author ID
            } else {
                System.out.println("No author found for postId: " + postId);
                return false;
            }
	    
        } catch (SQLException e){
            e.printStackTrace();
            return false;
	    }

	System.out.println("Author id going into query: " + authorId);


        // inserting the user and the post they bookmarked into the bookmark table 
        final String postSql = "insert into bookmark (userId, postId, authorId) values (?, ?, ?)";

        try (Connection conn = dataSource.getConnection(); //establish connection with database
            PreparedStatement postStmt = conn.prepareStatement(postSql)) { //passes sql queary

            postStmt.setString(1, user.getUserId());
            postStmt.setString(2, postId); // unique identifier for post (assuming this will be in the post table)
            postStmt.setString(3, authorId);
            // whose post is this

            int rowsAffected = postStmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e){
            e.printStackTrace();
            return false;
	    }
    }

    /*
     * Removes a bookmark from a post. 
     * returns true if the bookmark was removed.
     */
    public boolean removeBookmark(User user, String postId) throws SQLException {

        // shouldnt need this first part because we have both userId and postId to delete bookmark

        final String getAuthor = "select p.userId from post p where p.postId = ?";


        // run author query first
        String authorId = null;

        try (Connection conn = dataSource.getConnection(); //establish connection with database
            PreparedStatement authorStmt = conn.prepareStatement(getAuthor)) { //passes sql query
            authorStmt.setString(1, postId);
            ResultSet rs = authorStmt.executeQuery();
            // whose post is this

            if (rs.next()) {
                authorId = rs.getString("userId");  //  get author ID
            } else {
                System.out.println("No author found for postId: " + postId);
                return false;
            }
        } catch (SQLException e){
            e.printStackTrace();
            return false;
	    }

        // deleting the user and the post they bookmarked into the bookmark table 
        final String removeSql = "delete from bookmark where userId = ? and postId = ? and authorId = ?";

        try (Connection conn = dataSource.getConnection(); //establish connection with database
            PreparedStatement removeStmt = conn.prepareStatement(removeSql)) { //passes sql queary
            removeStmt.setString(1, user.getUserId());
            removeStmt.setString(2, postId); // unique identifier for post (assuming this will be in the post table)
            removeStmt.setString(3, authorId);

            int rowsAffected = removeStmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }

    }

    /*
     * Shows the posts that the User has bookmarked. 
     */
    public List<Post> getBookMarked(User user) throws SQLException {
        // joining post and bookmarks to get the posts that have been bookmarked
        /* 
        final String getBookMarkedSql = "select p.postId, b.authorId, p.content, p.postDate, u.firstName as authorFN, u.lastName as authorLN " +
        "from bookmark b " +
        "join post p on p.postId = b.postId " +  
        "join user u on u.userId = b.authorId " +
        "where b.userId = ?";
        */

        final String getBookMarkedSql = 
        "select count(distinct pl.userId) as heartsCount, p.postId, b.authorId as userId, p.content, p.postDate, u.firstName as authorFN, u.lastName as authorLN, " +
        "exists (select 1 from post_like pl2 where pl2.postId = p.postId and pl2.userId = ?) as isLiked " +
        "from bookmark b " +
        "join post p on p.postId = b.postId " +  
        "join user u on u.userId = b.authorId " +
        "left join post_like pl on pl.postId = p.postId " +
        "where b.userId = ? " +
        "group by p.postId, b.authorId, p.postDate, u.firstName, u.lastName";
        

        // final String getIsLikedByUser = "with userHearted as select(postId from post_like where userId = ?) as userHeartedPost";
        /* 
        boolean isLiked = false;
	    try(Connection conn = dataSource.getConnection();
	        PreparedStatement likeStmt = conn.prepareStatement(getBookMarkedSql)) {
            // params 
	        likeStmt.setString(1,user.getUserId());
            l
	        try(ResultSet rs = likeStmt.executeQuery()) {
		        if (rs.next()) {
		            isLiked = rs.getBoolean(1);
		        }
	        }
	    }
        */
        List<Post> posts = new ArrayList<>();

        try (Connection conn = dataSource.getConnection(); //establish connection with database
            PreparedStatement getBookedStmt = conn.prepareStatement(getBookMarkedSql)) { //passes sql queary

            getBookedStmt.setString(1, user.getUserId());
            getBookedStmt.setString(2, user.getUserId());

            try (ResultSet rs = getBookedStmt.executeQuery()) {
                while (rs.next()) {
                    // get the like count
                    int heartsCount = rs.getInt("heartsCount");

                    // get the liked by user
                    boolean isLiked = rs.getBoolean("isLiked");

                    User postAuthor = new User(
                        rs.getString("authorId"), 
                        rs.getString("authorFN"), 
                        rs.getString("authorLN")
                        );

                    Timestamp currentUTC = rs.getTimestamp("postDate"); //get timestamp in utc
                    //convert to Eastern time: -4 hours
                    LocalDateTime correctedEasterndateTime = currentUTC.toLocalDateTime().minusHours(4);
                
                    posts.add(postService.helpPost(rs, heartsCount, 0, isLiked, true));

                } 
            } // try
        } catch (SQLException e){
	    e.printStackTrace();
	}
	
        return posts;
    }

    
}
