USE moviedb;
DELIMITER $$
CREATE FUNCTION add_movie(
    movie_id VARCHAR(10),
    genre_id INT,
    star_id VARCHAR(10),
    movieTitle VARCHAR(100),
    movieYear INT,
    movieDirector VARCHAR(100),
    moviePrice VARCHAR(10),
    star VARCHAR(100),
    starBirthYear INT,
    genre VARCHAR(32))
    RETURNS INT
    DETERMINISTIC
BEGIN
    INSERT INTO movies VALUES (movie_id, movieTitle, movieYear, movieDirector, moviePrice);
    IF (genre_id != null) THEN
        INSERT INTO genres (id, name)
        SELECT genre_id, genre
        WHERE NOT EXISTS (SELECT id, name FROM genres WHERE id=genre_id);
        INSERT INTO genres_in_movies VALUES (genre_id, movie_id);
    END IF;
    IF (star_id != 'null') THEN
        INSERT INTO stars (id, name, birthYear)
        SELECT star_id, star, starBirthYear
        WHERE NOT EXISTS (SELECT id, name, birthYear FROM stars WHERE id=star_id);
        INSERT INTO stars_in_movies VALUES (star_id, movie_id);
    END IF;
    RETURN 1;
END $$
DELIMITER ;