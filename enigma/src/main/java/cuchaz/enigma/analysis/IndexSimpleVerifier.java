package cuchaz.enigma.analysis;

import cuchaz.enigma.Enigma;
import cuchaz.enigma.analysis.index.EntryIndex;
import cuchaz.enigma.analysis.index.InheritanceIndex;
import cuchaz.enigma.translation.representation.AccessFlags;
import cuchaz.enigma.translation.representation.entry.ClassDefEntry;
import cuchaz.enigma.translation.representation.entry.ClassEntry;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.SimpleVerifier;

import java.util.Set;

public class IndexSimpleVerifier extends SimpleVerifier {
	private static final Type OBJECT_TYPE = Type.getType("Ljava/lang/Object;");
	private final EntryIndex entryIndex;
	private final InheritanceIndex inheritanceIndex;

	public IndexSimpleVerifier(EntryIndex entryIndex, InheritanceIndex inheritanceIndex) {
		super(Enigma.ASM_VERSION, null, null, null, false);
		this.entryIndex = entryIndex;
		this.inheritanceIndex = inheritanceIndex;
	}

	@Override
	protected boolean isSubTypeOf(BasicValue value, BasicValue expected) {
		Type expectedType = expected.getType();
		Type type = value.getType();
		switch (expectedType.getSort()) {
			case Type.INT, Type.FLOAT, Type.LONG, Type.DOUBLE -> {
				return type.equals(expectedType);
			}
			case Type.ARRAY, Type.OBJECT -> {
				if (type.equals(NULL_TYPE)) {
					return true;
				} else if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
					if (this.isAssignableFrom(expectedType, type)) {
						return true;
					} else if (this.isInterface(expectedType)) {
						return this.isAssignableFrom(OBJECT_TYPE, type);
					} else {
						return false;
					}
				} else {
					return false;
				}
			}
			default -> throw new AssertionError();
		}
	}

	@Override
	protected boolean isInterface(Type type) {
		AccessFlags classAccess = this.entryIndex.getClassAccess(new ClassEntry(type.getInternalName()));
		if (classAccess != null) {
			return classAccess.isInterface();
		}

		Class<?> clazz = this.getClass(type);
		if (clazz != null) {
			return clazz.isInterface();
		}

		return false;
	}

	@Override
	protected Type getSuperClass(Type type) {
		ClassDefEntry definition = this.entryIndex.getDefinition(new ClassEntry(type.getInternalName()));
		if (definition != null) {
			return Type.getType('L' + definition.getSuperClass().getFullName() + ';');
		}

		Class<?> clazz = this.getClass(type);
		if (clazz != null) {
			return Type.getType(clazz.getSuperclass());
		}

		return OBJECT_TYPE;
	}

	@Override
	protected boolean isAssignableFrom(Type type1, Type type2) {
		if (type1.equals(type2)) {
			return true;
		}

		if (type2.equals(NULL_TYPE)) {
			return true;
		}

		if (type1.getSort() == Type.ARRAY) {
			return type2.getSort() == Type.ARRAY && this.isAssignableFrom(Type.getType(type1.getDescriptor().substring(1)), Type.getType(type2.getDescriptor().substring(1)));
		}

		if (type2.getSort() == Type.ARRAY) {
			return type1.equals(OBJECT_TYPE);
		}

		if (type1.getSort() == Type.OBJECT && type2.getSort() == Type.OBJECT) {
			if (type1.equals(OBJECT_TYPE)) {
				return true;
			}

			ClassEntry class1 = new ClassEntry(type1.getInternalName());
			ClassEntry class2 = new ClassEntry(type2.getInternalName());

			if (this.entryIndex.hasClass(class1) && this.entryIndex.hasClass(class2)) {
				return this.inheritanceIndex.getAncestors(class2).contains(class1);
			}

			Class<?> class1Class = this.getClass(Type.getType('L' + class1.getFullName() + ';'));
			Class<?> class2Class = this.getClass(Type.getType('L' + class2.getFullName() + ';'));

			if (class1Class == null) {
				return true; // missing classes to find out
			}

			if (class2Class != null) {
				return class1Class.isAssignableFrom(class2Class);
			}

			if (this.entryIndex.hasClass(class2)) {
				Set<ClassEntry> ancestors = this.inheritanceIndex.getAncestors(class2);

				for (ClassEntry ancestorEntry : ancestors) {
					Class<?> ancestor = this.getClass(Type.getType('L' + ancestorEntry.getFullName() + ';'));
					if (ancestor == null || class1Class.isAssignableFrom(ancestor)) {
						return true; // assignable, or missing classes to find out
					}
				}

				return false;
			}

			return true; // missing classes to find out
		}

		return false;
	}

	@Override
	protected final Class<?> getClass(Type type) {
		try {
			return Class.forName(type.getSort() == Type.ARRAY ? type.getDescriptor().replace('/', '.') : type.getClassName(), false, null);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
}
