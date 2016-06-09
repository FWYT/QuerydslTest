import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathImpl;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.sql.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * Created by frances on 6/9/16.
 */
public class main {

    static String query(Connection c)
    {
        System.out.println("Start query \n");
        SQLTemplates templates = new PostgreSQLTemplates();
        Configuration config = new Configuration(templates);

        PathBuilder<Object> playPath = new PathBuilder<Object>(Object.class, "playground");
        StringPath typePath = Expressions.stringPath(playPath, "type");
     
        SQLQuery res = new SQLQuery(c, config).from(playPath).where(typePath.eq("swing"));
        String q = res.getSQL().getSQL();

        System.out.println(q);
    }
    public static void main(String[] args)
    {
        System.out.println("Hello World");

        Connection c = null;
        try{
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/frances", "frances", "frances");

            System.out.println("Opened Database Successfully!");


            query(c);

            /*Statement stmt = null;
            stmt = c.createStatement();
            ResultSet sql = stmt.executeQuery("SELECT * FROM playground");

            while (sql.next())
            {
                int id = sql.getInt("id");
                System.out.println("id: " + id);
                System.out.println("type: " + sql.getString("type"));
                System.out.println("color: " + sql.getString("color"));
                System.out.println("location: " + sql.getString("location"));
                System.out.println("install date: " + sql.getDate("install_date"));
            }

            sql.close();
            stmt.close();*/
            c.close();
        } catch (Exception e)
        {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }


    }
}
