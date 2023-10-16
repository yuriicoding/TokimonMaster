package ca.cmpt213.a2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The Team class represents a group of Tokimons with additional information
 * such as an extra comment and a team index.
 * Each team is composed of an ArrayList of Tokimon objects, each with a name,
 * ID, and compatibility information.
 */

public class Team {
    private ArrayList<Tokimon> team;
    private String extra_comments;
    private int team_index;

    static class Tokimon {
        private String name;
        private String id;
        private Compatibility compatibility;

        @Override
        public String toString() {
            return "Name: " + name + ", ID: " + id + ", Compatibility: " + compatibility;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Compatibility getCompatibility() {
            return compatibility;
        }

        public void setCompatibility(Compatibility compatibility) {
            this.compatibility = compatibility;
        }

    }

    static class Compatibility {
        double score;
        String comment;

        @Override
        public String toString() {
            return "Score: " + score + ", Comment: " + comment;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }

    static void displayTeam(Team team1) {
        List<Team.Tokimon> team_to_show = team1.team;
        for (Team.Tokimon teamMember : team_to_show) {
            System.out.println("Name: " + teamMember.name);
            System.out.println("ID: " + teamMember.id);
            System.out.println("Compatibility: " + teamMember.compatibility);
        }

        // Display extra comments
        System.out.println("Extra Comments: " + team1.extra_comments);

    }

    void sortTokimonsByID() {
        Collections.sort(this.team, new Comparator<Tokimon>() {
            @Override
            public int compare(Tokimon t1, Tokimon t2) {
                // Compare the IDs alphabetically (case-insensitive)
                return t1.id.compareToIgnoreCase(t2.id);
            }
        });
    }

    public void setTeam_index(int team_index) {
        this.team_index = team_index;
    }

    public int getTeam_index() {
        return team_index;
    }

    public static int compareTeamsByID(Team team1, Team team2) {
        List<Tokimon> tokimons1 = team1.team;
        List<Tokimon> tokimons2 = team2.team;

        // Check if the teams have the same number of Tokimons
        if (tokimons1.size() != tokimons2.size()) {
            return 0;
        }

        for (int i = 0; i < tokimons1.size(); i++) {
            String id1 = tokimons1.get(i).id;
            String id2 = tokimons2.get(i).id;

            // Compare the IDs of Tokimons
            if (!id1.equalsIgnoreCase(id2)) {
                return 0;
            }
        }

        // All IDs match
        return 1;
    }

    public ArrayList<Tokimon> getTeam() {
        return team;
    }

    public void setTeam(ArrayList<Tokimon> team) {
        this.team = team;
    }

    public String getExtra_comments() {
        return extra_comments;
    }

    public void setExtra_comments(String extra_comments) {
        this.extra_comments = extra_comments;
    }

}
