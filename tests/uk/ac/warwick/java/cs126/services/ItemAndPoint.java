public class ItemAndPoint<E> {
	private E current;
	private ItemAndPoint<E> next;
	
	public ItemAndPoint(E item) {
		current = item;
		next = null;
	}
	
	public void gottaPointFast(ItemAndPoint<E> youreTooSlow) {
		next = youreTooSlow;
	}
	
	public E getCurrent() {
		return current;
	}
	
	public ItemAndPoint<E> getNext() {
		return next;
	}
}