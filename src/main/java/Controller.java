import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.embed.swing.SwingFXUtils;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.Timer;


public class Controller {
    // componentele grafice
    public AnchorPane mainPane;
    public Button setWidthButton;
    public ToggleButton nextGenerations;
    public TextField inputText;
    public TextField aliveCells;
    public Button reload;
    public Button saveCurrentGen;
    private int offset = 20; // offset-ul pentru marginea din stanga a ferestrei
    private Vector<Vector<Integer>> currentGen = new Vector<>(); // tabloul bidimensional care memoreaza generatia curenta
    private Vector<Vector<Integer>> nextGen = new Vector<>(); // tabloul bidimensional care memoreaza urmatoarea generatie
    private Vector<Vector<Integer>> savedGen = new Vector<>(); // tabloul bidimensional care memoreaza ultima generatie salvata
    private Vector<Vector<Rectangle>> cells = new Vector<>(); // tabloul bidimensional care memoreaza "patratele" care desemneaza celulele


    @FXML
    public void initialize() {
        // Initial cele 3 butoane care opereaza cu tabloul sunt disabled pentru a preveni erori si text field-ul pentru random case
        nextGenerations.setDisable(true);
        saveCurrentGen.setDisable(true);
        reload.setDisable(true);
        aliveCells.setDisable(true);
        aliveCells.setDisable(false);
    }

    // Functia deseneaza pe fereastra principala grid-ul ce desemneaza populatia de celule
    public void createGrid(int sizeOfMatrix) {
        long cellWidth = ((int) mainPane.getWidth() - 190) / (sizeOfMatrix - 1); // Ecuatia prin care se determina latimea unei casute ce reprezinta o celula
        for (int i = 1; i <= sizeOfMatrix - 1; i++)
            for (int j = 1; j <= sizeOfMatrix - 1; j++) {
                cells.get(i).get(j).setX((j - 1) * cellWidth + offset); // se seteaza abcisa punctului de start al patratului
                cells.get(i).get(j).setY((i - 1) * cellWidth + offset);// se seteaza ordonata punctului de start al patratului
                cells.get(i).get(j).setHeight(cellWidth);// se seteaza inaltimea
                cells.get(i).get(j).setWidth(cellWidth);// se seteaza lungimea
                if (currentGen.get(i).get(j) == 1)// daca celula este vie, atunci ea este desenata cu negru
                    cells.get(i).get(j).setFill(Color.BLACK);
                else
                    cells.get(i).get(j).setFill(Color.WHITE);// daca este moarta, cu alb
                cells.get(i).get(j).setStroke(Color.BLACK);// marginea patratului este de culoarea neagra
                mainPane.getChildren().add(cells.get(i).get(j));// se insereaza pe componenta grafica patratul astfel obtinut
            }
    }

    public boolean initializeCurrentGen(int sizeOfMatrix) throws IOException {
        File file = new File("C:\\Users\\dbogd\\Desktop\\ProiectMAP\\src\\main\\resources\\input");// calea absoluta a fisierului de input
        BufferedReader br = new BufferedReader(new FileReader(file));// un buffer pentru citire
        String st;
        int lineContor = 1;// contorul de linii, pentru a sti pe ce linie se insereaza valoarea, dar si pentru a verifica daca numarul de linii din fisier este valid
        while ((st = br.readLine()) != null){ // se preia fiecare linie
                int columnContor;//
                columnContor = st.length();// preluam lungimea sirului
                if(columnContor / 2 + 1 != sizeOfMatrix) { // Stim ca inputul contine spatii intre valori, asadar verificam daca avem acelasi numar de coloane ca si cel dat prin interfata grafica
                    // daca nu corespund, atunci se afiseaza o eroare
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setHeaderText("Input not valid");
                    errorAlert.setContentText("The number of cell's live states is not a valid one. Verify that your number of columns and the number from GUI is the same. Please check your input!");
                    errorAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                    errorAlert.showAndWait();
                    return false;
                }
                int columnIndex = 1;// un index de coloane, proces asemanator ca la contorul de linii
                for(int i = 0; i<columnContor; i+=2) {// se merge din 2 in 2 pentru a evita spatiile
                    Integer testValue = Integer.parseInt(String.valueOf(st.charAt(i)));// se preia numarul de pe rand
                    if( testValue == 1 || testValue == 0 ) // daca este o valoare valida, se insereaza
                        currentGen.get(lineContor).set(columnIndex++, testValue);
                    else {
                        // daca nu, se afiseaza o eroare
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setHeaderText("Input not valid");
                        errorAlert.setContentText("The value from row " + lineContor + " and column " + columnIndex + " is invalid. Please check your input!");
                        errorAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                        errorAlert.showAndWait();
                        return false;
                    }
                }
                lineContor++;// creste contorul de linii citite
        }
        if(lineContor - 1 < sizeOfMatrix){ // daca am citit mai putine linii decat numarul dat in interfata grafica, mi se afiseaza o eroare
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText("The number of cell's live states is not a valid one. You need to insert more rows! Please check your input!");
            errorAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            errorAlert.showAndWait();
            return false;
        }
        else if (lineContor - 1 > sizeOfMatrix){// la fel si pentru cazul in care am inserat mai multe linii
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText("The number of cell's live states is not a valid one. You need to delete some rows! Please check your input!");
            errorAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            errorAlert.showAndWait();
            return false;
        }
        // daca se ajunge in acest punct, datele au fost valide
            return true;
    }

