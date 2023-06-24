MERGE INTO mpa_ratings (rating_code) 
KEY (rating_code) 
VALUES ('G'), ('PG'), ('PG-13'), ('R'), ('NC-17');

MERGE INTO genres (genre_name)
KEY (genre_name)
VALUES 	('Комедия'), ('Драма'), ('Мультфильм'), ('Триллер'), ('Документальный'), ('Боевик');

INSERT INTO users (EMAIL, LOGIN, NAME, BIRTHDAY)
VALUES  ('antonuser1@mail.ru', 'anton-login1', 'Anton', '2000-10-01'),
		('alexuser2@mail.ru', 'alex-login2', 'Alex', '1999-11-15'),
		('peteruser3@mail.ru', 'peter-login3', 'Peter', '2005-01-02'),
		('nikitauser4@mail.ru', 'nikita-login4', 'Nikita', '2002-05-20');

INSERT INTO films (TITLE, DESCRIPTION, RELEASE_DATE, DURATION, MPA_RATING_ID)
VALUES 	('Inception', 'Inception description', '2010-06-08', 148, 3), --Action3, Adventure7, Sci-Fi8
		('Life Cycles', 'Life description',
		'2010-09-21', 47, 1), --Documentary4, Sport9
		('Shutter Island', 'Island description',
		'2010-02-13', 138, 4), --Mystery10, Thriller6 
		('Of Gods and Men', 'Film description', '2010-05-18', 122, 3), -- Drama2, History15
		('The Illusionist', 'Illusionist description', '2010-02-16', 80, 2); --Animation5, Drama2, Fantasy11
		
INSERT INTO FILMS_AND_GENRES (film_id, genre_id)
VALUES (1, 3), (1, 6), (2, 5), (3, 4), (4, 2), (4, 3);

INSERT INTO FRIENDSHIPS (user_id, friend_id) 
VALUES (1, 2), (3, 2);

INSERT INTO LIKES (film_id, user_id)
VALUES (1, 1), (1, 2), (3, 1), (2, 4), (4, 4);

