/*
Adam Dodson u1600262
Repo: https://github.com/Kark3ys/CS126_Witter_CW
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
adjacency hash matrix (n >> c^2 where c is frCap defined below) tends to O(n)

Oh yeah, and tracking the number of followers someone has will be in another 
hashtable for user ids (sounds very familiar, but I'll keep it separate for now).
Literal carbon copy of how I handled trends, but simlpicity comes above space efficiency.
*/

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.Weet;
import uk.ac.warwick.java.cs126.models.User;

import java.util.Date;


class FollowRelation implements Comparable<FollowRelation> {
	//Class literally stores two uids and the Date followed.
	//If it exists and passes through checks, then uidF(ollow) follows uidL(ead)
	//Also much more clear making it Follow and Lead for describe the relation
	//In code.
	private int uidL;
	private int uidF;
	private Date date;
	
	public FollowRelation(int uidL, int uidF, Date date) {
		this.uidL = uidL;
		this.uidF = uidF;
		this.date = date;
	}
	
	public boolean equals(int uidL, int uidF) {
		//Check that the uids provided match.
		return (this.uidL == uidL && this.uidF == uidF);
	}
	
	public boolean equals(FollowRelation fr) {
		return equals(fr.getL(), fr.getF());
	}
	
	public int compareTo(FollowRelation fr) {
		return this.date.compareTo(fr.getDate());
	}
	
	public int getL() {
		return this.uidL;
	}
	
	public int getF() {
		return this.uidF;
	}
	
	public Date getDate() {
		return this.date;
	}
}

class FollowAndPoint extends ItemAndPoint<FollowRelation> {
	public FollowAndPoint(FollowRelation fr) {
		super(fr);
	}
}
/*
class FollowAndPoint {
	//Used in FollowLinkList where the list is simply comprised of follows.
	//The FollowRelation is used as the check.
	private FollowRelation current;
	private FollowAndPoint next;
	
	public FollowAndPoint(FollowRelation fr) {
		current = fr;
		next = null;
	}
	
	public void gottaPointFast(FollowAndPoint youreTooSlow) {
		next = youreTooSlow;
	}
	
	public FollowRelation getCurrent() {
		return current;
	}
	
	public FollowAndPoint getNext() {
		return next;
	}
}*/

class FollowCounter implements Comparable<FollowCounter> {
	//Stores user ID and count of followers, for use getFollows and getTopUsers
	private int uid;
	private int count;
	
	public FollowCounter (int uid) {
		this.uid = uid;
		this.count = 1;
	}
	
	public void add() {
		this.count++;
	}
	
	public boolean equals(int uid) {
		return this.uid == uid;
	}
	
	public boolean equals(FollowCounter fc) {
		return this.uid == fc.getUID();
	}
	
	public int compareTo(FollowCounter fc) {
		return this.count - fc.getCount();
	}
	
	public int getUID() {
		return this.uid;
	}
	
	public int getCount() {
		return this.count;
	}
}

class FCAndPoint extends ItemAndPoint<FollowCounter> {
	public FCAndPoint(FollowCounter fc) {
		super(fc);
	}
}
/*
class FCAndPoint {
	private FollowCounter current;
	private FCAndPoint next;
	
	public FCAndPoint(FollowCounter fc) {
		current = fc;
		next = null;
	}
	
	public void gottaPointFast(FCAndPoint youreTooSlow) {
		next = youreTooSlow;
	}
	
	public FollowCounter getCurrent() {
		return current;
	}
	
	public FCAndPoint getNext() {
		return next;
	}
}*/

class SortByLead extends MergeSort {
	int compFunc(Object a, Object b) {
		return ((FollowRelation) b).getL() - ((FollowRelation) a).getL();
	}
}

class SortByFollow extends MergeSort {
	int compFunc(Object a, Object b) {
		return ((FollowRelation) b).getF() - ((FollowRelation) a).getF();
	}
}

class SortFRByDate extends MergeSort {
	int compFunc(Object a, Object b) {
		return ((FollowRelation) a).compareTo((FollowRelation) b);
	}
}

class SortByFCount extends MergeSort {
	int compFunc(Object a, Object b) {
		return ((FollowCounter) a).compareTo((FollowCounter) b);
	}
}

public class FollowerStore implements IFollowerStore {
	
