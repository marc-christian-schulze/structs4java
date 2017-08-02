//package org.structs4java;
//
//import org.eclipse.emf.ecore.resource.ResourceSet;
//import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
//import org.eclipse.xtext.common.types.access.ClasspathTypeProviderFactory;
//import org.eclipse.xtext.common.types.access.impl.ClasspathTypeProvider;
//import org.eclipse.xtext.common.types.access.impl.TypeResourceServices;
//
//import com.google.inject.Inject;
//
//public class ClasspathTypeProviderFactoryBridge extends ClasspathTypeProviderFactory {
//
//	@Inject
//	public ClasspathTypeProviderFactoryBridge(ClassLoader classLoader, TypeResourceServices services) {
//		super(classLoader, services);
//	}
//
//	@Override
//	public ClasspathTypeProvider createTypeProvider() {
//		ResourceSetImpl resourceSet = new ResourceSetImpl();
//		ClasspathTypeProvider original = super.createTypeProvider();
//		return new ClasspathTypeProviderBridge(getClassLoader(resourceSet), resourceSet, getIndexedJvmTypeAccess(),
//				services, original, new JavaSourceTypeProvider(resourceSet));
//	}
//
//	@Override
//	public ClasspathTypeProvider createTypeProvider(ResourceSet resourceSet) {
//		ClasspathTypeProvider original = super.createTypeProvider();
//		return new ClasspathTypeProviderBridge(getClassLoader(resourceSet), resourceSet, getIndexedJvmTypeAccess(),
//				services, original, new JavaSourceTypeProvider(resourceSet));
//	}
//}
