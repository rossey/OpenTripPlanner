package org.opentripplanner.routing.algorithm;

import java.io.File;

import junit.framework.TestCase;

import org.opentripplanner.model.calendar.CalendarServiceData;
import org.opentripplanner.ConstantsForTests;
import org.opentripplanner.gtfs.GtfsContext;
import org.opentripplanner.gtfs.GtfsLibrary;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.edgetype.factory.PatternHopFactory;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.opentripplanner.util.TestUtils;

import static org.opentripplanner.calendar.impl.CalendarServiceDataFactoryImpl.createCalendarServiceData;

public class TestAStar extends TestCase {
    
    AStar aStar = new AStar();

    public void testBasic() throws Exception {

        GtfsContext context = GtfsLibrary.readGtfs(new File(ConstantsForTests.CALTRAIN_GTFS));

        Graph gg = new Graph();
        PatternHopFactory factory = new PatternHopFactory(context);
        factory.run(gg);
        gg.putService(
                CalendarServiceData.class,
                createCalendarServiceData(context.getOtpTransitService())
        );
        RoutingRequest options = new RoutingRequest();

        ShortestPathTree spt;
        GraphPath path = null;

        String feedId = gg.getFeedIds().iterator().next();
        options.dateTime = TestUtils.dateInSeconds("America/Los_Angeles", 2009, 8, 7, 12, 0, 0);
        options.setRoutingContext(gg, feedId + ":Millbrae Caltrain", feedId + ":Mountain View Caltrain");
        spt = aStar.getShortestPathTree(options);
        path = spt.getPath(gg.getVertex(feedId + ":Mountain View Caltrain"), true);

        long endTime = TestUtils.dateInSeconds("America/Los_Angeles", 2009, 8, 7, 13, 29, 0);

        assertEquals(path.getEndTime(), endTime);

        /* test backwards traversal */
        options.setArriveBy(true);
        options.dateTime = endTime;
        options.setRoutingContext(gg, feedId + ":Millbrae Caltrain", feedId + ":Mountain View Caltrain");
        spt = aStar.getShortestPathTree(options);
        path = spt.getPath(gg.getVertex(feedId + ":Millbrae Caltrain"), true);

        long expectedStartTime = TestUtils.dateInSeconds("America/Los_Angeles", 2009, 8, 7, 12, 39, 0);

        assertTrue(path.getStartTime() - expectedStartTime <= 1);

    }

    public void testMaxTime() {

        Graph graph = ConstantsForTests.getInstance().getPortlandGraph();
        String feedId = graph.getFeedIds().iterator().next();
        Vertex start = graph.getVertex(feedId + ":8371");
        Vertex end = graph.getVertex(feedId + ":8374");

        RoutingRequest options = new RoutingRequest();
        long startTime = TestUtils.dateInSeconds("America/Los_Angeles", 2009, 11, 1, 12, 34, 25);
        options.dateTime = startTime;
        // one hour is more than enough time
        options.worstTime = startTime + 60 * 60; 
        options.setRoutingContext(graph, start, end);

        ShortestPathTree spt = aStar.getShortestPathTree(options);
        GraphPath path = spt.getPath(end, true);
        assertNotNull(path);
        
        // but one minute is not enough
        options.worstTime = startTime + 60; 
        spt = aStar.getShortestPathTree(options);
        path = spt.getPath(end, true);
        assertNull(path);        
    }

}
