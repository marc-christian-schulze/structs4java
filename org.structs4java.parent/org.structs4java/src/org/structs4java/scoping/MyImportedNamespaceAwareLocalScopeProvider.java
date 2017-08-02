package org.structs4java.scoping;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.scoping.impl.ImportNormalizer;
import org.eclipse.xtext.scoping.impl.ImportedNamespaceAwareLocalScopeProvider;
import org.structs4java.structs4JavaDsl.StructsFile;

/**
 * @see https://nittka.github.io/2011/08/07/scoping3.html
 * @author azrael
 *
 */
public class MyImportedNamespaceAwareLocalScopeProvider extends ImportedNamespaceAwareLocalScopeProvider {

	@Override
	protected List<ImportNormalizer> getImportedNamespaceResolvers(EObject context, boolean ignoreCase) {
		List<ImportNormalizer> resolvers = super.getImportedNamespaceResolvers(context, ignoreCase);
		boolean wildcard = true;
		resolvers.add(new ImportNormalizer(getPackageName(context), wildcard, ignoreCase));
		return resolvers;
	}

	private QualifiedName getPackageName(EObject eobject) {
		if (eobject instanceof StructsFile) {
			StructsFile file = (StructsFile) eobject;
			if (file.getName() == null) {
				return QualifiedName.create("");
			}
			return QualifiedName.create(file.getName().split("\\."));
		}
		return getPackageName(eobject.eContainer());
	}
}
