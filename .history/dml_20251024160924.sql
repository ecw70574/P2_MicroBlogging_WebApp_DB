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


-- purpose: get all posts a user including likes, comments, and whether they bookmarked or liked it
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

-- purpose: check if a post is bookmarked 
-- context: in PostService.java 
-- url: http://localhost:8080/post/{postId}

final String findIfBookmarked = "SELECT EXISTS (SELECT 1 FROM bookmark b WHERE b.postId = ? AND b.userId = ?)";


-- purpose: check if a post is liked 
-- context: in PostService.java 
-- url: http://localhost:8080/post/{postId}

final String findIfLiked = "SELECT EXISTS (SELECT 1 FROM post_like l WHERE l.postId = ? AND l.userId = ?)";


-- purpose: find a post by postId including the number of likes and comments
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


-- purpose: get all posts made by a specific user including whether the user liked or bookmarked them
-- context: in PostService.java 
-- url: http://localhost:8080/profile/{userId}

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


-- purpose: add a like to a post by inserting a record into the post_like table
-- context: in PostService.java 
-- url: http://localhost:8080/post/like/{postId}

String sql = "insert ignore into post_like (userId, postId) values (?, ?)";


-- purpose: remove a like from a post by deleting the record from the post_like table
-- context: in PostService.java 
-- url: http://localhost:8080/post/unlike/{postId}

String sql = "delete from post_like where userId = ? and postId = ?";


-- purpose: create comment under a post with the timestamp
-- context: in PostService.java 
-- url: http://localhost:8080/post/comment/{postId}

String commentSql = "insert into comment (commenterId, postId, content, commentDate) values (?, ?, ?, NOW())";


-- purpose: get all comments for a post including commenter information and comment date
-- context: in PostService.java 
-- url: http://localhost:8080/post/{postId}/comments

final String commentSql = "Select c.commentId, c.content, c.commentDate, u.userId, u.firstName, u.lastName " +
"From comment c Join user u ON c.commenterId = u.userId Where c.postId = ? order by c.commentDate desc";


-- purpose:  find the 10 most popular posts based on how many likes, comments, and bookmarks they have, showing the most active posts first
-- context: in TrendingService.java 
-- url: http://localhost:8080/trending

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
    order by totalScore desc, p.postDate desc
    limit 10;
""";


-- purpose: check if a username exists, so that we can then get the stored password to login 
-- context: in UserService.java 
-- url: http://localhost:8080/login

final String sql = "select * from user where username = ?";


-- purpose: add a new user to the database by adding username, eed password, first name, and last name
-- context: in UserService.java 
-- url: http://localhost:8080/register

final String registerSql = "insert into user (username, password, firstName, lastName) values (?, ?, ?, ?)";
