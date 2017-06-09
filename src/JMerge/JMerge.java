package JMerge;

import java.io.File;
import java.io.IOException;

/**
 * Created by Admin
 * on 27.5.2017.
 */
public class JMerge {
    public static void main(String[] params){
//        params = new String[4];
//        params[0] = "-clean";
//        params[1] = "C:\\BS12CODE\\maps\\torch\\torch-1.dmm.backup";
//        params[2] = "C:\\BS12CODE\\maps\\torch\\torch-1.dmm";
//        params[3] = "C:\\BS12CODE\\maps\\torch\\torch-1.dmm";

        if(params.length == 0){
            System.exit(about());
        }

        System.out.println("JMerge 1.0 Experimental");
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
        System.out.println("JMerge v1.0");
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
            MapSaver.saveMapToFile(new File(params[2]), newMap);
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
        return 1; // TODO
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
        for(int i = 0; i <= originMap.getMaxY(); i++) {
            for(int j = 0; j <= originMap.getMaxX(); j++) {
                Location location = new Location(j, i, 0);
                String locOrigin = originMap.getTilesByLocation().get(location);
                String locLocal = localMap.getTilesByLocation().get(location);
                String locRemote = remoteMap.getTilesByLocation().get(location);

                boolean originMatchesLocal = (locOrigin == null) ? (locLocal == null) : locOrigin.equals(locLocal);
                boolean originMatchesRemote = (locOrigin == null) ? (locRemote == null) : locOrigin.equals(locRemote);
                boolean remoteMatchesLocal = (locRemote == null) ? (locLocal == null) : locOrigin.equals(locLocal);

                if(!originMatchesLocal && !originMatchesRemote && !remoteMatchesLocal) {
                    conflictEncountered = true;
                    System.out.println("CONFLICT: [" + j + ", " + i + ", 0]");
                    conflictResolveMode = getResolveMode(conflictResolveMode);
                    switch(conflictResolveMode){
                        case 1:
                            resultMap.getTilesByLocation().put(location, localMap.getTilesByLocation().get(location));
                        case 2:
                            resultMap.getTilesByLocation().put(location, remoteMap.getTilesByLocation().get(location));
                        case 3:
                            resultMap.getTilesByLocation().put(location, originMap.getTilesByLocation().get(location));
                    }
                } else if (!originMatchesLocal) {
                    resultMap.getTilesByLocation().put(location, localMap.getTilesByLocation().get(location));
                } else if (!originMatchesRemote) {
                    resultMap.getTilesByLocation().put(location, remoteMap.getTilesByLocation().get(location));
                } else {
                    resultMap.getTilesByLocation().put(location, originMap.getTilesByLocation().get(location));
                }
            }
        }
        File file = new File(params[4]);
        if(conflictEncountered){
            System.out.println("WARN: Merge conlicts encountered. See above logs for more information. Map may require manual verification.");
        }
        try {
            MapSaver.saveMapToFile(file, resultMap);
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
        return conflictEncountered ? 1 : 0;
    }

    // 1: Local, 2:Remote, 3:Origin
    private static int getResolveMode(int currentMode){
        if(currentMode != 0){
            return currentMode;
        }
        while(true){
            System.out.println("A conflict has been detected. Please specify which version should be used (local/remote/origin/abort):");
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
