-- Create the database.
--create database if not exists csx370_mb_platform;

-- Use the created database.
-- use csx370_mb_platform;

-- Create the user table.
create table if not exists user (
    userId int auto_increment primary key,
    username varchar(255) not null unique,
    password varchar(255) not null,
    firstName varchar(255) not null,
    lastName varchar(255) not null
    -- primary key (userId),
    -- unique (username),
    -- constraint userName_min_length check (char_length(trim(username)) >= 2),
    -- constraint firstName_min_length check (char_length(trim(firstName)) >= 2),
    -- constraint lastName_min_length check (char_length(trim(lastName)) >= 2)
);

create table if not exists follow (
    followerId int not null,
    followeeId int not null,
    primary key (followerId, followeeId),
    foreign key (followerId) references user(userId) on delete cascade,
    foreign key (followeeId) references user(userId) on delete cascade
);

CREATE TABLE if not exists post(
    postId INT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(500),
    postDate VARCHAR(50),
    userId INT not null,
    heartsCount INT DEFAULT 0,
    commentsCount INT DEFAULT 0,
    isHearted BOOLEAN DEFAULT FALSE,
    isBookmarked BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (userId) REFERENCES user(userId) on delete cascade
);

-- DROP TABLE bookmark;
CREATE TABLE if not exists bookmark(
    userId INT, 
    postId INT,
    authorId INT not null,
    primary key (userId, postId),
    foreign key (userId) references user(userId) on delete cascade,
    foreign key (postId) references post(postId) on delete cascade,
    foreign key (authorId) references user(userId) on delete cascade
);

-- Table to store which users liked which posts
create table post_like (
    user_id int not null,
    post_id int not null,
    primary key (user_id, post_id),
    foreign key (user_id) references user(userid) on delete cascade,
    foreign key (post_id) references post(postid) on delete cascade
);
