package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import uga.menik.csx370.models.Comment;
import uga.menik.csx370.models.ExpandedPost;
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
    } //PostService
    
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
        } //try
    } //createPost

    /**
     * This function should query and return all posts.
     * @return posts 
     * @throws SQLException
     */
    public List<Post> getPosts() throws SQLException {
        List<Post> posts = new ArrayList<>();

        User this_user = userService.getLoggedInUser();
        String logged_in_userId = this_user.getUserId();
        final String bookmarked_liked_posts = "WITH userBookmarked AS ( SELECT postId	" +	//logged in user bookmarks
                                                                        "FROM bookmark " +  
                                                                        "WHERE userId = ? " + //logged in user
                                                                        "), " +
                                                    "userHearted AS ( SELECT postId	" +	//logged in user hearts
                                                                "FROM post_like " +
                                                                        "WHERE userId = ?" + //logged in user
                                                ") " +
                                                "SELECT p.postId, p.content, p.userId, p.postDate, " +
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
	                                            // once comments in implemented:
                                                    // (SELECT COUNT(*) FROM comments AS c WHERE c.postID = p.postID) AS commentsCount
                                                    "FROM post p " +
                                                    "JOIN user u ON p.userId = u.userId " +
                                                    "WHERE p.userId IN (SELECT f.followeeId FROM follow f WHERE f.followerId = ?) " +
                                                            "OR p.userId = ? " +
                                                    "ORDER BY p.postDate DESC ";
                                                    //"WHERE p.postId IN (SELECT b.postId FROM bookmark b WHERE b.userId = ? ) " +
                                                    //"and p.postId IN (SELECT l.postId FROM post_like l WHERE l.userId = ?)";

        try(Connection conn = dataSource.getConnection();
        PreparedStatement isBooked1 = conn.prepareStatement(bookmarked_liked_posts)) { //passes sql query
            isBooked1.setString(1, logged_in_userId);
            isBooked1.setString(2, logged_in_userId);
            isBooked1.setString(3, logged_in_userId);
            isBooked1.setString(4, logged_in_userId);
            try(ResultSet rs = isBooked1.executeQuery()) {
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
                    posts.add(helpPost(rs, heartsCount, commentCount, isHearted, isBookmarked)); // isHearted = true, isBookmarked = true
                } //while
            } //try
        } //try
        return posts;
    } //getPosts

    public List<Post> getPostById(String postId) throws SQLException {
        List<Post> posts = new ArrayList<>();
        User this_user = userService.getLoggedInUser();
        String logged_in_userId = this_user.getUserId();

	    // is current post bookmarked by current user?

	    final String findIfBookmarked = "SELECT EXISTS (SELECT 1 FROM bookmark b WHERE b.postId = ? AND b.userId = ?)";

	    boolean isBookmarked = false;
        try(Connection conn = dataSource.getConnection();
            PreparedStatement isitBookmarked = conn.prepareStatement(findIfBookmarked)) {
            isitBookmarked.setString(1,postId);
            isitBookmarked.setString(2,logged_in_userId);
            try(ResultSet rs = isitBookmarked.executeQuery()) {
                if (rs.next()) {
                    isBookmarked = rs.getBoolean(1);
                } //if
            } //try
        } //try

	    // is current post liked by current user?

	    final String findIfLiked = "SELECT EXISTS (SELECT 1 FROM post_like l WHERE l.postId = ? AND l.userId = ?)";

	    boolean isLiked = false;
        try(Connection conn = dataSource.getConnection();
            PreparedStatement likeStmt = conn.prepareStatement(findIfLiked)) {
            likeStmt.setString(1,postId);
            likeStmt.setString(2,logged_in_userId);
            try(ResultSet rs = likeStmt.executeQuery()) {
                if (rs.next()) {
                    isLiked = rs.getBoolean(1);
                } //if
            } //try
        } //try
        
        /*
        final String getPostSql = "select count(pl.userId) as heartsCount, p.postId, p.content, p.postDate, u.userId, u.firstName, u.lastName " +
        "from post p join user u on p.userId = u.userId where p.postId = ?" +
        "and join post_like pl where pl.postId = p.postId";
         */

        final String getPostSql = "SELECT COUNT(DISTINCT pl.userId) as heartsCount, p.postId, p.content, p.postDate, u.userId, u.firstName, u.lastName, " +
                                            "COUNT(DISTINCT c.commentId) as commentCount " +
                                    "FROM post p " + 
                                    "JOIN user u on p.userId = u.userId " +
                                    "LEFT JOIN post_like pl ON pl.postId = p.postId " +
                                    "LEFT JOIN comment c ON c.postId = p.postId " +
                                    "WHERE p.postId = ? " +
                                    "GROUP BY p.postId, p.content, p.postDate, u.userId, u.firstName, u.lastName";

        List<Comment> comments_on_post = getCommentsByPostId(postId);

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
		    
                    Timestamp currentUTC = rs.getTimestamp("postDate"); // UTC from DB
                    ZonedDateTime utcZoned = currentUTC.toInstant().atZone(ZoneId.of("UTC"));
                    ZonedDateTime easternZoned = utcZoned.withZoneSameInstant(ZoneId.of("America/New_York"));
                    String formattedDate = easternZoned.format(DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a"));

                    Post post = new ExpandedPost(
                        rs.getString("postId"),
                        rs.getString("content"),
			            formattedDate,
                        user,
                        rs.getInt("heartsCount"),
                        rs.getInt("commentCount"),
                        isLiked,
                        isBookmarked,
                        comments_on_post
                    );
                    posts.add(post); 
                } //while
            } //try
	    } //try
        return posts;
    } //getPostById


     /**
     * This function should query and return all from the specified userposts.
     * @return the posts
     * @throws SQLException
     */
    public List<Post> getUserPosts(String userId) throws SQLException {

        List<Post> posts = new ArrayList<>();

        User this_user = userService.getLoggedInUser();  //get the logged in user
        String logged_in_userId = this_user.getUserId(); //get the id of the logged in user
        
        final String getPostSql = "WITH userBookmarked AS ( SELECT postID	" +	//logged in user bookmarks
                                                            "FROM bookmark " +  
                                                            "WHERE userID = ? " + //logged in user
                                                          "), " +
                                        "userHearted AS ( SELECT postID	" +	//logged in user hearts
                                            "FROM post_like " +
                                            "WHERE userID = ?" + //logged in user
                                  ") " +
                                  "SELECT p.postID, p.content, p.postDate, " + //post info needed for post object
                                            "u.userID, u.firstName, u.lastName, " + //user info needed for user object
                                            "(SELECT ub.postID " + //get bookmarked post ids by logged in user
                                                "FROM userBookmarked AS ub " +
                                                "WHERE ub.postID = p.postID) " + //filter by the postID
                                            "AS userBookmarkedPost, " + //use this alias for boolean isBookmarked in helper method
                                            "(SELECT uh.postID " + //get hearted post ids by logged in user
                                                "FROM userHearted AS uh " +
                                                "WHERE uh.postID = p.postID) " + //filter by the postID
                                            "AS userHeartedPost, " + //use this alias for boolean isHearted in helper method
                                            "(SELECT COUNT(*) " + //get count of hearts for posts
                                                "FROM post_like AS pl " + 
                                                "WHERE pl.postID = p.postID)" + //post like for posts
                                            "AS heartsCount, " + //use this alias for int heartsCount in helper method
                                            "(SELECT COUNT(*) " +
	                                        "FROM comment as c " +
                                                "WHERE c.postId = p.postId) " +
                                            "AS commentCount " +

                                            // once comments in implemented:
                                            // (SELECT COUNT(*) FROM comments AS c WHERE c.postID = p.postID) AS commentsCount
                                  "FROM post AS p, user AS u " + //join post and user on userID
                                  "WHERE p.userID = u.userID " + 
                                    "AND p.userID = ? " + //of the specified userID user
                                  "ORDER BY p.postDate DESC;"; //newest posts at top
        try(Connection conn = dataSource.getConnection();
        PreparedStatement postStmt = conn.prepareStatement(getPostSql)) {//passes sql query
            //fill in the ?'s
            postStmt.setString(1, logged_in_userId); //for logged in user bookmarks
            postStmt.setString(2, logged_in_userId); //for logged in user hearts
            postStmt.setString(3, userId); //specified userpost users
        
            try(ResultSet rs = postStmt.executeQuery()) {
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
                    //once comments is implemented:
                    //int commentsCount = rs.getInt("heartsCount");
                    posts.add(helpPost(rs, heartsCount, commentCount, isHearted, isBookmarked)); //call helper method
                }
            } //tru
        } //try
        return posts;
    } //getUserPosts


    //adds a like to a post
    public boolean addLike(String userId, String postId) {

        String sql = "insert ignore into post_like (userId, postId) values (?, ?)";

        try (Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.setString(2, postId);

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("error adding like: " + e.getMessage());
            return false;
        } //try-catch
    } //addLike

        //removes a like from a post
    public boolean removeLike(String userId, String postId) {
        String sql = "delete from post_like where userId = ? and postId = ?";

        try (Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.setString(2, postId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("error removing like: " + e.getMessage());
            return false;
        } //try-catch
    } //removeLike

    //Example use:
    //    posts.set(helpPost(rs, 0, 0, false, false));
    public Post helpPost(ResultSet rs, int heartsCount, int commentsCount, boolean isHearted, boolean isBookmarked) throws SQLException {
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
                heartsCount,
                commentsCount,
                isHearted,
                isBookmarked
            );
        return post;
    } //helpPost

    public boolean createComment(String userId, String postId, String content) throws SQLException {
	System.out.println("Post controller has entered post service method for creating comment");
        String commentSql = "insert into comment (commenterId, postId, content, commentDate) values (?, ?, ?, NOW())";
        try (Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(commentSql)) {
            stmt.setString(1, userId);
            stmt.setString(2, postId);
            stmt.setString(3, content);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    } // createComment: Mariah's method


    public List<Comment> getCommentsByPostId(String postId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        final String commentSql = "Select c.commentId, c.content, c.commentDate, u.userId, u.firstName, u.lastName " +
            "From comment c Join user u ON c.commenterId = u.userId Where c.postId = ? order by c.commentDate desc";

        try(Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(commentSql)) {

            stmt.setString(1, postId);
            try(ResultSet rs = stmt.executeQuery()) {


                while (rs.next()) {

		    Timestamp commentTimestamp = rs.getTimestamp("commentDate");
		    ZonedDateTime easternTime = commentTimestamp.toInstant().atZone(ZoneId.of("America/New_York"));
		    String formattedCommentDate = easternTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a"));

		    User user = new User(
					 rs.getString("userId"),
					 rs.getString("firstName"),
					 rs.getString("lastName")

					 );
		    Comment comment = new Comment(
						  rs.getString("commentId"),
						  rs.getString("content"),
						  // rs.getTimestamp("commentDate").toString(),
						  formattedCommentDate,
						  user
						  );
		    comments.add(comment);
                }
            }
        }
        return comments;
    } // getCommentsByPostId: Mariah's method
}



