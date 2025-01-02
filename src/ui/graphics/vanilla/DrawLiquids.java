package ui.graphics.vanilla;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

import utils.Direction;
import utils.Utils;

import static utils.Direction.NONE;
import static utils.Direction.NORTH;
import static utils.Direction.NORTHEAST;
import static utils.Direction.SOUTHEAST;
import static utils.Direction.SOUTH;
import static utils.Direction.SOUTHWEST;
import static utils.Direction.NORTHWEST;
import static utils.Direction.TILING_DIRECTIONS;
import world.Tile;

public class DrawLiquids {
	
	private static Polygon makeCircle(int centerX, int centerY, int radius, int drawx, int drawy, int tileSize) {
		
		int[] xpoints = new int[] {
				centerX,centerX,centerX,centerX,
				centerX,centerX,centerX,centerX
		};
		int[] ypoints = new int[] {
				centerY,centerY,centerY,centerY,
				centerY,centerY,centerY,centerY
		};
		
		
		ypoints[0] -= radius;
		
		xpoints[1] += radius*2/3;
		ypoints[1] -= radius*2/3;
		
		xpoints[2] += radius;
		
		xpoints[3] += radius*2/3;
		ypoints[3] += radius*2/3;
		
		ypoints[4] += radius;
		
		xpoints[5] -= radius*2/3;
		ypoints[5] += radius*2/3;
		
		xpoints[6] -= radius;
		
		xpoints[7] -= radius*2/3;
		ypoints[7] -= radius*2/3;
		
		for (int i = 0; i < xpoints.length; i++) {
			xpoints[i] = (xpoints[i] < drawx ? drawx : xpoints[i]);
			xpoints[i] = (xpoints[i] > drawx + tileSize ? drawx + tileSize : xpoints[i]);
			ypoints[i] = (ypoints[i] < drawy ? drawy : ypoints[i]);
			ypoints[i] = (ypoints[i] > drawy + tileSize ? drawy + tileSize : ypoints[i]);
		}
		return new Polygon(xpoints, ypoints, xpoints.length);
	}
	
