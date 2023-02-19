import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;
import java.util.Random;
import java.math.BigInteger;
import java.sql.*;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class QueryRunner implements Runnable {
    // Declare socket for client access
    protected Socket socketConnection;

    public QueryRunner(Socket clientSocket) {
        this.socketConnection = clientSocket;
    }
    public static int book(String clientCommand,PrintWriter printWriter, Connection connection)
    {
        String responseQuery = "";
        try
        {
            StringTokenizer st = new StringTokenizer(clientCommand, ", ");

                    int num = Integer.parseInt(st.nextToken());

                    String[] passengers;
                    passengers = new String[num];

                    // System.out.println(st.nextToken());
                    for (int i = 0; i < num; i++) {
                        passengers[i] = st.nextToken();
                    }
                    int train_id = Integer.parseInt(st.nextToken());
                    String date = st.nextToken();
                    String coach_type = st.nextToken();

                    /*
                    * for (String element : passengers) {
                    * System.out.println(element);
                    * }
                    */

                    boolean available = false;
                    int book_a = 0, avail_a = 0;
                    int book_s = 0, avail_s = 0;
                        
                    
                        String q1 = "BEGIN;";
                        Statement stmt = connection.createStatement();
                        stmt.execute(q1);
                        String sql="Lock table trains IN ACCESS EXCLUSIVE MODE";
                        
                        stmt = connection.createStatement();
                        stmt.execute(sql);
                    
                    
                    if (coach_type.matches("AC")) {
                        String query_a_b = "select coalesce( (SELECT count(*) FROM trains WHERE trains.train_id = "
                                + train_id + " AND trains.doj = '" + date
                                + "' AND booked=1 AND trains.coach_type like 'AC' GROUP BY booked), 0 )";
                        String query_a_a = "select coalesce( (SELECT count(*) FROM trains WHERE trains.train_id = "
                                + train_id + " AND trains.doj = '" + date
                                + "' AND booked=0 AND trains.coach_type like 'AC' GROUP BY booked), 0 )";
                        
                            stmt = connection.createStatement();
                            ResultSet rs = stmt.executeQuery(query_a_a);
                            while (rs.next()) {
                                avail_a = rs.getInt(1);
                            }

                            rs = stmt.executeQuery(query_a_b);
                            while (rs.next()) {
                                book_a = rs.getInt(1);
                            }

                        // System.out.println("AC Coach of Train- " + train_id + " Dated- " + date + " has only " + avail_a
                        //         + " seats available.");
                        if (avail_a >= num) {
                            available = true;
                            // System.out.println("Required seats are avaliable");
                        } else {
                            // System.out.println("Required seats are not avaliable");
                        }
                    }

                    else {
                        
                        String query_s_b = "select coalesce( (SELECT count(*) FROM trains WHERE trains.train_id = "
                                + train_id + " AND trains.doj = '" + date
                                + "' AND booked=1 AND trains.coach_type like  'SL' GROUP BY booked), 0 )";
                        String query_s_a = "select coalesce( (SELECT count(*) FROM trains WHERE trains.train_id = "
                                + train_id + " AND trains.doj = '" + date
                                + "' AND booked=0 AND trains.coach_type like 'SL' GROUP BY booked), 0 )";
                                
                            stmt = connection.createStatement();
                            ResultSet rs = stmt.executeQuery(query_s_a);
                            while (rs.next()) {
                                avail_s = rs.getInt(1);
                            }

                            rs = stmt.executeQuery(query_s_b);
                            while (rs.next()) {
                                book_s = rs.getInt(1);
                            }

                        // System.out.println("SL Coach of Train- " + train_id + " Dated- " + date + " has only " + avail_s
                        //         + " seats available.");
                        if (avail_s >= num) {
                            available = true;
                            // System.out.println("Required seats are avaliable");
                        } else {
                            // System.out.println("Required seats are not avaliable");
                        }
                    }

                    BigInteger pnr = new BigInteger("0");
                    if (available == true) 
                    {
                        // seats are avaliable, can be booked
                        // call book_seats() procedure
                        // System.out.println("Ticket can be booked.");
                        Map<String, Integer> myMap = new HashMap<String, Integer>();
                        myMap.put("0", 1);
                        while (myMap.containsKey(pnr.toString())) {
                            BigInteger maxLimit = new BigInteger("5000000000000");
                            BigInteger minLimit = new BigInteger("25000000000");
                            BigInteger bigInteger = maxLimit.subtract(minLimit);
                            Random randNum = new Random();
                            int len = maxLimit.bitLength();
                            pnr = new BigInteger(len, randNum);
                            if (pnr.compareTo(minLimit) < 0) {
                                pnr = pnr.add(minLimit);
                            }
                            if (pnr.compareTo(bigInteger) >= 0) {
                                pnr = pnr.mod(bigInteger).add(minLimit);
                            }

                        }
                        myMap.put(pnr.toString(), 1);
                        // System.out.println("pnr number is " + pnr);

                        // Query to call book ticket
                        String query_call_book = "";

                        String pass = Arrays.toString(passengers);
                        pass = pass.replace('[', '{');
                        pass = pass.replace(']', '}');

                        if (coach_type.matches("AC")) {
                            query_call_book = "call assign_berth(" + pnr + "," + num + ",'" + pass + "'," + train_id + ",'"
                                    + date + "','" + coach_type + "'," + avail_a + "," + book_a + ");";

                        } else {
                            query_call_book = "call assign_berth(" + pnr + "," + num + ",'" + pass + "'," + train_id + ",'"
                                    + date + "','" + coach_type + "'," + avail_s + "," + book_s + ");";
                        }
                        

                            // System.out.println(query_call_book);
                            stmt = connection.createStatement();
                            stmt.execute(query_call_book);
                            String q2="COMMIT;";
                            stmt.execute(q2);

                    }

                    else 
                    {
                        printWriter.println("Tickets cannot be booked.");
                        stmt = connection.createStatement();    
                        String q2="COMMIT;";
                        stmt.execute(q2);
                    }

                    // Returning query for booked tickets. 
                    String pnt_str = pnr.toString();
                    String booked_tickets = "SELECT * FROM ticket WHERE pnr = " + pnt_str;
                    
                    ArrayList<String> responses = new ArrayList<String>();
                    
                    
                    stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(booked_tickets);
                    
                        while (rs.next()) 
                        {
                            int coach_no = rs.getInt("coach_no");
                            int berth_no = rs.getInt("berth_no");
                            String berth_type = rs.getString("berth_type");
                            String res_string = "PNR -> "+pnt_str + ", " +" Date -> "+ date+ ", " +" Coach_no -> "+ coach_no + ", "+ " Coach_type -> " +coach_type +", " +" Berth_no -> "+berth_no +", " +" Berth_type -> "+berth_type;
                            printWriter.println(res_string);
                        }
                    
                    
                    // Dummy response send to client
                    responseQuery = String.join("\n", responses);
                    // Sending data back to the client
                    return 1;
                    // Read next client query
                    }
            catch(Exception e)
            {
                e.printStackTrace();
                return 1;
            }
        
    }
    public void run() {

        Connection connection = null;

        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/db", "postgres", "pass");
            if (connection != null) {
                connection.setTransactionIsolation(2);
                System.out.println("connection OK");
            } else {
                System.out.println("connection FAILED");
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        try {
            // Reading data from client
            InputStreamReader inputStream = new InputStreamReader(socketConnection
                    .getInputStream());
            BufferedReader bufferedInput = new BufferedReader(inputStream);
            OutputStreamWriter outputStream = new OutputStreamWriter(socketConnection
                    .getOutputStream());
            BufferedWriter bufferedOutput = new BufferedWriter(outputStream);
            PrintWriter printWriter = new PrintWriter(bufferedOutput, true);
            String clientCommand = "";
            
            // Read client query from the socket endpoint
            clientCommand = bufferedInput.readLine();
            while (!clientCommand.equals("#")) {

            //   //  System.out.println("Recieved data <" + clientCommand + "> from client : "
            //             + socketConnection.getRemoteSocketAddress().toString());

                /***************
                 * Your DB code goes here
                 * 
                 * 
                 * fn booking;
                 * 
                 * resultset;
                 * 
                 * 
                 * print output file me -->
                 * 
                 * 
                 * 
                 * 
                 * 
                 * 
                 ****************/

                book(clientCommand, printWriter, connection);
                clientCommand = bufferedInput.readLine();
            }
            inputStream.close();
            bufferedInput.close();
            outputStream.close();
            bufferedOutput.close();
            printWriter.close();
            socketConnection.close();
        } catch (IOException e) {
            return;
        }
    }
}

/**
 * Main Class to controll the program flow
 */
public class ServiceModule {
    // Server listens to port
    static int serverPort = 7008;
    // Max no of parallel requests the server can process
    static int numServerCores = 50;

    // ------------ Main----------------------
    public static void main(String[] args) throws IOException {
        // Creating a thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(numServerCores);

        try (// Creating a server socket to listen for clients
                ServerSocket serverSocket = new ServerSocket(serverPort)) {
            Socket socketConnection = null;

            // Always-ON server
            while (true) {
                System.out.println("Listening port : " + serverPort + "\nWaiting for clients...");
                System.out.println("working");
                socketConnection = serverSocket.accept(); // Accept a connection from a client
                System.out.println("Accepted client :"
                        + socketConnection.getRemoteSocketAddress().toString()
                        + "\n");
                // Create a runnable task
                Runnable runnableTask = new QueryRunner(socketConnection);
                // Submit task for execution
                executorService.submit(runnableTask);
            }
        }
    }
}