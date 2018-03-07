package mk.ukim.finki.lab1;

import java.io.*;

/**
 * @author Kostadin Krstev
 */
public class HW01_5 {
    private static final String pathCSV = "/home/krstevkoki/Desktop/rezultati.csv";
    private static final String pathTSV = "/home/krstevkoki/Desktop/rezultati.tsv";

    private static void processFile(File file) throws IOException {
        if (file != null && file.exists() && file.isFile()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
                String line;
                String[] lineParts = null;
                String[] subjects = null;  // used for the names of the subjects
                double[] totalGradesPerSubject = null;  // storing the sum of the grades per subject
                int totalStudents = 0;  // counting how many students we have in the file
                int totalSubjects = 0;  // counting how many subjects we have in the file
                boolean isTSVFile = file.getName().endsWith(".tsv");  // used for correct processing of .tsv files

                System.out.println("===== Students =====");
                while ((line = reader.readLine()) != null && line.length() > 0) {
                    if (lineParts == null) {  // the first line
                        if (!isTSVFile)  // if the file is .csv file
                            lineParts = line.split(",");
                        else
                            lineParts = line.split("\t");

                        totalSubjects = lineParts.length - 1;
                        totalGradesPerSubject = new double[totalSubjects];
                        subjects = new String[totalSubjects];
                        /* copying the names of the subjects */
                        for (int i = 1; i < lineParts.length; ++i)
                            subjects[i - 1] = lineParts[i];
                    } else {  // other lines
                        if (!isTSVFile)  // if the file is .csv file
                            lineParts = line.split(",");
                        else
                            lineParts = line.split("\t");
                        double average = 0d;
                        for (int i = 1; i < lineParts.length; ++i) {
                            totalGradesPerSubject[i - 1] += Double.parseDouble(lineParts[i]);
                            average += Double.parseDouble(lineParts[i]);
                        }
                        if (totalSubjects > 0)
                            average /= totalSubjects;
                        else
                            average = 0d;
                        System.out.println(String.format("Student: %s, average: %.2f", lineParts[0], average));
                        ++totalStudents;
                    }
                }
                if (totalGradesPerSubject != null && totalStudents > 0) {
                    System.out.println("\n===== Subjects =====");
                    for (int i = 0; i < totalGradesPerSubject.length; ++i)
                        System.out.println(String.format("Subject: %s, average: %.2f", subjects[i],
                                totalGradesPerSubject[i] / totalStudents));

                }
            }
        }
    }

    private static void CSVtoTSVConverter(File from, File to) throws IOException {
        if (from != null && to != null && from.exists() && from.isFile()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(from)));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(to)))
            ) {
                String line;
                while ((line = reader.readLine()) != null && line.length() > 0) {
                    writer.write(line.replace(",", "\t"));
                    writer.newLine();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        processFile(new File(pathCSV));
        CSVtoTSVConverter(new File(pathCSV), new File(pathTSV));
        System.out.println();
        processFile(new File(pathTSV));
    }
}
