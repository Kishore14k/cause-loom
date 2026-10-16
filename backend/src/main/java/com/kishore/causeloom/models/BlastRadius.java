package com.kishore.causeloom.models;

import java.util.List;

public record BlastRadius(String origin,
                          List<AffectedService> affected,
                          List<AffectedService> hiddenVictims) {}