package com.rustbyte.level;

import java.util.Random;

import com.rustbyte.Human;

public class HumanSpawnerTile extends Tile {	
	public HumanSpawnerTile(int x, int y, int wid, int hgt, Level lev) {
		super(x, y, wid, hgt, lev);
		this.baseColor = 0x00FF21;
		this.tsetOffsetX = 1;
		this.tsetOffsetY = 43;
	}
	@Override
	public void init() {	
		Random rand = new Random();
		rand.setSeed(level.game.tickcount);
		
		for(int i = 0; i < 5 + rand.nextInt(5); i++) {
			level.game.addEntity(new Human(tx * 20, ty * 20,20,20,null, level.game));
		}
	}
}
