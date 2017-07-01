package JMerge;

import javax.naming.OperationNotSupportedException;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Admin
 * on 26.5.2017.
 */
public class NewMap implements Serializable {

    private int maxX = 0;
    private int maxY = 0;

    private int keyLength = -1;
    private int keyGeneratorCurrentId = 0;
    private static final String[] validKeyElements = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };


    private Map<String, String> tilesByKey;
    private Map<String, String> tilesByContent;
    private Map<Location, String> tilesByLocation;

    // If a File is passed JMerge will try to load and parse it. No changes will be made to the origin file.
    // Pass null to skip file loading.
    public NewMap(File mapFile) {
        tilesByKey = new TreeMap<>(new KeyComparator());
        tilesByContent = new HashMap<>();
        tilesByLocation = new HashMap<>();

        if(mapFile == null){
            return;
        }
        loadFromFile(mapFile);
    }

    public void loadFromFile(File mapFile){
        try{
            if(!mapFile.exists()){
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(mapFile)));
            loadKeys(reader);
            loadMap(reader);
            reader.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadKeys(BufferedReader reader) throws IOException {
        try {
            String loadedRow;
            while ((loadedRow = reader.readLine()) != null) {
                if (loadedRow.startsWith("\"")) {
                    if (keyLength == -1) {
                        setKeyLength(loadedRow.indexOf("\"", 1) - 1);
                    }
                    String key = loadedRow.substring(1, 1 + keyLength);
                    String value = loadedRow.substring(loadedRow.indexOf("("));
                    tilesByKey.put(key, value);
                    tilesByContent.put(value, key);
                } else {
                    break;
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void loadMap(BufferedReader reader) throws IOException, OperationNotSupportedException {
        String loadedRow;
        for(int i = 0; (loadedRow = reader.readLine()) != null ; i++){
            if(loadedRow.isEmpty() || loadedRow.startsWith("\"}")){
                break;
            }
            if (loadedRow.startsWith("(")) {
                // Skip the first line containing map start points.
                // I do not know about a map that would have different values than (1, 1, 1).
                // If you encounter such map, contact me and i'll add support for it.
                if(!loadedRow.startsWith("(1,1,1)")){
                    throw new OperationNotSupportedException("Map start points are different than 1,1,1. Actual contents of this line are: " + loadedRow);
                }
                i--;
                continue;
            }
            for(int j = 0; loadedRow.length() > 0; j++){
                try {
                    String key = loadedRow.substring(0, keyLength);
                    loadedRow = loadedRow.substring(keyLength);
                    Location location = new Location(j, i, 0);
                    tilesByLocation.put(location, tilesByKey.get(key));
                    maxX = Math.max(maxX, j + 1);
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
            maxY = Math.max(maxY, i+1);
        }
    }

    // Pass an original map instance to attempt to minimize key/row changes when possible.
    // Works with assumption that 'this' is the new map and 'otherMap' is the original map.
    public void cleanupMap(NewMap otherMap){
        if(otherMap.getKeyLength() != getKeyLength()){
            throw new RuntimeException("Key length of original and new map differs.");
        }

        Map<String, String> newTilesByKey = new TreeMap<>(new KeyComparator());
        Map<String, String> newTilesByContent = new HashMap<>();

        // First pass - try to reuse keys from original map. This is what minimizes the diff size.
        for (int i = 0; i < maxY; i++) {
            for (int j = 0; j < maxX; j++) {
                String contents = tilesByLocation.get(new Location(j, i, 0));
                if((newTilesByContent.get(contents) == null) && (otherMap.getTilesByContent().get(contents) != null)){
                    newTilesByContent.put(contents, otherMap.getTilesByContent().get(contents));
                    newTilesByKey.put(otherMap.getTilesByContent().get(contents), contents);
                }
            }
        }
        // Second pass - fill the remaining tiles with generated keys.
        for (int i = 0; i < maxY; i++) {
            for (int j = 0; j < maxX; j++) {
                String contents = tilesByLocation.get(new Location(j, i, 0));
                if(newTilesByContent.get(contents) == null){
                    String generatedKey = generateNewKey();
                    newTilesByContent.put(contents, generatedKey);
                    newTilesByKey.put(generatedKey, contents);
                }
            }
        }
        tilesByKey = newTilesByKey;
        tilesByContent = newTilesByContent;
    }

    // Attempts to generate a new, unused key. Relatively CPU intensive and therefore used only as last-resort method.
    private String generateNewKey() {
        while (true) {
            int localId = keyGeneratorCurrentId++;
            String generatedKey = "";
            while (localId >= validKeyElements.length) {
                int i = localId % validKeyElements.length;
                generatedKey = generatedKey + validKeyElements[i];
                localId -= i;
                localId /= validKeyElements.length;
            }
            generatedKey = generatedKey + validKeyElements[localId];
            if (tilesByKey.containsKey(generatedKey)) {
                continue;
            }
            if (generatedKey.length() == keyLength) {
                return generatedKey;
            } else {
                throw new RuntimeException("Generated key is outside of bounds.");
            }
        }
    }

    public void setKeyLength(int keyLength) {
        this.keyLength = keyLength;
        keyGeneratorCurrentId = (int) Math.pow(validKeyElements.length, keyLength - 1);
    }


    // Generated code below
    @Override
    public String toString() {
        return "NewMap{" +
                "maxX=" + maxX +
                ", maxY=" + maxY +
                ", keyLength=" + keyLength +
                ", tilesByKey=" + tilesByKey +
                ", tilesByContent=" + tilesByContent +
                ", tilesByLocation=" + tilesByLocation +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NewMap newMap = (NewMap) o;

        if (maxX != newMap.maxX) return false;
        if (maxY != newMap.maxY) return false;
        if (keyLength != newMap.keyLength) return false;
        if (tilesByKey != null ? !tilesByKey.equals(newMap.tilesByKey) : newMap.tilesByKey != null) return false;
        if (tilesByContent != null ? !tilesByContent.equals(newMap.tilesByContent) : newMap.tilesByContent != null)
            return false;
        return tilesByLocation != null ? tilesByLocation.equals(newMap.tilesByLocation) : newMap.tilesByLocation == null;
    }

    @Override
    public int hashCode() {
        int result = maxX;
        result = 31 * result + maxY;
        result = 31 * result + keyLength;
        result = 31 * result + (tilesByKey != null ? tilesByKey.hashCode() : 0);
        result = 31 * result + (tilesByContent != null ? tilesByContent.hashCode() : 0);
        result = 31 * result + (tilesByLocation != null ? tilesByLocation.hashCode() : 0);
        return result;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public Map<String, String> getTilesByKey() {
        return tilesByKey;
    }

    public void setTilesByKey(Map<String, String> tilesByKey) {
        this.tilesByKey = tilesByKey;
    }

    public Map<String, String> getTilesByContent() {
        return tilesByContent;
    }

    public void setTilesByContent(Map<String, String> tilesByContent) {
        this.tilesByContent = tilesByContent;
    }

    public Map<Location, String> getTilesByLocation() {
        return tilesByLocation;
    }

    public void setTilesByLocation(Map<Location, String> tilesByLocation) {
        this.tilesByLocation = tilesByLocation;
    }
}
