import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;


public class FabflixSAXParser extends DefaultHandler {
    private final String ACTORS_FILE = "actors63.xml";
    private final String CASTS_FILE = "casts124.xml";
    private final String MAIN_FILE = "mains243.xml";
    List<Movie> myMovies;
    List<StarInMovie> myStarInMovies;
    Map<String, Integer> myStars;    // map name to birthYear
    private String tempVal;
    private Movie tempMovie;
    private StarInMovie tempStarInMovie;
    private String tempDirector;
    private String currentFile;
    private String tempStarName;
    private int nullStarCount;
    private String reportString;
    private Locator locator;
//    private DataSource dataSource;


    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    public FabflixSAXParser() {
        myMovies = new ArrayList<Movie>();
        myStarInMovies = new ArrayList<StarInMovie>();
        myStars = new HashMap<String, Integer>();
        reportString = "";

//        try {
//            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void parseDocument(String document) {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            currentFile = document;
            reportString += currentFile + ":\n\n";
            InputSource source = new InputSource(currentFile);
            source.setEncoding("ISO-8859-1");
            sp.parse(source, this);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    private void printData(String document) {
        if (document.equals(MAIN_FILE)) {
            reportString += "No of movies '" + myMovies.size() + "'.\n\n";
            Iterator<Movie> it = myMovies.iterator();
            try {
                FileWriter f = new FileWriter(document + "_log.txt");
                while (it.hasNext()) {
                    //                System.out.println(it.next().toString());
                    f.write(it.next().toString() + "\n");
                }
                f.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (document.equals(CASTS_FILE)) {
            reportString += "No of casts '" + myStarInMovies.size() + "'.\n\n";
//            reportString += "No of casts omitted '" + nullStarCount + "' b/c of unknown actor ('sa' or 's a').\n\n";
            Iterator<StarInMovie> it = myStarInMovies.iterator();
            try {
                FileWriter f = new FileWriter(document + "_log.txt");
                while (it.hasNext()) {
                    //                System.out.println(it.next().toString());
                    f.write(it.next().toString() + "\n");
                }
                f.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            reportString += "No of stars '" + myStars.size() + "'.\n\n";
            try {
                FileWriter f = new FileWriter(currentFile + "_log.txt");
                for (String name : myStars.keySet()){
                    f.write(name + ": " + myStars.get(name) + "\n");
                }
                f.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (currentFile.equals(MAIN_FILE)) {
            if (qName.equalsIgnoreCase("film")) {
                tempMovie = new Movie();
            } else if (qName.equalsIgnoreCase("cats")) {
                tempMovie.setGenres(new ArrayList<String>());
            }
        } else if (currentFile.equals(CASTS_FILE)) {
            if (qName.equalsIgnoreCase("m")) {
                tempStarInMovie = new StarInMovie();
            }
        } else {
            if (qName.equalsIgnoreCase("actor")) {
                tempStarName = null;
            }
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (currentFile.equals(MAIN_FILE)) {
            if (qName.equalsIgnoreCase("directorfilms")) {
                tempDirector = null;
            } else if (qName.equalsIgnoreCase("dirname")) {
                tempDirector = tempVal;
            } else if (qName.equalsIgnoreCase("fid")) {
                tempMovie.setId(tempVal);
            } else if (qName.equalsIgnoreCase("t")) {
                tempMovie.setTitle(tempVal);
            } else if (qName.equalsIgnoreCase("year")) {
                try {
                    tempMovie.setYear(Integer.parseInt(tempVal));
                } catch (NumberFormatException e) {
                    tempMovie.setYear(null);
                }
            } else if (qName.equalsIgnoreCase("cat")) {
                tempMovie.addGenre(tempVal);
            } else if (qName.equalsIgnoreCase("film")) {
                tempMovie.setDirector(tempDirector);
                // add to myMovies regardless of null/ blank fields
                myMovies.add(tempMovie);
                if (tempMovie.getId() == null) {
                    reportString += "Line '" + this.locator.getLineNumber() + "': 'fid' element not found.\n";
                } else if (tempMovie.getTitle() == null) {
                    reportString += "Line '" + this.locator.getLineNumber() + "': 't' element not found.\n";
                } else if (tempMovie.getYear() == null) {
                    reportString += "Line '" + this.locator.getLineNumber() + "': 'year' element not found (or in non-integer format).\n";
                } else if (tempMovie.getDirector() == null) {
                    reportString += "Line '" + this.locator.getLineNumber() + "': 'dirname' element not found.\n";
                } else if (tempMovie.getGenres() == null) {
                    reportString += "Line '" + this.locator.getLineNumber() + "': 'cats' element not found.\n";
                }
            }
        } else if (currentFile.equals(CASTS_FILE)) {
            if (qName.equalsIgnoreCase("f")) {
                tempStarInMovie.setMovieId(tempVal);
            } else if (qName.equalsIgnoreCase("a")) {
                // ignores casts where actor is unknown
//                if (tempVal.replaceAll("\\s+", "").equalsIgnoreCase("sa")) {
//                    nullStarCount++;
//                } else {
//                    tempStarInMovie.setName(tempVal);
//                    // only add to myStarInMovies if name is not 'sa'
//                    myStarInMovies.add(tempStarInMovie);
//                }
                tempStarInMovie.setName(tempVal);
            } else if (qName.equalsIgnoreCase("m")) {
                myStarInMovies.add(tempStarInMovie);
                if (tempStarInMovie.getMovieId() == null) {
                    reportString += "Line '" + this.locator.getLineNumber() + "': 'f' element not found.\n";
                } else if (tempStarInMovie.getName() == null) {
                    reportString += "Line '" + this.locator.getLineNumber() + "': 'a' element not found.\n";
                }
            }
        } else {
            if (qName.equalsIgnoreCase("stagename")) {
                tempStarName = tempVal;
            } else if (qName.equalsIgnoreCase("dob")) {
                if (tempStarName != null) {
                    try {
                        myStars.put(tempStarName, Integer.parseInt(tempVal));
                    } catch (NumberFormatException e) {
                        myStars.put(tempStarName, null);
                    }
                }
            } else if (qName.equalsIgnoreCase("actor")) {
                if (tempStarName == null) {
                    reportString += "Line '" + this.locator.getLineNumber() + "': 'stagename' element not found.\n";
                } else if (!myStars.containsKey(tempStarName)) {
                    reportString += "Line '" + this.locator.getLineNumber() + "': 'dob' element not found.\n";
                }
            }
        }
    }

    private void addToDB(){
        long start = System.nanoTime();
        int moviesRowsChanged = 0, starsRowsChanged = 0, starsInMoviesRowsChanged = 0, genresInMoviesRowsChanged = 0;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/moviedb?autoReconnect=true&allowPublicKeyRetrieval=true&useSSL=false",
                    "",
                    ""
            );

            String movieIdQuery = "SELECT id FROM movies WHERE id REGEXP '^tt[0-9]{7}$' ORDER BY id DESC LIMIT 1";
            PreparedStatement movieIdQueryStatement = conn.prepareStatement(movieIdQuery);
            ResultSet movieIdQueryRs = movieIdQueryStatement.executeQuery();
            int highestMovieIdNumber = 0;
            while (movieIdQueryRs.next()) {
                highestMovieIdNumber = Integer.parseInt(movieIdQueryRs.getString("id")
                        .replaceAll("tt", ""));
            }
            movieIdQueryRs.close();
            movieIdQueryStatement.close();

            String movieUpdate = "INSERT IGNORE INTO movies VALUES(?, ?, ?, ?, ?)";
            PreparedStatement movieUpdateStatement = conn.prepareStatement(movieUpdate);
            String genreUpdate = "INSERT IGNORE INTO genres SELECT null, ? WHERE NOT EXISTS (SELECT * FROM genres WHERE name= ? )";
            PreparedStatement genreStatement = conn.prepareStatement(genreUpdate);
            String genresInMoviesUpdate = "INSERT IGNORE INTO genres_in_movies VALUES((SELECT id FROM genres WHERE name= ? ), ? )";
            PreparedStatement genresInMoviesStatement = conn.prepareStatement(genresInMoviesUpdate);
            Random r = new Random();
            int result, result4;
            for (Movie m : myMovies) {
                String[] priceArray = {"4.99", "9.99", "14.99", "19.99"};
                int randomIndex = r.nextInt(priceArray.length);
                String id;
                if (m.getId() == null || m.getId().equals("")) {
                    highestMovieIdNumber++;
                    id = "tt" + ((((double)highestMovieIdNumber / 10000000.0) + "").substring(2));
                } else {
                    id = m.getId();
                }

                // empty strings also made to be null
                movieUpdateStatement.setString(1, id);
                if (m.getTitle() == null) {
                    movieUpdateStatement.setNull(2, Types.NULL);
                } else {
                    movieUpdateStatement.setString(2, m.getTitle().equals("") ? null : m.getTitle());
                }
                if (m.getYear() == null) {
                    movieUpdateStatement.setNull(3, Types.NULL);
                } else {
                    movieUpdateStatement.setInt(3, m.getYear());
                }
                if (m.getDirector() == null) {
                    movieUpdateStatement.setNull(4, Types.NULL);
                } else {
                    movieUpdateStatement.setString(4, m.getDirector().equals("") ? null : m.getDirector());
                }
                movieUpdateStatement.setString(5, priceArray[randomIndex]);
                result = movieUpdateStatement.executeUpdate();
                moviesRowsChanged += result >= 0 ? result : 0;

                if (m.getGenres() != null) {
                    for (String g : m.getGenres()) {
                        String modifiedG = g.trim().replaceAll("\\s+", " ");
                        if (modifiedG.equals("") || modifiedG.equals(" ")) {
                            continue;
                        }
                        String genreSynonym = Constants.genresDefinition.containsKey(modifiedG.toLowerCase()) ?
                                Constants.genresDefinition.get(modifiedG.toLowerCase()) :
                                modifiedG.substring(0, 1).toUpperCase() + modifiedG.substring(1);
                        genreStatement.setString(1, genreSynonym);
                        genreStatement.setString(2, genreSynonym);
                        genreStatement.executeUpdate();

                        genresInMoviesStatement.setString(1, genreSynonym);
                        genresInMoviesStatement.setString(2, id);
                        result4 = genresInMoviesStatement.executeUpdate();
                        genresInMoviesRowsChanged += result4 >= 0 ? result4 : 0;
                    }
                }
            }
            movieUpdateStatement.close();
            genreStatement.close();
            genresInMoviesStatement.close();


            String starIdQuery = "SELECT id FROM stars ORDER BY id DESC LIMIT 1";
            PreparedStatement starIdQueryStatement = conn.prepareStatement(starIdQuery);
            ResultSet starIdQueryRs = starIdQueryStatement.executeQuery();
            int highestStarIdNumber = 0;
            while (starIdQueryRs.next()) {
                highestStarIdNumber = Integer.parseInt(starIdQueryRs.getString("id")
                        .replaceAll("nm", ""));
            }
            starIdQueryRs.close();
            starIdQueryStatement.close();

            // skips inserts with duplicate names
//            String starUpdate = "INSERT INTO stars SELECT ? , ? , ? WHERE NOT EXISTS (SELECT * FROM stars WHERE name= ? )";
            String starUpdate = "INSERT IGNORE INTO stars SELECT ? , ? , ? WHERE NOT EXISTS (SELECT * FROM stars WHERE MATCH (name) AGAINST ( ? IN BOOLEAN MODE) AND name= ?)";
            PreparedStatement starStatement = conn.prepareStatement(starUpdate);
            String starInMovieUpdate = "INSERT IGNORE INTO stars_in_movies SELECT id , ? FROM stars WHERE MATCH (name) AGAINST ( ? IN BOOLEAN MODE) AND name= ?";
//            String starInMovieUpdate = "INSERT IGNORE INTO stars_in_movies VALUES( ? , ? )";
            PreparedStatement starInMovieStatement = conn.prepareStatement(starInMovieUpdate);
//            String starIdQ = "SELECT id FROM stars WHERE MATCH (name) AGAINST ( ? IN BOOLEAN MODE) AND name= ? ";
//            PreparedStatement starIdQStatement = conn.prepareStatement(starIdQ);
            int result2, result3;
            for (StarInMovie s : myStarInMovies) {
                // insert stars start

                starStatement.setString(1, "nm" + ++highestStarIdNumber);
                if (s.getName() == null ||
                        s.getName().equals("") ||
                        s.getName().replaceAll("\\s+", "").equalsIgnoreCase("sa")) {
                    continue;
                } else {
                    starStatement.setString(2, s.getName());
                }
                if (myStars.get(s.getName()) == null) {
                    starStatement.setNull(3, Types.NULL);
                } else {
                    starStatement.setInt(3, myStars.get(s.getName()));
                }
//                starStatement.setString(4, s.getName());
                String match = "";
                for (String word : s.getName().trim().split("\\s+")) {
                    match += "+" + word.replaceAll("([()+-<>])", "\"$1\"") + " ";
                }
                starStatement.setString(4, match);

//                starStatement.setString(5, "%" + s.getName() + "%");
                starStatement.setString(5, s.getName());

//                System.out.println(starStatement.toString());
                result2 = starStatement.executeUpdate();
                starsRowsChanged += result2 >= 0 ? result2 : 0;
                // insert stars end

                // insert stars_in_movies start
//                starIdQStatement.setString(1, match);
//                starIdQStatement.setString(2, s.getName());
//                ResultSet starIdQRs = starIdQStatement.executeQuery();
//                String starId = "";
//                while (starIdQRs.next()) {
//                    starId = starIdQRs.getString("id");
//                }


                if (s.getMovieId() == null || s.getMovieId().equals("")) {
                    continue;
                }

//                starInMovieStatement.setString(1, starId);
//                starInMovieStatement.setString(2, s.getMovieId());
                starInMovieStatement.setString(1, s.getMovieId());
                starInMovieStatement.setString(2, match);
                starInMovieStatement.setString(3 ,s.getName());
                result3 = starInMovieStatement.executeUpdate();
                starsInMoviesRowsChanged += result3 >= 0 ? result3 : 0;
                // insert stars_in_movies end
            }
            starStatement.close();
            starInMovieStatement.close();
//            starIdQStatement.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("Writing to DB took '" + (double)(System.nanoTime() - start) / 1_000_000_000.0 + "' seconds.");
        System.out.println("Movies rows changed: " + moviesRowsChanged);
        System.out.println("Stars rows changed: " + starsRowsChanged);
        System.out.println("StarsInMovies rows changed: " + starsInMoviesRowsChanged);
        System.out.println("GenresInMovies rows changed: " + genresInMoviesRowsChanged);
    }

    private void createReport() {
        try {
            FileWriter f = new FileWriter("report.txt");
            f.write(reportString);
            f.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FabflixSAXParser parser = new FabflixSAXParser();
        System.out.println("Begun parsing, this will take a while...");
        parser.parseDocument(parser.MAIN_FILE);
        System.out.println("Finished parsing " + parser.MAIN_FILE + ".");
        parser.printData(parser.MAIN_FILE);
        parser.parseDocument(parser.CASTS_FILE);
        System.out.println("Finished parsing " + parser.CASTS_FILE + ".");
        parser.printData(parser.CASTS_FILE);
        parser.parseDocument(parser.ACTORS_FILE);
        System.out.println("Finished parsing " + parser.ACTORS_FILE + ".");
        parser.printData(parser.ACTORS_FILE);

        System.out.println("Begun inserting parsed data to database, this will take a while...");
        parser.addToDB();
        parser.createReport();
    }
}
