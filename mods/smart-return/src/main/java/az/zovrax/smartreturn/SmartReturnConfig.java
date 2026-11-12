package az.zovrax.smartreturn;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Independent settings; old 0.1.0 settings migrate via Gson defaults. */
public final class SmartReturnConfig {
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE=FabricLoader.getInstance().getConfigDir()
        .resolve("zovrax-smart-return.json");

    public boolean enabled=true;
    public boolean reminders=true;
    public int reminderMinutes=3;
    public int displayMode=0; // 0=3D only, 1=compass only, 2=both
    public boolean showDistance=true;
    public int compassSize=100; // percent [60..180]
    public int compassOpacity=88; // percent [15..100]
    public float compassX=0.5f; // normalized center of screen [0..1]
    public float compassY=0.14f;
    public int effectStyle=0; // slider [0..9]
    public boolean throughWalls=false;
    public int effectDensity=70; // percent [25..100]

    public boolean arrows(){return displayMode==0||displayMode==2;}
    public boolean compass(){return displayMode==1||displayMode==2;}
    public static SmartReturnConfig load(){
        try{
            if(Files.isRegularFile(FILE)){
                SmartReturnConfig c=GSON.fromJson(Files.readString(FILE),SmartReturnConfig.class);
                if(c!=null){c.validate();return c;}
            }
        }catch(Exception e){System.err.println("[SmartReturn] Invalid settings: "+e);}
        return new SmartReturnConfig();
    }
    public void validate(){
        displayMode=Math.max(0,Math.min(2,displayMode));
        reminderMinutes=Math.max(1,Math.min(20,reminderMinutes));
        compassSize=Math.max(60,Math.min(180,compassSize));
        compassOpacity=Math.max(15,Math.min(100,compassOpacity));
        compassX=Float.isFinite(compassX)?Math.max(0f,Math.min(1f,compassX)):.5f;
        compassY=Float.isFinite(compassY)?Math.max(0f,Math.min(1f,compassY)):.14f;
        effectStyle=Math.max(0,Math.min(9,effectStyle));
        effectDensity=Math.max(25,Math.min(100,effectDensity));
    }
    public void save(){
        validate();
        try{
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE,GSON.toJson(this),StandardCharsets.UTF_8);
        }catch(Exception e){System.err.println("[SmartReturn] Cannot save settings: "+e);}
    }
}
