package org.example;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.*;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import javafx.scene.SnapshotParameters;
import javafx.scene.paint.Color;

public class Main extends Application {
    private Image originalImage;
    private Image modifiedImage;
    private ImageView originalView = new ImageView();
    private ImageView modifiedView = new ImageView();
    private Button btnSave, btnRotateLeft, btnRotateRight, btnScale, btnExecute;
    private ComboBox<String> operationBox;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        AppLogger.init();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        VBox topBox = new VBox(5);
        topBox.setAlignment(Pos.CENTER);

        ImageView logoView = new ImageView();
        try {
            Image logoImage = new Image("file:PWr-7.png");
            logoView.setImage(logoImage);

            logoView.setFitHeight(150);
            logoView.setPreserveRatio(true);
        } catch (Exception ex) {
            AppLogger.logError("Nie udało się załadować pliku logo.png", ex);
        }

        topBox.getChildren().addAll(
                new Text("Aplikacja do Przetwarzania Obrazów"),
                logoView
        );
        root.setTop(topBox);

        // Podgląd
        HBox centerBox = new HBox(10);
        centerBox.setAlignment(Pos.CENTER);
        originalView.setFitWidth(300); originalView.setPreserveRatio(true);
        modifiedView.setFitWidth(300); modifiedView.setPreserveRatio(true);
        centerBox.getChildren().addAll(new VBox(new Text("Oryginał"), originalView), new VBox(new Text("Po edycji"), modifiedView));
        root.setCenter(centerBox);

        VBox bottomBox = new VBox(10);
        bottomBox.setAlignment(Pos.CENTER);
        Text footer = new Text("Autor: Konrad Gierczak 280142");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button btnLoad = new Button("Wczytaj (.jpg)"); //
        btnSave = new Button("Zapisz"); //
        btnRotateLeft = new Button("<- Obróć");
        btnRotateRight = new Button("Obróć ->");
        btnScale = new Button("Skaluj");

        operationBox = new ComboBox<>();
        operationBox.getItems().addAll("NULL", "Negatyw", "Progowanie", "Konturowanie");
        operationBox.setValue("NULL");
        btnExecute = new Button("Wykonaj");

        buttonBox.getChildren().addAll(btnLoad, btnSave, btnRotateLeft, btnRotateRight, btnScale, operationBox, btnExecute);
        bottomBox.getChildren().addAll(buttonBox, footer);
        root.setBottom(bottomBox);

        setButtonsDisabled(true);

        // Akcje
        btnLoad.setOnAction(e -> loadFile());
        btnSave.setOnAction(e -> saveFile());
        btnExecute.setOnAction(e -> executeOperation());
        btnScale.setOnAction(e -> showScaleDialog());
        // Obrót o 90 stopni
        btnRotateLeft.setOnAction(e -> rotateImage(-90));
        btnRotateRight.setOnAction(e -> rotateImage(90));

