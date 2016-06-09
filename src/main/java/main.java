import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathImpl;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.*;
import com.querydsl.sql.types.LocalDateTimeType;
import com.querydsl.sql.types.LocalTimeType;
import org.joda.time.LocalDate;

import org.postgresql.ds.PGPoolingDataSource;


import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

import static com.querydsl.sql.SQLExpressions.all;

/**
 * Created by frances on 6/9/16.
 */
public class main {

    static HashMap<Integer, String> types = new HashMap<Integer, String>();
    static HashMap<String, Integer> colTypes = new HashMap<String, Integer>();

    static String query(PGPoolingDataSource dataSource, Connection c)
    {
        System.out.println("Start query \n");
        SQLTemplates templates = new PostgreSQLTemplates();
        Configuration config = new Configuration(templates);
        config.setUseLiterals(true);

        SQLQueryFactory qF = new SQLQueryFactory(config, dataSource);

        PathBuilder<Object> playPath = new PathBuilder<Object>(Object.class, "playground");
        StringPath typePath = Expressions.stringPath(playPath, "type");
        StringPath locationPath = Expressions.stringPath(playPath, "location");
        StringPath colorPath = Expressions.stringPath(playPath, "color");
        NumberPath<Integer> idPath = Expressions.numberPath(Integer.class, playPath, "id");
        DatePath<LocalDate> datePath = Expressions.datePath(LocalDate.class, playPath, "install_date");
        StringPath uuidPath = Expressions.stringPath(playPath, "uuid");
        StringPath text = Expressions.stringPath(playPath, "comment");
        DateTimePath<Timestamp> timePath = Expressions.dateTimePath(Timestamp.class, playPath, "time_stamp");



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

        SQLQuery b = qF.select(all).from(playPath).where(timePath.stringValue().eq("2016-06-03 18:22:45.64+00")).limit(10);
        b.orderBy(locationPath.asc());
        b.orderBy(colorPath.asc());

        System.out.println(b.getSQL().getSQL());




        //List<Tuple> res = qF.select(idPath, locationPath, colorPath, typePath, datePath).from(playPath).where(timePath.stringValue().eq("2016-06-03 18:22:45.64+00")).fetch();


        //SQLBindings bindings = qF.select(all).from(playPath).where(timePath.stringValue().eq("2016-06-03 18:22:45.64+00"))
                //.orderBy(locationPath.asc()).limit(10).getSQL();

        //System.out.println(bindings.getSQL()+"\n");

       /* for (int i = 0; i<res.size();i++)
        {
            System.out.println(res.get(i)+"\n");
        }*/


        return "Results done!";
    }

