/*
Adam Dodson u1600262
Last Modified: 01:16 03/02/2017
Preambly Bit:
Implementation of UserStore via a hashtable where each element in the hashtable
is a singularly linked list (i.e. a closed bucket hash table). 
The keys being the user ID rather than the user object as a whole. 
I decided to make it this way due to:
a) getUser takes an ID as the parameter, so mapping to the hashtable makes 
the time complexity O(1) in best case, however it's O(n) worst if all the ids 
have the same hashcode.
b) Allows a base line of error handling if a dupplicate ID is attempted to be 
inserted into the table. If the keys were objects, those two users would 
be different objects with the same ID, and would be inserted into the table.
*/

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.User;
import java.util.Date;

/*
class User {
	//Class for storing user data.
	private int id;
	private String name;
	private Date date;
	
	public User(String name, int id, Date date) {
		this.id = id;
		this.name = name;
		this.date = date;
	}
	
	public int getId() {
		return this.id;
	}
	
	public Date getDateJoined() {
		return this.date;
	}
	
	public String getPrettyDateJoined() {
		return this.date.toString();
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getFirstName() {
		String[] temp = this.name.split(" ");
		//Split takes the regular expression provided, splitting the string into
		//an array at every occurance and not including it.
		//Similar to spliting the values in a csv when opened in something like excel.
		return temp[0];
	}
}
*/
class UserAndPoint {
	//Used to keep track of the users in a hashtable.
	//Only if there is a collision, but better safe than sorry.
	//Seeing as I'm using the user ids for the keys in the hash table, it makes
	//sense to make the comparison between the ids, keeps things easy.
	//Especially when having to add/get users as it's assumed that ids are
	//unique.
	private User current;
	private UserAndPoint next;
	
	public UserAndPoint(User usr) {
		//When we are provided a new user, we need to point to the provided user, 
		//then set the next pointer to null.
		current = usr;
		next = null;		
	}
	
	public void gottaPointFast(UserAndPoint youreTooSlow) {
		//Set this current UserAndPoint to point at the provided User, only used
		//in case of collisions.
		next = youreTooSlow;
	}
	
	public User getCurrent() {
		return this.current;
	}
	
	public UserAndPoint getNext() {
		return this.next;
	}
}

public class UserStore implements IUserStore {
	//Implement UserStore as a hashtable based on User.id.
	//Hash map will have a capacity of 100000.
	//Hash function is simply X mod 100000.
		private int capacity;
		private UserAndPoint[] hashtable;
		private int userCount;
		
    public UserStore() {
			capacity = 100000;
			hashtable = new UserAndPoint[capacity];
			userCount = 0;
			//Array is initialized at null.
    }

    public boolean addUser(User usr) {
			//Time Complexity
				//Best Case O(1) - Hashcode hasn't been used.
				//Worst Case O(n) - Every id has the same hashcode.
			//Space Complexity O(1)
			int uid = usr.getId();
			if (getUser(uid) != null) return false;	//Do a check to see if usr in.
			UserAndPoint temp = hashtable[hashFunction(uid)];
			UserAndPoint prev = null;
			//Set up some pointers.
			while (temp != null) {
				prev = temp;
				temp = temp.getNext();
			}
			//If the first try wasn't null, keep cycling until it is.
			if (prev == null) {
				hashtable[hashFunction(uid)] = new UserAndPoint(usr);
				//If this is the first value with this hashcode, point directly from
				//array.
			} else {
				prev.gottaPointFast(new UserAndPoint(usr));
				//If there is more than one user with this hashcode, have the previous 
				//one point to this one.
			}
			
			this.userCount++;	//Increment users.
			return true;
    }

    public User getUser(int uid) {
			//Time Complexity
				//Best Case O(1) - Hashcode used once/is unused.
				//Worst Case O(n) - Every id has the same hashcode.
			//Space Complexity O(1)
			UserAndPoint check = hashtable[hashFunction(uid)];
			while (check != null) {
				if (check.getCurrent().getId() == uid) return check.getCurrent();
				check = check.getNext();
			}
			return null;
    }

    public User[] getUsers() {
			//Time Complexity
				//Best Case O(n) - No more than one ID to every hashcode.
				//Worst Case O(n^2) - Every id has the same hashcode=capcity-1.
			//Space Complexity O(n)
			if (userCount == 0) return null;
			User[] retArray = new User[userCount];
			UserAndPoint temp;
			int j = 0; //Used for end pointer in retArray
			for (int i = 0; i < capacity; i++) {
				if (hashtable[i] != null) {
					temp = hashtable[i];
					while (temp != null) {
						retArray[j] = temp.getCurrent();
						j++;
						temp = temp.getNext();
					}
				}
			}
			//Sort by date joined and return the array.
			return sortByDJ(retArray);
    }

