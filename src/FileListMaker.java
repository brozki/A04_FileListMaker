import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * A04_FileListMaker
 * Refactor of Lab 10 ListMaker: adds Open (O) and Save (S) so lists can be
 * loaded from and saved to .txt files.
 *
 * Menu: A - Add, D - Delete, I - Insert, P - Print, O - Open, S - Save, Q - Quit
 *
 * Structure: outer do-while loop + switch in main(); every menu option is a method.
 * Exceptions: the file methods declare "throws IOException"; main() holds the
 * central try/catch that handles them.
 */
public class FileListMaker {

    private static final Scanner in = new Scanner(System.in);
    private static ArrayList<String> list = new ArrayList<>();
    private static String currentFile = null;   // file the list was opened from / last saved to
    private static boolean needsToBeSaved = false;

    public static void main(String[] args) {
        boolean done = false;

        do {
            displayList();
            String choice = getMenuChoice();

            try {
                switch (choice) {
                    case "A":
                        addItem();
                        break;
                    case "D":
                        deleteItem();
                        break;
                    case "I":
                        insertItem();
                        break;
                    case "P":
                        printList();
                        break;
                    case "O":
                        openListFile();   // throws IOException
                        break;
                    case "S":
                        saveListFile();   // throws IOException
                        break;
                    case "Q":
                        done = quit();    // may call saveListFile(), so may throw IOException
                        break;
                    default:
                        System.out.println("Invalid choice. Please pick a letter from the menu.");
                }
            } catch (IOException e) {
                System.out.println("File error: " + e.getMessage());
            }
        } while (!done);

        System.out.println("Goodbye!");
    }

    // ---------- Menu ----------

    private static String getMenuChoice() {
        System.out.println();
        System.out.println("A - Add an item to the list");
        System.out.println("D - Delete an item from the list");
        System.out.println("I - Insert an item into the list");
        System.out.println("P - Print the list to the screen");
        System.out.println("O - Open a list file from disk");
        System.out.println("S - Save the current list file to disk");
        System.out.println("Q - Quit the program");
        System.out.print("Enter your choice: ");
        return in.nextLine().trim().toUpperCase();
    }

    // ---------- Menu option methods ----------

    private static void addItem() {
        System.out.print("Enter the item to add: ");
        String item = in.nextLine().trim();
        if (item.isEmpty()) {
            System.out.println("Nothing added (blank item).");
            return;
        }
        list.add(item);
        needsToBeSaved = true;
        System.out.println("Added: " + item);
    }

    private static void deleteItem() {
        if (list.isEmpty()) {
            System.out.println("The list is empty. Nothing to delete.");
            return;
        }
        printList();
        int number = getInt("Enter the number of the item to delete: ", 1, list.size());
        String removed = list.remove(number - 1);
        needsToBeSaved = true;
        System.out.println("Deleted: " + removed);
    }

    private static void insertItem() {
        System.out.print("Enter the item to insert: ");
        String item = in.nextLine().trim();
        if (item.isEmpty()) {
            System.out.println("Nothing inserted (blank item).");
            return;
        }
        int number = getInt("Enter the position to insert it at (1-" + (list.size() + 1) + "): ",
                1, list.size() + 1);
        list.add(number - 1, item);
        needsToBeSaved = true;
        System.out.println("Inserted \"" + item + "\" at position " + number);
    }

    private static void printList() {
        displayList();
    }

    /** Loads a .txt list file from disk into the working list. */
    private static void openListFile() throws IOException {
        if (needsToBeSaved && !confirm("You have unsaved changes. Discard them and open another file? (Y/N): ")) {
            System.out.println("Open cancelled.");
            return;
        }
        System.out.print("Enter the file name to open (e.g. groceries.txt): ");
        String fileName = addTxtExtension(in.nextLine().trim());
        Path path = Paths.get(fileName);

        // Throws IOException (e.g. NoSuchFileException) if the file can't be read
        list = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));
        currentFile = fileName;
        needsToBeSaved = false;
        System.out.println("Opened " + fileName + " (" + list.size() + " items).");
    }

    /** Writes the working list to a .txt file on disk. */
    private static void saveListFile() throws IOException {
        String prompt = "Enter a file name to save as (e.g. groceries.txt)";
        if (currentFile != null) {
            prompt += " or press Enter to save to " + currentFile;
        }
        System.out.print(prompt + ": ");
        String fileName = in.nextLine().trim();

        if (fileName.isEmpty()) {
            if (currentFile == null) {
                System.out.println("Save cancelled (no file name).");
                return;
            }
            fileName = currentFile;
        } else {
            fileName = addTxtExtension(fileName);
        }

        // Throws IOException if the file can't be written
        Files.write(Paths.get(fileName), list, StandardCharsets.UTF_8);
        currentFile = fileName;
        needsToBeSaved = false;
        System.out.println("Saved " + list.size() + " items to " + fileName);
    }

    private static boolean quit() throws IOException {
        if (needsToBeSaved && confirm("You have unsaved changes. Save before quitting? (Y/N): ")) {
            saveListFile();
        }
        return confirm("Are you sure you want to quit? (Y/N): ");
    }

    // ---------- Helpers ----------

    private static void displayList() {
        System.out.println();
        System.out.println("---- Current List" + (currentFile != null ? " (" + currentFile + ")" : "")
                + (needsToBeSaved ? " *unsaved changes*" : "") + " ----");
        if (list.isEmpty()) {
            System.out.println("(empty)");
        } else {
            for (int i = 0; i < list.size(); i++) {
                System.out.println((i + 1) + ". " + list.get(i));
            }
        }
        System.out.println("--------------------------------");
    }

    private static String addTxtExtension(String fileName) {
        return fileName.toLowerCase().endsWith(".txt") ? fileName : fileName + ".txt";
    }

    private static int getInt(String prompt, int low, int high) {
        while (true) {
            System.out.print(prompt);
            String text = in.nextLine().trim();
            try {
                int value = Integer.parseInt(text);
                if (value >= low && value <= high) {
                    return value;
                }
                System.out.println("Please enter a number from " + low + " to " + high + ".");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private static boolean confirm(String prompt) {
        while (true) {
            System.out.print(prompt);
            String answer = in.nextLine().trim().toUpperCase();
            if (answer.equals("Y")) return true;
            if (answer.equals("N")) return false;
            System.out.println("Please enter Y or N.");
        }
    }
}