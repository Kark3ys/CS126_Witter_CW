/**
 * Your preamble here
 *
 * @author: Your university ID
 */

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.Weet;
import uk.ac.warwick.java.cs126.models.User;

import java.util.Date;


public class FollowerStore implements IFollowerStore {

    public FollowerStore() {
    }

    public boolean addFollower(int uid1, int uid2, Date followDate) {
        // TODO 
        return false;
    }  

    public int[] getFollowers(int uid) {
        // TODO 
        return null;
    }

    public int[] getFollows(int uid) {
        // TODO 
        return null;
    }

    public boolean isAFollower(int uidFollower, int uidFollows) {
        // TODO 
        return false;
    }

    public int getNumFollowers(int uid) {
        // TODO 
        return -1;
    }

    public int[] getMutualFollowers(int uid1, int uid2) {
        // TODO 
        return null;
    }

    public int[] getMutualFollows(int uid1, int uid2) {
        // TODO 
        return null;
    }

    public int[] getTopUsers() {
        // TODO
        return null;
    }

}