        Scene scene = new Scene(root, 900, 600);
        stage.setScene(scene);
        stage.setTitle("Lab 6 - PPNiJ");
        stage.setOnCloseRequest(e -> {
            AppLogger.logInfo("Zamknięcie aplikacji");
            ImageProcessor.shutdown();
        });
        stage.show();
    }

    private void loadFile() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG Images", "*.jpg"));
        File file = chooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                originalImage = new Image(file.toURI().toString());
                modifiedImage = originalImage;
                updateViews();
                setButtonsDisabled(false);
                showToast("Pomyślnie załadowano plik");
                AppLogger.logInfo("Wczytano plik: " + file.getName());
            } catch (Exception ex) {
                showToast("Nie udało się załadować pliku");
                AppLogger.logError("Błąd ładowania pliku", ex);
            }
        }
    }

    private void saveFile() {
        if (originalImage == modifiedImage) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Na pliku nie zostały wykonane żadne operacje!");
            alert.showAndWait();
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Zapisz obraz");
        dialog.setHeaderText("Podaj nazwę pliku (3-100 znaków):");

        dialog.showAndWait().ifPresent(name -> {
            if (name.length() < 3 || name.length() > 100) {
                showToast("Wpisz co najmniej 3 znaki (max 100)");
                return;
            }

            File outDir = new File(System.getProperty("user.home"), "Pictures");
            if (!outDir.exists()) {
                outDir.mkdirs();
            }
            File outFile = new File(outDir, name + ".jpg");

            if (outFile.exists()) {
                showToast("Plik " + name + ".jpg już istnieje w systemie. Podaj inną nazwę pliku!");
                return;
            }

            try {
                java.awt.image.BufferedImage bImage = SwingFXUtils.fromFXImage(modifiedImage, null);

                java.awt.image.BufferedImage rgbImage = new java.awt.image.BufferedImage(
                        bImage.getWidth(), bImage.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);

                java.awt.Graphics2D graphics = rgbImage.createGraphics();
                graphics.setPaint(java.awt.Color.WHITE);
                graphics.fillRect(0, 0, bImage.getWidth(), bImage.getHeight());
                graphics.drawImage(bImage, 0, 0, null);
                graphics.dispose();

                ImageIO.write(rgbImage, "jpg", outFile);

                showToast("Zapisano obraz w pliku " + outFile.getName());
                AppLogger.logInfo("Zapisano plik: " + outFile.getAbsolutePath());
            } catch (Exception ex) {
                showToast("Nie udało się zapisać pliku " + outFile.getName());
                AppLogger.logError("Błąd zapisu", ex);
            }
        });
    }

    private void executeOperation() {
        String op = operationBox.getValue();
        if ("NULL".equals(op)) {
            showToast("Nie wybrano operacji do wykonania");
            return;
        }

        try {
            if ("Negatyw".equals(op)) {
                modifiedImage = ImageProcessor.createNegative(modifiedImage);
                showToast("Negatyw został wygenerowany pomyślnie!");
            } else if ("Progowanie".equals(op)) {
                TextInputDialog td = new TextInputDialog("128");
                td.setHeaderText("Podaj wartość progu (0-255):");
                td.showAndWait().ifPresent(val -> {
                    try {
                        modifiedImage = ImageProcessor.applyThresholding(modifiedImage, Integer.parseInt(val));
                        showToast("Progowanie zostało przeprowadzone pomyślnie!");
                    } catch (Exception e) {
                        showToast("Nie udało się wykonać progowania.");
                    }
                });
            } else if ("Konturowanie".equals(op)) {
                modifiedImage = ImageProcessor.applyContour(modifiedImage);
                showToast("Konturowanie zostało przeprowadzone pomyślnie!");
            }
            updateViews();
            AppLogger.logInfo("Wykonano operację: " + op);
        } catch (Exception ex) {
            showToast("Wystąpił błąd podczas operacji: " + op);
            AppLogger.logError("Błąd operacji " + op, ex);
        }
    }

    private void showScaleDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Skalowanie obrazu");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField widthField = new TextField(String.valueOf((int) modifiedImage.getWidth()));
        TextField heightField = new TextField(String.valueOf((int) modifiedImage.getHeight()));
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);

        grid.add(new Label("Szerokość (0-3000):"), 0, 0);
        grid.add(widthField, 1, 0);
        grid.add(new Label("Wysokość (0-3000):"), 0, 1);
        grid.add(heightField, 1, 1);
        grid.add(errorLabel, 1, 2);

        Button btnResize = new Button("Zmień rozmiar");
        Button btnCancel = new Button("Anuluj");
        Button btnRestore = new Button("Przywróć oryginał");

        HBox buttons = new HBox(10, btnResize, btnRestore, btnCancel);
        grid.add(buttons, 0, 3, 2, 1);

        btnRestore.setOnAction(e -> {
            widthField.setText(String.valueOf((int) originalImage.getWidth()));
            heightField.setText(String.valueOf((int) originalImage.getHeight()));
            errorLabel.setText(""); // Czyszczenie ew. błędów
        });

        btnCancel.setOnAction(e -> dialog.close());

        btnResize.setOnAction(e -> {
            String wStr = widthField.getText();
            String hStr = heightField.getText();

            if (wStr.trim().isEmpty() || hStr.trim().isEmpty()) {
                errorLabel.setText("Pole jest wymagane");
                return;
            }

            try {
                int w = Integer.parseInt(wStr);
                int h = Integer.parseInt(hStr);

                if (w <= 0 || w > 3000 || h <= 0 || h > 3000) {
                    errorLabel.setText("Wartości poza zakresem (0-3000)!");
                    return;
                }

                // Skalowanie obrazka
                ImageView iv = new ImageView(modifiedImage);
                iv.setFitWidth(w);
                iv.setFitHeight(h);
                modifiedImage = iv.snapshot(null, null);
                updateViews();

                AppLogger.logInfo("Przeskalowano obraz do " + w + "x" + h);
                dialog.close();

            } catch (NumberFormatException ex) {
                errorLabel.setText("Podano nieprawidłowe znaki!");
            }
        });

        Scene dialogScene = new Scene(grid);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void rotateImage(int angle) {
        ImageView iv = new ImageView(modifiedImage);
        iv.setRotate(angle);
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        modifiedImage = iv.snapshot(params, null);
        updateViews();
        AppLogger.logInfo("Obrócono obraz o " + angle + " stopni");
    }

    private void updateViews() {
        originalView.setImage(originalImage);
        modifiedView.setImage(modifiedImage);
    }

    private void setButtonsDisabled(boolean disabled) {
        btnSave.setDisable(disabled);
        btnRotateLeft.setDisable(disabled);
        btnRotateRight.setDisable(disabled);
        btnScale.setDisable(disabled);
        btnExecute.setDisable(disabled);
    }

    private void showToast(String message) {
        Stage toastStage = new Stage();
        toastStage.initOwner(primaryStage);
        toastStage.initStyle(StageStyle.TRANSPARENT);

        Label text = new Label(message);
        text.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-padding: 10px; -fx-background-radius: 5px;");

        Scene scene = new Scene(new StackPane(text));
        scene.setFill(Color.TRANSPARENT);
        toastStage.setScene(scene);
        toastStage.show();

        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> toastStage.close());
        delay.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}