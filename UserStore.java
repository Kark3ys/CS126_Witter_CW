/**
 * Your preamble here
 *
 * @author: Your university ID
 */

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.User;

import java.util.Date;


public class UserStore implements IUserStore {

    public UserStore() {
    }

    public boolean addUser(User usr) {
        // TODO 
        return false;
    }

    public User getUser(int uid) {
        // TODO 
        return null;
    }

    public User[] getUsers() {
        // TODO 
        return null;
    }

    public User[] getUsersContaining(String query) {
        // TODO
        return null;
    }

    public User[] getUsersJoinedBefore(Date dateBefore) {
        // TODO 
        return null;
    }
    
}
