package org.structs4java.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.xtext.builder.nature.Messages;
import org.eclipse.xtext.builder.nature.ToggleXtextNatureAction;
import org.eclipse.xtext.ui.editor.IXtextEditorCallback;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.util.DontAskAgainDialogs;

import com.google.inject.Inject;

public class CustomNatureAddingEditorCallback extends IXtextEditorCallback.NullImpl {
	private static final String ADD_XTEXT_NATURE = "add_xtext_nature";
	@Inject
	private ToggleXtextNatureAction toggleNature;

	private @Inject DontAskAgainDialogs dialogs;

	@Override
	public void afterCreatePartControl(XtextEditor editor) {
		IResource resource = editor.getResource();
		if (resource != null && !toggleNature.hasNature(resource.getProject()) && resource.getProject().isAccessible()
				&& !resource.getProject().isHidden()) {
			String title = "Enable Structs4Java?";
			String message = Messages.NatureAddingEditorCallback_MessageDialog_Msg0 + resource.getProject().getName()
					+ " to a Structs4Java Project?";
			boolean addNature = false;
			if (MessageDialogWithToggle.PROMPT.equals(dialogs.getUserDecision(ADD_XTEXT_NATURE))) {
				int userAnswer = dialogs.askUser(message, title, ADD_XTEXT_NATURE, editor.getEditorSite().getShell());
				if (userAnswer == IDialogConstants.YES_ID) {
					addNature = true;
				} else if (userAnswer == IDialogConstants.CANCEL_ID) {
					return;
				}
			} else if (MessageDialogWithToggle.ALWAYS.equals(dialogs.getUserDecision(ADD_XTEXT_NATURE))) {
				addNature = true;
			}
			if (addNature) {
				toggleNature.toggleNature(resource.getProject());
			}
		}
	}
}
