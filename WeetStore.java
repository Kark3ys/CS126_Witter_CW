/*
Adam Dodson u1600262
Repo: https://github.com/Kark3ys/CS126_Witter_CW
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

class WeetAndPoint extends ItemAndPoint<Weet> {
	//Wrapper item for the singly linked list implementation of WeetStore
	public WeetAndPoint(Weet w) {
		super(w);
	}
}

class WeetBucket extends GenericBucket<Weet> {
	public WeetBucket() {
		super();
	}
	
	int compFunc(Weet a, Weet b) {
		return a.getDateWeeted().compareTo(b.getDateWeeted());
	}
	
	public Weet[] getWeets() {
		Object[] arrObj = super.getEees();
		Weet[] retArray = new Weet[arrObj.length];
		for (int i = 0; i < retArray.length; i++)
			//I hate the lack of generic arrays in java.
			retArray[i] = (Weet) arrObj[i];
		return retArray;
	}
}

class BucketPointUID extends BucketAndPoint<WeetBucket, Integer> {
	public BucketPointUID(Integer ch, WeetBucket wb) {
		super(ch, wb);
	}
}

class BucketPointDate extends BucketAndPoint<WeetBucket, Date> {
	public BucketPointDate(Date ch, WeetBucket wb) {
		super(ch, wb);
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

class TrendAndPoint extends ItemAndPoint<Trend> {
	public TrendAndPoint(String tag) {
		super(new Trend(tag));
	}
	
	public Trend getTrend() {
		Trend retVal = super.getCurrent();
		return retVal;
	}
}

class DBPDBS extends DateBinarySearch<BucketPointDate> {
	int compFunc(BucketPointDate item, Date target) {
		return item.getCheck().compareTo(target);
	}
}

class SortByDate extends MergeSort {
	int compFunc(Object a, Object b) {
		return ((BucketPointDate) a).getCheck().compareTo(((BucketPointDate) b).getCheck());
	}
}

class SortTrends extends MergeSort {
	int compFunc(Object a, Object b) {
		Trend first = (Trend) a;
		Trend second = (Trend) b;
		return ((Trend) a).getCount() - ((Trend) b).getCount();
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
	private DBPDBS searcher;
	private SortByDate sorterDate;
	private SortTrends sorterTrend;
	private int weetCount;
	private int dateCount; //Number of Date Buckets
	private int uniqueWeetedUsers; //Number of UID Buckets.
	private int trendCount;
	
	public WeetStore() {
		idHashSize = 100000;
		uidHashSize = 100000;
		dateHashSize = 100000;
		trendHashSize = 100000;
		idHashtable = new WeetAndPoint[idHashSize];
		uidHashtable = new BucketPointUID[uidHashSize];
		dateHashtable = new BucketPointDate[dateHashSize];
		trendTable = new TrendAndPoint[trendHashSize];
		searcher = new DBPDBS();
		sorterDate = new SortByDate();
		sorterTrend = new SortTrends();
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
			temp = (WeetAndPoint) temp.getNext();
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
			temp = (BucketPointDate) temp.getNext();
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
			((WeetBucket) prev.getNext().getCurrent()).insertAndSort(weet);
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
			temp = (BucketPointUID) temp.getNext();
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
			((WeetBucket) prev.getNext().getCurrent()).insertAndSort(weet);
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
				temp = (TrendAndPoint) temp.getNext();
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
			check = (WeetAndPoint) check.getNext();
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
					temp = (BucketPointDate) temp.getNext();
				}
			}
		}
		
		Object[] result = new Object[dateBuckets.length];
		for (i = 0; i < result.length; i++)
			result[i] = (Object) dateBuckets[i];
		result = sorterDate.sort(result);
		for (i = 0; i < result.length; i++)
			dateBuckets[i] = (BucketPointDate) result[i];

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
			if (!match) temp = (BucketPointUID) temp.getNext();
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
			if (!match) temp = (BucketPointDate) temp.getNext();
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
		count = searcher.search(dateBuckets, date, 0, dateBuckets.length - 1);
		if (count == null) return null;
		count += 1;
		BucketPointDate[] focusedDateBuckets = new BucketPointDate[dateBuckets.length - count];
		for (int i = count; i < dateBuckets.length; i++) focusedDateBuckets[i - count] = dateBuckets[i];
		return extractWeetsFromBuckets(focusedDateBuckets);
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
			temp = trendTable[i];
			while (temp != null) {
				trends[j] = temp.getTrend();
				j++;
				temp = (TrendAndPoint) temp.getNext();
			}
		}
		Object[] result = new Object[trends.length];
		for (i = 0; i < result.length; i++)
			result[i] = (Object) trends[i];
		result = sorterTrend.sort(result);
		for (i = 0; i < result.length; i++)
			trends[i] = (Trend) result[i];
		
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
}