	private static void drawWaterLayer(Tile tile, Graphics g, int drawx, int drawy, int tileSize, float[] amounts, int scale) {
		if ((int)(amounts[NONE.ordinal()] * tileSize/scale) <= 0) {
			return;
		}
		
		int[] neighborAmount = new int[amounts.length];
		int[] avgsFull = new int[amounts.length];
		int[] avgsHalf = new int[amounts.length];
		for (int i = 0; i < amounts.length; i++) {
			neighborAmount[i] = (int) (amounts[i]*tileSize/scale);
			
			int avg = (int)((amounts[NONE.ordinal()] + amounts[i])*tileSize/2/scale);

			avgsFull[i] = Math.min(avg, tileSize);
			avgsHalf[i] = Math.min(avg/2, tileSize/2);
		}

		int bitmap = 0;
		for (Direction d : TILING_DIRECTIONS) {
			if (neighborAmount[d.ordinal()] > 0) {
				bitmap |= d.tilingBit;
			}
		}

//		 only liquid on tile and not neighbors
		if (bitmap == NONE.tilingBit 
//				|| amounts[NONE.ordinal()] > 0
				) {
			g.fillPolygon(makeCircle(drawx + tileSize/2, drawy + tileSize/2, avgsHalf[NONE.ordinal()], drawx, drawy, tileSize));
		}
		
		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHEAST.tilingBit)) {
			g.fillPolygon(draw2or3side(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTH, NORTHEAST));
		}
		
		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHWEST.tilingBit)) {
			g.fillPolygon(draw2or3side(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTH, NORTHWEST));
		}
		
		if (bitmap == (NONE.tilingBit | SOUTH.tilingBit | SOUTHEAST.tilingBit)) {
			g.fillPolygon(draw2or3side(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTH, SOUTHEAST));
		}
		
		if (bitmap == (NONE.tilingBit | SOUTH.tilingBit | SOUTHWEST.tilingBit)) {
			g.fillPolygon(draw2or3side(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTH, SOUTHWEST));
		}
		
		if (bitmap == (NONE.tilingBit | NORTHWEST.tilingBit | SOUTHWEST.tilingBit)) {
			g.fillPolygon(draw2sideB(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTHWEST, SOUTHWEST));
		}
		
		if (bitmap == (NONE.tilingBit | NORTHEAST.tilingBit | SOUTHEAST.tilingBit)) {
			g.fillPolygon(draw2sideB(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTHEAST, SOUTHEAST));
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHEAST.tilingBit | SOUTHEAST.tilingBit)) {
			g.fillPolygon(draw2or3side(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTH, SOUTHEAST));
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHWEST.tilingBit | SOUTHWEST.tilingBit)) {
			g.fillPolygon(draw2or3side(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTH, SOUTHWEST));
		}

		if (bitmap == (NONE.tilingBit | SOUTH.tilingBit | NORTHWEST.tilingBit | SOUTHWEST.tilingBit)) {
			g.fillPolygon(draw2or3side(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTH, NORTHWEST));
		}

		if (bitmap == (NONE.tilingBit | SOUTH.tilingBit | NORTHEAST.tilingBit | SOUTHEAST.tilingBit)) {
			g.fillPolygon(draw2or3side(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTH, NORTHEAST));
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHEAST.tilingBit | NORTHWEST.tilingBit)) {
			g.fillPolygon(draw3side(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTH));
		}

		if (bitmap == (NONE.tilingBit | SOUTH.tilingBit | SOUTHEAST.tilingBit | SOUTHWEST.tilingBit)) {
			g.fillPolygon(draw3side(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTH));
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHEAST.tilingBit | SOUTHEAST.tilingBit 
				| SOUTH.tilingBit)) {
			g.fillPolygon(draw4sideB(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTHEAST));
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHWEST.tilingBit | SOUTHWEST.tilingBit 
				| SOUTH.tilingBit)) {
			g.fillPolygon(draw4sideB(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTHWEST));
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHWEST.tilingBit | NORTHEAST.tilingBit 
				| SOUTHEAST.tilingBit)) {
			g.fillPolygon(draw4side(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTHEAST, NORTHWEST));
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHWEST.tilingBit | NORTHEAST.tilingBit 
				| SOUTHWEST.tilingBit)) {
			g.fillPolygon(draw4side(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTHWEST, NORTHEAST));
		}

		if (bitmap == (NONE.tilingBit | SOUTH.tilingBit | NORTHWEST.tilingBit | SOUTHEAST.tilingBit 
				| SOUTHWEST.tilingBit)) {
			g.fillPolygon(draw4side(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTHWEST, SOUTHEAST));
		}

		if (bitmap == (NONE.tilingBit | SOUTH.tilingBit | NORTHEAST.tilingBit | SOUTHEAST.tilingBit 
				| SOUTHWEST.tilingBit)) {
			g.fillPolygon(draw4side(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTHEAST, SOUTHWEST));
		}

		if (bitmap == (NONE.tilingBit | NORTHWEST.tilingBit | NORTH.tilingBit | NORTHEAST.tilingBit 
					| SOUTHEAST.tilingBit | SOUTH.tilingBit)) {
			g.fillPolygon(draw5side(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTHWEST, SOUTH, NORTHWEST));
		}

		if (bitmap == (NONE.tilingBit | NORTHWEST.tilingBit | NORTH.tilingBit | NORTHEAST.tilingBit 
					| SOUTHWEST.tilingBit | SOUTH.tilingBit)) {
			g.fillPolygon(draw5side(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTHEAST, SOUTH, NORTHEAST));
		}

		if (bitmap == (NONE.tilingBit | NORTHWEST.tilingBit | NORTH.tilingBit | SOUTHEAST.tilingBit 
					| SOUTHWEST.tilingBit | SOUTH.tilingBit)) {
			g.fillPolygon(draw5side(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTHEAST, NORTH, SOUTHEAST));
		}

		if (bitmap == (NONE.tilingBit | SOUTHEAST.tilingBit | NORTH.tilingBit | NORTHEAST.tilingBit 
					| SOUTHWEST.tilingBit | SOUTH.tilingBit)) {
			g.fillPolygon(draw5side(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTHWEST, NORTH, SOUTHWEST));
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHEAST.tilingBit | SOUTHEAST.tilingBit 
				| SOUTHWEST.tilingBit | NORTHWEST.tilingBit)) {
			g.fillPolygon(draw5sideB(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTH, SOUTHWEST, SOUTHEAST));
		}

		if (bitmap == (NONE.tilingBit | SOUTH.tilingBit | NORTHEAST.tilingBit | SOUTHEAST.tilingBit 
				| SOUTHWEST.tilingBit | NORTHWEST.tilingBit)) {
			g.fillPolygon(draw5sideB(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTH, NORTHWEST, NORTHEAST));
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHEAST.tilingBit | SOUTHEAST.tilingBit 
				| SOUTH.tilingBit | SOUTHWEST.tilingBit | NORTHWEST.tilingBit)) {
			g.fillRect(drawx, drawy, tileSize, tileSize);
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHEAST.tilingBit
				| SOUTH.tilingBit | NORTHWEST.tilingBit)) {
			g.fillPolygon(draw3and1side(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTH));
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | SOUTHEAST.tilingBit
				| SOUTH.tilingBit | SOUTHWEST.tilingBit)) {
			g.fillPolygon(draw3and1side(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTH));
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHWEST.tilingBit
				| SOUTHEAST.tilingBit | SOUTHWEST.tilingBit)) {
			g.fillPolygon(draw3and1sideB(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTHEAST));
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHEAST.tilingBit
				| SOUTHEAST.tilingBit | SOUTHWEST.tilingBit)) {
			g.fillPolygon(draw3and1sideB(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTHWEST));
		}

		if (bitmap == (NONE.tilingBit | SOUTH.tilingBit | NORTHWEST.tilingBit
				| NORTHEAST.tilingBit | SOUTHWEST.tilingBit)) {
			g.fillPolygon(draw3and1sideB(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTHEAST));
		}

		if (bitmap == (NONE.tilingBit | SOUTH.tilingBit | NORTHWEST.tilingBit
				| NORTHEAST.tilingBit | SOUTHEAST.tilingBit)) {
			g.fillPolygon(draw3and1sideB(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTHWEST));
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHWEST.tilingBit
				| SOUTH.tilingBit | SOUTHEAST.tilingBit)) {
			g.fillPolygon(draw2and2side(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTHWEST));
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHEAST.tilingBit
				| SOUTH.tilingBit | SOUTHWEST.tilingBit)) {
			g.fillPolygon(draw2and2side(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTHEAST));
		}

		if (bitmap == (NONE.tilingBit | NORTHWEST.tilingBit | SOUTHWEST.tilingBit
				| NORTHEAST.tilingBit | SOUTHEAST.tilingBit)) {
			g.fillPolygon(draw2and2sideB(drawx, drawy, tileSize, avgsFull, avgsHalf));
		}

		if (bitmap == (NONE.tilingBit | SOUTHWEST.tilingBit | NORTH.tilingBit
				| SOUTHEAST.tilingBit)) {
			g.fillPolygon(draw1and1and1side(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTH));
		}

		if (bitmap == (NONE.tilingBit | NORTHWEST.tilingBit | SOUTH.tilingBit
				| NORTHEAST.tilingBit)) {
			g.fillPolygon(draw1and1and1side(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTH));
		}

		if (bitmap == (NONE.tilingBit | NORTHWEST.tilingBit | NORTH.tilingBit
				| SOUTHEAST.tilingBit)) {
			g.fillPolygon(draw2and1side(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTHEAST));
		}

		if (bitmap == (NONE.tilingBit | NORTHEAST.tilingBit | NORTH.tilingBit
				| SOUTHWEST.tilingBit)) {
			g.fillPolygon(draw2and1side(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTHWEST));
		}

		if (bitmap == (NONE.tilingBit | NORTHEAST.tilingBit | SOUTH.tilingBit
				| SOUTHWEST.tilingBit)) {
			g.fillPolygon(draw2and1side(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTHEAST));
		}

		if (bitmap == (NONE.tilingBit | NORTHWEST.tilingBit | SOUTH.tilingBit
				| SOUTHEAST.tilingBit)) {
			g.fillPolygon(draw2and1side(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTHWEST));
		}

		if (bitmap == (NONE.tilingBit | NORTHWEST.tilingBit | NORTH.tilingBit
				| SOUTH.tilingBit)) {
			g.fillPolygon(draw2and1sideB(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTHWEST));
		}

		if (bitmap == (NONE.tilingBit | NORTHEAST.tilingBit | NORTH.tilingBit
				| SOUTH.tilingBit)) {
			g.fillPolygon(draw2and1sideB(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTHEAST));
		}

		if (bitmap == (NONE.tilingBit | SOUTHWEST.tilingBit | NORTH.tilingBit
				| SOUTH.tilingBit)) {
			g.fillPolygon(draw2and1sideB(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTHWEST));
		}

		if (bitmap == (NONE.tilingBit | SOUTHEAST.tilingBit | NORTH.tilingBit
				| SOUTH.tilingBit)) {
			g.fillPolygon(draw2and1sideB(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTHEAST));
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | SOUTHWEST.tilingBit)) {
			g.fillPolygon(draw1and1side(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTHWEST));
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | SOUTHEAST.tilingBit)) {
			g.fillPolygon(draw1and1side(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTHEAST));
		}

		if (bitmap == (NONE.tilingBit | SOUTH.tilingBit | NORTHWEST.tilingBit)) {
			g.fillPolygon(draw1and1side(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTHWEST));
		}

		if (bitmap == (NONE.tilingBit | SOUTH.tilingBit | NORTHEAST.tilingBit)) {
			g.fillPolygon(draw1and1side(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTHEAST));
		}

		if (bitmap == (NONE.tilingBit | NORTHWEST.tilingBit | NORTHEAST.tilingBit)) {
			g.fillPolygon(draw1and1sideB(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTHWEST));
		}

		if (bitmap == (NONE.tilingBit | SOUTHWEST.tilingBit | SOUTHEAST.tilingBit)) {
			g.fillPolygon(draw1and1sideB(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTHWEST));
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | SOUTH.tilingBit)) {
			g.fillPolygon(draw1and1sideC(drawx, drawy, tileSize, avgsFull, avgsHalf));
		}

		
		if (bitmap == (NONE.tilingBit | NORTH.tilingBit)) {
			g.fillPolygon(draw1side(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTH));
		}
		
		if (bitmap == (NONE.tilingBit | SOUTH.tilingBit)) {
			g.fillPolygon(draw1side(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTH));
		}
		
		if (bitmap == (NONE.tilingBit | NORTHEAST.tilingBit)) {
			g.fillPolygon(draw1sideB(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTHEAST));
		}
		
		if (bitmap == (NONE.tilingBit | SOUTHEAST.tilingBit)) {
			g.fillPolygon(draw1sideB(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTHEAST));
		}
		
		if (bitmap == (NONE.tilingBit | SOUTHWEST.tilingBit)) {
			g.fillPolygon(draw1sideB(drawx, drawy, tileSize, avgsFull, avgsHalf, SOUTHWEST));
		}
		
		if (bitmap == (NONE.tilingBit | NORTHWEST.tilingBit)) {
			g.fillPolygon(draw1sideB(drawx, drawy, tileSize, avgsFull, avgsHalf, NORTHWEST));
		}		
	}

	
	public static void drawPolygonLiquid2(Tile tile, Graphics g, int drawx, int drawy, int tileSize, RenderingState state) {
		
		float[] amounts = new float[Direction.values().length];
		float myamount = tile.liquidAmount;
//		float myamount = tile.liquidAmount * tileSize / scale;
		amounts[NONE.ordinal()] = myamount;
		for (int i = 0; i < amounts.length; i++) {
			amounts[i] = myamount; // default out of bounds neighbors to same amount
		}
		for (Tile neighbor : tile.getNeighbors()) {
			Direction d = Direction.getDirection(tile.getLocation(), neighbor.getLocation());
			amounts[d.ordinal()] = neighbor.liquidAmount;
//			amounts[d.ordinal()] = neighbor.liquidAmount * tileSize / scale;
		}
		for (int layer = 0; layer <= 2; layer++) {
			int depth = layer*10;
			Color color = tile.liquidType.getMipMap().getColor(tileSize);
			if (layer == 2) {
				color = color.darker();
			}
			float[] reducedAmounts = new float[amounts.length];
			for (int i = 0; i < amounts.length; i++) {
				reducedAmounts[i] = Math.max(0, amounts[i] - depth);
			}
			Utils.setTransparency(g, 0.5);
			g.setColor(color);
			drawWaterLayer(tile, g, drawx, drawy, tileSize, reducedAmounts, 20);
			Utils.setTransparency(g, 1);
		}
	}
	private static Polygon draw1sideB(int drawx, int drawy, int tileSize, 
			int[] avgsFull, int[] avgsHalf, Direction shortSide) {

		int startx = shortSide.deltax() < 0 ? drawx : drawx + tileSize;
		int mx = shortSide.deltax() < 0 ? 1 : -1;

		int starty = shortSide.deltay() < 0 ? drawy : drawy + tileSize;
		int endy = shortSide.deltay() > 0 ? drawy : drawy + tileSize;
		int my = shortSide.deltay() < 0 ? 1 : -1;

		return new Polygon(new int[] {
				startx,
				startx + mx*avgsFull[NONE.ordinal()],
				startx + mx*avgsFull[NONE.ordinal()],
				startx,
		}, new int[] {
				(starty*3 + endy)/4 - my*avgsHalf[shortSide.ordinal()]/2,
				(starty*3 + endy)/4 - my*avgsHalf[NONE.ordinal()]/2,
				(starty*3 + endy)/4 + my*avgsFull[NONE.ordinal()]*3/4,
				(starty*3 + endy)/4 + my*avgsHalf[shortSide.ordinal()]/2,
		}, 4);
	}
	private static Polygon draw1side(int drawx, int drawy, int tileSize, 
			int[] avgsFull, int[] avgsHalf, Direction longSide) {
		
		int starty = longSide.deltay() < 0 ? drawy : drawy + tileSize;
		int my = longSide.deltay() < 0 ? 1 : -1;

		return new Polygon(new int[] {
				drawx + tileSize/2 + avgsHalf[longSide.ordinal()],
//				drawx + tileSize/2 + avgsHalf[NONE.ordinal()],
				drawx + tileSize/2 + avgsHalf[NONE.ordinal()],
				drawx + tileSize/2 - avgsHalf[NONE.ordinal()],
//				drawx + tileSize/2 - avgsHalf[NONE.ordinal()],
				drawx + tileSize/2 - avgsHalf[longSide.ordinal()],
		}, new int[] {
				starty,
//				starty + my*avgsHalf[NONE.ordinal()]/2,
				starty + my*avgsFull[NONE.ordinal()],
				starty + my*avgsFull[NONE.ordinal()],
//				starty + my*avgsHalf[NONE.ordinal()]/2,
				starty,
		}, 4);
		
	}
	private static Polygon draw1and1sideC(int drawx, int drawy, int tileSize, 
			int[] avgsFull, int[] avgsHalf) {

		return new Polygon(new int[] {
				drawx + tileSize/2 + avgsHalf[NORTH.ordinal()],
				drawx + tileSize/2 + avgsHalf[NONE.ordinal()],
				drawx + tileSize/2 + avgsHalf[SOUTH.ordinal()],
				drawx + tileSize/2 - avgsHalf[SOUTH.ordinal()],
				drawx + tileSize/2 - avgsHalf[NONE.ordinal()],
				drawx + tileSize/2 - avgsHalf[NORTH.ordinal()],
		}, new int[] {
				drawy,
				drawy + tileSize/2,
				drawy + tileSize,
				drawy + tileSize,
				drawy + tileSize/2,
				drawy,
		}, 6);
		
		
	}
	private static Polygon draw1and1sideB(int drawx, int drawy, int tileSize, 
			int[] avgsFull, int[] avgsHalf, Direction westSide) {
		
		int starty = westSide.deltay() < 0 ? drawy : drawy + tileSize;
		int endy = westSide.deltay() > 0 ? drawy : drawy + tileSize;
		int my = westSide.deltay() < 0 ? 1 : -1;
		
		Direction eastSide = westSide.deltay() < 0 ? NORTHEAST : SOUTHEAST;

		return new Polygon(new int[] {
				drawx + tileSize/2 + avgsHalf[NONE.ordinal()],
				drawx + tileSize,
				drawx + tileSize,
				drawx + tileSize/2,
				drawx,
				drawx,
				drawx + tileSize/2 - avgsHalf[NONE.ordinal()],
		}, new int[] {
				(starty + endy)/2 + my*avgsHalf[NONE.ordinal()],
				(starty*3 + endy)/4 + my*avgsHalf[eastSide.ordinal()]/2,
				(starty*3 + endy)/4 - my*avgsHalf[eastSide.ordinal()]/2,
				(starty + endy)/2 - my*avgsHalf[NONE.ordinal()],
				(starty*3 + endy)/4 - my*avgsHalf[westSide.ordinal()]/2,
				(starty*3 + endy)/4 + my*avgsHalf[westSide.ordinal()]/2,
				(starty + endy)/2 + my*avgsHalf[NONE.ordinal()],
		}, 7);
	}
	private static Polygon draw1and1side(int drawx, int drawy, int tileSize, 
			int[] avgsFull, int[] avgsHalf, Direction shortSide) {

		int startx = shortSide.deltax() < 0 ? drawx : drawx + tileSize;
		int endx = shortSide.deltax() > 0 ? drawx : drawx + tileSize;
		int mx = shortSide.deltax() < 0 ? 1 : -1;
		int starty = shortSide.deltay() > 0 ? drawy : drawy + tileSize;
		int endy = shortSide.deltay() < 0 ? drawy : drawy + tileSize;
		int my = shortSide.deltay() > 0 ? 1 : -1;
		
		Direction longSide = shortSide.deltay() > 0 ? NORTH : SOUTH;
		
		return new Polygon(new int[] {
				(startx + endx)/2 + mx*avgsHalf[longSide.ordinal()],
				(startx + endx)/2 + mx*avgsHalf[NONE.ordinal()],
				startx,
				startx,
				(startx + endx)/2 - mx*avgsHalf[NONE.ordinal()],
				(startx + endx)/2 - mx*avgsHalf[longSide.ordinal()],
		}, new int[] {
				starty,
				(starty + endy)/2 + my*avgsHalf[NONE.ordinal()],
				(starty + endy*3)/4 + my*avgsHalf[shortSide.ordinal()]/2,
				(starty + endy*3)/4 - my*avgsHalf[shortSide.ordinal()]/2,
				(starty + endy)/2 - my*avgsHalf[NONE.ordinal()]/2,
				starty,
		}, 6);
	}
	private static Polygon draw2and1sideB(int drawx, int drawy, int tileSize, 
			int[] avgsFull, int[] avgsHalf, Direction shortInside) {
		
		int startx = shortInside.deltax() < 0 ? drawx : drawx + tileSize;
		int endx = shortInside.deltax() > 0 ? drawx : drawx + tileSize;
		int mx = shortInside.deltax() < 0 ? 1 : -1;
		int starty = shortInside.deltay() < 0 ? drawy : drawy + tileSize;
		int endy = shortInside.deltay() > 0 ? drawy : drawy + tileSize;
		int my = shortInside.deltay() < 0 ? 1 : -1;
		
		Direction longInside = shortInside.deltay() > 0 ? NORTH : SOUTH;
		Direction longSolo = shortInside.deltay() < 0 ? SOUTH : NORTH;

		return new Polygon(new int[] {
				startx,
				startx + mx*avgsFull[longInside.ordinal()],
				(startx + endx)/2 + mx*avgsHalf[NONE.ordinal()],
				(startx + endx)/2 + mx*avgsHalf[longSolo.ordinal()],
				(startx + endx)/2 - mx*avgsHalf[longSolo.ordinal()],
				(startx + endx)/2 - mx*avgsHalf[NONE.ordinal()],
				startx,
		}, new int[] {
				starty,
				starty,
				(starty + endy)/2,
				endy,
				endy,
				(starty + endy)/2 + my*avgsHalf[NONE.ordinal()]/2,
				starty + my*avgsHalf[shortInside.ordinal()],
		}, 7);
	}
	private static Polygon draw2and1side(int drawx, int drawy, int tileSize, 
			int[] avgsFull, int[] avgsHalf, Direction shortSolo) {
		
		int startx = shortSolo.deltax() > 0 ? drawx : drawx + tileSize;
		int endx = shortSolo.deltax() < 0 ? drawx : drawx + tileSize;
		int mx = shortSolo.deltax() > 0 ? 1 : -1;
		int starty = shortSolo.deltay() > 0 ? drawy : drawy + tileSize;
		int endy = shortSolo.deltay() < 0 ? drawy : drawy + tileSize;
		int my = shortSolo.deltay() > 0 ? 1 : -1;
		
		Direction longEdge = shortSolo.deltay() > 0 ? NORTH : SOUTH;
		Direction shortEdge = (shortSolo.deltay() > 0) 
				? (shortSolo.deltax() > 0 ? NORTHWEST : NORTHEAST) 
						: (shortSolo.deltax() > 0 ? SOUTHWEST : SOUTHEAST);

		return new Polygon(new int[] {
				startx + mx*avgsFull[longEdge.ordinal()],
				(startx + endx)/2 + mx*avgsHalf[NONE.ordinal()],
				endx,
				endx,
				(startx + endx)/2 - mx*avgsHalf[NONE.ordinal()],
				startx,
				startx,
		}, new int[] {
				starty,
				(starty + endy)/2 - my*avgsHalf[NONE.ordinal()],
				(starty + endy*3)/4 - my*avgsHalf[shortSolo.ordinal()]/2,
				(starty + endy*3)/4 + my*avgsHalf[shortSolo.ordinal()]/2,
				(starty + endy)/2 + my*avgsHalf[NONE.ordinal()],
				starty + my*avgsHalf[shortEdge.ordinal()],
				starty,
		}, 7);
	}
	private static Polygon draw1and1and1side(int drawx, int drawy, int tileSize, 
			int[] avgsFull, int[] avgsHalf, Direction inside) {
		Direction easternInside = (inside == NORTH) ? SOUTHEAST : NORTHEAST;
		Direction westernInside = (inside == NORTH) ? SOUTHWEST: NORTHWEST;

		int starty = (inside.deltay() < 0) ? drawy : drawy + tileSize;
		int endy = (inside.deltay() > 0) ? drawy : drawy + tileSize;
		int my = (inside.deltay() < 0) ? 1 : -1;
		
		return new Polygon(new int[] {
				drawx + tileSize/2 + avgsHalf[inside.ordinal()],
				drawx + tileSize/2 + avgsHalf[NONE.ordinal()],
				drawx + tileSize,
				drawx + tileSize,
				drawx + tileSize/2,
				drawx,
				drawx,
				drawx + tileSize/2 - avgsHalf[NONE.ordinal()],
				drawx + tileSize/2 - avgsHalf[inside.ordinal()],
		}, new int[] {
				starty,
				(starty + endy)/2 - my*avgsHalf[NONE.ordinal()],
				(starty + endy*3)/4 - my*avgsHalf[easternInside.ordinal()]/2,
				(starty + endy*3)/4 + my*avgsHalf[easternInside.ordinal()]/2,
				(starty + endy)/2 + my*avgsHalf[NONE.ordinal()],
				(starty + endy*3)/4 + my*avgsHalf[westernInside.ordinal()]/2,
				(starty + endy*3)/4 - my*avgsHalf[westernInside.ordinal()]/2,
				(starty + endy)/2 - my*avgsHalf[NONE.ordinal()],
				starty,
		}, 9);
	}
	private static Polygon draw2and2sideB(int drawx, int drawy, int tileSize, 
			int[] avgsFull, int[] avgsHalf) {
		return new Polygon(new int[] {
				drawx,
				drawx + tileSize/2,
				drawx + tileSize,
				drawx + tileSize,
				drawx + tileSize/2,
				drawx,
		}, new int[] {
				drawy + tileSize/2 - avgsHalf[NORTHWEST.ordinal()],
				drawy + tileSize/2 - avgsHalf[NONE.ordinal()],
				drawy + tileSize/2 - avgsHalf[NORTHEAST.ordinal()],
				drawy + tileSize/2 + avgsHalf[SOUTHEAST.ordinal()],
				drawy + tileSize/2 + avgsHalf[NONE.ordinal()],
				drawy + tileSize/2 + avgsHalf[SOUTHWEST.ordinal()],
		}, 6);
	}
	private static Polygon draw2and2side(int drawx, int drawy, int tileSize, 
			int[] avgsFull, int[] avgsHalf, Direction topInside) {
		Direction bottomInside = (topInside == NORTHWEST) ? SOUTHEAST : SOUTHWEST;
		int startx = (topInside == NORTHWEST) ? drawx : drawx + tileSize;
		int endx = (topInside == NORTHEAST) ? drawx : drawx + tileSize;
		int mx = (topInside == NORTHWEST) ? 1 : -1;
		return new Polygon(new int[] {
				startx,
				startx,
				startx + mx*avgsFull[NORTH.ordinal()],
				(startx + endx)/2 + mx*avgsHalf[NONE.ordinal()],
				endx,
				endx,
				endx - mx*avgsFull[SOUTH.ordinal()],
				(startx + endx)/2 - mx*avgsHalf[NONE.ordinal()],
		}, new int[] {
				drawy + avgsHalf[topInside.ordinal()],
				drawy,
				drawy,
				drawy + tileSize/2 - avgsHalf[NONE.ordinal()],
				drawy + tileSize - avgsHalf[bottomInside.ordinal()],
				drawy + tileSize,
				drawy + tileSize,
				drawy + tileSize/2 + avgsHalf[NONE.ordinal()],
		}, 8);
	}
	private static Polygon draw3and1sideB(int drawx, int drawy, int tileSize, 
			int[] avgsFull, int[] avgsHalf, Direction soloSide) {
		
		Direction shortEdge = NONE;
		Direction longEdge = soloSide.deltay() > 0 ? NORTH : SOUTH;
		int startx = soloSide.deltax() > 0 ? drawx : drawx + tileSize;
		int endx = soloSide.deltax() < 0 ? drawx : drawx + tileSize;
		int mx = soloSide.deltax() > 0 ? 1 : -1;
		
		int starty = soloSide.deltay() > 0 ? drawy : drawy + tileSize;
		int endy = soloSide.deltay() < 0 ? drawy : drawy + tileSize;
		int my = soloSide.deltay() > 0 ? 1 : -1;
		
		if (soloSide == SOUTHEAST) {
			shortEdge = SOUTHWEST;
		}
		else if (soloSide == SOUTHWEST) {
			shortEdge = SOUTHEAST;
		}
		else if (soloSide == NORTHEAST) {
			shortEdge = NORTHWEST;
		}
		else if(soloSide == NORTHWEST) {
			shortEdge = NORTHEAST;
		}

		return new Polygon(new int[] {
				startx,
				startx,
				startx + mx*avgsFull[longEdge.ordinal()],
				(startx + endx)/2 + mx*avgsHalf[NONE.ordinal()],
				endx,
				endx,
				(startx + endx)/2 - mx*avgsHalf[NONE.ordinal()],
		}, new int[] {
				(starty + endy)/2 + my*avgsHalf[shortEdge.ordinal()],
				starty,
				starty,
				(starty + endy)/2 - my*avgsHalf[NONE.ordinal()],
				(starty + endy*3)/4 - my*avgsHalf[soloSide.ordinal()]/2,
				(starty + endy*3)/4 + my*avgsHalf[soloSide.ordinal()]/2,
				(starty + endy)/2 + my*avgsHalf[NONE.ordinal()],
		}, 7);
	}
	private static Polygon draw3and1side(int drawx, int drawy, int tileSize, 
			int[] avgsFull, int[] avgsHalf, Direction soloSide) {
		
		int tile1stHalf = avgsFull[NONE.ordinal()] > tileSize/2 ? tileSize/2 : avgsFull[NONE.ordinal()];
		int tile2ndHalf = avgsFull[NONE.ordinal()];
		
		int starty = soloSide.deltay() > 0 ? drawy : drawy + tileSize;
		int endy = soloSide.deltay() < 0 ? drawy : drawy + tileSize;
		int my = soloSide.deltay() > 0 ? 1 : -1;
		
		Direction eastDir = soloSide.deltay() > 0 ? NORTHEAST : SOUTHEAST;
		Direction westDir = soloSide.deltay() > 0 ? NORTHWEST : SOUTHWEST;

		return new Polygon(new int[] {
				drawx + tileSize,
				drawx + tileSize,
				drawx + tileSize - tile1stHalf*2 + tile2ndHalf,
				drawx + tileSize/2 + avgsHalf[NONE.ordinal()],
				drawx + tileSize/2 + avgsHalf[NONE.ordinal()],
				drawx + tileSize/2 + avgsHalf[soloSide.ordinal()],
				drawx + tileSize/2 - avgsHalf[soloSide.ordinal()],
				drawx + tileSize/2 - avgsHalf[NONE.ordinal()],
				drawx + tileSize/2 - avgsHalf[NONE.ordinal()],
				drawx + tile1stHalf*2 - tile2ndHalf,
				drawx,
				drawx,
		}, new int[] {
				starty,
				starty + my*avgsHalf[eastDir.ordinal()],
				starty + my*tile1stHalf,
				starty + my*tile2ndHalf,
				(starty + endy*3)/4,
				starty + my*tileSize,
				starty + my*tileSize,
				(starty + endy*3)/4,
				starty + my*tile2ndHalf,
				starty + my*tile1stHalf,
				starty + my*avgsHalf[westDir.ordinal()],
				starty,
		}, 12);
	}
	private static Polygon draw4sideB(int drawx, int drawy, int tileSize, 
			int[] avgsFull, int[] avgsHalf, Direction whichSide) {
		int startx = whichSide.deltax() < 0 ? drawx : drawx + tileSize;
		int mx = whichSide.deltax() < 0 ? 1 : -1;
		return new Polygon(new int[] {
				startx + mx * avgsFull[NORTH.ordinal()],
				startx,
				startx,
				startx + mx * avgsFull[SOUTH.ordinal()],
				startx + mx * avgsFull[NONE.ordinal()],
				startx + mx * avgsFull[NONE.ordinal()]
		}, new int[] {
				drawy,
				drawy,
				drawy + tileSize,
				drawy + tileSize,
				drawy + tileSize*3/4,
				drawy + tileSize*1/4
		}, 6);
	}
	private static Polygon draw4side(int drawx, int drawy, int tileSize, 
			int[] avgsFull, int[] avgsHalf, Direction farSide, Direction closeSide) {
		
		int startx = farSide.deltax() < 0 ? drawx : drawx + tileSize;
		int endx = farSide.deltax() < 0 ? drawx + tileSize : drawx;
		int mx = farSide.deltax() < 0 ? 1 : -1;
		
		int starty = farSide.deltay() > 0 ? drawy : drawy + tileSize;
		int endy = farSide.deltay() < 0 ? drawy : drawy + tileSize;
		int my = farSide.deltay() > 0 ? 1 : -1;
		return new Polygon(new int[] {
				startx,
				startx,
				endx,
				endx,
				(startx*3 + endx)/4 + mx * avgsFull[NONE.ordinal()]*3/4,
				startx + mx * avgsFull[NONE.ordinal()],
				startx + mx * avgsFull[NONE.ordinal()]*3/4,
		}, new int[] {
				drawy + tileSize/2 + my * avgsHalf[farSide.ordinal()],
				starty,
				starty,
				starty + my * avgsHalf[closeSide.ordinal()],
				starty + my * avgsFull[NONE.ordinal()]*3/4,
				starty + my * avgsFull[NONE.ordinal()],
				(starty*3 + endy)/4 + my * avgsFull[NONE.ordinal()]*3/4,
		}, 6);
	}
	private static Polygon draw3side(int drawx, int drawy, int tileSize, 
			int[] avgsFull, int[] avgsHalf, Direction northOrSouth) {
		int starty = northOrSouth.deltay() < 0 ? drawy : drawy + tileSize;
		int my = northOrSouth.deltay() < 0 ? 1 : -1;
		Direction westSide = northOrSouth.deltay() < 0 ? NORTHWEST : SOUTHWEST;
		Direction eastSide = northOrSouth.deltay() < 0 ? NORTHEAST : SOUTHEAST;
		return new Polygon(new int[] {
				drawx,
				drawx,
				drawx + tileSize,
				drawx + tileSize,
				drawx + tileSize*3/4,
				drawx + tileSize*1/4
		}, new int[] {
				starty + my*avgsHalf[westSide.ordinal()],
				starty,
				starty,
				starty + my*avgsHalf[eastSide.ordinal()],
				starty + my*avgsFull[NONE.ordinal()],
				starty + my*avgsFull[NONE.ordinal()]
		}, 6);
	}

	private static Polygon draw2sideB(int drawx, int drawy, int tileSize, 
			int[] avgsFull, int[] avgsHalf, Direction topEdge, Direction bottomEdge) {
		
		int startx = 0;
		int mx = 0;
		if (topEdge == NORTHWEST) {
			startx = drawx;
			mx = 1;
		}
		else if (topEdge == NORTHEAST) {
			startx = drawx + tileSize;
			mx = -1;
		}
		else {
			System.err.println("INVALID topEdge");
			System.exit(0);
		}

		return new Polygon(new int[] {
				startx,
				startx,
				startx + mx*avgsFull[NONE.ordinal()],
				startx + mx*avgsFull[NONE.ordinal()]
		}, new int[] {
				drawy + tileSize/2 - avgsHalf[topEdge.ordinal()],
				drawy + tileSize/2 + avgsHalf[bottomEdge.ordinal()],
				drawy + tileSize/2 + avgsFull[NONE.ordinal()]/2,
				drawy + tileSize/2 - avgsFull[NONE.ordinal()]/2,
		}, 4);
	}
	
	private static Polygon draw2or3side(int drawx, int drawy, int tileSize, 
			int[] avgsFull, int[] avgsHalf, Direction longEdge, Direction shortEdge) {

		int startx = 0;
		int starty = 0;
		int starty2 = 0;
		int multiplyX = 0;
		int multiplyY = 0;
		if (shortEdge.deltax() < 0) {
			startx = drawx;
			multiplyX = 1;
		}
		else {
			startx = drawx + tileSize;
			multiplyX = -1;
		}
		if (longEdge.deltay() < 0) {
			starty = drawy;
			if (shortEdge.deltay() < 0) {
				starty2 = starty;
			}
			else {
				starty2 = drawy + tileSize/2;
			}
			multiplyY = 1;
		}
		else {
			starty = drawy + tileSize;
			if (shortEdge.deltay() > 0) {
				starty2 = starty;
			}
			else {
				starty2 = drawy + tileSize/2;
			}
			multiplyY = -1;
		}

		return new Polygon(new int[] {
				startx + multiplyX * avgsFull[longEdge.ordinal()],
				startx,
				startx,
				startx + multiplyX * avgsFull[NONE.ordinal()]
		}, new int[] {
				starty,
				starty,
				starty2 + multiplyY * avgsHalf[shortEdge.ordinal()],
				starty + multiplyY * avgsFull[NONE.ordinal()]
		}, 4);
	}
	
	private static Polygon draw5sideB(int drawx, int drawy, int tileSize, 
			int[] avgsFull, int[] avgsHalf, Direction missing, Direction westSide, Direction eastSide) {

		int starty = 0;
		int multiplyY = 0;
		if(missing == SOUTH) {
			starty = drawy;
			multiplyY = 1;
		}
		if(missing == NORTH) {
			starty = drawy + tileSize;
			multiplyY = -1;
		}
		return new Polygon(new int[] {
				drawx,
				drawx,
				drawx + tileSize,
				drawx + tileSize,
				drawx + tileSize*3/4,
				drawx + tileSize*1/4,
		}, new int[] {
				drawy + tileSize/2 + multiplyY*avgsHalf[westSide.ordinal()],
				starty,
				starty,
				drawy + tileSize/2 + multiplyY*avgsHalf[eastSide.ordinal()],
				starty + multiplyY*avgsFull[NONE.ordinal()],
				starty + multiplyY*avgsFull[NONE.ordinal()],
		}, 6);
	}
	
	private static Polygon draw5side(int drawx, int drawy, int tileSize, 
			int[] avgsFull, int[] avgsHalf, Direction missing, Direction longEdge, Direction shortEdge) {

		int startx = 0;
		int starty = 0;
		int endx = 0;
		int endy = 0;
		int multiplyX = 0;
		int multiplyY = 0;
		if (missing == SOUTHWEST || missing == NORTHWEST) {
			startx = drawx + tileSize;
			endx = drawx;
			multiplyX = -1;
		}
		if (missing == SOUTHEAST || missing == NORTHEAST) {
			multiplyX = 1;
			startx = drawx;
			endx = drawx + tileSize;
		}
		if(missing == SOUTHEAST || missing == SOUTHWEST) {
			starty = drawy;
			endy = drawy + tileSize;
			multiplyY = 1;
		}
		if(missing == NORTHEAST || missing == NORTHWEST) {
			starty = drawy + tileSize;
			endy = drawy;
			multiplyY = -1;
		}
		return new Polygon(new int[] {
				endx,
				endx,
				startx,
				startx,
				startx + multiplyX*avgsFull[longEdge.ordinal()],
				startx + multiplyX*avgsFull[NONE.ordinal()],
				startx + multiplyX*avgsFull[NONE.ordinal()],
		}, new int[] {
				starty + multiplyY*avgsHalf[shortEdge.ordinal()],
				starty,
				starty,
				endy,
				endy,
				(starty + endy*3)/4,
				starty + multiplyY*avgsFull[NONE.ordinal()]*3/4,
		}, 7);
	}
}
