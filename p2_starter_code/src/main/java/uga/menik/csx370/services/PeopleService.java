/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import uga.menik.csx370.models.FollowableUser;

/**
 * This service contains people related functions.
 */
@Service
public class PeopleService {

    private final DataSource dataSource;

    public PeopleService(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * This function should query and return all users that 
     * are followable. The list should not contain the user 
     * with id userIdToExclude.
     */
    public List<FollowableUser> getFollowableUsers(String userIdToExclude) {
        // Write an SQL query to find the users that are not the current user.
        List<FollowableUser> followableUsers = new ArrayList<>();

        // get the users that we do follow - sql query 
        final String doesfollowSql = "SELECT u.userId, u.firstName, u.lastName, " +
	"COALESCE ((SELECT MAX(p.postDate) FROM post p WHERE p.userId = u.userId), '') AS lastActiveDate " +
        "FROM user u join follow f " +
        "on u.userId = f.followeeId " + 
        "WHERE f.followeeId <> ? and f.followerId = ?"; // the logged in user is always the follower and never the followee
        
        
        // how to create a list of FollowableUsers
        // Run the query with a datasource.        
        // Inject DataSource instance and use it to run a query.
        try (Connection conn = dataSource.getConnection();
            PreparedStatement followableStmt = conn.prepareStatement(doesfollowSql)) { //passes sql queary

                followableStmt.setString(1, userIdToExclude);
                followableStmt.setString(2, userIdToExclude);
                try (ResultSet rs = followableStmt.executeQuery()) {
                    // Traverse the result rows one at a time.
                    // Note: This specific while loop will only run at most once 
                    // since ID is unique
                    while (rs.next()) {

			// String last_active_field = rs.getString("lastActiveDate");
            Timestamp last_active_timestamp = rs.getTimestamp("lastActiveDate");
            // convert to Eastern time
            LocalDateTime correctedEastern = last_active_timestamp.toLocalDateTime().minusHours(4);
			if (correctedEastern == null || correctedEastern.trim().isEmpty()) {
			    correctedEastern = "Never";
			}			    
                        // Note: rs.get.. functions access attributes of the current row.
                        // Access rows and their attributes
                        // from the query result.
                        followableUsers.add(new FollowableUser (
                            rs.getString("userId"),
                            rs.getString("firstName"),
                            rs.getString("lastName"),
                            true,
                            correctedEastern
                        ));
                    } //while
                } //try
            } catch (SQLException e) {
                System.out.println(e);
            } //try-catch

        // users that we dont follow
        final String doesNotfollowSql = "SELECT u.userId, u.firstName, u.lastName, " +
	"COALESCE ((SELECT MAX(p.postDate) FROM post p WHERE p.userId = u.userId), '') AS lastActiveDate " + 
        "FROM user u " +
        "WHERE u.userId NOT IN ( " +
        "SELECT f.followeeId FROM follow f WHERE f.followerId = ?) " +
        "and u.userId <> ? "; // the logged in user is always the follower and never the followee

        try (Connection conn = dataSource.getConnection();
        PreparedStatement notFollowableStmt = conn.prepareStatement(doesNotfollowSql)) { //passes sql queary

            notFollowableStmt.setString(1, userIdToExclude);
            notFollowableStmt.setString(2, userIdToExclude);
            try (ResultSet rs = notFollowableStmt.executeQuery()) {
                // Traverse the result rows one at a time.
                // Note: This specific while loop will only run at most once 
                // since ID is unique
                while (rs.next()) {
                    // Note: rs.get.. functions access attributes of the current row.
                    // Access rows and their attributes
                    // from the query result.
		    String last_active_field = rs.getString("lastActiveDate");
		    if (last_active_field == null || last_active_field.trim().isEmpty()) {
			last_active_field = "Never";
		    }
			
                    followableUsers.add(new FollowableUser (
                        rs.getString("userId"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        false,
                        last_active_field
                    ));
                } //while
            } //try
        } catch (SQLException e) {
            System.out.println(e);
        }
        // return the list you created.
        return followableUsers; 
    } //getFollowableUsers


    public boolean followUser(String followerId, String followeeId) {
        final String sql = "INSERT INTO follow (followerId, followeeId) VALUES (?, ?)";

        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, followerId);
            pstmt.setString(2, followeeId);

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }
    }

    public boolean unfollowUser(String followerId, String followeeId) {
        final String sql = "DELETE FROM follow WHERE followerId = ? AND followeeId = ?";

        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, followerId);
            pstmt.setString(2, followeeId);

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }
    }


}