	private int frCap;
	private FollowAndPoint[][] frHashtable;
	private int followCount;
	private int fcCap;
	private FCAndPoint[] fcHashtable;
	private int FCObjCount;
	private SortByLead sorterL;
	private SortByFollow sorterF;
	private SortFRByDate sorterD;
	private SortByFCount sorterFC;

	public FollowerStore() {
		frCap = fcCap = 10000;
		frHashtable = new FollowAndPoint[frCap][frCap];
		fcHashtable = new FCAndPoint[fcCap];
		followCount = FCObjCount = 0;
		sorterL = new SortByLead();
		sorterF = new SortByFollow();
		sorterD = new SortFRByDate();
		sorterFC = new SortByFCount();
	}

	public boolean addFollower(int uid1, int uid2, Date followDate) {
		int uidL = uid2;
		int uidF = uid1;
		//Get rid of the god awful parameter names.
		boolean result = addFollowRelation(uidF, uidL, followDate);
		if (result) addFollowerTally(uidL);
		return result;
	}  
	
	private boolean addFollowRelation(int uidF, int uidL, Date followDate) {
		if (isAFollower(uidF, uidL)) return false; //No need to do jack if already following.
		FollowRelation newFR = new FollowRelation(uidL, uidF, followDate);
		FollowAndPoint temp = frHashtable[frHFunc(uidL)][frHFunc(uidF)];
		FollowAndPoint prev = null;
		
		while (temp != null) {
			prev = temp;
			temp = (FollowAndPoint) temp.getNext();
		}
		
		if (prev == null) {
			frHashtable[frHFunc(uidL)][frHFunc(uidF)] = new FollowAndPoint(newFR);
		} else {
			prev.gottaPointFast(new FollowAndPoint(newFR));
		}
		prev = temp = null;
		this.followCount++;
		return true;
	}
	
	private boolean addFollowerTally(int uidL) {
		if (followCount == 0) return false;
		FCAndPoint temp = fcHashtable[fcHFunc(uidL)];
		FCAndPoint prev = null;
		boolean match = false;
		
		while (!match && temp != null) {
			match = temp.getCurrent().equals(uidL);
			prev = temp;
			temp = (FCAndPoint) temp.getNext();
		}
		
		if (prev == null) {
			fcHashtable[fcHFunc(uidL)] = new FCAndPoint(new FollowCounter(uidL));
		} else if (match) {
			prev.getCurrent().add();
		} else {
			prev.gottaPointFast(new FCAndPoint(new FollowCounter(uidL)));
		}
		
		if (!match) this.FCObjCount++;
		prev = temp = null;
		return true;
	}
	
	public int[] getFollowers(int uid) {
		if (this.followCount == 0) return null;
		FollowRelation[] arrFR = getFollowersFR(uid);
		if (arrFR == null) return null;
		int[] retArray = new int[arrFR.length];
		for (int i = 0; i < arrFR.length; i++) {
			retArray[i] = arrFR[i].getF();
		}
		return retArray;
	}
	
	private FollowRelation[] getFollowersFR(int uid) {
		if (this.followCount == 0) return null;
		int uidL = uid;
		int i = 0; //Loop Counter
		int count = 0; //number of hits.
		FollowRelation[] arrFR = new FollowRelation[this.frCap];
		//Theoretically, everyone could be following this user.
		FollowAndPoint temp = null;
		for (i = 0; i < this.frCap; i++) {
			if(frHashtable[frHFunc(uidL)][i] != null) {
				temp  = frHashtable[frHFunc(uidL)][i];
				while (temp != null) {
					if (temp.getCurrent().getL() == uidL) {
						arrFR[count] = temp.getCurrent();
						count++;
						temp = (FollowAndPoint) temp.getNext();
					}
				}
			}
		}
		if (count == 0) return null;
		//At this stage, we'll have an array of FollowRelation up to count,
		//past count will just be null values.
		Object[] arrObj = new Object[arrFR.length];
		for (i = 0; i < arrObj.length; i++)
			arrObj[i] = (Object) arrFR[i];
		arrObj = sorterD.sort(arrObj, count);
		FollowRelation[] retArray = new FollowRelation[count];
		for (i = 0; i < retArray.length; i++)
			retArray[i] = (FollowRelation) arrObj[i];
		return retArray;
		//Slight difference to the usual merge sort where we will also pass the length
		//we wish to merge sort over. The normal merge sort would simply call this but
		//with the arrIn.length passed into the second parameter.
		//Now that everything is sorted by date, we can just extract the follower ids
		//and return that.
	}

