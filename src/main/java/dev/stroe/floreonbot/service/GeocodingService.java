package dev.stroe.floreonbot.service;

import java.io.IOException;
import dev.stroe.floreonbot.model.Location;

public interface GeocodingService {
    Location getCoordinates(String locationName) throws IOException, InterruptedException;
}