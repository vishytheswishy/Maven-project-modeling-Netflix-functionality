DROP DATABASE IF EXISTS moviedb;    
CREATE DATABASE moviedb;
use moviedb;

DROP TABLE IF EXISTS stars;
create table stars(
           id varchar(10),
           name varchar(100) not null,
           birthYear integer,
           primary key(id)
       );

DROP TABLE IF EXISTS movies;
create table movies( 
            id varchar(10) primary key,
            title varchar(100) NOT NULL,
            year INTEGER NOT NULL,
            director varchar(100) NOT NULL
);


DROP TABLE IF EXISTS genres;
create table genres(
            id INT NOT NULL AUTO_INCREMENT,
            names varchar(32) NOT NULL,
            primary key(id)
);

DROP TABLE IF EXISTS creditcards;
create table creditcards(
            id varchar(20) PRIMARY KEY,
            firstName varchar(50) NOT NULL,
            lastName VARCHAR(50) NOT NULL,
            expiration date NOT NULL
);

DROP TABLE IF EXISTS customers;
create table customers(
            id integer primary key AUTO_INCREMENT,
            firstName varchar(50),
            lastName varchar(50),
            ccld varchar(20) REFERENCES creditcards.id,
            address varchar(200),
            email varchar(50),
            password varchar(20)
);

DROP TABLE IF EXISTS sales;
create table sales(
            id INTEGER PRIMARY KEY AUTO_INCREMENT,
            customerID integer REFERENCES customers.id,
            movieID varchar(10) REFERENCES movies.id,
            saleDate date
);

DROP TABLE IF EXISTS ratings;
create table ratings(
            movieID varchar(10) REFERENCES movies.id,
            rating FLOAT NOT NULL,
            numVotes INTEGER NOT NULL
);

DROP TABLE IF EXISTS stars_in_movies;
create table stars_in_movies(
            starId varchar(10), 
            movieId varchar(10)
);

DROP TABLE IF EXISTS employees;
create table employees(
        email varchar(50) primary key,
        password varchar(20) not null,
        fullName varchar(100)
);

ALTER TABLE stars_in_movies
ADD CONSTRAINT FK_StarID
FOREIGN KEY (starId) REFERENCES stars(id) ON DELETE CASCADE;

ALTER TABLE stars_in_movies
ADD CONSTRAINT FK_MovieID
FOREIGN KEY (movieId) REFERENCES movies(id) ON DELETE CASCADE;

DROP TABLE IF EXISTS genres_in_movies;
create table genres_in_movies(
            genreID INT,
            movieID varchar(10)

);

ALTER TABLE genres_in_movies
ADD CONSTRAINT FK_genreID
FOREIGN KEY (genreID) REFERENCES genres(id) ON DELETE CASCADE;

ALTER TABLE genres_in_movies
ADD CONSTRAINT FK_genre_movieID
FOREIGN KEY (movieId) REFERENCES movies(id) ON DELETE CASCADE;

-- SELECT m.title, m.year, GROUP_CONCAT(g.names , ", "), GROUP_CONCAT(s.name, ", "), r.rating 
-- from movies as m, genres_in_movies as gm, stars_in_movies as sm, ratings as r, stars as s
-- where m.title = ? AND m.id = r.movieID AND gm.movieID = m.id AND 

CREATE VIEW top20ratings AS select ratings.movieID, ratings.rating from ratings ORDER BY ratings.rating DESC LIMIT 20;

CREATE VIEW movienullcheck As select m.id, r.rating from movies as m left join ratings as r on m.id = r.movieid;

Create VIEW starsnumofmovies As select distinct s.name, s.id, count(DISTINCT sm.movieid) as numofmov from stars as s, stars_in_movies as sm where s.id = sm.starID group by s.name, s.id ORDER BY COUNT(sm.movieID) DESC;

