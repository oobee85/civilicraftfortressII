package ui.view;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import game.Research;
import ui.KUIConstants;

public class ResearchProgressView extends JPanel {
	
	private GameView gameView;
	public ResearchProgressView(GameView gameView) {
		this.gameView = gameView;
		this.setFocusable(false);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (gameView.getFaction() == null) {
			return;
		}
		Research target = gameView.getFaction().getResearchTarget();
		if (target == null || target.isCompleted()) {
			return;
		}
		g.setFont(KUIConstants.infoFont);
		double completedRatio = 1.0 * target.getPointsSpent()
				/ target.getRequiredPoints();
		String progress = String.format(target + " %d/%d",
				target.getPointsSpent(), target.getRequiredPoints());
		KUIConstants.drawProgressBar(g, Color.blue, Color.gray, Color.white, completedRatio, progress,
				0, 0, getWidth(), getHeight());
	}
}
