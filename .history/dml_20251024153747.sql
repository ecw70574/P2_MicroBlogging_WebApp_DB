-- PURPOSE: Retrieve the authorId (user who created the post) based on a given postId.
-- CONTEXT: Used in BookmarksService.java → addBookmark() and removeBookmark()
-- URL: http://localhost:8080/bookmarks/add  (and similar remove path)

final String getAuthor = "select p.userId from post p where p.postId = ?";


-- PURPOSE: Add a bookmark record linking the logged-in user to the selected post.
-- CONTEXT: Used in BookmarksService.java → addBookmark()
-- URL: http://localhost:8080/bookmarks/add

final String postSql = "insert into bookmark (userId, postId, authorId) values (?, ?, ?)";


-- Purpose: Remove an existing bookmark (unbookmark a post).
-- CONTEXT: Used in BookmarksService.java → removeBookmark()
-- URL: http://localhost:8080/bookmarks/remove

final String removeSql = "delete from bookmark where userId = ? and postId = ? and authorId = ?";


-- PURPOSE: Retrieve all posts bookmarked by the logged-in user, including like count, comment count, and author info.
-- Context: Used in BookmarksService.java → getBookMarked()
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
