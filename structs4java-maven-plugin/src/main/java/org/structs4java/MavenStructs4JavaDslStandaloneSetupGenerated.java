package org.structs4java;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class MavenStructs4JavaDslStandaloneSetupGenerated extends Structs4JavaDslStandaloneSetupGenerated {

	@Override
	public Injector createInjector() {
		return Guice.createInjector(new MavenStructs4JavaDslRuntimeModule());
	}
}
