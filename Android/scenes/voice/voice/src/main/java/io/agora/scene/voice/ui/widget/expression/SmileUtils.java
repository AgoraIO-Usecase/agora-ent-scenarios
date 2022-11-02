package io.agora.scene.voice.ui.widget.expression;

import android.content.Context;
import android.net.Uri;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.style.ImageSpan;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmileUtils {
    public static final String DELETE_KEY = "em_delete_delete_expression";
    
	public static final String ee_1 = "U+1F600";
	public static final String ee_2 = "U+1F604";
	public static final String ee_3 = "U+1F609";
	public static final String ee_4 = "U+1F62E";
	public static final String ee_5 = "U+1F92A";
	public static final String ee_6 = "U+1F60E";
	public static final String ee_7 = "U+1F971";
	public static final String ee_8 = "U+1F974";
	public static final String ee_9 = "U+263A";
	public static final String ee_10 = "U+1F641";
	public static final String ee_11 = "U+1F62D";
	public static final String ee_12 = "U+1F610";
	public static final String ee_13 = "U+1F607";
	public static final String ee_14 = "U+1F62C";
	public static final String ee_15 = "U+1F913";
	public static final String ee_16 = "U+1F633";
	public static final String ee_17 = "U+1F973";
	public static final String ee_18 = "U+1F620";
	public static final String ee_19 = "U+1F644";
	public static final String ee_20 = "U+1F910";
	public static final String ee_21 = "U+1F97A";
	public static final String ee_22 = "U+1F928";
	public static final String ee_23 = "U+1F62B";
	public static final String ee_24 = "U+1F637";
	public static final String ee_25 = "U+1F912";
	public static final String ee_26 = "U+1F631";
	public static final String ee_27 = "U+1F618";
	public static final String ee_28 = "U+1F60D";
	public static final String ee_29 = "U+1F922";
	public static final String ee_30 = "U+1F47F";
	public static final String ee_31 = "U+1F92C";
	public static final String ee_32 = "U+1F621";
	public static final String ee_33 = "U+1F44D";
	public static final String ee_34 = "U+1F44E";
	public static final String ee_35 = "U+1F44F";
	public static final String ee_36 = "U+1F64C";
	public static final String ee_37 = "U+1F91D";
	public static final String ee_38 = "U+1F64F";
	public static final String ee_39 = "U+2764";
	public static final String ee_40 = "U+1F494";
	public static final String ee_41 = "U+1F495";
	public static final String ee_42 = "U+1F4A9";
	public static final String ee_43 = "U+1F48B";
	public static final String ee_44 = "U+2600";
	public static final String ee_45 = "U+1F31C";
	public static final String ee_46 = "U+1F308";
	public static final String ee_47 = "U+2B50";
	public static final String ee_48 = "U+1F31F";
	public static final String ee_49 = "U+1F389";
	public static final String ee_50 = "U+1F490";
	public static final String ee_51 = "U+1F382";
	public static final String ee_52 = "U+1F381";

	
	private static final Factory spannableFactory = Factory
	        .getInstance();
	
	private static final Map<Pattern, Object> emoticons = new HashMap<Pattern, Object>();
	

	static {
	    ExpressionIcon[] emojicons = DefaultExpressionData.getData();
		for (ExpressionIcon emojicon : emojicons) {
			addPattern(emojicon.getLabelString(), emojicon.getIcon());
		}
	}

	/**
	 * add text and icon to the map
	 * @param emojiText-- text of emoji
	 * @param icon -- resource id or local path
	 */
	public static void addPattern(String emojiText, Object icon){
	    emoticons.put(Pattern.compile(Pattern.quote(emojiText)), icon);
	}
	

	/**
	 * replace existing spannable with smiles
	 * @param context
	 * @param spannable
	 * @return
	 */
	public static boolean addSmiles(Context context, Spannable spannable) {
	    boolean hasChanges = false;
	    for (Entry<Pattern, Object> entry : emoticons.entrySet()) {
	        Matcher matcher = entry.getKey().matcher(spannable);
	        while (matcher.find()) {
	            boolean set = true;
	            for (ImageSpan span : spannable.getSpans(matcher.start(),
	                    matcher.end(), ImageSpan.class))
	                if (spannable.getSpanStart(span) >= matcher.start()
	                        && spannable.getSpanEnd(span) <= matcher.end())
	                    spannable.removeSpan(span);
	                else {
	                    set = false;
	                    break;
	                }
	            if (set) {
	                hasChanges = true;
	                Object value = entry.getValue();
	                if(value instanceof String && !((String) value).startsWith("http")){
	                    File file = new File((String) value);
	                    if(!file.exists() || file.isDirectory()){
	                        return false;
	                    }
	                    spannable.setSpan(new ImageSpan(context, Uri.fromFile(file)),
	                            matcher.start(), matcher.end(),
	                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	                }else{
	                    spannable.setSpan(new ImageSpan(context, (Integer)value),
	                            matcher.start(), matcher.end(),
	                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	                }
	            }
	        }
	    }
	    
	    return hasChanges;
	}

	public static Spannable getSmiledText(Context context, CharSequence text) {
	    Spannable spannable = spannableFactory.newSpannable(text);
	    addSmiles(context, spannable);
	    return spannable;
	}
	
	public static boolean containsKey(String key){
		boolean b = false;
		for (Entry<Pattern, Object> entry : emoticons.entrySet()) {
	        Matcher matcher = entry.getKey().matcher(key);
	        if (matcher.find()) {
	        	b = true;
	        	break;
	        }
		}
		
		return b;
	}
	
	public static int getSmilesSize(){
        return emoticons.size();
    }
    
	
}
