package es.uvigo.ei.sing.pubdown.util;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

import java.util.List;
import java.util.stream.Stream;

/**
 * Utility class with several methods to compare object colections
 */
public final class CompareCollections {
	private CompareCollections() {
	}

	public static boolean equalsIgnoreOrder(final Iterable<?> i1, final Iterable<?> i2) {
		if (i1 == null && i2 == null) {
			return true;
		} else if (i1 == null || i2 == null) {
			return false;
		} else {
			final List<?> l1 = stream(i1.spliterator(), false).collect(toList());

			return stream(i2.spliterator(), false).allMatch(l1::remove) && l1.isEmpty();
		}
	}

	public static boolean equalsIgnoreOrder(final Object[] i1, final Object[] i2) {
		if (i1 == null && i2 == null) {
			return true;
		} else if (i1 == null || i2 == null) {
			return false;
		} else {
			final List<?> l1 = Stream.of(i1).collect(toList());

			return Stream.of(i2).allMatch(l1::remove) && l1.isEmpty();
		}
	}
}
