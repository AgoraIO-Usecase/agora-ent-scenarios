package io.agora.voice.common.net.model;

public class VRException extends Exception{

   protected int errorCode = -1;
   protected String desc = "";


   public VRException() {
      super();
   }

   /**
    * \~chinese
    * 用给定的描述构造一个异常。
    * @param desc 异常信息。
    *
    * \~english
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
    * \~chinese
    * 用给定的描述和异常的起因构造一个异常。
    * @param desc 异常描述。
    * @param cause 异常起因。
    *
    * \~english
    * Constructs an exception with the given description and exception cause.
    * @param desc The exception description.
    * @param cause The exception cause.
    */
   public VRException(String desc, Throwable cause) {
      super(desc);
      super.initCause(cause);
   }

   /**
    * \~chinese
    * 用给定的错误码和异常描述构造一个异常。
    * @param errorCode 错误码。
    * @param desc 异常描述。
    *
    * \~english
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
    * \~chinese
    * 获取错误码。
    * @return  错误码。
    *
    * \~english
    * Gets the error code.
    * @return  The error code.
    */
   public int getErrorCode() {
      return errorCode;
   }

   /**
    * \~chinese
    * 获取异常信息。
    * @return  异常信息。
    *
    * \~english
    * Gets the exception description.
    * @return  The exception description.
    */
   public String getDescription() {
      return this.desc;
   }

   /**
    * \~chinese
    * 设置错误码。
    * @param errorCode 错误码。
    *
    * \~english
    * Sets the error code.
    * @param errorCode The error code to set.
    */
   public void setErrorCode(int errorCode) {
      this.errorCode = errorCode;
   }

}
