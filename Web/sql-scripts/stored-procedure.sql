use moviedb;
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