package com.crispytwig.bbb.platform.services;

import com.crispytwig.bbb.common.config.BlockGroup;

import java.util.Set;

public interface IConfigHelper {
    boolean cherryWoodSounds();

    boolean prismarineDeepslateSounds();

    Set<BlockGroup> disabledGroups();

    boolean needsRestart();
}
