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
		// only liquid on tile and not neighbors
		if (bitmap == NONE.tilingBit 
				|| amounts[NONE.ordinal()] > 0) {
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
			Polygon p = new Polygon(new int[] {
					drawx + tileSize - avgsFull[NORTH.ordinal()],
					drawx + tileSize,
					drawx + tileSize,
					drawx + tileSize - avgsFull[NONE.ordinal()]
			}, new int[] {
					drawy,
					drawy,
					drawy + avgsHalf[NORTHEAST.ordinal()],
					drawy + avgsFull[NONE.ordinal()]
			}, 4);
			g.fillPolygon(p);
		}
		
		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHWEST.tilingBit)) {
			Polygon p = new Polygon(new int[] {
					drawx + avgsFull[NORTH.ordinal()],
					drawx,
					drawx,
					drawx + avgsFull[NONE.ordinal()]
			}, new int[] {
					drawy,
					drawy,
					drawy + avgsHalf[NORTHEAST.ordinal()],
					drawy + avgsFull[NONE.ordinal()]
			}, 4);
			g.fillPolygon(p);
		}
		
		if (bitmap == (NONE.tilingBit | SOUTH.tilingBit | SOUTHEAST.tilingBit)) {
			Polygon p = new Polygon(new int[] {
					drawx + tileSize - avgsFull[SOUTH.ordinal()],
					drawx + tileSize,
					drawx + tileSize,
					drawx + tileSize - avgsFull[NONE.ordinal()]
			}, new int[] {
					drawy + tileSize,
					drawy + tileSize,
					drawy + tileSize - avgsHalf[SOUTHEAST.ordinal()],
					drawy + tileSize - avgsFull[NONE.ordinal()]
			}, 4);
			g.fillPolygon(p);
		}
		
		if (bitmap == (NONE.tilingBit | SOUTH.tilingBit | SOUTHWEST.tilingBit)) {
			Polygon p = new Polygon(new int[] {
					drawx + avgsFull[SOUTH.ordinal()],
					drawx,
					drawx,
					drawx + avgsFull[NONE.ordinal()]
			}, new int[] {
					drawy + tileSize,
					drawy + tileSize,
					drawy + tileSize - avgsHalf[SOUTHWEST.ordinal()],
					drawy + tileSize - avgsFull[NONE.ordinal()]
			}, 4);
			g.fillPolygon(p);
		}
		
		if (bitmap == (NONE.tilingBit | NORTHEAST.tilingBit | SOUTHEAST.tilingBit)) {
			Polygon p = new Polygon(new int[] {
					drawx + tileSize,
					drawx + tileSize,
					drawx + tileSize - avgsFull[NONE.ordinal()],
					drawx + tileSize - avgsFull[NONE.ordinal()]
			}, new int[] {
					drawy + tileSize/2 - avgsHalf[NORTHEAST.ordinal()],
					drawy + tileSize/2 + avgsHalf[SOUTHEAST.ordinal()],
					drawy + tileSize/2 + avgsFull[NONE.ordinal()]/2,
					drawy + tileSize/2 - avgsFull[NONE.ordinal()]/2,
			}, 4);
			g.fillPolygon(p);
		}
		
		if (bitmap == (NONE.tilingBit | NORTHWEST.tilingBit | SOUTHWEST.tilingBit)) {
			Polygon p = new Polygon(new int[] {
					drawx,
					drawx,
					drawx + avgsFull[NONE.ordinal()],
					drawx + avgsFull[NONE.ordinal()]
			}, new int[] {
					drawy + tileSize/2 - avgsHalf[NORTHWEST.ordinal()],
					drawy + tileSize/2 + avgsHalf[SOUTHWEST.ordinal()],
					drawy + tileSize/2 + avgsFull[NONE.ordinal()]/2,
					drawy + tileSize/2 - avgsFull[NONE.ordinal()]/2,
			}, 4);
			g.fillPolygon(p);
		}
		
		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHEAST.tilingBit | SOUTHEAST.tilingBit)) {
			Polygon p = new Polygon(new int[] {
					drawx + tileSize - avgsFull[NORTH.ordinal()],
					drawx + tileSize,
					drawx + tileSize,
					drawx + tileSize - avgsFull[NONE.ordinal()]
			}, new int[] {
					drawy,
					drawy,
					drawy + tileSize/2 + avgsHalf[SOUTHEAST.ordinal()],
					drawy + avgsFull[NONE.ordinal()]
			}, 4);
			g.fillPolygon(p);
		}
		
		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHWEST.tilingBit | SOUTHWEST.tilingBit)) {
			Polygon p = new Polygon(new int[] {
					drawx + avgsFull[NORTH.ordinal()],
					drawx,
					drawx,
					drawx + avgsFull[NONE.ordinal()]
			}, new int[] {
					drawy,
					drawy,
					drawy + tileSize/2 + avgsHalf[SOUTHWEST.ordinal()],
					drawy + avgsFull[NONE.ordinal()]
			}, 4);
			g.fillPolygon(p);
		}
		
		if (bitmap == (NONE.tilingBit | SOUTH.tilingBit | NORTHWEST.tilingBit | SOUTHWEST.tilingBit)) {
			Polygon p = new Polygon(new int[] {
					drawx + avgsFull[SOUTH.ordinal()],
					drawx,
					drawx,
					drawx + avgsFull[NONE.ordinal()]
			}, new int[] {
					drawy + tileSize,
					drawy + tileSize,
					drawy + tileSize/2 - avgsHalf[NORTHWEST.ordinal()],
					drawy + tileSize - avgsFull[NONE.ordinal()]
			}, 4);
			g.fillPolygon(p);
		}
		
		if (bitmap == (NONE.tilingBit | SOUTH.tilingBit | NORTHEAST.tilingBit | SOUTHEAST.tilingBit)) {
			Polygon p = new Polygon(new int[] {
					drawx + tileSize - avgsFull[SOUTH.ordinal()],
					drawx + tileSize,
					drawx + tileSize,
					drawx + tileSize - avgsFull[NONE.ordinal()]
			}, new int[] {
					drawy + tileSize,
					drawy + tileSize,
					drawy + tileSize/2 - avgsHalf[NORTHWEST.ordinal()],
					drawy + tileSize - avgsFull[NONE.ordinal()]
			}, 4);
			g.fillPolygon(p);
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHEAST.tilingBit | NORTHWEST.tilingBit)) {
			Polygon p = new Polygon(new int[] {
					drawx,
					drawx,
					drawx + tileSize,
					drawx + tileSize,
					drawx + tileSize*3/4,
					drawx + tileSize*1/4
			}, new int[] {
					drawy + avgsHalf[NORTHWEST.ordinal()],
					drawy,
					drawy,
					drawy + avgsHalf[NORTHEAST.ordinal()],
					drawy + avgsFull[NONE.ordinal()],
					drawy + avgsFull[NONE.ordinal()]
			}, 6);
			g.fillPolygon(p);
		}

		if (bitmap == (NONE.tilingBit | SOUTH.tilingBit | SOUTHEAST.tilingBit | SOUTHWEST.tilingBit)) {
			Polygon p = new Polygon(new int[] {
					drawx,
					drawx,
					drawx + tileSize,
					drawx + tileSize,
					drawx + tileSize*3/4,
					drawx + tileSize*1/4
			}, new int[] {
					drawy + tileSize -avgsHalf[NORTHWEST.ordinal()],
					drawy + tileSize,
					drawy + tileSize,
					drawy + tileSize -avgsHalf[NORTHEAST.ordinal()],
					drawy + tileSize -avgsFull[NONE.ordinal()],
					drawy + tileSize -avgsFull[NONE.ordinal()]
			}, 6);
			g.fillPolygon(p);
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHEAST.tilingBit | SOUTHEAST.tilingBit | SOUTH.tilingBit)) {
			Polygon p = new Polygon(new int[] {
					drawx + tileSize - avgsFull[NORTH.ordinal()],
					drawx + tileSize,
					drawx + tileSize,
					drawx + tileSize - avgsFull[SOUTH.ordinal()],
					drawx + tileSize - avgsFull[NONE.ordinal()],
					drawx + tileSize - avgsFull[NONE.ordinal()]
			}, new int[] {
					drawy,
					drawy,
					drawy + tileSize,
					drawy + tileSize,
					drawy + tileSize*3/4,
					drawy + tileSize*1/4
			}, 6);
			g.fillPolygon(p);
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHWEST.tilingBit | SOUTHWEST.tilingBit | SOUTH.tilingBit)) {
			Polygon p = new Polygon(new int[] {
					drawx + avgsFull[NORTH.ordinal()],
					drawx,
					drawx,
					drawx + avgsFull[SOUTH.ordinal()],
					drawx + avgsFull[NONE.ordinal()],
					drawx + avgsFull[NONE.ordinal()]
			}, new int[] {
					drawy,
					drawy,
					drawy + tileSize,
					drawy + tileSize,
					drawy + tileSize*3/4,
					drawy + tileSize*1/4
			}, 6);
			g.fillPolygon(p);
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHWEST.tilingBit | NORTHEAST.tilingBit | SOUTHEAST.tilingBit)) {
			Polygon p = new Polygon(new int[] {
					drawx,
					drawx,
					drawx + tileSize,
					drawx + tileSize,
					drawx + tileSize - avgsFull[NONE.ordinal()]*3/4,
					drawx + tileSize - avgsFull[NONE.ordinal()]
			}, new int[] {
					drawy + avgsHalf[NORTHWEST.ordinal()],
					drawy,
					drawy,
					drawy + tileSize/2 + avgsHalf[SOUTHEAST.ordinal()],
					drawy + avgsFull[NONE.ordinal()],
					drawy + avgsFull[NONE.ordinal()]*3/4
			}, 6);
			g.fillPolygon(p);
		}

		if (bitmap == (NONE.tilingBit | NORTH.tilingBit | NORTHWEST.tilingBit | NORTHEAST.tilingBit | SOUTHWEST.tilingBit)) {
			Polygon p = new Polygon(new int[] {
					drawx + tileSize,
					drawx + tileSize,
					drawx,
					drawx,
					drawx + avgsFull[NONE.ordinal()]*3/4,
					drawx + avgsFull[NONE.ordinal()]
			}, new int[] {
					drawy + avgsHalf[NORTHEAST.ordinal()],
					drawy,
					drawy,
					drawy + tileSize/2 + avgsHalf[SOUTHWEST.ordinal()],
					drawy + avgsFull[NONE.ordinal()],
					drawy + avgsFull[NONE.ordinal()]*3/4
			}, 6);
			g.fillPolygon(p);
		}

		if (bitmap == (NONE.tilingBit | SOUTH.tilingBit | NORTHWEST.tilingBit | SOUTHEAST.tilingBit | SOUTHWEST.tilingBit)) {
			Polygon p = new Polygon(new int[] {
					drawx + tileSize,
					drawx + tileSize,
					drawx,
					drawx,
					drawx + avgsFull[NONE.ordinal()]*3/4,
					drawx + avgsFull[NONE.ordinal()]
			}, new int[] {
					drawy + tileSize - avgsHalf[SOUTHEAST.ordinal()],
					drawy + tileSize,
					drawy + tileSize,
					drawy + tileSize/2 - avgsHalf[NORTHWEST.ordinal()],
					drawy + tileSize - avgsFull[NONE.ordinal()],
					drawy + tileSize - avgsFull[NONE.ordinal()]*3/4
			}, 6);
			g.fillPolygon(p);
		}

		if (bitmap == (NONE.tilingBit | SOUTH.tilingBit | NORTHEAST.tilingBit | SOUTHEAST.tilingBit | SOUTHWEST.tilingBit)) {
			Polygon p = new Polygon(new int[] {
					drawx,
					drawx,
					drawx + tileSize,
					drawx + tileSize,
					drawx + tileSize - avgsFull[NONE.ordinal()]*3/4,
					drawx + tileSize - avgsFull[NONE.ordinal()]
			}, new int[] {
					drawy + tileSize - avgsHalf[SOUTHWEST.ordinal()],
					drawy + tileSize,
					drawy + tileSize,
					drawy + tileSize/2 - avgsHalf[NORTHEAST.ordinal()],
					drawy + tileSize - avgsFull[NONE.ordinal()],
					drawy + tileSize - avgsFull[NONE.ordinal()]*3/4
			}, 6);
			g.fillPolygon(p);
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
			Polygon p = new Polygon(new int[] {
					drawx,
					drawx + tileSize,
					drawx + tileSize,
					drawx,
			}, new int[] {
					drawy,
					drawy,
					drawy + tileSize,
					drawy + tileSize,
			}, 4);
			g.fillPolygon(p);
		}
		
		Utils.setTransparency(g, 1);
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
