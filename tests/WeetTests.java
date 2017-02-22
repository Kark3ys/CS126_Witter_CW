import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Random;
import java.util.concurrent.TimeUnit;


import uk.ac.warwick.java.cs126.models.User;
import uk.ac.warwick.java.cs126.services.IWeetStore;
import uk.ac.warwick.java.cs126.services.WeetStore;
import uk.ac.warwick.java.cs126.models.Weet;

public class WeetTests {
	private Random random;
	//Used to generate random numbers.
	private WeetStore weets;
	private User[] users;//Simply array of a few users to test making some posts.
	private String[] setTrends;
	
	public WeetTests() {
		weets = new WeetStore();
		random = new Random();
		users = new User[5];
		setTrends = new String[] {
			"#wednesday", "#dude", "#DCS", "#hydrate",
			"#morning", "#TESCO", "#Switch", "#Nintendo",
			"#Witter", "#2hu", "#Warwick", "#Coventry"
		};
		int i = 0;
		for (i = 0; i < 5; i++) {
			users[i] = new User("Spammer", i, new Date((long) random.nextInt()));
		}
	}
	public int testAddWeet() 
	{
		//Add a single weet with random id numbers.
		//Returns the ID number used if successful, else return -1.
		int randInt = random.nextInt(1000000); //Pick a random ID number for the weet
		String strTrend = "";
		for (int i = 0; i < random.nextInt(5); i++) {
			strTrend += setTrends[random.nextInt(12)] + " ";
		}
		Weet temp = new Weet(randInt, users[random.nextInt(5)].getId(), strTrend 
			+ "A weet " + randInt, new Date((long) random.nextInt()));
		System.out.println(weetToString(temp));
		return ((weets.addWeet(temp)) ? randInt : -1);
	}
	
	public boolean testAddWithID(int wid) {
		Weet temp = new Weet(wid, users[random.nextInt(5)].getId(), "Conflicting Tweet " + wid, new Date((long) random.nextInt()));
		return weets.addWeet(temp);
	}
	
	public Weet testGetWeet(int wid) {
		//Return null if user not found.
		return weets.getWeet(wid);
	}
	
	public Weet[] testGetWeets() {
		return weets.getWeets();
	}
	
	public Weet[] testGetWeetsByUser(User u) {
		return weets.getWeetsByUser(u);
	}
	
	public Weet[] testGetWeetsContaining(String q) {
		return weets.getWeetsContaining(q);
	}
	
	public Weet[] testGetWeetsOn(Date d) {
		return weets.getWeetsOn(d);
	}
	
	public Weet[] testGetWeetsBefore(Date d) {
		return weets.getWeetsBefore(d);
	}
	
	public String[] testGetTrending() {
		return weets.getTrending();
	}
	
	public String[] getTrends() {
		return setTrends;
	}
	
	public User[] getWorkingUsers() {
		return users;
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
	
	public String arrUserToString() {
		//Deal with the working user array instead.
		if (users == null) return "NO ARRAY";
		String retString = "[\n";
		for (int i = 0; i < users.length; i++) {
			retString += userToString(users[i]) + "\n";
		}
		retString += "]";
		return retString;
	}
	
	public String arrWeetToString(Weet[] arr) {
		if (arr == null) return "NO ARRAY";
		String retString = "[\n";
		for (int i = 0; i < arr.length; i++) {
			retString += weetToString(arr[i]) + "\n";
		}
		retString += "]";
		return retString;			
	}
	
	public String userToString(User u) {
		if (u == null) return "NO USER";
		return "(" + u.getName() + " " + u.getId() + " " + u.getPrettyDateJoined() + ")";
	}
	
	public String weetToString(Weet w) {
		if (w == null) return "NO WEET";
		return "(" + w.getId() + " - " + w.getUserId() + " - " + w.getMessage() + " - " + w.getPrettyDateWeeted() + ")";
	}
		
	public static void main(String[] args) {
		WeetTests test = new WeetTests();
		System.out.println("Start WeetTests");
		//Initialise Stuff
		final int idLength = 30;
		User[] users = test.getWorkingUsers();
		Weet[] weets;
		int[] ids = new int[idLength];
		int i = 0;
		int j = 0;
		System.out.println();
		System.out.println("Add " + idLength + " Weets");
		//Add 30 users to the userStore with random IDs.
		while (i < idLength) {
			j = test.testAddWeet();
			if (j > -1) {
				ids[i] = j;
				System.out.println("Added Weet to Array, ID = " + j);
				i++;
			} else {
				System.out.println("Collision of IDs, weet not added.");
			}
		}
		System.out.println();
		System.out.println("Check collisions with " + idLength + " Weets");
		//Explicit Collision Tests.
		for (i = 0; i < idLength; i++)	
			System.out.println("Success in adding while ID exists: " + test.testAddWithID(ids[i]));
		
		System.out.println();
		System.out.println("Check getting by ID");
		//Check we can access all 30 weets by ID.
		for (i = 0; i < idLength; i++)
			System.out.println("Success in getting: " + (test.testGetWeet(ids[i]) != null));
		
		//Extract all weets added so far.
		System.out.println();
		weets = test.testGetWeets();
		System.out.println("Get All Weets Test: " + (weets != null));
		System.out.println(test.arrWeetToString(weets));
		//Array of users will be sorted by date, therefore the middle value should
		//bisect the list in terms of dates. Used later.
		System.out.println(test.arrUserToString());
		Date dateTemp = weets[idLength / 2].getDateWeeted();
		
		//Check weets by user.
		System.out.println();
		System.out.println("Check weets made by user ID: " + users[0].getId());
		weets = test.testGetWeetsByUser(users[0]);
		System.out.println(test.arrWeetToString(weets));
		
		//Check for string containing.
		System.out.println();
		System.out.println("Check generic name part i.e. query = 'weet'");
		weets = test.testGetWeetsContaining("weet");
		System.out.println(test.arrWeetToString(weets));
		System.out.println();
		String[] arrStrTrends = test.getTrends();
		for (i = 0; i < arrStrTrends.length; i++) {
			System.out.println("Check weet contains string = " + arrStrTrends[i]);
			weets = test.testGetWeetsContaining(arrStrTrends[i]);
			System.out.println(test.arrWeetToString(weets));
		}		
		//Check for weeted before date.
		System.out.println();
		System.out.println("Check before date: " + dateTemp.toString());
		weets = test.testGetWeetsBefore(dateTemp);
		System.out.println(test.arrWeetToString(weets));
		
		//Check for weeted on date.
		System.out.println();
		System.out.println("Check on date: " + dateTemp.toString());
		weets = test.testGetWeetsOn(dateTemp);
		System.out.println(test.arrWeetToString(weets));
		
		//Check trends.
		System.out.println();
		System.out.println("Get trending topics.");
		arrStrTrends = test.testGetTrending();
		for (i = 0; i < arrStrTrends.length; i++) System.out.println(arrStrTrends[i]);
		
		System.out.println("End WeetStore Tests");
	}
}
