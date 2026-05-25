package com.houzicore.shared.core.music;

import org.bukkit.Sound;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NBS (Note Block Studio) file parser.
 *
 * <p>Supports two format variants:
 * <ul>
 *   <li><b>Classic (v0)</b>: first short = song length (non-zero).
 *   <li><b>New format (v1-v5+)</b>: first short = 0, followed by version byte,
 *       then a larger header. Introduced in NBS 3.7.0+ / OpenNBS.
 * </ul>
 *
 * <p>References:
 * <a href="https://opennbs.org/nbs">OpenNBS format spec</a>
 */
public class NBSParser {

    public static Song parse(File file) {
        try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
            return parseInternal(in);
        } catch (Exception e) {
            System.err.println("[NBSParser] Failed to parse " + file.getName() + ": " + e.getMessage());
            return null;
        }
    }

    private static Song parseInternal(DataInputStream in) throws IOException {
        short firstShort = readShort(in);

        int    version;
        String name;
        String author;
        short  length;
        short  tempo;

        if (firstShort == 0) {
            // ── New NBS format (v1–v5+) ──────────────────────────────────────
            // Header: 0x0000, version byte, vanilla instrument count, song length, ...
            version = in.readUnsignedByte();  // NBS version (1-5)
            in.readUnsignedByte();            // vanilla instrument count (skip)

            if (version >= 3) {
                length = readShort(in);       // song length in ticks
            } else {
                length = 0;                   // determined from note data
            }

            readShort(in);                    // layer count (skip)
            name   = readString(in);          // song name
            author = readString(in);          // song author
            readString(in);                   // original author (skip)
            readString(in);                   // description (skip)
            tempo  = readShort(in);           // tempo × 100 (e.g. 2000 = 20.00 TPS)
            in.readUnsignedByte();            // auto-save
            in.readUnsignedByte();            // auto-save duration
            in.readUnsignedByte();            // time signature
            readInt(in);                      // minutes spent
            readInt(in);                      // left-clicks
            readInt(in);                      // right-clicks
            readInt(in);                      // note blocks added
            readInt(in);                      // note blocks removed
            readString(in);                   // MIDI/schematic file name

            if (version >= 4) {
                in.readUnsignedByte();        // loop on/off
                in.readUnsignedByte();        // max loop count
                readShort(in);               // loop start tick
            }
        } else {
            // ── Classic NBS format (v0) ───────────────────────────────────────
            // Header: song_length(short), height(short), name, author, orig_author,
            //         desc, tempo, auto-save, auto-save-dur, time-sig, 4 ints, midi
            length = firstShort;
            readShort(in);                    // height (skip)
            name   = readString(in);
            author = readString(in);
            readString(in);                   // original author (skip)
            readString(in);                   // description (skip)
            tempo  = readShort(in);
            in.readByte();                    // auto-save
            in.readByte();                    // auto-save duration
            in.readByte();                    // time signature
            readInt(in);                      // minutes
            readInt(in);                      // left-clicks
            readInt(in);                      // right-clicks
            readInt(in);                      // notes added
            readInt(in);                      // notes removed
            readString(in);                   // midi info
            version = 0;
        }

        // ── Note data (same layout in all versions) ───────────────────────────
        Map<Integer, List<NoteBlock>> ticks = new HashMap<>();
        short maxTick = 0;

        short tick = -1;
        while (true) {
            short jumpTicks = readShort(in);
            if (jumpTicks == 0) break;
            tick += jumpTicks;
            if (tick > maxTick) maxTick = tick;

            short layer = -1;
            while (true) {
                short jumpLayers = readShort(in);
                if (jumpLayers == 0) break;
                layer += jumpLayers;

                byte instrument = in.readByte();
                byte key        = in.readByte();

                // v4+ has extra per-note fields: velocity, panning, pitch
                if (version >= 4) {
                    in.readUnsignedByte(); // note block velocity
                    in.readUnsignedByte(); // note panning (200 = center)
                    readShort(in);         // fine pitch (0 = no detune)
                }

                ticks.computeIfAbsent((int) tick, k -> new ArrayList<>())
                     .add(new NoteBlock(layer, instrument, key));
            }
        }

        // Derive length if not provided (classic/v1-v2)
        int songLength = length > 0 ? length : maxTick;
        return new Song(name, author, tempo / 100.0f, ticks, songLength);
    }

    // -----------------------------------------------------------------------
    // Primitive readers — NBS uses little-endian byte order
    // -----------------------------------------------------------------------

    private static short readShort(DataInputStream in) throws IOException {
        int b1 = in.readUnsignedByte();
        int b2 = in.readUnsignedByte();
        return (short) (b1 + (b2 << 8));
    }

    private static int readInt(DataInputStream in) throws IOException {
        int b1 = in.readUnsignedByte();
        int b2 = in.readUnsignedByte();
        int b3 = in.readUnsignedByte();
        int b4 = in.readUnsignedByte();
        return b1 + (b2 << 8) + (b3 << 16) + (b4 << 24);
    }

    private static String readString(DataInputStream in) throws IOException {
        int len = readInt(in);
        if (len < 0 || len > 65536) {
            throw new IOException("NBS string length out of range: " + len);
        }
        byte[] buf = new byte[len];
        in.readFully(buf);
        return new String(buf, java.nio.charset.StandardCharsets.UTF_8);
    }

    // -----------------------------------------------------------------------
    // Data classes
    // -----------------------------------------------------------------------

    public static class Song {
        public final String name;
        public final String author;
        public final float ticksPerSecond;
        public final Map<Integer, List<NoteBlock>> ticks;
        public final int length;

        public Song(String name, String author, float tps,
                    Map<Integer, List<NoteBlock>> ticks, int length) {
            this.name = name;
            this.author = author;
            this.ticksPerSecond = tps;
            this.ticks = ticks;
            this.length = length;
        }
    }

    public static class NoteBlock {
        public final int layer;
        public final byte instrument;
        public final byte key;

        public NoteBlock(int layer, byte instrument, byte key) {
            this.layer = layer;
            this.instrument = instrument;
            this.key = key;
        }

        public Sound getBukkitSound() {
            return switch (instrument) {
                case 1  -> Sound.BLOCK_NOTE_BLOCK_BASS;
                case 2  -> Sound.BLOCK_NOTE_BLOCK_BASEDRUM;
                case 3  -> Sound.BLOCK_NOTE_BLOCK_SNARE;
                case 4  -> Sound.BLOCK_NOTE_BLOCK_HAT;
                case 5  -> Sound.BLOCK_NOTE_BLOCK_GUITAR;
                case 6  -> Sound.BLOCK_NOTE_BLOCK_FLUTE;
                case 7  -> Sound.BLOCK_NOTE_BLOCK_BELL;
                case 8  -> Sound.BLOCK_NOTE_BLOCK_CHIME;
                case 9  -> Sound.BLOCK_NOTE_BLOCK_XYLOPHONE;
                case 10 -> Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE;
                case 11 -> Sound.BLOCK_NOTE_BLOCK_COW_BELL;
                case 12 -> Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO;
                case 13 -> Sound.BLOCK_NOTE_BLOCK_BIT;
                case 14 -> Sound.BLOCK_NOTE_BLOCK_BANJO;
                case 15 -> Sound.BLOCK_NOTE_BLOCK_PLING;
                default -> Sound.BLOCK_NOTE_BLOCK_HARP;
            };
        }

        public float getPitch() {
            int adjusted = Math.min(Math.max(key - 33, 0), 24);
            return (float) Math.pow(2.0, (adjusted - 12.0) / 12.0);
        }
    }
}
