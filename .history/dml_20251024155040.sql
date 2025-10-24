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


-- purpose: get all users that the user does not  follow
-- context: in PeopleService.java 
-- url: http://localhost:8080/people

final String doesNotfollowSql = "SELECT u.userId, u.firstName, u.lastName, " +
"(SELECT MAX(STR_TO_DATE(p.postDate, '%Y-%m-%d %H:%i:%s')) " +
"FROM post p WHERE p.userId = u.userId) AS lastActiveDate " + 
"FROM user u " +
"WHERE u.userId NOT IN ( " +
"SELECT f.followeeId FROM follow f WHERE f.followerId = ?) " +
"and u.userId <> ?";


-- purpose: follow another user by adding a new record linking follower and followee
-- context: in PeopleService.java 
-- url: http://localhost:8080/people/follow

final String sql = "INSERT INTO follow (followerId, followeeId) VALUES (?, ?)";


-- purpose: unfollow a user by removing the follower-followee record
-- context: in PeopleService.java 
-- url: http://localhost:8080/people/unfollow

final String sql = "DELETE FROM follow WHERE followerId = ? AND followeeId = ?";
