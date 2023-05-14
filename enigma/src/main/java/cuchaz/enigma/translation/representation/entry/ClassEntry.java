package cuchaz.enigma.translation.representation.entry;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import cuchaz.enigma.source.RenamableTokenType;
import cuchaz.enigma.translation.TranslateResult;
import cuchaz.enigma.translation.Translator;
import cuchaz.enigma.translation.mapping.EntryMapping;
import cuchaz.enigma.translation.mapping.IdentifierValidation;
import cuchaz.enigma.translation.representation.TypeDescriptor;
import cuchaz.enigma.utils.validation.Message;
import cuchaz.enigma.utils.validation.ValidationContext;

public class ClassEntry extends ParentedEntry<ClassEntry> implements Comparable<ClassEntry> {
	private final String fullName;
	private final String localPrefix;

	public ClassEntry(String className) {
		this(getOuterClass(className), getInnerName(className), null);
	}

	public ClassEntry(@Nullable ClassEntry parent, String className) {
		this(parent, className, null);
	}

	public ClassEntry(@Nullable ClassEntry parent, String className, @Nullable String javadocs) {
		super(parent, className, javadocs);

		if (parent != null) {
			// local classes have a number prefix
			int i = 0;
			while (i < name.length() && Character.isDigit(name.charAt(i))) {
				i++;
			}
			// if entire inner name is a number, this class is anonymous, not local
			if (i == name.length()) {
				i = 0;
			}
			fullName = parent.getFullName() + "$" + name;
			localPrefix = name.substring(0, i);
		} else {
			fullName = name;
			localPrefix = "";
		}

		if (parent == null && className.indexOf('.') >= 0) {
			throw new IllegalArgumentException("Class name must be in JVM format. ie, path/to/package/class$inner : " + className);
		}
	}

	@Override
	public Class<ClassEntry> getParentType() {
		return ClassEntry.class;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getSimpleName() {
		int packagePos = this.name.lastIndexOf('/');
		if (packagePos > 0) {
			return this.name.substring(packagePos + 1);
		}

		return name.substring(localPrefix.length());
	}

	@Override
	public String getFullName() {
		return this.fullName;
	}

	@Override
	public String getSourceRemapName() {
		return this.getSimpleName();
	}

	@Override
	public String getContextualName() {
		if (this.isInnerClass()) {
			return this.parent.getSimpleName() + "$" + this.name;
		}

		return this.getSimpleName();
	}

	@Override
	public String getNamePrefix() {
		return localPrefix;
	}

	@Override
	public TranslateResult<? extends ClassEntry> extendedTranslate(Translator translator, @Nonnull EntryMapping mapping) {
		if (this.name.charAt(0) == '[') {
			TranslateResult<TypeDescriptor> translatedName = translator.extendedTranslate(new TypeDescriptor(this.name));
			return translatedName.map(desc -> new ClassEntry(this.parent, desc.toString()));
		}

		String translatedName = mapping.targetName() != null ? mapping.targetName() : this.name;
		String docs = mapping.javadoc();
		return TranslateResult.of(
				mapping.targetName() == null ? RenamableTokenType.OBFUSCATED : RenamableTokenType.DEOBFUSCATED,
				new ClassEntry(this.parent, translatedName, docs)
		);
	}

	@Override
	public ClassEntry getContainingClass() {
		return this;
	}

	@Override
	public int hashCode() {
		return this.fullName.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof ClassEntry entry && this.equals(entry);
	}

	public boolean equals(ClassEntry other) {
		return other != null && Objects.equals(this.parent, other.parent) && this.name.equals(other.name);
	}

	@Override
	public boolean canConflictWith(Entry<?> entry, Predicate<Entry<?>> isStatic) {
		return true;
	}

	@Override
	public boolean canShadow(Entry<?> entry) {
		return false;
	}

	@Override
	public void validateName(ValidationContext vc, String name) {
		if (!name.startsWith(localPrefix)) {
			vc.raise(Message.MISSING_LOCAL_PREFIX, name, localPrefix);
		}
		IdentifierValidation.validateClassName(vc, name.substring(localPrefix.length()), this.isInnerClass());
	}

	@Override
	public ClassEntry withName(String name) {
		return new ClassEntry(this.parent, name, this.javadocs);
	}

	@Override
	public ClassEntry withParent(ClassEntry parent) {
		return new ClassEntry(parent, this.name, this.javadocs);
	}

	@Override
	public String toString() {
		return this.getFullName();
	}

	public String getPackageName() {
		return getParentPackage(this.fullName);
	}

	/**
	 * Returns whether this class entry has a parent, and therefore is an inner class.
	 */
	public boolean isInnerClass() {
		return this.parent != null;
	}

	@Nullable
	public ClassEntry getOuterClass() {
		return this.parent;
	}

	@Nonnull
	public ClassEntry getOutermostClass() {
		if (this.parent == null) {
			return this;
		}

		return this.parent.getOutermostClass();
	}

	public ClassEntry buildClassEntry(List<ClassEntry> classChain) {
		assert (classChain.contains(this));
		StringBuilder buf = new StringBuilder();
		for (ClassEntry chainEntry : classChain) {
			if (buf.length() == 0) {
				buf.append(chainEntry.getFullName());
			} else {
				buf.append("$");
				buf.append(chainEntry.getSimpleName());
			}

			if (chainEntry == this) {
				break;
			}
		}

		return new ClassEntry(buf.toString());
	}

	public boolean isJre() {
		String packageName = this.getPackageName();
		return packageName != null && (packageName.startsWith("java/") || packageName.startsWith("javax/"));
	}

	public static String getParentPackage(String name) {
		int pos = name.lastIndexOf('/');
		if (pos > 0) {
			return name.substring(0, pos);
		}

		return null;
	}

	public static String getNameInPackage(String name) {
		int pos = name.lastIndexOf('/');

		if (pos == name.length() - 1) {
			return "(empty)";
		}

		if (pos > 0) {
			return name.substring(pos + 1);
		}

		return name;
	}

	@Nullable
	public static ClassEntry getOuterClass(String name) {
		if (name.charAt(0) == '[') {
			return null;
		}

		int index = name.lastIndexOf('$');
		if (index >= 0) {
			return new ClassEntry(name.substring(0, index));
		}

		return null;
	}

	public static String getInnerName(String name) {
		if (name.charAt(0) == '[') {
			return name;
		}

		int innerClassPos = name.lastIndexOf('$');
		if (innerClassPos > 0) {
			return name.substring(innerClassPos + 1);
		}

		return name;
	}

	@Override
	public int compareTo(ClassEntry entry) {
		String name = this.getFullName();
		String otherFullName = entry.getFullName();

		if (name.length() != otherFullName.length()) {
			return name.length() - otherFullName.length();
		}

		return name.compareTo(otherFullName);
	}
}
