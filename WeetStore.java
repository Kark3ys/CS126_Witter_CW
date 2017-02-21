/*
Adam Dodson u1600262
Preambly Bit:
I like closed bucket hashtables.
I also like linked lists inside buckets.
Space efficiency isn't the main concern, time efficieny is for apps dealing
with a large number of clients nowadays (like twitter).
Therefore there are four hashtables used to represent WeetStore.
Two of these hashtables point to buckets that point to linked lists, rather
than pointing straight to a linked list.
Weets stored by weet ID have their own cbht.
Weets stored in buckets of user ID, where the buckets are accessed via a 
hash table. These hashtables point to linked lists of buckets.
Weets stored by date is done in exactly the same way, except their hash function
is using date.
The Trending tags are also stored in their own hashtable as a set of tags, with
a separate count variable to aid in sorting when the top tags are asked for.
Though the end result is quite a large (still O(n)) amount of data stored on the
server, the resulting time complexity for certain access operations is greatly
reduced.
*/

//Need to turn a lot of this stuff into generics with the supress unchecked stuff.

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.User;
import uk.ac.warwick.java.cs126.models.Weet;

import java.io.BufferedReader;
import java.util.Date;
import java.io.FileReader;
import java.text.ParseException;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class WeetAndPoint {
	//Wrapper item for the singly linked list implementation of WeetStore
	private Weet current;
	private WeetAndPoint next;
	
	public WeetAndPoint(Weet wt) {
		current = wt;
		next = null;
	}
	
	public void gottaPointFast(WeetAndPoint youreTooSlow) {
		next = youreTooSlow;
	}
	
	public Weet getCurrent() {
		return current;
	}
	
	public WeetAndPoint getNext() {
		return next;
	}
}

class WeetBucket {
	//WeetAndPoint Single Linked Lists sorted by Date.
	private WeetAndPoint head;
	private int count;

	public WeetBucket() {
		head = null;
		count = 0;
	}
	
	public boolean insertAndSort(Weet wt) {
		//Quickly insert the item via an insertion sort with one run through
		//Time Complexity: O(n)
		//Means that all weets are sorted by date making it a lot quicker to output
		//The weets when requested.
		boolean inserted = false;
		count++;
		WeetAndPoint temp = new WeetAndPoint(wt);
		if (head == null) {
			head = temp;
			return true;
		}
		Date dateTemp = wt.getDateWeeted();
		WeetAndPoint prev = null;
		WeetAndPoint ptr = head;
		while (!inserted && ptr != null) {
			if(dateTemp.compareTo(ptr.getCurrent().getDateWeeted()) < 1) {
				//Insert at current position and shif the current ptr down a place
				inserted = true;
			} else {
				prev = ptr;
				ptr = ptr.getNext();
			}
		}
		
		temp.gottaPointFast(ptr);
		if (prev == null) {
			//If prev is null, that means the new item is the new head of the list.
			head = temp;
		} else {
			//If prev is set, then the new item needs to go between prev and ptr
			prev.gottaPointFast(temp);
		}
		
		return true;
	}	
	
	public boolean clear() {
		head = null;
		count = 0;
		return true;
	}
	
	public Weet[] getWeets() {
		if (count == 0) return null;
		Weet[] retArray = new Weet[count];
		int i = 0;
		WeetAndPoint ptr = head;
		while (ptr != null) {
			retArray[i] = ptr.getCurrent();
			i++;
			ptr = ptr.getNext();
		}
		
		return retArray;
	}
	
	public int getCount() {
		return count;
	}
}

class BucketPointUID {
	//Bucket pointer where check is either date or uid.
	private int check;
	private WeetBucket current;
	private BucketPointUID next;
	
	public BucketPointUID(int ch, WeetBucket wb) {
		check = ch;
		current = wb;
		next = null;
	}
	
	public void gottaPointFast(BucketPointUID youreTooSlow) {
		//Sonic Speed: O(1)
		next = youreTooSlow;
	}
	
	public WeetBucket getCurrent() {
		return current;
	}
	
	public BucketPointUID getNext() {
		return next;
	}
	
	public int getCheck() {
		return check;
	}
}

class BucketPointDate {
	//Bucket pointer where check is either date or uid.
	private Date check;
	private WeetBucket current;
	private BucketPointDate next;
	
	public BucketPointDate(Date ch, WeetBucket wb) {
		check = ch;
		current = wb;
		next = null;
	}
	
