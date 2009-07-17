/**
 * This is another example of a keystoned surface, this time using Andres
 * Colubri's GLGraphics library. The library has an offscreen renderer that
 * will draw your sketch to an OpenGL texture.
 *
 * To run this example, you'll need the GLGraphics library. Get it here:
 * http://users.design.ucla.edu/~acolubri/processing/glgraphics/home/ 
 */

import processing.opengl.*;
import codeanticode.glgraphics.*;

import deadpixel.keystone.*;

// this object is key! you can use it to render fully accelerated
// OpenGL scenes directly to a texture
GLGraphicsOffScreen offscreen;

Keystone ks;
CornerPinSurface surface;

void setup() {
  size(1024, 768, GLConstants.GLGRAPHICS);

  offscreen = new GLGraphicsOffScreen(this, width, height);

  ks = new Keystone(this);
  surface = ks.createCornerPinSurface(width, height, 20);
}

void draw() {
  // convert 
  PVector mouse = surface.getTransformedMouse();

  // first draw the sketch offscreen
  offscreen.beginDraw();
  offscreen.background(50);
  offscreen.lights();
  offscreen.fill(255);
  offscreen.translate(mouse.x, mouse.y);
  offscreen.rotateX(millis()/200.0);
  offscreen.rotateY(millis()/400.0);
  offscreen.box(100);
  offscreen.endDraw();

  // then render the sketch using the 
  // keystoned surface
  background(0);
  surface.render(offscreen.getTexture());
}

// Controls for the Keystone object
void keyPressed() {

  switch(key) {
  case 'c':
    // enter/leave calibration mode, where surfaces can be warped 
    // & moved
    ks.toggleCalibration();
    break;

  case 'l':
    // loads the saved layout
    ks.load();
    break;

  case 's':
    // saves the layout
    ks.save();
    break;
  }
}
