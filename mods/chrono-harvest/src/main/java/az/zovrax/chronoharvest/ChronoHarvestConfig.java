package az.zovrax.chronoharvest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.Path;
public final class ChronoHarvestConfig {
 private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
 private static final Path FILE=FabricLoader.getInstance().getConfigDir().resolve("chrono-harvest.json");
 public boolean enabled=true;
 public int secondsPerStage=240;
 public boolean requireShiftForImmatureHarvest=true;
 public boolean regrowAfterHarvest=true;
 public static ChronoHarvestConfig load(){
  try { if(Files.isRegularFile(FILE)){ ChronoHarvestConfig c=GSON.fromJson(Files.readString(FILE),ChronoHarvestConfig.class); if(c!=null){c.sanitize();return c;} } }
  catch(Exception e){System.err.println("[ChronoHarvest] Config load failed: "+e);}
  return new ChronoHarvestConfig();
 }
 public void sanitize(){secondsPerStage=Math.clamp(secondsPerStage,30,3600);}
 public void save(){sanitize();try{Files.createDirectories(FILE.getParent());Files.writeString(FILE,GSON.toJson(this)+"\n");}catch(Exception e){System.err.println("[ChronoHarvest] Config save failed: "+e);}}
}
