-- Purpose: Based on a given postId, figure out authorID (user who created the post)
-- Context: in BookmarksService.java 
-- URL: http://localhost:8080/bookmarks/add  

final String getAuthor = "select p.userId from post p where p.postId = ?";


-- Purpose: Add a bookmark record linking the user to the selected post
-- Context: in BookmarksService.java 
-- URL: http://localhost:8080/bookmarks/add

final String postSql = "insert into bookmark (userId, postId, authorId) values (?, ?, ?)";


-- Purpose: Post is unbookmarked
-- Context: in BookmarksService.java 
-- URL: http://localhost:8080/bookmarks/remove

final String removeSql = "delete from bookmark where userId = ? and postId = ? and authorId = ?";


-- Purpose: get all posts bookmarked by user, including like count, comment count, and author info
-- Context: in BookmarksService.java 
-- URL: http://localhost:8080/bookmarks

final String getBookMarkedSql = 
"select count(distinct pl.userId) as heartsCount, p.postId, b.authorId as userId, p.content, p.postDate, u.firstName, u.lastName, " +
    "count(distinct c.commentId) as commentCount, " + 
    "exists (select 1 from post_like pl2 where pl2.postId = p.postId and pl2.userId = ?) as isLiked " +
"from bookmark b " +
"join post p on p.postId = b.postId " +  
"join user u on u.userId = b.authorId " +
"left join post_like pl on pl.postId = p.postId " +
"left join comment c on c.postId = p.postId " +
"where b.userId = ? " +
"group by p.postId, b.authorId, p.postDate, u.firstName, u.lastName " +
"order by p.postDate desc";


-- purpose: find posts that include one or more hashtags typed in the search bar
-- context: in HashtagService.java 
-- url: http://localhost:8080/hashtagsearch

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



-- purpose: get all the users that a user follows
-- context: in PeopleService.java 
-- url: http://localhost:8080/people

final String doesfollowSql = "SELECT u.userId, u.firstName, u.lastName, " +
"(SELECT MAX(STR_TO_DATE(p.postDate, '%Y-%m-%d %H:%i:%s')) " +
"FROM post p WHERE p.userId = u.userId) AS lastActiveDate " +
"FROM user u join follow f " +
"on u.userId = f.followeeId " + 
"WHERE f.followeeId <> ? and f.followerId = ?";


-- purpose: get all users that the user does not follow right now
-- context: in PeopleService.java 
-- url: http://localhost:8080/people

final String doesNotfollowSql = "SELECT u.userId, u.firstName, u.lastName, " +
"(SELECT MAX(STR_TO_DATE(p.postDate, '%Y-%m-%d %H:%i:%s')) " +
"FROM post p WHERE p.userId = u.userId) AS lastActiveDate " + 
"FROM user u " +
"WHERE u.userId NOT IN ( " +
"SELECT f.followeeId FROM follow f WHERE f.followerId = ?) " +
"and u.userId <> ?";


-- purpose: insert follower and followee information into follow table when following another user
-- context: in PeopleService.java 
-- url: http://localhost:8080/people/follow

final String sql = "INSERT INTO follow (followerId, followeeId) VALUES (?, ?)";


-- purpose: deleting the follower-followee record from follow tablw when unfollowing a user
-- context: in PeopleService.java 
-- url: http://localhost:8080/people/unfollow

final String sql = "DELETE FROM follow WHERE followerId = ? AND followeeId = ?";


-- purpose: create a new post with the user id, content, and the current timestamp
-- context: in PostService.java 
-- url: http://localhost:8080/post/add

final String postSql = "insert into post (userId, content, postDate) values (?, ?, NOW())";


-- purpose: get all posts for the logged-in user's feed including likes, comments, and whether they bookmarked or liked it
-- context: in PostService.java 
-- url: http://localhost:8080/home

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

-- purpose: check if a post is bookmarked by the logged-in user
-- context: in PostService.java 
-- url: http://localhost:8080/post/{postId}

final String findIfBookmarked = "SELECT EXISTS (SELECT 1 FROM bookmark b WHERE b.postId = ? AND b.userId = ?)";


-- purpose: check if a post is liked by the logged-in user
-- context: in PostService.java 
-- url: http://localhost:8080/post/{postId}

final String findIfLiked = "SELECT EXISTS (SELECT 1 FROM post_like l WHERE l.postId = ? AND l.userId = ?)";


-- purpose: get a single post by postId including the number of likes and comments
-- context: in PostService.java 
-- url: http://localhost:8080/post/{postId}

final String getPostSql = "SELECT COUNT(DISTINCT pl.userId) as heartsCount, p.postId, p.content, p.postDate, u.userId, u.firstName, u.lastName, " +
                                            "COUNT(DISTINCT c.commentId) as commentCount " +
                                    "FROM post p " + 
                                    "JOIN user u on p.userId = u.userId " +
                                    "LEFT JOIN post_like pl ON pl.postId = p.postId " +
                                    "LEFT JOIN comment c ON c.postId = p.postId " +
                                    "WHERE p.postId = ? " +
                                    "GROUP BY p.postId, p.content, p.postDate, u.userId, u.firstName, u.lastName";


-- purpose: get all posts made by a specific user including whether the logged-in user liked or bookmarked them
-- context: in PostService.java 
-- url: http://localhost:8080/profile/{userId}

final String getPostSql = "WITH userBookmarked AS ( SELECT postID " +	
"FROM bookmark " +  
"WHERE userID = ? " + 
"), " +
"userHearted AS ( SELECT postID " +	
"FROM post_like " +
"WHERE userID = ?" + 
") " +
"SELECT p.postID, p.content, p.postDate, " +
"u.userID, u.firstName, u.lastName, " +
"(SELECT ub.postID " +
"FROM userBookmarked AS ub " +
"WHERE ub.postID = p.postID) " +
"AS userBookmarkedPost, " +
"(SELECT uh.postID " +
"FROM userHearted AS uh " +
"WHERE uh.postID = p.postID) " +
"AS userHeartedPost, " +
"(SELECT COUNT(*) " +
"FROM post_like AS pl " + 
"WHERE pl.postID = p.postID)" +
"AS heartsCount, " +
"(SELECT COUNT(*) " +
"FROM comment as c " +
"WHERE c.postId = p.postId) " +
"AS commentCount " +
"FROM post AS p, user AS u " +
"WHERE p.userID = u.userID " + 
"AND p.userID = ? " +
"ORDER BY p.postDate DESC;";


-- purpose: add a like to a post by inserting a record into the post_like table
-- context: in PostService.java 
-- url: http://localhost:8080/post/like/{postId}

String sql = "insert ignore into post_like (userId, postId) values (?, ?)";


-- purpose: remove a like from a post by deleting the record from the post_like table
-- context: in PostService.java 
-- url: http://localhost:8080/post/unlike/{postId}

String sql = "delete from post_like where userId = ? and postId = ?";


-- purpose: create a new comment under a post with the current timestamp
-- context: in PostService.java 
-- url: http://localhost:8080/post/comment/{postId}

String commentSql = "insert into comment (commenterId, postId, content, commentDate) values (?, ?, ?, NOW())";


-- purpose: get all comments for a given post including commenter information and comment date
-- context: in PostService.java 
-- url: http://localhost:8080/post/{postId}/comments

final String commentSql = "Select c.commentId, c.content, c.commentDate, u.userId, u.firstName, u.lastName " +
"From comment c Join user u ON c.commenterId = u.userId Where c.postId = ? order by c.commentDate desc";