	public BucketPointDate(BucketPointDate bpd) {
		check = bpd.getCheck();
		current = bpd.getCurrent();
		next = null;
	}
	
	public void gottaPointFast(BucketPointDate youreTooSlow) {
		//Sonic Speed: O(1)
		next = youreTooSlow;
	}
	
	public WeetBucket getCurrent() {
		return current;
	}
	
	public BucketPointDate getNext() {
		return next;
	}
	
	public Date getCheck() {
		return check;
	}
}

class Trend {
	private String trend;
	private int count;
	
	public Trend(String tag) {
		trend = tag;
		count = 1;
	}
	
	public void add() {
		count++;
	}
	
	public String getTrend() {
		return trend;
	}
	
	public int getCount() {
		return count;
	}
}

class TrendAndPoint {
	private Trend trend;
	private TrendAndPoint next;
	
	public TrendAndPoint(String tag) {
		trend = new Trend(tag);
		next = null;
	}
	
	public void gottaPointFast(TrendAndPoint youreTooSlow) {
		next = youreTooSlow;
	}
	
	public Trend getTrend() {
		return trend;
	}
	
	public TrendAndPoint getNext() {
		return next;
	}
}

public class WeetStore implements IWeetStore {
	private int idHashSize;
	private int uidHashSize;
	private int dateHashSize;
	private int trendHashSize;
	private WeetAndPoint[] idHashtable;
	//Hashtable storing weets based on ID
	private BucketPointUID[] uidHashtable;
	//Hashtable storing buckets of weets based on uid.
	private BucketPointDate[] dateHashtable;
	//Hashtable storing buckets of weets based on date.
	private TrendAndPoint[] trendTable;
	private int weetCount;
	private int dateCount; //Number of Date Buckets
	private int uniqueWeetedUsers; //Number of UID Buckets.
	private int trendCount;
	
	public WeetStore() {
		idHashSize = 1000000;
		uidHashSize = 1000000;
		dateHashSize = 100000;
		trendHashSize = 100000;
		idHashtable = new WeetAndPoint[idHashSize];
		uidHashtable = new BucketPointUID[uidHashSize];
		dateHashtable = new BucketPointDate[dateHashSize];
		trendTable = new TrendAndPoint[trendHashSize];
		weetCount = 0;		
		dateCount = 0;
		uniqueWeetedUsers = 0;
		trendCount = 0;
	}
	
	private int idHFunc(int id) {
		return id % idHashSize;
	}
	
	private int uidHFunc(int uid) {
		return uid % uidHashSize;
	}
	
	private int dateHFunc(Date date) {
		//As the buckets are based on the day of the weet, we need to eliminate
		//the time part of the milliseconds number.
		//To do this we just divide by the number of milliseconds in a day
		//by the time before modulating it.
		int retVal = (int) ((date.getTime() / TimeUnit.DAYS.toMillis(1)) % dateHashSize);
		if (retVal < 0) retVal += dateHashSize;
		return retVal;
	}
	
	private int trendHFunc(String tag) {
		//Use a Polynomial Acummulator first before modulating the number.
		//http://letslearncs.com/hash-functions/
		char[] input = tag.toCharArray();
		long acc = 0;
		for (int i = 0; i < input.length; i++) {
			acc = (long) (acc + (int) input[i]);
			acc = (long) acc * 41;
		}
		acc = acc % trendHashSize;
		if (acc < 0) acc += trendHashSize;
		return (int) acc;		
	}

	public boolean addWeet(Weet weet) {
		
		boolean result = false;
		result = addWeetIDTable(weet);
		if (result) result = addWeetDateBucket(weet);
		if (result) result = addWeetUserBucket(weet);
		if (result) addTagData(weet);	//We don't really care if a tag was added.
		
		return result;
	}
	
	private boolean addWeetIDTable(Weet weet) {
		//INSERT INTO WID HT
		//Time Complexity
		//	Best Case: O(1)
		//	Worst Case: O(n) - All weet ids point to the same hashtable index
		//		after passing through the hash function.
		int wid = weet.getId();
		if (getWeet(wid) != null) return false;
		WeetAndPoint temp = idHashtable[idHFunc(wid)];
		WeetAndPoint prev = null;
		while (temp != null) {
			prev = temp;
			temp = temp.getNext();
		}
		
		if (prev == null) {
			idHashtable[idHFunc(wid)] = new WeetAndPoint(weet);
		} else {
			prev.gottaPointFast(new WeetAndPoint(weet));
		}
		this.weetCount++;
		return true;
	}
	
