package net.minecraft.world;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldType
{
    public static WorldType[] WORLD_TYPES = new WorldType[16];
    public static final WorldType DEFAULT = (new WorldType(0, "default", 1)).setVersioned();
    public static final WorldType FLAT = new WorldType(1, "flat");
    public static final WorldType LARGE_BIOMES = new WorldType(2, "largeBiomes");
    public static final WorldType AMPLIFIED = (new WorldType(3, "amplified")).enableInfoNotice();
    public static final WorldType CUSTOMIZED = new WorldType(4, "customized");
    public static final WorldType DEBUG_ALL_BLOCK_STATES = new WorldType(5, "debug_all_block_states");
    public static final WorldType DEFAULT_1_1 = (new WorldType(8, "default_1_1", 0)).setCanBeCreated(false);
    private final int id;
    private final String name;
    private final int version;
    private boolean canBeCreated;
    private boolean versioned;
    private boolean hasInfoNotice;

    private WorldType(int id, String name)
    {
        this(id, name, 0);
    }

    private WorldType(int id, String name, int version)
    {
        if (name.length() > 16 && DEBUG_ALL_BLOCK_STATES != null) throw new IllegalArgumentException("World type names must not be longer then 16: " + name);
        this.name = name;
        this.version = version;
        this.canBeCreated = true;
        this.id = id;
        WORLD_TYPES[id] = this;
    }

    public String getName()
    {
        return this.name;
    }

    @SideOnly(Side.CLIENT)
    public String getTranslationKey()
    {
        return "generator." + this.name;
    }

    @SideOnly(Side.CLIENT)
    public String getInfoTranslationKey()
    {
        return this.getTranslationKey() + ".info";
    }

    public int getVersion()
    {
        return this.version;
    }

    public WorldType getWorldTypeForGeneratorVersion(int version)
    {
        return this == DEFAULT && version == 0 ? DEFAULT_1_1 : this;
    }

    private WorldType setCanBeCreated(boolean enable)
    {
        this.canBeCreated = enable;
        return this;
    }

    @SideOnly(Side.CLIENT)
    public boolean canBeCreated()
    {
        return this.canBeCreated;
    }

    private WorldType setVersioned()
    {
        this.versioned = true;
        return this;
    }

    public boolean isVersioned()
    {
        return this.versioned;
    }

    public static WorldType byName(String type)
    {
        for (WorldType worldtype : WORLD_TYPES)
        {
            if (worldtype != null && worldtype.name.equalsIgnoreCase(type))
            {
                return worldtype;
            }
        }

        return null;
    }

    public int getId()
    {
        return this.id;
    }

    @SideOnly(Side.CLIENT)
    public boolean hasInfoNotice()
    {
        return this.hasInfoNotice;
    }

    private WorldType enableInfoNotice()
    {
        this.hasInfoNotice = true;
        return this;
    }

    public net.minecraft.world.biome.BiomeProvider getBiomeProvider(World world)
    {
        if (this == FLAT)
        {
            net.minecraft.world.gen.FlatGeneratorInfo flatgeneratorinfo = net.minecraft.world.gen.FlatGeneratorInfo.createFlatGeneratorFromString(world.getWorldInfo().getGeneratorOptions());
            return new net.minecraft.world.biome.BiomeProviderSingle(net.minecraft.world.biome.Biome.getBiome(flatgeneratorinfo.getBiome(), net.minecraft.init.Biomes.DEFAULT));
        }
        else if (this == DEBUG_ALL_BLOCK_STATES)
        {
            return new net.minecraft.world.biome.BiomeProviderSingle(net.minecraft.init.Biomes.PLAINS);
        }
        else
        {
            return new net.minecraft.world.biome.BiomeProvider(world.getWorldInfo());
        }
    }

    public net.minecraft.world.gen.IChunkGenerator getChunkGenerator(World world, String generatorOptions)
    {
        if (this == FLAT) return new net.minecraft.world.gen.ChunkGeneratorFlat(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), generatorOptions);
        if (this == DEBUG_ALL_BLOCK_STATES) return new net.minecraft.world.gen.ChunkGeneratorDebug(world);
        if (this == CUSTOMIZED) return new net.minecraft.world.gen.ChunkGeneratorOverworld(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), generatorOptions);
        return new net.minecraft.world.gen.ChunkGeneratorOverworld(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), generatorOptions);
    }

    public int getMinimumSpawnHeight(World world)
    {
        return this == FLAT ? 4 : world.getSeaLevel() + 1;
    }

    public double getHorizon(World world)
    {
        return this == FLAT ? 0.0D : 63.0D;
    }

    public double voidFadeMagnitude()
    {
        return this == FLAT ? 1.0D : 0.03125D;
    }

    public boolean handleSlimeSpawnReduction(java.util.Random random, World world)
    {
        return this == FLAT ? random.nextInt(4) != 1 : false;
    }

    /*=================================================== FORGE START ======================================*/
    private static int getNextID()
    {
        for (int x = 0; x < WORLD_TYPES.length; x++)
        {
            if (WORLD_TYPES[x] == null)
            {
                return x;
            }
        }

        int oldLen = WORLD_TYPES.length;
        WORLD_TYPES = java.util.Arrays.copyOf(WORLD_TYPES, oldLen + 16);
        return oldLen;
    }

    /**
     * Creates a new world type, the ID is hidden and should not be referenced by modders.
     * It will automatically expand the underlying workdType array if there are no IDs left.
     * @param name
     */
    public WorldType(String name)
    {
        this(getNextID(), name);
    }

    /**
     * Called when 'Create New World' button is pressed before starting game
     */
    public void onGUICreateWorldPress() { }

    /**
     * Gets the spawn fuzz for players who join the world.
     * Useful for void world types.
     * @return Fuzz for entity initial spawn in blocks.
     */
    public int getSpawnFuzz(WorldServer world, net.minecraft.server.MinecraftServer server)
    {
        return Math.max(0, server.getSpawnRadius(world));
    }

    /**
     * Called when the 'Customize' button is pressed on world creation GUI
     * @param mc The Minecraft instance
     * @param guiCreateWorld the createworld GUI
     */
    @SideOnly(Side.CLIENT)
    public void onCustomizeButton(net.minecraft.client.Minecraft mc, net.minecraft.client.gui.GuiCreateWorld guiCreateWorld)
    {
        if (this == WorldType.FLAT)
        {
            mc.displayGuiScreen(new net.minecraft.client.gui.GuiCreateFlatWorld(guiCreateWorld, guiCreateWorld.chunkProviderSettingsJson));
        }
        else if (this == WorldType.CUSTOMIZED)
        {
            mc.displayGuiScreen(new net.minecraft.client.gui.GuiCustomizeWorldScreen(guiCreateWorld, guiCreateWorld.chunkProviderSettingsJson));
        }
    }

    /**
     * Should world creation GUI show 'Customize' button for this world type?
     * @return if this world type has customization parameters
     */
    public boolean isCustomizable()
    {
        return this == FLAT || this == WorldType.CUSTOMIZED;
    }


    /**
     * Get the height to render the clouds for this world type
     * @return The height to render clouds at
     */
    public float getCloudHeight()
    {
        return 128.0F;
    }

    /**
     * Creates the GenLayerBiome used for generating the world with the specified ChunkProviderSettings JSON String
     * *IF AND ONLY IF* this WorldType == WorldType.CUSTOMIZED.
     *
     *
     * @param worldSeed The world seed
     * @param parentLayer The parent layer to feed into any layer you return
     * @param chunkSettings The ChunkGeneratorSettings constructed from the custom JSON
     * @return A GenLayer that will return ints representing the Biomes to be generated, see GenLayerBiome
     */
    public net.minecraft.world.gen.layer.GenLayer getBiomeLayer(long worldSeed, net.minecraft.world.gen.layer.GenLayer parentLayer, net.minecraft.world.gen.ChunkGeneratorSettings chunkSettings)
    {
        net.minecraft.world.gen.layer.GenLayer ret = new net.minecraft.world.gen.layer.GenLayerBiome(200L, parentLayer, this, chunkSettings);
        ret = net.minecraft.world.gen.layer.GenLayerZoom.magnify(1000L, ret, 2);
        ret = new net.minecraft.world.gen.layer.GenLayerBiomeEdge(1000L, ret);
        return ret;
    }
}
