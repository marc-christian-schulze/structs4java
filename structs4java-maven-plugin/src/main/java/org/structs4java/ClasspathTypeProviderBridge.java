//package org.structs4java;
//
//import java.util.Objects;
//
//import org.eclipse.emf.ecore.resource.ResourceSet;
//import org.eclipse.xtext.common.types.JvmType;
//import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
//import org.eclipse.xtext.common.types.access.impl.ClasspathTypeProvider;
//import org.eclipse.xtext.common.types.access.impl.IndexedJvmTypeAccess;
//import org.eclipse.xtext.common.types.access.impl.TypeResourceServices;
//
//import com.google.inject.Inject;
//
//public class ClasspathTypeProviderBridge extends ClasspathTypeProvider {
//
//	private IJvmTypeProvider original;
//	private IJvmTypeProvider alternativeProvider;
//
//	@Inject
//	public ClasspathTypeProviderBridge(ClassLoader classLoader, ResourceSet resourceSet,
//			IndexedJvmTypeAccess indexedJvmTypeAccess, TypeResourceServices services, IJvmTypeProvider original, IJvmTypeProvider alternativeProvider) {
//		super(classLoader, resourceSet, indexedJvmTypeAccess, services);
//		this.original = Objects.requireNonNull(original);
//		this.alternativeProvider = Objects.requireNonNull(alternativeProvider);
//	}
//
//	@Override
//	public JvmType findTypeByName(String name) {
//		System.out.println("ClasspathTypeProviderBridge::findTypeByName(name="+name+")");
//		JvmType typeByName = original.findTypeByName(name);
//		if (typeByName == null) {
//			typeByName = alternativeProvider.findTypeByName(name);
//		}
//		return typeByName;
//	}
//
//	@Override
//	public JvmType findTypeByName(String name, boolean binaryNestedTypeDelimiter) {
//		System.out.println("ClasspathTypeProviderBridge::findTypeByName(name="+name+",binaryNestedTypeDelimiter)");
//		JvmType typeByName = original.findTypeByName(name, binaryNestedTypeDelimiter);
//		if (typeByName == null) {
//			typeByName = alternativeProvider.findTypeByName(name, binaryNestedTypeDelimiter);
//		}
//		return typeByName;
//	}
//}
