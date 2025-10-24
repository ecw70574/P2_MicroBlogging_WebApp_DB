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

select count(distinct pl.userId) as heartsCount, p.postId, b.authorId as userId, p.content, p.postDate, u.firstName, u.lastName, " +
    "count(distinct c.commentId) as commentCount, " + 
    "exists (select 1 from post_like pl2 where pl2.postId = p.postId and pl2.userId = ?) as isLiked " +
"from bookmark b " +
"join post p on p.postId = b.postId " +  
"join user u on u.userId = b.authorId " +
"left join post_like pl on pl.postId = p.postId " +
"left join comment c on c.postId = p.postId " +
"where b.userId = ? " +
"group by p.postId, b.authorId, p.postDate, u.firstName, u.lastName " +
"order by p.postDate desc;


-- purpose: find posts that include one or more hashtags typed in the search bar
-- context: in HashtagService.java 
-- url: http://localhost:8080/hashtagsearch
with userBookmarked AS ( 
    select postId	
    from bookmark  
    where userId = ?  
), 
userHearted AS ( 
    select postId	
    from post_like 
    where userId = ?
) 
select p.postId, p.content, p.postDate, 
       u.userID, u.firstName, u.lastName, 
       (select ub.postID 
        from userBookmarked AS ub 
        where ub.postId = p.postId) 
        as userBookmarkedPost, 
       (select uh.postId 
        from userHearted AS uh 
        where uh.postId = p.postId) 
        as userHeartedPost, 
       (select count(*) 
        from post_like AS pl 
        where pl.postId = p.postId) 
        as heartsCount, 
       (select count(*) 
        from comment as c 
        where c.postId = p.postId) 
        as commentCount 
from post AS p, user AS u 
where p.userId = u.userId and (
    p.content regexp ?
) 
order by p.postDate desc;
