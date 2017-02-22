import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Random;

import uk.ac.warwick.java.cs126.services.IFollowerStore;
import uk.ac.warwick.java.cs126.services.FollowerStore;

public class FollowerTests {
	private Random random;
	private FollowerStore followers;
	private int[] uids;
	private Date[][] followDates;
	private static final int UID_COUNT = 15;
	
	public FollowerTests() {
		followers = new FollowerStore();
		random = new Random();
		uids = new int[UID_COUNT];
		for (int i = 0; i < UID_COUNT; i++)
			uids[i] = i;
		followDates = new Date[UID_COUNT][UID_COUNT];
	}
	
	public boolean testAddFollower(int uidF, int uidL) {
		Date ranDate = new Date((long) random.nextInt());
		System.out.format("Attempting to make %d follow %d on %td/%tm/%tY%n", uidF, uidL, ranDate, ranDate, ranDate);
		if (followers.addFollower(uidF, uidL, ranDate)) {
			followDates[uidL][uidF] = ranDate;
			return true;
		}
		else 
			return false;
	}
	
	public int[] testGetFollowers(int uid) {
		return followers.getFollowers(uid);
	}
	
	public int[] testGetFollows(int uid) {
		return followers.getFollows(uid);
	}
	
	public boolean testIsAFollower(int uidF, int uidL) {
		return followers.isAFollower(uidF, uidL);
	}
	
	public int testGetNumFollowers(int uid) {
		return followers.getNumFollowers(uid);
	}
	
	public int[] testGetMutualFollowers(int uid1, int uid2) {
		return followers.getMutualFollowers(uid1, uid2);
	}
	
	public int[] testGetMutualFollows(int uid1, int uid2) {
		return followers.getMutualFollows(uid1, uid2);
	}
	
	public int[] testGetTopUsers() {
		return followers.getTopUsers();
	}
	
	public void arrsToString() {
		int i,j;
		for (i = 0; i < UID_COUNT; i++) {
			for (j = 0; j < UID_COUNT; j++) {
				System.out.format("%3d | %3d | %td/%tm/%tY%n", i, j, followDates[i][j], followDates[i][j], followDates[i][j]);
			}
		}
		System.out.println();
		for(i=-1; i<UID_COUNT; i++) {
			if (i >= 0) System.out.format("%3d|",i);
			else System.out.print("    ");
			for(j=0; j<UID_COUNT; j++) {
				if (i < 0) System.out.format("%3d|", j);
				else System.out.print("" + ((followDates[i][j] != null) ? " X " : "   ") + "|");
			}
			String repeated = new String(new char[80]).replace("\0", "-"); //http://stackoverflow.com/a/4903603
			System.out.println("\n"+repeated);
		}
	}
	
	public static void main(String[] args) {
		Random random = new Random();
		FollowerTests test = new FollowerTests();
		System.out.println("Start Follower Tests");
		System.out.println("Start Adding Follower Relations.");
		int i = 0;
		int j = 0;
		int k = 0;
		for (i = 0; i < UID_COUNT * UID_COUNT / 2 ; i++)
			System.out.println(test.testAddFollower(random.nextInt(UID_COUNT), random.nextInt(UID_COUNT)));
		System.out.println(test.testAddFollower(0,1));
		System.out.println(test.testAddFollower(0,1));
		test.testAddFollower(2,0);
		test.testAddFollower(2,1);
		test.testAddFollower(3,0);
		test.testAddFollower(3,1);
		test.testAddFollower(0,4);
		test.testAddFollower(1,4);
		test.arrsToString();
		System.out.println();
		i = 0;
		System.out.println("Get follower count for " + i );
		System.out.println(test.testGetNumFollowers(i));
		
		System.out.println();
		System.out.println("Start Test Get Followers for uid=0 and uid=1");
		int[] result = test.testGetFollowers(0);
		System.out.print("[");
		if (result != null)
			for (j = 0; j < result.length; j++)
				System.out.print(result[j] + ", ");
		System.out.print("\b\b]");
		result = test.testGetFollowers(1);
		System.out.print("[");
		if (result != null)
			for (j = 0; j < result.length; j++)
				System.out.print(result[j] + ", ");
		System.out.print("\b\b]");
		
		System.out.println();
		System.out.println("Start Test Get Follows for uid=0 and uid=1");
		result = test.testGetFollows(0);
		System.out.print("[");
		for (j = 0; j < result.length; j++)
			System.out.print(result[j] + ", ");
		System.out.print("\b\b]");
		result = test.testGetFollows(1);
		System.out.print("[");
		for (j = 0; j < result.length; j++)
			System.out.print(result[j] + ", ");
		System.out.print("\b\b]");
		
		System.out.println();
		i = 0;
		j = 1;
		System.out.println("Start test get mutual followers for uid1=" + i + " uid2=" + j);
		result = test.testGetMutualFollowers(i,j);
		System.out.print("[");
		for (k = 0; k < result.length; k++)
			System.out.print(result[k] + ", ");
		System.out.print("\b\b]");
		
		System.out.println();
		System.out.println("Start test get mutual follows for uid1=" + i + " uid2=" + j);
		result = test.testGetMutualFollows(i,j);
		System.out.print("[");
		for (k = 0; k < result.length; k++)
			System.out.print(result[k] + ", ");
		System.out.print("\b\b]");
		
		System.out.println();
		System.out.println("Testing for get top users, first print out all users follower count.");
		int count = 0;
		for (i = 0; i < UID_COUNT; i++) {
			System.out.println("id=" + i + " count=" + (test.testGetFollowers(i).length));
		}
		System.out.println("Now run test for top users.");
		result = test.testGetTopUsers();
		System.out.print("[");
		for (k = 0; k < result.length; k++)
			System.out.print(result[k] + ", ");
		System.out.print("\b\b]\n");
		
		System.out.println("End FollowerTests");
	}
}