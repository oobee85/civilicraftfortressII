package networking.view;

import java.awt.*;

import javax.swing.*;

import game.*;
import ui.*;
import utils.*;
import world.*;

public class MainMenuImageView extends JPanel {

	private long startTime = System.currentTimeMillis();
	private int imgSize = 40;
	private int padding = 5;
	private int speed = 100;
	public MainMenuImageView() {
		setOpaque(false);
	}
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(super.getPreferredSize().width, imgSize + 2);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int delta = (int) (System.currentTimeMillis() - startTime);
		int index = 0;
		for(UnitType type : Game.unitTypeList) {
			if(draw(g, type, index, delta)) {
				return;
			}
			index++;
		}
		for(BuildingType type : Game.buildingTypeList) {
			if(draw(g, type, index, delta)) {
				return;
			}
			index++;
		}
		for(ResearchType type : Game.researchTypeList) {
			if(draw(g, type, index, delta)) {
				return;
			}
			index++;
		}
		for(PlantType type : PlantType.values()) {
			if(draw(g, type, index, delta)) {
				return;
			}
			index++;
		}
		for(ResourceType type : ResourceType.values()) {
			if(draw(g, type, index, delta)) {
				return;
			}
			index++;
		}
		for(ItemType type : ItemType.values()) {
			if(draw(g, type, index, delta)) {
				return;
			}
			index++;
		}
		for(ProjectileType type : ProjectileType.values()) {
			if(draw(g, type, index, delta)) {
				return;
			}
			index++;
		}
		if(delta/speed > (index+1)*(imgSize+padding) + getWidth()) {
			startTime = System.currentTimeMillis();
		}
	}
	
	private boolean draw(Graphics g, HasImage hasImage, int index, int delta) {
		int x = -delta/speed + index*(imgSize+padding);
		if(x < -getWidth() - imgSize) {
			return false;
		}
		else if(x > 0) {
			return true;
		}
		Image image = hasImage.getImage(imgSize);
		g.drawImage(image, getWidth() + x, 1, imgSize, imgSize, null);
		return false;
	}
}