    public void setWidthButtonPressed(ActionEvent actionEvent) throws IOException {
        int iInputText = Integer.parseInt(inputText.getText());// preiau latimea grid-ului
        if (iInputText > 200) { // o limitare a aplicatiei, de la griduri de 200 in sus incep sa apara delay-uri de la mici la mari
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText("The width of the grid is too high");
            errorAlert.showAndWait();
        } else { // daca totul e ok, initializez datele
            int sizeOfMatrix = iInputText + 1;
            for (int i = 0; i <= sizeOfMatrix; i++) {
                Vector<Rectangle> auxRectangle = new Vector<>();
                Vector<Integer> aux = new Vector<>();
                Vector<Integer> aux2 = new Vector<>();
                Vector<Integer> aux3 = new Vector<>();
                for (int j = 0; j <= sizeOfMatrix; j++) {
                    aux.add(0);
                    aux2.add(0);
                    aux3.add(0);
                    auxRectangle.add(new Rectangle());
                }
                currentGen.add(aux);
                nextGen.add(aux2);
                savedGen.add(aux3);
                cells.add(auxRectangle);
            }
//          boolean correctInitialization =  initializeCurrentGen(iInputText); // verific daca grid-ul pentru generatia curenta este corect initializat
//
            int numberOfCells = Integer.parseInt(aliveCells.getText()); // variabila care ajuta la cazul de random

            if (iInputText * iInputText < numberOfCells) { // nu pot avea mai multe celule decat grid-ul poate accepta
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setHeaderText("Input not valid");
                errorAlert.setContentText("The number of cells ^ 2 should not be higher than grid's width ^ 2");
                errorAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                errorAlert.showAndWait();
            }
            Random randomNumber = new Random();
            for (int i = 1; i <= numberOfCells; i++) {
                int a, b;
                do {
                    a = randomNumber.nextInt(sizeOfMatrix - 1) + 1;
                    b = randomNumber.nextInt(sizeOfMatrix - 1) + 1;
                } while (currentGen.get(a).get(b) == 1);
                currentGen.get(a).set(b, 1);
            }
  //        if(correctInitialization){ // daca da, pot merge mai departe
                createGrid(sizeOfMatrix); // desenez gridul
               // dezactivez butonul de initializare si le activez pe celalalte
                setWidthButton.setDisable(true);
                nextGenerations.setDisable(false);
                saveCurrentGen.setDisable(false);
                inputText.setDisable(true);
            //}
        }
    }