    public User[] getUsersContaining(String query) {
			//Time Complexity O(n)
			//Space Complexity O(n)
			User[] arrAllUsers = getUsers();
			User[] retArray;
			if (arrAllUsers == null) return null;
			boolean[] arrMatch = new boolean[arrAllUsers.length]; 
			//Indexes match with the user indexes in arrAllUsers, so if true in this
			//array, then it means that user matches the string check.
			//This round about way is required so we can find the length of the 
			//return value array, then actually assign the object references without
			//having to do another set of string checks.
			int count = 0; //Counter for the number of matches found.
			int i = 0;
			for (i = 0; i < arrMatch.length; i++) {
				arrMatch[i] = arrAllUsers[i].getName().toLowerCase().contains(query.toLowerCase());
				//Compare the lower case versions of the name and query. If query is
				//or is part of the current user name, then that index has it's value 
				//set to true.
				count++;
			}
			if (count == 0) return null;
			retArray = new User[count];
			int j = 0; //Iterator through retArray.
			for (i = 0; i < arrMatch.length; i++)
				if (arrMatch[i]) {
					retArray[j] = arrAllUsers[i];
					j++;
				}
			//No need to resort the array as we've kept the users in date order, just
			//shuffled up to fill the gaps.
			return retArray;
    }

    public User[] getUsersJoinedBefore(Date dateBefore) {
			User[] arrAllUsers = getUsers();
			if (arrAllUsers == null) return null;
			int count = 0;
			while (arrAllUsers[count].getDateJoined().compareTo(dateBefore) < 0) count++;
			//Keep looking through the array until we find the date joined which is
			//equal to or later than the date provided.
			if (count == 0) return null;
			User[] retArray = new User[count];
			for (int i = 0; i < count; i++) retArray[i] = arrAllUsers[i];
			//We simply want to truncate the array of the latter parts which contains
			//users registered after or on the specified date.
			//As a result sorting isn't needed.
			return retArray;
    }
		
		private int hashFunction(int uid) {
			return uid % this.capacity;
		}
		
		private User[] sortByDJ(User[] arrIn) {
			//Merge sort is implemented here with fixed length arrays.
			//Used arrays because I really like using arrays from working with
			//Pascal/Delphi. Making a linked list implemented merge sort would've
			//saved space, but computers and servers have more than enough memory.
			//The provided parameter is an array anyways, it would require some effort
			//just to convert that array into a linked list, then convert the linked 
			//list back into an array. The other two sorts I was considering, heap and
			//quicksort, kinda confuse me, so I picked this one instead.
			//Time Complexity O(n log(n)) 
			//https://www.khanacademy.org/computing/computer-science/algorithms/merge-sort/a/analysis-of-merge-sort
			//Space Complexity O(n)
			int length = arrIn.length;	//Find array length for reference.
			if (length == 1) return arrIn;	//No need to split
			User[] arrLeft = new User[length/2];
			User[] arrRight = new User[length - length/2];
			//Get the left and right side of the array, on odd numbers,
			//arrLeft.length + 1 == arrRight.length.
			int i = 0;
			for (i = 0; i < length/2 - 1; i++) arrLeft[i] = arrIn[i];
			for (i = length/2; i < length - 1; i++) arrRight[i] = arrIn[i];
			//Set up the left and right arrays to point at the correct users on the 
			//left and right of the input array.
			
			arrLeft = sortByDJ(arrLeft);
			arrRight = sortByDJ(arrRight);
			//Recursion here
			
			//Time to do some merging.
			User[] retArray = new User[length];
			i = 0; //Loop counter for left array.
			int j = 0; //Loop counter for right array.
			int k = 0; //Loop counter for return array.
			while (i < arrLeft.length && j < arrRight.length) {
				if(arrLeft[i].getDateJoined().compareTo(arrRight[j].getDateJoined()) < 0) {
					retArray[k] = arrLeft[i];
					i++;
				} else {
					retArray[k] = arrRight[j];
					j++;
				}
				k++;
			}
			//At the end of this loop, either the left or right array will have a 
			//single element remaining.
			if (i < arrLeft.length) retArray[k] = arrLeft[i];
			else retArray[k] = arrRight[j];
			arrLeft = arrRight = null; //Dereference our l/r arrays.
			return retArray;
			//Done with the merging, let's bring it back up.
		}
}
