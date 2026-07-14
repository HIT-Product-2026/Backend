CREATE INDEX idx_posts_user
ON posts(user_id);

CREATE INDEX idx_post_user_created
ON posts(user_id, created_at DESC);

CREATE INDEX idx_profile_latest_post
ON profiles(latest_post_id);

CREATE INDEX idx_profile_favorite_post
ON profiles(favorite_post_id);

CREATE INDEX idx_reup_post
ON profile_reup_posts(post_id);

CREATE INDEX idx_friendship_requester
ON friendships(requester_id);

CREATE INDEX idx_friendship_receiver
ON friendships(receiver_id);

CREATE INDEX idx_conversation_user1
ON conversations(user1_id);

CREATE INDEX idx_conversation_user2
ON conversations(user2_id);

CREATE INDEX idx_message_conversation
ON message_text(conversation_id);

CREATE INDEX idx_message_sender
ON message_text(sender_id);

CREATE INDEX idx_message_created_at
ON message_text(created_at);

CREATE INDEX idx_emoji_post
ON emoji_posts(post_id);

CREATE INDEX idx_emoji_sender
ON emoji_posts(sender_id);

CREATE INDEX idx_profile
ON achievements(profile_id);