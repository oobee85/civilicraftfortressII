package ui.graphics.vanilla;

import java.awt.*;
import java.util.List;
import java.util.*;

import game.*;
import ui.*;
import utils.*;
import world.*;
import world.liquid.LiquidType;

public class RenderingPipeline {
	
	public static final int MIN_TILESIZE_FOR_DEBUG_STRINGS = 150;

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
			if (mode == MapMode.FLOW) {
				pipeline.steps.add(airFlow);
			}
		}
		else {
			pipeline.steps.add(terrain);
			pipeline.steps.add(resource);
			pipeline.steps.add(factions);
			pipeline.steps.add(roads);
			pipeline.steps.add(gradientShading);
			pipeline.steps.add(liquids);
			pipeline.steps.add(sunShadows);
			pipeline.steps.add(modifiers);
			pipeline.steps.add(tileInventory);
			pipeline.steps.add(plants);
			pipeline.steps.add(buildings);
			pipeline.steps.add(units);
			
			pipeline.steps.add(thingInventoryHealthBarHitsplat);
			pipeline.steps.add(projectiles);
			pipeline.steps.add(unitQuantitySquares);
			if (mode == MapMode.FLOW2) {
				pipeline.steps.add(airFlow2);
			}

//			pipeline.steps.add(fogOfWar);
			pipeline.steps.add(target);
			pipeline.steps.add(hoveredTiles);
			pipeline.steps.add(plannedThing);
			pipeline.steps.add(selectedThings);
		}
		
		pipeline.steps.add(debugStrings);
		return pipeline;
	}

	private static RenderingStep fogOfWar = new RenderingStep(state -> {
		if (!Game.DISABLE_NIGHT) {
			int startxoffset = state.lowerX - 2;
			int startyoffset = state.lowerY - 2;
			int numtilesx = state.upperX - state.lowerX + 4;
			int numtilesy = state.upperY - state.lowerY + 4;
			
			int startx = startxoffset * state.tileSize;
			int starty = startyoffset * state.tileSize;
			int width = numtilesx * state.tileSize;
			int height = numtilesy * state.tileSize;

			state.g.drawImage(state.fogOfWarImage, 
					startx, starty, 
					startx + width, starty + height, 
					startxoffset * 2, startyoffset * 2, 
					startxoffset * 2 + numtilesx * 2, startyoffset * 2 + numtilesy * 2, 
					null);
		}
	}, null);
	
	private static RenderingStep target = new RenderingStep(state -> {
		if (state.gameViewState.leftClickAction == LeftClickAction.ATTACK) {
			RenderingFunctions.drawTarget(state, state.gameViewState.hoveredTile);
		}
	}, null);
	
	private static RenderingStep debugStrings = new RenderingStep(state -> {
		if (state.gameViewState.drawDebugStrings && state.tileSize >= MIN_TILESIZE_FOR_DEBUG_STRINGS) {
			int fontsize = Math.min(13, state.tileSize / 4);
			Font font = new Font("Consolas", Font.PLAIN, fontsize);
			state.g.setFont(font);
		}
	}, (state, tile, drawat) -> {
		if (state.gameViewState.drawDebugStrings && state.tileSize >= MIN_TILESIZE_FOR_DEBUG_STRINGS) {
			RenderingFunctions.drawDebugStringsHelper1(state, tile, drawat, state.g.getFont().getSize());
		}
	});

	private static RenderingStep airFlow2 = new RenderingStep(null, (state, tile, drawat) -> {
		RenderingFunctions.drawAirFlow(state, false, tile, drawat);
	});
	
	private static RenderingStep airFlow = new RenderingStep(null, (state, tile, drawat) -> {
		RenderingFunctions.drawAirFlow(state, true, tile, drawat);
	});
	
	private static RenderingStep unitQuantitySquares = new RenderingStep(null, (state, tile, drawat) -> {
		RenderingFunctions.drawUnitQuantitySquares(state, tile, drawat);
	});
	
	private static RenderingStep projectiles = new RenderingStep(state -> {
		RenderingFunctions.drawProjectiles(state);
	}, null);
	
	private static RenderingStep thingInventoryHealthBarHitsplat = new RenderingStep(null, (state, tile, drawat) -> {
		RenderingFunctions.drawInventoryHealthBar(state, tile, drawat);
	});
	
	private static RenderingStep selectedThings = new RenderingStep(state -> {
		RenderingFunctions.drawSelectedThings(state);
	}, null);

	private static RenderingStep gradientShading = new RenderingStep(null, (state, tile, drawat) -> {
		RenderingFunctions.drawTileGradientShading(state, tile, drawat);
	});

	private static RenderingStep plannedThing = new RenderingStep(state -> {
		RenderingFunctions.drawPlannedThing(state);
	}, null);
	
	private static RenderingStep hoveredTiles = new RenderingStep(state -> {
		RenderingFunctions.drawHoveredTiles(state);
	}, null);
	
	private static RenderingStep getHeatmapMaxMin = new RenderingStep(null, (state, tile, drawat) -> {
		state.highHeight = Math.max(state.highHeight, tile.getHeight());
		state.lowHeight = Math.min(state.lowHeight, tile.getHeight());
		state.highPressure = Math.max(state.highPressure, tile.getAir().getPressure());
		state.lowPressure = Math.min(state.lowPressure, tile.getAir().getPressure());
		state.highTemp = Math.max(state.highTemp, tile.getAir().getTemperature());
		state.lowTemp = Math.min(state.lowTemp, tile.getAir().getTemperature());
		state.highHumidity = Math.max(state.highHumidity, tile.getAir().getHumidity());
		state.lowHumidity = Math.min(state.lowHumidity, tile.getAir().getHumidity());
	});
	
	private static RenderingStep snowTemp = new RenderingStep(null, (state, tile, drawat) -> {
		RenderingFunctions.drawSnowTemp(state, tile, drawat);
	});
	
	private static RenderingStep heatmapColor = new RenderingStep(null, (state, tile, drawat) -> {
		RenderingFunctions.drawHeatMapColor(state, tile, drawat);
	});
	
	private static RenderingStep terrain = new RenderingStep(null, (state, tile, drawat) -> {
		state.g.drawImage(tile.getTerrain().getImage(state.tileSize), 
				drawat.x, drawat.y, drawat.w, drawat.h, null);
	});
	
	private static RenderingStep resource = new RenderingStep(null, (state, tile, drawat) -> {
		if (tile.getResource() != null 
				&& state.faction.areRequirementsMet(tile.getResource().getType())) {
			RenderingFunctions.drawResource(tile.getResource(), state.g, 
					drawat.x, drawat.y, state.tileSize);
		}
	});
	
	private static RenderingStep factions = new RenderingStep(null, (state, tile, drawat) -> {
		if (tile.getFaction() != null 
				&& tile.getFaction().id() != World.NO_FACTION_ID) {
			RenderingFunctions.drawFactionBorders(tile, state.g, 
					drawat.x, drawat.y, state.tileSize, state.tileSize, state.tileSize);
		}
	});
	
	private static RenderingStep liquids = new RenderingStep(null, (state, tile, drawat) -> {
		if (tile.liquidType != LiquidType.DRY) {
			RenderingFunctions.drawLiquid(tile, state.g, drawat.x, drawat.y, state.tileSize, state);
		}
	});
	
	private static RenderingStep modifiers = new RenderingStep(null, (state, tile, drawat) -> {
		if (tile.getModifier() != null) {
			RenderingFunctions.drawModifiers(tile.getModifier(), state.g, drawat.x, drawat.y, state.tileSize);
		}
	});
	
	private static RenderingStep tileInventory = new RenderingStep(null, (state, tile, drawat) -> {
		if (state.tileSize > 20 && !tile.getInventory().isEmpty()) {
			RenderingFunctions.drawInventory(state.g, 
					tile.getInventory(), 
					drawat.x + state.tileSize / 5,
					drawat.y + state.tileSize / 5, 
					state.tileSize * 3/5, 
					state.tileSize * 3/5,
					tile);
		}
	});
	
	private static RenderingStep plants = new RenderingStep(null, (state, tile, drawat) -> {
		if (tile.getPlant() != null) {
			RenderingFunctions.drawPlant(tile.getPlant(), state.g, drawat.x, drawat.y, state.tileSize);
		}
	});

	private static RenderingStep roads = new RenderingStep(null, (state, tile, drawat) -> {
		if (tile.getRoad() != null) {
			RenderingFunctions.drawBuilding(tile.getRoad(), state.g, drawat.x, drawat.y, state.tileSize);
		}
	});
	
	private static RenderingStep buildings = new RenderingStep(null, (state, tile, drawat) -> {
		if (tile.getBuilding() != null) {
			RenderingFunctions.drawBuilding(tile.getBuilding(), state.g, drawat.x, drawat.y, state.tileSize);
		}
	});
	
	private static RenderingStep units = new RenderingStep(null, (state, tile, drawat) -> {
		RenderingFunctions.drawUnitImages(state, tile, drawat);
	});
	
	private static RenderingStep sunShadows = new RenderingStep(null, (state, tile, drawat) -> {
		RenderingFunctions.drawSunShadows(state, tile, drawat);
	});
}
