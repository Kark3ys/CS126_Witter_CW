import java.util.function.*;

public final class JavaGenericBullshitAvoider {
	//Move all the repeated array shuffling 
	//Lambda for Life.
	private JavaGenericBullshitAvoider() {
		int i = 0;	//Some filler
	}
	
	/*public interface MidFunc {
	//All mid funcs return object arrays.
		public Object[] method(Object[] arrObj);
	}*/
	
	/*public interface MidFuncExtra {
	//For the extra cases where we also pass a cutoff length.
		public Object[] method(Object[] arrObj, int len);
	}*/
	
	public static <T> void justDoIt(Function<Object[], Object[]> mf, T[] target) {
		Object[] arrObj = new Object[target.length];
		int i = 0;
		for (i = 0; i < arrObj.length; i++)
			arrObj[i] = (Object) target[i];
		arrObj = mf.apply(arrObj);
		for (i = 0; i < arrObj.length; i++)
			target[i] = (T) arrObj[i];
	}
}
		