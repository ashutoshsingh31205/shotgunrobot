package com.rustbyte.level;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.rustbyte.level.Tile;
import com.rustbyte.vector.Vector2;
import com.rustbyte.Game;
import com.rustbyte.Bitmap;
import com.rustbyte.Entity;
import com.rustbyte.Human;
import com.rustbyte.Game;
import com.rustbyte.Player;

public class Level {	
	public int width;
	public int height;
	public int tileWidth;
	public int tileHeight;
	private Tile[] map;
	
	public int viewX;
	public int viewY;
	public int viewWidth;
	public int viewHeight;
	public Game game = null;
	private Tile lastUnlockedSpawnLocation = null;
	
	public Level(int w, int h, int tw, int th, Game g) {
		width = w;
		height = h;
		tileWidth = tw;
		tileHeight = th;		
		map = new Tile[w * h];
		game = g;
	}
	
	public Level(Bitmap bitmap, int tw, int th, Game g) {
		width = bitmap.width;
		height = bitmap.height;
		tileWidth = tw;
		tileHeight = th;
		game = g;
		map = new Tile[width * height];
		for(int i=0; i < (width * height);i++) {
			Tile t = Tile.createTile(bitmap.pixels[i] & 0xFFFFFF,
									 i % width, i / width, 
									 tileWidth, tileHeight, this);
			t.tileID = i;
			map[i] = t;
		}
		
		for(int i=0; i < (width * height);i++)
			map[i].init();			
	}
	public Player getPlayer() {
		return this.game.getPlayer();
	}
	
	public void setPlayerSpawn(Tile t) {
		lastUnlockedSpawnLocation = t;
	}
	public Tile getPlayerSpawn() {
		Tile spawnLocation = null;
		if(lastUnlockedSpawnLocation != null) {
			spawnLocation = lastUnlockedSpawnLocation;
		} else {
			for(int i=0; i < map.length; i++)
				if( map[i] instanceof PlayerSpawnTile ) {
					spawnLocation = map[i];
					break;
				}
		}
		return spawnLocation;
	}
	public Tile getTile(int tx, int ty) {
		if(tx < 0 || tx > (width-1) || ty < 0 || ty > (height-1)) return null;
		return ((Tile)map[tx + ty * width]);
	}
	public Tile getTileFromPoint(double px, double py) {
		int tx = (int)(px / (double)tileWidth);
		int ty = (int)(py / (double)tileHeight);
		return getTile(tx,ty);
	}
	public Tile getTileFromID(int id) {
		return map[id];
	}
	public void setViewPos(int vx, int vy) {
		viewX = vx - (viewWidth / 2);
		viewY = vy - (viewHeight / 2);
		if(viewX < 0) viewX = 0;
		if(viewX > (width * tileWidth) - viewWidth) viewX = (width * tileWidth) - viewWidth;
		if(viewY < 0) viewY = 0;
		if(viewY > (height * tileHeight) - viewHeight) viewY = (height * tileHeight) - viewHeight;		
	}
	
	public void tick() {
		for(int i=0; i < (width * height); i++)
			map[i].tick();
	}
	public void draw(Bitmap dest) {
		for(int i=0; i < (width * height); i++) {
			Tile t = (Tile)map[i];
			if( t instanceof EmptyTile) continue;
			
			int xx = (i % width) * tileWidth;
			int yy = (i / width) * tileHeight;
			if( (xx + tileWidth) < viewX || ( xx > (viewX + viewWidth))) continue;
			if( (yy + tileHeight) < viewY || ( yy > (viewY + viewHeight))) continue;						
			
			t.draw(dest, xx - viewX, yy - viewY);
		}
	}
	
	public boolean moveEntity(Entity ent, double dx, double dy) {
		if(dx == 0 && dy == 0) 
			return true;
		
		double xo = ent.xx;
		double yo = ent.yy;
		double xv = dx < 0.0 ? Math.floor(dx) : Math.ceil(dx);
		double yv = dy;		
		double xr = ent.xr;
		double yr = ent.yr;		
				
		if( dx != 0.0) {
			if((xo - xr + dx) < 0) { 
				ent.xx = 1.0 + xr; 
				return false; 
			}
			if((xo + xr + dx) >= width * tileWidth) { 
				ent.xx = (width * tileWidth) - xr - 1.0; 
				return false; 
			}			
		}		
		
		if( dy != 0.0) {
			if((yo - yr + dy) < 0) {
				ent.yy = 1.0 + yr;
				return false;
			}
			if((yo + yr + dy) >= height * tileHeight) {
				ent.yy = (height * tileHeight) - yr - 1.0;
				return false;
			}
		}
		// Get min/max tile currently standing on
		// NOTE: Right now we only use the tile directly under entity center point.
		//       Not sure if this will cut it in the future. Previous version use xr/yr
		//       to include additional tiles the entity touch.
		/*int xtoMin = (int)(xo) / tileWidth;
		int ytoMin = (int)(yo) / tileHeight;
		int xtoMax = (int)(xo) / tileWidth;
		int ytoMax = (int)(yo) / tileHeight;*/
		
		// Get min/max tile that we move to
		int xtMin = (int)((xo + xv) - xr) / tileWidth;
		int ytMin = (int)((yo + yv) - yr) / tileHeight;
		int xtMax = (int)((xo + xv) + xr) / tileWidth;
		int ytMax = (int)((yo + yv) + yr) / tileHeight;
				
		
		for(int yt=ytMin; yt <= ytMax; yt++) {
			for(int xt=xtMin; xt <= xtMax; xt++) {
				//if(xt >=xtoMin && xt <= xtoMax && yt >= ytoMin && yt <= ytoMax)
				//	continue;
				Tile t = getTile(xt,yt);
				if(t == null) continue;
				
				if(t.sloped) {
					double dir = Math.signum(xv);
					dir = xr * (dir == 0.0 ? 1.0 : dir);
					System.out.println("dir: " + dir + " xr: " + xr);
					
					// calculate hight based on how far in the entity is
					double ydelta = Math.abs((ent.xx + dir)  - (t.tx * 20));
					//System.out.println("ydelta: " + ydelta);

					ent.yy = (((yt * 20) + 20) - (ydelta)) - ent.yr;					
					ent.velocity.y = 0.0;
					ent.onground = true;
					return true;
				} else {				
					if(tileIsBlocking(xt,yt)) {
						if( dy > 0) {			
							ent.yy = (yt * tileHeight) - yr - 1;
							ent.velocity.y = 0.0;
							ent.onground = true;
						}
						if( dy < 0) {
							ent.yy = ((yt * tileHeight) + tileHeight) + yr;
							ent.velocity.y = 0.0;						
						}
						if( dx > 0) {
							ent.xx = (xt * tileWidth) - xr - 1;						
							ent.velocity.x = 0.0;
						}
						if( dx < 0) {
							ent.xx = ((xt * tileWidth) + tileWidth) + xr;
							ent.velocity.x = 0.0;						
						}
						return false;
					}
				}
			}
		}
		
		return true;
	}
		
	private boolean tileIsBlocking(int tx, int ty) {
		if(tx < 0 || tx > (width-1) || ty < 0 || ty > (height-1)) return true;
		return getTile(tx,ty).blocking;
	}
	private boolean pointInTile(double px, double py, int tx, int ty) {
		
		int tx1 = tx * tileWidth;
		int ty1 = ty * tileHeight;
		int tx2 = tx1 + tileWidth;
		int ty2 = ty1 + tileHeight;
		
		if( (px >= tx1) && (px <= tx2) &&
			(py >= ty1) && (py <= ty2) ) {
			return true;
		}
		
		return false;
	}

}
