package world;

import java.util.*;

import game.*;
import networking.server.*;
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
	
	private LinkedList<WeatherEvent> weatherEvents = new LinkedList<WeatherEvent>();
	private LinkedList<WeatherEvent> newWeatherEvents = new LinkedList<WeatherEvent>();
	
	// Stuff server keeps track of to send to clients
	private LinkedList<Projectile> projectilesToSend = new LinkedList<>();
	private LinkedList<WeatherEvent> weatherEventsToSend = new LinkedList<>();
	private LinkedList<Thing> deadThings = new LinkedList<>();

	public void addWeatherEvent(WeatherEvent newEvent) {
		synchronized (newWeatherEvents) {
			newWeatherEvents.add(newEvent);
		}
	}
	public LinkedList<WeatherEvent> getWeatherEvents() {
		return weatherEvents;
	}
	public void filterDeadWeatherEvents() {
		LinkedList<WeatherEvent> weatherEventsNew = new LinkedList<WeatherEvent>();
		for (WeatherEvent weather : weatherEvents) {
			Tile tile = weather.getTile();
			if(weather.isDead() == false) {
				weatherEventsNew.add(weather);
			} else {
				tile.setWeather(null);
			}
		}
		synchronized (newWeatherEvents) {
			synchronized(weatherEventsToSend) {
				weatherEventsToSend.addAll(newWeatherEvents);
			}
			weatherEventsNew.addAll(newWeatherEvents);
			newWeatherEvents.clear();
		}
		weatherEvents = weatherEventsNew;
	}
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
	}
	public LinkedList<Unit> getUnits() {
		return units;
	}
	public void filterDeadUnits() {
		// UNITS
		LinkedList<Unit> unitsNew = new LinkedList<Unit>();
		for (Unit unit : units) {
			if (unit.isDead() == true) {
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

	public LinkedList<WeatherEvent> clearWeatherEventsToSend() {
		LinkedList<WeatherEvent> copy = new LinkedList<>();
		synchronized (weatherEventsToSend) {
			copy.addAll(weatherEventsToSend);
			weatherEventsToSend.clear();
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
				" \tprojectiles: " 		+ getProjectiles().size() +
				" \tweatherEvents: "	+ getWeatherEvents().size();
	}
}
