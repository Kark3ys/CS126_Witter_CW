/*
Adam Dodson u1600262
Preambly Bit:
So, relational data between two entities whether or not there is a connection.
Sounds like a directed graph with loads of nodes.
Adjacency Matrix representation is probably a good call.
But then making an adjacency matrix of n users will result in space complexity
of O(n^2). Not such a good call.
Solution, make it a hash function to access a smaller Adjacency Matrix Hashtable
weird hybrid. Could be collisions, so each nested hashtable reference needs to
have a closed bucket solution so we can check we've got the correct relation.
In the end, it's another Closed Bucket Hashtable yay!
Or Rather a Closed Bucket Hashtable^2.
In actual fact this is basically an Adjacency list where you hash to find the
starting node, then hash again to find the target node, check through the
bucket found to find the right node references (uid) then find the boolean value
stored there. As a result, we don't really need a boolean value, just the object
existing in the first place is enough to confirm that the relation exists.
Theoretically quicker access times, with (slightly) less overhead in storage space
required.
Instead the storage space for large number of users compared to the size of the 
adjacency hash matrix (
*/

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.Weet;
import uk.ac.warwick.java.cs126.models.User;

import java.util.Date;


class FollowRelation {
	//Class literally stores two uids.
	//If it exists and passes through checks, then uidF(ollow) follows uidL(ead)
	//Also much more clear making it Follow and Lead for describe the relation
	//In code.
	private int uidL;
	private int uidF;
	
	public FollowRelation(int uidL, int uidF) {
		this.uidL = uidL;
		this.uidF = uidF;
	}
	
	public boolean isOurs(int uidL, int uidF) {
		//Check that the uids provided match.
		return (this.uidL == uidL && this.uidF == uidF);
	}
}

class FollowAndPoint {
	//Used in FollowLinkList where the list is simply comprised of follows.
	//The FollowRelation is used as the check.
	private FollowRelation fr;
	private FollowAndPoint next;
	
	public FollowLink 
}

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
