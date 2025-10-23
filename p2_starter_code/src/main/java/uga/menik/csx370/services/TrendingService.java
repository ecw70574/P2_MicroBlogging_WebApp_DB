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

@Service
public class TrendingService {


    private final DataSource dataSource;
    private final UserService userService;
    private final PostService postService;



    public TrendingService(DataSource dataSource, UserService userService, PostService postService) {
        this.dataSource = dataSource;
        this.userService = userService;
        this.postService = postService;
    }

    public List<Post> getTrendingPosts() throws SQLException {
        List<Post> posts = new ArrayList<>();

        User this_user = userService.getLoggedInUser();
        String logged_in_userId = this_user.getUserId();

        final String trendingSql = """
            With userBookmarked As (
                Select postId
                From bookmark
                Where userId = ?),
            userHearted As (
                Select postId
                From post_like
                Where userId = ?),
            likeCounts As (
                Select postId, Count(*) As likeCount
                From post_like
                Group by postId),
            bookmarkCounts As(
                Select postId, Count(*) as bookmarkCount
                from bookmark
                group by postId),
	    commentCounts As(
		Select postId, Count(*) as commentCount
		from comment
		group by postId)
            Select p.postId, p.content, p.userId, p.postDate,
                u.userId, u.firstName, u.lastName,
                (Select ub.postId from userBookmarked ub where ub.postId = p.postId) as userBookmarkedPost,
                (Select uh.postId from userHearted uh where uh.postId = p.postId) as userHeartedPost,
                ifnull(lc.likeCount, 0) as heartsCount,
	        ifnull(cc.commentCount,0) as commentCount,
                ifnull(bc.bookmarkCount, 0) as bookmarkCount,
	    ifnull(likeCount, 0) + ifnull(commentCount, 0) + ifnull(bookmarkCount, 0) as totalScore
            from post p
            join user u on p.userId = u.userId
            left join likeCounts lc on p.postId = lc.postId
	    left join commentCounts cc on p.postId = cc.postId
            left join bookmarkCounts bc on p.postId = bc.postId
            order by totalScore desc
            limit 10;
             """;
        
        try(Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(trendingSql)) { //passes sql query
                stmt.setString(1, logged_in_userId);
                stmt.setString(2, logged_in_userId);
                System.out.println("im somewhere");

                try(ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        System.out.println("im somewhere inside while");

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
                        posts.add(postService.helpPost(rs, heartsCount, commentCount, isHearted, isBookmarked)); // isHearted = true, isBookmarked = true
                    }
                }
            }
        return posts;
    }
        

}
