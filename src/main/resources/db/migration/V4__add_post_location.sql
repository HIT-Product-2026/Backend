CREATE EXTENSION IF NOT EXISTS postgis;


ALTER TABLE posts
    ADD COLUMN location geography(Point,4326);


UPDATE posts
SET location = ST_SetSRID(
        ST_MakePoint(longitude, latitude),
        4326
               )::geography
WHERE latitude IS NOT NULL
  AND longitude IS NOT NULL;


CREATE INDEX idx_posts_location
    ON posts
    USING GIST(location);