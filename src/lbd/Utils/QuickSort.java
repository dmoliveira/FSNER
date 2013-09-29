package lbd.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class QuickSort {

	public static final Random RND = new Random();

	@SuppressWarnings("rawtypes")
	private static List objectList;

	@SuppressWarnings("unchecked")
	private static <E extends Comparable<? super E>, T> void swap(E[] array, int i, int j) {

		E tmp = array[i];
		array[i] = array[j];
		array[j] = tmp;

		if(objectList != null) {
			T tmp2 = (T) objectList.get(i);
			objectList.set(i, objectList.get(j));
			objectList.set(j, tmp2);
		}
	}

	private static <E extends Comparable<? super E>> int partition(E[] array, int begin, int end) {
		int index = begin + RND.nextInt(end - begin + 1);
		E pivot = array[index];
		swap(array, index, end);
		for (int i = index = begin; i < end; ++i) {
			if (array[i].compareTo(pivot) <= 0) {
				swap(array, index++, i);
			}
		}
		swap(array, index, end);
		return (index);
	}

	private static <E extends Comparable<? super E>> void qsort(E[] array, int begin, int end) {
		if (end > begin) {
			int index = partition(array, begin, end);
			qsort(array, begin, index - 1);
			qsort(array, index + 1, end);
		}
	}

	public static <E extends Comparable<? super E>, T> void sort(E[] array, ArrayList<T> objectList) {

		QuickSort.objectList = objectList;
		qsort(array, 0, array.length - 1);
	}

	// Example
	public static void main(String[] args) {

		// Sort integers
		Integer[] l1 = { 5, 1024, 1, 88, 0, 1024 };
		System.out.println("l1  start:" + Arrays.toString(l1));
		QuickSort.sort(l1, null);
		System.out.println("l1 sorted:" + Arrays.toString(l1));

		// Sort Strings
		String[] l2 = { "gamma", "beta", "alpha", "zoolander" };
		System.out.println("l2  start:" + Arrays.toString(l2));
		QuickSort.sort(l2, null);
		System.out.println("l2 sorted:" + Arrays.toString(l2));
	}
}
