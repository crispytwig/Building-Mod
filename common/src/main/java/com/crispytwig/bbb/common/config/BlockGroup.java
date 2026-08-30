package com.crispytwig.bbb.common.config;

public enum BlockGroup {
    BOARDS("boards", "Boards"),
    POLISHED("polished", "Polished Wood"),
    TRIM("trim", "Trim"),
    PILLARS("pillars", "Pillars"),
    BEAMS("beams", "Beams"),
    TABLES("tables", "Tables"),
    CHAIRS("chairs", "Chairs"),
    FRAMES("frames", "Frames"),
    TIMBER_FRAMES("timber_frames", "Timber Frames"),
    SHUTTERS("shutters", "Shutters"),
    LANTERNS("lanterns", "Lanterns"),
    WINDOWS("windows", "Windows"),
    COLORED_WOOD("colored_planks", "Coloured Wood"),
    STONE_TILES("stone_tiles", "Stone Tiles"),
    PRISMARINE_TILES("prismarine_tiles", "Prismarine Tiles"),
    TERRACOTTA("terracotta", "Terracotta"),
    PLASTER("plaster", "Plaster"),
    SOFAS("sofas", "Sofas"),
    CURTAINS("curtains", "Curtains"),
    LAYERS("layers", "Layers");

    private final String key;
    private final String title;

    BlockGroup(String key, String title) {
        this.key = key;
        this.title = title;
    }

    public String key() {
        return key;
    }

    public String title() {
        return title;
    }
}
