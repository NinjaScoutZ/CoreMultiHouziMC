package com.houzicore.shared.common.util;

import java.util.*;

/**
 * HouziColorParser — Rich text parser adapted from PixelMOTD's UniversalColorParser.
 *
 * Supported formats:
 *   Classic: &a, &l, &r, &k, &n, &o, &m (all vanilla §-codes via & prefix)
 *   Hex inline: &#RRGGBB or &xRRGGBB
 *   Named colors: &gold&, &red&, <red>, <gold>, etc.
 *   MiniMessage hex: <#RRGGBB>text</#>
 *   Gradient: <GRADIENT:ff0000,0000ff>text</GRADIENT>
 *   Rainbow: <rainbow>text</rainbow>
 *   Styles: <bold>, <italic>, <underline>, <strikethrough>, <obfuscated>
 *
 * Usage:
 *   String result = HouziColorParser.parse("&aHello <GRADIENT:#ff0000,#0000ff>World</GRADIENT>");
 *   // Returns a Bukkit-compatible §-encoded string with RGB §x codes for 1.16+
 */
public class HouziColorParser {

    private static final int MAX_SEGMENTS = 50_000;
    private static final int MAX_GRADIENT_EXPANSION = 4096;

    // ── Internal data model ──────────────────────────────────────────────────

    public static class Segment {
        public final String text;
        public final RGBColor color;
        public final boolean gradient;
        public final boolean bold, italic, underlined, strikethrough, obfuscated;

        public Segment(String text, RGBColor color, boolean gradient,
                       boolean bold, boolean italic, boolean underlined,
                       boolean strikethrough, boolean obfuscated) {
            this.text = text; this.color = color; this.gradient = gradient;
            this.bold = bold; this.italic = italic; this.underlined = underlined;
            this.strikethrough = strikethrough; this.obfuscated = obfuscated;
        }
        public Segment(String text, RGBColor color,
                       boolean bold, boolean italic, boolean underlined,
                       boolean strikethrough, boolean obfuscated) {
            this(text, color, false, bold, italic, underlined, strikethrough, obfuscated);
        }
        public Segment(String text, RGBColor color) {
            this(text, color, false, false, false, false, false);
        }
    }

    public static class RGBColor {
        public final int r, g, b;
        public RGBColor(int r, int g, int b) { this.r=r; this.g=g; this.b=b; }

        public static RGBColor fromHex(String hex) {
            String h = hex.replace("#","");
            if (h.length() == 3) h = ""+h.charAt(0)+h.charAt(0)+h.charAt(1)+h.charAt(1)+h.charAt(2)+h.charAt(2);
            if (h.length() != 6) return null;
            try {
                int v = Integer.parseInt(h, 16);
                return new RGBColor((v>>16)&0xFF, (v>>8)&0xFF, v&0xFF);
            } catch (NumberFormatException e) { return null; }
        }

        public String toHex() { return String.format("#%02x%02x%02x", r, g, b); }

        /** Bukkit §x§r§r§g§g§b§b format for 1.16+ Spigot */
        public String toBukkitHex() {
            return "§x" +
                "§" + Character.forDigit((r >> 4) & 0xF, 16) +
                "§" + Character.forDigit(r & 0xF, 16) +
                "§" + Character.forDigit((g >> 4) & 0xF, 16) +
                "§" + Character.forDigit(g & 0xF, 16) +
                "§" + Character.forDigit((b >> 4) & 0xF, 16) +
                "§" + Character.forDigit(b & 0xF, 16);
        }

        @Override public boolean equals(Object o) {
            if (!(o instanceof RGBColor)) return false;
            RGBColor c = (RGBColor) o;
            return c.r==r && c.g==g && c.b==b;
        }
        @Override public int hashCode() { return Objects.hash(r,g,b); }
    }

    // ── Named color map ──────────────────────────────────────────────────────

