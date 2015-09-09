/**
 * Copyright (C) 2009-15 David Bouchard
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

import java.util.ArrayList;
import processing.awt.PGraphicsJava2D;
import processing.core.*;
import processing.data.XML;
import processing.event.MouseEvent;
import processing.opengl.PGraphics3D;
 
/**
 * This class manages the creation and calibration of keystoned surfaces.
 * 
 * To move and warp surfaces, place the Keystone object in calibrate mode. It catches mouse events and 
 * allows you to drag surfaces and control points with the mouse.
 *
 * The Keystone object also provides load/save functionality, once you've calibrated the layout to 
 * your liking. 
 * 
 * Version: 0.31
 */
public class Keystone {

	public final String VERSION = "006";

	PApplet parent;

	ArrayList<CornerPinSurface> surfaces;

	Draggable dragged;

	// calibration mode is application-wide, so I made this flag static
	// there should only be one Keystone object around anyway
	static boolean calibrate;

	/**
	 * @param parent
	 *            applet
	 */
	public Keystone(PApplet parent) {
		this.parent = parent;
		this.parent.registerMethod("mouseEvent", this);

		surfaces = new ArrayList<CornerPinSurface>();
		dragged = null;

		// check the renderer type
		// issue a warning if we're not in 3D mode 
		PGraphics pg = parent.g;
		if ((pg instanceof PGraphics3D) == false ) {
			PApplet.println("The keystone library will not work with 2D graphics as the renderer because it relies on texture mapping. " +
					"Try P3D or OPENGL.");
		}
		
		PApplet.println("Keystone " + VERSION);
	}

	/**
	 * Creates and registers a new corner pin keystone surface. 
	 * 
	 * @param w width
	 * @param h height
	 * @param res resolution (number of tiles per axis)
	 * @return
	 */
	public CornerPinSurface createCornerPinSurface(int w, int h, int res) {
		CornerPinSurface s = new CornerPinSurface(parent, w, h, res);
		surfaces.add(s);
		return s;
	}

	/**
	 * Starts the calibration mode. Mouse events will be intercepted to drag surfaces 
	 * and move control points around.
	 */
	public void startCalibration() {
		calibrate = true;
	}
	
	/**
	 * Stops the calibration mode
	 */
	public void stopCalibration() {
		calibrate = false;
	}
	
	/**
	 * Toggles the calibration mode
	 */
	public void toggleCalibration() {
		calibrate = !calibrate;
	}
	
	public boolean isCalibrating() {
		return calibrate;
	}

	/**
	 * Returns the version of the library.
	 * 
	 * @return String
	 */
	public String version() {
		return VERSION;
	}
	
	/**
	 * Saves the layout to an XML file.
	 */
	public void save(String filename) {

		XML root = new XML("keystone");

		// create XML elements for each surface containing the resolution
		// and control point data
		for (CornerPinSurface s : surfaces) {
			XML surface = new XML("surface");
			surface.setInt("res", s.getRes());
			surface.setFloat("x", s.x);
			surface.setFloat("y", s.y);
			surface.setInt("w", s.w);
			surface.setInt("h", s.h);
			for (int i=0; i < s.mesh.length; i++) {
				if (s.mesh[i].isControlPoint()) {
					XML point = new XML("point");
					point.setInt("i", i);
					point.setFloat("x", s.mesh[i].x);
					point.setFloat("y", s.mesh[i].y);
					point.setFloat("u", s.mesh[i].u);
					point.setFloat("v", s.mesh[i].v);
					//TODO: Guy's addition
					//point.setString("id", s.mesh[i].id);
					surface.addChild(point);
				}
			}
			root.addChild(surface);
			
		}
		/*
		// write the settings to keystone.xml in the sketch's data folder
		try {
			OutputStream stream = parent.createOutput(parent.dataPath(filename));
			root.save(stream); 
		} catch (Exception e) {
			PApplet.println(e.getStackTrace());
		}		
		*/
		parent.saveXML(root, filename);
		PApplet.println("Keystone: layout saved to " + filename);
	}
	
	/**
	 * Saves the current layout into "keystone.xml"
	 */
	public void save() {
		save("keystone.xml");
	}

	/**
	 * Loads a saved layout from a given XML file
	 */
	public void load(String filename) {
		XML root = parent.loadXML(filename);
		
		/*
		// Guy's version -- need to figure out why this doesn't work
		surfaces.clear();
		for (int i=0; i < root.getChildCount(); i++) {
			XML surfaceEl = root.getChild(i);
			int w = surfaceEl.getInt("w");
			int h = surfaceEl.getInt("h");
			int res = surfaceEl.getInt("res");
			CornerPinSurface surface = createCornerPinSurface(w, h, res);
			surface.load(surfaceEl);
		}
		*/
		
		XML[] surfaceXML = root.getChildren("surface");
		for (int i=0; i < surfaceXML.length; i++) {
			surfaces.get(i).load(surfaceXML[i]);
		}

		PApplet.println("Keystone: layout loaded from " + filename);
	}
	
	/**
	 * Loads a saved layout from "keystone.xml"
	 */
	public void load() {
		load("keystone.xml");
	}
	

	/**
	 * @invisible
	 */
	public void mouseEvent(MouseEvent e) {
		
		// ignore input events if the calibrate flag is not set
		if (!calibrate)
			return;

		int x = e.getX();
		int y = e.getY();

		switch (e.getAction()) {

		case MouseEvent.PRESS:
			CornerPinSurface top = null;
			// navigate the list backwards, as to select 
			for (int i=surfaces.size()-1; i >= 0; i--) {
				CornerPinSurface s = surfaces.get(i);
				dragged = s.select(x, y);
				if (dragged != null) {
					top = s;
					break;
				}
			}

			if (top != null) {
				// moved the dragged surface to the beginning of the list
				// this actually breaks the load/save order.
				// in the new version, add IDs to surfaces so we can just 
				// re-load in the right order (or create a separate list 
				// for selection/rendering)
				//int i = surfaces.indexOf(top);
				//surfaces.remove(i);
				//surfaces.add(0, top);
			}
			break;

		case MouseEvent.DRAG:
			if (dragged != null)
				dragged.moveTo(x, y);
			break;

		case MouseEvent.RELEASE:
			dragged = null;
			break;
		}
	}

	public CornerPinSurface getSurface(int i) {
		return surfaces.get(i);
	}

	public int getSurfaceCount() {
		return surfaces.size();
	}

	public void clearSurfaces() {
		surfaces.clear();
	}

}