INSERT INTO employees(email, password, fullName) VALUES ('classta@email.edu', 'classta', 'TA CS122B');
--  Indexes used 
--  create unique index m_id on movies(id);
--  Alter table sales add quantity int default 1;
delimiter $
drop procedure IF EXISTS add_movie;
create procedure add_movie( IN titlem varchar(100), IN myear INTEGER, IN mdirector varchar(100),
                IN starName varchar(100) , IN starBY INT, IN genreName varchar(32))

            begin
                set @movieIDT = '0';
                set @starIDT = '0';
                Select (substring( max(id) , 3 ) + 1) into @movieIDT from movies;

                Select (substring( max(id) , 3 )  + 1) into @starIDT from stars;

                set @movieid = CONCAT('tt0', @movieIDT ) ,  @starID = CONCAT('nm', @starIDT );

                INSERT INTO movies(id, title, year, director) VALUES ( @movieid, titlem, myear, mdirector );

                set @boolS = 0;
                select COUNT(distinct if (starName = g.name, 1, 0)) into @boolS from stars as g;
                if @boolS = 1 then
                    if starBY = 0
                    then
                        INSERT INTO stars(id, name, birthYear ) VALUES ( @starID , starName, NULL);
                    else INSERT INTO stars(id, name, birthYear ) VALUES ( @starID , starName, starBY);
                    INSERT INTO stars_in_movies(starId, movieId) VALUES ( @starID, @movieid );
                    end if;
                else
                    set @sID = 0;
                    select id into @sID from stars as s where s.name = starName limit 1;
                    INSERT INTO stars_in_movies(starId, movieID) VALUES (@sID, @movieid);
                end if;
                insert into ratings(movieID, rating, numVotes) VALUES(@movieid, 0, 0);
                set @boolG = 0;
                select COUNT(distinct if (genreName = g.names, 1, 0)) into @boolG from genres as g;
                IF @boolG = 1 then
                    set @gID = 0;
                    INSERT INTO genres(names) VALUES (genreName);
                    select id into @gID from genres where genres.names = genreName;
                    INSERT INTO genres_in_movies(genreID, movieID) VALUES (@gID, @movieid);
                ELSE
                    set @gID = 0;
                    select id into @gID from genres where genres.names = genreName;
                    INSERT INTO genres_in_movies(genreID, movieID) VALUES (@gID, @movieid);
                end IF;
            end; $


delimiter //
drop procedure IF EXISTS add_genre;
create procedure add_genre(IN genreName varchar(32), IN newMovieID varchar(10) )
begin
    set @boolG = 0;
                select COUNT(distinct if (genreName = g.names, 1, 0)) into @boolG from genres as g;
                IF @boolG = 1 then
                    set @gID = 0;
                    INSERT INTO genres(names) VALUES (genreName);
                    select id into @gID from genres where genres.names = genreName;
                    INSERT INTO genres_in_movies(genreID, movieID) VALUES (@gID, newMovieID);
                ELSE
                    set @gID = 0;
                    select id into @gID from genres where genres.names = genreName;
                    INSERT INTO genres_in_movies(genreID, movieID) VALUES (@gID, newMovieID);
                end IF;
            end; //


delimiter ?
drop procedure IF EXISTS add_stars;
create procedure add_stars(IN starName varchar(100), IN newMovieID varchar(10) )
    begin
        set @starIDT = '0';
                Select (substring( max(id) , 3 )  + 1) into @starIDT from stars;
                set @starID = CONCAT('nm', @starIDT );
        set @boolS = 0;
                select COUNT(distinct if (starName = g.name, 1, 0)) into @boolS from stars as g;
                if @boolS = 1 then
                    INSERT INTO stars(id, name, birthYear ) VALUES ( @starID , starName, NULL );
                    INSERT INTO stars_in_movies(starId, movieId) VALUES ( @starID, newMovieID );
                else
                    set @sID = 0;
                    select id into @sID from stars as s where s.name = starName limit 1;
                    INSERT INTO stars_in_movies(starId, movieID) VALUES (@sID, newMovieID);
                end if;
            end; ?