    private static final Map<String, Character> NAME_TO_LEGACY = new HashMap<>();
    static {
        NAME_TO_LEGACY.put("black",'0'); NAME_TO_LEGACY.put("dark_blue",'1');
        NAME_TO_LEGACY.put("dark_green",'2'); NAME_TO_LEGACY.put("dark_aqua",'3');
        NAME_TO_LEGACY.put("darkred",'4'); NAME_TO_LEGACY.put("dark_red",'4');
        NAME_TO_LEGACY.put("dark_purple",'5'); NAME_TO_LEGACY.put("gold",'6');
        NAME_TO_LEGACY.put("gray",'7'); NAME_TO_LEGACY.put("dark_gray",'8');
        NAME_TO_LEGACY.put("blue",'9'); NAME_TO_LEGACY.put("green",'a');
        NAME_TO_LEGACY.put("aqua",'b'); NAME_TO_LEGACY.put("red",'c');
        NAME_TO_LEGACY.put("light_purple",'d'); NAME_TO_LEGACY.put("yellow",'e');
        NAME_TO_LEGACY.put("white",'f');
    }

    private static RGBColor legacyToColor(char code) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> new RGBColor(0,0,0);
            case '1' -> new RGBColor(0,0,170);
            case '2' -> new RGBColor(0,170,0);
            case '3' -> new RGBColor(0,170,170);
            case '4' -> new RGBColor(170,0,0);
            case '5' -> new RGBColor(170,0,170);
            case '6' -> new RGBColor(255,170,0);
            case '7' -> new RGBColor(170,170,170);
            case '8' -> new RGBColor(85,85,85);
            case '9' -> new RGBColor(85,85,255);
            case 'a' -> new RGBColor(85,255,85);
            case 'b' -> new RGBColor(85,255,255);
            case 'c' -> new RGBColor(255,85,85);
            case 'd' -> new RGBColor(255,85,255);
            case 'e' -> new RGBColor(255,255,85);
            case 'f' -> new RGBColor(255,255,255);
            default -> null;
        };
    }

    // ── Main API: parse to Bukkit string ─────────────────────────────────────

    /**
     * Parse a rich-text string and return a §-encoded Bukkit string.
     * Works on Spigot 1.16+ for hex colors; falls back to nearest legacy for older.
     */
    public static String parse(String input) {
        if (input == null) return "";
        List<Segment> segments = parseToSegments(input);
        return segmentsToBukkit(segments);
    }

    /**
     * Same as parse() but operates on a list of lines (e.g. scoreboard rows).
     */
    public static List<String> parseLines(List<String> lines) {
        List<String> out = new ArrayList<>();
        for (String line : lines) out.add(parse(line));
        return out;
    }

    // ── Segment renderer ─────────────────────────────────────────────────────

    private static String segmentsToBukkit(List<Segment> segments) {
        StringBuilder sb = new StringBuilder();
        for (Segment seg : segments) {
            if (seg.color != null) {
                sb.append(seg.color.toBukkitHex());
            }
            if (seg.bold)          sb.append("§l");
            if (seg.italic)        sb.append("§o");
            if (seg.underlined)    sb.append("§n");
            if (seg.strikethrough) sb.append("§m");
            if (seg.obfuscated)    sb.append("§k");
            sb.append(seg.text);
        }
        return sb.toString();
    }

    // ── Parser ───────────────────────────────────────────────────────────────

    public static List<Segment> parseToSegments(String input) {
        List<Segment> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        RGBColor curColor = null;
        boolean bold=false, italic=false, under=false, strike=false, obf=false;
        int segCreated = 0;
        int i=0, len = input.length();

        while (i < len) {
            if (segCreated > MAX_SEGMENTS) { cur.append(input.substring(i)); break; }
            char ch = input.charAt(i);

            // ── & codes ───────────────────────────────────────────────────────
            if (ch == '&') {
                // escaped &&
                if (i+1 < len && input.charAt(i+1) == '&') { cur.append('&'); i+=2; continue; }

                // &x hex (&xRRGGBB or &x&R&R&G&G&B&B)
                ParseHexResult ph = tryParseAmpHex(input, i);
                if (ph != null) {
                    if (!cur.isEmpty()) { out.add(new Segment(cur.toString(), curColor, false, bold, italic, under, strike, obf)); cur.setLength(0); segCreated++; }
                    curColor = ph.color; i = ph.newIndex; continue;
                }

                // &#RRGGBB
                if (i+1 < len && input.charAt(i+1) == '#') {
                    if (!cur.isEmpty()) { out.add(new Segment(cur.toString(), curColor, false, bold, italic, under, strike, obf)); cur.setLength(0); segCreated++; }
                    int j = i+2; StringBuilder hx = new StringBuilder();
                    while (j < len && isHex(input.charAt(j)) && hx.length() < 6) { hx.append(input.charAt(j)); j++; }
                    if (hx.length() >= 3) { curColor = RGBColor.fromHex(hx.toString()); i = j; continue; }
                }

                // &name& style
                int j = i+1;
                while (j < len && (Character.isLetter(input.charAt(j)) || input.charAt(j)=='_' || input.charAt(j)=='-')) j++;
                if (j < len && j > i+1 && input.charAt(j) == '&') {
                    String name = input.substring(i+1, j).toLowerCase();
                    Character code = NAME_TO_LEGACY.get(name);
                    if (code != null) {
                        if (!cur.isEmpty()) { out.add(new Segment(cur.toString(), curColor, false, bold, italic, under, strike, obf)); cur.setLength(0); segCreated++; }
                        curColor = legacyToColor(code); i = j+1; continue;
                    }
                }

                // single char codes
                if (i+1 < len) {
                    if (!cur.isEmpty()) { out.add(new Segment(cur.toString(), curColor, false, bold, italic, under, strike, obf)); cur.setLength(0); segCreated++; }
                    char code = Character.toLowerCase(input.charAt(i+1)); i+=2;
                    switch (code) {
                        case 'k': obf = true; break;
                        case 'l': bold = true; break;
                        case 'm': strike = true; break;
                        case 'n': under = true; break;
                        case 'o': italic = true; break;
                        case 'r': curColor = null; bold = italic = under = strike = obf = false; break;
                        default:
                            RGBColor lc = legacyToColor(code);
                            if (lc != null) { curColor = lc; }
                            else { cur.append('&').append(code); }
                            break;
                    }
                    continue;
                }
                cur.append('&'); i++; continue;
            }

            // ── < tags ────────────────────────────────────────────────────────
            if (ch == '<') {
                // <#RRGGBB>text</#>
                if (i+1 < len && input.charAt(i+1)=='#') {
                    if (!cur.isEmpty()) { out.add(new Segment(cur.toString(), curColor, false, bold, italic, under, strike, obf)); cur.setLength(0); segCreated++; }
                    int j = i+2; StringBuilder hx = new StringBuilder();
                    while (j < len && isHex(input.charAt(j)) && hx.length() < 6) { hx.append(input.charAt(j)); j++; }
                    if (j < len && input.charAt(j) == '>') {
                        j++;
                        RGBColor tagColor = RGBColor.fromHex(hx.toString());
                        int closeSimple = input.indexOf("</#>", j);
                        int end = (closeSimple != -1) ? closeSimple : len;
                        String body = input.substring(j, end);
                        out.addAll(applyColorOverride(body, tagColor, bold, italic, under, strike, obf));
                        i = (closeSimple != -1) ? closeSimple + 4 : len;
                        continue;
                    }
                }

                // <GRADIENT:hex1,hex2,...>text</GRADIENT>
                if (matchesIgnoreCase(input, i, "<GRADIENT:")) {
                    int colon = i + "<GRADIENT:".length();
                    int closeTag = input.indexOf('>', colon);
                    if (closeTag != -1) {
                        String colorsStr = input.substring(colon, closeTag);
                        List<RGBColor> stops = parseColorList(colorsStr);
                        if (!stops.isEmpty()) {
                            int bodyStart = closeTag + 1;
                            int closeGrad = indexOfIgnoreCase(input, "</GRADIENT>", bodyStart);
                            String body = (closeGrad != -1) ? input.substring(bodyStart, closeGrad) : input.substring(bodyStart);
                            out.addAll(expandGradient(body, stops, bold, italic, under, strike, obf));
                            i = (closeGrad != -1) ? closeGrad + "</GRADIENT>".length() : len;
                            continue;
                        }
                    }
                }

                // <RAINBOW>text</RAINBOW>
                if (matchesIgnoreCase(input, i, "<RAINBOW>") || matchesIgnoreCase(input, i, "<rainbow>")) {
                    int bodyStart = input.indexOf('>', i) + 1;
                    int closeRainbow = indexOfIgnoreCase(input, "</RAINBOW>", bodyStart);
                    String body = (closeRainbow != -1) ? input.substring(bodyStart, closeRainbow) : input.substring(bodyStart);
                    out.addAll(expandRainbow(body, bold, italic, under, strike, obf));
                    i = (closeRainbow != -1) ? closeRainbow + "</RAINBOW>".length() : len;
                    continue;
                }

                // named color/style tags: <red>, <gold>, <bold>, <italic>, etc.
                String tag = readTagName(input, i);
                if (tag != null) {
                    String tl = tag.toLowerCase();
                    boolean isStyle = tl.equals("b") || tl.equals("bold") || tl.equals("i") || tl.equals("italic")
                            || tl.equals("u") || tl.equals("underline") || tl.equals("s") || tl.equals("strikethrough")
                            || tl.equals("obf") || tl.equals("obfuscated");

                    if (isStyle) {
                        int openEnd = input.indexOf('>', i); if (openEnd == -1) { i++; continue; }
                        int close = indexOfIgnoreCase(input, "</" + tag + ">", openEnd + 1);
                        String body = (close != -1) ? input.substring(openEnd+1, close) : input.substring(openEnd+1);
                        out.addAll(applyStyleOverride(body, tl));
                        i = (close != -1) ? close + tag.length() + 3 : len;
                        continue;
                    }

                    Character code = NAME_TO_LEGACY.get(tl);
                    if (code != null) {
                        int openEnd = input.indexOf('>', i); if (openEnd == -1) { i++; continue; }
                        int close = indexOfIgnoreCase(input, "</" + tag + ">", openEnd + 1);
                        String body = (close != -1) ? input.substring(openEnd+1, close) : input.substring(openEnd+1);
                        out.addAll(applyColorOverride(body, legacyToColor(code), bold, italic, under, strike, obf));
                        i = (close != -1) ? close + tag.length() + 3 : len;
                        continue;
                    }
                }
            }

            cur.append(ch); i++;
        }

        if (!cur.isEmpty()) out.add(new Segment(cur.toString(), curColor, false, bold, italic, under, strike, obf));
        return mergeSegments(out);
    }

    // ── Gradient expansion ───────────────────────────────────────────────────

    private static List<Segment> expandGradient(String body, List<RGBColor> stops,
                                                 boolean bold, boolean italic, boolean under,
                                                 boolean strike, boolean obf) {
        record CE(char ch, boolean b, boolean iv, boolean u, boolean s, boolean k) {}
        List<CE> chars = new ArrayList<>();
        for (Segment sg : parseToSegments(body))
            for (int j=0; j<sg.text.length(); j++)
                chars.add(new CE(sg.text.charAt(j), sg.bold||bold, sg.italic||italic, sg.underlined||under, sg.strikethrough||strike, sg.obfuscated||obf));

        int n = chars.size(); if (n == 0) return Collections.emptyList();
        int numStops = stops.size();
        List<Segment> out = new ArrayList<>(n);

        for (int idx = 0; idx < n; idx++) {
            double tGlobal = (double) idx / Math.max(1, n-1);
            double scaled = tGlobal * (numStops - 1);
            int left = Math.min((int) Math.floor(scaled), numStops - 2);
            double localT = scaled - left;
            RGBColor c = lerpColor(stops.get(left), stops.get(left+1), localT);
            CE ce = chars.get(idx);
            out.add(new Segment(String.valueOf(ce.ch()), c, true, ce.b(), ce.iv(), ce.u(), ce.s(), ce.k()));
        }
        return mergeSegments(out);
    }

    private static List<Segment> expandRainbow(String body,
                                                boolean bold, boolean italic, boolean under,
                                                boolean strike, boolean obf) {
        record CE(char ch, boolean b, boolean iv, boolean u, boolean s, boolean k) {}
        List<CE> chars = new ArrayList<>();
        for (Segment sg : parseToSegments(body))
            for (int j=0; j<sg.text.length(); j++)
                chars.add(new CE(sg.text.charAt(j), sg.bold||bold, sg.italic||italic, sg.underlined||under, sg.strikethrough||strike, sg.obfuscated||obf));

        int n = chars.size(); if (n == 0) return Collections.emptyList();
        List<Segment> out = new ArrayList<>(n);
        for (int idx=0; idx<n; idx++) {
            double t = (double) idx / Math.max(1, n-1);
            RGBColor c = hsvToRgb(t, 1.0, 1.0);
            CE ce = chars.get(idx);
            out.add(new Segment(String.valueOf(ce.ch()), c, true, ce.b(), ce.iv(), ce.u(), ce.s(), ce.k()));
        }
        return mergeSegments(out);
    }

    // ── Override helpers ─────────────────────────────────────────────────────

    private static List<Segment> applyColorOverride(String body, RGBColor color,
                                                     boolean bold, boolean italic, boolean under,
                                                     boolean strike, boolean obf) {
        List<Segment> out = new ArrayList<>();
        for (Segment s : parseToSegments(body))
            for (int k=0; k<s.text.length(); k++)
                out.add(new Segment(String.valueOf(s.text.charAt(k)), color, s.bold||bold, s.italic||italic, s.underlined||under, s.strikethrough||strike, s.obfuscated||obf));
        return mergeSegments(out);
    }

    private static List<Segment> applyStyleOverride(String body, String styleTag) {
        boolean addBold = styleTag.equals("b") || styleTag.equals("bold");
        boolean addItalic = styleTag.equals("i") || styleTag.equals("italic");
        boolean addUnder = styleTag.equals("u") || styleTag.equals("underline");
        boolean addStrike = styleTag.equals("s") || styleTag.equals("strikethrough");
        boolean addObf = styleTag.equals("obf") || styleTag.equals("obfuscated");
        List<Segment> out = new ArrayList<>();
        for (Segment s : parseToSegments(body))
            for (int k=0; k<s.text.length(); k++)
                out.add(new Segment(String.valueOf(s.text.charAt(k)), s.color, s.gradient, s.bold||addBold, s.italic||addItalic, s.underlined||addUnder, s.strikethrough||addStrike, s.obfuscated||addObf));
        return mergeSegments(out);
    }

    // ── Merge adjacent same-style segments ───────────────────────────────────

    private static List<Segment> mergeSegments(List<Segment> in) {
        if (in.isEmpty()) return in;
        List<Segment> out = new ArrayList<>();
        Segment cur = in.get(0);
        for (int i=1; i<in.size(); i++) {
            Segment s = in.get(i);
            if (Objects.equals(cur.color, s.color) && cur.bold==s.bold && cur.italic==s.italic
                    && cur.underlined==s.underlined && cur.strikethrough==s.strikethrough
                    && cur.obfuscated==s.obfuscated && cur.gradient==s.gradient) {
                cur = new Segment(cur.text+s.text, cur.color, cur.gradient, cur.bold, cur.italic, cur.underlined, cur.strikethrough, cur.obfuscated);
            } else { out.add(cur); cur = s; }
        }
        out.add(cur);
        return out;
    }

    // ── String utilities ─────────────────────────────────────────────────────

    private static List<RGBColor> parseColorList(String str) {
        List<RGBColor> list = new ArrayList<>();
        for (String part : str.split("[,;|:]")) {
            RGBColor c = RGBColor.fromHex(part.trim().replace("#",""));
            if (c != null) list.add(c);
        }
        return list;
    }

    private static boolean matchesIgnoreCase(String input, int idx, String prefix) {
        if (idx + prefix.length() > input.length()) return false;
        return input.substring(idx, idx + prefix.length()).equalsIgnoreCase(prefix);
    }

    private static int indexOfIgnoreCase(String s, String sub, int from) {
        return s.toLowerCase().indexOf(sub.toLowerCase(), from);
    }

    private static String readTagName(String input, int idx) {
        if (idx >= input.length() || input.charAt(idx) != '<') return null;
        int j = idx + 1; StringBuilder b = new StringBuilder();
        while (j < input.length()) {
            char c = input.charAt(j);
            if (Character.isLetter(c) || c=='_' || c=='-') { b.append(c); j++; }
            else break;
        }
        return b.isEmpty() ? null : b.toString();
    }

    private static boolean isHex(char c) {
        return (c>='0'&&c<='9') || (c>='a'&&c<='f') || (c>='A'&&c<='F');
    }

    private static class ParseHexResult { final RGBColor color; final int newIndex; ParseHexResult(RGBColor c, int i){color=c;newIndex=i;} }

    private static ParseHexResult tryParseAmpHex(String input, int idx) {
        int len = input.length(); if (idx+1 >= len) return null;
        char x = input.charAt(idx+1); if (x != 'x' && x != 'X') return null;
        int pos = idx + 2;
        // &x&R&R&G&G&B&B format
        if (pos < len && input.charAt(pos)=='&') {
            int p = pos; StringBuilder hx = new StringBuilder(6);
            for (int k=0; k<6; k++) {
                if (p >= len || input.charAt(p)!='&') return null; p++;
                if (p >= len) return null; char hc = input.charAt(p);
                if (!isHex(hc)) return null; hx.append(hc); p++;
            }
            return new ParseHexResult(RGBColor.fromHex(hx.toString()), p);
        }
        // &xRRGGBB format
        if (pos + 6 <= len) {
            boolean ok = true;
            for (int k=0; k<6; k++) if (!isHex(input.charAt(pos+k))) { ok=false; break; }
            if (ok) return new ParseHexResult(RGBColor.fromHex(input.substring(pos, pos+6)), pos+6);
        }
        return null;
    }

    // ── Color math ───────────────────────────────────────────────────────────

    private static RGBColor lerpColor(RGBColor a, RGBColor b, double t) {
        return new RGBColor(clamp((int)Math.round(a.r+(b.r-a.r)*t)), clamp((int)Math.round(a.g+(b.g-a.g)*t)), clamp((int)Math.round(a.b+(b.b-a.b)*t)));
    }
    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
    private static RGBColor hsvToRgb(double h, double s, double v) {
        int i = (int)Math.floor(h*6); double f=h*6-i, p=v*(1-s), q=v*(1-f*s), t=v*(1-(1-f)*s);
        double r=0,g=0,b=0;
        switch (i%6) { case 0: r=v;g=t;b=p; break; case 1: r=q;g=v;b=p; break; case 2: r=p;g=v;b=t; break; case 3: r=p;g=q;b=v; break; case 4: r=t;g=p;b=v; break; default: r=v;g=p;b=q; break; }
        return new RGBColor((int)Math.round(r*255),(int)Math.round(g*255),(int)Math.round(b*255));
    }
}
