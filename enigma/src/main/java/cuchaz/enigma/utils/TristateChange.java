package cuchaz.enigma.utils;

import java.util.Objects;

public final class TristateChange<T> {

	private static final TristateChange<?> UNCHANGED = new TristateChange<>(Type.UNCHANGED, null);
	private static final TristateChange<?> RESET = new TristateChange<>(Type.RESET, null);

	private final Type searchType;
	private final T val;

	@SuppressWarnings("unchecked")
	public static <T> TristateChange<T> unchanged() {
		return (TristateChange<T>) TristateChange.UNCHANGED;
	}

	@SuppressWarnings("unchecked")
	public static <T> TristateChange<T> reset() {
		return (TristateChange<T>) TristateChange.RESET;
	}

	public static <T> TristateChange<T> set(T value) {
		return new TristateChange<>(Type.SET, value);
	}

	private TristateChange(Type searchType, T val) {
		this.searchType = searchType;
		this.val = val;
	}

	public Type getType() {
		return this.searchType;
	}

	public boolean isUnchanged() {
		return this.searchType == Type.UNCHANGED;
	}

	public boolean isReset() {
		return this.searchType == Type.RESET;
	}

	public boolean isSet() {
		return this.searchType == Type.SET;
	}

	public T getNewValue() {
		if (this.searchType != Type.SET) throw new IllegalStateException(String.format("No concrete value in %s", this));
		return this.val;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TristateChange<?> that = (TristateChange<?>) o;
		return searchType == that.searchType &&
				Objects.equals(val, that.val);
	}

	@Override
	public int hashCode() {
		return Objects.hash(searchType, val);
	}

	@Override
	public String toString() {
		return String.format("TristateChange { searchType: %s, val: %s }", searchType, val);
	}

	public enum Type {
		UNCHANGED,
		RESET,
		SET,
	}

}
