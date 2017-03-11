package uk.ac.warwick.java.cs126.services;

public abstract class GenericBucket<E> {
	
	private ItemAndPoint<E> head;
	private int count;
	
	public GenericBucket() {
		head = null;
		count = 0;
	}
	
	abstract int compFunc(E a, E b);
	
	public boolean insertAndSort(E item) {
		//Quickly insert the item via an insertion sort with one run through
		//Time Complexity: O(n)
		//Means that all weets are sorted by date making it a lot quicker to output
		//The weets when requested.
		boolean inserted = false;
		count++;
		ItemAndPoint<E> temp = new ItemAndPoint<>(item);
		if (head == null) {
			head = temp;
			return true;
		}
		ItemAndPoint<E> prev = null;
		ItemAndPoint<E> ptr = head;
		while (!inserted && ptr != null) {
			if(compFunc(item, ptr.getCurrent()) < 1) {
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
	
	@SuppressWarnings("unchecked")
	public E[] getEees() {
		if (count == 0) return null;
		E[] retArray = (E[]) new Object[count];
		int i = 0;
		ItemAndPoint ptr = head;
		while (ptr != null) {
			retArray[i] = (E) ptr.getCurrent();
			i++;
			ptr = ptr.getNext();
		}
		
		return retArray;
	}
	
}