	private boolean addWeetDateBucket(Weet weet) {
		//Technically the buckets are for each day.
		//Time Complexity
		//	Best Case: O(1)
		//	Worst Case: O(n) - All weet dates point to the same hashtable index
		//		after passing through the hash function. Or weets are made all on the
		//		same day.
		Date wDate = truncDate(weet.getDateWeeted());
		//Get the date from the day only, truncate the hours, minutes, seconds.
		BucketPointDate temp = dateHashtable[dateHFunc(wDate)];
		BucketPointDate prev = null;
		boolean match = false;
		while (!match && temp != null) {
			match = temp.getCheck().equals(wDate);
			prev = temp;
			temp = temp.getNext();
		}
		
		if (prev == null) {
			//If there are no date buckets in this slot, insert a new one and
			//Fill the first slot of that bucket.
			dateHashtable[dateHFunc(wDate)] = new BucketPointDate(wDate, new WeetBucket());
			dateHashtable[dateHFunc(wDate)].getCurrent().insertAndSort(weet);
		} else if (match) {
			//On match, we want to insert this weet into the end of the bucket we've found.
			prev.getCurrent().insertAndSort(weet);
		} else {
			//A match hasn't been found yet buckets exist in this slot.
			//New bucket needs appending.
			prev.gottaPointFast(new BucketPointDate(wDate, new WeetBucket()));
			prev.getNext().getCurrent().insertAndSort(weet);
		}
		wDate = null;
		if (!match) dateCount++;
		return true;
	}
	
	private boolean addWeetUserBucket(Weet weet) {
		//Time Complexity
		//	Best Case: O(1)
		//	Worst Case: O(n) - All weet user ids point to the same hashtable index
		//		after passing through the hash function. Or weets are made by the same
		//		user.
		int uid = weet.getUserId();
		BucketPointUID temp = uidHashtable[uidHFunc(uid)];
		BucketPointUID prev = null;
		boolean match = false;
		while (!match && temp != null) {
			match = temp.getCheck() == uid;
			prev = temp;
			temp = temp.getNext();
		}
		
		if (prev == null) {
			//If there are no uid buckets in this slot, insert a new one and
			//Fill the first slot of that bucket.
			uidHashtable[uidHFunc(uid)] = new BucketPointUID(uid, new WeetBucket());
			uidHashtable[uidHFunc(uid)].getCurrent().insertAndSort(weet);
		} else if (match) {
			//On match, we want to insert this weet into the end of the bucket we've found.
			prev.getCurrent().insertAndSort(weet);
		} else {
			//A match hasn't been found yet buckets exist in this slot.
			//New bucket needs appending.
			prev.gottaPointFast(new BucketPointUID(uid, new WeetBucket()));
			prev.getNext().getCurrent().insertAndSort(weet);
		}
		if (!match) uniqueWeetedUsers++;
		return true;
	}
	
	private boolean addTagData(Weet weet) {
		//Time Complexity
		//	Best Case: O(1)
		//	Worst Case: O(n) - All weet user ids point to the same hashtable index
		//		after passing through the hash function. Or weets are made by the same
		//		user.
		//Return true if tags are added, false if not.
		String[] tags = extractTags(weet.getMessage());
		if (tags == null) return false;
		/*for (int i = 0; i < tags.length; i++) {
			System.out.println(tags[i]);
		}*/
		TrendAndPoint temp, prev;
		temp = prev = null;
		boolean match = false;
		//If we have tags, we need to insert them into the set of trends.
		for (int k = 0; k < tags.length; k++) {
			temp = trendTable[trendHFunc(tags[k])];
			match = false;
			while (!match && temp != null) {
				match = temp.getTrend().getTrend().equals(tags[k]);
				prev = temp;
				temp = temp.getNext();
			}
			
			if (prev == null) {
				trendTable[trendHFunc(tags[k])] = new TrendAndPoint(tags[k]);
			} else if (match) {
				prev.getTrend().add();
			} else {
				prev.gottaPointFast(new TrendAndPoint(tags[k]));
			}
			if (!match) trendCount++;
			prev = temp = null;
		}
		return true;
	}
	
	public Weet getWeet(int wid) {
		WeetAndPoint check = idHashtable[idHFunc(wid)];
		while (check != null) {
			if (check.getCurrent().getId() == wid) return check.getCurrent();
			check = check.getNext();
		}
		return null;
	}

