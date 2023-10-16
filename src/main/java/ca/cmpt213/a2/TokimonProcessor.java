package ca.cmpt213.a2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
            System.out.println("\nEnter directory with JSON input files: \n");
            String search_folder = scanner.nextLine();
            System.out.println("\nEnter directory to create CSV file in: \n");
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
                Team.displayTeam(t);
                System.out.println();
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

            for (Team t : teams) {
                System.out.println(t.getTeam_index());
                Team.displayTeam(t);
                System.out.println();
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

}
