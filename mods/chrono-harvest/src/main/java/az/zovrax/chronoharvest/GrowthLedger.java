package az.zovrax.chronoharvest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
public final class GrowthLedger {
 private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
 private static final Type TYPE=new TypeToken<Map<String,Long>>(){}.getType();
 private static final Map<String,Long> PLANTED=new HashMap<>();
 private static Path file; private static long lastSave; private static MinecraftServer server;
 private GrowthLedger(){}
 public static boolean supported(BlockState s){var b=s.getBlock();return b instanceof CropBlock||b instanceof NetherWartBlock||b instanceof SweetBerryBushBlock||b instanceof CocoaBlock;}
 public static IntProperty ageProperty(BlockState s){for(Property<?> p:s.getProperties())if(p instanceof IntProperty a&&"age".equals(p.getName()))return a;return null;}
 private static String key(ServerWorld w,BlockPos p){return w.getRegistryKey().getValue()+"|"+p.asLong();}
 public static void load(MinecraftServer s){
  server=s;file=s.getSavePath(WorldSavePath.ROOT).resolve("data/chrono-harvest-planted.json");PLANTED.clear();
  try{if(Files.isRegularFile(file)){Map<String,Long> m=GSON.fromJson(Files.readString(file),TYPE);if(m!=null)PLANTED.putAll(m);}}
  catch(Exception e){System.err.println("[ChronoHarvest] Timestamp load failed: "+e);}lastSave=0;
 }
 public static void save(MinecraftServer s){
  if(file==null)return;
  try{Files.createDirectories(file.getParent());Path tmp=file.resolveSibling(file.getFileName()+".tmp");Files.writeString(tmp,GSON.toJson(PLANTED));
   try{Files.move(tmp,file,java.nio.file.StandardCopyOption.REPLACE_EXISTING,java.nio.file.StandardCopyOption.ATOMIC_MOVE);}
   catch(java.nio.file.AtomicMoveNotSupportedException e){Files.move(tmp,file,java.nio.file.StandardCopyOption.REPLACE_EXISTING);}
  }catch(Exception e){System.err.println("[ChronoHarvest] Timestamp save failed: "+e);}
 }
 public static void periodicSave(){if(server!=null&&server.getTicks()-lastSave>=200){save(server);lastSave=server.getTicks();}}
 public static void markPlanted(ServerWorld w,BlockPos p){PLANTED.put(key(w,p),System.currentTimeMillis());}
 public static void forget(ServerWorld w,BlockPos p){PLANTED.remove(key(w,p));}
 public static boolean applyElapsedGrowth(ServerWorld w,BlockPos p){
  if(!ChronoHarvest.CONFIG.enabled)return false;BlockState s=w.getBlockState(p);if(!supported(s))return false;
  IntProperty age=ageProperty(s);if(age==null)return false;
  long planted=PLANTED.computeIfAbsent(key(w,p),k->System.currentTimeMillis());
  long elapsed=Math.max(0,System.currentTimeMillis()-planted), stageMs=ChronoHarvest.CONFIG.secondsPerStage*1000L;
  int max=age.getValues().stream().mapToInt(Integer::intValue).max().orElse(0);
  int expected=(int)Math.min(max,elapsed/stageMs), current=s.get(age);
  if(expected>current)w.setBlockState(p,s.with(age,expected),3);
  return true;
 }
 public static void onChunkLoad(ServerWorld w,WorldChunk c){
  if(!ChronoHarvest.CONFIG.enabled)return;int sx=c.getPos().getStartX(),sz=c.getPos().getStartZ();
  for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=w.getBottomY();y<w.getTopY();y++){
   BlockPos p=new BlockPos(sx+x,y,sz+z);if(supported(c.getBlockState(p)))applyElapsedGrowth(w,p);
  }
 }
}
