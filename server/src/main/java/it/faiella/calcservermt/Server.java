package it.faiella.calcservermt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server della calcolatrice (multithread).
 * Resta in ascolto su una porta e, per ogni client che si connette,
 * avvia un thread dedicato che esegue il calcolo richiesto.
 */
public class Server {

    public static void main(String[] args) {
        int porta = 12345;  // porta su cui il server è in ascolto

        // ServerSocket = "porta d'ingresso" del server.
        // Con il try-with-resources viene chiuso automaticamente alla fine.
        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            System.out.println("Server in ascolto sulla porta " + porta);

            // Ciclo infinito: il server non si ferma mai e aspetta nuovi client
            while (true) {
                // accept() blocca il programma finché un client non si connette,
                // poi restituisce il Socket per parlare con quel client
                Socket socket = serverSocket.accept();
                System.out.println("Client connesso: " + socket.getInetAddress());

                // Creo un thread per questo client e lo avvio.
                // Così il server torna subito ad accept() e può accettare
                // altri client mentre questo viene ancora servito.
                new Thread(new GestoreClient(socket)).start();
            }
        } catch (IOException e) {
            // Succede, ad esempio, se la porta è già occupata
            System.out.println("Errore del server: " + e.getMessage());
        }
    }
}

/**
 * Gestisce la comunicazione con UN solo client.
 * Implementa Runnable, quindi il suo metodo run() viene eseguito in un thread.
 */
class GestoreClient implements Runnable {

    // Socket del client che questo thread deve servire
    private Socket socket;

    // Costruttore: riceve il socket dal server
    public GestoreClient(Socket socket) {
        this.socket = socket;
    }

    // Metodo eseguito quando il thread parte
    @Override
    public void run() {
        // try-with-resources: gli stream vengono chiusi in automatico.
        // - BufferedReader "in": per LEGGERE le righe di testo inviate dal client
        // - PrintWriter "out": per SCRIVERE la risposta al client
        //   (il parametro true attiva l'autoflush: ogni println invia subito i dati)
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Leggo una riga inviata dal client, es. "10 + 5"
            String richiesta = in.readLine();
            System.out.println("Ricevuto: " + richiesta);

            // Calcolo il risultato (o il messaggio d'errore) e lo rispedisco
            String risposta = calcola(richiesta);
            out.println(risposta);

        } catch (IOException e) {
            // Problema di rete con questo client (es. si è disconnesso all'improvviso)
            System.out.println("Errore nella comunicazione con il client: " + e.getMessage());
        } finally {
            // Qualunque cosa succeda, chiudo la connessione con il client.
            // Poi il thread termina e il server resta in attesa di altri client.
            try {
                socket.close();
            } catch (IOException e) {
                // niente da fare se la chiusura fallisce
            }
            System.out.println("Client disconnesso");
        }
    }

    /**
     * Elabora la richiesta nel formato "numero operatore numero".
     * Restituisce sempre una stringa: "RISULTATO: ..." oppure "ERRORE: ...".
     */
    private String calcola(String richiesta) {
        // readLine() restituisce null se il client chiude senza inviare nulla
        if (richiesta == null) {
            return "ERRORE: richiesta vuota";
        }

        // trim() toglie gli spazi a inizio/fine; split("\\s+") divide la stringa
        // in pezzi usando uno o più spazi come separatore.
        // Esempio: "10 + 5" -> ["10", "+", "5"]
        String[] parti = richiesta.trim().split("\\s+");

        // Controllo di sintassi: devono esserci esattamente 3 elementi
        if (parti.length != 3) {
            return "ERRORE: formato non valido, usa: numero operatore numero";
        }

        double a, b;
        try {
            // Converto il primo e il terzo elemento in numeri (interi o decimali)
            a = Double.parseDouble(parti[0]);
            b = Double.parseDouble(parti[2]);
        } catch (NumberFormatException e) {
            // Se uno dei due non è un numero (es. "abc") parseDouble lancia questa eccezione
            return "ERRORE: operando non numerico";
        }

        // L'operatore è il secondo elemento
        String operatore = parti[1];
        double risultato;

        // Scelgo l'operazione in base all'operatore
        switch (operatore) {
            case "+":
                risultato = a + b;
                break;
            case "-":
                risultato = a - b;
                break;
            case "*":
                risultato = a * b;
                break;
            case "/":
                // Errore matematico: non si può dividere per zero
                if (b == 0) {
                    return "ERRORE: divisione per zero";
                }
                risultato = a / b;
                break;
            default:
                // Qualsiasi altro simbolo non è supportato
                return "ERRORE: operatore '" + operatore + "' non riconosciuto";
        }

        return "RISULTATO: " + risultato;
    }
}