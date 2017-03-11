package uk.ac.warwick.java.cs126.services;
import java.util.Date;

public abstract class DateBinarySearch<E> {
	//Binary search where the target is a date.
	abstract int compFunc(E item, Date target); 
	//Proide similar capability to compareTo but can be specified for custom searches.
	
	public Integer search(E[] arrIn, Date target, int left, int right) {
		//Binary search through arrIn based on target.
		//Returns the index in arrIn either on target or just past where target would be.
		//System.out.println("Bin Sort, Left: " + left + " Right: " + right);
		if (left >= right) return left;
		int mid = (left+right)/2;
		//System.out.println("\tMid: " + mid);
		int comp = compFunc(arrIn[mid], target);
		//System.out.println("\tComp: " + comp);
		if (comp > 0) return search(arrIn, target, mid+1, right);
		else if (comp < 0) return search(arrIn, target, left, mid-1);
		else if (comp == 0) return mid;
		return null; //Return null if we get here, something has gone wrong.
	}
}