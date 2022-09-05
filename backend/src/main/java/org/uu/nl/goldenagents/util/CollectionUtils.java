package org.uu.nl.goldenagents.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class CollectionUtils {
	
	private CollectionUtils() {}
	
	public static enum SortingOrder {ASCENDING, DESCENDING};
	
	public static <T> Set<T> intersect(Collection<T> base, Collection<T> other) {
		Set<T> intersection = new HashSet<>(base);
		intersection.retainAll(other);
		return intersection;
	}
	
	public static <T> Set<T> union(Collection<T> set1, Collection<T> set2) {
		Set<T> union = new HashSet<>(set1);
		union.addAll(set2);
		return union;
	}
	
	public static <T> Set<T> jointRemainAll(Collection<Set<T>> linksets, Set<T> base, Set<T> other){
		Set<T> items = new HashSet<>();
		linksets.stream().forEach(set -> {
			T item = bothContains(set, base, other);
			if(item != null) {
				items.add(item);
			}
		});
		return items;
	}
	
	/**
	 * Check if linkset includes an both an item in base set and the other set.
	 * If yes, returns the item in the base, otherwise null;   
	 * @param <T>
	 * @param linkset
	 * @param base
	 * @param other
	 * @return
	 */
	private static <T> T bothContains(Set<T> linkset, Set<T> base, Set<T> other) {
		T item = null;
		boolean isInOther = false;
		for(T el : linkset) {
			if(base.contains(el)) {
				item = el;
			}
			isInOther = isInOther || other.contains(el);
		}
		if(isInOther) {
			return item;
		}
		return null;
	}
	
	public static <T> String printify(Collection<T> set, String seperator) {
		return set.stream().map(T::toString)
				.collect(Collectors.joining(seperator));
	}
	
	public static <K, V extends Comparable<? super V>> List<Entry<K, V>> sortByValue(Map<K, V> map, SortingOrder o) {
        List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
        switch (o) {
		case DESCENDING:
			list.sort(Entry.comparingByValue(Comparator.reverseOrder()));
			break;
		default:
			list.sort(Entry.comparingByValue());
			break;
		}
        return list;
    }
	
	public static <K, V extends Comparable<? super V>> K getKeyOfMaxValue(Map<K, V> map){
		return sortByValue(map, SortingOrder.DESCENDING).iterator().next().getKey();
	}
	
	public static <K, V extends Comparable<? super V>> K getKeyOfMinValue(Map<K, V> map){
		return sortByValue(map, SortingOrder.ASCENDING).iterator().next().getKey();
	}
}
