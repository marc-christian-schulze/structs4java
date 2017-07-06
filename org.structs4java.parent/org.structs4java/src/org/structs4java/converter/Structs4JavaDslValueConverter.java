package org.structs4java.converter;

import org.eclipse.xtext.common.services.DefaultTerminalConverters;
import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverter;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter;
import org.eclipse.xtext.conversion.impl.INTValueConverter;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.util.Strings;

public class Structs4JavaDslValueConverter extends DefaultTerminalConverters {

	@Override
	@ValueConverter(rule = "INT")
	public IValueConverter<Integer> INT() {
		return new INTValueConverter();
	}

	@ValueConverter(rule = "LONG")
	public IValueConverter<Long> LONG() {
		return new LONGValueConverter();
	}

	private static final class INTValueConverter extends AbstractLexerBasedConverter<Integer> {
		@Override
		public Integer toValue(String string, INode node) {
			if (Strings.isEmpty(string))
				throw new ValueConverterException("Couldn't convert empty string to an int value.", node, null);

			try {
				return Integer.decode(string);
			} catch (NumberFormatException e) {
				throw new ValueConverterException("Couldn't convert empty string to an int value.", node, e);
			}
		}
	}

	private static final class LONGValueConverter extends AbstractLexerBasedConverter<Long> {
		@Override
		public Long toValue(String string, INode node) {
			if (Strings.isEmpty(string))
				throw new ValueConverterException("Couldn't convert empty string to an long value.", node, null);

			try {
				return Long.decode(string);
			} catch (NumberFormatException e) {
				throw new ValueConverterException("Couldn't convert empty string to an long value.", node, e);
			}
		}
	}
}
