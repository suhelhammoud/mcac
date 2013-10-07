package weka.classifiers.rules.mcac.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.primitives.Ints;

public class ColumnID implements Comparable<ColumnID> {

	final public static ColumnID ZERO = of();

	public static List<Integer> getAtomics(ColumnID col) {
		List<Integer> result = new ArrayList<>(col.size());
		for (int id : col.ids) {
			result.add(id);
		}
		return result;
	}

	private static int premutations(int d) {
		return d * (d - 1) / 2;
	}

	/**
	 * Optimized method which runs on parallel threads
	 * 
	 * @param ids
	 *            : columnId of certain order
	 * @return collection of possible columnIds of one step more higher order
	 */
	public static List<ColumnID> combined(final List<ColumnID> ids) {
		final Multiset<ColumnID> counterSet = HashMultiset.create();

		if (ids.size() == 0)
			return new ArrayList<>();

		final int outSize = ids.get(0).size() + 1;
		final int outCount = premutations(outSize);

		int nrOfProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(nrOfProcessors);

		for (int i = 0; i < ids.size() - 1; i++) {
			for (int j = i + 1; j < ids.size(); j++) {

				final int first = i, second = j;
				exec.execute(new Runnable() {

					@Override
					public void run() {
						ColumnID id = join(ids.get(first), ids.get(second));
						if (id.size() == outSize) {
							synchronized (counterSet) {
								counterSet.add(id);
							}
						}

					}
				});
			}
		}
		exec.shutdown();
		try {
			boolean okDoneAllTasks = exec.awaitTermination(1800,
					TimeUnit.SECONDS);// TODO adjust timing
			assert okDoneAllTasks;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		List<ColumnID> result = new ArrayList<>();

		for (ColumnID columnID : counterSet.elementSet()) {
			if (counterSet.count(columnID) == outCount)
				result.add(columnID);
		}
		return result;
	}

	public ColumnID dropFirst() {
		if (size() < 2)
			return ZERO;
		return of(Arrays.copyOfRange(ids, 1, ids.length));
	};

	public ColumnID dropLast() {
		if (size() < 2)
			return ZERO;
		return of(Arrays.copyOf(ids, ids.length - 1));
	}

	public static ColumnID join(ColumnID col1, ColumnID col2) {
		if (col1.size() != col2.size())
			return ZERO;
		if (col1.equals(ZERO))
			return ZERO;

		Set<Integer> set1 = col1.asSet();
		Set<Integer> set2 = col2.asSet();

		set1.addAll(set2);
		if (set1.size() != set2.size() + 1)
			return ZERO;

		return of(set1);
	}

	public static ColumnID of(Collection<Integer> ids) {
		return of(Ints.toArray(ids));
	}

	public static ColumnID of(int... ids) {
		return new ColumnID(ids);
	}

	final private int[] ids;
	final int hashcode;

	public ColumnID(int... ids) {
		int[] sortedID = Arrays.copyOf(ids, ids.length);

		this.ids = sortedID;
		Arrays.sort(sortedID);

		// Be carefull, guava Objects.hashCode() is not suitable here
		hashcode = Arrays.hashCode(sortedID);
	}

	public int size() {
		return ids.length;
	}

	public int get(int i) {
		return ids[i];
	}

	@Override
	public int hashCode() {
		return hashcode;
	}

	@Override
	public String toString() {
		return "[" + Joiner.on(", ").join(this.asList()) + "]";
	}

	/**
	 * 
	 * @return first none zero id
	 */
	public int getAtomic() {
		return ids[0];
	}

	public List<Integer> asList() {
		List<Integer> result = new ArrayList<>(size());
		for (int i = 0; i < size(); i++) {
			result.add(get(i));
		}
		return result;
	}

	public Set<Integer> asSet() {
		Set<Integer> result = new HashSet<>();
		for (int i = 0; i < size(); i++) {
			result.add(get(i));
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return Arrays.equals(this.ids, ((ColumnID)obj).ids);
//		
//		if (obj == null)
//			return false;
//		if (this == obj)
//			return true;
//
//		if (!(obj instanceof ColumnID))
//			return false;
//
//		ColumnID that = (ColumnID) obj;
//
//		if (this.size() != that.size())
//			return false;
//		if (this.size() == 0 && that.size() == 0)
//			return true;
//
//		for (int i = 0; i < size(); i++) {
//			if (this.get(i) != that.get(i))
//				return false;
//		}
//		return true;
	}

	public static void main(String[] args) {
		ColumnID col1 = of(1, 2, 4, 5);
		ColumnID col2 = of(1, 4, 2, 5);
		System.out.println(col1.size());

		Set<ColumnID> set = new HashSet<>();
		set.add(col1);
		set.add(col2);

		int hash1 = Arrays.hashCode(col1.ids);
		int hash2 = Arrays.hashCode(col2.ids);

		System.out.println(Arrays.equals(col1.ids, col2.ids));
		System.out.println(hash1 == hash2);

		System.out.println("Set =" + set);
		// ColumnID col2 = of(3,11, 2, 4);
		// System.out.println(col1);
		// System.out.println(col2);
		// System.out.println(join(col1, col2));
	}

	@Override
	public int compareTo(ColumnID other) {
		int diff = ids.length - other.ids.length;
		if (diff != 0)
			return diff;

		int i = 0;
		while (i < ids.length && i < other.ids.length)
			if (ids[i] == other.ids[i])
				continue;
			else
				return ids[i] - other.ids[i];

		return 0;
	}

}
