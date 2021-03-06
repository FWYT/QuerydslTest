import com.mysema.commons.lang.Pair;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.*;
import com.querydsl.sql.types.LocalDateTimeType;
import com.querydsl.sql.types.LocalTimeType;
import org.joda.time.LocalDate;

import org.postgresql.ds.PGPoolingDataSource;


import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.inject.Provider;

import static com.querydsl.sql.SQLExpressions.all;

/**
 * Created by frances on 6/9/16.
 */
public class main {

    static HashMap<Integer, String> types = new HashMap<Integer, String>();
    //static HashMap<String, Integer> colTypes = new HashMap<String, Integer>();

    static String query(PGPoolingDataSource dataSource, Connection c)
    {
        System.out.println("Start query \n");
        SQLTemplates templates = new PostgreSQLTemplates();
        Configuration config = new Configuration(templates);
        config.setUseLiterals(true);

        SQLQueryFactory qF = new SQLQueryFactory(config, dataSource);
        SQLQuery sq = new SQLQuery(c, config);

        PathBuilder<Object> playPath = new PathBuilder<Object>(Object.class, "playground");
        Path<String> typePath = Expressions.path(String.class, playPath, "type");
        StringPath locationPath = Expressions.stringPath(playPath, "location");
        StringPath colorPath = Expressions.stringPath(playPath, "color");
        NumberPath<Integer> idPath = Expressions.numberPath(Integer.class, playPath, "id");
        DatePath<LocalDate> datePath = Expressions.datePath(LocalDate.class, playPath, "install_date");
        StringPath uuidPath = Expressions.stringPath(playPath, "uuid");
        StringPath text = Expressions.stringPath(playPath, "comment");
        DateTimePath<Timestamp> timePath = Expressions.dateTimePath(Timestamp.class, playPath, "time_stamp");
        BooleanPath tfPath = Expressions.booleanPath(playPath, "trueFalse");
        NumberPath<Double> dotsPath = Expressions.numberPath(Double.class, playPath, "dots");




        BooleanBuilder where = new BooleanBuilder();
        HashMap<String,Pair<String,String>> filters = new HashMap<String,Pair<String,String>>();
        filters.put("type", new Pair("slide", "="));
        filters.put("location", new Pair("east", "="));
        filters.put("color", new Pair("yellow", "="));
        filters.put("install_date", new Pair("2009-02-09", ">"));

        Operator op = Ops.LOE;
        Predicate p = Expressions.predicate(op, timePath, Expressions.constant(filters.get("install_date").getFirst()));

        sq.select(all).from(playPath).where(p).orderBy(colorPath.asc()).limit(10);
        String q = sq.getSQL().getSQL();
        System.out.println(q);
        //where.and(typePath.);
        //where.and(colorPath.eq(filters.get("color")));
        //where.and(locationPath.eq(filters.get("location")));
        where.and(p);

        BooleanBuilder sort = new BooleanBuilder();
        HashMap<String, String> sorts = new HashMap<String, String>();
        sorts.put("type", "asc");
        sorts.put("location", "desc");
        sorts.put("color", "desc");
        sorts.put("date", "asc");

        SQLQuery b = qF.select(all).from(playPath).where(where).orderBy(datePath.asc());
        //b.orderBy(locationPath.asc());
        //b.orderBy(colorPath.asc());

        //System.out.println(b.getSQL().getSQL());




        //List<Tuple> res = qF.select(idPath, locationPath, colorPath, typePath, datePath).from(playPath).where(timePath.stringValue().eq("2016-06-03 18:22:45.64+00")).fetch();


        //SQLBindings bindings = qF.select(all).from(playPath).where(where).getSQL();
                //.orderBy(locationPath.asc()).limit(10).getSQL();

        //System.out.println(bindings.getSQL()+"\n");

       /* for (int i = 0; i<res.size();i++)
        {
            System.out.println(res.get(i)+"\n");
        }*/


        return "Results done!";
    }

    static Operator getOp(String cmp) //note: key is to the left and value is to the right. So key <cmp> value.
    {
        if (cmp == "=")
        {
            return Ops.EQ;
        }
        else if (cmp == "!=")
        {
            return Ops.NE;
        }
        else if (cmp == ">")
        {
            return Ops.GT;
        }
        else if (cmp == ">=")
        {
            return Ops.GOE;
        }
        else if (cmp == "<")
        {
            return Ops.LT;
        }
        else if (cmp == "<=")
        {
            return Ops.LOE;
        }
        else
        {
            System.out.println("Invalid comparison operator.");
            System.exit(1);
            return null;
        }
    }

