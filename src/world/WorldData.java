package world;

import java.util.*;
import java.util.Map.Entry;

import game.*;
import networking.server.*;
import sounds.Sound;
import sounds.SoundEffect;
import sounds.SoundManager;
import utils.*;

public class WorldData {

	private LinkedList<Plant> plants = new LinkedList<Plant>();
	private LinkedList<Plant> newPlants = new LinkedList<Plant>();
	private LinkedList<Unit> units = new LinkedList<Unit>();
	private LinkedList<Unit> newUnits = new LinkedList<Unit>();
	private LinkedList<Building> buildings = new LinkedList<Building>();
	private LinkedList<Building> newBuildings = new LinkedList<Building>();
	private LinkedList<Projectile> projectiles = new LinkedList<Projectile>();
	private LinkedList<Projectile> newProjectiles = new LinkedList<Projectile>();
	private LinkedList<GroundModifier> groundModifiers = new LinkedList<GroundModifier>();
	private LinkedList<GroundModifier> newGroundModifiers = new LinkedList<GroundModifier>();
	
	// Stuff server keeps track of to send to clients
	private LinkedList<Projectile> projectilesToSend = new LinkedList<>();
	private LinkedList<Thing> deadThings = new LinkedList<>();

	public void addBuilding(Building newBuilding) {
		synchronized(newBuildings) {
			newBuildings.add(newBuilding);
		}
		newBuilding.getFaction().addBuilding(newBuilding);
	}
	public LinkedList<Building> getBuildings() {
		return buildings;
	}
	public void filterDeadBuildings() {
		LinkedList<Building> buildingsNew = new LinkedList<Building>();
		for (Building building : buildings) {
			if (building.isDead() == true) {
				
				// iterate through buildings cost and drop resources
				for (Entry<ItemType, Integer> entry : building.getType().getCost().entrySet()) {
					if(!building.getType().isRoad()) {
						Item item = new Item(entry.getValue() / Constants.RATIO_BUILDING_RESOURCE_DROP, entry.getKey()); // add 1/5 of resources to tile inventory
						building.getTile().getInventory().addItem(item);
					}
					
				}
				
				building.getFaction().removeBuilding(building);
				ThingMapper.removed(building);
				if(building == building.getTile().getRoad()) {
					if(building.getTile().getRoad() == building) {
						building.getTile().setRoad(null);
					}
				}
				else {
					if(building.getTile().getBuilding() == building) {
						building.getTile().setBuilding(null);
					}
				}
				addDeadThing(building);
				
				if(building.isRemoved() == false) {
					if(building.getType().isWoodConstruction()) {
						Sound sound = new Sound(SoundEffect.BUILDING_WOOD_DEATH, null, building.getTile());
						SoundManager.theSoundQueue.add(sound);
					}else 
					if(building.getType().isStoneConstruction()) {
						Sound sound = new Sound(SoundEffect.BUILDING_STONE_DEATH, null, building.getTile());
						SoundManager.theSoundQueue.add(sound);
					}else {
						Sound sound = new Sound(SoundEffect.BUILDING_WOOD_DEATH, null, building.getTile());
						SoundManager.theSoundQueue.add(sound);
					}
				}
				
				
				
			} else {
				buildingsNew.add(building);
			}
			
		}
		synchronized(newBuildings) {
			buildingsNew.addAll(newBuildings);
			newBuildings.clear();
		}
		buildings = buildingsNew;
	}
	public void addPlant(Plant newPlant) {
		synchronized(newPlants) {
			newPlants.add(newPlant);
		}
	}
	public LinkedList<Plant> getPlants() {
		return plants;
	}
	public void filterDeadPlants() {
		LinkedList<Plant> plantsCopy = new LinkedList<Plant>();
		for(Plant plant : plants) {
			if(plant.isDead() == true) {
				ThingMapper.removed(plant);
				plant.getTile().setHasPlant(null);
				addDeadThing(plant);
				// removed is a similar thing to
				if(plant.isRemoved() == false) {
					Sound sound = new Sound(SoundEffect.DEATH_PLANT, null, plant.getTile());
					SoundManager.theSoundQueue.add(sound);
				}
				
			} else {
				plantsCopy.add(plant);
			}
		}
		synchronized(newPlants) {
			plantsCopy.addAll(newPlants);
			newPlants.clear();
		}
		plants = plantsCopy;
	}
	
	public void addUnit(Unit unit) {
		synchronized(newUnits) {
			newUnits.add(unit);
		}
		unit.getFaction().addUnit(unit);
	}
	public LinkedList<Unit> getUnits() {
		return units;
	}
	