	public int[] getFollows(int uid) {
		if (this.followCount == 0) return null;
		FollowRelation[] arrFR = getFollowsFR(uid);
		if (arrFR == null) return null;
		int[] retArray = new int[arrFR.length];
		for (int i = 0; i < arrFR.length; i++) {
			retArray[i] = arrFR[i].getL();
		}
		return retArray;
	}
	
	private FollowRelation[] getFollowsFR(int uid) {
		if (this.followCount == 0) return null;
		int uidF = uid;
		int i = 0; //Loop Counter
		int count = 0; //number of hits.
		FollowRelation[] arrFR = new FollowRelation[this.frCap];
		//Theoretically, everyone could be following this user.
		FollowAndPoint temp = null;
		for (i = 0; i < this.frCap; i++) {
			if(frHashtable[i][frHFunc(uidF)] != null) {
				temp  = frHashtable[i][frHFunc(uidF)];
				while (temp != null) {
					if (temp.getCurrent().getF() == uidF) {
						arrFR[count] = temp.getCurrent();
						count++;
						temp = (FollowAndPoint) temp.getNext();
					}
				}
			}
		}
		if (count == 0) return null;
		//At this stage, we'll have an array of FollowRelation up to count,
		//past count will just be null values.
		Object[] arrObj = new Object[arrFR.length];
		for (i = 0; i < arrObj.length; i++)
			arrObj[i] = (Object) arrFR[i];
		arrObj = sorterD.sort(arrObj, count);
		FollowRelation[] retArray = new FollowRelation[count];
		for (i = 0; i < retArray.length; i++)
			retArray[i] = (FollowRelation) arrObj[i];
		return retArray;
		//Slight difference to the usual merge sort where we will also pass the length
		//we wish to merge sort over. The normal merge sort would simply call this but
		//with the arrIn.length passed into the second parameter.
		//Now that everything is sorted by date, we can just extract the leader ids
		//and return that.
	}

	public boolean isAFollower(int uidFollower, int uidFollows) {
		if (this.followCount == 0) return false;
		int uidL = uidFollows;
		int uidF = uidFollower;
		FollowAndPoint temp = frHashtable[frHFunc(uidL)][frHFunc(uidF)];
		FollowAndPoint prev = null;
		boolean match = false;
		while (!match && temp != null) {
			match  = temp.getCurrent().equals(uidL, uidF);
			prev = temp;
			temp = (FollowAndPoint) temp.getNext();
		}
		return match;
	}

	public int getNumFollowers(int uid) {
		if (this.FCObjCount == 0) return 0;
		int uidL = uid;
		FCAndPoint temp = fcHashtable[fcHFunc(uidL)];
		FCAndPoint prev = null;
		boolean match = false;
		while (!match && temp != null) {
			match = temp.getCurrent().equals(uidL);
			prev = temp;
			temp = (FCAndPoint) temp.getNext();
		}
		if (!match) return 0;
		return prev.getCurrent().getCount();
	}

	public int[] getMutualFollowers(int uid1, int uid2) {
		int i = 0;
		if (this.followCount == 0) return null;
		FollowRelation[] set1 = getFollowersFR(uid1);
		if (set1 == null) return null;
		FollowRelation[] set2 = getFollowersFR(uid2);
		if (set2 == null) return null;
		
		Object[] arrObj = new Object[set1.length];
		for (i = 0; i < arrObj.length; i++) 
			arrObj[i] = (Object) set1[i];
		arrObj = sorterF.sort(arrObj);
		for (i = 0; i < arrObj.length; i++)
			set1[i] = (FollowRelation) arrObj[i];
		
		arrObj = new Object[set2.length];
		for (i = 0; i < arrObj.length; i++)
			arrObj[i] = (Object) set2[i];
		arrObj = sorterF.sort(arrObj);
		for (i = 0; i < arrObj.length; i++)
			set2[i] = (FollowRelation) arrObj[i];		
		
		FollowRelation[] tempSet = new FollowRelation[((set1.length > set2.length) ? set1.length : set2.length)];
		i = 0;
		int j = 0;
		int k = 0;
		int comp = 0;
		//Loop counters through set1, set2 and tempSet.
		//comp used to defactor calculate compareTo.
		while (i < set1.length && j < set2.length) {
			comp = set1[i].getF() - set2[j].getF();
			if (comp < 0) i++;
			else if (comp > 0) j++;
			else {
				tempSet[k] = (set1[i].getDate().compareTo(set2[j].getDate()) > 0) ? set1[i] : set2[j];
				k++;
				i++;
				j++;
			}
		}
		if (k == 0) return null;
		
		
		arrObj = new Object[tempSet.length];
		for (i = 0; i < arrObj.length; i++)
			arrObj[i] = (Object) tempSet[i];
		arrObj = sorterD.sort(arrObj, k);
		for (i = 0; i < arrObj.length; i++)
			tempSet[i] = (FollowRelation) arrObj[i];
		
		int[] retArray = new int[k];
		for (i = 0; i < k; i++)
			retArray[i] = tempSet[i].getF();
		return retArray;
	}

