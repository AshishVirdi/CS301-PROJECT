import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;



public class insert_train 
{

   public static void main(String[] args) 
   {
    /*
    22517 2022-11-01 999 999
    04652 2022-11-01 999 999
    22517 2022-11-02 999 999
    04652 2022-11-02 999 999
    12058 2022-11-01 999 999
    #
    */

  
  try(
    FileInputStream fstream = new FileInputStream("booking.txt");  
    BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
  )
  {
  String line;

  //Read File Line By Line
  while ((line = br.readLine()) != null && !line.equals("#"))  
  {
      StringTokenizer st = new StringTokenizer(line, ", ");
      int train_id = Integer.parseInt(st.nextToken());
      String date =  st.nextToken();
      int num_coaches_ac = Integer.parseInt(st.nextToken());
      int num_coaches_sl = Integer.parseInt(st.nextToken());

        String QUERY = "call add_train(" +train_id+",'"+date +"',"+ num_coaches_ac +","+ num_coaches_sl + ");";
      
         try(Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/db", "postgres", "pass");
            Statement stmt = conn.createStatement();
            ) 
            {
              stmt.execute(QUERY);		      
            } 
            catch (SQLException e) {
            e.printStackTrace();
            } 
   }

   fstream.close();
  }
  catch (Exception e) 
  {
    e.printStackTrace();
  }
    
}
  
}