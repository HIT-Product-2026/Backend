package com.example.app.unit;


import com.example.lockly.config.minio.MinioProperties;
import com.example.lockly.domain.dto.response.LocationUserResponseDto;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.entity.main.Friendship;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.FriendshipStatus;
import com.example.lockly.domain.entity.main.enumEntity.UserMode;
import com.example.lockly.repository.main.FriendshipsRepository;
import com.example.lockly.repository.main.UserRepository;
import com.example.lockly.service.MinIOService;
import com.example.lockly.service.RedisService;
import com.example.lockly.service.Impl.UserServiceImpl;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.minio.PutObjectArgs;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import com.example.lockly.domain.dto.response.common.UserSimpleResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import com.example.lockly.mapper.user.UserResponseMapper;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserResponseMapper userResponseMapper;

    @Mock
    private FriendshipsRepository friendshipsRepository;

    @Mock
    private MinIOService minIOService;

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioProperties props;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private User friend1;
    private User friend2;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(UUID.randomUUID())
                .username("user")
                .displayName("User")
                .email("user@test.com")
                .passwordHash("password")
                .mode(UserMode.PUBLIC)
                .build();

        friend1 = User.builder()
                .id(UUID.randomUUID())
                .username("friend1")
                .displayName("Friend One")
                .email("friend1@test.com")
                .passwordHash("password")
                .mode(UserMode.PUBLIC)
                .fcmToken("token-1")
                .build();

        friend2 = User.builder()
                .id(UUID.randomUUID())
                .username("friend2")
                .displayName("Friend Two")
                .email("friend2@test.com")
                .passwordHash("password")
                .mode(UserMode.PUBLIC)
                .fcmToken("token-2")
                .build();
    }

    private Friendship requesterFriendship(User requester, User receiver) {
        return Friendship.builder()
                .requester(requester)
                .receiver(receiver)
                .status(FriendshipStatus.ACCEPTED)
                .build();
    }

    private Friendship receiverFriendship(User requester, User receiver) {
        return Friendship.builder()
                .requester(requester)
                .receiver(receiver)
                .status(FriendshipStatus.ACCEPTED)
                .build();
    }

    @Test
    void findFriendsByUser_ShouldReturnAllAcceptedFriends() {

        Friendship friendship1 = requesterFriendship(user, friend1);
        Friendship friendship2 = receiverFriendship(friend2, user);


        when(friendshipsRepository.findByRequesterAndStatus(
                user,
                FriendshipStatus.ACCEPTED
        ))
                .thenReturn(List.of(friendship1));


        when(friendshipsRepository.findByReceiverAndStatus(
                user,
                FriendshipStatus.ACCEPTED
        ))
                .thenReturn(List.of(friendship2));


        when(userResponseMapper.from(friend1))
                .thenReturn(
                        new UserResponseDto(
                                friend1.getId(),
                                friend1.getUsername(),
                                friend1.getDisplayName(),
                                friend1.getMode(),
                                null
                        )
                );


        when(userResponseMapper.from(friend2))
                .thenReturn(
                        new UserResponseDto(
                                friend2.getId(),
                                friend2.getUsername(),
                                friend2.getDisplayName(),
                                friend2.getMode(),
                                null
                        )
                );


        List<UserResponseDto> result =
                userService.findFriendsByUser(user);



        assertEquals(2, result.size());


        assertTrue(
                result.stream()
                        .anyMatch(
                                f -> f.id().equals(friend1.getId())
                        )
        );


        assertTrue(
                result.stream()
                        .anyMatch(
                                f -> f.id().equals(friend2.getId())
                        )
        );
    }

    @Test
    void isFriendByUserId_ShouldReturnTrue_WhenFriendExists() {

        Friendship friendship =
                requesterFriendship(user, friend1);



        when(friendshipsRepository.findByRequesterAndStatus(
                user,
                FriendshipStatus.ACCEPTED
        ))
                .thenReturn(List.of(friendship));



        when(friendshipsRepository.findByReceiverAndStatus(
                user,
                FriendshipStatus.ACCEPTED
        ))
                .thenReturn(List.of());



        when(userResponseMapper.from(friend1))
                .thenReturn(
                        new UserResponseDto(
                                friend1.getId(),
                                friend1.getUsername(),
                                friend1.getDisplayName(),
                                friend1.getMode(),
                                null
                        )
                );



        boolean result =
                userService.isFriendByUserId(
                        user,
                        friend1.getId()
                );



        assertTrue(result);
    }

    @Test
    void isFriendByUserId_ShouldReturnFalse_WhenFriendDoesNotExist() {

        Friendship friendship =
                requesterFriendship(user, friend1);



        when(friendshipsRepository.findByRequesterAndStatus(
                user,
                FriendshipStatus.ACCEPTED
        ))
                .thenReturn(List.of(friendship));



        when(friendshipsRepository.findByReceiverAndStatus(
                user,
                FriendshipStatus.ACCEPTED
        ))
                .thenReturn(List.of());



        when(userResponseMapper.from(friend1))
                .thenReturn(
                        new UserResponseDto(
                                friend1.getId(),
                                friend1.getUsername(),
                                friend1.getDisplayName(),
                                friend1.getMode(),
                                null
                        )
                );



        boolean result =
                userService.isFriendByUserId(
                        user,
                        UUID.randomUUID()
                );



        assertFalse(result);
    }

    @Test
    void updateAvatarByUserId_ShouldUploadAvatarAndSaveUser() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "image-content".getBytes()
        );

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        when(props.getBucketName())
                .thenReturn("bucket");

        userService.updateAvatarByUserId(user, file);

        ArgumentCaptor<PutObjectArgs> putObjectCaptor =
                ArgumentCaptor.forClass(PutObjectArgs.class);

        verify(minioClient).putObject(putObjectCaptor.capture());

        verify(userRepository).save(user);

        assertNotNull(user.getObjectNameAvatar());
    }

    @Test
    void isUserOnlineByUserId_ShouldReturnTrue() {

        LocationUserResponseDto location =
                new LocationUserResponseDto(
                        user.getId(),
                        10.0,
                        20.0,
                        LocalDateTime.now()
                );

        when(redisService.getUserLocation(user.getId()))
                .thenReturn(location);

        boolean result = userService.isUserOnlineByUserId(user);

        assertTrue(result);
    }

    @Test
    void findFcmTokenOfFriendsByUserId_ShouldReturnFriendTokens() {

        Friendship friendship1 = requesterFriendship(user, friend1);
        Friendship friendship2 = receiverFriendship(friend2, user);

        when(friendshipsRepository.findByRequesterAndStatus(
                user,
                FriendshipStatus.ACCEPTED
        )).thenReturn(List.of(friendship1));

        when(friendshipsRepository.findByReceiverAndStatus(
                user,
                FriendshipStatus.ACCEPTED
        )).thenReturn(List.of(friendship2));

        List<String> result =
                userService.findFcmTokenOfFriendsByUserId(user);

        assertEquals(2, result.size());
        assertEquals("token-1", result.get(0));
        assertEquals("token-2", result.get(1));
    }

    @Test
    void updateDisplayNameByUserId_ShouldUpdateDisplayName() {

        String newDisplayName = "New Display Name";

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        userService.updateDisplayNameByUserId(user, newDisplayName);

        assertEquals(newDisplayName, user.getDisplayName());

        verify(userRepository).save(user);
    }

    @Test
    void updateModeByUserId_ShouldUpdateMode() {

        userService.updateModeByUserId(user, UserMode.PRIVATE);

        assertEquals(UserMode.PRIVATE, user.getMode());

        verify(userRepository).save(user);
    }

    @Test
    void updateFcmTokenByUserId_ShouldUpdateFcmToken() {

        String token = "new-fcm-token";

        userService.updateFcmTokenByUserId(user, token);

        assertEquals(token, user.getFcmToken());

        verify(userRepository).save(user);
    }

    @Test
    void getAvatar_ShouldReturnPresignedUrl() {

        user.setObjectNameAvatar("users/avatar/avatar.png");

        when(minIOService.generatePresignedUrl(user.getObjectNameAvatar()))
                .thenReturn("https://minio/avatar");

        String result = userService.getAvatar(user);

        assertEquals("https://minio/avatar", result);

        verify(minIOService).generatePresignedUrl(user.getObjectNameAvatar());
    }

    @Test
    void searchFriend_ShouldReturnUserSimpleResponseDtos() {

        User stranger = User.builder()
                .id(UUID.randomUUID())
                .username("stranger")
                .displayName("Stranger")
                .email("stranger@test.com")
                .passwordHash("password")
                .build();

        Page<User> page = new PageImpl<>(List.of(stranger));

        when(userRepository.searchStrangers(
                eq(user.getId()),
                eq("str"),
                any(Pageable.class)
        )).thenReturn(page);

        List<UserSimpleResponseDto> result =
                userService.searchFriend(user, "str");

        assertEquals(1, result.size());
        assertEquals(stranger.getId(), result.get(0).userId());
        assertEquals(stranger.getDisplayName(), result.get(0).displayName());

        verify(userRepository).searchStrangers(
                eq(user.getId()),
                eq("str"),
                any(Pageable.class)
        );
    }
}