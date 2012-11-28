/**
 * Copyright (C) 2009 David Bouchard
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package deadpixel.keystone;

/**
 * Represents a single point in the mesh, along with its precomputed (u,v) 
 * texture coordinates. 
 */
public class MeshPoint implements Draggable {
	
	public float x;
	public float y;
	public float u;
	public float v;
	String id = "";

	boolean isControlPoint;
	
	CornerPinSurface parent;
	
	MeshPoint(CornerPinSurface parent, float x, float y, float u, float v) {
		this.x = x;
		this.y = y;
		this.u = u;
		this.v = v;
		this.isControlPoint = false;
		this.parent = parent;
	}
	
	public boolean isControlPoint() {
		return isControlPoint;
	}
	
	public void moveTo(float x, float y) {
		this.x = x - parent.x;
		this.y = y - parent.y;
		//parent.calculateMesh(this.id);
		parent.calculateMesh();
	}
	
	protected void setControlPoint(boolean cp) {
		isControlPoint = cp;
	}
	
	/*
	protected void setControlPoint(String id) {
		isControlPoint = true;
		this.id = id;
	}
	*/
	
	/**
	 * This creates a new MeshPoint with (u,v) = (0,0) and does
	 * not modify the current MeshPoint. Its used to generate 
	 * temporary points for the interpolation.
	 */
	MeshPoint interpolateTo(MeshPoint p, float f) {
		float nX = this.x + (p.x - this.x) * f;
		float nY = this.y + (p.y - this.y) * f;
		return new MeshPoint(parent, nX, nY, 0, 0);
	}
	
	void interpolateBetween(MeshPoint start, MeshPoint end, float f) {
		this.x = start.x + (end.x - start.x) * f;
		this.y = start.y + (end.y - start.y) * f;
	}	
}
