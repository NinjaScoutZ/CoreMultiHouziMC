package com.houzicore.shared.core.common.block.schematic;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Nbt {

    public static final class NBTConstants {
        public static final Charset CHARSET = Charset.forName("UTF-8");
        public static final int TYPE_END = 0;
        public static final int TYPE_BYTE = 1;
        public static final int TYPE_SHORT = 2;
        public static final int TYPE_INT = 3;
        public static final int TYPE_LONG = 4;
        public static final int TYPE_FLOAT = 5;
        public static final int TYPE_DOUBLE = 6;
        public static final int TYPE_BYTE_ARRAY = 7;
        public static final int TYPE_STRING = 8;
        public static final int TYPE_LIST = 9;
        public static final int TYPE_COMPOUND = 10;
        public static final int TYPE_INT_ARRAY = 11;
        
        private NBTConstants() {}
    }

    public static abstract class Tag {
        public abstract Object getValue();
    }

    public static final class EndTag extends Tag {
        @Override
        public Object getValue() {
            return null;
        }
    }

    public static final class ByteTag extends Tag {
        private final byte value;
        public ByteTag(byte value) { this.value = value; }
        @Override
        public Byte getValue() { return value; }
    }

    public static final class ShortTag extends Tag {
        private final short value;
        public ShortTag(short value) { this.value = value; }
        @Override
        public Short getValue() { return value; }
    }

    public static final class IntTag extends Tag {
        private final int value;
        public IntTag(int value) { this.value = value; }
        @Override
        public Integer getValue() { return value; }
    }

    public static final class LongTag extends Tag {
        private final long value;
        public LongTag(long value) { this.value = value; }
        @Override
        public Long getValue() { return value; }
    }

    public static final class FloatTag extends Tag {
        private final float value;
        public FloatTag(float value) { this.value = value; }
        @Override
        public Float getValue() { return value; }
    }

    public static final class DoubleTag extends Tag {
        private final double value;
        public DoubleTag(double value) { this.value = value; }
        @Override
        public Double getValue() { return value; }
    }

    public static final class ByteArrayTag extends Tag {
        private final byte[] value;
        public ByteArrayTag(byte[] value) { this.value = value; }
        @Override
        public byte[] getValue() { return value; }
    }

    public static final class StringTag extends Tag {
        private final String value;
        public StringTag(String value) { this.value = value; }
        @Override
        public String getValue() { return value; }
    }

    public static final class ListTag extends Tag {
        private final Class<? extends Tag> type;
        private final List<Tag> value;
        public ListTag(Class<? extends Tag> type, List<Tag> value) {
            this.type = type;
            this.value = Collections.unmodifiableList(value);
        }
        public Class<? extends Tag> getType() { return type; }
        @Override
        public List<Tag> getValue() { return value; }
    }

    public static final class CompoundTag extends Tag {
        private final Map<String, Tag> value;
        public CompoundTag(Map<String, Tag> value) {
            this.value = Collections.unmodifiableMap(value);
        }
        @Override
        public Map<String, Tag> getValue() { return value; }
    }

    public static final class IntArrayTag extends Tag {
        private final int[] value;
        public IntArrayTag(int[] value) { this.value = value; }
        @Override
        public int[] getValue() { return value; }
    }

    public static final class NamedTag {
        private final String name;
        private final Tag tag;
        public NamedTag(String name, Tag tag) {
            this.name = name;
            this.tag = tag;
        }
        public String getName() { return name; }
        public Tag getTag() { return tag; }
    }

    public static final class NBTInputStream implements Closeable {
        private final DataInputStream is;
        public NBTInputStream(InputStream is) {
            this.is = new DataInputStream(is);
        }
        public NamedTag readNamedTag() throws IOException {
            return readNamedTag(0);
        }
        private NamedTag readNamedTag(int depth) throws IOException {
            int type = is.readByte() & 0xFF;
            String name;
            if (type != NBTConstants.TYPE_END) {
                int nameLength = is.readShort() & 0xFFFF;
                byte[] nameBytes = new byte[nameLength];
                is.readFully(nameBytes);
                name = new String(nameBytes, NBTConstants.CHARSET);
            } else {
                name = "";
            }
            return new NamedTag(name, readTagPayload(type, depth));
        }
        private Tag readTagPayload(int type, int depth) throws IOException {
            switch (type) {
                case NBTConstants.TYPE_END:
                    if (depth == 0) {
                        throw new IOException("TAG_End found without a TAG_Compound/TAG_List tag preceding it.");
                    } else {
                        return new EndTag();
                    }
                case NBTConstants.TYPE_BYTE:
                    return new ByteTag(is.readByte());
                case NBTConstants.TYPE_SHORT:
                    return new ShortTag(is.readShort());
                case NBTConstants.TYPE_INT:
                    return new IntTag(is.readInt());
                case NBTConstants.TYPE_LONG:
                    return new LongTag(is.readLong());
                case NBTConstants.TYPE_FLOAT:
                    return new FloatTag(is.readFloat());
                case NBTConstants.TYPE_DOUBLE:
                    return new DoubleTag(is.readDouble());
                case NBTConstants.TYPE_BYTE_ARRAY:
                    int length = is.readInt();
                    byte[] bytes = new byte[length];
                    is.readFully(bytes);
                    return new ByteArrayTag(bytes);
                case NBTConstants.TYPE_STRING:
                    length = is.readShort() & 0xFFFF;
                    bytes = new byte[length];
                    is.readFully(bytes);
                    return new StringTag(new String(bytes, NBTConstants.CHARSET));
                case NBTConstants.TYPE_LIST:
                    int childType = is.readByte() & 0xFF;
                    length = is.readInt();
                    List<Tag> tagList = new ArrayList<>();
                    for (int i = 0; i < length; ++i) {
                        Tag tag = readTagPayload(childType, depth + 1);
                        if (tag instanceof EndTag) {
                            throw new IOException("TAG_End not permitted in a list.");
                        }
                        tagList.add(tag);
                    }
                    return new ListTag(getTypeClass(childType), tagList);
                case NBTConstants.TYPE_COMPOUND:
                    Map<String, Tag> tagMap = new HashMap<>();
                    while (true) {
                        NamedTag namedTag = readNamedTag(depth + 1);
                        Tag tag = namedTag.getTag();
                        if (tag instanceof EndTag) {
                            break;
                        } else {
                            tagMap.put(namedTag.getName(), tag);
                        }
                    }
                    return new CompoundTag(tagMap);
                case NBTConstants.TYPE_INT_ARRAY:
                    length = is.readInt();
                    int[] data = new int[length];
                    for (int i = 0; i < length; i++) {
                        data[i] = is.readInt();
                    }
                    return new IntArrayTag(data);
                default:
                    throw new IOException("Invalid tag type: " + type + ".");
            }
        }
        @Override
        public void close() throws IOException {
            is.close();
        }
    }

    public static final class NBTOutputStream implements Closeable {
        private final DataOutputStream os;
        public NBTOutputStream(OutputStream os) {
            this.os = new DataOutputStream(os);
        }
        public void writeNamedTag(String name, Tag tag) throws IOException {
            int type = getTypeCode(tag.getClass());
            os.writeByte(type);
            if (type != NBTConstants.TYPE_END) {
                byte[] nameBytes = name.getBytes(NBTConstants.CHARSET);
                os.writeShort(nameBytes.length);
                os.write(nameBytes);
                writeTagPayload(tag);
            }
        }
        private void writeTagPayload(Tag tag) throws IOException {
            int type = getTypeCode(tag.getClass());
            switch (type) {
                case NBTConstants.TYPE_END:
                    break;
                case NBTConstants.TYPE_BYTE:
                    os.writeByte(((ByteTag) tag).getValue());
                    break;
                case NBTConstants.TYPE_SHORT:
                    os.writeShort(((ShortTag) tag).getValue());
                    break;
                case NBTConstants.TYPE_INT:
                    os.writeInt(((IntTag) tag).getValue());
                    break;
                case NBTConstants.TYPE_LONG:
                    os.writeLong(((LongTag) tag).getValue());
                    break;
                case NBTConstants.TYPE_FLOAT:
                    os.writeFloat(((FloatTag) tag).getValue());
                    break;
                case NBTConstants.TYPE_DOUBLE:
                    os.writeDouble(((DoubleTag) tag).getValue());
                    break;
                case NBTConstants.TYPE_BYTE_ARRAY:
                    byte[] bytes = ((ByteArrayTag) tag).getValue();
                    os.writeInt(bytes.length);
                    os.write(bytes);
                    break;
                case NBTConstants.TYPE_STRING:
                    bytes = ((StringTag) tag).getValue().getBytes(NBTConstants.CHARSET);
                    os.writeShort(bytes.length);
                    os.write(bytes);
                    break;
                case NBTConstants.TYPE_LIST:
                    ListTag listTag = (ListTag) tag;
                    int childType = getTypeCode(listTag.getType());
                    List<Tag> list = listTag.getValue();
                    os.writeByte(childType);
                    os.writeInt(list.size());
                    for (Tag t : list) {
                        writeTagPayload(t);
                    }
                    break;
                case NBTConstants.TYPE_COMPOUND:
                    CompoundTag compoundTag = (CompoundTag) tag;
                    for (Map.Entry<String, Tag> entry : compoundTag.getValue().entrySet()) {
                        writeNamedTag(entry.getKey(), entry.getValue());
                    }
                    os.writeByte(NBTConstants.TYPE_END);
                    break;
                case NBTConstants.TYPE_INT_ARRAY:
                    int[] data = ((IntArrayTag) tag).getValue();
                    os.writeInt(data.length);
                    for (int val : data) {
                        os.writeInt(val);
                    }
                    break;
                default:
                    throw new IOException("Invalid tag type: " + type + ".");
            }
        }
        @Override
        public void close() throws IOException {
            os.close();
        }
    }

    public static Class<? extends Tag> getTypeClass(int type) {
        switch (type) {
            case NBTConstants.TYPE_END: return EndTag.class;
            case NBTConstants.TYPE_BYTE: return ByteTag.class;
            case NBTConstants.TYPE_SHORT: return ShortTag.class;
            case NBTConstants.TYPE_INT: return IntTag.class;
            case NBTConstants.TYPE_LONG: return LongTag.class;
            case NBTConstants.TYPE_FLOAT: return FloatTag.class;
            case NBTConstants.TYPE_DOUBLE: return DoubleTag.class;
            case NBTConstants.TYPE_BYTE_ARRAY: return ByteArrayTag.class;
            case NBTConstants.TYPE_STRING: return StringTag.class;
            case NBTConstants.TYPE_LIST: return ListTag.class;
            case NBTConstants.TYPE_COMPOUND: return CompoundTag.class;
            case NBTConstants.TYPE_INT_ARRAY: return IntArrayTag.class;
            default: throw new IllegalArgumentException("Invalid tag type: " + type);
        }
    }

    public static int getTypeCode(Class<? extends Tag> clazz) {
        if (clazz.equals(ByteArrayTag.class)) return NBTConstants.TYPE_BYTE_ARRAY;
        if (clazz.equals(ByteTag.class)) return NBTConstants.TYPE_BYTE;
        if (clazz.equals(CompoundTag.class)) return NBTConstants.TYPE_COMPOUND;
        if (clazz.equals(DoubleTag.class)) return NBTConstants.TYPE_DOUBLE;
        if (clazz.equals(EndTag.class)) return NBTConstants.TYPE_END;
        if (clazz.equals(FloatTag.class)) return NBTConstants.TYPE_FLOAT;
        if (clazz.equals(IntTag.class)) return NBTConstants.TYPE_INT;
        if (clazz.equals(ListTag.class)) return NBTConstants.TYPE_LIST;
        if (clazz.equals(LongTag.class)) return NBTConstants.TYPE_LONG;
        if (clazz.equals(ShortTag.class)) return NBTConstants.TYPE_SHORT;
        if (clazz.equals(StringTag.class)) return NBTConstants.TYPE_STRING;
        if (clazz.equals(IntArrayTag.class)) return NBTConstants.TYPE_INT_ARRAY;
        throw new IllegalArgumentException("Invalid tag class: " + clazz.getName());
    }
}
