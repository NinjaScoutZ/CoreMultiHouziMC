package com.houzicore.shared.core.displayentity.function;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.joml.Matrix4f;

/**
 * Tiny BDEngine mcfunction runner used by the display-entity furniture workflow.
 * <p>
 * This is intentionally not a full datapack implementation. It supports the
 * subset emitted by block-display.com/BDEngine exports: direct command lines,
 * {@code function namespace:path}, {@code schedule function namespace:path ...},
 * and the common {@code execute as @e[...] ... run function namespace:path}
 * trampoline used by animated keyframes.
 */
public class BdeFunctionRuntime {

    private static final Pattern NAMESPACE_REF = Pattern.compile("(?:function|storage)\\s+([a-z0-9_.-]+):", Pattern.CASE_INSENSITIVE);
    private static final Pattern SCHEDULE_FUNCTION = Pattern.compile("^schedule\\s+function\\s+([a-z0-9_.-]+:[a-z0-9_./-]+)\\s+([^\\s]+).*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DIRECT_FUNCTION = Pattern.compile("^function\\s+([a-z0-9_.-]+:[a-z0-9_./-]+)\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern RUN_FUNCTION = Pattern.compile("\\brun\\s+function\\s+([a-z0-9_.-]+:[a-z0-9_./-]+)\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXECUTE_AS_SELECTOR = Pattern.compile("^execute\\s+as\\s+(@e\\[[^\\]]+\\])", Pattern.CASE_INSENSITIVE);
    private static final Pattern TAG_CONDITION = Pattern.compile("\\b(if|unless)\\s+entity\\s+@s\\[tag=([^\\]]+)\\]", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXECUTE_TAG_SELF = Pattern.compile("^execute\\s+as\\s+(@e\\[[^\\]]+\\])(?:\\s+at\\s+@s)?\\s+run\\s+tag\\s+@s\\s+(add|remove)\\s+([a-z0-9_./-]+)\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXECUTE_DATA_MODIFY_ENTITY_INT = Pattern.compile("^execute\\s+as\\s+(@e\\[[^\\]]+\\])(?:\\s+at\\s+@s)?\\s+run\\s+data\\s+modify\\s+entity\\s+(@e\\[[^\\]]+\\])\\s+([a-z_]+)\\s+set\\s+value\\s+(-?\\d+)\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATA_MERGE_ENTITY = Pattern.compile("^data\\s+merge\\s+entity\\s+(@e\\[[^\\]]+\\])\\s+\\{(.+)}\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATA_MODIFY_ENTITY_INT = Pattern.compile("^data\\s+modify\\s+entity\\s+(@e\\[[^\\]]+\\])\\s+([a-z_]+)\\s+set\\s+value\\s+(-?\\d+)\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern TRANSFORMATION_LIST = Pattern.compile("transformation\\s*:\\s*\\[([^\\]]+)]", Pattern.CASE_INSENSITIVE);
    private static final Pattern BLOCK_DISPLAY_SUMMON_POSITION = Pattern.compile("^summon\\s+block_display\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\{.*)$", Pattern.CASE_INSENSITIVE);
    private static final String SELECTOR_VALUE_PATTERN = "(?:^|\\[|,)%s=([^,\\]]+)";

    private final JavaPlugin _plugin;
    private final Map<String, List<String>> _functions = new HashMap<>();
    private final Map<String, BdeFunctionPack> _packs = new LinkedHashMap<>();
    private final Map<String, List<BukkitTask>> _scheduledTasks = new HashMap<>();

    public BdeFunctionRuntime(JavaPlugin plugin) {
        _plugin = plugin;
    }

    public void reload(File modelsDirectory) {
        cancelAllScheduled();
        _functions.clear();
        _packs.clear();

        if (modelsDirectory == null) {
            return;
        }

        List<File> functionRoots = findFunctionRoots(modelsDirectory);
        for (File root : functionRoots) {
            loadFunctionRoot(root);
        }

        System.out.println("[BdeFunctionRuntime] Loaded " + _functions.size() + " function(s) from "
                + _packs.size() + " pack(s).");
    }

    public Collection<BdeFunctionPack> getPacks() {
        return Collections.unmodifiableCollection(_packs.values());
    }

    public Optional<BdeFunctionPack> getPack(String namespace) {
        if (namespace == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(_packs.get(namespace.toLowerCase(Locale.ROOT)));
    }

    public boolean hasFunction(String functionId) {
        return _functions.containsKey(normalizeFunctionId(functionId));
    }

    public boolean executeFunction(String functionId, Location origin) {
        return executeFunction(functionId, new ExecutionContext(origin), 0);
    }

    public boolean spawnAndPlayLoop(String namespace, Location origin) {
        Optional<BdeFunctionPack> packOpt = getPack(namespace);
        if (packOpt.isEmpty()) {
            return false;
        }

        BdeFunctionPack pack = packOpt.get();
        cancelScheduled(pack.getNamespace());
        String create = pack.getCreateFunctionId();
        if (create == null) {
            return false;
        }

        boolean ok = executeFunction(create, origin);
        for (String loop : pack.getLoopAnimationFunctionIds()) {
            executeFunction(loop, origin);
            break;
        }
        return ok;
    }

    public FunctionFootprint estimateFootprint(String namespace) {
        Optional<BdeFunctionPack> packOpt = getPack(namespace);
        if (packOpt.isEmpty()) {
            return FunctionFootprint.defaultFootprint();
        }

        String create = packOpt.get().getCreateFunctionId();
        List<String> lines = _functions.get(normalizeFunctionId(create));
        if (lines == null) {
            return FunctionFootprint.defaultFootprint();
        }

        double[] rootOffset = parseRootOffset(lines, packOpt.get().getNamespace());
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (String line : lines) {
            Matcher matcher = TRANSFORMATION_LIST.matcher(line);
            while (matcher.find()) {
                float[] matrix = parseTransformationMatrixBody(matcher.group(1));
                if (matrix == null) {
                    continue;
                }

                double x = matrix[3];
                double y = matrix[7];
                double z = matrix[11];
                double halfX = Math.max(0.08, vectorLength(matrix[0], matrix[1], matrix[2]) * 0.5);
                double halfY = Math.max(0.08, vectorLength(matrix[4], matrix[5], matrix[6]) * 0.5);
                double halfZ = Math.max(0.08, vectorLength(matrix[8], matrix[9], matrix[10]) * 0.5);

                minX = Math.min(minX, x - halfX);
                minY = Math.min(minY, y - halfY);
                minZ = Math.min(minZ, z - halfZ);
                maxX = Math.max(maxX, x + halfX);
                maxY = Math.max(maxY, y + halfY);
                maxZ = Math.max(maxZ, z + halfZ);
            }
        }

        if (!Double.isFinite(minX)) {
            return FunctionFootprint.defaultFootprint();
        }

        return new FunctionFootprint(
                rootOffset[0] + ((minX + maxX) * 0.5),
                rootOffset[1] + ((minY + maxY) * 0.5),
                rootOffset[2] + ((minZ + maxZ) * 0.5),
                Math.max(0.45, maxX - minX),
                Math.max(0.45, maxY - minY),
                Math.max(0.45, maxZ - minZ)
        );
    }

    public boolean stopPackAnimation(String namespace, Location origin) {
        Optional<BdeFunctionPack> packOpt = getPack(namespace);
        if (packOpt.isEmpty()) {
            return false;
        }

        BdeFunctionPack pack = packOpt.get();
        cancelScheduled(pack.getNamespace());
        String stop = pack.getNamespace() + ":_/stop_anim";
        return !hasFunction(stop) || executeFunction(stop, origin);
    }

    public boolean deletePackEntities(String namespace, Location origin) {
        Optional<BdeFunctionPack> packOpt = getPack(namespace);
        if (packOpt.isEmpty()) {
            return false;
        }

        BdeFunctionPack pack = packOpt.get();
        cancelScheduled(pack.getNamespace());
        String delete = pack.getDeleteFunctionId();
        return delete != null && executeFunction(delete, origin);
    }

    private boolean executeFunction(String functionId, ExecutionContext context, int depth) {
        String normalized = normalizeFunctionId(functionId);
        if (normalized.endsWith("/play_anim") || normalized.endsWith("/play_anim_loop")) {
            cancelScheduled(namespaceOf(normalized));
        }

        List<String> lines = _functions.get(normalized);
        if (lines == null) {
            System.out.println("[BdeFunctionRuntime] Missing function: " + normalized);
            return false;
        }
        if (depth > 32) {
            System.out.println("[BdeFunctionRuntime] Function recursion limit reached at: " + normalized);
            return false;
        }

        for (String raw : lines) {
            executeLine(raw, context, depth);
        }
        return true;
    }

    private void executeLine(String raw, ExecutionContext context, int depth) {
        String line = raw.trim();
        if (line.isEmpty() || line.startsWith("#")) {
            return;
        }

        Matcher direct = DIRECT_FUNCTION.matcher(line);
        if (direct.matches()) {
            executeFunction(direct.group(1), context, depth + 1);
            return;
        }

        Matcher schedule = SCHEDULE_FUNCTION.matcher(line);
        if (schedule.matches()) {
            String target = schedule.group(1);
            long delayTicks = parseDelayTicks(schedule.group(2));
            ExecutionContext scheduledContext = context.copy();
            scheduleFunction(target, scheduledContext, delayTicks);
            return;
        }

        Matcher runFunction = RUN_FUNCTION.matcher(line);
        if (runFunction.find()) {
            executeRunFunctionLine(line, runFunction.group(1), context, depth);
            return;
        }

        if (tryApplyTagSelf(line, context)) {
            return;
        }

        if (tryApplyDataMerge(line, context)) {
            return;
        }

        if (tryApplyDataModify(line, context)) {
            return;
        }

        if (tryApplyExecuteDataModify(line, context)) {
            return;
        }

        dispatchVanilla(line, context);
    }

    private void scheduleFunction(String target, ExecutionContext context, long delayTicks) {
        String namespace = namespaceOf(target);
        final BukkitTask[] holder = new BukkitTask[1];
        BukkitTask task = Bukkit.getScheduler().runTaskLater(_plugin, () -> {
            removeScheduled(namespace, holder[0]);
            // A scheduled function is a fresh tick, not recursive stack depth.
            executeFunction(target, context, 0);
        }, delayTicks);
        holder[0] = task;
        _scheduledTasks.computeIfAbsent(namespace, k -> new ArrayList<>()).add(task);
    }

    private void removeScheduled(String namespace, BukkitTask task) {
        List<BukkitTask> tasks = _scheduledTasks.get(namespace);
        if (tasks == null) {
            return;
        }
        tasks.remove(task);
        if (tasks.isEmpty()) {
            _scheduledTasks.remove(namespace);
        }
    }

    private void cancelScheduled(String namespace) {
        List<BukkitTask> tasks = _scheduledTasks.remove(namespace);
        if (tasks == null) {
            return;
        }
        for (BukkitTask task : tasks) {
            task.cancel();
        }
    }

    private void cancelAllScheduled() {
        for (List<BukkitTask> tasks : _scheduledTasks.values()) {
            for (BukkitTask task : tasks) {
                task.cancel();
            }
        }
        _scheduledTasks.clear();
    }

    private void executeRunFunctionLine(String line, String functionId, ExecutionContext context, int depth) {
        Matcher executeAs = EXECUTE_AS_SELECTOR.matcher(line);
        if (!executeAs.find()) {
            executeFunction(functionId, context, depth + 1);
            return;
        }

        Selector selector = Selector.parse(executeAs.group(1));
        List<Entity> targets = selector.find(context.origin);
        Matcher tagCondition = TAG_CONDITION.matcher(line);

        for (Entity target : targets) {
            if (tagCondition.find(0)) {
                boolean hasTag = target.getScoreboardTags().contains(tagCondition.group(2));
                boolean required = tagCondition.group(1).equalsIgnoreCase("if");
                if (required != hasTag) {
                    continue;
                }
            }

            executeFunction(functionId, new ExecutionContext(target.getLocation()), depth + 1);
        }
    }

    private boolean tryApplyTagSelf(String line, ExecutionContext context) {
        Matcher matcher = EXECUTE_TAG_SELF.matcher(line);
        if (!matcher.matches()) {
            return false;
        }

        Selector selector = Selector.parse(matcher.group(1));
        boolean add = matcher.group(2).equalsIgnoreCase("add");
        String tag = matcher.group(3);
        for (Entity entity : selector.find(context.origin)) {
            if (add) {
                entity.addScoreboardTag(tag);
            } else {
                entity.removeScoreboardTag(tag);
            }
        }
        return true;
    }

    private void dispatchVanilla(String command, ExecutionContext context) {
        Location origin = context.origin;
        if (origin == null || origin.getWorld() == null) {
            return;
        }

        command = applyRootSummonTransform(command, context);
        String wrapped = "execute in " + origin.getWorld().getKey()
                + " positioned " + format(origin.getX())
                + " " + format(origin.getY())
                + " " + format(origin.getZ())
                + " rotated " + format(origin.getYaw())
                + " " + format(origin.getPitch())
                + " run " + command;

        CommandSender console = Bukkit.getConsoleSender();
        boolean ok = Bukkit.dispatchCommand(console, wrapped);
        if (!ok) {
            System.out.println("[BdeFunctionRuntime] Command returned false: " + wrapped);
        }
    }

    private boolean tryApplyDataMerge(String line, ExecutionContext context) {
        Matcher matcher = DATA_MERGE_ENTITY.matcher(line);
        if (!matcher.matches()) {
            return false;
        }

        Selector selector = Selector.parse(matcher.group(1));
        String nbt = matcher.group(2);
        List<Entity> targets = selector.find(context.origin);
        if (targets.isEmpty()) {
            return true;
        }

        float[] matrix = parseTransformationMatrix(nbt);
        Integer interpolationDuration = parseNbtInt(nbt, "interpolation_duration");
        Integer interpolationDelay = parseNbtInt(nbt, "start_interpolation");
        Integer teleportDuration = parseNbtInt(nbt, "teleport_duration");

        for (Entity entity : targets) {
            if (!(entity instanceof Display display)) {
                continue;
            }

            if (matrix != null) {
                display.setTransformationMatrix(matrixFromMinecraftRowMajor(matrix));
            }
            if (interpolationDuration != null) {
                display.setInterpolationDuration(clampTicks(interpolationDuration, 0, 59));
            }
            if (interpolationDelay != null) {
                display.setInterpolationDelay(clampTicks(interpolationDelay, -59, 59));
            }
            if (teleportDuration != null) {
                display.setTeleportDuration(clampTicks(teleportDuration, 0, 59));
            }
        }
        return true;
    }

    private boolean tryApplyDataModify(String line, ExecutionContext context) {
        Matcher matcher = DATA_MODIFY_ENTITY_INT.matcher(line);
        if (!matcher.matches()) {
            return false;
        }

        String field = matcher.group(2).toLowerCase(Locale.ROOT);
        int value;
        try {
            value = Integer.parseInt(matcher.group(3));
        } catch (NumberFormatException ex) {
            return true;
        }

        if (!field.equals("teleport_duration")
                && !field.equals("interpolation_duration")
                && !field.equals("start_interpolation")) {
            return false;
        }

        for (Entity entity : Selector.parse(matcher.group(1)).find(context.origin)) {
            if (!(entity instanceof Display display)) {
                continue;
            }
            if (field.equals("teleport_duration")) {
                display.setTeleportDuration(clampTicks(value, 0, 59));
            } else if (field.equals("interpolation_duration")) {
                display.setInterpolationDuration(clampTicks(value, 0, 59));
            } else {
                display.setInterpolationDelay(clampTicks(value, -59, 59));
            }
        }
        return true;
    }

    private boolean tryApplyExecuteDataModify(String line, ExecutionContext context) {
        Matcher matcher = EXECUTE_DATA_MODIFY_ENTITY_INT.matcher(line);
        if (!matcher.matches()) {
            return false;
        }

        Selector sourceSelector = Selector.parse(matcher.group(1));
        Selector targetSelector = Selector.parse(matcher.group(2));
        String field = matcher.group(3).toLowerCase(Locale.ROOT);
        int value;
        try {
            value = Integer.parseInt(matcher.group(4));
        } catch (NumberFormatException ex) {
            return true;
        }

        if (!isSupportedDisplayIntField(field)) {
            return false;
        }

        for (Entity source : sourceSelector.find(context.origin)) {
            applyDisplayIntField(targetSelector.find(source.getLocation()), field, value);
        }
        return true;
    }

    private static boolean isSupportedDisplayIntField(String field) {
        return field.equals("teleport_duration")
                || field.equals("interpolation_duration")
                || field.equals("start_interpolation");
    }

    private static void applyDisplayIntField(List<Entity> entities, String field, int value) {
        for (Entity entity : entities) {
            if (!(entity instanceof Display display)) {
                continue;
            }
            if (field.equals("teleport_duration")) {
                display.setTeleportDuration(clampTicks(value, 0, 59));
            } else if (field.equals("interpolation_duration")) {
                display.setInterpolationDuration(clampTicks(value, 0, 59));
            } else {
                display.setInterpolationDelay(clampTicks(value, -59, 59));
            }
        }
    }

    private static float[] parseTransformationMatrix(String nbt) {
        Matcher matcher = TRANSFORMATION_LIST.matcher(nbt);
        if (!matcher.find()) {
            return null;
        }

        return parseTransformationMatrixBody(matcher.group(1));
    }

    private static float[] parseTransformationMatrixBody(String body) {
        String[] tokens = body.split(",");
        if (tokens.length != 16) {
            return null;
        }

        float[] matrix = new float[16];
        for (int i = 0; i < tokens.length; i++) {
            try {
                matrix[i] = Float.parseFloat(tokens[i].trim().replace("f", "").replace("F", ""));
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return matrix;
    }

    private static double vectorLength(float x, float y, float z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    private static String applyRootSummonTransform(String command, ExecutionContext context) {
        Location origin = context.origin;
        if (origin == null) {
            return command;
        }

        String lower = command.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("summon block_display ") || !lower.contains("_root")) {
            return command;
        }

        Matcher matcher = BLOCK_DISPLAY_SUMMON_POSITION.matcher(command);
        if (!matcher.matches()) {
            return command;
        }

        String xToken = matcher.group(1);
        String yToken = matcher.group(2);
        String zToken = matcher.group(3);
        double x;
        double z;
        if (isRelativeCoordinate(xToken) && isRelativeCoordinate(zToken)) {
            double[] rotated = rotateOffset(parseCoordinateToken(xToken), parseCoordinateToken(zToken), origin.getYaw());
            x = origin.getX() + rotated[0];
            z = origin.getZ() + rotated[1];
        } else {
            x = resolveCoordinate(origin.getX(), xToken);
            z = resolveCoordinate(origin.getZ(), zToken);
        }
        double y = resolveCoordinate(origin.getY(), yToken);

        return String.format(Locale.US,
                "summon block_display %.4f %.4f %.4f %s",
                x,
                y,
                z,
                addRotationToRootNbt(matcher.group(4), origin.getYaw(), origin.getPitch())
        );
    }

    private static double[] parseRootOffset(List<String> lines, String namespace) {
        String rootTag = "\"" + namespace.toLowerCase(Locale.ROOT) + "_root\"";
        for (String raw : lines) {
            String line = raw.trim();
            String lower = line.toLowerCase(Locale.ROOT);
            if (!lower.startsWith("summon block_display ") || !lower.contains(rootTag)) {
                continue;
            }

            Matcher matcher = BLOCK_DISPLAY_SUMMON_POSITION.matcher(line);
            if (!matcher.matches()) {
                continue;
            }

            return new double[] {
                    parseCoordinateToken(matcher.group(1)),
                    parseCoordinateToken(matcher.group(2)),
                    parseCoordinateToken(matcher.group(3))
            };
        }
        return new double[] { 0, 0, 0 };
    }

    private static String addRotationToRootNbt(String nbt, float yaw, float pitch) {
        if (nbt == null || !nbt.startsWith("{") || Pattern.compile("\\bRotation\\s*:", Pattern.CASE_INSENSITIVE).matcher(nbt).find()) {
            return nbt;
        }
        return "{Rotation:[" + format(yaw) + "f," + format(pitch) + "f]," + nbt.substring(1);
    }

    private static double resolveCoordinate(double base, String token) {
        if (isRelativeCoordinate(token)) {
            return base + parseCoordinateToken(token);
        }
        return parseCoordinateToken(token);
    }

    private static boolean isRelativeCoordinate(String token) {
        return token != null && token.trim().startsWith("~");
    }

    private static double[] rotateOffset(double x, double z, float yawDegrees) {
        if (Math.abs(yawDegrees) < 0.0001f) {
            return new double[] { x, z };
        }
        double radians = Math.toRadians(-yawDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new double[] {
                x * cos + z * sin,
                -x * sin + z * cos
        };
    }

    private static double parseCoordinateToken(String token) {
        if (token == null || token.isBlank()) {
            return 0;
        }

        String value = token.trim();
        if (value.startsWith("~")) {
            if (value.length() == 1) {
                return 0;
            }
            value = value.substring(1);
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static Integer parseNbtInt(String nbt, String key) {
        Matcher matcher = Pattern.compile("\\b" + Pattern.quote(key) + "\\s*:\\s*(-?\\d+)", Pattern.CASE_INSENSITIVE).matcher(nbt);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Matrix4f matrixFromMinecraftRowMajor(float[] matrix) {
        return new Matrix4f(
                matrix[0], matrix[4], matrix[8], matrix[12],
                matrix[1], matrix[5], matrix[9], matrix[13],
                matrix[2], matrix[6], matrix[10], matrix[14],
                matrix[3], matrix[7], matrix[11], matrix[15]
        );
    }

    private static int clampTicks(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String format(double value) {
        return String.format(Locale.US, "%.4f", value);
    }

    private static long parseDelayTicks(String token) {
        String normalized = token.toLowerCase(Locale.ROOT).trim();
        try {
            if (normalized.endsWith("s")) {
                double seconds = Double.parseDouble(normalized.substring(0, normalized.length() - 1));
                return Math.max(1L, Math.round(seconds * 20.0));
            }
            if (normalized.endsWith("t")) {
                return Math.max(1L, Long.parseLong(normalized.substring(0, normalized.length() - 1)));
            }
            return Math.max(1L, Long.parseLong(normalized));
        } catch (NumberFormatException ex) {
            return 1L;
        }
    }

    private void loadFunctionRoot(File root) {
        List<File> files = listMcfunctionFiles(root);
        if (files.isEmpty()) {
            return;
        }

        String namespace = inferNamespace(files, root.getParentFile() != null ? root.getParentFile().getName() : "bde");
        List<String> functionIds = new ArrayList<>();

        for (File file : files) {
            try {
                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                String rel = root.toPath().relativize(file.toPath()).toString()
                        .replace(File.separatorChar, '/')
                        .replaceAll("\\.mcfunction$", "");
                String functionId = namespace + ":" + rel.toLowerCase(Locale.ROOT);
                _functions.put(functionId, lines);
                functionIds.add(functionId);
            } catch (IOException ex) {
                System.out.println("[BdeFunctionRuntime] Failed reading " + file + ": " + ex.getMessage());
            }
        }

        _packs.put(namespace, new BdeFunctionPack(namespace, root, functionIds));
    }

    private static List<File> findFunctionRoots(File modelsDirectory) {
        List<File> result = new ArrayList<>();
        File direct = new File(modelsDirectory, "function");
        if (direct.isDirectory()) {
            result.add(direct);
        }

        File[] children = modelsDirectory.listFiles(File::isDirectory);
        if (children != null) {
            for (File child : children) {
                File nested = new File(child, "function");
                if (nested.isDirectory() && !nested.equals(direct)) {
                    result.add(nested);
                }
            }
        }
        return result;
    }

    private static List<File> listMcfunctionFiles(File root) {
        List<File> files = new ArrayList<>();
        collectMcfunctionFiles(root, files);
        return files;
    }

    private static void collectMcfunctionFiles(File dir, List<File> files) {
        File[] children = dir.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                collectMcfunctionFiles(child, files);
            } else if (child.getName().endsWith(".mcfunction")) {
                files.add(child);
            }
        }
    }

    private static String inferNamespace(List<File> files, String fallback) {
        for (File file : files) {
            try {
                for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                    Matcher matcher = NAMESPACE_REF.matcher(line.toLowerCase(Locale.ROOT));
                    if (matcher.find()) {
                        return sanitizeNamespace(matcher.group(1));
                    }
                }
            } catch (IOException ignored) {
            }
        }
        return sanitizeNamespace(fallback);
    }

    private static String sanitizeNamespace(String value) {
        String sanitized = value == null ? "bde" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "");
        return sanitized.isEmpty() ? "bde" : sanitized;
    }

    private static String normalizeFunctionId(String id) {
        return id == null ? "" : id.toLowerCase(Locale.ROOT).replace('\\', '/');
    }

    private static String namespaceOf(String functionId) {
        String normalized = normalizeFunctionId(functionId);
        int idx = normalized.indexOf(':');
        return idx > 0 ? normalized.substring(0, idx) : normalized;
    }

    public static final class FunctionFootprint {
        private final double centerX;
        private final double centerY;
        private final double centerZ;
        private final double width;
        private final double height;
        private final double depth;

        private FunctionFootprint(double centerX, double centerY, double centerZ, double width, double height, double depth) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.centerZ = centerZ;
            this.width = width;
            this.height = height;
            this.depth = depth;
        }

        public static FunctionFootprint defaultFootprint() {
            return new FunctionFootprint(0, 0.65, 0, 0.9, 1.1, 0.9);
        }

        public double getCenterX() { return centerX; }
        public double getCenterY() { return centerY; }
        public double getCenterZ() { return centerZ; }
        public double getWidth() { return width; }
        public double getHeight() { return height; }
        public double getDepth() { return depth; }
    }

    private static final class ExecutionContext {
        private final Location origin;

        private ExecutionContext(Location origin) {
            this.origin = origin == null ? null : origin.clone();
        }

        private ExecutionContext copy() {
            return new ExecutionContext(origin);
        }
    }

    private static final class Selector {
        private final String tag;
        private final String type;
        private final Double maxDistance;
        private final int limit;
        private final boolean nearest;

        private Selector(String tag, String type, Double maxDistance, int limit, boolean nearest) {
            this.tag = tag;
            this.type = type;
            this.maxDistance = maxDistance;
            this.limit = limit;
            this.nearest = nearest;
        }

        private static Selector parse(String selector) {
            return new Selector(
                    selectorValue(selector, "tag"),
                    selectorValue(selector, "type"),
                    parseMaxDistance(selectorValue(selector, "distance")),
                    parseInt(selectorValue(selector, "limit"), Integer.MAX_VALUE),
                    "nearest".equalsIgnoreCase(selectorValue(selector, "sort"))
            );
        }

        private List<Entity> find(Location origin) {
            if (origin == null || origin.getWorld() == null) {
                return List.of();
            }

            World world = origin.getWorld();
            List<Entity> matches = new ArrayList<>();
            for (Entity entity : world.getEntities()) {
                if (tag != null && !entity.getScoreboardTags().contains(tag)) {
                    continue;
                }
                if (type != null && !matchesType(entity, type)) {
                    continue;
                }
                if (maxDistance != null && entity.getLocation().distanceSquared(origin) > maxDistance * maxDistance) {
                    continue;
                }
                matches.add(entity);
            }

            if (nearest) {
                matches.sort((a, b) -> Double.compare(
                        a.getLocation().distanceSquared(origin),
                        b.getLocation().distanceSquared(origin)));
            }

            if (matches.size() > limit) {
                return new ArrayList<>(matches.subList(0, limit));
            }
            return matches;
        }

        private static boolean matchesType(Entity entity, String requested) {
            String normalized = requested.toLowerCase(Locale.ROOT);
            String key = entity.getType().getKey().toString().toLowerCase(Locale.ROOT);
            String simple = entity.getType().getKey().getKey().toLowerCase(Locale.ROOT);
            return normalized.equals(key) || normalized.equals(simple) || normalized.equals("minecraft:" + simple);
        }

        private static String selectorValue(String selector, String key) {
            Pattern pattern = Pattern.compile(String.format(SELECTOR_VALUE_PATTERN, Pattern.quote(key)), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(selector);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        }

        private static Double parseMaxDistance(String value) {
            if (value == null) {
                return null;
            }
            try {
                if (value.startsWith("..")) {
                    return Double.parseDouble(value.substring(2));
                }
                if (!value.contains("..")) {
                    return Double.parseDouble(value);
                }
            } catch (NumberFormatException ignored) {
            }
            return null;
        }

        private static int parseInt(String value, int fallback) {
            if (value == null) {
                return fallback;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                return fallback;
            }
        }
    }
}
