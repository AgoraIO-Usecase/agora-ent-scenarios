package io.agora.scene.ktv.singbattle.ktvapi;// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: LrcTime.proto

public final class LrcTimeOuterClass {
  private LrcTimeOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  /**
   * Protobuf enum {@code MsgType}
   */
  public enum MsgType
      implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <code>UNKNOWN_TYPE = 0;</code>
     */
    UNKNOWN_TYPE(0),
    /**
     * <code>LRC_TIME = 1001;</code>
     */
    LRC_TIME(1001),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>UNKNOWN_TYPE = 0;</code>
     */
    public static final int UNKNOWN_TYPE_VALUE = 0;
    /**
     * <code>LRC_TIME = 1001;</code>
     */
    public static final int LRC_TIME_VALUE = 1001;


    public final int getNumber() {
      if (this == UNRECOGNIZED) {
        throw new IllegalArgumentException(
            "Can't get the number of an unknown enum value.");
      }
      return value;
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @Deprecated
    public static MsgType valueOf(int value) {
      return forNumber(value);
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     */
    public static MsgType forNumber(int value) {
      switch (value) {
        case 0: return UNKNOWN_TYPE;
        case 1001: return LRC_TIME;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<MsgType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        MsgType> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<MsgType>() {
            public MsgType findValueByNumber(int number) {
              return MsgType.forNumber(number);
            }
          };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      if (this == UNRECOGNIZED) {
        throw new IllegalStateException(
            "Can't get the descriptor of an unrecognized enum value.");
      }
      return getDescriptor().getValues().get(ordinal());
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return LrcTimeOuterClass.getDescriptor().getEnumTypes().get(0);
    }

    private static final MsgType[] VALUES = values();

    public static MsgType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      if (desc.getIndex() == -1) {
        return UNRECOGNIZED;
      }
      return VALUES[desc.getIndex()];
    }

    private final int value;

    private MsgType(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:MsgType)
  }

  public interface LrcTimeOrBuilder extends
      // @@protoc_insertion_point(interface_extends:LrcTime)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>.MsgType type = 1;</code>
     * @return The enum numeric value on the wire for type.
     */
    int getTypeValue();
    /**
     * <code>.MsgType type = 1;</code>
     * @return The type.
     */
    MsgType getType();

    /**
     * <code>bool forward = 2;</code>
     * @return The forward.
     */
    boolean getForward();

    /**
     * <code>int64 ts = 3;</code>
     * @return The ts.
     */
    long getTs();

    /**
     * <code>string songId = 4;</code>
     * @return The songId.
     */
    String getSongId();
    /**
     * <code>string songId = 4;</code>
     * @return The bytes for songId.
     */
    com.google.protobuf.ByteString
        getSongIdBytes();

    /**
     * <code>int32 uid = 5;</code>
     * @return The uid.
     */
    int getUid();
  }
  /**
   * Protobuf type {@code LrcTime}
   */
  public static final class LrcTime extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:LrcTime)
      LrcTimeOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use LrcTime.newBuilder() to construct.
    private LrcTime(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private LrcTime() {
      type_ = 0;
      songId_ = "";
    }

    @Override
    @SuppressWarnings({"unused"})
    protected Object newInstance(
        UnusedPrivateParameter unused) {
      return new LrcTime();
    }

    @Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private LrcTime(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
        throw new NullPointerException();
      }
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 8: {
              int rawValue = input.readEnum();

              type_ = rawValue;
              break;
            }
            case 16: {

              forward_ = input.readBool();
              break;
            }
            case 24: {

              ts_ = input.readInt64();
              break;
            }
            case 34: {
              String s = input.readStringRequireUtf8();

              songId_ = s;
              break;
            }
            case 40: {

              uid_ = input.readInt32();
              break;
            }
            default: {
              if (!parseUnknownField(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return LrcTimeOuterClass.internal_static_LrcTime_descriptor;
    }

    @Override
    protected FieldAccessorTable
        internalGetFieldAccessorTable() {
      return LrcTimeOuterClass.internal_static_LrcTime_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              LrcTime.class, Builder.class);
    }

    public static final int TYPE_FIELD_NUMBER = 1;
    private int type_;
    /**
     * <code>.MsgType type = 1;</code>
     * @return The enum numeric value on the wire for type.
     */
    @Override public int getTypeValue() {
      return type_;
    }
    /**
     * <code>.MsgType type = 1;</code>
     * @return The type.
     */
    @Override public MsgType getType() {
      @SuppressWarnings("deprecation")
      MsgType result = MsgType.valueOf(type_);
      return result == null ? MsgType.UNRECOGNIZED : result;
    }

    public static final int FORWARD_FIELD_NUMBER = 2;
    private boolean forward_;
    /**
     * <code>bool forward = 2;</code>
     * @return The forward.
     */
    @Override
    public boolean getForward() {
      return forward_;
    }

    public static final int TS_FIELD_NUMBER = 3;
    private long ts_;
    /**
     * <code>int64 ts = 3;</code>
     * @return The ts.
     */
    @Override
    public long getTs() {
      return ts_;
    }

    public static final int SONGID_FIELD_NUMBER = 4;
    private volatile Object songId_;
    /**
     * <code>string songId = 4;</code>
     * @return The songId.
     */
    @Override
    public String getSongId() {
      Object ref = songId_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        songId_ = s;
        return s;
      }
    }
    /**
     * <code>string songId = 4;</code>
     * @return The bytes for songId.
     */
    @Override
    public com.google.protobuf.ByteString
        getSongIdBytes() {
      Object ref = songId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        songId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int UID_FIELD_NUMBER = 5;
    private int uid_;
    /**
     * <code>int32 uid = 5;</code>
     * @return The uid.
     */
    @Override
    public int getUid() {
      return uid_;
    }

    private byte memoizedIsInitialized = -1;
    @Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (type_ != MsgType.UNKNOWN_TYPE.getNumber()) {
        output.writeEnum(1, type_);
      }
      if (forward_ != false) {
        output.writeBool(2, forward_);
      }
      if (ts_ != 0L) {
        output.writeInt64(3, ts_);
      }
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(songId_)) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 4, songId_);
      }
      if (uid_ != 0) {
        output.writeInt32(5, uid_);
      }
      unknownFields.writeTo(output);
    }

    @Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (type_ != MsgType.UNKNOWN_TYPE.getNumber()) {
        size += com.google.protobuf.CodedOutputStream
          .computeEnumSize(1, type_);
      }
      if (forward_ != false) {
        size += com.google.protobuf.CodedOutputStream
          .computeBoolSize(2, forward_);
      }
      if (ts_ != 0L) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt64Size(3, ts_);
      }
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(songId_)) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(4, songId_);
      }
      if (uid_ != 0) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(5, uid_);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof LrcTime)) {
        return super.equals(obj);
      }
      LrcTime other = (LrcTime) obj;

      if (type_ != other.type_) return false;
      if (getForward()
          != other.getForward()) return false;
      if (getTs()
          != other.getTs()) return false;
      if (!getSongId()
          .equals(other.getSongId())) return false;
      if (getUid()
          != other.getUid()) return false;
      if (!unknownFields.equals(other.unknownFields)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + TYPE_FIELD_NUMBER;
      hash = (53 * hash) + type_;
      hash = (37 * hash) + FORWARD_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
          getForward());
      hash = (37 * hash) + TS_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
          getTs());
      hash = (37 * hash) + SONGID_FIELD_NUMBER;
      hash = (53 * hash) + getSongId().hashCode();
      hash = (37 * hash) + UID_FIELD_NUMBER;
      hash = (53 * hash) + getUid();
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static LrcTime parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static LrcTime parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static LrcTime parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static LrcTime parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static LrcTime parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static LrcTime parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static LrcTime parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static LrcTime parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static LrcTime parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static LrcTime parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static LrcTime parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static LrcTime parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(LrcTime prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @Override
    protected Builder newBuilderForType(
        BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code LrcTime}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:LrcTime)
        LrcTimeOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return LrcTimeOuterClass.internal_static_LrcTime_descriptor;
      }

      @Override
      protected FieldAccessorTable
          internalGetFieldAccessorTable() {
        return LrcTimeOuterClass.internal_static_LrcTime_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                LrcTime.class, Builder.class);
      }

      // Construct using LrcTimeOuterClass.LrcTime.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      @Override
      public Builder clear() {
        super.clear();
        type_ = 0;

        forward_ = false;

        ts_ = 0L;

        songId_ = "";

        uid_ = 0;

        return this;
      }

      @Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return LrcTimeOuterClass.internal_static_LrcTime_descriptor;
      }

      @Override
      public LrcTime getDefaultInstanceForType() {
        return LrcTime.getDefaultInstance();
      }

      @Override
      public LrcTime build() {
        LrcTime result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @Override
      public LrcTime buildPartial() {
        LrcTime result = new LrcTime(this);
        result.type_ = type_;
        result.forward_ = forward_;
        result.ts_ = ts_;
        result.songId_ = songId_;
        result.uid_ = uid_;
        onBuilt();
        return result;
      }

      @Override
      public Builder clone() {
        return super.clone();
      }
      @Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return super.setField(field, value);
      }
      @Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return super.clearField(field);
      }
      @Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return super.clearOneof(oneof);
      }
      @Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, Object value) {
        return super.setRepeatedField(field, index, value);
      }
      @Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return super.addRepeatedField(field, value);
      }
      @Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof LrcTime) {
          return mergeFrom((LrcTime)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(LrcTime other) {
        if (other == LrcTime.getDefaultInstance()) return this;
        if (other.type_ != 0) {
          setTypeValue(other.getTypeValue());
        }
        if (other.getForward() != false) {
          setForward(other.getForward());
        }
        if (other.getTs() != 0L) {
          setTs(other.getTs());
        }
        if (!other.getSongId().isEmpty()) {
          songId_ = other.songId_;
          onChanged();
        }
        if (other.getUid() != 0) {
          setUid(other.getUid());
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      @Override
      public final boolean isInitialized() {
        return true;
      }

      @Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        LrcTime parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (LrcTime) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      private int type_ = 0;
      /**
       * <code>.MsgType type = 1;</code>
       * @return The enum numeric value on the wire for type.
       */
      @Override public int getTypeValue() {
        return type_;
      }
      /**
       * <code>.MsgType type = 1;</code>
       * @param value The enum numeric value on the wire for type to set.
       * @return This builder for chaining.
       */
      public Builder setTypeValue(int value) {
        
        type_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>.MsgType type = 1;</code>
       * @return The type.
       */
      @Override
      public MsgType getType() {
        @SuppressWarnings("deprecation")
        MsgType result = MsgType.valueOf(type_);
        return result == null ? MsgType.UNRECOGNIZED : result;
      }
      /**
       * <code>.MsgType type = 1;</code>
       * @param value The type to set.
       * @return This builder for chaining.
       */
      public Builder setType(MsgType value) {
        if (value == null) {
          throw new NullPointerException();
        }
        
        type_ = value.getNumber();
        onChanged();
        return this;
      }
      /**
       * <code>.MsgType type = 1;</code>
       * @return This builder for chaining.
       */
      public Builder clearType() {
        
        type_ = 0;
        onChanged();
        return this;
      }

      private boolean forward_ ;
      /**
       * <code>bool forward = 2;</code>
       * @return The forward.
       */
      @Override
      public boolean getForward() {
        return forward_;
      }
      /**
       * <code>bool forward = 2;</code>
       * @param value The forward to set.
       * @return This builder for chaining.
       */
      public Builder setForward(boolean value) {
        
        forward_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>bool forward = 2;</code>
       * @return This builder for chaining.
       */
      public Builder clearForward() {
        
        forward_ = false;
        onChanged();
        return this;
      }

      private long ts_ ;
      /**
       * <code>int64 ts = 3;</code>
       * @return The ts.
       */
      @Override
      public long getTs() {
        return ts_;
      }
      /**
       * <code>int64 ts = 3;</code>
       * @param value The ts to set.
       * @return This builder for chaining.
       */
      public Builder setTs(long value) {
        
        ts_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>int64 ts = 3;</code>
       * @return This builder for chaining.
       */
      public Builder clearTs() {
        
        ts_ = 0L;
        onChanged();
        return this;
      }

      private Object songId_ = "";
      /**
       * <code>string songId = 4;</code>
       * @return The songId.
       */
      public String getSongId() {
        Object ref = songId_;
        if (!(ref instanceof String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          String s = bs.toStringUtf8();
          songId_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      /**
       * <code>string songId = 4;</code>
       * @return The bytes for songId.
       */
      public com.google.protobuf.ByteString
          getSongIdBytes() {
        Object ref = songId_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (String) ref);
          songId_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string songId = 4;</code>
       * @param value The songId to set.
       * @return This builder for chaining.
       */
      public Builder setSongId(
          String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  
        songId_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>string songId = 4;</code>
       * @return This builder for chaining.
       */
      public Builder clearSongId() {
        
        songId_ = getDefaultInstance().getSongId();
        onChanged();
        return this;
      }
      /**
       * <code>string songId = 4;</code>
       * @param value The bytes for songId to set.
       * @return This builder for chaining.
       */
      public Builder setSongIdBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
        
        songId_ = value;
        onChanged();
        return this;
      }

      private int uid_ ;
      /**
       * <code>int32 uid = 5;</code>
       * @return The uid.
       */
      @Override
      public int getUid() {
        return uid_;
      }
      /**
       * <code>int32 uid = 5;</code>
       * @param value The uid to set.
       * @return This builder for chaining.
       */
      public Builder setUid(int value) {
        
        uid_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>int32 uid = 5;</code>
       * @return This builder for chaining.
       */
      public Builder clearUid() {
        
        uid_ = 0;
        onChanged();
        return this;
      }
      @Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      @Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:LrcTime)
    }

    // @@protoc_insertion_point(class_scope:LrcTime)
    private static final LrcTime DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new LrcTime();
    }

    public static LrcTime getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<LrcTime>
        PARSER = new com.google.protobuf.AbstractParser<LrcTime>() {
      @Override
      public LrcTime parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new LrcTime(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<LrcTime> parser() {
      return PARSER;
    }

    @Override
    public com.google.protobuf.Parser<LrcTime> getParserForType() {
      return PARSER;
    }

    @Override
    public LrcTime getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_LrcTime_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_LrcTime_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    String[] descriptorData = {
      "\n\rLrcTime.proto\"[\n\007LrcTime\022\026\n\004type\030\001 \001(\016" +
      "2\010.MsgType\022\017\n\007forward\030\002 \001(\010\022\n\n\002ts\030\003 \001(\003\022" +
      "\016\n\006songId\030\004 \001(\t\022\013\n\003uid\030\005 \001(\005**\n\007MsgType\022" +
      "\020\n\014UNKNOWN_TYPE\020\000\022\r\n\010LRC_TIME\020\351\007b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_LrcTime_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_LrcTime_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_LrcTime_descriptor,
        new String[] { "Type", "Forward", "Ts", "SongId", "Uid", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
