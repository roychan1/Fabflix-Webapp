use moviedb;
ALTER TABLE movies
ADD COLUMN priceForEach varchar(10) NOT NULL;
SET SQL_SAFE_UPDATES = 0;
UPDATE movies
SET priceForEach = ELT(1 + FLOOR(RAND() * 4), "4.99", "9.99", "14.99", "19.99");
SET SQL_SAFE_UPDATES = 1;