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
        final String followableSql = "SELECT userID, firstName, lastName FROM user WHERE userId <> ?"; //creates sql query 
        // how to create a list of FollowableUsers
        List<FollowableUser> followableUsers = new ArrayList<>();
        // Run the query with a datasource.        
        // Inject DataSource instance and use it to run a query.
        try (Connection conn = dataSource.getConnection();
            PreparedStatement followableStmt = conn.prepareStatement(followableSql)) { //passes sql queary

                followableStmt.setString(1, userIdToExclude);
                try (ResultSet rs = followableStmt.executeQuery()) {
                    
                    while (rs.next()) {
                        
                        followableUsers.add(new FollowableUser (
                            rs.getString("userId"),
                            rs.getString("firstName"),
                            rs.getString("lastName"),
                            false,
                            ""
                        ));
                    } //while
                } //try
            } catch (SQLException e) {
                System.out.println(e);
            } //try-catch
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
