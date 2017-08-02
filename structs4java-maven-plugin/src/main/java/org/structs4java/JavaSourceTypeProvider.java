package org.structs4java;

import java.util.Objects;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;

public class JavaSourceTypeProvider implements IJvmTypeProvider {
	
	private ResourceSet resourceSet;
	
	public JavaSourceTypeProvider(ResourceSet resourceSet) {
		this.resourceSet = Objects.requireNonNull(resourceSet);
	}

	@Override
	public JvmType findTypeByName(String name) {
		System.out.println("JavaSourceTypeProvider::findTypeByName(name="+name+")");
		for(Resource res : resourceSet.getResources()) {
			System.out.println(res);
		}
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JvmType findTypeByName(String name, boolean binaryNestedTypeDelimiter) {
		return findTypeByName(name);
	}

	@Override
	public ResourceSet getResourceSet() {
		return resourceSet;
	}

}
