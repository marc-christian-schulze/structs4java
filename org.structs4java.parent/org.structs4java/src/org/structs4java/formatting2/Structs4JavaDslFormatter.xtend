/*
 * generated by Structs4Java 2.10.0
 */
package org.structs4java.formatting2

import org.eclipse.xtext.formatting2.AbstractFormatter2
import org.eclipse.xtext.formatting2.IFormattableDocument
import org.structs4java.structs4JavaDsl.EnumDeclaration
import org.structs4java.structs4JavaDsl.Import
import org.structs4java.structs4JavaDsl.Member
import org.structs4java.structs4JavaDsl.StructDeclaration
import org.structs4java.structs4JavaDsl.Item
import org.structs4java.structs4JavaDsl.Structs4JavaDslPackage
import org.eclipse.xtext.EcoreUtil2

class Structs4JavaDslFormatter extends AbstractFormatter2 {

	def dispatch void format(org.structs4java.structs4JavaDsl.StructsFile structsFile,
		extension IFormattableDocument document) {
		// TODO: format HiddenRegions around keywords, attributes, cross references, etc. 
		for (Import imports : structsFile.getImports()) {
			imports.format;
		}
		for (StructDeclaration structs : structsFile.getStructs()) {
			structs.format;
		}
		for (EnumDeclaration enums : structsFile.getEnums()) {
			enums.format;
		}

		structsFile.regionFor.keyword(";").append[newLines = 2]
		structsFile.regionFor.keyword(";").prepend[noSpace]
	}

	def dispatch void format(StructDeclaration structDeclaration, extension IFormattableDocument document) {
		val open = structDeclaration.regionFor.keyword("{")
		val close = structDeclaration.regionFor.keyword("}")

		open.prepend[newLine].append[newLine]
		close.append[newLines = 2]
		interior(open, close)[indent]

		for (Member members : structDeclaration.getMembers()) {
			members.format;
		}
	}

	def dispatch void format(Import imp, extension IFormattableDocument document) {
		if ((imp.eContainer as org.structs4java.structs4JavaDsl.StructsFile).getImports().lastOrNull == imp) {
			imp.regionFor.keyword(";").prepend[noSpace].append[newLines = 2]
		} else {
			imp.regionFor.keyword(";").prepend[noSpace].append[newLine]
		}
	}

	def dispatch void format(Member member, extension IFormattableDocument document) {
		val nextMember = EcoreUtil2.getNextSibling(member) as Member
		if (nextMember !== null && nextMember.comments.size > 0) {
			member.regionFor.keyword(";").prepend[noSpace].append[newLines = 2]
		} else {
			member.regionFor.keyword(";").prepend[noSpace].append[newLine]
		}

		val comments = member.regionFor.features(Structs4JavaDslPackage.Literals.MEMBER__COMMENTS)
		comments.forEach[append[newLine]]
	}

	def dispatch void format(EnumDeclaration _enum, extension IFormattableDocument document) {
		val open = _enum.regionFor.keyword("{")
		val close = _enum.regionFor.keyword("}")

		open.prepend[newLine].append[newLine]
		close.prepend[newLine].append[newLines = 2]
		interior(open, close)[indent]

		_enum.regionFor.keyword(":").surround[oneSpace]

		for (Item itm : _enum.items) {
			itm.format
		}
	}

	def dispatch void format(Item item, extension IFormattableDocument document) {
		var region = item.regionFor.keyword(",")
		if(region !== null) {
			region.prepend[noSpace]
		}
		
		if (region === null) {
			region = item.regionFor.feature(Structs4JavaDslPackage.Literals.ITEM__VALUE);
		}
		
		val nextItem = EcoreUtil2.getNextSibling(item) as Item
		if (nextItem !== null && nextItem.comments.size > 0) {
			region.prepend[noSpace].append[newLines = 2]
		} else {
			region.append[newLine]
		}
		
		item.regionFor.keyword("=").surround[oneSpace]

		val comments = item.regionFor.features(Structs4JavaDslPackage.Literals.ITEM__COMMENTS)
		comments.forEach[append[newLine]]
	}
}
