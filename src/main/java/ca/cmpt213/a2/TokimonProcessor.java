package ca.cmpt213.a2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import com.google.gson.Gson;

/**
 * The TokimonProcessor class is responsible for processing JSON files
 * containing Tokimon data, performing error checks, and generating CSV output.
 */
public class TokimonProcessor {

    /**
     * The main entry point of the program to process JSON files and generate CSV
     * output.
     */
    public static void main(String[] args) {

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("\nEnter directory with JSON input files:");
            String search_folder = scanner.nextLine();
            System.out.println("\nEnter directory to create CSV file in:");
            String output_folder = scanner.nextLine();

            // ./InputTestDataSets/1-OneTeamOneToki
            File rootFolder = new File(search_folder);

            if (!rootFolder.exists() || !rootFolder.isDirectory()) {
                System.err.println("Input folder does not exist or is not a directory.");
                System.exit(-1);
            }

            List<File> jsonFiles = findJsonFiles(rootFolder);

            if (jsonFiles.isEmpty()) {
                System.err.println("No JSON files are found in the specified directory.");
                System.exit(-1);
            }

            List<Team> teams = new ArrayList<Team>();

            for (File jsonFile : jsonFiles) {
                Team processedTokimons = processJsonFile(jsonFile);
                teams.add(processedTokimons);
            }

            for (Team t : teams) {
                t.sortTokimonsByID();
            }

            List<Team> distinctGroups = new ArrayList<>();
            int teamIndexCounter = 1;

            for (Team teamToCheck : teams) {
                boolean foundMatch = false;

                for (Team teamDistinct : distinctGroups) {
                    int result = Team.compareTeamsByID(teamToCheck, teamDistinct);

                    if (result == 1) {
                        teamToCheck.setTeam_index(teamDistinct.getTeam_index());
                        foundMatch = true;
                        break; // No need to continue checking other distinct teams
                    }
                }

                if (!foundMatch) {
                    teamToCheck.setTeam_index(teamIndexCounter);
                    distinctGroups.add(teamToCheck);
                    teamIndexCounter++;
                }
            }

            //Tokimon was mentioned in two different teams
            List<String> all_team_ids = new ArrayList<String>();
            for (Team team_to_check1 : distinctGroups){
                List<Team.Tokimon> members1 = team_to_check1.getTeam();
                for (Team.Tokimon member1 : members1){
                    all_team_ids.add(member1.getId());
                }

            }

            if (hasCaseInsensitiveDuplicates(all_team_ids) == true){
                System.err.println("Some id's are present in multiple teams.");
                System.exit(-1);
            }
            
            //All tokimons submitted JSON file
            List<Integer> team_quantity = new ArrayList<Integer>();
            for (int i = 1; i < teamIndexCounter; i++){
                int team_counter = 0;
                for (Team t : teams){
                    if (t.getTeam_index() == i){
                        team_counter = team_counter+1;
                    }
                }
                team_quantity.add(team_counter);
                team_counter = 0;
            }

            int supposed_files = all_team_ids.size();
            int actual_files = 0;
            for (Integer a : team_quantity){
                actual_files = actual_files + a;
            }

            if (actual_files != supposed_files){
                System.err.println("Not all tokimons provided a JSON file.");
                System.exit(-1);
            }

            //All Tokimons in all teams mentioned their teammates
            for (Team t : teams){
                int members_num = t.getTeam().size();
                int team_index = t.getTeam_index();
                int supposed_size = team_quantity.get(team_index-1);

                if (supposed_size != members_num){
                    System.err.println("Some Tokimons have not mentioned their teammates.");
                    System.exit(-1);
                }

            }


            String outputFilePath = output_folder + "/output.csv";
            CSVGenerator.generateCSV(teams, outputFilePath, teamIndexCounter);
        }
    }

    /**
     * Recursively find JSON files within the specified directory and its
     * subdirectories.
     *
     * @param folder The root directory to search for JSON files.
     * @return A list of JSON files found within the specified directory and
     *         subdirectories.
     */

    static List<File> findJsonFiles(File folder) {
        List<File> jsonFiles = new ArrayList<>();

        if (folder.isDirectory()) {
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        // Recursively search in subdirectories
                        jsonFiles.addAll(findJsonFiles(file));
                    } else if (file.isFile() && file.getName().endsWith(".json")) {
                        jsonFiles.add(file);
                    }
                }
            }
        }

        return jsonFiles;
    }

    
    /** 
     * @return Team
     */
    // Experimental version of processJsonFile
    // static Team processJsonFile(File jsonFile) {
    // try {
    // // Read and process the JSON file
    // BufferedReader reader = new BufferedReader(new FileReader(jsonFile));
    // StringBuilder jsonContent = new StringBuilder();
    // String line;
    // while ((line = reader.readLine()) != null) {
    // jsonContent.append(line).append("\n");
    // }
    // reader.close();

    // // Parse the JSON string using Gson into a Tokimon object
    // Team retrivedTokimons = sortTokimons(jsonContent);
    // return retrivedTokimons;

    // } catch (IOException e) {
    // e.printStackTrace();
    // return null;
    // }
    // }

    /**
     * Process a JSON file, read its content, and convert it into a Tokimon object.
     * It also performs error checking and handles errors.
     *
     * @param jsonFile The JSON file to be processed.
     * @return A Tokimon object representing the parsed JSON data.
     */
    static Team processJsonFile(File jsonFile) {
        try {
            // Read and process the JSON file
            BufferedReader reader = new BufferedReader(new FileReader(jsonFile));
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line).append("\n");
            }
            reader.close();

            // Parse the JSON string using Gson into a Tokimon object
            Team retrivedTokimons = sortTokimons(jsonContent);

            if (retrivedTokimons == null) {
                CSVGenerator.handleErrors("Bad JSON file format or missing required fields", jsonFile.getPath());
            }

            // Check for other errors and handle them accordingly
            if (retrivedTokimonsContainsErrors(retrivedTokimons)) {
                CSVGenerator.handleErrors("Errors in the input data", jsonFile.getPath());
            }

            return retrivedTokimons;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if a Tokimon object contains any errors.
     *
     * @param team The Tokimon object to check for errors.
     * @return True if the Tokimon object contains errors; otherwise, false.
     */
    static boolean retrivedTokimonsContainsErrors(Team team) {
        boolean hasErrors = false;
        if (team == null) {
            hasErrors = true;
        } else {
            for (Team.Tokimon tokimon : team.getTeam()) {
                if (tokimon.getCompatibility().getScore() < 0) {
                    hasErrors = true;
                    break;
                }
            }
        }
        return hasErrors;
    }

    /**
     * Convert a JSON string to a Tokimon object using Gson.
     *
     * @param source The JSON data as a StringBuilder.
     * @return A Tokimon object representing the parsed JSON data.
     */
    static Team sortTokimons(StringBuilder source) {
        Gson gson = new Gson();
        Team team1 = gson.fromJson(source.toString(), Team.class);
        return (team1);
    }

    
    /** 
     * @param strings
     * @return boolean
     */
    public static boolean hasCaseInsensitiveDuplicates(List<String> strings) {
        Set<String> uniqueStrings = new HashSet<>();
        
        for (String str : strings) {
            // Convert the string to lowercase to make the comparison case-insensitive
            String lowercaseStr = str.toLowerCase();
            
            // If the lowercase string is already in the set, there's a duplicate
            if (uniqueStrings.contains(lowercaseStr)) {
                return true;
            }
            
            // Otherwise, add it to the set
            uniqueStrings.add(lowercaseStr);
        }
        
        // No duplicates found
        return false;
    }

}
