package org.structs4java.converter;

import org.eclipse.xtext.common.services.DefaultTerminalConverters;
import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverter;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.INTValueConverter;
import org.eclipse.xtext.nodemodel.INode;

public class Structs4JavaDslValueConverter extends DefaultTerminalConverters {

	@Override
	@ValueConverter(rule = "INT")
	public IValueConverter<Integer> INT() {
		return new HexINTValueConverter();
	}

	private static final class HexINTValueConverter extends INTValueConverter {
		@Override
		public Integer toValue(String string, INode node) {
			if (string.startsWith("0x")) {
				try {
					int intValue = Integer.parseInt(string.substring(2), 16);
					return Integer.valueOf(intValue);
				} catch (NumberFormatException e) {
					throw new ValueConverterException("Couldn't convert '" + string + "' to an int value.", node, e);
				}
			}
			return super.toValue(string, node);
		}
	}
}
