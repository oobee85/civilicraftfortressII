package ui.graphics.vanilla;

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

	
	public static void drawPolygonLiquid2(Tile tile, Graphics g, int drawx, int drawy, int tileSize, RenderingState state) {
		Utils.setTransparency(g, 0.5);
		int scale = 20;
		int[] amounts = new int[Direction.values().length];
		for (int i = 0; i < amounts.length; i++) {
			amounts[i] = 9999;
		}
		for (Tile neighbor : tile.getNeighbors()) {
			Direction d = Direction.getDirection(tile.getLocation(), neighbor.getLocation());
			amounts[d.ordinal()] = (int) neighbor.liquidAmount * tileSize / scale;
		}
		amounts[NONE.ordinal()] = (int) tile.liquidAmount * tileSize / scale;
		
		int[] avgs = new int[amounts.length];
		int[] avgsFull = new int[amounts.length];
		int[] avgsHalf = new int[amounts.length];
		for (int i = 0; i < amounts.length; i++) {
			avgs[i] = (amounts[NONE.ordinal()] + amounts[i])/2;

			avgsFull[i] = Math.min(avgs[i], tileSize);
			avgsHalf[i] = Math.min(avgs[i]/2, tileSize/2);
		}
		
		
		
		int bitmap = 0;
		for (Direction d : TILING_DIRECTIONS) {
			if (amounts[d.ordinal()] != 0) {
				bitmap |= d.tilingBit;
			}
		}

		g.setColor(tile.liquidType.getMipMap().getColor(tileSize));
//		 only liquid on tile and not neighbors
		if (bitmap == NONE.tilingBit 
				|| amounts[NONE.ordinal()] > 0
				) {
			g.fillPolygon(makeCircle(drawx + tileSize/2, drawy + tileSize/2, amounts[NONE.ordinal()], drawx, drawy, tileSize));
		}
		
		if (bitmap == (NONE.tilingBit | NORTH.tilingBit)) {
			g.fillPolygon(makeCircle(drawx + tileSize/2, drawy, amounts[NONE.ordinal()], drawx, drawy, tileSize));
		}
		
		if (bitmap == (NONE.tilingBit | NORTHEAST.tilingBit)) {
			g.fillPolygon(makeCircle(drawx + tileSize, drawy + tileSize/4, amounts[NONE.ordinal()], drawx, drawy, tileSize));
		}
		
		if (bitmap == (NONE.tilingBit | SOUTHEAST.tilingBit)) {
			g.fillPolygon(makeCircle(drawx + tileSize, drawy + tileSize*3/4, amounts[NONE.ordinal()], drawx, drawy, tileSize));
		}
		
		if (bitmap == (NONE.tilingBit | SOUTH.tilingBit)) {
			g.fillPolygon(makeCircle(drawx + tileSize/2, drawy + tileSize, amounts[NONE.ordinal()], drawx, drawy, tileSize));
		}
		
		if (bitmap == (NONE.tilingBit | SOUTHWEST.tilingBit)) {
			g.fillPolygon(makeCircle(drawx, drawy + tileSize*3/4, amounts[NONE.ordinal()], drawx, drawy, tileSize));
		}
		
		if (bitmap == (NONE.tilingBit | NORTHWEST.tilingBit)) {
			g.fillPolygon(makeCircle(drawx, drawy + tileSize/4, amounts[NONE.ordinal()], drawx, drawy, tileSize));
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
		
		Utils.setTransparency(g, 1);
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
