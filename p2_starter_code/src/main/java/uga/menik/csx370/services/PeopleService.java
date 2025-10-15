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

        // Run the query with a datasource.
        // See UserService.java to see how to inject DataSource instance and
        // use it to run a query.

        // Use the query result to create a list of followable users.
        // See UserService.java to see how to access rows and their attributes
        // from the query result.
        // Check the following createSampleFollowableUserList function to see 
        // how to create a list of FollowableUsers.

        // Replace the following line and return the list you created.
        final String followableSql = "SELECT userID, firstName, lastName FROM user WHERE userId <> ?"; //creates sql query 
        List<FollowableUser> followableUsers = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
            PreparedStatement followableStmt = conn.prepareStatement(followableSql)) { //passes sql queary

                followableStmt.setString(1, userIdToExclude);
                try (ResultSet rs = followableStmt.executeQuery()) {
                    // Traverse the result rows one at a time.
                    // Note: This specific while loop will only run at most once 
                    // since ID is unique
                    while (rs.next()) {
                    // Note: rs.get.. functions access attributes of the current row.
                        followableUsers.add(new FollowableUser (
                            rs.getString("userId"),
                            rs.getString("firstName"),
                            rs.getString("lastName"),
                            false,
                            ""
                        ));
                    }
                }
            } catch (SQLException e) {
                System.out.println(e);
            }

        return followableUsers;
    }

}
