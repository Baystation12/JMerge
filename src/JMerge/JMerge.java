package JMerge;

import java.io.File;
import java.io.IOException;

/**
 * Created by Admin
 * on 27.5.2017.
 */
public class JMerge {
    public static void main(String[] params){
        // FOR USE ONLY DURING DEBUGGING - replace with your map file paths.
//        params = new String[4];
//        params[0] = "-clean";
//        params[1] = "C:/temporary/torch-2.dmm.backup";
//        params[2] = "C:/temporary/torch-2.dmm";
//        params[3] = "C:/temporary/torch-2-cleaned.dmm";

        if(params.length == 0){
            System.exit(about());
        }

        System.out.println("JMerge 1.4");
        System.out.println("Running with parameters:");
        for(int i = 0; i < params.length; i++){
            System.out.println(params[i]);
        }

        switch(params[0]){
            case "-merge":
                System.exit(merge(params));
            case "-clean":
                System.exit(clean(params));
            default:
                System.exit(about());
        }
    }

    private static int about(){
        System.out.println("JMerge v1.4");
        System.out.println("The following operators may be used:");
        System.out.println("-merge : Attemts to merge two maps which originate from the same map, but have different changes.");
        System.out.println("-clean : Cleans a map after changes have been made, usually greatly reducing diff size.");
        return 1;
    }

    private static int clean(String[] params){
        NewMap originMap = new NewMap(new File(params[1]));
        NewMap newMap = new NewMap(new File(params[2]));
        newMap.cleanupMap(originMap);
        try {
            MapSaver.saveMapToFile(new File(params[3]), newMap);
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
        System.out.println("Successfully cleaned map. Exiting.");
        return 0;
    }

    private static int merge(String[] params){
        NewMap originMap = new NewMap(new File(params[1]));
        NewMap localMap = new NewMap(new File(params[2]));
        NewMap remoteMap = new NewMap(new File(params[3]));
        NewMap resultMap = new NewMap(new File(params[4]));
        boolean conflictEncountered = false;
        int conflictResolveMode = 0;

        if((originMap.getMaxX() != localMap.getMaxX()) || (originMap.getMaxX() != remoteMap.getMaxX()) || (localMap.getMaxX() != remoteMap.getMaxX())) {
            System.out.println("ERROR: Map sizes differ. Unable to merge. Aborting.");
            return 1;
        }
        if((originMap.getKeyLength() != localMap.getKeyLength()) || (originMap.getKeyLength() != remoteMap.getKeyLength()) || (localMap.getKeyLength() != remoteMap.getKeyLength())) {
            System.out.println("ERROR: Map key lengths differ. Unable to merge. Aborting.");
            return 1;
        }
        resultMap.setMaxX(originMap.getMaxX());
        resultMap.setMaxY(originMap.getMaxY());
        resultMap.setKeyLength(originMap.getKeyLength());
        for(int i = 0; i <= originMap.getMaxY()-1; i++) {
            for(int j = 0; j <= originMap.getMaxX()-1; j++) {
                Location location = new Location(j, i, 0);
                String locOrigin = originMap.getTilesByLocation().get(location);
                String locLocal = localMap.getTilesByLocation().get(location);
                String locRemote = remoteMap.getTilesByLocation().get(location);

                boolean originMatchesLocal = (locOrigin == null) ? (locLocal == null) : locOrigin.equals(locLocal);
                boolean originMatchesRemote = (locOrigin == null) ? (locRemote == null) : locOrigin.equals(locRemote);
                boolean remoteMatchesLocal = (locRemote == null) ? (locLocal == null) : locOrigin.equals(locLocal);

                String key, contents;
                if(!originMatchesLocal && !originMatchesRemote && !remoteMatchesLocal) {
                    conflictEncountered = true;
                    System.out.println("CONFLICT: [" + j + ", " + i + ", 0]");
                    conflictResolveMode = getResolveMode(conflictResolveMode);
                    switch(conflictResolveMode){
                        case 1:
                            contents = locLocal;
                            key = localMap.getTilesByContent().get(contents);
                            break;
                        case 2:
                            contents = locRemote;
                            key = remoteMap.getTilesByContent().get(contents);
                            break;
                        case 3:
                            contents = locOrigin;
                            key = originMap.getTilesByContent().get(contents);
                            break;
                        default:
                            throw new RuntimeException("Incorrect conflict resolution mode! Aborting operation.");
                    }
                } else if (!originMatchesLocal) {
                    contents = locLocal;
                    key = localMap.getTilesByContent().get(contents);
                } else if (!originMatchesRemote) {
                    contents = locRemote;
                    key = remoteMap.getTilesByContent().get(contents);
                } else {
                    contents = locOrigin;
                    key = originMap.getTilesByContent().get(contents);
                }
                resultMap.getTilesByLocation().put(location, contents);
                resultMap.getTilesByKey().put(key, contents);
                resultMap.getTilesByContent().put(contents, key);
            }
        }
        resultMap.cleanupMap(originMap);
        File file = new File(params[4]);
        try {
            MapSaver.saveMapToFile(file, resultMap);
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
        if(conflictEncountered){
            System.out.println("WARN: Merge conlicts encountered. See above logs for more information. Map may require manual verification.");
            return 1;
        }
        System.out.println("Successfully merged maps. Exiting.");
        return 0;
    }

    // 1: Local, 2:Remote, 3:Origin
    private static int getResolveMode(int currentMode){
        if(currentMode != 0){
            return currentMode;
        }
        while(true){
            System.out.println("A conflict has been detected. Please specify which version should be used (local/remote/origin/abort):");
            String input = System.console().readLine();
            if((input == null) || input.isEmpty()) {
                continue;
            }
            switch(System.console().readLine()){
                case "local":
                    return 1;
                case "remote":
                    return 2;
                case "origin":
                    return 3;
                case "abort":
                    System.exit(1);
            }
        }
    }
}
