package it.faiella.calcoclientmt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Client della calcolatrice.
 * Si connette al server, invia due numeri e un operatore
 * e stampa la risposta ricevuta.
 */
public class Client {

    public static void main(String[] args) {
        String indirizzo = "localhost";  // il server gira sullo stesso computer
        int porta = 12345;               // deve coincidere con la porta del server

        // Scanner per leggere i dati inseriti da tastiera
        Scanner tastiera = new Scanner(System.in);

        // try-with-resources: socket e stream vengono chiusi automaticamente.
        // - Socket: la connessione verso il server
        // - PrintWriter "out": per INVIARE testo al server (autoflush attivo)
        // - BufferedReader "in": per RICEVERE testo dal server
        try (Socket socket = new Socket(indirizzo, porta);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connesso al server " + indirizzo + ":" + porta);

            // Leggo i dati come semplici stringhe (next() legge una parola).
            // Non uso nextDouble() così, se l'utente sbaglia, il programma
            // non va in crash: sarà il server a segnalare l'errore.
            System.out.print("Primo numero: ");
            String num1 = tastiera.next();

            System.out.print("Operatore (+, -, *, /): ");
            String operatore = tastiera.next();

            System.out.print("Secondo numero: ");
            String num2 = tastiera.next();

            // Invio al server una stringa unica separata da spazi, es. "10 + 5".
            // println aggiunge il "a capo", che il server usa per capire dove finisce la riga.
            out.println(num1 + " " + operatore + " " + num2);

            // Aspetto la risposta del server (readLine blocca finché non arriva)
            String risposta = in.readLine();

            // Se è null il server ha chiuso la connessione senza rispondere
            if (risposta == null) {
                System.out.println("Il server ha chiuso la connessione senza rispondere");
            } else {
                System.out.println("Risposta dal server -> " + risposta);
            }

        } catch (IOException e) {
            // Succede, ad esempio, se il server non è avviato
            System.out.println("Errore di connessione: " + e.getMessage());
        }

        // Chiudo lo Scanner e saluto: la connessione col server è già stata chiusa
        // automaticamente all'uscita dal blocco try
        tastiera.close();
        System.out.println("Connessione chiusa");
    }
}