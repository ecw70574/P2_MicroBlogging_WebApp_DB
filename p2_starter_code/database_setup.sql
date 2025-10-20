-- Create the database.
-- drop database csx370_mb_platform;
create database if not exists csx370_mb_platform;

-- Use the created database.
use csx370_mb_platform;

-- Create the user table.
create table if not exists user (
    userId int auto_increment,
    username varchar(255) not null,
    password varchar(255) not null,
    firstName varchar(255) not null,
    lastName varchar(255) not null,
    primary key (userId),
    unique (username),
    constraint userName_min_length check (char_length(trim(userName)) >= 2),
    constraint firstName_min_length check (char_length(trim(firstName)) >= 2),
    constraint lastName_min_length check (char_length(trim(lastName)) >= 2)
);

-- Follow table - to store relationships between users
create table if not exists follow (
    followerId int not null,
    followeeId int not null,
    primary key (followerId, followeeId),
    foreign key (followerId) references user(userId) on delete cascade,
    foreign key (followeeId) references user(userId) on delete cascade
);

-- Post table - to store post information
CREATE TABLE post (
    postId INT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(500),
    postDate VARCHAR(50),
    userId VARCHAR(50),
    heartsCount INT DEFAULT 0,
    commentsCount INT DEFAULT 0,
    isHearted BOOLEAN DEFAULT FALSE,
    isBookmarked BOOLEAN DEFAULT FALSE
);

create table if not exists bookmark (
   userId int auto_increment,
   postId int auto_increment,
   primary key (userId, postId)
   foreign key (userId) references user(userId) on delete cascade,
   foreign key (postId) references post(userId) on delete cascade
);

