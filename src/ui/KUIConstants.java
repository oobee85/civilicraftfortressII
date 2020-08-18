package ui;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

public class KUIConstants {

	public static final Color NORMAL_COLOR = new Color(240, 240, 240);
	public static final Color HOVERED_COLOR = new Color(220, 220, 230);
	public static final Color SELECTED_COLOR = new Color(0, 0, 100);
	
	public static final Color NORMAL_TEXT_COLOR = Color.black;
	public static final Color SELECTED_TEXT_COLOR = Color.white;
	public static final Color DISABLED_TEXT_COLOR = new Color(50, 50, 50);
	


//	private static final String fontName = "Comic Sans MS";
//	private static final String fontName = "Chiller";
	private static final String fontName = "TW Cen MT";

	public static final Font infoFont = new Font(fontName, Font.PLAIN, 22);
	public static final Font infoFontSmall = new Font(fontName, Font.PLAIN, 20);
	public static final Font infoFontTiny = new Font(fontName, Font.PLAIN, 14);
	
	public static final Font buttonFont = new Font(fontName, Font.PLAIN, 17);
	public static final Font buttonFontSmall = new Font(fontName, Font.PLAIN, 14);
	public static final Font buttonFontMini = new Font(fontName, Font.PLAIN, 13);
	

	public static final Insets zeroMargin = new Insets(0, 0, 0, 0);

	public static final Border massiveBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY, 1), BorderFactory.createEmptyBorder(5, 5, 5, 5));
	
	public static KButton setupButton(String text, Icon icon, Dimension size) {
		KButton b = new KButton(text, icon);
		b.setMargin(zeroMargin);
		b.setHorizontalAlignment(SwingConstants.LEFT);
		setComponentAttributes(b, size);
		return b;
	}
	public static JToggleButton setupToggleButton(String text, Icon icon, Dimension size) {
		JToggleButton b = new KToggleButton(text, icon);
		b.setMargin(zeroMargin);
		b.setHorizontalAlignment(SwingConstants.LEFT);
		setComponentAttributes(b, size);
		return b;
	}

	public static KLabel setupLabel(String text, Icon icon, Dimension size) {
		KLabel b = new KLabel(icon);
		b.setText(text);
		b.setHorizontalAlignment(SwingConstants.LEFT);
		setComponentAttributes(b, size);
		return b;
	}

	public static JLabel setupMiniLabel(String text, Icon icon, Dimension size) {
		JLabel b = new JLabel(icon);
		b.setText(text);
		b.setHorizontalAlignment(SwingConstants.LEFT);
		setComponentAttributes(b, size);
		b.setBorder(null);
		b.setFont(KUIConstants.buttonFontMini);
		return b;
	}

	public static void setComponentAttributes(JComponent c, Dimension size) {
		c.setFont(KUIConstants.buttonFont);
		c.setBorder(massiveBorder);
		c.setFocusable(false);
		if (size != null)
			c.setPreferredSize(size);
	}
}