    // Algoritmul prin care se calculeaza generatia urmatoare
    public void calculateNextGen() {
        for (int i = 1; i <= currentGen.size() - 2; i++) { // de mentionat este ca grid-ul este bordat cu valori de 0 pentru a functiona corect algoritmul
            int localSum; // numarul de vecini
            for (int j = 1; j <= currentGen.size() - 2; j++) {
                localSum = 0;
                localSum += currentGen.get(i - 1).get(j);// vecinul de sus
                localSum += currentGen.get(i - 1).get(j + 1);// vecinul din dreapta sus
                localSum += currentGen.get(i).get(j + 1);// vecinul din dreapta
                localSum += currentGen.get(i + 1).get(j + 1);// vecinul din dreapta jos
                localSum += currentGen.get(i + 1).get(j);// vecinul de sub
                localSum += currentGen.get(i + 1).get(j - 1);// vecinul din stanga jos
                localSum += currentGen.get(i).get(j - 1);// vecinul din stanga
                localSum += currentGen.get(i - 1).get(j - 1);// vecinul din dreapta sus
                if (currentGen.get(i).get(j) == 1) {
                    if (localSum < 2) // daca este vie si are mai putin de 1 vecin viu
                        nextGen.get(i).set(j, 0);//aceasta va muri in generatia urmatoare
                    else if (localSum >= 4)// daca este vie si are mai mult de 4 vecini vii
                        nextGen.get(i).set(j, 0);//aceasta va muri in generatia urmatoare
                    else
                        nextGen.get(i).set(j, 1);// daca are 2 sau 3 vecini vii, aceasta este vie in generatia urmatoare
                } else {
                    if (localSum == 3) // daca este moarta si are 3 vecini vii
                        nextGen.get(i).set(j, 1); // devine vie in generatia urmatoare
                    else
                        nextGen.get(i).set(j, 0);// in caz contrar ramane moarta
                }
            }
        }
    }

    // Functia de desenare a grid-ului
    public void paintGen(Vector<Vector<Integer>> inputArray) {
        for (int i = 1; i <= cells.size() - 2; i++)
            for (int j = 1; j <= cells.size() - 2; j++)
                if (inputArray.get(i).get(j) == 1) // daca este vie, patratul se coloreaza cu negru
                    cells.get(i).get(j).setFill(Color.BLACK);
                else // respectiv cu alb, daca acesta este moarta
                    cells.get(i).get(j).setFill(Color.WHITE);
    }

    // O functie ce face transfer-ul de date intre tablourile bidimensionale
    public void copyMatrix(Vector<Vector<Integer>> src, Vector<Vector<Integer>> dest){
        for( int i = 1; i <= src.size() - 2; i++)
            for( int j = 1; j <= src.size() - 2; j++ ){
                int a;
                a = src.get(i).get(j);
                dest.get(i).set(j, a);
            }
    }

    // Cand butonul de generare al generatiei urmatoare este apasat
    public void nextGenerationsPressed(ActionEvent actionEvent) {
        TimerTask task = new TimerTask() { // se creaza un timer task
            @Override
            public void run() { // cand se executa
                if (nextGenerations.isSelected()) { // daca butonul este inca apasat
                    calculateNextGen();// calculeaza generatia urmatoare
                    paintGen(nextGen);// deseneaza generatia urmatoare
                    copyMatrix(nextGen, currentGen);// generatia urmatoare devine generatie curenta pentru a trece la urmatorul pas

                } else { // daca nu mai este apasat, opresc task-ul
                    this.cancel();
                }
            }
        };
        Timer timer = new Timer();// un timer, care faciliteaza multitasking-ul, astfel desenarea este animata
        timer.schedule(task, 300L, 300L);// task-urile se executa timp de 300 de ms, dupa un delay de 300 de ms

    }

    // Cand butonul de salvare al generatiei este apasat
    public void saveCurrentGenPressed(ActionEvent actionEvent) {
        copyMatrix(currentGen, savedGen);// se face transferul de informatie din generatia curenta in grid-ul ajutator
        // mesaj de confirmare
        Alert errorAlert = new Alert(Alert.AlertType.CONFIRMATION);
        errorAlert.setHeaderText("Confirmation dialog");
        errorAlert.setContentText("Successfully saved!");
        errorAlert.showAndWait();
        // Se realizeaza un screenshot
        WritableImage img = mainPane.snapshot(new SnapshotParameters(), null);
        // Un file chooser
        JFileChooser chooser = new JFileChooser();
        // Care porneste de la fisierele de baza ale user-ului
        chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        // Se converteste imaginea in buffered image
        BufferedImage img2 = SwingFXUtils.fromFXImage(img, null);
        // Se afiseaza dialogul de salvare
        int result = chooser.showSaveDialog(null);
        // Daca este posibila salvarea
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                // Se creaza un fisier
                File fileToSave = chooser.getSelectedFile();
                // Care se salveaza ca png si in care este inserat screenshot-ul
                ImageIO.write(img2, "png", fileToSave);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        reload.setDisable(false);
    }
    // Cand butonul de reluare la ultima salvare facuta este apasat
    public void reloadPressed(ActionEvent actionEvent) {
        copyMatrix(savedGen, currentGen);// se face transferul din ultima generatie salvata in generatia curenta
        paintGen(currentGen);// si se deseneaza generatia curenta
    }
}
