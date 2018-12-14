package org.processmining.logskeleton.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class AllSet<T> implements Set<T> {

	private Map<T, Integer> posMap;
	private Map<T, Integer> posMap2;
	private Map<T, Integer> ttlMap;
	private int threshold;

	public AllSet(Collection<T> set, int threshold) {
		posMap = new HashMap<T, Integer>();
		posMap2 = new HashMap<T, Integer>();
		ttlMap = new HashMap<T, Integer>();
		for (T t : set) {
			posMap.put(t, 0);
			posMap2.put(t, 0);
			ttlMap.put(t, 0);
		}
		this.threshold = threshold; 
	}
	
	public int size() {
		int size = 0;
		for (T t : posMap.keySet()) {
			if (contains(t)) {
				size++;
			}
		}
		return size;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public boolean contains(Object o) {
//		System.out.println("[AllSet] contains " + posMap.get(o) + "/" + ttlMap.get(o) + "@" + threshold);
		return (posMap.get(o) * 100 >= ttlMap.get(o) * threshold);
	}

	public Iterator<T> iterator() {
		Set<T> set = new HashSet<T>();
		for (T t : posMap.keySet()) {
			if (contains(t)) {
				set.add(t);
			}
		}
		return set.iterator();
	}

	public Object[] toArray() {
		return null;
	}

	public <T> T[] toArray(T[] a) {
		return null;
	}

	public boolean add(T t) {
		return false;
	}

	public boolean remove(Object o) {
		return false;
	}

	public boolean containsAll(Collection<?> c) {
		Set<T> set = new HashSet<T>();
		for (T t : posMap.keySet()) {
			if (contains(t)) {
				set.add(t);
			}
		}
		return set.containsAll(c);
	}

	public boolean addAll(Collection<? extends T> c) {
		return retainAll(c);
	}

	public boolean retainAll(Collection<?> c) {
//		System.out.println("[AllSet] retainAll");
		for (T t : posMap.keySet()) {
			if (c.contains(t)) {
				posMap.put(t, posMap.get(t) + 1);
				posMap2.put(t, posMap2.get(t) + 1);
			}
			ttlMap.put(t, ttlMap.get(t) + 1);
		}
		return true;
	}

	public boolean removeAll(Collection<?> c) {
//		System.out.println("[AllSet] removeAll");
		for (T t : posMap.keySet()) {
			if (c.contains(t)) {
				posMap.put(t, 0); //-Math.abs(posMap.get(t)));
			}
		}
		return true;
	}

	public void clear() {
//		for (T t : posMap.keySet()) {
//			posMap.put(t, 0);
//			posMap2.put(t, 0);
//			ttlMap.put(t, 0);
//		}
	}

	public void reset() {
//		System.out.println("[AllSet] do reset " + posMap);
		for (T t : posMap.keySet()) {
			posMap.put(t, posMap2.get(t));
		}
//		System.out.println("[AllSet] did reset " + posMap);
	}
	
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}
	
	public int getThreshold() {
		return threshold;
	}
	
	public int getMaxThreshold(Object o) {
		return posMap.get(o) * 100 / ttlMap.get(o);
	}
}