    static BooleanBuilder returnFilterBooleanBuilder(BooleanBuilder where, String key, int tp, String value, String cmp, PathBuilder playPath)
    {
        Operator op = getOp(cmp);
        Predicate p = null;
        switch (tp)
        {
            case 12: //varchar
                StringPath sp = Expressions.stringPath(playPath, key);
                p = Expressions.predicate(op, sp, Expressions.constant(value));
                break;
            case 8: //double
                NumberPath<Double> fp = Expressions.numberPath(Double.class, playPath, key);
                p = Expressions.predicate(op, fp, Expressions.constant(value));
                break;
            case 4: //integer
                NumberPath<Integer> np = Expressions.numberPath(Integer.class, playPath, key);
                p = Expressions.predicate(op, np, Expressions.constant(value));
                break;
            case 2013: //Time with timezone
            case 2014: //timestamp with time zone
            case 93: //timestamp
                DateTimePath<Timestamp> dtp = Expressions.dateTimePath(Timestamp.class, playPath, key);
                p = Expressions.predicate(op, dtp, Expressions.constant(value));
                break;
            case 91: //date
                DatePath<LocalDate> dp = Expressions.datePath(LocalDate.class, playPath, key);
                p = Expressions.predicate(op, dp, Expressions.constant(value));
            case 16: //boolean
            case -7: //boolean BIT
                BooleanPath tfPath = Expressions.booleanPath(playPath, key);
                if (value == "true")
                {
                    p = Expressions.predicate(op, tfPath, Expressions.constant(true));
                }
                else
                {
                    p = Expressions.predicate(op, tfPath, Expressions.constant(false));
                }
                break;
            case 1111: //other, aka UUID
                StringPath otp = Expressions.stringPath(playPath, key);
                p = Expressions.predicate(op, otp, Expressions.constant(value));
                break;
        }
        if (p == null)
        {
            System.out.println("Data type doesn't exist");
            System.exit(1);
        }
        return where.and(p);
    }

