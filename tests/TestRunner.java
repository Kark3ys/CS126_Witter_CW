import java.util.Date;
import uk.ac.warwick.java.cs126.models.User;

public class TestRunner {
	
	public static void main(String[] args) {
		//USER TEST SECTION START
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
		//USER TEST SECTION END
		
	}
}
