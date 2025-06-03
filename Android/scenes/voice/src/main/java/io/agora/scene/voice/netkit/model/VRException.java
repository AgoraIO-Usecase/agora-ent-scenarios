package io.agora.scene.voice.netkit.model;

public class VRException extends Exception{

   protected int errorCode = -1;
   protected String desc = "";


   public VRException() {
      super();
   }

   /**
    * Constructs an exception with the given description.
    * @param desc The exception description.
    */
   public VRException(String desc) {
      super(desc);
   }

   public VRException(VRError error) {
      super(error.errMsg());
      errorCode = error.errCode();
      desc = error.errMsg();
   }

   /**
    * Constructs an exception with the given description and exception cause.
    * @param desc The exception description.
    * @param cause The exception cause.
    */
   public VRException(String desc, Throwable cause) {
      super(desc);
      super.initCause(cause);
   }

   /**
    * Constructs an exception with the given description and error code.
    * @param errorCode The error code.
    * @param desc The exception description.
    */
   public VRException(int errorCode, String desc){
      super(desc);
      this.errorCode = errorCode;
      this.desc = desc;
   }

   /**
    * Gets the error code.
    * @return  The error code.
    */
   public int getErrorCode() {
      return errorCode;
   }

   /**
    * Gets the exception description.
    * @return  The exception description.
    */
   public String getDescription() {
      return this.desc;
   }

   /**
    * Sets the error code.
    * @param errorCode The error code to set.
    */
   public void setErrorCode(int errorCode) {
      this.errorCode = errorCode;
   }

}
