package com.rustbyte;

public class Bitmap {
	public static String chararray = "ABCDEFGHIJKLMNOPQRSTUVWXYZ.,!?\"�/\\<>()[]{}" +
									 "abcdefghijklmnopqrstuvwxyz_               " +
									 "0123456789+-=*:;                          ";
	public static final int FONTWIDTH = 6;
	public static final int FONTHEIGHT = 8;
	
	public int width;
	public int height;
	public int[] pixels;
	public Bitmap(int w, int h) {
		width = w;
		height = h;
		pixels = new int[width * height];
	}
	
	public void clear(int col){
		if(col == -1)
			col = 0xFF5DFF;
		for(int i=0; i < width * height; i++)
			pixels[i] = col;
	}
	
	public void draw(Bitmap dest, int x, int y) {
		for(int yy=0; yy<height;yy++) {
			for(int xx=0; xx<width; xx++) {
				dest.pixels[(x + xx) + ((y + yy)*dest.width)] = pixels[xx + yy * width];
			}
		}
	}
	public void draw(Bitmap dest, int dx, int dy, int sx, int sy, int w, int h) {
		
		if( ((dx + w) < 0) || (dx >= dest.width) ) return;
		if( ((dy + h) < 0) || (dy >= dest.height) ) return;
		
		int xOffset = 0;
		int yOffset = 0;
		int xDest = dx;
		int yDest = dy;
		if( dx < 0 ) {
			xOffset = -dx;
			xDest = 0;
		}
		if( dy < 0 ) {
			yOffset = -dy;
			yDest = 0;
		}
		if( (dx + w) > dest.width ) {
			w -= ((dx + w) - dest.width);
		}
		if( (dy + h) > dest.height ) {
			h -= ((dy + h) - dest.height);
		}
		
		for(int yy=yOffset; yy < h; yy++) {
			for(int xx=xOffset; xx < w; xx++) {
				int srcCol = pixels[(sx + xx) + (sy + yy) * width];
				if( (srcCol & 0xFFFFFF) != 0xFF5DFF )
					dest.pixels[(xDest + (xx-xOffset)) + (yDest + (yy-yOffset)) * dest.width] = srcCol;
			}
		}
	}
	
	public void draw(Bitmap dest, int dx, int dy, int sx, int sy, int w, int h, boolean flip) {
		
		if( ((dx + w) < 0) || (dx >= dest.width) ) return;
		if( ((dy + h) < 0) || (dy >= dest.height) ) return;
		
		int xOffset = 0;
		int yOffset = 0;
		int xDest = dx;
		int yDest = dy;
		int frameWidth = w;
		int frameHeight = h;
		
		if( dx < 0 ) {
			xOffset = -dx;
			xDest = 0;
		}
		if( dy < 0 ) {
			yOffset = -dy;
			yDest = 0;
		}
		if( (dx + frameWidth) > dest.width ) {			
			frameWidth -= ((dx + frameWidth) - dest.width);
		}
		if( (dy + frameHeight) > dest.height ) {			
			frameHeight -= ((dy + frameHeight) - dest.height);
		}
		
		for(int yy=yOffset; yy < frameHeight; yy++) {
			for(int xx=xOffset; xx < frameWidth; xx++) {
				int offsX;
				
				if(flip) 
					offsX = (sx + w - 1) - xx;
				else offsX = (sx + xx);
				
				int srcCol = pixels[(offsX) + (sy + yy) * width];				
				if( (srcCol & 0xFFFFFF) != 0xFF5DFF )
					dest.pixels[(xDest + (xx-xOffset)) + (yDest + (yy-yOffset)) * dest.width] = srcCol;
			}
		}
	}
	
	public void drawText(Bitmap fontBitmap, String text, int xOffset, int yOffset, int col, boolean shadow) {
		if(shadow) {			
			drawText(fontBitmap, text,xOffset + 1 , yOffset   , 0);
			drawText(fontBitmap, text,xOffset + 1 , yOffset + 1   , 0);
		}
		drawText(fontBitmap, text,xOffset, yOffset, col);
	}
	private void drawText(Bitmap fontBitmap, String text, int xOffset, int yOffset, int col) {
				
		int charIndex = -1;
		
		int fontOffsetX = -1; 
		int fontOffsetY = -1;
		
		int xFinal = -1;
		int yFinal = -1;
		
		for(int i=0; i < text.length(); i++) {			
			charIndex = chararray.indexOf(text.charAt(i));
			
			fontOffsetX = (charIndex % 42) * FONTWIDTH;
			fontOffsetY = (charIndex / 42) * FONTHEIGHT;
			
			for(int y=fontOffsetY; y < (fontOffsetY + FONTHEIGHT); y++) {
				yFinal = yOffset + (y - fontOffsetY);
				if(yFinal < 0 || yFinal >= height) continue;
				
				for(int x=fontOffsetX; x < (fontOffsetX + FONTWIDTH); x++) {
					xFinal = (xOffset + (x - fontOffsetX)) + (i * FONTWIDTH);
					if(xFinal < 0 || xFinal >= width) continue;
					
					int src = fontBitmap.pixels[x + y * fontBitmap.width];
					if((src & 0xffffff) != 0xFF5DFF)
						pixels[xFinal + yFinal * width] = col;
				}
			}
		}		
	}

	public void drawScaled(Bitmap dest, int dx, int dy, int scaleX, int scaleY,
						                int sx, int sy, int w, int h, boolean flip) {
		if( ((dx + w) < 0) || (dx >= dest.width) ) return;
		if( ((dy + h) < 0) || (dy >= dest.height) ) return;
		
		int xOffset = 0;
		int yOffset = 0;
		int xDest = dx;
		int yDest = dy;
		int frameWidth = w * scaleX;
		int frameHeight = h * scaleY;
		
		if( dx < 0 ) {
			xOffset = -dx;
			xDest = 0;
		}
		if( dy < 0 ) {
			yOffset = -dy;
			yDest = 0;
		}
		if( (dx + frameWidth) > dest.width ) {			
			frameWidth -= ((dx + frameWidth) - dest.width);
		}
		if( (dy + frameHeight) > dest.height ) {			
			frameHeight -= ((dy + frameHeight) - dest.height);
		}
		
		for(int yy=yOffset; yy < frameHeight; yy++) {
			for(int xx=xOffset; xx < frameWidth; xx++) {
				int offsX;
				
				if(flip) 
					offsX = (sx + w - 1) - xx / scaleX;
				else offsX = (sx + xx / scaleX);
				
				int srcCol = pixels[(offsX) + (sy + yy / scaleY) * width];				
				if( (srcCol & 0xFFFFFF) != 0xFF5DFF )
					dest.pixels[(xDest + (xx-xOffset)) + (yDest + (yy-yOffset)) * dest.width] = srcCol;
			}
		}		
	}	
}
