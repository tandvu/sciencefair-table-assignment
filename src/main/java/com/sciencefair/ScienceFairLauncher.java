package com.sciencefair;

public class ScienceFairLauncher {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar science-fair-table-assignment.jar [--cli | --gui | --html | --batch]");
            System.out.println("  --cli   : Run in command-line mode");
            System.out.println("  --gui   : Run Assignment GUI (select two input files, produce output folder)");
            System.out.println("  --html  : Run HTML Generator GUI (select output.csv, produce output_from_csv_conversion.html)");
            System.out.println("  --batch : Run in batch mode (CSV with two tabs)");
            return;
        }
        try {
            switch (args[0]) {
                case "--cli":
                    ScienceFairCli.main(new String[]{});
                    break;
                case "--gui":
                    com.sciencefair.gui.ScienceFairAssignmentGui.main(new String[]{});
                    break;
                case "--html":
                    com.sciencefair.gui.HtmlGeneratorGui.main(new String[]{});
                    break;
                case "--batch":
                    // TODO: Implement batch mode logic here
                    System.out.println("Batch mode not yet implemented.");
                    break;
                default:
                    System.out.println("Unknown option: " + args[0]);
                    System.out.println("Usage: java -jar science-fair-table-assignment.jar [--cli | --gui | --html | --batch]");
            }
        } catch (Exception e) {
            System.err.println("Error running mode: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
