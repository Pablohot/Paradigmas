package TAREA1205;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JFrameTexto2 {
    private static final int ORTOGRAFIA_REVISION_INTERVALO_SEGUNDOS = 60;
    private static final String ARCHIVO_TEXTO_NOMBRE = "Texto.txt";
    private static final String DICCIONARIO_NOMBRE = "dict.txt";
    private static final String MARCADOR_COLOR = "#f75454";

    public static void main(String[] args) {

        //Texto.txt
        File file = new File("Texto.txt");
        if (!file.exists()) {
            // Si el archivo no existe, crearlo
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //JFrame
        JFrame frame = new JFrame("Editor de texto");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setLocationRelativeTo(null);

        //Scroll
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));
        textArea.setLineWrap(true); // Agregar salto de línea automático
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // Cambiar la política de desplazamiento horizontal
        frame.add(scrollPane, BorderLayout.CENTER);

// Cada vez que se agregue texto, desplazar automáticamente hacia abajo
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateScroll();
            }
            public void removeUpdate(DocumentEvent e) {
                updateScroll();
            }
            public void insertUpdate(DocumentEvent e) {
                updateScroll();
            }
            public void updateScroll() {
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }
        });




        // Leer el archivo de texto y cargar su contenido en el área de texto
        String texto = leerArchivoTexto();
        textArea.setText(texto);

        // Inicializar el hilo que guarda el texto en el archivo cada 5 segundos
        Thread hiloGuardado = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    guardarArchivoTexto(textArea.getText());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        hiloGuardado.start();

        // Cargar el diccionario en un ArrayList
        List<String> diccionario = cargarDiccionario();

        // Inicializar el hilo que revisa la ortografía cada 60 segundos
        Thread hiloOrtografia = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(ORTOGRAFIA_REVISION_INTERVALO_SEGUNDOS * 100);
                    resaltarErroresOrtograficos(textArea, diccionario);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        hiloOrtografia.start();

        frame.setVisible(true);
    }

    private static String leerArchivoTexto() {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(ARCHIVO_TEXTO_NOMBRE), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private static void guardarArchivoTexto(String texto) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(ARCHIVO_TEXTO_NOMBRE), StandardCharsets.UTF_8))) {
            bw.write(texto);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> cargarDiccionario() {
        List<String> diccionario = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(DICCIONARIO_NOMBRE), StandardCharsets.UTF_8))) {
            String palabra;
            while ((palabra = br.readLine()) != null) {
                diccionario.add(palabra.toLowerCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return diccionario;
    }
    private static void resaltarErroresOrtograficos(JTextArea textArea, List<String> diccionario) {
        Highlighter highlighter = textArea.getHighlighter();
        DefaultHighlighter.DefaultHighlightPainter painter =
                new DefaultHighlighter.DefaultHighlightPainter(Color.decode(MARCADOR_COLOR));
        String texto = textArea.getText();
        List<String> palabras = Arrays.asList(texto.split("\\W+"));
        for (String palabra : palabras) {
            if (!diccionario.contains(palabra.toLowerCase())) {
                int inicio = texto.indexOf(palabra);
                int fin = inicio + palabra.length();
                try {
                    highlighter.addHighlight(inicio, fin, painter);
                } catch (javax.swing.text.BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
