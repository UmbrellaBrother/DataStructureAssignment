import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import java.util.*;
import java.io.*;
import java.nio.file.Paths;

public class BFS extends Application {

    private static final String AIRPORTS_FILE = "airports.txt";
    private static final String EDGES_FILE = "edges.txt";
    private static final String POSITIONS_FILE = "positions.txt";

    private static String[] vertices;
    private static int[][] edges;
    private static double[][] cityPositions;

    private static List<Integer> shortestPath = new ArrayList<>();
    private static String startCity = "";
    private static String endCity = "";
    
    private Stage primaryStage;
    private BorderPane root;
    private Pane graphPane;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Show current working directory for debugging
        String currentDir = Paths.get("").toAbsolutePath().toString();
        System.out.println("Current working directory: " + currentDir);
        
        // Load data from files first
        try {
            loadDataFromFiles();
            showMainMenu();
        } catch (IOException e) {
            System.out.println("Error loading data from files: " + e.getMessage());
            showErrorScreen("Failed to load data files. Current directory: " + currentDir + 
                          "\nPlease make sure airports.txt, edges.txt, and positions.txt are in this directory.");
        }
    }
    
    private void loadDataFromFiles() throws IOException {
        // Show where we're looking for files
        System.out.println("Looking for files in: " + new File(".").getAbsolutePath());
        
        // Check if files exist
        File airportsFile = new File(AIRPORTS_FILE);
        File edgesFile = new File(EDGES_FILE);
        File positionsFile = new File(POSITIONS_FILE);
        
        System.out.println("airports.txt exists: " + airportsFile.exists());
        System.out.println("edges.txt exists: " + edgesFile.exists());
        System.out.println("positions.txt exists: " + positionsFile.exists());
        
        if (!airportsFile.exists() || !edgesFile.exists() || !positionsFile.exists()) {
            throw new IOException("One or more data files are missing");
        }

        // Load vertices from airports.txt
        List<String> vertexList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(AIRPORTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    vertexList.add(line.trim());
                }
            }
        }
        vertices = vertexList.toArray(new String[0]);
        System.out.println("Loaded " + vertices.length + " airports");
        
        // Load edges from edges.txt
        List<int[]> edgeList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(EDGES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        int from = Integer.parseInt(parts[0].trim());
                        int to = Integer.parseInt(parts[1].trim());
                        edgeList.add(new int[]{from, to});
                    }
                }
            }
        }
        edges = edgeList.toArray(new int[0][]);
        System.out.println("Loaded " + edges.length + " edges");
        
        // Load positions from positions.txt
        List<double[]> positionList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(POSITIONS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        double x = Double.parseDouble(parts[0].trim());
                        double y = Double.parseDouble(parts[1].trim());
                        positionList.add(new double[]{x, y});
                    }
                }
            }
        }
        cityPositions = positionList.toArray(new double[0][]);
        System.out.println("Loaded " + cityPositions.length + " positions");
        
        // Verify that all arrays have the same length
        if (vertices.length != cityPositions.length) {
            throw new IOException("Mismatch between number of airports (" + vertices.length + 
                                ") and positions (" + cityPositions.length + ")");
        }
    }
    
    private void showErrorScreen(String message) {
        root = new BorderPane();
        VBox errorBox = new VBox(20);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPadding(new Insets(20));
        
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: red;");
        errorLabel.setWrapText(true);
        
        Button retryButton = new Button("Retry");
        retryButton.setOnAction(e -> {
            try {
                loadDataFromFiles();
                showMainMenu();
            } catch (IOException ex) {
                showErrorScreen("Still cannot load files: " + ex.getMessage());
            }
        });
        
        // Create file creation instructions
        Label instructionLabel = new Label("Create the following files in the directory shown above:");
        instructionLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        
        VBox fileInstructions = new VBox(5);
        fileInstructions.getChildren().addAll(
            new Label("1. airports.txt - List of airport names, one per line"),
            new Label("2. edges.txt - Flight connections in format 'from,to'"),
            new Label("3. positions.txt - Coordinates in format 'x,y'")
        );
        
        errorBox.getChildren().addAll(errorLabel, instructionLabel, fileInstructions, retryButton);
        root.setCenter(errorBox);
        
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    
    private void showMainMenu() {
        root = new BorderPane();
        root.setPrefSize(800, 600);
        
        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        
        Label title = new Label("Malaysia Flight Routes");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Button flightRouteBtn = new Button("Find Flight Route");
        flightRouteBtn.setStyle("-fx-font-size: 16px; -fx-min-width: 200px;");
        flightRouteBtn.setOnAction(e -> {
            runConsoleBFS();
            if (!shortestPath.isEmpty()) {
                showGraphVisualization();
            }
        });
        
        Button addAirportBtn = new Button("Add New Airport");
        addAirportBtn.setStyle("-fx-font-size: 16px; -fx-min-width: 200px;");
        addAirportBtn.setOnAction(e -> addNewAirport());
        
        menuBox.getChildren().addAll(title, flightRouteBtn, addAirportBtn);
        root.setCenter(menuBox);
        
        Scene scene = new Scene(root);
        primaryStage.setTitle("Malaysia Flight Route System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void runConsoleBFS() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter start city: ");
        startCity = scanner.nextLine();
        System.out.print("Enter destination city: ");
        endCity = scanner.nextLine();
        
        Graph<String> graph = new UnweightedGraph<>(vertices, edges);
        int start = graph.getIndex(startCity);
        int end = graph.getIndex(endCity);

        if (start == -1 || end == -1) {
            System.out.println("Invalid city name entered.");
            return;
        }

        AbstractGraph<String>.Tree bfsTree = graph.bfs(start);

        List<Integer> path = new ArrayList<>();
        int current = end;
        while (current != -1) {
            path.add(current);
            current = bfsTree.getParent(current);
        }
        Collections.reverse(path);
        shortestPath = path;

        System.out.println("\nShortest route from " + startCity + " to " + endCity + ":");
        for (int i = 0; i < path.size(); i++) {
            System.out.print(graph.getVertex(path.get(i)));
            if (i != path.size() - 1) System.out.print(" -> ");
        }
        System.out.println();
    }
    
    private void addNewAirport() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the name of the new airport: ");
        String airportName = scanner.nextLine();

        int x = -1, y = -1;
        while (x < 0 || x > 800) {
            System.out.print("Enter the X position (0-800): ");
            try {
                x = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                x = -1;
            }
            if (x < 0 || x > 800) {
                System.out.println("Invalid X position. Please enter a value between 0 and 800.");
            }
        }
        while (y < 0 || y > 800) {
            System.out.print("Enter the Y position (0-800): ");
            try {
                y = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                y = -1;
            }
            if (y < 0 || y > 800) {
                System.out.println("Invalid Y position. Please enter a value between 0 and 800.");
            }
        }

        // Add the new airport to the data structures
        String[] newVertices = Arrays.copyOf(vertices, vertices.length + 1);
        newVertices[vertices.length] = airportName;
        vertices = newVertices;

        double[][] newPositions = Arrays.copyOf(cityPositions, cityPositions.length + 1);
        newPositions[cityPositions.length] = new double[]{x, y};
        cityPositions = newPositions;

        // Save the updated data to files
        try {
            saveDataToFiles();
            System.out.println("Airport '" + airportName + "' added successfully at position (" + x + ", " + y + ")!");
        } catch (IOException e) {
            System.out.println("Error saving data to files: " + e.getMessage());
        }

        showMainMenu();
    }
    
    private void saveDataToFiles() throws IOException {
        // Save vertices to airports.txt
        try (PrintWriter writer = new PrintWriter(new FileWriter(AIRPORTS_FILE))) {
            for (String vertex : vertices) {
                writer.println(vertex);
            }
        }
        
        // Save positions to positions.txt
        try (PrintWriter writer = new PrintWriter(new FileWriter(POSITIONS_FILE))) {
            for (double[] position : cityPositions) {
                writer.println(position[0] + "," + position[1]);
            }
        }
        
        // Note: edges.txt is not modified when adding a new airport
        // since new airports don't automatically get connections
    }
    
    private void showGraphVisualization() {
        root = new BorderPane();
        root.setPrefSize(800, 600);
        
        graphPane = new Pane();
        graphPane.setPrefSize(800, 600);

        // Draw all edges 
        for (int[] edge : edges) {
            int from = edge[0];
            int to = edge[1];
            Line line = new Line(
                cityPositions[from][0], cityPositions[from][1],
                cityPositions[to][0], cityPositions[to][1]
            );
            line.setStroke(Color.LIGHTGRAY);
            line.setStrokeWidth(2);
            graphPane.getChildren().add(line);
        }

        // Draw shortest path edges 
        for (int i = 0; i < shortestPath.size() - 1; i++) {
            int from = shortestPath.get(i);
            int to = shortestPath.get(i + 1);
            Line pathLine = new Line(
                cityPositions[from][0], cityPositions[from][1],
                cityPositions[to][0], cityPositions[to][1]
            );
            pathLine.setStroke(Color.RED);
            pathLine.setStrokeWidth(4);
            graphPane.getChildren().add(pathLine);
        }

        // Draw cities (nodes)
        for (int i = 0; i < vertices.length; i++) {
            Circle cityCircle = new Circle(cityPositions[i][0], cityPositions[i][1], 15);
            
            // Color cities on the path differently
            if (shortestPath.contains(i)) {
                cityCircle.setFill(Color.RED);
            } else {
                cityCircle.setFill(Color.BLUE);
            }
            
            Text cityLabel = new Text(cityPositions[i][0] - 30, cityPositions[i][1] - 20, vertices[i]);
            graphPane.getChildren().addAll(cityCircle, cityLabel);
        }

        // Add title to the top section
        Label title = new Label("Shortest Path: " + startCity + " → " + endCity);
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 15px;");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        
        // Add back button to return to main menu
        Button backButton = new Button("Back to Main Menu");
        backButton.setOnAction(e -> showMainMenu());
        
        HBox topBox = new HBox(title, backButton);
        topBox.setAlignment(Pos.CENTER);
        topBox.setSpacing(20);
        topBox.setPadding(new Insets(10));
        
        // Set up the layout
        root.setTop(topBox);
        root.setCenter(graphPane);
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("BFS Shortest Path Visualization");
        primaryStage.show();
    }

    public static void main(String[] args) {
    launch(args);
}

}