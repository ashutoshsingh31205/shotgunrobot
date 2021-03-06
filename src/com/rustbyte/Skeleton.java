package com.rustbyte;

import java.util.List;

import com.rustbyte.level.Tile;
import com.rustbyte.vector.Vector2;

public class Skeleton extends Mob {
	private int ANIM_WALK_RIGHT;
	private int ANIM_WALK_LEFT;
	private int ANIM_IDLE_LEFT;
	private int ANIM_IDLE_RIGHT;
	private int actionTimer = 0;
	private Entity currentTarget = null;
	private boolean targetAquired = false;
	
	public Skeleton(double x, double y, int w, int h, Entity p, Game g) {
		super(x, y, w, h, p, g);

		ANIM_IDLE_RIGHT = animator.addAnimation(7, 121,164, w, h, false, 1);
		ANIM_IDLE_LEFT = animator.addAnimation(7, 121,164, w, h, true, 1);
		ANIM_WALK_RIGHT = animator.addAnimation(8, 100,185, w, h, false, 1);
		ANIM_WALK_LEFT = animator.addAnimation(8, 100,185, w, h, true, 1);
				
		animator.setCurrentAnimation(ANIM_IDLE_LEFT);
		
		hitpoints = 100;
		speed = 0.75;
	}

	@Override
	public void tick() {
		super.tick();
		
		if(hitpoints <= 0)
			alive = false;
				
		facing = -1;
		
		if(hurtTimer <= 0)
			knockedBack = false;
		
		if(!knockedBack) {
			if(--actionTimer <= 0 && currentTarget == null) {
				int tempNewDir = 0;
				rand.setSeed(System.nanoTime());
				switch(1 + rand.nextInt(3)) {
				case 1:
					tempNewDir = 0;
					break;
				case 2:
					tempNewDir = -1;
					break;
				case 3:
					tempNewDir = 1;
					break;
				}
				dirX = tempNewDir;
				actionTimer = 100 + rand.nextInt(200);
			}
			
			// Try to find something to chew on..........
			if( currentTarget == null) {							
				Tile currentTile = game.level.getTileFromPoint(xx,yy);
				
				// "Sence" things two tiles behind, and "see" things 4 tiles infront.
				int txStart = currentTile.tx + ( (-facing) * 2);
				int tyStart = currentTile.ty;
				for(int i=0; i < 7 && currentTarget == null; i++) {
					Tile nextTile = game.level.getTile(txStart + (facing * i), tyStart);
					if( nextTile == null || nextTile.blocking) {
						//System.out.println("Wall blocking my senses.");
						break;
					}
					List<Entity> ents = nextTile.getEntities();
					for(int j=0; j < ents.size();j++) {
						Entity nextEntity = ents.get(j);
						if(  nextEntity instanceof Player ||
							 nextEntity instanceof Human) {
							
							// Yummy...this one looks fat and tasty....
							//System.out.println("Target aquired.");
							currentTarget = nextEntity;
							targetAquired = true;
							break;
						}
					}
				}
			} else {
				dirX = currentTarget.xx < xx ? -1 : 1;
				
				// Got something juciy to succle on....
				Vector2 v1 = new Vector2(xx,yy);
				Vector2 v2 = new Vector2(currentTarget.xx, currentTarget.yy);
				Vector2 v3 = v1.sub(v2);
				if(v3.length() < 5) {
					if(currentTarget.alive)
						((Mob)currentTarget).takeDamage(this, 20);
				} else if( v3.length() > 90 ) {
					// Rabbit got away from me....
					currentTarget = null;
				}
				if( currentTarget != null && currentTarget.alive) {
					Tile myTile = game.level.getTileFromPoint(xx,yy);
					Tile targetTile = game.level.getTileFromPoint(currentTarget.xx, currentTarget.yy);
					int yStart = myTile.ty < targetTile.ty ? myTile.ty : targetTile.ty;
					int yEnd = myTile.ty < targetTile.ty ? targetTile.ty : myTile.ty;
					if(myTile.ty != targetTile.ty) {
						for(int y=yStart; y < yEnd; y++) {
							Tile t = game.level.getTile(myTile.tx,y);
							if(t != null && t.blocking) {
								System.out.println("Roof/floor blocking my path!");
								currentTarget = null;
							} 
						}
					}					
				}
			}
			
			if((currentTarget == null || !currentTarget.alive) && targetAquired) {
				//System.out.println("Lost target");
				currentTarget = null;
				targetAquired = false;
			}
			if(blockedX && onground) {
				int tx = (int)this.xx / game.level.tileWidth;
				int ty = (int)this.yy / game.level.tileHeight;
				boolean jumpObstacle = false;
				for(int i=0; i < 2; i++) {
					Tile t = game.level.getTile(tx + dirX, ty - (i+1));
					if( t == null || !t.blocking) {
						jump();
						jumpObstacle = true;
						break;
					}
				}
				if(!jumpObstacle)
					dirX = -dirX;
			}
			velocity.x = dirX * ( speed * (currentTarget == null ? 1 : 2));
		}
		
		if(externalForce.x != 0.0)
			velocity = velocity.add(externalForce);
		
		move();
		
		if(!knockedBack) {
			if(velocity.x < 0 ) this.animator.setCurrentAnimation(ANIM_WALK_LEFT);
			else if(velocity.x > 0) this.animator.setCurrentAnimation(ANIM_WALK_RIGHT);
			else {
				if(facing == -1) this.animator.setCurrentAnimation(ANIM_IDLE_LEFT);
				if(facing == 1) this.animator.setCurrentAnimation(ANIM_IDLE_RIGHT);
			}
		}
		
		animator.tick();		
	}
	
	@Override
	public void takeDamage(Entity source, int amount) {
		hitpoints -= amount;
		if(hitpoints > 0) {
			hurt(20);
			this.knockBack( (source.xx - xx), 2.0 );
		} else {
			// initiate death-sequence
			if( source instanceof Grenade )
				this.explode(16, Art.getColor(255,0,0), 50);
			else
				this.breakApart(16, Art.getColor(255,0,0), 10);
			
			Powerup p = Powerup.createPowerup(Powerup.POWERUP_TYPE_BATTERY, (int)xx, (int)yy, null, game);
			p.velocity.x = -1.5;
			p.velocity.y = -2.5;
			game.addEntity( p );
			
			game.zombiesKilled++;
		}
		game.addEntity(new FloatingText("-" + amount, Art.getColor(255, 255, 0), xx,yy - 10, new Vector2(0,-1), null, game));
		int px = 0;
		int py = -10;
		ParticleEmitter pe = new ParticleEmitter(px, py, (double)source.facing, -1.0, 1, 10, Art.getColor(255,0,0), this, game);
		game.addEntity(pe);		
		
	}

	@Override
	public void render() throws Exception {
		if(isHurt()) {
			flashEffect.clear();
			animator.render(flashEffect.renderFrame, 0, 0);			
			flashEffect.render(game.tickcount, game.screen, (((int)xx) - (wid / 2)) - game.level.viewX, 
				   	  					    			    (((int)yy) - (hgt / 2)) - game.level.viewY);
		} else {		
			animator.render(game.screen, (((int)xx) - (wid / 2)) - game.level.viewX, 
	                				   	 (((int)yy) - (hgt / 2)) - game.level.viewY);
		}				
	}

}
