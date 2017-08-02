package org.structs4java;

import org.eclipse.xtext.naming.DefaultDeclarativeQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.structs4java.structs4JavaDsl.EnumDeclaration;
import org.structs4java.structs4JavaDsl.StructDeclaration;
import org.structs4java.structs4JavaDsl.StructsFile;

public class Structs4JavaQualifiedNameProvider extends DefaultDeclarativeQualifiedNameProvider{
	
//  QualifiedName qualifiedName(StructsFile file) {
//	  return null;
//  }
//
//	 
//    QualifiedName qualifiedName(StructDeclaration struct) {
//    	StructsFile file = (StructsFile) struct.eContainer();
//    	if(file.getName() != null) {
//    		return QualifiedName.create(file.getName(), struct.getName());
//    	} else {
//    		return QualifiedName.create(struct.getName());
//    	}
//    }
//    
//    QualifiedName qualifiedName(EnumDeclaration _enum) {
//    	try {
//    		System.out.println("--->");
//    	StructsFile file = (StructsFile) _enum.eContainer();
//    	if(file.getName() != null) {
//    		System.out.println("qualifiedName(package: "+file.getName()+", enum: " + _enum.getName() + ")");
//    		System.out.println("return " + QualifiedName.create(file.getName(), _enum.getName()));
//    		return QualifiedName.create(file.getName(), _enum.getName());
//    	} else {
//    		System.out.println("qualifiedName(enum: " + _enum.getName() + ")");
//    		System.out.println("return " + QualifiedName.create(_enum.getName()));
//    		return QualifiedName.create(_enum.getName());
//    	}
//    	}finally {
//    		System.out.println("<---");
//    	}
//    }
 
}