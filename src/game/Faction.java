package game;

import java.awt.*;
import java.util.*;
import java.util.Map.*;

import ui.*;
import world.*;

public class Faction {
	
	private static final Color[] factionColors = new Color[] { 
			Game.neutralColor, Game.playerColor, Color.blue, Color.green.darker(), 
			Color.orange, Color.cyan, Color.yellow};
	private static int idCounter = 0;

	private final HashMap<ItemType, Item> items;
	private LinkedList<AttackedNotification> attacked = new LinkedList<>();
	public final int id;
	public final Color color;
	public final String name;
	
	public Faction(String name, boolean useItems) {
		this.id = idCounter++;
		this.color = id < factionColors.length ? factionColors[id] : Game.neutralColor;
		this.name = name;
		this.items = useItems ? new HashMap<ItemType, Item>() : null;
	}
	
	public void gotAttacked(Tile tile) {
		attacked.add(new AttackedNotification(tile));
	}
	public LinkedList<AttackedNotification> getAttackedNotifications() {
		return attacked;
	}
	public void clearExpiredAttackedNotifications() {
		LinkedList<AttackedNotification> attackedNew = new LinkedList<>();
		for(AttackedNotification a : attacked) {
			if(!a.isExpired()) {
				attackedNew.add(a);
			}
		}
		attacked = attackedNew;
	}
	
	public boolean hasItems() {
		return items != null;
	}
	
	public int getItemAmount(ItemType type) {
		if(items != null) {
			return items.containsKey(type) ? items.get(type).getAmount() : 0;
		}
		return 0;
	}
	
	public void addItem(ItemType type, int quantity) {
		if(items != null) {
			if(!items.containsKey(type)) {
				items.put(type, new Item(0, type));
			}
			items.get(type).addAmount(quantity);
		}
	}
	
	public boolean canAfford(HashMap<ItemType, Integer> cost) {
		if(items == null) {
			return true;
		}
		for (Entry<ItemType, Integer> entry : cost.entrySet()) {
			if(!items.containsKey(entry.getKey()) || items.get(entry.getKey()).getAmount() < entry.getValue()) {
				return false;
			}
		}
		return true;
	}
	public boolean canAfford(ItemType type, int quantity) {
		if(items == null) {
			return true;
		}
		return items.containsKey(type) && items.get(type).getAmount() >= quantity;
	}
	
	public void payCost(HashMap<ItemType, Integer> cost) {
		if(items == null) {
			return;
		}
		for (Entry<ItemType, Integer> entry : cost.entrySet()) {
			items.get(entry.getKey()).addAmount(-entry.getValue());
		}
	}
	public void payCost(ItemType type, int quantity) {
		if(items == null) {
			return;
		}
		items.get(type).addAmount(-quantity);
	}
	
	@Override
	public String toString() {
		return name;
	}
}
