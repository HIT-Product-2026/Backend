ALTER TABLE message_text
RENAME TO messages;

ALTER TABLE messages
ADD COLUMN object_name TEXT;