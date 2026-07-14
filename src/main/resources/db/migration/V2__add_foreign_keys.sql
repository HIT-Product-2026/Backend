-- =====================================
-- POSTS
-- =====================================
ALTER TABLE posts
ADD CONSTRAINT fk_posts_user
FOREIGN KEY (user_id)
REFERENCES users(id)
ON DELETE SET NULL;

-- =====================================
-- PROFILES
-- =====================================
ALTER TABLE profiles
ADD CONSTRAINT fk_profiles_user
FOREIGN KEY (user_id)
REFERENCES users(id)
ON DELETE CASCADE;

ALTER TABLE profiles
ADD CONSTRAINT fk_profiles_latest_post
FOREIGN KEY (latest_post_id)
REFERENCES posts(id)
ON DELETE SET NULL;

ALTER TABLE profiles
ADD CONSTRAINT fk_profiles_favorite_post
FOREIGN KEY (favorite_post_id)
REFERENCES posts(id)
ON DELETE SET NULL;

-- =====================================
-- PROFILE COLLECTIONS
-- =====================================
ALTER TABLE profile_visited_profiles
ADD CONSTRAINT fk_profile_visited_profiles
FOREIGN KEY (profile_id)
REFERENCES profiles(id)
ON DELETE CASCADE;

ALTER TABLE profile_visitors
ADD CONSTRAINT fk_profile_visitors
FOREIGN KEY (profile_id)
REFERENCES profiles(id)
ON DELETE CASCADE;

ALTER TABLE profile_photographed_friends
ADD CONSTRAINT fk_profile_photographed_friends
FOREIGN KEY (profile_id)
REFERENCES profiles(id)
ON DELETE CASCADE;

ALTER TABLE profile_cities
ADD CONSTRAINT fk_profile_cities
FOREIGN KEY (profile_id)
REFERENCES profiles(id)
ON DELETE CASCADE;

ALTER TABLE profile_reup_posts
ADD CONSTRAINT fk_profile_reup_posts
FOREIGN KEY (profile_id)
REFERENCES profiles(id)
ON DELETE CASCADE;

-- =====================================
-- FRIENDSHIPS
-- =====================================
ALTER TABLE friendships
ADD CONSTRAINT fk_friendship_requester
FOREIGN KEY (requester_id)
REFERENCES users(id)
ON DELETE CASCADE;

ALTER TABLE friendships
ADD CONSTRAINT fk_friendship_receiver
FOREIGN KEY (receiver_id)
REFERENCES users(id)
ON DELETE CASCADE;

-- =====================================
-- CONVERSATIONS
-- =====================================
ALTER TABLE conversations
ADD CONSTRAINT fk_conversation_user1
FOREIGN KEY (user1_id)
REFERENCES users(id)
ON DELETE CASCADE;

ALTER TABLE conversations
ADD CONSTRAINT fk_conversation_user2
FOREIGN KEY (user2_id)
REFERENCES users(id)
ON DELETE CASCADE;

-- =====================================
-- MESSAGE_TEXT
-- =====================================
ALTER TABLE message_text
ADD CONSTRAINT fk_message_conversation
FOREIGN KEY (conversation_id)
REFERENCES conversations(id)
ON DELETE CASCADE;

ALTER TABLE message_text
ADD CONSTRAINT fk_message_sender
FOREIGN KEY (sender_id)
REFERENCES users(id)
ON DELETE CASCADE;

ALTER TABLE conversations
ADD CONSTRAINT fk_conversation_last_message
FOREIGN KEY (last_message_id)
REFERENCES message_text(id)
ON DELETE SET NULL;

-- =====================================
-- EMOJI POSTS
-- =====================================
ALTER TABLE emoji_posts
ADD CONSTRAINT fk_emoji_post
FOREIGN KEY (post_id)
REFERENCES posts(id)
ON DELETE CASCADE;

ALTER TABLE emoji_posts
ADD CONSTRAINT fk_emoji_sender
FOREIGN KEY (sender_id)
REFERENCES users(id)
ON DELETE CASCADE;

-- =====================================
-- ACHIEVEMENTS
-- =====================================
ALTER TABLE achievements
ADD CONSTRAINT fk_achievement_profile
FOREIGN KEY (profile_id)
REFERENCES profiles(id)
ON DELETE CASCADE;