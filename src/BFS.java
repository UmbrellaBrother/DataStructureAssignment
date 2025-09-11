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

    private static final String AIRPORTS_FILE = "src\\resources\\airports.txt";
    private static final String EDGES_FILE = "src\\resources\\edges.txt";
    private static final String POSITIONS_FILE = "src\\resources\\positions.txt";

    private static String[] vertices;
    private static int[][] edges;
    private static double[][] cityPositions;

    private static List<Integer> shortestPath = new ArrayList<>();
    private static String startCity = "";
    private static String endCity = "";

    private static double[][] edgeWeights; // [time, cost] for each edge
    private static String[][] edgeLabels; // Added: to store "50min, RM120" for each edge

    private Stage primaryStage;
    private BorderPane root;
    private Pane graphPane;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        try {
            loadDataFromFiles();
            showMainMenu();
        } catch (IOException e) {
            showErrorScreen("Failed to load data files. Please make sure airports.txt, edges.txt, and positions.txt are in this directory.");
        }
    }

    private void loadDataFromFiles() throws IOException {
        // Load airports
        List<String> vertexList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(AIRPORTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) vertexList.add(line.trim());
            }
        }
        vertices = vertexList.toArray(new String[0]);

        // Load edges with time and cost
        List<int[]> edgeList = new ArrayList<>();
        List<double[]> edgeWeightList = new ArrayList<>(); // [time, cost]
        try (BufferedReader reader = new BufferedReader(new FileReader(EDGES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 4) { // expecting: from,to,time,cost
                        int from = Integer.parseInt(parts[0].trim());
                        int to = Integer.parseInt(parts[1].trim());
                        int time = Integer.parseInt(parts[2].trim());
                        int cost = Integer.parseInt(parts[3].trim());
                        edgeList.add(new int[]{from, to});
                        edgeWeightList.add(new double[]{time, cost});
                    }
                }
            }
        }
        edges = edgeList.toArray(new int[0][]);
        edgeWeights = edgeWeightList.toArray(new double[0][]); // save weights for later

        // Initialize edgeLabels array and fill it
        edgeLabels = new String[vertices.length][vertices.length]; // Added
        for (int i = 0; i < edgeList.size(); i++) { // Added
            int from = edgeList.get(i)[0];
            int to = edgeList.get(i)[1];
            double time = edgeWeights[i][0];
            double cost = edgeWeights[i][1];
            edgeLabels[from][to] = (int) time + "min, RM" + (int) cost; // Added
        }

        // Load positions
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

        if (vertices.length != cityPositions.length) {
            throw new IOException("Mismatch between number of airports and positions.");
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

        errorBox.getChildren().addAll(errorLabel, retryButton);
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
        flightRouteBtn.setStyle("-fx-font-size: 16px; -fx-min-width: 250px;");
        flightRouteBtn.setOnAction(e -> {
            runConsoleBFS();
            if (!shortestPath.isEmpty()) {
                showGraphVisualization();
            }
        });

        Button flightRouteBtn2 = new Button("Find Cheapest/Fastest Route");
        flightRouteBtn2.setStyle("-fx-font-size: 16px; -fx-min-width: 250px;");
        flightRouteBtn2.setOnAction(e -> {
            runConsoleDijkstra();
            if (!shortestPath.isEmpty()) {
                showGraphVisualization();
            }
        });

        Button addAirportBtn = new Button("Add New Airport");
        addAirportBtn.setStyle("-fx-font-size: 16px; -fx-min-width: 250px;");
        addAirportBtn.setOnAction(e -> addNewAirport());

        menuBox.getChildren().addAll(title, flightRouteBtn, flightRouteBtn2, addAirportBtn);
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

    private void runConsoleDijkstra() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter start city: ");
        startCity = scanner.nextLine();
        System.out.print("Enter destination city: ");
        endCity = scanner.nextLine();

        // Build weighted graph using both time and cost
        WeightedGraph<String> graph = new WeightedGraph<>(vertices);
        for (int i = 0; i < edges.length; i++) {
            int from = edges[i][0];
            int to = edges[i][1];
            double time = edgeWeights[i][0]; // time in minutes
            double cost = edgeWeights[i][1]; // cost in RM
            graph.addEdge(from, to, time, cost); // updated addEdge
        }

        Dijkstra<String> dijkstra = new Dijkstra<>(graph);

        // Ask user which mode they want
        System.out.print("Calculate shortest route by 'time' or 'cost'? ");
        String mode = scanner.nextLine().trim().toLowerCase();
        if (!mode.equals("time") && !mode.equals("cost")) {
            System.out.println("Invalid mode, defaulting to 'time'.");
            mode = "time";
        }

        List<String> path = dijkstra.getPath(startCity, endCity, mode);

        if (path.isEmpty()) {
            System.out.println("Invalid city name or no route found.");
            return;
        }

        shortestPath.clear();
        for (String city : path) {
            shortestPath.add(graph.getIndex(city));
        }

        System.out.println("\n" + (mode.equals("time") ? "Fastest" : "Cheapest") +
                        " route from " + startCity + " to " + endCity + ":");
        for (int i = 0; i < path.size(); i++) {
            System.out.print(path.get(i));
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
        }
        while (y < 0 || y > 800) {
            System.out.print("Enter the Y position (0-800): ");
            try {
                y = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                y = -1;
            }
        }

        String[] newVertices = Arrays.copyOf(vertices, vertices.length + 1);
        newVertices[vertices.length] = airportName;
        vertices = newVertices;

        double[][] newPositions = Arrays.copyOf(cityPositions, cityPositions.length + 1);
        newPositions[cityPositions.length] = new double[]{x, y};
        cityPositions = newPositions;

        try {
            saveDataToFiles();
            System.out.println("Airport '" + airportName + "' added successfully at (" + x + ", " + y + ")");
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }

        showMainMenu();
    }

    private void saveDataToFiles() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(AIRPORTS_FILE))) {
            for (String vertex : vertices) {
                writer.println(vertex);
            }
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(POSITIONS_FILE))) {
            for (double[] position : cityPositions) {
                writer.println(position[0] + "," + position[1]);
            }
        }
    }

    private void drawArrow(double startX, double startY, double endX, double endY, Color color) {
        // Draw main line
        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(color);
        line.setStrokeWidth(2);

        // Arrowhead size
        double arrowLength = 20;
        double arrowWidth = 10;

        // Calculate angle of line
        double angle = Math.atan2(endY - startY, endX - startX);

        // Points for arrowhead
        double x1 = endX - arrowLength * Math.cos(angle - Math.PI / 6);
        double y1 = endY - arrowLength * Math.sin(angle - Math.PI / 6);

        double x2 = endX - arrowLength * Math.cos(angle + Math.PI / 6);
        double y2 = endY - arrowLength * Math.sin(angle + Math.PI / 6);

        Line arrow1 = new Line(endX, endY, x1, y1);
        Line arrow2 = new Line(endX, endY, x2, y2);

        arrow1.setStroke(color);
        arrow2.setStroke(color);
        arrow1.setStrokeWidth(2);
        arrow2.setStrokeWidth(2);

        // Add to graph pane
        graphPane.getChildren().addAll(line, arrow1, arrow2);
    }

    private void showGraphVisualization() {
        root = new BorderPane();
        root.setPrefSize(800, 600);

        graphPane = new Pane();
        graphPane.setPrefSize(800, 600);

        for (int[] edge : edges) {
            int from = edge[0];
            int to = edge[1];

            double x1 = cityPositions[from][0];
            double y1 = cityPositions[from][1];
            double x2 = cityPositions[to][0];
            double y2 = cityPositions[to][1];

            // Draw line
            Line line = new Line(x1, y1, x2, y2);
            line.setStroke(Color.GRAY);
            line.setStrokeWidth(1);
            graphPane.getChildren().add(line);

            // Draw arrow at the end
            double angle = Math.atan2(y2 - y1, x2 - x1);
            double arrowLength = 10;
            double arrowAngle = Math.toRadians(15);
            Line arrow1 = new Line(
                    x2, y2,
                    x2 - arrowLength * Math.cos(angle - arrowAngle),
                    y2 - arrowLength * Math.sin(angle - arrowAngle)
            );
            Line arrow2 = new Line(
                    x2, y2,
                    x2 - arrowLength * Math.cos(angle + arrowAngle),
                    y2 - arrowLength * Math.sin(angle + arrowAngle)
            );
            graphPane.getChildren().addAll(arrow1, arrow2);

            // Draw edge label
            String label = edgeLabels[from][to]; // Added
            if (label != null) { // Added
                double midX = (x1 + x2) / 2;
                double midY = (y1 + y2) / 2;
                Text text = new Text(midX, midY, label);
                text.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
                graphPane.getChildren().add(text);
            }
        }

        // Draw shortest path edges 
        for (int i = 0; i < shortestPath.size() - 1; i++) {
            int from = shortestPath.get(i);
            int to = shortestPath.get(i + 1);
            drawArrow(
                    cityPositions[from][0], cityPositions[from][1],
                    cityPositions[to][0], cityPositions[to][1],
                    Color.RED
            );
        }

        // Draw cities (nodes)
        for (int i = 0; i < vertices.length; i++) {
            double radius = 5; // smaller circle
            Circle cityCircle = new Circle(cityPositions[i][0], cityPositions[i][1], radius);
            if (shortestPath.contains(i)) {
                cityCircle.setFill(Color.RED);
            } else {
                cityCircle.setFill(Color.BLUE);
            }
            Text cityLabel = new Text(cityPositions[i][0] - 30, cityPositions[i][1] - 20, vertices[i]);
            graphPane.getChildren().addAll(cityCircle, cityLabel);
        }

        Label title = new Label("Cheapest/Fastest Path: " + startCity + " → " + endCity);
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 15px;");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);

        Button backButton = new Button("Back to Main Menu");
        backButton.setOnAction(e -> showMainMenu());

        HBox topBox = new HBox(title, backButton);
        topBox.setAlignment(Pos.CENTER);
        topBox.setSpacing(20);
        topBox.setPadding(new Insets(10));

        root.setTop(topBox);
        root.setCenter(graphPane);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Flight Route Visualization (Dijkstra)");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
