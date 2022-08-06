package ui.graphics.vanilla;

import java.awt.Color;
import java.util.*;

import game.*;
import ui.MapMode;
import world.*;
import world.liquid.LiquidType;

public class RenderingPipeline {

	public List<RenderingStep> steps;
	private RenderingPipeline() {
		steps = new ArrayList<>();
	}
	
	public static RenderingPipeline getRenderingPipeline(MapMode mode) {
		RenderingPipeline pipeline = new RenderingPipeline();
		if (mode.isHeatMapType()) {
			pipeline.steps.add(getHeatmapMaxMin);
			pipeline.steps.add(heatmapColor);
			if (mode == MapMode.TEMPURATURE) {
				pipeline.steps.add(snowTemp);
			}
		}
		else {
			pipeline.steps.add(terrain);
			pipeline.steps.add(resource);
			pipeline.steps.add(factions);
			pipeline.steps.add(roads);
			pipeline.steps.add(liquids);
			pipeline.steps.add(modifiers);
			pipeline.steps.add(inventory);
			pipeline.steps.add(plants);
			pipeline.steps.add(buildings);
			pipeline.steps.add(units);
		}
		
		return pipeline;
	}
	
	private static RenderingStep getHeatmapMaxMin = state -> {
		state.highHeight = Math.max(state.highHeight, state.tile.getHeight());
		state.lowHeight = Math.min(state.lowHeight, state.tile.getHeight());
		state.highPressure = Math.max(state.highPressure, state.tile.getAir().getPressure());
		state.lowPressure = Math.min(state.lowPressure, state.tile.getAir().getPressure());
		state.highTemp = Math.max(state.highTemp, state.tile.getAir().getTemperature());
		state.lowTemp = Math.min(state.lowTemp, state.tile.getAir().getTemperature());
		state.highHumidity = Math.max(state.highHumidity, state.tile.getAir().getHumidity());
		state.lowHumidity = Math.min(state.lowHumidity, state.tile.getAir().getHumidity());
	};
	
	private static RenderingStep snowTemp = state -> {
		RenderingFunctions.drawSnowTemp(state.tile, state.g, state.drawx, state.drawy, state.draww, state.drawh);
	};
	
	private static RenderingStep heatmapColor = state -> {
		RenderingFunctions.drawHeatMapColor(state);
	};
	
	private static RenderingStep terrain = state -> {
		state.g.drawImage(state.tile.getTerrain().getImage(state.tileSize), state.drawx, state.drawy, state.draww, state.drawh, null);
	};
	
	private static RenderingStep resource = state -> {
		if (state.tile.getResource() != null 
				&& state.faction.areRequirementsMet(state.tile.getResource().getType())) {
			RenderingFunctions.drawResource(state.tile.getResource(), state.g, state.drawx, state.drawy, state.draww, state.drawh, state.tileSize);
		}
	};
	
	private static RenderingStep factions = state -> {
		if (state.tile.getFaction() != null 
				&& state.tile.getFaction().id() != World.NO_FACTION_ID) {
			RenderingFunctions.drawFactionBorders(state.tile, state.g, state.drawx, state.drawy, state.draww, state.drawh, state.tileSize);
		}
	};
	
	private static RenderingStep liquids = state -> {
		if (state.tile.liquidType != LiquidType.DRY) {
			RenderingFunctions.drawLiquid(state.tile, state.g, state.drawx, state.drawy, state.draww, state.drawh, state.tileSize);
		}
	};
	
	private static RenderingStep modifiers = state -> {
		if (state.tile.getModifier() != null) {
			RenderingFunctions.drawModifiers(state.tile.getModifier(), state.g, state.drawx, state.drawy, state.draww, state.drawh, state.tileSize);
		}
	};
	
	private static RenderingStep inventory = state -> {
		if (state.tileSize <= 20) {
			return;
		}
		if (!state.tile.getInventory().isEmpty()) {
			RenderingFunctions.drawInventory(state.g, 
					state.tile.getInventory(), 
					state.drawx + state.tileSize / 5,
					state.drawy + state.tileSize / 5, 
					state.tileSize * 3/5, 
					state.tileSize * 3/5);
		}
	};
	
	private static RenderingStep plants = state -> {
		Plant p = state.tile.getPlant();
		if (p != null) {
			RenderingFunctions.drawPlant(p, state.g, state.drawx, state.drawy, state.draww, state.drawh, state.tileSize);
		}
	};

	private static RenderingStep roads = state -> {
		Building b = state.tile.getRoad();
		if (b != null) {
			RenderingFunctions.drawBuilding(b, state.g, state.drawx, state.drawy, state.draww, state.drawh, false, state.tileSize);
		}
	};
	
	private static RenderingStep buildings = state -> {
		Building b = state.tile.getBuilding();
		if (b != null) {
			RenderingFunctions.drawBuilding(b, state.g, state.drawx, state.drawy, state.draww, state.drawh, true, state.tileSize);
		}
	};
	
	private static RenderingStep units = state -> {
		for (Unit unit : state.tile.getUnits()) {
			RenderingFunctions.drawUnit(unit, state);
		}
	};
}
