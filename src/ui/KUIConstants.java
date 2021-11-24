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

	public static final Dimension MAIN_MENU_BUTTON_SIZE = new Dimension(200, 40);
	


//	private static final String fontName = "Comic Sans MS";
//	private static final String fontName = "Chiller";
	private static final String fontName = "TW Cen MT";

	public static final Font infoFont = new Font(fontName, Font.PLAIN, 22);
	public static final Font infoFontSmall = new Font(fontName, Font.PLAIN, 20);
	public static final Font infoFontSmaller = new Font(fontName, Font.PLAIN, 18);
	public static final Font infoFontTiny = new Font(fontName, Font.PLAIN, 14);

	public static final Font combatStatsFont = new Font(fontName, Font.BOLD, 16);
	
	public static final Font buttonFont = new Font(fontName, Font.PLAIN, 17);
	public static final Font buttonFontSmall = new Font(fontName, Font.PLAIN, 14);
	public static final Font buttonFontMini = new Font(fontName, Font.PLAIN, 13);
	

	public static final Insets zeroMargin = new Insets(0, 0, 0, 0);

	public static final Border massiveBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY, 1), BorderFactory.createEmptyBorder(5, 5, 5, 5));
	public static final Border tinyBorder = BorderFactory.createLineBorder(Color.GRAY, 1);
	
	public static KButton setupButton(String text, Icon icon, Dimension size) {
		KButton b = new KButton(text, icon);
		b.setMargin(zeroMargin);
		b.setHorizontalAlignment(SwingConstants.LEFT);
		setComponentAttributes(b, size);
		return b;
	}
	public static KToggleButton setupToggleButton(String text, Icon icon, Dimension size) {
		KToggleButton b = new KToggleButton(text, icon);
		b.setMargin(zeroMargin);
		b.setHorizontalAlignment(SwingConstants.LEFT);
		setComponentAttributes(b, size);
		return b;
	}
	public static JRadioButton setupRadioButton(String text, Icon icon, Dimension size) {
		JRadioButton b = new KRadioButton(text, icon);
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
		b.setOpaque(true);
		return b;
	}
	public static JTextField setupTextField(String text, Dimension size) {
		JTextField b = new JTextField(text);
		b.setHorizontalAlignment(SwingConstants.LEFT);
		setComponentAttributes(b, size);
		b.setOpaque(true);
		b.setFocusable(true);
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
		if (size != null) {
			c.setPreferredSize(size);
			c.setMaximumSize(size);
		}
	}
	

	public static void drawProgressBar(Graphics g, Color foreground, Color background, Color textColor, double ratio, String text, int x, int y, int w, int h) {
		g.setColor(background);
		g.fillRect(x, y, w, h);
		g.setColor(foreground);
		g.fillRect(x, y, (int) (w * ratio), h);
		g.setColor(textColor);
		int textWidth = g.getFontMetrics().stringWidth(text);
		g.drawString(text, x + w/2 - textWidth/2, y + h/2 + g.getFont().getSize()/3);
	}
}
