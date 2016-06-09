import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathImpl;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.*;
import org.joda.time.LocalDate;
import org.postgresql.ds.PGPoolingDataSource;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by frances on 6/9/16.
 */
public class main {

    static String query(PGPoolingDataSource dataSource)
    {
        System.out.println("Start query \n");
        SQLTemplates templates = new PostgreSQLTemplates();
        Configuration config = new Configuration(templates);

        SQLQueryFactory qF = new SQLQueryFactory(config, dataSource);

        PathBuilder<Object> playPath = new PathBuilder<Object>(Object.class, "playground");
        StringPath typePath = Expressions.stringPath(playPath, "type");
        StringPath locationPath = Expressions.stringPath(playPath, "location");
        StringPath colorPath = Expressions.stringPath(playPath, "color");
        NumberPath<Integer> idPath = Expressions.numberPath(Integer.class, playPath, "id");
        DatePath<LocalDate> datePath = Expressions.datePath(LocalDate.class, playPath, "install_date");

        BooleanBuilder where = new BooleanBuilder();
        HashMap<String,String> filters = new HashMap<String,String>();
        filters.put("type", "slide");
        filters.put("location", "east");
        filters.put("color", "yellow");

        where.and(typePath.eq(filters.get("type")));
        //where.and(colorPath.eq(filters.get("color")));
        //where.and(locationPath.eq(filters.get("location")));

        BooleanBuilder sort = new BooleanBuilder();
        HashMap<String, String> sorts = new HashMap<String, String>();
        sorts.put("type", "asc");
        sorts.put("location", "desc");
        sorts.put("color", "desc");
        sorts.put("date", "asc");





        List<Tuple> res = qF.select(idPath, locationPath, colorPath, typePath, datePath).from(playPath).where(where).orderBy(datePath.desc()).fetch();



     
        //SQLQuery res = new SQLQuery(c, config).from(playPath).where(typePath.eq("swing"));
        //String q = res.getSQL().getSQL();

        for (int i = 0; i<res.size();i++)
        {
            System.out.println(res.get(i)+"\n");
        }

        return "Results done!";
    }

    static void queryGeneric(HashMap<String,String> filter, HashMap<String,String> sort, int limit, PGPoolingDataSource dataSource)
    {
        System.out.println("Start generic query \n");
        SQLTemplates templates = new PostgreSQLTemplates();
        Configuration config = new Configuration(templates);

        SQLQueryFactory qF = new SQLQueryFactory(config, dataSource);
    }

    public static void main(String[] args)
    {
        System.out.println("Hello World\n");

        PGPoolingDataSource dataSource = new PGPoolingDataSource();
        dataSource.setDataSourceName("Playground Data Source");
        dataSource.setServerName("localhost");
        dataSource.setDatabaseName("frances");
        dataSource.setUser("frances");
        dataSource.setPassword("frances");
        dataSource.setMaxConnections(10);

        Connection c = null;
        try{
            Class.forName("org.postgresql.Driver");
            //c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/frances", "frances", "frances");
            c = dataSource.getConnection();
            System.out.println("Opened Database Successfully!\n");


            query(dataSource);

            /*
            List<String> type = Arrays.asList("swing", "slide", "seesaw", "basketball", "tetherball", "rope", "sandbox");
            List<String> color = Arrays.asList("yellow", "black", "red", "blue", "green", "purple", "brown");
            List<String> location = Arrays.asList("north", "east", "south", "west");


            //random inserts
            Statement stmt = null;
            stmt=c.createStatement();
            Random rand = new Random();
            String sql;

            for (int i = 0; i<10; i++)
            {
                int n = rand.nextInt(type.size());
                int m = rand.nextInt(color.size());
                int o = rand.nextInt(location.size());
                int month = rand.nextInt(12)+1;
                int day = rand.nextInt(31)+1;
                int year = rand.nextInt(16)+1;
                sql = "INSERT INTO playground (type,location,color,install_date) VALUES ('"+type.get(n)+"','"+location.get(o)+
                        "','" + color.get(m)+"','"+month+"-"+day+"-"+year+"');";
                stmt.executeUpdate(sql);
;            }
            stmt.close();*/



            /*
            //select all
            Statement stmt = null;
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
                System.out.println("\n");
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
