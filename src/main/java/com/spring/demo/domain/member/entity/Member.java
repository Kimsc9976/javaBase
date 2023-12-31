package com.spring.demo.domain.member.entity;

import com.spring.demo.domain.common.entity.BaseEntity;
import com.spring.demo.domain.member.constant.MemberType;
import com.spring.demo.domain.member.constant.Role;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@EntityScan
@Getter
@NoArgsConstructor
public class Member extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 128, nullable = false)
    private String email;

    @Column
    private String password;

    @Column
    private String name;

    @Column
    private String nickname;

    @Column(columnDefinition = "LONGTEXT", name="profile_path")
    private String profilePath;

    @Column
    private String refreshToken;

    @Column
    private Boolean status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Role role;

    @Column
    private Integer dailyStoryCount;

    @Column
    private Integer reportedCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MemberType memberType;

    @Column
    private String fcmToken;

//    private LocalDateTime tokenExpirationTime;

//    @OneToMany(mappedBy = "member")
//    private List<Story> memberStories = new ArrayList<>();
//
//    @OneToMany(mappedBy = "member")
//    private List<FireBaseMessage> messages = new ArrayList<>();
//
//    @OneToMany(mappedBy = "member")
//    private List<MemberLikeStory> memberLikedStories = new ArrayList<>();
//
//    @OneToMany(mappedBy = "member")
//    private List<StoryBoxMember> storyBoxes = new ArrayList<>();

    @Builder
    public Member(String email, String password, String nickname, String profilePath, Role role, MemberType memberType){
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profilePath = profilePath;
        this.role = role;
        this.memberType = memberType;
        this.status = true;
        this.dailyStoryCount = 0;
        this.reportedCount = 0;
        this.fcmToken = "";
    }

    public void updateMemberToken(String refreshToken){
        this.refreshToken = refreshToken;
    }

    public void updateFCMToken(String fcmToken){
        this.fcmToken = fcmToken;
    }

    public void updatePassword(String password){
        this.password = password;
    }
    public void updateProfile(String email, String nickname, String profilePath){
        this.email = email;
        this.nickname = nickname;
        this.profilePath = profilePath;
    }
    public void updateReportCount(Integer reportedCount){this.reportedCount = reportedCount;}

    public void updateDailyStoryCount(Integer dailyStory){this.dailyStoryCount =dailyStory;}

    public int dailyStoryMaximum(){
        int limit;
        if (this.role.equals(Role.USER)){
            limit = 5;
        } else if (this.role.equals(Role.SUBSCRIBER)) {
            limit = 6;
        }else{ // this.role.equals(Role.ADMIN)
            limit = Integer.MAX_VALUE;
        }
        return limit;
    }

    public void withdrawMember(){
        this.email = appendTimestampForDeletion(this.email);
        this.name = null;
        this.profilePath = null;
        this.status = false;
        this.refreshToken = null;
    }

    private static String appendTimestampForDeletion(String email) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String timestamp = LocalDateTime.now().format(formatter);
        return timestamp + "_dt_" + email;
    }
}
