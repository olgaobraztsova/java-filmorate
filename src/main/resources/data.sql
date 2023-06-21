MERGE INTO mpa_ratings (rating_code) 
KEY (rating_code) 
VALUES ('G'), ('PG'), ('PG-13'), ('R'), ('NC-17');

MERGE INTO genres (genre_name)
KEY (genre_name)
VALUES 	('Комедия'), ('Драма'), ('Мультфильм'), ('Триллер'), ('Документальный'), ('Боевик');