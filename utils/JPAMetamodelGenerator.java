/*
 * Spark - The inventory management application
 * Copyright (C) 2026 Yegore Vlussove
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JPAMetamodelGenerator {

	public record FieldRecord(String type, String name, String collectionType) { }

	static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("No entity class specfied");
			return;
		}
		String entityClass = args[0];
		if (!entityClass.endsWith(".java")) {
			System.out.println("Entity class must be a java class");
			return;
		}

		Path entityClassPath = Path.of(entityClass);
		Path parentDir = entityClassPath.getParent();
		String simpleName = entityClassPath.getName(entityClassPath.getNameCount() - 1).toString();
		String taillessName = simpleName.substring(0, simpleName.length() - ".java".length());

		String entityClassContent = Files.readString(entityClassPath);
		Pattern packagePattern = Pattern.compile("\\npackage [^\\n]+;");
		Matcher packageMatcher = packagePattern.matcher(entityClassContent);
		if (!packageMatcher.find()) {
			System.out.println("No package");
		}
		String packageLocation = entityClassContent.substring(packageMatcher.start(), packageMatcher.end()).trim();

		Pattern fieldPattern = Pattern.compile("\\n\\t((private|public|protected|) )?[A-Za-z0-9<> ,]+ [A-Za-z0-9]+;");
		Matcher fieldMatcher = fieldPattern.matcher(entityClassContent);
		ArrayList<FieldRecord> fields = new ArrayList<>();
		while(fieldMatcher.find()) {
			String foundField = entityClassContent.substring(fieldMatcher.start(), fieldMatcher.end()).trim();
			if (
				foundField.startsWith("private")	||
				foundField.startsWith("public")		||
				foundField.startsWith("protected")
			) {
				foundField = foundField.substring(foundField.indexOf(' ') + 1);
			}
			String fieldType = foundField.substring(0, foundField.indexOf(' '));
			String fieldName = foundField.substring(foundField.indexOf(' ') + 1, foundField.indexOf(';'));
			if (fieldType.equals("int")) {
				fields.add(new FieldRecord("Integer", fieldName, null));
			} else if (fieldType.equals("long")) {
				fields.add(new FieldRecord("Long", fieldName, null));
			} else if (fieldType.equals("double")) {
				fields.add(new FieldRecord("Double", fieldName, null));
			} else if (fieldType.equals("boolean")) {
				fields.add(new FieldRecord("Boolean", fieldName, null));
			} else if (fieldType.startsWith("Collection<")) {
				fieldType = fieldType.substring(fieldType.indexOf('<') + 1, fieldType.indexOf('>'));
				fields.add(new FieldRecord(fieldType, fieldName, "Collection"));
			} else if (fieldType.startsWith("Set<")) {
				fieldType = fieldType.substring(fieldType.indexOf('<') + 1, fieldType.indexOf('>'));
				fields.add(new FieldRecord(fieldType, fieldName, "Set"));
			} else if (fieldType.startsWith("List<")) {
				fieldType = fieldType.substring(fieldType.indexOf('<') + 1, fieldType.indexOf('>'));
				fields.add(new FieldRecord(fieldType, fieldName, "List"));
			} else if (fieldType.startsWith("Map<")) {
				fieldType = fieldType.substring(fieldType.indexOf('<') + 1, fieldType.indexOf('>'));
				fields.add(new FieldRecord(fieldType, fieldName, "Map"));
			} else {
				fields.add(new FieldRecord(fieldType, fieldName, null));
			}
		}

		try (Writer writer = Files.newBufferedWriter(
			Path.of(parentDir.toString(), taillessName + "_.java"),
			StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
		) {
			writer
				.append(gplBoilerplate).append("\n")
				.append(packageLocation).append("\n\n")
				.append(imports).append("\n")
				.append("@StaticMetamodel(").append(taillessName).append(".class)").append("\n")
				.append("public class ").append(taillessName).append("_").append(" {").append("\n")
				.append("\tpublic static volatile EntityType<").append(taillessName).append("> class_;").append("\n\n");

			for (FieldRecord f: fields) {
				StringBuilder propertyDeclarationField = new StringBuilder();
				for (int i = 0; i < f.name().length(); i++) {
					char c = f.name().charAt(i);
					if (Character.isUpperCase(c)) {
						propertyDeclarationField.append("_").append(c);
					} else {
						propertyDeclarationField.append(Character.toUpperCase(c));
					}
				}
				writer
					.append("\tpublic static volatile String ")
					.append(propertyDeclarationField)
					.append(" = \"")
					.append(f.name())
					.append("\";\n");
			}
			writer.append("\n");

			for (FieldRecord f: fields) {
				writer.append("\tpublic static volatile ");
				if (f.collectionType() == null) {
					writer
						.append("SingularAttribute<")
						.append(taillessName)
						.append(", ")
						.append(f.type())
						.append("> ")
						.append(f.name())
						.append(";\n");
				} else {
					writer
						.append(f.collectionType())
						.append("Attribute<")
						.append(taillessName)
						.append(", ")
						.append(f.type())
						.append("> ")
						.append(f.name())
						.append(";\n");
				}
			}

			writer.append("}\n");
		}
	}

	static final String gplBoilerplate = """
		/*
		 * Spark - The inventory management application
		 * Copyright (C) 2026 Yegore Vlussove
		 *
		 * This program is free software: you can redistribute it and/or modify
		 * it under the terms of the GNU General Public License as published by
		 * the Free Software Foundation; either version 3 of the License, or
		 * (at your option) any later version.
		 *
		 * This program is distributed in the hope that it will be useful,
		 * but WITHOUT ANY WARRANTY; without even the implied warranty of
		 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
		 * GNU General Public License for more details.
		 *
		 * You should have received a copy of the GNU General Public License
		 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
		 */
		""";

	static final String imports = """
		import jakarta.persistence.metamodel.*;
		""";
}