	public Weet[] getWeets() {
		//Get weets by extracting through the date buckets and sorting.
		//When buckets are sorted, weets can be expanded out and all's good.
		if (weetCount == 0) return null;
		BucketPointDate[] dateBuckets = getWeetDateBuckets();
		return extractWeetsFromBuckets(dateBuckets);
	}
	
	private BucketPointDate[] getWeetDateBuckets() {
		//Run through the date bucket hashtable and collect all buckets.
		//Sort said buckets by date, then return them buckets.
		//Time complexity: O(d) where "d" is the size of the date bucket hashtable.
		if (weetCount == 0) return null;
		BucketPointDate[] dateBuckets = new BucketPointDate[dateCount];
		BucketPointDate temp;
		int i = 0;
		int j = 0;
		for (i = 0; i < dateHashSize; i++) {
			if (dateHashtable[i] != null) {
				temp = dateHashtable[i];
				while (temp != null) {
					dateBuckets[j] = temp;
					j++;
					temp = temp.getNext();
				}
			}
		}
		dateBuckets = sortByDate(dateBuckets);

		return dateBuckets;
	}
	
	private Weet[] extractWeetsFromBuckets(BucketPointDate[] dateBuckets) {
		//Pulls all the weets out of the array of buckets provided.
		//Time Complexity: O(n) as manually pulling out all weets from provided date
		//	buckets. At least no need for another sorting algorithm on the weets.
		if (dateBuckets == null) return null;
		Weet[] retArray;
		int count = 0;
		int i = 0;
		int j = 0;
		for (i = 0; i < dateBuckets.length; i++) count += dateBuckets[i].getCurrent().getCount();
		retArray = new Weet[count];
		for (i = 0; i < dateBuckets.length; i++) {
			Weet[] temp = dateBuckets[i].getCurrent().getWeets();
			for (int k = 0; k < temp.length; k++) {
				retArray[j] = temp[k];
				j++;
			}
		}
		return retArray;
	}

	public Weet[] getWeetsByUser(User usr) {
		//Just check for an existing user id bucket, and if it's there, 
		//dump the contents as the return value.
		//Due to the insertion sort for each bucket, it should be date ordered.
		//Time Complexity: O(n)
		if (weetCount == 0) return null;
		int uid = usr.getId();
		BucketPointUID temp = uidHashtable[uidHFunc(uid)];
		boolean match = false;
		while (!match && temp != null) {
			match = temp.getCheck() == uid;
			if (!match) temp = temp.getNext();
		}
		if (temp == null) return null;
		return temp.getCurrent().getWeets();
	}

	public Weet[] getWeetsContaining(String query) {
		//This one is just brute force, nothing more nothing less.
		//Time Complexity: O(n)
		if (weetCount == 0) return null;
		Weet[] arrAllWeets = getWeets();
		Weet[] retArray;
		boolean[] arrMatch = new boolean[arrAllWeets.length];
		//Indexes match with the user indexes in arrAllWeets, so if true in this
		//array, then it means that user matches the string check.
		//This round about way is required so we can find the length of the 
		//return value array, then actually assign the object references without
		//having to do another set of string checks.
		//In why I'm using array.length instead of weetCount, I just like the look
		//of it better and helps me understand that I'm specifically linking these
		//two arrays.
		int count = 0;
		int i = 0;
		for (i = 0; i < arrMatch.length; i++) {
			arrMatch[i] = arrAllWeets[i].getMessage().toLowerCase().contains(query.toLowerCase());
			if (arrMatch[i]) count++;
		}
		if (count == 0) return null;
		retArray = new Weet[count];
		int j = 0;
		for (i = 0; i < arrMatch.length; i++) 
			if (arrMatch[i]) {
				retArray[j] = arrAllWeets[i];
				j++;
			}
		
		return retArray;
	}

	public Weet[] getWeetsOn(Date dateOn) {
		//Find the bucket corresponding to a specific day.
		//Weets in each bucket should be sorted by date i.e. the milliseconds date
		//not my truncated version.
		//Time Complexity: O(1) YAY!!!!! 
		//	But then O(n) for extracting the weets from said bucket :') 
		if (weetCount == 0) return null;
		Date date = truncDate(dateOn);
		BucketPointDate temp = dateHashtable[dateHFunc(date)];
		boolean match = false;
		while (!match && temp != null) {
			match = temp.getCheck().equals(date);
			if (!match) temp = temp.getNext();
		}
		if (temp == null) return null;	//No weets made on that day.
		return temp.getCurrent().getWeets();
	}

