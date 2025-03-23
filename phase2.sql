-- Table for Login for security purposes
DROP TABLE IF EXISTS Login;
CREATE TABLE Login (
    login CHAR(15) UNIQUE NOT NULL,
    password CHAR(50) NOT NULL
);

-- Table for Users
DROP TABLE IF EXISTS Users;
CREATE TABLE Users (
    phoneNumber CHAR(60) NOT NULL,
    role CHAR(10) NOT NULL,
    favoriteItem CHAR(500),
    login CHAR(15) NOT NULL,
    PRIMARY KEY (phoneNumber),
    FOREIGN KEY (login) REFERENCES Login(login)
);

-- Table for Items
DROP TABLE IF EXISTS Items;
CREATE TABLE Items (
    itemName CHAR(50) UNIQUE NOT NULL,
    type CHAR(40) NOT NULL,
    price FLOAT NOT NULL,
    ingredients CHAR(500) NOT NULL,
    description CHAR(600),
    imageURL CHAR(256),
    PRIMARY KEY (itemName)
);

-- Table for Stores
DROP TABLE IF EXISTS Stores;
CREATE TABLE Stores (
    storeID CHAR(50) UNIQUE NOT NULL,
    address CHAR(40) NOT NULL,
    city CHAR(40) NOT NULL,
    state CHAR(40) NOT NULL,
    isOpen CHAR(40) NOT NULL,
    reviewScore FLOAT,
    PRIMARY KEY (storeID)
);

-- Table for Orders
DROP TABLE IF EXISTS Orders;
CREATE TABLE Orders (
    orderID CHAR(60) UNIQUE NOT NULL,
    orderTimestamp TIMESTAMP NOT NULL,
    orderStatus CHAR(50) NOT NULL,
    totalPrice FLOAT NOT NULL,
    phoneNumber CHAR(60) NOT NULL,
    storeID CHAR(50) NOT NULL,
    PRIMARY KEY (orderID),
    FOREIGN KEY (phoneNumber) REFERENCES Users(phoneNumber),
    FOREIGN KEY (storeID) REFERENCES Stores(storeID)
);

-- Relationship: User views Item
DROP TABLE IF EXISTS Views;
CREATE TABLE Views (
    phoneNumber CHAR(60) NOT NULL,
    itemName CHAR(50) NOT NULL,
    PRIMARY KEY (phoneNumber, itemName),
    FOREIGN KEY (phoneNumber) REFERENCES Users(phoneNumber),
    FOREIGN KEY (itemName) REFERENCES Items(itemName)
);

-- Relationship: Store has Item
DROP TABLE IF EXISTS AvailableAt;
CREATE TABLE AvailableAt (
    storeID CHAR(50) NOT NULL,
    itemName CHAR(50) NOT NULL,
    PRIMARY KEY (storeID, itemName),
    FOREIGN KEY (storeID) REFERENCES Stores(storeID),
    FOREIGN KEY (itemName) REFERENCES Items(itemName)
);

-- Relationship: Order has Item
DROP TABLE IF EXISTS Has;
CREATE TABLE Has (
    orderID CHAR(60) NOT NULL,
    itemName CHAR(50) NOT NULL,
    PRIMARY KEY (orderID, itemName),
    FOREIGN KEY (orderID) REFERENCES Orders(orderID),
    FOREIGN KEY (itemName) REFERENCES Items(itemName)
);
