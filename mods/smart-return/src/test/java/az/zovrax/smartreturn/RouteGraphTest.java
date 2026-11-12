package az.zovrax.smartreturn;

import com.google.gson.JsonObject;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Tests are pure route calculations; no Minecraft window is needed. */
final class RouteGraphTest {
    private static Vec3d p(double x,double y,double z){return new Vec3d(x,y,z);}
    private static final java.util.function.BiPredicate<Vec3d,Vec3d> OPEN=(a,b)->true;

    @Test void choosesShortestOfTwoEntrancesByTraversedDistance(){
        RouteGraph cave=new RouteGraph(p(0,60,0));
        for(int z=1;z<=22;z++)cave.sample(p(0,60,z),OPEN);
        cave.addExitNear(p(0,60,22));
        RouteGraph.Path far=cave.homeward(p(0,60,18));
        assertNotNull(far);
        assertEquals(22,far.exit().z,2.1);
        RouteGraph.Path near=cave.homeward(p(0,60,3));
        assertNotNull(near);
        assertEquals(0,near.exit().z,2.1);
    }

    @Test void remembersSeparateEntrancesWhenNetworksMeet(){
        RouteGraph east=new RouteGraph(p(0,70,0));
        for(int z=1;z<=20;z++)east.sample(p(0,70,z),OPEN);
        RouteGraph west=new RouteGraph(p(5,70,20));
        for(int x=4;x>=0;x--)west.sample(p(x,70,20),OPEN);
        assertTrue(east.mergeAt(west,p(0,70,20),OPEN));
        assertEquals(2,east.entrances().size());
        assertNotNull(east.homeward(p(0,70,18)));
        RouteGraph.Path best=east.homeward(p(0,70,18));
        assertEquals(5,best.exit().x,1.0);
        assertEquals(20,best.exit().z,1.0);
    }

    @Test void noTeleportShortcutsAndWorldGeometryGateConnections(){
        RouteGraph route=new RouteGraph(p(0,40,0));
        route.sample(p(0,40,3),OPEN);
        int before=route.count();
        route.sample(p(0,40,80),OPEN);
        assertEquals(before,route.count(),"Teleport should not add a fantasy edge");
        // Through-wall new segment is deliberately disconnected.
        route.sample(p(0,40,83),(a,b)->false);
        assertNull(route.homeward(p(0,40,83)));
    }

    @Test void oldBetaJsonLoadsAndNewFormatPersistsExits(){
        RouteGraph cave=new RouteGraph(p(0,50,0));
        for(int i=1;i<=14;i++)cave.sample(p(0,50,i),OPEN);
        cave.addExitNear(p(0,50,14));
        RouteGraph restored=RouteGraph.fromJson(cave.toJson());
        assertEquals(2,restored.entrances().size());
        assertNotNull(restored.homeward(p(0,50,7)));
        JsonObject old=cave.toJson();
        old.addProperty("format",1);
        old.remove("exits");
        RouteGraph migrated=RouteGraph.fromJson(old);
        assertEquals(1,migrated.entrances().size());
        assertNotNull(migrated.homeward(p(0,50,10)));
    }
}