	public void filterDeadUnits() {
		// UNITS
		LinkedList<Unit> unitsNew = new LinkedList<Unit>();
		for (Unit unit : units) { // cycle through unit list
			if (unit.isDead() == true) { // if the unit is dead
				
				// check if unit has inventory, and drop it
				if(unit.getInventory() != null) {
					for(Item item : unit.getInventory().getItems()) {
						if(item != null) {
							unit.getTile().getInventory().addItem(item);
						}
					}
				}
				// if unit has a deadItem to drop, drop to tile
				for (Item item : unit.getType().getDeadItem()) {
					if(item != null) {
						unit.getTile().getInventory().addItem(item);
					}
				}
//				SoundManager.theSoundQueue.add(SoundEffect.DEATH);
				if(unit.isRemoved() == false) {
					Sound sound = new Sound(SoundEffect.DEATH_UNIT, unit.getFaction(), unit.getTile());
					SoundManager.theSoundQueue.add(sound);
				}
				
				
				unit.getFaction().removeUnit(unit);
				unit.getTile().removeUnit(unit);
				ThingMapper.removed(unit);
				addDeadThing(unit);
			} else {
				unitsNew.add(unit);
			}
		}
		synchronized(newUnits) {
			unitsNew.addAll(newUnits);
			newUnits.clear();
		}
		units = unitsNew;
	}
	public void addProjectile(Projectile newProjectile) {
		synchronized(newProjectiles) {
			newProjectiles.add(newProjectile);
		}
	}
	public LinkedList<Projectile> getProjectiles() {
		return projectiles;
	}
	public void filterDeadProjectiles() {
		// PROJECTILES
		LinkedList<Projectile> projectilesNew = new LinkedList<Projectile>();
		for(Projectile projectile : projectiles) {
			if(projectile.reachedTarget()) {
				projectile.getTile().removeProjectile(projectile);
				
				if(projectile.isHeavyProjectile()) {
					Sound sound = new Sound(SoundEffect.PROJECTILE_IMPACT_HEAVY, null, projectile.getTile());
					SoundManager.theSoundQueue.add(sound);
				}else
				if(projectile.isLightProjectile()) {
					Sound sound = new Sound(SoundEffect.PROJECTILE_IMPACT_LIGHT, null, projectile.getTile());
					SoundManager.theSoundQueue.add(sound);
				}else {
					Sound sound = new Sound(SoundEffect.PROJECTILE_IMPACT_GENERIC, null, projectile.getTile());
					SoundManager.theSoundQueue.add(sound);
				}
			} else {
				projectilesNew.add(projectile);
			}
		}
		synchronized(newProjectiles) {
			synchronized(projectilesToSend) {
				projectilesToSend.addAll(newProjectiles);
			}
			projectilesNew.addAll(newProjectiles);
			newProjectiles.clear();
		}
		projectiles = projectilesNew;
	}
	public LinkedList<Projectile> clearProjectilesToSend() {
		LinkedList<Projectile> copy = new LinkedList<>();
		synchronized (projectilesToSend) {
			copy.addAll(projectilesToSend);
			projectilesToSend.clear();
		}
		return copy;
	}

	public void addGroundModifier(GroundModifier gm) {
		synchronized (newGroundModifiers) {
			newGroundModifiers.add(gm);
		}
	}
	
	public void filterDeadGroundModifiers() {
		LinkedList<GroundModifier> groundModifiersNew = new LinkedList<GroundModifier>();
		for(GroundModifier modifier : groundModifiers) {
			Tile tile = modifier.getTile();
			if(modifier.isDead() == false) {
				groundModifiersNew.add(modifier);
			} else {
				tile.setModifier(null);
			}
		}
		synchronized (newGroundModifiers) {
			groundModifiersNew.addAll(newGroundModifiers);
			newGroundModifiers.clear();
		}
		groundModifiers = groundModifiersNew;
	}
	
	public void addDeadThing(Thing deadThing) {
		synchronized (deadThings) {
			deadThings.add(deadThing);
		}
	}
	public LinkedList<Thing> clearDeadThings() {
		LinkedList<Thing> copy = new LinkedList<>();
		synchronized (deadThings) {
			copy.addAll(deadThings);
			deadThings.clear();
		}
		return copy;
	}
	
	@Override
	public String toString() {
		return 	"units: " 				+ units.size() + 
				" \tbuildings: " 		+ buildings.size() + 
				" \tplants: " 			+ plants.size() + 
				" \tgroundModifiers: " 	+ groundModifiers.size() + 
				" \tprojectiles: " 		+ getProjectiles().size();
	}
}
