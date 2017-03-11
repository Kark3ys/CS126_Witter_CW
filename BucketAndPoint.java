package uk.ac.warwick.java.cs126.services;

public class BucketAndPoint<B, C> {

	private C check;
	private B current;
	private BucketAndPoint<B, C> next;
	
	public BucketAndPoint(C ch, B buck) {
		check = ch;
		current = buck;
		next = null;
	}
	
	public void gottaPointFast(BucketAndPoint<B, C> youreTooSlow) {
		//Sonic Speed: O(1)
		next = youreTooSlow;
	}
	
	public B getCurrent() {
		return current;
	}
	
	public BucketAndPoint getNext() {
		return next;
	}
	
	public C getCheck() {
		return check;
	}	

}