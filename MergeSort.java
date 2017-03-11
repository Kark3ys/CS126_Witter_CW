package uk.ac.warwick.java.cs126.services;

public abstract class MergeSort<E> {
	
	abstract int compFunc(E a, E b);
	
	public E[] sort(E[] arrIn) {
		return sort(arrIn, arrIn.length);
	}
	
	@SuppressWarnings("unchecked")
	public E[] sort(E[] arrIn, int len) {
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
		int length = len;	//Find array length for reference.
		if (length == 1) {
			E[] retArray = (E[]) new Object[1];
			retArray[0] = arrIn[1];
			return retArray;	//No need to split
		}
		E[] arrLeft = (E[]) new Object[length/2];
		E[] arrRight = (E[]) new Object[length - length/2];
		//Get the left and right side of the array, on odd numbers,
		//arrLeft.length + 1 == arrRight.length.
		int i = 0;
		for (i = 0; i < length/2; i++) arrLeft[i] = arrIn[i];
		for (i = length/2; i < length; i++) arrRight[i - length/2] = arrIn[i];
		//Set up the left and right arrays to point at the correct users on the 
		//left and right of the input array.
		//System.out.println("Start Recursion");
		arrLeft = sort(arrLeft);
		arrRight = sort(arrRight);
		//Recursion here
		//System.out.println("End Recursion");
		//Time to do some merging.
		E[] retArray = (E[]) new Object[length];
		i = 0; //Loop counter for left array.
		int j = 0; //Loop counter for right array.
		int k = 0; //Loop counter for return array.
		//System.out.println("Array Lengths: L=" + arrLeft.length + " R=" + arrRight.length);
		while (i < arrLeft.length && j < arrRight.length) {
			//System.out.println("Nums: i=" + i + " j=" + j + " k=" + k);
			//System.out.println("Compare=" + arrLeft[i].getDateJoined().compareTo(arrRight[j].getDateJoined()));
			if(compFunc(arrLeft[i], arrRight[i]) > 0) {
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