    static SQLQuery returnSortBooleanBuilder(SQLQuery sql, String key, int value, PathBuilder playPath, HashMap<String,Integer> colTypes)
    {
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
            case 8: //double
                NumberPath<Double> fp = Expressions.numberPath(Double.class, playPath, key);
                if (value == 1)
                {
                    sql.orderBy(fp.asc());
                }
                else
                {
                    sql.orderBy(fp.desc());
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
            case 16: //boolean
            case -7: //boolean BIT
                BooleanPath tfPath = Expressions.booleanPath(playPath, key);
                if (value == 1)
                {
                    sql.orderBy(tfPath.asc());
                }
                else
                {
                    sql.orderBy(tfPath.desc());
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

        return sql;
    }

    static boolean validColumn(String col, HashMap<String,Integer> colTypes)
    {
        return colTypes.get(col) != null;
    }

    static boolean validValue(String value, int type)
    {
        String mm = "((0?[1-9])|1[012])"; //1-9 or 10,11,12
        String dd = "((0?[1-9])|[12][0-9]|3[01])"; //1-9 or 10-29 or 30-31
        String yyyy = "([0-9][0-9][0-9][0-9])"; //00-99
        String c = "[-/]";
        String time = "(((0?[0-9])|1[0-9]|2[0-4]):([0-5][0-9]):([0-5][0-9]))";

        switch (type) {
            case 12: //varchar
                return true;
            case 8: //double
                if (!value.matches("^[-]?[0-9]*([.])?[0-9]*$")) {
                    System.out.println("Not valid double.");
                    return false;
                }
                return true;
            case 4: //integer
                if (!value.matches("^[-]?[0-9]+$")) {
                    System.out.println("Not valid integer");
                    return false;
                }
                return true;
            case 2013: //Time with timezone
                if (!value.matches("^" + time+"(.*)?"))
                {
                    System.out.println("Not valid timestamp.");
                    return false;
                }
                return true;

            case 93: //timestamp
                if (!value.matches("^"+mm+c+dd+c+yyyy + "((\\s)" + time +"(.*))?") && !value.matches("^"+yyyy+c+mm+c+dd + "((\\s)" + time +"(.*))?"))
                {
                    System.out.println("Not valid timestamp.");
                    return false;
                }
                return true;
            case 2014: //timestamp with time zone //must have date
                if (!value.matches("^"+mm+c+dd+c+yyyy + "((\\s)" + time +"(.*))?") && !value.matches("^"+yyyy+c+mm+c+dd + "((\\s)" + time +"(.*))?"))
                {
                    System.out.println("Not valid timestamp.");
                    return false;
                }
                return true;
            case 91: //date
                //mm/dd/yyyy, first number always becomes month
                if (!value.matches("^"+mm+c+dd+c+yyyy+"$") && !value.matches("^"+yyyy+c+mm+c+dd+"$"))
                {
                    System.out.println("Not valid date. Make sure numbers are within appropriate bounds. Valid format: mm/dd/yyyy or mm-dd-yyyy or yyyy-mm-dd or yyyy/mm/dd.");
                    return false;
                }
                return true;
            case 16: //boolean
            case -7: //boolean BIT
                boolean b = (value == "true" || value == "false");
                if (!b)
                {
                    System.out.println("Wrong boolean format. Write t for true and f for false.");
                }
                return b;

            case 1111: //other, aka UUID
                //format 8-4-4-4-12, alphanumeric
                String[] splitted = value.split("-");
                for (int i =0; i<splitted.length;i++) {
                    if (i == 0)
                    {
                        if (splitted[i].length() != 8 || !splitted[i].matches("[A-Za-z0-9]+"))
                        {
                            System.out.println("Not valid UUID format.");
                            return false;
                        }
                    }
                    else if (i>0 && i<4)
                    {
                        if (splitted[i].length() != 4 || !splitted[i].matches("[A-Za-z0-9]+"))
                        {
                            System.out.println("Not valid UUID format.");
                            return false;
                        }
                    }
                    else if (i == 4)
                    {
                        if (splitted[i].length() != 12 || !splitted[i].matches("[A-Za-z0-9]+"))
                        {
                            System.out.println("Not valid UUID format.");
                            return false;
                        }
                    }
                }
                return true;
        }
        return true;
    }

    public String getQuery(HashMap<String,Pair<String, String>> filter, LinkedHashMap<String,Integer> sort, int limit, Connection c)
    {
        System.out.println("Start query \n");

        if (limit<=0)
        {
            System.out.println("Limit must be greater than 0.");
            return "";
        }

        SQLTemplates templates = new PostgreSQLTemplates().builder().printSchema().build();
        Configuration config = new Configuration(templates);
        config.setUseLiterals(true); //shows schema in sql query

        //SQLQueryFactory qF = new SQLQueryFactory(config, dataSource);
        SQLQuery qF = new SQLQuery(c,config);

        //create map between table columns and data type
        HashMap<String,Integer> colTypes = generateMaps(c);

        //path to the table
        PathBuilder<Object> tablePath = new PathBuilder<Object>(Object.class, "bnr.bnr_out_current");

        //create filter predicate
        BooleanBuilder where = new BooleanBuilder();

        for (Map.Entry<String, Pair<String, String>> entry : filter.entrySet())
        {
            String key = entry.getKey(); //column name
            String value = entry.getValue().getFirst(); //what it should be equal to
            String cmp = entry.getValue().getSecond(); //the comparison operator

            //check that column is valid
            if (!validColumn(key, colTypes))
            {
                System.out.println("Invalid column name. Check case sensitivity and spelling.");
                return "";
            }

            int tp = colTypes.get(key);

            //check value against data type
            if (!validValue(value, tp))
            {
                System.out.println("Invalid value. Make sure filter values are of correct syntax.");
                return "";
            }
            where = returnFilterBooleanBuilder(where, key, tp, value, cmp, tablePath);
        }

        qF.select(all).from(tablePath).where(where).limit(limit);

        //create sort predicate
        for (Map.Entry<String, Integer> entry : sort.entrySet())
        {
            String key = entry.getKey(); //column name
            Integer value = entry.getValue(); //1 = asc, -1 = desc

            if (!validColumn(key, colTypes))
            {
                System.out.println("Invalid column name. Check case sensitivity and spelling.");
                return "";
            }
            if (value != -1 && value != 1)
            {
                System.out.println("Invalid input value for sort. -1 = descending, 1 = ascending.");
                return "";
            }

            qF = returnSortBooleanBuilder(qF, key, value, tablePath, colTypes);
        }

        //generate query string
        String query = qF.getSQL().getSQL();

        //get rid of double quotes due to schema
        query = query.replace("\"", "");

        System.out.println("\n" + query +"\n");
        return query;
    }

    static void insertRandom(Connection c)
    {
        try {
            List<String> type = Arrays.asList("swing", "slide", "seesaw", "basketball", "tetherball", "rope", "sandbox");
            List<String> color = Arrays.asList("yellow", "black", "red", "blue", "green", "purple", "brown");
            List<String> location = Arrays.asList("north", "east", "south", "west");
            List<String> b = Arrays.asList("true", "false");


            //random inserts
            Statement stmt = null;
            stmt = c.createStatement();
            Random rand = new Random();
            String sql;

            for (int i = 0; i < 20; i++) {
                int n = rand.nextInt(type.size());
                int m = rand.nextInt(color.size());
                int o = rand.nextInt(location.size());
                int month = rand.nextInt(12) + 1;
                int day = rand.nextInt(29) + 1;
                int year = rand.nextInt(16) + 1;
                UUID uuid = UUID.randomUUID();
                String u = uuid.toString();
                String com = UUID.randomUUID().toString();
                int tf = rand.nextInt(2);
                sql = "INSERT INTO playground (type,location,color,install_date, uuid, comment, trueFalse) VALUES ('" + type.get(n) + "','" + location.get(o) +
                        "','" + color.get(m) + "','" + month + "-" + day + "-" + year + "','" + u + "','" + com + "','" +
                        b.get(tf) + "');";
                stmt.executeUpdate(sql);
                ;
            }
            stmt.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
    }

    static void printAll(Connection c)
    {
        try {
            Statement stmt = null;
            stmt = c.createStatement();
            ResultSet sql = stmt.executeQuery("SELECT * FROM playground");
            //ResultSet sql = stmt.executeQuery("select * from playground where cast(playground.install_date as varchar) >= '2015-02-09' order by playground.install_date asc");

            while (sql.next()) {
                int id = sql.getInt("id");
                System.out.println("id: " + id);
                System.out.println("type: " + sql.getString("type"));
                System.out.println("color: " + sql.getString("color"));
                System.out.println("location: " + sql.getString("location"));
                System.out.println("install date: " + sql.getDate("install_date"));
                System.out.println("\n");
            }

            sql.close();
            stmt.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
    }

    static HashMap<String,Integer> generateMaps(Connection c)
    {
        HashMap<String, Integer> colTypes = new HashMap<String, Integer>();
        try {
            DatabaseMetaData dbmd = c.getMetaData();
            ResultSet cols = dbmd.getColumns(null, null, "playground", "%");

            Field[] fields = java.sql.Types.class.getFields();

            //make type map
            for (int i = 0; i < fields.length; i++) {
                Integer val = (Integer) fields[i].get(null);
                String name = fields[i].getName();
                types.put(val, name);
            }

            while (cols.next()) {
                String name = cols.getString("COLUMN_NAME");

                int dataType = cols.getShort("DATA_TYPE");
                colTypes.put(name, dataType);
            }
           // System.out.println(colTypes);
            System.out.println(types+"\n");


        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        System.out.println(types+"\n");
        return colTypes;
    }

    public static void main(String[] args)
    {
        System.out.println("Hello World\n");

        //Set up datasource for connection and queryFactory
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

            //generate number to sql type map and column to number map
            //don't need number to sql map for actual usability
            generateMaps(c);

            HashMap<String,Pair<String,String>> filters = new HashMap<String,Pair<String,String>>();
            //filters.put("type", "swing");
            //filters.put("location", "east");
            filters.put("color", new Pair("red","="));
            filters.put("install_date", new Pair("2015-5-2",">"));
            //filters.put("time_stamp", new Pair("2016-06-07 16:47:58.642+00", ">="));
            //filters.put("uuid","bd3a1773-e047-4394-a4b8-c984f0232410");
            //filters.put("truefalse", new Pair("false", "="));
            //filters.put("dots", new Pair("3.5", "="));
            //filters.put("lines", "4");

            //System.out.println(colTypes.get("trueFalse"));

            LinkedHashMap<String, Integer> sorts = new LinkedHashMap<String, Integer>();
            //sorts.put("type", 1);
            //sorts.put("location", -1);
            sorts.put("color", -1);
            sorts.put("install_date", 1);
            //sorts.put("dots", 1);

            //validValue("2016-06-09 13:12:51+00",2014);

            query(dataSource, c);
            //getQuery(filters, sorts, 10, c);
            //insertRandom(c);
            //printAll(c);

            //System.out.println(getOp("p"));

            c.close();
        } catch (Exception e)
        {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }


    }
}
