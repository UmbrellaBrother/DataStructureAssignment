import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GraphViewer extends Application {

    private static final String AIRPORTS_FILE = "src/resources/airports.txt";
    private static final String EDGES_FILE = "src/resources/edges.txt";

    private String[] airports;
    private List<int[]> edges = new ArrayList<>();
    private double[][] positions;

    @Override
    public void start(Stage stage) {
        // --- Main Menu ---
        Button viewGraphBtn = new Button("View Graph");
        Button exitBtn = new Button("Exit");

        VBox menuLayout = new VBox(20, viewGraphBtn, exitBtn);
        menuLayout.setStyle("-fx-alignment: center; -fx-padding: 50;");
        Scene menuScene = new Scene(menuLayout, 800, 600);

        // --- Graph Viewer Page ---
        Pane graphPane = new Pane();
        BorderPane graphLayout = new BorderPane();
        graphLayout.setCenter(graphPane);

        Button backBtn = new Button("Back to Menu");
        graphLayout.setBottom(backBtn);
        Scene graphScene = new Scene(graphLayout, 800, 600);

        // Button Actions
        viewGraphBtn.setOnAction(e -> {
            try {
                loadAirports();
                loadEdges();
                drawGraph(graphPane);
                stage.setScene(graphScene);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        backBtn.setOnAction(e -> stage.setScene(menuScene));
        exitBtn.setOnAction(e -> stage.close());

        // Start with menu page
        stage.setTitle("Graph Application");
        stage.setScene(menuScene);
        stage.show();
    }

    private void loadAirports() throws IOException {
        List<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(AIRPORTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) list.add(line.trim());
            }
        }
        airports = list.toArray(new String[0]);

        // auto-generate circular layout
        positions = new double[airports.length][2];
        double centerX = 400, centerY = 300, radius = 200;
        for (int i = 0; i < airports.length; i++) {
            double angle = 2 * Math.PI * i / airports.length;
            positions[i][0] = centerX + radius * Math.cos(angle);
            positions[i][1] = centerY + radius * Math.sin(angle);
        }
    }

    private void loadEdges() throws IOException {
        edges.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(EDGES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        int from = Integer.parseInt(parts[0].trim());
                        int to = Integer.parseInt(parts[1].trim());
                        edges.add(new int[]{from, to});
                    }
                }
            }
        }
    }

    private void drawGraph(Pane pane) {
        pane.getChildren().clear();

        // Draw edges
        for (int[] edge : edges) {
            int from = edge[0];
            int to = edge[1];

            double x1 = positions[from][0];
            double y1 = positions[from][1];
            double x2 = positions[to][0];
            double y2 = positions[to][1];

            Line line = new Line(x1, y1, x2, y2);
            line.setStroke(Color.GRAY);
            pane.getChildren().add(line);
        }

        // Draw nodes
        for (int i = 0; i < airports.length; i++) {
            double x = positions[i][0];
            double y = positions[i][1];
            Circle circle = new Circle(x, y, 8, Color.LIGHTBLUE);
            Text label = new Text(x - 20, y - 10, airports[i]);
            pane.getChildren().addAll(circle, label);
        }
    }
    // public static void main(String[] args) {
    //     launch(args);
    // }
}


