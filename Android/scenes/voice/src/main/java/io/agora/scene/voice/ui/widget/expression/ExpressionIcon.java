package io.agora.scene.voice.ui.widget.expression;

public class ExpressionIcon {
    public ExpressionIcon(){
    }
    
    /**
     * constructor
     * @param icon- resource id of the icon
     * @param label- text of emoji icon
     */
    public ExpressionIcon(int icon, String label){
        this.icon = icon;
        this.label = label;
        this.type = Type.NORMAL;
    }
    
    /**
     * constructor
     * @param icon - resource id of the icon
     * @param label - text of emoji icon
     * @param type - normal
     */
    public ExpressionIcon(int icon, String label, Type type){
        this.icon = icon;
        this.label = label;
        this.type = type;
    }
    
    
    /**
     * identity code
     */
    private String identityCode;
    
    /**
     * static icon resource id
     */
    private int icon;

    
    /**
     * label of emoji
     */
    private String label;
    
    /**
     * name of emoji icon
     */
    private String name;
    
    /**
     * normal or big
     */
    private Type type;
    
    /**
     * path of icon
     */
    private String iconPath;

    
    
    /**
     * get the resource id of the icon
     * @return
     */
    public int getIcon() {
        return icon;
    }


    /**
     * set the resource id of the icon
     * @param icon
     */
    public void setIcon(int icon) {
        this.icon = icon;
    }


    /**
     * get label of emoji icon
     * @return
     */
    public String getLabelString() {
        return label;
    }


    /**
     * set label of emoji icon
     * @param label
     */
    public void setLabelString(String label) {
        this.label = label;
    }

    /**
     * get name of emoji icon
     * @return
     */
    public String getName() {
        return name;
    }
    
    /**
     * set name of emoji icon
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * get type
     * @return
     */
    public Type getType() {
        return type;
    }


    /**
     * set type
     * @param type
     */
    public void setType(Type type) {
        this.type = type;
    }


    /**
     * get icon path
     * @return
     */
    public String getIconPath() {
        return iconPath;
    }


    /**
     * set icon path
     * @param iconPath
     */
    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }



    /**
     * get identity code
     * @return
     */
    public String getIdentityCode() {
        return identityCode;
    }
    
    /**
     * set identity code
     * @param identityCode
     */
    public void setIdentityCode(String identityCode) {
        this.identityCode = identityCode;
    }

    public static String newEmojiText(int codePoint) {
        if (Character.charCount(codePoint) == 1) {
            return String.valueOf(codePoint);
        } else {
            return new String(Character.toChars(codePoint));
        }
    }



    public enum Type{
        /**
         * normal icon, can be input one or more in edit view
         */
        NORMAL
    }
}
