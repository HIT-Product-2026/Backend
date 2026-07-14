-- ===========================
-- USERS
-- ===========================
CREATE TABLE users (
    id UUID NOT NULL,
    username VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(255),
    mode VARCHAR(50),
    fcm_token VARCHAR(255),
    created_at TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email)
);

-- ===========================
-- POSTS
-- ===========================
CREATE TABLE posts (
    id UUID NOT NULL,
    user_id UUID,

    bucket VARCHAR(255),
    object_name VARCHAR(255),
    caption VARCHAR(100),

    mode_location VARCHAR(50),

    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,

    nsfw VARCHAR(50),

    created_at TIMESTAMP,

    PRIMARY KEY (id)
);

-- ===========================
-- PROFILES
-- ===========================
CREATE TABLE profiles (
    id UUID NOT NULL,
    user_id UUID NOT NULL,

    birthday DATE,
    gender VARCHAR(20),
    phone_number VARCHAR(20),

    post_count INT DEFAULT 0,
    current_post_streak INT DEFAULT 0,
    longest_post_streak INT DEFAULT 0,

    latest_post_id UUID,
    favorite_post_id UUID,

    count_post_reup INT DEFAULT 0,
    power INT DEFAULT 0,

    create_at TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT uk_profiles_user UNIQUE (user_id)
);

-- ===========================
-- PROFILE VISITED PROFILES
-- ===========================
CREATE TABLE profile_visited_profiles (
    profile_id UUID NOT NULL,
    visited_profile_id UUID NOT NULL,

    PRIMARY KEY (profile_id, visited_profile_id)
);

-- ===========================
-- PROFILE VISITORS
-- ===========================
CREATE TABLE profile_visitors (
    profile_id UUID NOT NULL,
    visitor_profile_id UUID NOT NULL,

    PRIMARY KEY (profile_id, visitor_profile_id)
);

-- ===========================
-- PROFILE PHOTOGRAPHED FRIENDS
-- ===========================
CREATE TABLE profile_photographed_friends (
    profile_id UUID NOT NULL,
    friend_user_id UUID NOT NULL,

    PRIMARY KEY (profile_id, friend_user_id)
);

-- ===========================
-- PROFILE CITIES
-- ===========================
CREATE TABLE profile_cities (
    profile_id UUID NOT NULL,
    city VARCHAR(255) NOT NULL,

    PRIMARY KEY (profile_id, city)
);

-- ===========================
-- PROFILE REUP POSTS
-- ===========================
CREATE TABLE profile_reup_posts (
    profile_id UUID NOT NULL,
    post_id UUID NOT NULL,

    PRIMARY KEY (profile_id, post_id)
);

-- =====================================
-- INVALIDATED TOKEN
-- =====================================
CREATE TABLE invalidated_token (
    id VARCHAR(255) NOT NULL,
    expiry_time TIMESTAMP,

    PRIMARY KEY (id)
);

-- =====================================
-- FRIENDSHIPS
-- =====================================
CREATE TABLE friendships (
    id UUID NOT NULL,

    requester_id UUID NOT NULL,
    receiver_id UUID NOT NULL,

    status VARCHAR(50) NOT NULL,

    created_at TIMESTAMP,
    updated_at TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT uk_friendship UNIQUE (requester_id, receiver_id),

    CHECK (requester_id <> receiver_id)
);

-- =====================================
-- CONVERSATIONS
-- =====================================
CREATE TABLE conversations (
    id UUID NOT NULL,

    user1_id UUID NOT NULL,
    user2_id UUID NOT NULL,

    last_message_id UUID,

    last_message_content TEXT,
    last_message_time TIMESTAMP,

    created_at TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT uk_conversation UNIQUE (user1_id, user2_id)
);

-- =====================================
-- MESSAGE_TEXT
-- =====================================
CREATE TABLE message_text (
    id UUID NOT NULL,

    conversation_id UUID,
    sender_id UUID NOT NULL,

    content TEXT,

    type VARCHAR(50),

    created_at TIMESTAMP,

    PRIMARY KEY (id)
);

-- =====================================
-- EMOJI POSTS
-- =====================================
CREATE TABLE emoji_posts (
    id UUID NOT NULL,

    post_id UUID NOT NULL,
    sender_id UUID NOT NULL,

    emoji VARCHAR(50) NOT NULL,

    created_at TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT uk_post_sender UNIQUE (post_id, sender_id)
);

-- =====================================
-- ACHIEVEMENTS
-- =====================================
CREATE TABLE achievements (
    id UUID NOT NULL,

    profile_id UUID NOT NULL,

    achievement_name VARCHAR(50) NOT NULL,

    type VARCHAR(50) NOT NULL,

    created_at TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT uk_profile_achievement UNIQUE (profile_id, achievement_name)
);