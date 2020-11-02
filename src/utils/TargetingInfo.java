package utils;

import java.util.*;

import game.*;
import wildlife.*;

public class TargetingInfo {
	public final Object type;
	public final String faction;
	public TargetingInfo(Object type, String faction) {
		this.type = type;
		this.faction = faction;
	}
	
	private boolean doesFactionSatify(Faction animalFaction, Faction potentialTargetFaction) {
		if(potentialTargetFaction.name().equals(faction) || 
				(faction == null && (animalFaction.isNeutral() || animalFaction != potentialTargetFaction))) {
			return true;
		}
		return false;
	}
	public Thing getValidTargetFor(Animal animal, LinkedList<Unit> units, LinkedList<Building> buildings) {
		if(type == Unit.class || type instanceof UnitType) {
			Unit target = null;
			UnitType targetUnitType = null;
			if(type instanceof UnitType) {
				targetUnitType = (UnitType)type;
			}
			for(Unit potentialTarget : units) {
				if((targetUnitType != null && potentialTarget.getType() != targetUnitType) || potentialTarget == animal) {
					continue;
				}
				if(doesFactionSatify(animal.getFaction(), potentialTarget.getFaction()) && Math.random() < 0.3) {
					target = potentialTarget;
					if(Math.random() < 0.2) {
						break;
					}
				}
			}
			return target;
		}
		else if(type == Building.class || type instanceof BuildingType) {
			Building target = null;
			BuildingType targetBuildingType = null;
			if(type instanceof BuildingType) {
				targetBuildingType = (BuildingType)type;
			}
			for(Building potentialTarget : buildings) {
				if(targetBuildingType != null && potentialTarget.getType() != targetBuildingType) {
					continue;
				}
				if(doesFactionSatify(animal.getFaction(), potentialTarget.getFaction()) && Math.random() < 0.3) {
					target = potentialTarget;
					if(Math.random() < 0.2) {
						break;
					}
				}
			}
			return target;
		}
		return null;
	}
	@Override
	public String toString() {
		return "(" + type + "," + faction + ")";
	}
}
