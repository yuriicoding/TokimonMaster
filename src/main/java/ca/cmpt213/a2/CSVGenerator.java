package ca.cmpt213.a2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The CSVGenerator class provides methods for generating CSV output from a list
 * of teams and handling errors in the process.
 */
public class CSVGenerator {

    /**
     * Generate a CSV file with team information and tokimon data.
     *
     * @param teams          The list of teams to be included in the CSV file.
     * @param outputFilePath The path where the CSV file will be saved.
     * @param numberOfTeams  The number of teams in the list.
     */
    public static void generateCSV(List<Team> teams, String outputFilePath, int number_of_teams) {
        try (FileWriter writer = new FileWriter(outputFilePath)) {

            writer.append("Team#,From Toki,To Toki,Score,Comment,Extra comments");
            writer.append("\n");

            for (int i = 1; i < number_of_teams; i++) {
                writer.append("\nTeam " + i + ",,,,,\n");

                for (Team team : teams) {
                    int team_index_val = team.getTeam_index();
                    if (team_index_val == i) {
                        ArrayList<Team.Tokimon> teamMembers = team.getTeam();

                        String comment_from = teamMembers.get(0).getCompatibility().getComment();
                        String[] parts1 = comment_from.split("'");
                        String name_from = parts1[1];

                        String from_id = "not found";
                        for (Team.Tokimon member_from : teamMembers) {
                            String name_to_check = member_from.getName();
                            if (name_to_check.equals(name_from)) {
                                from_id = member_from.getId();
                            }
                        }

                        for (Team.Tokimon member : teamMembers) {
                            String comment = member.getCompatibility().getComment();
                            comment = comment.replaceAll("\n", " ");

                            String[] parts = comment.split("'");
                            String name_to = parts[3];

                            String to_id = "not_found";
                            for (Team.Tokimon member_to : teamMembers) {
                                String name_to_check = member_to.getName();
                                if (name_to_check.equals(name_to)) {
                                    to_id = member_to.getId();
                                }
                            }

                            String score_val = String.valueOf(member.getCompatibility().getScore());

                            writer.append("\n," + from_id + "," + to_id + "," + score_val + "," + comment + ",");
                        }

                        String extra = team.getExtra_comments();
                        writer.append(extra);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle errors by displaying an error message, the filename of the problematic
     * JSON file, and exit the program with code -1.
     *
     * @param errorMessage The error message to be displayed.
     * @param jsonFilePath The path of the problematic JSON file (optional, can be
     *                     null).
     */
    public static void handleErrors(String errorMessage, String jsonFilePath) {
        System.err.println(errorMessage);
        if (jsonFilePath != null) {
            System.err.println("Problematic JSON file: " + jsonFilePath);
        }
        System.exit(-1);
    }
}
