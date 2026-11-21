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
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
public final class GrowthLedger {
 private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
 private static final Type TYPE=new TypeToken<Map<String,Long>>(){}.getType();
 private static final Map<String,Long> PLANTED=new HashMap<>();
 private static final ArrayDeque<ChunkScan> QUEUE=new ArrayDeque<>();
 private static final Map<String,ChunkScan> PENDING=new HashMap<>();
 private static final int BLOCK_BUDGET_PER_TICK=8192;
 private static Path file; private static long lastSave; private static MinecraftServer server;
 private GrowthLedger(){}
 private static final class ChunkScan {
  final ServerWorld world; final WorldChunk chunk; final String key;
  final int startX,startZ,bottomY,totalBlocks; final BlockPos.Mutable pos=new BlockPos.Mutable();
  int cursor;
  ChunkScan(ServerWorld w,WorldChunk c){
   world=w;chunk=c;key=chunkKey(w,c);startX=c.getPos().getStartX();startZ=c.getPos().getStartZ();
   bottomY=w.getBottomY();totalBlocks=w.getHeight()*256;
  }
 }
 public static boolean supported(BlockState s){
  var b=s.getBlock();return b instanceof CropBlock||b instanceof NetherWartBlock||b instanceof SweetBerryBushBlock||b instanceof CocoaBlock;
 }
 public static IntProperty ageProperty(BlockState s){
  for(Property<?> p:s.getProperties())if(p instanceof IntProperty a&&"age".equals(p.getName()))return a;return null;
 }
 private static String key(ServerWorld w,BlockPos p){return w.getRegistryKey().getValue()+"|"+p.asLong();}
 private static String chunkKey(ServerWorld w,WorldChunk c){return w.getRegistryKey().getValue()+"|chunk|"+c.getPos().x+"|"+c.getPos().z;}
 public static void load(MinecraftServer s){
  server=s;file=s.getSavePath(WorldSavePath.ROOT).resolve("data/chrono-harvest-planted.json");
  PLANTED.clear();QUEUE.clear();PENDING.clear();
  try{if(Files.isRegularFile(file)){Map<String,Long> m=GSON.fromJson(Files.readString(file),TYPE);if(m!=null)PLANTED.putAll(m);}}
  catch(Exception e){System.err.println("[ChronoHarvest] Timestamp load failed: "+e);}
  lastSave=0;
 }
 public static void save(MinecraftServer s){
  if(file==null)return;
  try{
   Files.createDirectories(file.getParent());Path tmp=file.resolveSibling(file.getFileName()+".tmp");
   Files.writeString(tmp,GSON.toJson(PLANTED));
   try{Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
   catch(java.nio.file.AtomicMoveNotSupportedException e){Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING);}
  }catch(Exception e){System.err.println("[ChronoHarvest] Timestamp save failed: "+e);}
 }
 public static void markPlanted(ServerWorld w,BlockPos p){PLANTED.put(key(w,p),System.currentTimeMillis());}
 public static void forget(ServerWorld w,BlockPos p){PLANTED.remove(key(w,p));}
 public static boolean applyElapsedGrowth(ServerWorld w,BlockPos p){
  if(!ChronoHarvest.CONFIG.enabled)return false;
  BlockState s=w.getBlockState(p);if(!supported(s))return false;
  IntProperty age=ageProperty(s);if(age==null)return false;
  long planted=PLANTED.computeIfAbsent(key(w,p),k->System.currentTimeMillis());
  long elapsed=Math.max(0,System.currentTimeMillis()-planted),stageMs=ChronoHarvest.CONFIG.secondsPerStage*1000L;
  int max=age.getValues().stream().mapToInt(Integer::intValue).max().orElse(0);
  int expected=(int)Math.min(max,elapsed/stageMs);
  if(expected>s.get(age))w.setBlockState(p,s.with(age,expected),3);
  return true;
 }
 /** Queue work only: never inspect a whole chunk in the synchronous chunk-load callback. */
 public static void onChunkLoad(ServerWorld w,WorldChunk c){
  if(!ChronoHarvest.CONFIG.enabled)return;
  String k=chunkKey(w,c);if(PENDING.containsKey(k))return;
  ChunkScan scan=new ChunkScan(w,c);PENDING.put(k,scan);QUEUE.addLast(scan);
 }
 /** Discard incomplete scans when a chunk unloads; a later load queues it again. */
 public static void onChunkUnload(ServerWorld w,WorldChunk c){
  ChunkScan scan=PENDING.remove(chunkKey(w,c));if(scan!=null)QUEUE.remove(scan);
 }
 /** Scan at most 8,192 block positions per tick so chunk loading remains responsive. */
 public static void tick(MinecraftServer current){
  if(!ChronoHarvest.CONFIG.enabled){QUEUE.clear();PENDING.clear();}
  else{
   int budget=BLOCK_BUDGET_PER_TICK;
   while(budget>0&&!QUEUE.isEmpty()){
    ChunkScan scan=QUEUE.removeFirst();if(PENDING.get(scan.key)!=scan)continue;
    int amount=Math.min(budget,scan.totalBlocks-scan.cursor);
    for(int i=0;i<amount;i++){
     int n=scan.cursor++,x=n&15,z=(n>>>4)&15,y=n>>>8;
     BlockPos.Mutable p=scan.pos.set(scan.startX+x,scan.bottomY+y,scan.startZ+z);
     if(supported(scan.chunk.getBlockState(p)))applyElapsedGrowth(scan.world,p);
    }
    budget-=amount;
    if(scan.cursor>=scan.totalBlocks)PENDING.remove(scan.key,scan);else QUEUE.addLast(scan);
   }
  }
  if(server!=null&&server.getTicks()-lastSave>=200){save(server);lastSave=server.getTicks();}
 }
}
