-- Recent Chats View
CREATE VIEW recent_chats AS
SELECT
    cr.room_id,
    cr.last_message,
    cr.last_message_time,
    u.user_id,
    u.username,
    u.profile_image_url,
    u.is_online,
    COALESCE(unread.unread_count, 0) as unread_count
FROM chat_rooms cr
JOIN room_participants rp ON cr.room_id = rp.room_id
JOIN users u ON rp.user_id = u.user_id
LEFT JOIN (
    SELECT room_id, COUNT(*) as unread_count
    FROM messages
    WHERE is_read = FALSE
    GROUP BY room_id
) unread ON cr.room_id = unread.room_id;

-- User Status View
CREATE VIEW user_status_view AS
SELECT
    u.user_id,
    u.username,
    u.is_online,
    u.last_active,
    COALESCE(us.status_text, '') as status_text
FROM users u
LEFT JOIN user_status us ON u.user_id = us.user_id
WHERE (us.id IS NULL) OR
      (us.id IN (
          SELECT MAX(id)
          FROM user_status
          GROUP BY user_id
      ));