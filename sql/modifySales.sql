USE moviedb;
CREATE TABLE copies_purchased (
	id int NOT NULL AUTO_INCREMENT,
	movie_id varchar(10) NOT NULL,
    	copies int NOT NULL,
    	PRIMARY KEY (id),
	FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE
);
SET foreign_key_checks = 0;
DROP TABLE sales;
SET foreign_key_checks = 1;
CREATE TABLE sales (
	id int NOT NULL AUTO_INCREMENT,
	customerId int NOT NULL,
	copiesPurchasedIds JSON NOT NULL,
	saleDate date NOT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (customerId) REFERENCES customers(id) ON DELETE CASCADE
);
