import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class XMLDOMParser {
    public XMLDOMParser() {
        String temp  = "";
    }

    private static final Map<String, String> genreByCat = new HashMap<String, String>() {{
        put("susp", "Thriller"); put("cnr", "Cops and Robbers"); put("advt", "Adventure"); put("dram", "Drama");
        put("west", "Western"); put("myst", "Mystery"); put("s.f.", "Sci-Fi"); put("horr", "Horror"); put("romt", "Romance");
        put("comd", "Comedy"); put("docu", "Documentary"); put("porn", "Pornography");
        put("noir", "Black"); put("biop", "Biographical Picture"); put("tv", "TV Show"); put("tvs", "TV Series"); put("tvm", "TV Miniseries");
        put("ctxx", "Unknown"); put("avga", "Avant Garde"); put("hist", "History"); put("cart", "Cartoon");
        put("epic", "Epic"); put("disa", "Disaster"); put("scfi", "Sci-Fi");
        put("surl", "Surreal"); put("camp", "Now - Camp"); put("faml", "Family"); put("actn", "Action");
        put("fant", "Fantasy"); put("musc", "Musical"); put("", "Unknown"); put("cnrb", "Cops and Robbers"); put("cult", "Cult");
    }};
    private static Map<String, String> movieSet = new HashMap<String, String>();
    private static Map<String, String> starSet = new HashMap<String, String>();
    private static Map<String, String> dirSet = new HashMap<String, String>();
    String loginUser = "mytestuser";
    String loginPasswd = "mypassword";
    String serverName = "localhost";
    String loginUrl = "jdbc:mysql://" + serverName + ":3306/moviedb";
    Document dom;
    static int maxStarID = 0;

    public static void main(String[] args) {
        try {


            populateMovieSet();
            getMaxStarID();
//            System.out.println(maxStarID);

            long startTimeMParse = System.nanoTime();
            File movieXML = new File("src/mains243.xml");
            parseMovieDocument(movieXML);

            File actorXML = new File("src/actors63.xml");
            parseActorsDocument(actorXML);

            movieSet.clear();
            dirSet.clear();
            starSet.clear();

            populateMovieSet();
            getMaxStarID();

            File castsXML = new File("src/casts124.xml");
            parseCastDocument(castsXML);
            long totalTime = System.nanoTime() - startTimeMParse;
            System.out.println(totalTime /1000000000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void populateMovieSet() throws SQLException, IOException {
        Connection connection = getConnection();
        String query = String.format("select * from movies as m");
        PreparedStatement prep = connection.prepareStatement(query);
        ResultSet rs = prep.executeQuery(query);
        String movieID = "";
        String movieName = "";
        String dirName = "";
        while (rs.next()) {
            movieID = rs.getString("id");
            movieName = rs.getString("title");
            dirName = rs.getString("director");
            movieSet.put(movieID, movieName.toLowerCase());
            dirSet.put(movieID, dirName.toLowerCase());
        }
        rs.close();
        query = String.format("select * from stars");
        PreparedStatement stars = connection.prepareStatement(query);
        ResultSet rsr = stars.executeQuery(query);
        String starID = "";
        String starName = "";
        while (rsr.next()) {
            starID = rsr.getString("id");
            starName = rsr.getString("name");
            starSet.put(starID, starName.toLowerCase());
        }
        rsr.close();
        connection.close();

    }


    public static void parseCastDocument(File castXML) throws ParserConfigurationException, IOException, SAXException, SQLException {
        //get the root element

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(castXML);

        doc.getDocumentElement().normalize();
        Element docEle = doc.getDocumentElement();
        int[] iNoRows = null;
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        String squery = "INSERT INTO stars_in_movies(starId, movieId) VALUES (? , ?);";
        PreparedStatement insert = connection.prepareStatement(squery);
        NodeList directors = docEle.getElementsByTagName("dirfilms");
        for (int i = 0; i < directors.getLength(); ++i) {
            //Get a single director
            Node director = directors.item(i);
//            System.out.println(director);
            String dirname = "";

            if (director.getNodeType() == Node.ELEMENT_NODE) {
                Element directorElement = (Element) director;
                //Get director name
                if (directorElement.getElementsByTagName("is").getLength() > 0) {
                    dirname = directorElement.getElementsByTagName("is").item(0).getTextContent();
                    if (dirname.matches(".*\\d.*")) {
                        System.out.println("DIRECTOR NAME "  + dirname +  " INVALID(nonalphanumeric)");
                        continue;
                    }
                }
//                System.out.println("D-Name: (HEAD OF TREE)): " + dirname);

                // Get list of films by the director
                NodeList films = directorElement.getElementsByTagName("filmc");
                for (int j = 0; j < films.getLength(); j++) {
                    // Get a single film element
                    Element film = (Element) films.item(j);
                    String filmName = film.getElementsByTagName("t").item(0).getTextContent().replaceAll("[ ](?=[ ])|[^-_,A-Za-z0-9 ]+", "");
//                    System.out.println("FILM NAME: " + filmName);
                    String movieID = movieExists(filmName, dirname);
//                        System.out.println(movieID);
                    if (movieID.equals("")) { // movie exists in db
//                        System.out.println("MOVIE NOT IN DB, IGNORING " + filmName);
                        continue;
                    }
                    NodeList actors = film.getElementsByTagName("a");
                    for (int k = 0; k < actors.getLength(); ++k) {
                        String actor = actors.item(k).getTextContent().replaceAll("[ ](?=[ ])|[^-_,A-Za-z0-9 ]+", "");
                        if (actor.replace(" ", "").length() < 4) {
//                            System.out.println("ACTOR NAME: " + actor + " INVALID");
                            continue;
                        } else {
                            String starID = starExists(actor, connection);
                            insert.setString(1, starID);
                            insert.setString(2, movieID);
                            insert.addBatch();
                        }
                    }

                }

            }

        }

        iNoRows = insert.executeBatch();
        connection.commit();
        try {
            if (insert != null) insert.close();
            if (connection != null) connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static Connection getConnection() throws SQLException {
        String loginUser = "mytestuser";
        String loginPasswd = "mypassword";
        String serverName = "localhost";
        String loginUrl = "jdbc:mysql://" + serverName + ":3306/moviedb";
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
    }


    public static String movieExists(String movieName, String director) throws SQLException {
        if(movieSet.containsValue(movieName.toLowerCase())) {
                for (Map.Entry<String, String> entry : movieSet.entrySet()) {
                    if (movieName.toLowerCase().equals(entry.getValue())) {

                        String movieID =  entry.getKey();
                        String direcName = dirSet.get(movieID);
                        if (direcName.equals(director.toLowerCase())){
                            return movieID;
                        }
                    }
                }
            }

        return "";

    }
    public static String starExists(String starName, Connection connection) throws SQLException {
        if(starSet.containsValue(starName.toLowerCase())) {
            for (Map.Entry<String, String> entry : starSet.entrySet()) {
                if (starName.toLowerCase().equals(entry.getValue())) {
                    String starID =  entry.getKey();
                    return starID;
                    }
                }
        } else {
            String actorID = genStarID(maxStarID);
            String q = "insert into stars(id, name) values (? , ?) ";
            PreparedStatement insertStar = connection.prepareStatement(q);
            insertStar.setString(1, actorID);
            insertStar.setString(2, starName);
            insertStar.executeUpdate();
            insertStar.close();
            starSet.put(actorID, starName.toLowerCase());
            maxStarID++;
            return actorID;
        }


        return "";

    }


    private static void parseMovieDocument(File movieXML) throws ParserConfigurationException, IOException, SAXException, SQLException, ClassNotFoundException {
        //get the root element
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(movieXML);

        doc.getDocumentElement().normalize();

        Element docEle = doc.getDocumentElement();
        String loginUser = "mytestuser";
        String loginPasswd = "mypassword";
        String serverName = "localhost";
        String loginUrl = "jdbc:mysql://" + serverName + ":3306/moviedb";
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        //get a nodelist of <employee> elements
        //get the employee element

        NodeList directors = docEle.getElementsByTagName("directorfilms");

        for (int i = 0; i < directors.getLength(); i++) {
            //Get a single director
            Node director = directors.item(i);
            String dirname = "";
            //
            if (director.getNodeType() == Node.ELEMENT_NODE) {
                Element directorElement = (Element) director;
                //Get director name
                if (directorElement.getElementsByTagName("dirname").getLength() > 0) {
                    dirname = directorElement.getElementsByTagName("dirname").item(0).getTextContent();
                }


                NodeList films = directorElement.getElementsByTagName("film");
                // get list of films by the director
//                System.out.println("num of films: " + films.getLength());
                for (int j = 0; j < films.getLength(); j++) {

                    // Get a single film element
                    Element film = (Element) films.item(j);

                    //replace single quotes in title to avoid query error
                    String title = getTextValue(film, "t");

                    if (title == null) {
                        System.out.println("NO TITLE WAS PROVIDED IN LINE: " + (i + j));
                        continue;
                    }
                    title = title.replaceAll("[ ](?=[ ])|[^-_,A-Za-z0-9 ]+", "");
//                    System.out.println("-----------------");
//                    System.out.println("Movie Title: " + title);

                    //Add to hash set of film names

                    // Get or create movieId
//                            String xmlID = getTextValue(film, "fid");

                    // get new ID for inserting into table

                    String query = "select CONCAT('tt0', (substring( max(id) , 3 ) + 1) ) as newID from movies;";
                    PreparedStatement prep = connection.prepareStatement(query);
                    ResultSet rs = prep.executeQuery(query);
                    String ID = "";
                    if (!rs.next()) {
                        System.out.println("could not generate a new id");
                    } else {
                        ID = rs.getString("newID").trim();
                    }
                    rs.close();

                    String yearString = getTextValue(film, "year");
                    int year;
                    try {
                        year = Integer.parseInt(yearString);

                    } catch (Exception e) {
                        System.out.println("CANNOT ADD '" + title + "' YEAR DATA ERROR");
                        //continue to next film
                        continue;
                    }
//                            System.out.println(year);
                    //Get genres

                    // insert movie here
                    NodeList genres = film.getElementsByTagName("cats");

                    //Create a list of all genreIDs in this movie to add to genres_in_movies later
//                            ArrayList<String> gList = new ArrayList<>();
                    //insert movie here
                    if (dirname != "") {
                        String checkQ = "select count(*) as num, m.id as movieid from movies as m where m.title='" + title + "' and m.year =" + year + " and" +
                                " m.director = '" + dirname + "' GROUP BY m.id;";
//                        System.out.println(checkQ);
                        PreparedStatement checkquery = connection.prepareStatement(checkQ);
                        ResultSet rsq = checkquery.executeQuery(checkQ);
                        String num = "";
                        String movieID = "";
                        while (rsq.next()) {
                            num = rsq.getString("num");
                            movieID = rsq.getString("movieid");
                        }

//                        System.out.println(num);
                        checkquery.close();
                        rsq.close();
                        if (!num.equals("")) {
//                            System.out.println("already in database setting id to movieid");
                            ID = movieID;
                        } else {

                            String insertMovieQuery = "INSERT INTO movies(id, title, year, director) VALUES ('" + ID + "', '" +
                                    title + "', " + year + ", '" + dirname + "')";
//                                    Connection insertConnect = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
                            PreparedStatement insert = connection.prepareStatement(insertMovieQuery);
//                            System.out.println(insert.toString());
                            insert.executeUpdate(insertMovieQuery);
//                                    insertConnect.close();
                            insert.close();
                            String ratingQ = "INSERT into ratings(movieid, rating, numVotes) " + "VALUES('" + ID + "', 0, 0)";
                            PreparedStatement insertR = connection.prepareStatement(ratingQ);
//                                    System.out.println(ratingQ);
                            insertR.executeUpdate(ratingQ);
//                                insertConnect.close();
                            insert.close();
                        }


                    } else {
                        System.out.println("DIRECTOR NAME NOT VALID AT LINE: " + i);
                        continue;
                    }

                    for (int k = 0; k < genres.getLength(); k++) {
                        // genre name
                        String genre = genres.item(k).getTextContent().trim();
                        if (genreByCat.get(genre.toLowerCase()) == null) {
                            if (genre.length() > 4) {

                                String[] genreList = (genre.split("(?<=\\G....)"));
                                genreList = new HashSet<String>(Arrays.asList(genreList)).toArray(new String[0]);
                                for (String js : genreList) {
                                    String gname = (genreByCat.get(js.trim().toLowerCase()));
                                    // GENRE CHECK (get genre ID)
                                    if (gname == null) {
                                        gname = "Unknown";
                                    }


                                    // GENRE CHECK
                                    String gquery = ("call add_genre('" + gname + "', '" + ID + "')");
                                    PreparedStatement insert = connection.prepareStatement(query);
                                    insert.executeUpdate(gquery);
//                                    System.out.println("m:" + gquery);
                                    insert.close();

                                }
                            }

                        } else {
                            String newGenre = genreByCat.get(genre.trim().toLowerCase());
                            if (newGenre.equals("null")) {
                                newGenre = "Unknown";
                            }
                            String gquery = ("call add_genre('" + newGenre + "', '" + ID + "')");
                            PreparedStatement insert = connection.prepareStatement(query);
//                            System.out.println("s: " + gquery);
                            insert.executeUpdate(gquery);
                            insert.close();
                        }


                    } // genres
                } // film check
            }// node check
        }// directors for loop
        connection.close();


    }// method


    private static String getTextValue(Element ele, String tagName) {

        String textVal = null;

        try {
            NodeList nl = ele.getElementsByTagName(tagName);
            if (nl != null && nl.getLength() > 0) {
                Element el = (Element) nl.item(0);
                textVal = el.getFirstChild().getNodeValue();
            }
        } catch (Exception e) {
            return textVal;
        }

        return textVal;
    }

    public static void parseActorsDocument(File actorsXML) throws ParserConfigurationException, IOException, SAXException, SQLException {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(actorsXML);

        doc.getDocumentElement().normalize();

        //get the root elememt
        Element root = doc.getDocumentElement();

        //get list of actors
        NodeList actors = doc.getElementsByTagName("actor");
        System.out.println(actors.getLength());
        PreparedStatement psInsertRecord = null;
        String sqlInsertRecord = null;
        int[] iNoRows = null;
        sqlInsertRecord = "insert into stars(id, name, birthYear) values( ? , ? , ? )";
        try {
            Connection conn = getConnection();
            conn.setAutoCommit(false);

            psInsertRecord = conn.prepareStatement(sqlInsertRecord);




            for (int i = 0; i < actors.getLength(); i++) {

                Node actor = actors.item(i);
                if (actor.getNodeType() == Node.ELEMENT_NODE) {

                    Element actorElement = (Element) actor;
                    String birthYear = null;

                    String actorName = actorElement.getElementsByTagName("stagename").item(0).getTextContent().replaceAll("'", " ");

                    String dob = getTextValue(actorElement, "dob");
                    if (dob == null || dob.length() > 4 || !checkIfInt(dob)) {
                        // just set to 0 if none provided
                        birthYear = "0";
                    } else {
                        birthYear = dob;
                    }
                    String actorID = "";
                    if (starSet.containsValue(actorName.toLowerCase())) {
                        for (Map.Entry<String, String> entry : starSet.entrySet()) {
                            if (actorName.toLowerCase().equals(entry.getValue())) {
                                continue;
                            }
                        }
                    } else {
//                        System.out.println("generating new starID");
                        actorID = genStarID(maxStarID);
                        maxStarID++;
                        psInsertRecord.setString(1, actorID);
                        psInsertRecord.setString(2, actorName);
                        if (birthYear.equals("0")) {
                            psInsertRecord.setNull(3, Types.NULL);
                        } else {
                            psInsertRecord.setInt(3, Integer.parseInt(birthYear));
                        }
                        psInsertRecord.addBatch();
//                        System.out.println(psInsertRecord.toString());

                    }

                }

            }
            iNoRows = psInsertRecord.executeBatch();
            conn.commit();
            System.out.println(iNoRows);

            try {
                if (psInsertRecord != null) psInsertRecord.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

//            iNoRows=psInsertRecord.executeBatch();
//            conn.commit();
        }catch (SQLException e)
        {
            e.printStackTrace();
        }
        System.out.println(actors.getLength());

    }
    // HELPER FUNCTIONS FOR PARSEACTORSXML
    private static boolean checkIfInt(String s)
    {
        try
        {
            Integer.parseInt(s);
            return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }
    private static String genStarID(int number)
    {
        String ID = "nm" + number;
        return ID;
    }

    private static int getMaxStarID()
    {
        int returnID = -1;
        try
        {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            String query = "select max(id) from stars";

            ResultSet rs = statement.executeQuery(query);
            String ID = "";
            while(rs.next()) {
                ID = rs.getString(1);
            }
            @SuppressWarnings("resource")
            Scanner in = new Scanner(ID).useDelimiter("[^0-9]+");
            returnID = in.nextInt();
            connection.close();
            rs.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        System.out.println(returnID);
        maxStarID = returnID + 1;
        return maxStarID;
    }
}

