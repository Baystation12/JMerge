package JMerge;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Admin
 * on 27.5.2017.
 */
public class MapSaver {
    public static void saveMapToFile(File file, NewMap map) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write key block
            for (String key : map.getTilesByKey().keySet()) {
                writer.write("\"" + key + "\" = " + map.getTilesByKey().get(key) + "\r\n");
            }
            writer.write("\r\n");

            // And then the map.
            writer.write("(1,1,1) = {\"\r\n");
            for (int i = 0; i < map.getMaxY(); i++) {
                for (int j = 0; j < map.getMaxX(); j++) {
                    String contents = map.getTilesByLocation().get(new Location(j, i, 0));
                    writer.write(map.getTilesByContent().get(contents));
                }
                writer.write("\r\n");
            }
            writer.write("\"}");
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