	public Weet[] getWeetsBefore(Date dateBefore) {
		//Gets all date buckets, sorts them, then truncates the returned date bucket
		//	array so that everything is still ordered but it's the time period we want.
		//Time Complexity: O(n)
		if (weetCount == 0) return null;
		BucketPointDate[] dateBuckets = getWeetDateBuckets();
		Integer count = null;
		Date date = truncDate(dateBefore);
		count = binSearchDate(dateBuckets, date, 0, dateBuckets.length - 1);
		if (count == null) return null;
		count += 1;
		BucketPointDate[] focusedDateBuckets = new BucketPointDate[dateBuckets.length - count];
		for (int i = count; i < dateBuckets.length; i++) focusedDateBuckets[i - count] = dateBuckets[i];
		return extractWeetsFromBuckets(focusedDateBuckets);
	}
	
	private Integer binSearchDate(BucketPointDate[] bpd, Date target, int left, int right) {
		//Binary search through an array of bpd sorted by date.
		//Returns the index of the date before and after target.
		//System.out.println("Bin Sort, Left: " + left + " Right: " + right);
		if (left == right) return left;
		int mid = (left+right)/2;
		//System.out.println("\tMid: " + mid);
		int comp = bpd[mid].getCheck().compareTo(target);
		//System.out.println("\tComp: " + comp);
		if (comp > 0) return binSearchDate(bpd, target, mid+1, right);
		else if (comp < 0) return binSearchDate(bpd, target, left, mid-1);
		else if (comp == 0) return mid;
		return null; //Return null if we get here, something has gone wrong.
	}

	public String[] getTrending() {
		//Build list of trending topics.
		//Just dump all the trends into an array
		//Then use the merge sort on it again.
		//Time Complexity: O(n) for extracting the data from the trending hashtable
		if (trendCount == 0) return null;
		Trend[] trends = new Trend[trendCount];
		TrendAndPoint temp;
		int j = 0;
		int i = 0;
		for (i = 0; i < trendHashSize; i++) {
			if (trendTable[i] != null) {
				temp = trendTable[i];
				while (temp != null) {
					trends[j] = temp.getTrend();
					j++;
					temp = temp.getNext();
				}
			}
		}
		
		trends = sortTrends(trends);
		String[] retArray = new String[10];
		for (i = 0; i < retArray.length && i < trends.length; i++)
			retArray[i] = trends[i].getTrend();
		return retArray;
	}
	
	private String[] extractTags(String msg) {
		//http://stackoverflow.com/a/24694378
		//http://stackoverflow.com/a/3413712
		//Used stack overflow to see how I could build a string array.
		Pattern pat = Pattern.compile("#\\w+", Pattern.CASE_INSENSITIVE);
		Matcher match = pat.matcher(msg);
		String temp = "";
		String holder = "";
		while (match.find()) {
			holder = match.group();
			if (!temp.contains(holder))
				//Make sure no dupplicates get through.
				temp += holder + " ";
		}
		if (temp == "") return null;
		temp += "\b";
		String[] arrTemp = temp.split(" ");
		//Seems as though there's an extra slot at the end of the array.
		String[] retArray = new String[arrTemp.length - 1];
		for (int i = 0; i < retArray.length; i++)
			retArray[i] = arrTemp[i];
		//retArray needed as there always seemed to be an empty element appended
		//to the arrTemp when splitting the string, so just need to do a copy and
		//truncate.
		return retArray;
	}
	
	private Date truncDate(Date date) {
		//Convert a date object into a date for the start of a particular day.
		return new Date(date.getTime() / TimeUnit.DAYS.toMillis(1) * TimeUnit.DAYS.toMillis(1));
	}
	
