package com.photon.ui.components.fields;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import com.photon.ui.components.utils.AbstractButton;
import com.photon.ui.components.utils.AbstractCheckbox;

@SuppressWarnings("serial")
public class FieldCharLimit extends PlainDocument {
	
	public int limit;
	public JComponent nextComponent;
	public boolean switchAfterCharLimitHit;
	
	public FieldCharLimit(int limit, JComponent nextComponent, boolean b) {
		super();
		this.limit = limit;
		this.nextComponent = nextComponent;
		this.switchAfterCharLimitHit = b;
   }
	
	public void insertString(int offset, String  str, AttributeSet attr) throws BadLocationException {
		if (str == null) return;
		if ((getLength() + str.length()) <= limit) {
			super.insertString(offset, str, attr);
			if(this.switchAfterCharLimitHit && this.nextComponent != null) { switchComponent(this.nextComponent); }
		}
	}
	
	public static void switchComponent(JComponent component) {
		component.requestFocus();
		if(component instanceof JButton) { ((JButton)component).doClick(); }
		if(component instanceof AbstractButton) { ((AbstractButton)component).doClick(); }
		if(component instanceof AbstractCheckbox) { ((AbstractCheckbox)component).switchChecked(); }
		if(component instanceof JCheckBox) { ((JCheckBox)component).setSelected(!((JCheckBox)component).isSelected()); }
	}
}
