package Backend.BangWool.member.domain;

import Backend.BangWool.util.CONSTANT;
import jakarta.persistence.*;
import lombok.*;

import java.net.URI;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int memberID;

    @Column(unique = true, nullable = false)
    private String email;

    @Setter
    private String password;

    @Column(nullable = false)
    private String name;

    @Setter
    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(nullable = false)
    private LocalDate birth;

    @Setter
    private String googleId;
    @Setter
    private String kakaoId;

    @Setter
    private URI profileImage;


    @Builder
    public MemberEntity(String email, String password, String name, String nickname, LocalDate birth, String googleId, String kakaoId) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.birth = birth;
        this.googleId = googleId;
        this.kakaoId = kakaoId;
        this.profileImage = CONSTANT.DEFAULT_PROFILE;
    }

}