	private BucketPointDate[] sortByDate(BucketPointDate[] arrIn) {
		//I really need to stop copying and pasting this merge sort and make a more generalised version.
		
		//This merge sort is a literal copy paste from UserStore.java.
		//It's been modified to work with the BucketPoint objects and sort them
		//By the date stored int their check. As a result it won't work with 
		//the user buckets, but their used for something different anyway.
		
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
		BucketPointDate[] arrLeft = new BucketPointDate[length/2];
		BucketPointDate[] arrRight = new BucketPointDate[length - length/2];
		//Get the left and right side of the array, on odd numbers,
		//arrLeft.length + 1 == arrRight.length.
		int i = 0;
		for (i = 0; i < length/2; i++) arrLeft[i] = arrIn[i];
		for (i = length/2; i < length; i++) arrRight[i - length/2] = arrIn[i];
		//Set up the left and right arrays to point at the correct users on the 
		//left and right of the input array.
		//System.out.println("Start Recursion");
		arrLeft = sortByDate(arrLeft);
		arrRight = sortByDate(arrRight);
		//Recursion here
		//System.out.println("End Recursion");
		//Time to do some merging.
		BucketPointDate[] retArray = new BucketPointDate[length];
		i = 0; //Loop counter for left array.
		int j = 0; //Loop counter for right array.
		int k = 0; //Loop counter for return array.
		//System.out.println("Array Lengths: L=" + arrLeft.length + " R=" + arrRight.length);
		while (i < arrLeft.length && j < arrRight.length) {
			//System.out.println("Nums: i=" + i + " j=" + j + " k=" + k);
			//System.out.println("Compare=" + arrLeft[i].getCheck().compareTo(arrRight[j].getCheck()));
			if(arrLeft[i].getCheck().compareTo(arrRight[j].getCheck()) > 0) {
				retArray[k] = arrLeft[i];
				//System.out.println("AddL");
				i++;
			} else {
				retArray[k] = arrRight[j];
				//System.out.println("AddR");
				j++;
			}
			k++;
		}
		//At the end of this loop, just fill in with the other array.
		while (i < arrLeft.length) {
			retArray[k] = arrLeft[i];
			//System.out.println("AddL");
			i++;
			k++;
		}
		
		while (j < arrRight.length) {
			retArray[k] = arrRight[j];
			//System.out.println("AddR");
			j++;
			k++;
		}
		
		arrLeft = arrRight = null; //Dereference our l/r arrays.
		//System.out.println("End Merge");
		return retArray;
		//Done with the merging, let's bring it back up.
	}
	
	private Trend[] sortTrends(Trend[] arrIn) {
		//This merge sort is a literal copy paste from the on above.
		//It's been modified to work with the Trend objects and sort them
		//By their count. Lazy programming ftw.
		
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
		//System.out.println(length);
		if (length == 1) return arrIn;	//No need to split
		if (length < 1) throw new NullPointerException("You fucked up the merge");
		Trend[] arrLeft = new Trend[length/2];
		Trend[] arrRight = new Trend[length - length/2];
		//Get the left and right side of the array, on odd numbers,
		//arrLeft.length + 1 == arrRight.length.
		int i = 0;
		for (i = 0; i < length/2; i++) arrLeft[i] = arrIn[i];
		for (i = length/2; i < length; i++) arrRight[i - length/2] = arrIn[i];
		//Set up the left and right arrays to point at the correct users on the 
		//left and right of the input array.
		//System.out.println("Start Recursion");
		arrLeft = sortTrends(arrLeft);
		arrRight = sortTrends(arrRight);
		//Recursion here
		//System.out.println("End Recursion");
		//Time to do some merging.
		Trend[] retArray = new Trend[length];
		i = 0; //Loop counter for left array.
		int j = 0; //Loop counter for right array.
		int k = 0; //Loop counter for return array.
		//System.out.println("Array Lengths: L=" + arrLeft.length + " R=" + arrRight.length);
		while (i < arrLeft.length && j < arrRight.length) {
			//System.out.println("Nums: i=" + i + " j=" + j + " k=" + k);
			//System.out.println("Compare=" + (arrLeft[i].getCount() - arrRight[j].getCount()));
			if(arrLeft[i].getCount() > arrRight[j].getCount()) {
				retArray[k] = arrLeft[i];
				//System.out.println("AddL");
				i++;
			} else {
				retArray[k] = arrRight[j];
				//System.out.println("AddR");
				j++;
			}
			k++;
		}
		//At the end of this loop, just fill in with the other array.
		while (i < arrLeft.length) {
			retArray[k] = arrLeft[i];
			//System.out.println("AddL");
			i++;
			k++;
		}
		
		while (j < arrRight.length) {
			retArray[k] = arrRight[j];
			//System.out.println("AddR");
			j++;
			k++;
		}
		
		arrLeft = arrRight = null; //Dereference our l/r arrays.
		//System.out.println("End Merge");
		return retArray;
		//Done with the merging, let's bring it back up.
	}
}
