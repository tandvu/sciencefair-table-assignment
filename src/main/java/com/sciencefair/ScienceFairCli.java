package com.sciencefair;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.FileNameCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScienceFairCli {
    public static void main(String[] args) throws Exception {
        Terminal terminal = TerminalBuilder.builder().system(true).build();
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(new FileNameCompleter())
                .build();

        String tableFile = reader.readLine("Enter table input file: ");
        File table = new File(tableFile);
        if (!table.exists()) {
            System.out.println("File not found: " + tableFile);
            return;
        }

        String projectFile = reader.readLine("Enter project input file: ");
        File project = new File(projectFile);
        if (!project.exists()) {
            System.out.println("File not found: " + projectFile);
            return;
        }

    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String outDirName = "ScienceFairOutput_" + timestamp;
    File outDir = new File(outDirName);
    outDir.mkdir();

        String csvOut = outDirName + File.separator + "output.csv";
        String htmlOut = outDirName + File.separator + "output.html";

        ScienceFairTableAssignmentApp.runCommandLine(tableFile, projectFile, csvOut);
        ScienceFairTableAssignmentApp.generateHtmlLayoutFromCsv(csvOut, htmlOut);

        System.out.println("Output files created in " + outDirName + ":");
        System.out.println("- " + csvOut);
        System.out.println("- " + htmlOut);
    }
}
