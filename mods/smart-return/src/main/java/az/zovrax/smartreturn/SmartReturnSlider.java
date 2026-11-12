package az.zovrax.smartreturn;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/** Native accessible Minecraft slider for saved integer-valued settings. */
public final class SmartReturnSlider extends SliderWidget {
    private final int minimum,maximum;
    private final IntConsumer changed;
    private final IntFunction<Text> label;
    private int selected;

    public SmartReturnSlider(int x,int y,int width,int minimum,int maximum,int initial,
                             IntFunction<Text> label,IntConsumer changed){
        super(x,y,width,20,Text.empty(),
            (initial-minimum)/(double)Math.max(1,maximum-minimum));
        this.minimum=minimum;
        this.maximum=maximum;
        this.selected=initial;
        this.changed=changed;
        this.label=label;
        updateMessage();
    }
    @Override protected void updateMessage(){
        if(label!=null)setMessage(label.apply(selected));
    }
    @Override protected void applyValue(){
        if(changed==null)return;
        int newValue=minimum+(int)Math.round(value*(maximum-minimum));
        if(newValue==selected)return;
        selected=newValue;
        changed.accept(selected);
        updateMessage();
    }
}
