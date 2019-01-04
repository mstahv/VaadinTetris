# Vaadin Tetris

This is a technology stunt that proves server side Java can work even for
interactive games. Game code runs in a servlet engine (Spring Boot in this case) and the UI is built using
[Vaadin](http://vaadin.com/). The automatic push connection in Vaadin uses 
WebSocket communication if it suits for both client and server (and proxies), 
else falls back to long polling, or streaming. 

Graphics are drawn brutally using the [Canvas addon](https://vaadin.com/directory/component/canvas-java) - on 
each and every game state change. This naturally causes lots of traffic
traffic, but this could be really easily optimized. E.g. using SVG or just simple html table based solution the amount of transfered data
 would be much smaller and only the changes would need to be sent. 
Still the game is playable, even over mobile GSM network.

The example also was an example for Geographical load balancing in a webinar by Vaadin and Amazon about AWS Route 53. If you playt this game from a different continent, where it is served, it may be bit sluggish. In the webinar we deployed it to multiple continents -> making the app good globablly.

The origin of the example is in a talk by Sami Ekblad in "Vaadin and HTML5" in JavaDay Riga, November 2011.
![Screenshot](https://raw.githubusercontent.com/mstahv/VaadinTetris/master/vaadin-tetris.png)

The Tetris game engine tries to follow the guidelines given at  http://tetris.wikia.com/

## Running

Check out the project (or download the zip) and issue following command:

    mvn spring-boot:run