	public int[] getMutualFollows(int uid1, int uid2) {
		if (this.followCount == 0) return null;
		FollowRelation[] set1 = getFollowsFR(uid1);
		if (set1 == null) return null;
		FollowRelation[] set2 = getFollowsFR(uid2);
		if (set2 == null) return null;
		
		int i = 0;		
		Object[] arrObj = new Object[set1.length];
		for (i = 0; i < arrObj.length; i++) 
			arrObj[i] = (Object) set1[i];
		arrObj = sorterL.sort(arrObj);
		for (i = 0; i < arrObj.length; i++)
			set1[i] = (FollowRelation) arrObj[i];

		arrObj = new Object[set2.length];
		for (i = 0; i < arrObj.length; i++)
			arrObj[i] = (Object) set2[i];
		arrObj = sorterL.sort(arrObj);
		for (i = 0; i < arrObj.length; i++)
			set2[i] = (FollowRelation) arrObj[i];
		
		
		FollowRelation[] tempSet = new FollowRelation[((set1.length > set2.length) ? set1.length : set2.length)];
		i = 0;
		int j = 0;
		int k = 0;
		int comp = 0;
		//Loop counters through set1, set2 and tempSet.
		//comp used to defactor calculate compareTo.
		while (i < set1.length && j < set2.length) {
			comp = set1[i].getL() - set2[j].getL();
			if (comp < 0) i++;
			else if (comp > 0) j++;
			else {
				tempSet[k] = (set1[i].getDate().compareTo(set2[j].getDate()) > 0) ? set1[i] : set2[j];
				k++;
				i++;
				j++;
			}
		}
		if (k == 0) return null;
		
		arrObj = new Object[tempSet.length];
		for (i = 0; i < arrObj.length; i++)
			arrObj[i] = (Object) tempSet[i];
		arrObj = sorterD.sort(arrObj, k);
		for (i = 0; i < arrObj.length; i++)
			tempSet[i] = (FollowRelation) arrObj[i];
		
		int[] retArray = new int[k];
		for (i = 0; i < k; i++)
			retArray[i] = tempSet[i].getL();
		return retArray;
	}

	public int[] getTopUsers() {
		if (this.FCObjCount == 0) return null;
		FollowCounter[] arrFC = new FollowCounter[this.FCObjCount];
		FCAndPoint temp = null;
		int i = 0; //Loop Counter for fcHashtable
		int j = 0; //Loop counter for arrFC
		
		for (i = 0; i < this.fcCap; i++) {
			temp = fcHashtable[i];
			while (temp != null) {
				arrFC[j] = temp.getCurrent();
				j++;
				temp = (FCAndPoint) temp.getNext();
			}
		}
		
		Object[] arrObj = new Object[arrFC.length];
		for (i = 0; i < arrObj.length; i++)
			arrObj[i] = (Object) arrFC[i];
		arrObj = sorterFC.sort(arrObj);
		for (i = 0; i < arrObj.length; i++)
			arrFC[i] = (FollowCounter) arrObj[i];
		
		int[] retArray = new int[this.FCObjCount];
		for (i = 0; i < this.FCObjCount; i++)
			retArray[i] = arrFC[i].getUID();
				
		return retArray;
	}
		
	private int frHFunc(int uid) {
		return uid % this.frCap;
	}
	
	private int fcHFunc(int uid) {
		return uid % this.fcCap;
	}

}
