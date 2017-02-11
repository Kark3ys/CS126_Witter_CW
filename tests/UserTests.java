import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Random;


import uk.ac.warwick.java.cs126.services.IUserStore;
import uk.ac.warwick.java.cs126.services.UserStore;
import uk.ac.warwick.java.cs126.models.User;

public class UserTests {
    private Random random;
		//Used to generate random numbers.
		private UserStore users;
		private int randInt;
		
		public UserTests() {
			users = new UserStore();
			random = new Random();
			randInt = 0;
		}
		public int testAddUser() 
		{
			//Add a single user with random id numbers.
			//Returns the ID number used if successful, else return -1.
			randInt = random.nextInt(1000000);
			User temp = new User("Just A Dude" + randInt % 10,randInt, new Date((long) random.nextInt()));
			System.out.println(userToString(temp));
			return ((users.addUser(temp)) ? randInt : -1);
		}
		
		public boolean testAddWithID(int uid) {
			randInt = random.nextInt(1000000);
			User temp = new User("Just A Dude" + randInt % 10, uid, new Date((long) random.nextInt()));
			return users.addUser(temp);
		}
		
		public User testGetUser(int uid) {
			//Return null if user not found.
			return users.getUser(uid);
		}
		
		public User[] testGetUsers() {
			return users.getUsers();
		}
		
		public User[] testGetUsersContaining(String q) {
			return users.getUsersContaining(q);
		}
		
		public User[] testGetUsersJoinedBeforeDate(Date date) {
			return users.getUsersJoinedBefore(date);
		}
		
		public String arrUserToString(User[] arr) {
			if (arr == null) return "NO ARRAY";
			String retString = "[\n";
			for (int i = 0; i < arr.length; i++) {
				retString += userToString(arr[i]) + "\n";
			}
			retString += "]";
			return retString;
		}
		
		public String userToString(User u) {
			if (u == null) return "NO USER";
			return "(" + u.getName() + " " + u.getId() + " " + u.getPrettyDateJoined() + ")";
		}
		
		public static void main(String[] args) {
		UserTests test = new UserTests();
		
		System.out.println("Start UserTests");
		//Initialise Stuff
		final int idLength = 10;
		User[] users;
		int[] ids = new int[idLength];
		int i = 0;
		int j = 0;
		System.out.println();
		System.out.println("Add " + idLength + " Users");
		//Add ten users to the userStore with random IDs.
		while (i < idLength) {
			j = test.testAddUser();
			if (j > -1) {
				ids[i] = j;
				System.out.println("Added User to Array, ID = " + j);
				i++;
			} else {
				System.out.println("Collision of IDs, user not added, ID = " + j);
			}
		}
		System.out.println();
		System.out.println("Check collisions with " + idLength + " Users");
		//Explicit Collision Tests.
		for (i = 0; i < idLength; i++)	
			System.out.println("Success in adding while ID exists: " + test.testAddWithID(ids[i]));
		
		System.out.println();
		System.out.println("Check getting by ID");
		//Check we can access all ten users by ID.
		for (i = 0; i < idLength; i++)
			System.out.println("Success in getting: " + (test.testGetUser(ids[i]) != null));
		
		//Extract all users added so far.
		users = test.testGetUsers();
		System.out.println();
		System.out.println("Get All Users Test: " + (users != null));
		System.out.println(test.arrUserToString(users));
		//Array of users will be sorted by date, therefore the middle value should
		//bisect the list in terms of dates. Used later.
		Date dateTemp = users[idLength / 2].getDateJoined();
		
		//Check for string containing.
		System.out.println();
		System.out.println("Check generic name part i.e. query = 'Just'");
		users = test.testGetUsersContaining("Just");
		System.out.println(test.arrUserToString(users));
		System.out.println();
		System.out.println("Check against an ID number's LSD = " + ids[idLength / 2] % 10);
		users = test.testGetUsersContaining("" + (ids[idLength / 2] % 10));
		System.out.println(test.arrUserToString(users));
		
		//Check for joined before date.
		System.out.println();
		System.out.println("Check before date: " + dateTemp.toString());
		System.out.println(test.arrUserToString(test.testGetUsersJoinedBeforeDate(dateTemp)));
		
		System.out.println("End UserStore Tests");
		
		
	}
}