    static void queryGeneric(HashMap<String,String> filter, HashMap<String,Integer> sort, int limit, PGPoolingDataSource dataSource, Connection c)
    {
        System.out.println("Start generic query \n");
        SQLTemplates templates = new PostgreSQLTemplates();
        Configuration config = new Configuration(templates);
        config.setUseLiterals(true);

        SQLQueryFactory qF = new SQLQueryFactory(config, dataSource);

        //path to the table
        PathBuilder<Object> playPath = new PathBuilder<Object>(Object.class, "playground");

        Iterator filterI = filter.entrySet().iterator();
        BooleanBuilder where = new BooleanBuilder();

        while (filterI.hasNext())
        {
            Map.Entry entry = (Map.Entry)filterI.next();
            String key = (String)entry.getKey(); //column name
            String value = (String)entry.getValue(); //what it should be equal to

            int tp = colTypes.get(key);

            switch (tp)
            {
                case 12: //varchar
                    StringPath sp = Expressions.stringPath(playPath, key);
                    where.and(sp.eq(value));
                    break;
                case 4: //integer
                    NumberPath<Integer> np = Expressions.numberPath(Integer.class, playPath, key);
                    where.and(np.eq(Integer.parseInt(value)));
                    break;
                case 2013: //Time with timezone
                case 2014: //timestamp with time zone
                case 93: //timestamp
                    DateTimePath<Timestamp> dtp = Expressions.dateTimePath(Timestamp.class, playPath, key);
                    where.and(dtp.stringValue().eq(value));
                    break;
                case 91: //date
                    DatePath<LocalDate> dp = Expressions.datePath(LocalDate.class, playPath, key);
                    where.and(dp.stringValue().eq(value));
                    break;
                case 1111: //other, aka UUID
                    StringPath op = Expressions.stringPath(playPath, key);
                    where.and(op.eq(value));
                    break;
            }

        }

        SQLQuery sql = qF.select(all).from(playPath).where(where).limit(limit);

        Iterator sortI = sort.entrySet().iterator();

        while (sortI.hasNext())
        {
            Map.Entry entry = (Map.Entry)sortI.next();
            String key = (String)entry.getKey(); //column name
            Integer value = (Integer)entry.getValue(); //1 = asc, -1 = desc

            switch (colTypes.get(key))
            {
                case 12: //varchar
                    StringPath sp = Expressions.stringPath(playPath, key);
                    if (value == 1)
                    {
                        sql.orderBy(sp.asc());
                    }
                    else
                    {
                        sql.orderBy(sp.desc());
                    }
                    break;
                case 4: //integer
                    NumberPath<Integer> np = Expressions.numberPath(Integer.class, playPath, key);
                    if (value == 1)
                    {
                        sql.orderBy(np.asc());
                    }
                    else
                    {
                        sql.orderBy(np.desc());
                    }
                    break;
                case 2013: //Time with timezone
                case 2014: //timestamp with time zone
                case 93: //timestamp
                    DateTimePath<Timestamp> dtp = Expressions.dateTimePath(Timestamp.class, playPath, key);
                    if (value == 1)
                    {
                        sql.orderBy(dtp.asc());
                    }
                    else
                    {
                        sql.orderBy(dtp.desc());
                    }
                    break;
                case 91: //date
                    DatePath<LocalDate> dp = Expressions.datePath(LocalDate.class, playPath, key);
                    if (value == 1)
                    {
                        sql.orderBy(dp.asc());
                    }
                    else
                    {
                        sql.orderBy(dp.desc());
                    }
                    break;
                case 1111: //other, aka UUID
                    StringPath op = Expressions.stringPath(playPath, key);
                    if (value == 1)
                    {
                        sql.orderBy(op.asc());
                    }
                    else
                    {
                        sql.orderBy(op.desc());
                    }
                    break;
            }

        }

        //generate query string
        SQLBindings bindings = sql.getSQL();

        System.out.println(bindings.getSQL()+"\n");
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


            //finding type of column
            DatabaseMetaData dbmd = c.getMetaData();
            ResultSet cols = dbmd.getColumns(null,null,"playground", "%");

            Field[] fields = java.sql.Types.class.getFields();

            //make type map
            for (int i = 0; i<fields.length;i++)
            {
                Integer val = (Integer)fields[i].get(null);
                String name = fields[i].getName();
                types.put(val,name);
            }

            while(cols.next())
            {
                String name = cols.getString("COLUMN_NAME");

                int dataType = cols.getShort("DATA_TYPE");
                colTypes.put(name, dataType);
            }
            System.out.println(colTypes);
            System.out.println(types);




            HashMap<String,String> filters = new HashMap<String,String>();
            filters.put("type", "slide");
            //filters.put("location", "east");
            //filters.put("color", "yellow");
            //filters.put("install_date", "2016-08-09");
            //filters.put("time_stamp","2016-06-07 16:47:58.642+00");
            filters.put("uuid","bd3a1773-e047-4394-a4b8-c984f0232410");

            HashMap<String, Integer> sorts = new HashMap<String, Integer>();
            sorts.put("type", 1);
            //sorts.put("location", -1);
            //sorts.put("color", -1);
            //sorts.put("date", 1);

            //query(dataSource, c);

            queryGeneric(filters, sorts, 10, dataSource, c);

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
